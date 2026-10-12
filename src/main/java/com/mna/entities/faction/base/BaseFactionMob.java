 
package com.mna.entities.faction.base;

import com.google.common.collect.Maps;
import com.mna.api.entities.IFactionEnemy;
import com.mna.entities.IAnimPacketSync;
import com.mna.entities.ai.RetaliateOnAttackGoal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Tuple;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import org.apache.commons.lang3.mutable.MutableInt;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class BaseFactionMob<T extends BaseFactionMob<?>>
extends Monster
implements IFactionEnemy<T>,
GeoEntity,
IAnimPacketSync<T> {
    private static final UUID TierHealthID = UUID.fromString("76082490-0ded-11ee-be56-0242ac120002");
    private static final UUID TierArmorID = UUID.fromString("79474e2e-0ded-11ee-be56-0242ac120002");
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(BaseFactionMob.class, (EntityDataSerializer)EntityDataSerializers.INT);
    protected Player raidTarget;
    protected int tier;
    protected HashMap<String, Integer> damageResists;
    protected AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private final Map<String, Tuple<MutableInt, Runnable>> timers = Maps.newHashMap();
    private final Map<Integer, Integer> cooldowns = Maps.newHashMap();

    protected BaseFactionMob(EntityType<T> mobType, Level world) {
        super(mobType, world);
        this.damageResists = new HashMap();
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, 0);
    }

    protected void setTimer(String id, int delay, Runnable callback) {
        this.setTimer(id, delay, callback, true);
    }

    protected void setTimer(String id, int delay, Runnable callback, boolean runIfExists) {
        if (runIfExists && this.timers.containsKey(id)) {
            ((Runnable)this.timers.get(id).getB()).run();
        }
        this.timers.put(id, (Tuple<MutableInt, Runnable>)new Tuple(new MutableInt(delay), callback));
    }

    protected void setCooldown(int id, int ticks) {
        this.cooldowns.put(id, ticks);
    }

    protected boolean isOnCooldown(int id) {
        return this.cooldowns.getOrDefault(id, 0) > 0;
    }

    public void tick() {
        super.tick();
        this.cooldowns.keySet().forEach(c -> {
            int cd = this.cooldowns.get(c);
            if (cd > 0) {
                this.cooldowns.put((Integer)c, cd - 1);
            }
        });
        ArrayList timersToRemove = new ArrayList();
        this.timers.forEach((k, v) -> {
            ((MutableInt)this.timers.get(k).getA()).subtract(1);
            if (((MutableInt)this.timers.get(k).getA()).getValue() <= 0) {
                timersToRemove.add(k);
            }
        });
        timersToRemove.forEach(id -> {
            ((Runnable)this.timers.get(id).getB()).run();
            this.timers.remove(id);
        });
    }

    protected int getStateFlag() {
        return (Integer)this.entityData.get(DATA_STATE);
    }

    protected void setState(int ... flags) {
        int finalFlag = 0;
        for (int i : flags) {
            finalFlag |= i;
        }
        this.entityData.set(DATA_STATE, finalFlag);
    }

    protected void registerGoals() {
        this.targetSelector.addGoal(1, (Goal)new RetaliateOnAttackGoal((Mob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, this::factionTargetPlayerPredicate));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, this::factionTargetHelpPredicate));
        this.targetSelector.addGoal(4, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 10, true, false, e -> e instanceof IFactionEnemy && ((IFactionEnemy)e).getFaction() != this.getFaction()));
    }

    public float getStepHeight() {
        return 1.2f;
    }

    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, SpawnGroupData spawnDataIn, CompoundTag dataTag) {
        this.applyInitialSpawnTier((LevelAccessor)worldIn);
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    public boolean canBeLeashed(Player player) {
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, state -> this.handleAnimState(state));
        this.addControllerListeners(controller);
        registrar.add(new AnimationController[]{controller});
    }

    protected void addControllerListeners(AnimationController<T> controller) {
    }

    @Override
    public void setRaidTarget(Player player) {
        this.raidTarget = player;
        this.setTarget((LivingEntity)player);
    }

    @Override
    public Player getRaidTarget() {
        return this.raidTarget;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public void setTier(int tier) {
        this.tier = tier;
        AttributeInstance maxHPAttr = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance maxArmorAttr = this.getAttribute(Attributes.ARMOR);
        maxHPAttr.removePermanentModifier(TierHealthID);
        maxArmorAttr.removePermanentModifier(TierArmorID);
        maxHPAttr.addPermanentModifier(new AttributeModifier(TierHealthID, (double)(tier + 1), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        maxArmorAttr.addPermanentModifier(new AttributeModifier(TierArmorID, (double)(5 * tier), AttributeModifier.Operation.ADD_VALUE));
        this.setHealth(this.getMaxHealth());
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.writeFactionData(compound);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readFactionData(compound);
    }

    @Override
    public HashMap<String, Integer> getDamageResists() {
        return this.damageResists;
    }

    public boolean hurt(DamageSource source, float amount) {
        amount = this.applyDamageResists(source, amount);
        return super.hurt(source, amount);
    }

    public void checkDespawn() {
        super.checkDespawn();
        this.raidTargetDespawn();
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void die(DamageSource cause) {
        super.die(cause);
        this.onKilled(cause);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.tier = additionalData.readInt();
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.tier);
    }

    protected abstract PlayState handleAnimState(AnimationState<? extends BaseFactionMob<?>> var1);
}


