 
package com.mna.entities.faction.util;

import com.mna.tools.MATools;
import com.mna.Registries;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.FactionRaidRegistry;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.tools.math.MathUtils;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class FactionWar
extends Mob {
    public static final int MAXIMUM_STRENGTH = 300;
    public static final int MINIMUM_STRENGTH = 30;
    int strength_first = 0;
    int strength_second = 0;
    boolean calculatedStrength = false;
    IFaction first;
    IFaction second;

    public FactionWar(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void tick() {
        if (!this.calculatedStrength) {
            this.calculateStrength();
        }
        if (this.level().isClientSide()) {
            return;
        }
        if (this.first == null || this.second == null || this.strength_first <= 0 && this.strength_second <= 0) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }
        if (this.level().getGameTime() % 10L != 0L) {
            return;
        }
        if (this.strength_first > 0) {
            this.strength_first = this.spawnRaidEntity(this.first, this.strength_first);
        }
        if (this.strength_second > 0) {
            this.strength_second = this.spawnRaidEntity(this.second, this.strength_second);
        }
    }

    public static boolean checkMonsterSpawnRules(EntityType<FactionWar> pType, ServerLevelAccessor pLevel, MobSpawnType pReason, BlockPos pPos, RandomSource pRandom) {
        if (IFactionEnemy.canMnASpawn((LevelAccessor)pLevel, pPos, true)) {
            return pLevel.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn((ServerLevelAccessor)pLevel, (BlockPos)pPos, (RandomSource)pRandom) && FactionWar.checkMobSpawnRules(pType, (LevelAccessor)pLevel, (MobSpawnType)pReason, (BlockPos)pPos, (RandomSource)pRandom);
        }
        return false;
    }

    private int spawnRaidEntity(IFaction faction, int strength) {
        Pair<EntityType<? extends IFactionEnemy<? extends Mob>>, Integer> soldier = FactionRaidRegistry.getSoldier(faction, strength);
        if (soldier == null) {
            return 0;
        }
        IFactionEnemy entity = (IFactionEnemy)((EntityType)soldier.getFirst()).create(this.level());
        entity.setTier((Integer)soldier.getSecond());
        Vec3 spawnPos = this.position();
        for (int i = 0; i < 10; ++i) {
            BlockPos testSpawnPos = this.blockPosition().offset((int)(-8.0 + Math.random() * 16.0), (int)(-8.0 + Math.random() * 16.0), (int)(-8.0 + Math.random() * 16.0));
            int adjustCount = 0;
            while (this.level().isEmptyBlock(testSpawnPos) && adjustCount++ < 10) {
                testSpawnPos = testSpawnPos.below();
            }
            if (this.level().isEmptyBlock(testSpawnPos)) continue;
            adjustCount = 0;
            while (!this.level().isEmptyBlock(testSpawnPos) && adjustCount++ < 10) {
                testSpawnPos = testSpawnPos.above();
            }
            if (!this.level().isEmptyBlock(testSpawnPos)) continue;
            spawnPos = Vec3.atBottomCenterOf((Vec3i)testSpawnPos);
            break;
        }
        LivingEntity living = (LivingEntity)entity;
        living.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 3));
        if ((MATools.isUndead(living)) && this.level().isDay()) {
            living.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()), 300, 0));
        }
        this.level().addFreshEntity((Entity)entity);
        int entity_strength_rating = FactionRaidRegistry.getStrengthRating(faction, (EntityType<? extends IFactionEnemy<? extends Mob>>)((EntityType)soldier.getFirst()), (Integer)soldier.getSecond());
        if (entity_strength_rating == -1) {
            return 0;
        }
        return strength - entity_strength_rating;
    }

    private void calculateStrength() {
        this.calculatedStrength = true;
        MutableInt totalNearbyTier = new MutableInt(0);
        MutableInt totalNearbyLevel = new MutableInt(0);
        AABB bb = this.getBoundingBox().inflate(128.0);
        this.level().players().forEach(player -> {
            if (bb.intersects(player.getBoundingBox())) {
                MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> totalNearbyLevel.add(m.getMagicLevel()));
                MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> totalNearbyTier.add(p.getTier()));
            }
        });
        if (totalNearbyTier.getValue() == 0 || totalNearbyLevel.getValue() == 0) {
            this.strength_first = 0;
            this.strength_second = 0;
            return;
        }
        this.strength_first = totalNearbyLevel.getValue() / 3 * totalNearbyTier.getValue();
        this.strength_second = this.strength_first = MathUtils.clamp(this.strength_first, 30, 300);
    }

    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.calculatedStrength) {
            this.calculateStrength();
            List<IFaction> allFactions = ManaAndArtificeMod.getFactionHelper().getAllFactions();
            this.first = allFactions.get((int)(Math.random() * (double)allFactions.size()));
            List<IFaction> otherFactions = ManaAndArtificeMod.getFactionHelper().getFactionsExcept(this.first);
            this.second = otherFactions.get((int)(Math.random() * (double)otherFactions.size()));
        }
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.strength_first = pCompound.getInt("strength_first");
        this.strength_second = pCompound.getInt("strength_second");
        if (pCompound.contains("first") && pCompound.contains("second")) {
            this.first = ManaAndArtificeMod.getFactionHelper().getFaction(ResourceLocation.parse(pCompound.getString("first")));
            this.second = ManaAndArtificeMod.getFactionHelper().getFaction(ResourceLocation.parse(pCompound.getString("second")));
            this.calculatedStrength = this.first != null && this.second != null;
        }
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("strength_first", this.strength_first);
        pCompound.putInt("strength_second", this.strength_second);
        if (this.first != null) {
            pCompound.putString("first", Registries.Factions.getKey(this.first).toString());
        }
        if (this.second != null) {
            pCompound.putString("second", Registries.Factions.getKey(this.second).toString());
        }
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ATTACK_DAMAGE, 0.0).add(Attributes.ATTACK_SPEED, 0.0).add(Attributes.ATTACK_KNOCKBACK, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    public boolean canBeAffected(MobEffectInstance pPotioneffect) {
        return false;
    }

    public boolean canChangeDimensions() {
        return false;
    }

    public boolean canBeSeenByAnyone() {
        return false;
    }

    public boolean mnaCanBreatheUnderwater() {
        return true;
    }

    public boolean fireImmune() {
        return true;
    }

    public void setSecondsOnFire(int pSeconds) {
    }

    public boolean attackable() {
        return false;
    }

    public boolean canFreeze() {
        return false;
    }

    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }
}


