 
package com.mna.entities.projectile;

import com.mna.api.sound.SFX;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SkeletonAssassinBolo
extends AbstractArrow
implements GeoEntity {
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public SkeletonAssassinBolo(EntityType<? extends SkeletonAssassinBolo> p_i231584_1_, Level p_i231584_2_) {
        super(p_i231584_1_, p_i231584_2_);
    }

    public SkeletonAssassinBolo(Level world, LivingEntity entity) {
        super((EntityType)EntityInit.SKELETON_ASSASSIN_BOLO.get(), world);
        Vec3 position = entity.position().add(0.0, 1.0, 0.0);
        this.teleportTo(position.x, position.y, position.z);
        this.setOwner((Entity)entity);
        this.pickup = AbstractArrow.Pickup.DISALLOWED;
    }

    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        if (!(entity instanceof LivingEntity) || entity.getType() == EntityInit.SKELETON_ASSASSIN.get() || entity.getType() == EntityInit.HULKING_ZOMBIE.get()) {
            return;
        }
        ((LivingEntity)entity).addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.ENTANGLE.get()), 100));
        this.playSound(SFX.Entity.SkeletonAssassin.SHURIKEN_IMPACT, 1.0f, (float)(0.9 + Math.random() * 0.2));
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    protected void tickDespawn() {
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    public boolean isInGround() {
        return this.inGround;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> PlayState.STOP)});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
}


