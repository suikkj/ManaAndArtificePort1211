 
package com.mna.capabilities.playerdata.progression;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.capabilities.CodexBreadcrumb;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.entities.FactionRaidRegistry;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.FactionDifficultyStats;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.progression.ProgressionEventHandler;
import com.mna.recipes.progression.ProgressionCondition;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class PlayerProgression
implements IPlayerProgression {
    public static final int MAX_TIERS = 5;
    public static final double RAID_IRE = 1.0;
    private boolean has_synced = false;
    private int tier = 1;
    private IFaction allied_faction = null;
    private int faction_standing;
    private List<ResourceLocation> metTierProgressions;
    private HashMap<Long, Boolean> codex_entry_status;
    private int ally_cooldown;
    private HashMap<IFaction, Double> raidChances = new HashMap();
    private ArrayList<IFaction> forcedRaids;
    private boolean dirty;
    private LinkedList<CodexBreadcrumb> _breadcrumbs;
    private HashMap<IFaction, FactionDifficultyStats> factionDifficultyStats;

    public PlayerProgression() {
        this.codex_entry_status = new HashMap();
        this.factionDifficultyStats = new HashMap();
        this.metTierProgressions = new ArrayList<ResourceLocation>();
        this._breadcrumbs = new LinkedList();
        this.forcedRaids = new ArrayList();
        this.setDirty();
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public void setTier(int tier, @Nullable Player player, boolean notifyTierRote) {
        if (this.tier == tier) {
            return;
        }
        if (player != null) {
            AttributeModifier modifier;
            int modifierAmount;
            int index;
            int t;
            AttributeInstance inst = player.getAttribute(Attributes.MAX_HEALTH);
            for (t = 1; t <= 5; ++t) {
                index = t - 1;
                modifierAmount = GeneralConfigValues.TierHealthBoosts.size() >= index ? GeneralConfigValues.TierHealthBoosts.get(index) : 0;
                modifier = new AttributeModifier(UUID.fromString(Tier_Health_Boost_IDs[index]), "Tier Health Boost " + t, (double)modifierAmount, AttributeModifier.Operation.ADD_VALUE);
                if (!inst.hasModifier(modifier)) continue;
                inst.removeModifier(modifier.getId());
            }
            for (t = 1; t <= 5; ++t) {
                index = t - 1;
                modifierAmount = GeneralConfigValues.TierHealthBoosts.size() >= index ? GeneralConfigValues.TierHealthBoosts.get(index) : 0;
                modifier = new AttributeModifier(UUID.fromString(Tier_Health_Boost_IDs[index]), "Tier Health Boost " + t, (double)modifierAmount, AttributeModifier.Operation.ADD_VALUE);
                if (modifierAmount <= 0 || tier < t || inst.hasModifier(modifier)) continue;
                inst.addPermanentModifier(modifier);
            }
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
        this.tier = MathUtils.clamp(tier, 1, 5);
        this.metTierProgressions.clear();
        if (player != null && player instanceof ServerPlayer) {
            CustomAdvancementTriggers.TIER_CHANGE.trigger((ServerPlayer)player, tier);
        }
        int belowTier = GeneralConfigValues.AutoRoteLowTier;
        int roteTier = this.tier - belowTier;
        if (player != null && roteTier > 0) {
            MNACap.get(player, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
                Registries.Shape.getValues().forEach(s -> {
                    if (s.getTier(player.level()) <= roteTier) {
                        r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote());
                    }
                });
                Registries.SpellEffect.getValues().forEach(s -> {
                    if (s.getTier(player.level()) <= roteTier) {
                        r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote());
                    }
                });
                Registries.Modifier.getValues().forEach(s -> {
                    if (s.getTier(player.level()) <= roteTier) {
                        r.addRoteXP(null, (ISpellComponent)s, s.requiredXPForRote());
                    }
                });
                if (!player.level().isClientSide() && notifyTierRote) {
                    player.sendSystemMessage((Component)Component.translatable((String)"mna:progresscondition.tier_rote", (Object[])new Object[]{roteTier}));
                }
            });
        }
        if (player != null) {
            ProgressionEventHandler.confirmExistingAdvancements(player);
        }
        this.setDirty();
    }

    @Override
    public IFaction getAlliedFaction() {
        return this.allied_faction;
    }

    @Override
    public boolean hasAlliedFaction() {
        return this.allied_faction != null;
    }

    @Override
    public void setAlliedFaction(@Nullable IFaction faction, @Nullable Player player) {
        this.allied_faction = faction;
        if (player != null) {
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                if (faction != null) {
                    boolean curResourceIsForFaction = Arrays.asList(faction.getCastingResources()).contains(magic.getCastingResource().getRegistryName());
                    if (magic.getCastingResource() == null || !curResourceIsForFaction) {
                        magic.setCastingResourceType(faction.getCastingResource(player));
                    }
                } else {
                    magic.setCastingResourceType(CastingResourceIDs.MANA);
                }
            });
            if (player instanceof ServerPlayer) {
                CustomAdvancementTriggers.FACTION_JOIN.trigger((ServerPlayer)player, faction == null ? null : Registries.Factions.getKey(faction));
            }
        }
        this.setDirty();
    }

    @Override
    public int getFactionStanding() {
        return this.faction_standing;
    }

    @Override
    public void setFactionStanding(int standing) {
        this.faction_standing = standing;
        this.setDirty();
    }

    @Override
    public void increaseFactionStanding(int amount) {
        this.faction_standing += amount;
        this.setDirty();
    }

    @Override
    public boolean needsSync() {
        return this.dirty;
    }

    @Override
    public void clearSyncFlag() {
        this.dirty = false;
    }

    @Override
    public void addTierProgressionComplete(ResourceLocation complededCondition) {
        if (!this.metTierProgressions.contains(complededCondition)) {
            this.metTierProgressions.add(complededCondition);
            this.setDirty();
        }
    }

    @Override
    public float getTierProgress(Level world) {
        int req = ProgressionCondition.getCompletionRequirementForTier(world, this.getTier());
        return MathUtils.clamp01((float)this.metTierProgressions.size() / (float)req);
    }

    @Override
    public List<ResourceLocation> getCompletedProgressionSteps() {
        return this.metTierProgressions;
    }

    @Override
    public void setTierProgression(List<ResourceLocation> complededCondition) {
        this.metTierProgressions.clear();
        this.metTierProgressions.addAll(complededCondition);
        this.setDirty();
    }

    @Override
    public void setDirty() {
        this.dirty = true;
    }

    @Override
    public double getRaidChance(IFaction faction) {
        if (this.forcedRaids.contains(faction)) {
            return 1.0;
        }
        return this.raidChances.getOrDefault(faction, 0.0);
    }

    @Override
    public void setRaidChance(IFaction faction, double chance) {
        this.raidChances.put(faction, chance);
    }

    @Override
    public int getRelativeRaidStrength(IFaction faction, Player player) {
        if (this.hasForceRaid()) {
            return GeneralConfigValues.ForcedRaidStrength;
        }
        int strength = 0;
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        if (magic != null) {
            strength = (int)((double)magic.getMagicLevel() * 1.5);
        }
        return strength *= this.tier;
    }

    @Override
    public boolean canBeRaided(Player player) {
        if (this.hasForceRaid()) {
            return true;
        }
        return !player.isCreative() && this.getAlliedFaction() != null && Registries.Factions.getValues().stream().anyMatch(f -> this.canBeRaided((IFaction)f, player));
    }

    @Override
    public boolean canBeRaided(IFaction faction, Player player) {
        if (this.hasForceRaid()) {
            return true;
        }
        return this.raidChances.getOrDefault(faction, 0.0) >= 1.0;
    }

    @Override
    public boolean hasForceRaid() {
        return this.forcedRaids.size() > 0;
    }

    @Override
    public void raidImmediate(IFaction faction) {
        if (Registries.Factions.containsValue(faction)) {
            this.forcedRaids.add(faction);
        }
    }

    @Override
    @Nullable
    public IFaction getForceRaid() {
        if (this.forcedRaids.size() > 0) {
            return this.forcedRaids.get(0);
        }
        return null;
    }

    @Override
    public void clearForceRaid() {
        if (this.forcedRaids.size() > 0) {
            this.forcedRaids.remove(0);
        }
    }

    @Override
    public void incrementFactionAggro(IFaction faction, float min, float max) {
        double chance = this.raidChances.getOrDefault(faction, 0.0) + ((double)min + Math.random() * (double)(max - min));
        this.raidChances.put(faction, chance);
    }

    @Override
    @Nullable
    public CodexBreadcrumb popCodexBreadcrumb() {
        return this._breadcrumbs.pollLast();
    }

    @Override
    @Nullable
    public CodexBreadcrumb peekCodexBreadcrumb() {
        return this._breadcrumbs.peekLast();
    }

    @Override
    public int breadcrumbLength() {
        return this._breadcrumbs.size();
    }

    @Override
    public void clearCodexEntryHistory() {
        this._breadcrumbs.clear();
    }

    @Override
    public void pushCodexBreadcrumb(CodexBreadcrumb.breadcrumbType type, String key, int page, String ... metadata) {
        if (!this._breadcrumbs.stream().anyMatch(c -> c.breadcrumbType == type && c.Key == key && c.Page == page)) {
            this._breadcrumbs.add(new CodexBreadcrumb(type, key, page, metadata));
        }
    }

    @Override
    public int getTierMaxComplexity() {
        if (this.getTier() == 5) {
            return GeneralConfigValues.Tier5ComplexityLimit;
        }
        return 20 * this.getTier();
    }

    @Override
    public FactionDifficultyStats getFactionDifficultyStats(IFaction faction) {
        if (!this.factionDifficultyStats.containsKey(faction)) {
            this.factionDifficultyStats.put(faction, new FactionDifficultyStats());
        }
        return this.factionDifficultyStats.get(faction);
    }

    @Override
    public void tickClassicRaids(Player player) {
        if (!GeneralConfigValues.ClassicRaids || this.getAlliedFaction() == null || this.getTier() < 3) {
            return;
        }
        if (player.level().getGameTime() % 12000L == 0L) {
            double base_increase = GeneralConfigValues.RaidChanceBase;
            double tier_increase = GeneralConfigValues.RaidChanceTier * (double)(this.getTier() - 3);
            List allFactions = Registries.Factions.getValues().stream().toList();
            IFaction anger = (IFaction)allFactions.get((int)Math.random() * allFactions.size());
            if (anger == null || anger == this.getAlliedFaction()) {
                return;
            }
            float aggro_amount = (float)(base_increase + tier_increase);
            this.incrementFactionAggro(anger, aggro_amount, aggro_amount);
        }
    }

    @Override
    public int getAllyCooldown() {
        return this.ally_cooldown;
    }

    @Override
    public void setAllyCooldown(int cooldown) {
        this.ally_cooldown = cooldown;
    }

    @Override
    public void summonRandomAlly(Player ally) {
        Pair<EntityType<? extends IFactionEnemy<? extends Mob>>, Integer> soldier = FactionRaidRegistry.getSoldier(this.getAlliedFaction(), 20 * this.getTier());
        if (soldier == null) {
            return;
        }
        IFactionEnemy entity = (IFactionEnemy)((EntityType)soldier.getFirst()).create(ally.level());
        SummonUtils.setSummon((Mob)entity, (LivingEntity)ally, 1200);
        entity.setTier((Integer)soldier.getSecond());
        ((LivingEntity)entity).setPos(ally.getX(), ally.getY(), ally.getZ());
        ((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 3));
        ally.level().addFreshEntity((Entity)entity);
        this.setAllyCooldown(6000);
    }

    @Override
    public boolean performedInitialSync() {
        return this.has_synced;
    }

    @Override
    public void setHasSynced() {
        this.has_synced = true;
    }

    @Override
    public boolean isCodexEntryRead(long hash) {
        return this.codex_entry_status.getOrDefault(hash, false);
    }

    @Override
    public boolean isCodexEntryUnlocked(long hash) {
        return this.codex_entry_status.containsKey(hash);
    }

    @Override
    public void setCodexEntryUnlocked(long hash) {
        if (!this.codex_entry_status.containsKey(hash)) {
            this.codex_entry_status.put(hash, false);
        }
    }

    @Override
    public boolean setCodexEntryRead(long hash) {
        if (!this.codex_entry_status.getOrDefault(hash, false).booleanValue()) {
            this.codex_entry_status.put(hash, true);
            return true;
        }
        return false;
    }

    @Override
    public void resetCodexUnlocks() {
        this.codex_entry_status.clear();
    }

    @Override
    public CompoundTag saveCodexUnlocks() {
        CompoundTag data = new CompoundTag();
        long[] keys = new long[this.codex_entry_status.size()];
        byte[] values = new byte[this.codex_entry_status.size()];
        int count = 0;
        for (Map.Entry<Long, Boolean> entry : this.codex_entry_status.entrySet()) {
            keys[count] = entry.getKey();
            values[count] = entry.getValue() != false ? (byte)1 : 0;
            ++count;
        }
        data.putLongArray("keys", keys);
        data.putByteArray("values", values);
        return data;
    }

    @Override
    public void loadCodexUnlocks(CompoundTag data) {
        if (!data.contains("keys", 12) || !data.contains("values", 7)) {
            ManaAndArtifice.LOGGER.error("Missing data when attempting to load codex unlocks.  Aborting.");
            return;
        }
        long[] keys = data.getLongArray("keys");
        byte[] values = data.getByteArray("values");
        this.loadCodexUnlocks(keys, values);
    }

    @Override
    public void loadCodexUnlocks(long[] keys, byte[] values) {
        this.resetCodexUnlocks();
        if (keys.length != values.length) {
            ManaAndArtifice.LOGGER.error("Key/Value Length Mismatch when loading codex unlocks.  Resetting to default and continuing");
            return;
        }
        for (int i = 0; i < keys.length; ++i) {
            this.codex_entry_status.put(keys[i], values[i] == 1);
        }
    }

    @Override
    public void setEntriesUnlocked(long[] hashes) {
        for (long l : hashes) {
            if (this.codex_entry_status.containsKey(l)) continue;
            this.codex_entry_status.put(l, false);
        }
    }

    @Override
    public int unlockedEntryCount() {
        return this.codex_entry_status.size();
    }

    @Override
    public void copyFrom(IPlayerProgression other, Player player) {
        this.setAlliedFaction(other.getAlliedFaction(), null);
        this.setFactionStanding(other.getFactionStanding());
        this.setTier(other.getTier(), player, false);
        this.setTierProgression(other.getCompletedProgressionSteps());
        this.setAllyCooldown(other.getAllyCooldown());
        for (Map.Entry f : Registries.Factions.getEntries()) {
            this.setRaidChance((IFaction)f.getValue(), other.getRaidChance((IFaction)f.getValue()));
            this.getFactionDifficultyStats((IFaction)f.getValue()).readFromNBT(other.getFactionDifficultyStats((IFaction)f.getValue()).writeToNBT());
        }
        this.loadCodexUnlocks(other.saveCodexUnlocks());
    }
}

