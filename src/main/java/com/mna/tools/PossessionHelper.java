 
package com.mna.tools;

import com.mna.api.entities.possession.PossessionActions;
import com.mna.effects.EffectInit;
import com.mna.network.messages.to_server.PossessionInputMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;

public class PossessionHelper {
    public static void handleRemoteInput(PossessionInputMessage message, Player sendingPlayer, Mob living) {
        switch (message.getType()) {
            case CLICK: {
                if (sendingPlayer.level().isClientSide()) {
                    return;
                }
                if (sendingPlayer.getPersistentData().contains("posession_left_click_time")) {
                    long time = sendingPlayer.getPersistentData().getLong("posession_left_click_time");
                    if (sendingPlayer.level().getGameTime() < time) {
                        return;
                    }
                }
                if (!PossessionActions.Invoke(living) && !PossessionActions.InvokeDefault(living)) {
                    living.playAmbientSound();
                }
                sendingPlayer.getPersistentData().putLong("posession_left_click_time", sendingPlayer.level().getGameTime() + 30L);
                break;
            }
            case MOVEMENT: {
                boolean canFly;
                living.absMoveTo(living.getX(), living.getY(), living.getZ(), message.getYaw(), message.getPitch());
                living.setYHeadRot(message.getYawHead());
                double y = 0.0;
                boolean bl = canFly = living.isNoGravity() || living instanceof FlyingMob || living instanceof Bat || living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LEVITATION.get()));
                if (message.getJump()) {
                    if (living.onGround() && !canFly) {
                        PossessionHelper.jump((LivingEntity)living);
                    } else if (canFly) {
                        living.setNoGravity(true);
                        y = 1.0;
                    }
                } else if (message.getSneak() && canFly) {
                    if (living.onGround()) {
                        living.setNoGravity(false);
                    }
                    y = -1.0;
                }
                living.setSpeed((float)living.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.5f);
                if (living instanceof FlyingMob) {
                    living.travel(new Vec3((double)message.getStrafe(), y, (double)message.getForward()));
                    break;
                }
                PossessionHelper.doTravel((LivingEntity)living, new Vec3((double)message.getStrafe(), y, (double)message.getForward()));
                break;
            }
        }
    }

    public static void doTravel(LivingEntity ent, Vec3 travelVector) {
        double d0 = 0.08;
        d0 = ent.getAttribute(Attributes.GRAVITY).getValue();
        boolean flag = ent.getDeltaMovement().y <= 0.0;
        FluidState fluidstate = ent.level().getFluidState(ent.blockPosition());
        if (ent.isInWater() && !ent.canStandOnFluid(fluidstate)) {
            double d8 = ent.getY();
            float f5 = ent.isSprinting() ? 0.9f : 0.8f;
            float f6 = 0.02f;
            float f7 = 0;
            if (f7 > 3.0f) {
                f7 = 3.0f;
            }
            if (!ent.onGround()) {
                f7 *= 0.5f;
            }
            if (f7 > 0.0f) {
                f5 += (0.54600006f - f5) * f7 / 3.0f;
                f6 += (ent.getSpeed() - f6) * f7 / 3.0f;
            }
            if (ent.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                f5 = 0.96f;
            }
            ent.moveRelative(f6 *= (float)ent.getAttribute(NeoForgeMod.SWIM_SPEED).getValue(), travelVector);
            ent.move(MoverType.SELF, ent.getDeltaMovement());
            Vec3 vector3d6 = ent.getDeltaMovement();
            if (ent.horizontalCollision && ent.onClimbable()) {
                vector3d6 = new Vec3(vector3d6.x, 0.2, vector3d6.z);
            }
            ent.setDeltaMovement(vector3d6.multiply((double)f5, (double)0.8f, (double)f5));
            Vec3 vector3d2 = ent.getFluidFallingAdjustedMovement(d0, flag, ent.getDeltaMovement());
            ent.setDeltaMovement(vector3d2);
            if (ent.horizontalCollision && ent.isFree(vector3d2.x, vector3d2.y + (double)0.6f - ent.getY() + d8, vector3d2.z)) {
                ent.setDeltaMovement(vector3d2.x, (double)0.3f, vector3d2.z);
            }
        } else if (ent.isInLava() && !ent.canStandOnFluid(fluidstate)) {
            double d7 = ent.getY();
            ent.moveRelative(0.02f, travelVector);
            ent.move(MoverType.SELF, ent.getDeltaMovement());
            if (ent.getFluidHeight(FluidTags.LAVA) <= ent.getFluidJumpThreshold()) {
                ent.setDeltaMovement(ent.getDeltaMovement().multiply(0.5, (double)0.8f, 0.5));
                Vec3 vector3d3 = ent.getFluidFallingAdjustedMovement(d0, flag, ent.getDeltaMovement());
                ent.setDeltaMovement(vector3d3);
            } else {
                ent.setDeltaMovement(ent.getDeltaMovement().scale(0.5));
            }
            if (!ent.isNoGravity()) {
                ent.setDeltaMovement(ent.getDeltaMovement().add(0.0, -d0 / 4.0, 0.0));
            }
            Vec3 vector3d4 = ent.getDeltaMovement();
            if (ent.horizontalCollision && ent.isFree(vector3d4.x, vector3d4.y + (double)0.6f - ent.getY() + d7, vector3d4.z)) {
                ent.setDeltaMovement(vector3d4.x, (double)0.3f, vector3d4.z);
            }
        } else if (ent.isFallFlying()) {
            double d10;
            double d6;
            float f2;
            Vec3 vector3d = ent.getDeltaMovement();
            if (vector3d.y > -0.5) {
                ent.fallDistance = 1.0f;
            }
            Vec3 vector3d1 = ent.getLookAngle();
            float f = ent.getXRot() * ((float)Math.PI / 180);
            double d1 = Math.sqrt(vector3d1.x * vector3d1.x + vector3d1.z * vector3d1.z);
            double d3 = Math.sqrt(vector3d.horizontalDistanceSqr());
            double d4 = vector3d1.length();
            float f1 = Mth.cos((float)f);
            f1 = (float)((double)f1 * (double)f1 * Math.min(1.0, d4 / 0.4));
            vector3d = ent.getDeltaMovement().add(0.0, d0 * (-1.0 + (double)f1 * 0.75), 0.0);
            if (vector3d.y < 0.0 && d1 > 0.0) {
                double d5 = vector3d.y * -0.1 * (double)f1;
                vector3d = vector3d.add(vector3d1.x * d5 / d1, d5, vector3d1.z * d5 / d1);
            }
            if (f < 0.0f && d1 > 0.0) {
                double d9 = d3 * (double)(-Mth.sin((float)f)) * 0.04;
                vector3d = vector3d.add(-vector3d1.x * d9 / d1, d9 * 3.2, -vector3d1.z * d9 / d1);
            }
            if (d1 > 0.0) {
                vector3d = vector3d.add((vector3d1.x / d1 * d3 - vector3d.x) * 0.1, 0.0, (vector3d1.z / d1 * d3 - vector3d.z) * 0.1);
            }
            ent.setDeltaMovement(vector3d.multiply((double)0.99f, (double)0.98f, (double)0.99f));
            ent.move(MoverType.SELF, ent.getDeltaMovement());
            if (ent.horizontalCollision && !ent.level().isClientSide() && (f2 = (float)((d6 = d3 - (d10 = Math.sqrt(ent.getDeltaMovement().horizontalDistanceSqr()))) * 10.0 - 3.0)) > 0.0f) {
                ent.playSound(SoundEvents.GENERIC_BIG_FALL, (float)((double)0.9f + Math.random() * (double)0.2f), (float)((double)0.9f + Math.random() * (double)0.2f));
                ent.hurt(ent.damageSources().flyIntoWall(), f2);
            }
            if (ent.onGround() && !ent.level().isClientSide()) {
                ent.setSharedFlag(7, false);
            }
        } else {
            BlockPos blockpos = ent.blockPosition().below();
            float f3 = ent.level().getBlockState(blockpos).getFriction((LevelReader)ent.level(), blockpos, (Entity)ent);
            float f4 = ent.onGround() ? f3 * 0.91f : 0.91f;
            Vec3 vector3d5 = ent.handleRelativeFrictionAndCalculateMovement(travelVector, f3);
            double d2 = vector3d5.y;
            if (ent.hasEffect(MobEffects.LEVITATION)) {
                d2 += (0.05 * (double)(ent.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vector3d5.y) * 0.2;
                ent.fallDistance = 0.0f;
            } else if (ent.level().isClientSide() && !ent.level().isLoaded(blockpos)) {
                d2 = ent.getY() > 0.0 ? -0.1 : 0.0;
            } else if (!ent.isNoGravity()) {
                d2 -= d0;
            }
            ent.setDeltaMovement(vector3d5.x * (double)f4, d2 * (double)0.98f, vector3d5.z * (double)f4);
        }
    }

    public static float getJumpUpwardsMotion(LivingEntity ent) {
        return 0.42f * PossessionHelper.getJumpFactor(ent);
    }

    public static void jump(LivingEntity ent) {
        float f = PossessionHelper.getJumpUpwardsMotion(ent);
        if (ent.hasEffect(MobEffects.JUMP)) {
            f += 0.1f * (float)(ent.getEffect(MobEffects.JUMP).getAmplifier() + 1);
        }
        Vec3 vector3d = ent.getDeltaMovement();
        ent.setDeltaMovement(vector3d.x, (double)f, vector3d.z);
        if (ent.isSprinting()) {
            float f1 = ent.getYRot() * ((float)Math.PI / 180);
            ent.setDeltaMovement(ent.getDeltaMovement().add((double)(-Mth.sin((float)f1) * 0.2f), 0.0, (double)(Mth.cos((float)f1) * 0.2f)));
        }
        ent.hasImpulse = true;
        CommonHooks.onLivingJump((LivingEntity)ent);
    }

    public static float getJumpFactor(LivingEntity ent) {
        float f = ent.level().getBlockState(ent.blockPosition()).getBlock().getJumpFactor();
        float f1 = ent.level().getBlockState(ent.blockPosition().below()).getBlock().getJumpFactor();
        return (double)f == 1.0 ? f1 : f;
    }
}

