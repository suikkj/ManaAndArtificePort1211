 
package com.mna.api.entities;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import java.util.HashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public interface IFactionEnemy<T extends LivingEntity> {
    public void setRaidTarget(Player var1);

    @Nullable
    public Player getRaidTarget();

    public IFaction getFaction();

    default public void setDamageResists(String typeID, int level) {
        this.getDamageResists().put(typeID, level);
    }

    public HashMap<String, Integer> getDamageResists();

    public int getTier();

    public void setTier(int var1);

    default public int getTierMax() {
        return 3;
    }

    default public void raidTargetDespawn() {
        if (this.getRaidTarget() != null) {
            if (!this.getRaidTarget().isAlive()) {
                this.onDespawnDueToTargetDeath();
                ((LivingEntity)this).remove(Entity.RemovalReason.DISCARDED);
            } else if (this.getRaidTarget().distanceToSqr((Entity)this) > 4096.0) {
                this.onDespawnDueToDistance();
                ((LivingEntity)this).remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    default public boolean factionTargetPlayerPredicate(LivingEntity potentialTarget) {
        if (!(potentialTarget instanceof Player)) {
            return false;
        }
        IPlayerProgression progression = (IPlayerProgression)MNACap.get(potentialTarget, ManaAndArtificeMod.getProgressionCapability()).orElse(null);
        if (progression == null) {
            return false;
        }
        return potentialTarget.isAlive() && progression.getAlliedFaction() != null && progression.getAlliedFaction().getEnemyFactions().contains(this.getFaction());
    }

    default public boolean factionTargetHelpPredicate(LivingEntity potentialTarget) {
        if (!(potentialTarget instanceof Mob)) {
            return false;
        }
        Mob mob = (Mob)potentialTarget;
        if (mob instanceof IFactionEnemy && ((IFactionEnemy)mob).getFaction() == this.getFaction()) {
            return false;
        }
        if (mob.isAlive() && mob.getTarget() instanceof Player) {
            MutableBoolean isFaction = new MutableBoolean(false);
            MNACap.get((Player)mob.getTarget(), ManaAndArtificeMod.getProgressionCapability()).ifPresent(p -> {
                IFaction myFaction = this.getFaction();
                IFaction playerFaction = p.getAlliedFaction();
                if (myFaction != null && playerFaction != null && myFaction.isAlliedTo(playerFaction)) {
                    isFaction.setTrue();
                }
            });
            return isFaction.booleanValue();
        }
        return false;
    }

    default public void writeFactionData(CompoundTag nbt) {
        CompoundTag factionData = new CompoundTag();
        factionData.putInt("tier", this.getTier());
        nbt.put("faction_data", (Tag)factionData);
    }

    default public void readFactionData(CompoundTag nbt) {
        if (!nbt.contains("faction_data")) {
            return;
        }
        CompoundTag factionData = nbt.getCompound("faction_data");
        if (factionData.contains("tier")) {
            this.setTier(factionData.getInt("tier"));
        }
    }

    default public void onKilled(DamageSource source) {
        if (source.getEntity() instanceof Player) {
            MNACap.get((Player)source.getEntity(), ManaAndArtificeMod.getProgressionCapability()).ifPresent(p -> p.getFactionDifficultyStats(this.getFaction()).onFactionMobKilled(source));
        }
    }

    default public void onDespawnDueToDistance() {
        if (this.getRaidTarget() != null) {
            MNACap.get(this.getRaidTarget(), ManaAndArtificeMod.getProgressionCapability()).ifPresent(p -> p.getFactionDifficultyStats(this.getFaction()).onFactionMobDespawnDueToDistance());
        }
    }

    default public void onDespawnDueToTargetDeath() {
        if (this.getRaidTarget() != null) {
            MNACap.get(this.getRaidTarget(), ManaAndArtificeMod.getProgressionCapability()).ifPresent(p -> p.getFactionDifficultyStats(this.getFaction()).onFactionKilledPlayer());
        }
    }

    default public float applyDamageResists(DamageSource source, float amount) {
        return amount - amount * ((float)this.getDamageResists().getOrDefault(source.getMsgId(), 0).intValue() * 0.2f);
    }

    default public void applyInitialSpawnTier(LevelAccessor world) {
        int tier;
        MutableInt enemyTiers = new MutableInt(0);
        world.players().forEach(player -> MNACap.get(player, ManaAndArtificeMod.getProgressionCapability()).ifPresent(p -> {
            if (player.distanceToSqr((Entity)((LivingEntity)this)) < 4096.0) {
                enemyTiers.add(p.getTier());
            }
        }));
        float tierTotal = enemyTiers.getValue().intValue();
        double[] weights = new double[]{this.getTierWeight(tierTotal, 0.0f, 8.0f), this.getTierWeight(tierTotal, 3.0f, 4.0f), tierTotal <= 10.0f ? this.getTierWeight(tierTotal, 6.0f, 8.0f) : 1.0};
        double totalWeight = 0.0;
        for (int i = 0; i < weights.length; ++i) {
            totalWeight += weights[i];
        }
        double r = Math.random() * totalWeight;
        for (tier = 0; tier < weights.length - 1 && !((r -= weights[tier]) <= 0.0); ++tier) {
        }
        int difficultyOrdinal = world.getDifficulty().ordinal();
        if (tier > difficultyOrdinal - 1) {
            tier = Math.max(difficultyOrdinal - 1, 0);
        }
        if (tier > 2) {
            tier = 2;
        }
        if (tier < 0) {
            tier = 0;
        }
        this.setTier(tier);
    }

    default public double getTierWeight(float x, float shift, float steepness) {
        return Math.pow(Math.E, -1.0 * (Math.pow(x - shift, 2.0) / (double)steepness));
    }

    public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> pType, ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
        if (IFactionEnemy.canMnASpawn((LevelAccessor)pLevel, pPos)) {
            return pLevel.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn((ServerLevelAccessor)pLevel, (BlockPos)pPos, (RandomSource)pRandom) && Monster.checkMobSpawnRules(pType, (LevelAccessor)pLevel, (MobSpawnType)pSpawnType, (BlockPos)pPos, (RandomSource)pRandom);
        }
        return false;
    }

    public static boolean canMnASpawn(LevelAccessor world, BlockPos pos) {
        return IFactionEnemy.canMnASpawn(world, pos, false);
    }

    public static boolean canMnASpawn(LevelAccessor world, BlockPos pos, boolean ignoreConfig) {
        LevelChunk levelChunk;
        IChunkMagic chunkMagic;
        ChunkAccess chunkAccess;
        if (!ignoreConfig && !GeneralConfigValues.ChunkMagicRequiredForSpawns) {
            return true;
        }
        return world.isAreaLoaded(pos, 1) && (chunkAccess = (net.minecraft.world.level.chunk.ChunkAccess)world.getChunk(pos)) instanceof LevelChunk && (chunkMagic = (IChunkMagic)MNACap.get((LevelChunk)chunkAccess, ChunkMagicProvider.MAGIC).orElse(null)) != null && chunkMagic.getResidualMagic() > 0.0f;
    }
}

