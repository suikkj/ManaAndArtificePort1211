 
package com.mna.effects.harmful;

import com.mna.network.ServerMessageDispatcher;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;

public class EffectIcarianFlight
extends MobEffect implements com.mna.api.effects.IMNAEffectHook {
    public EffectIcarianFlight() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    public void onEffectAdded(LivingEntity pLivingEntity, int pAmplifier) {
            EffectIcarianFlight.writeIcarianData(pLivingEntity);
    
    }

    public void addAttributeModifiers(AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pAttributeMap, pAmplifier);
    }

    public void onEffectRemoved(LivingEntity pLivingEntity, int pAmplifier) {
            EffectIcarianFlight.clearIcarianData(pLivingEntity);
    
    }

    public void removeAttributeModifiers(AttributeMap pAttributeMap) {
        super.removeAttributeModifiers(pAttributeMap);
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        Vec3 vec = EffectIcarianFlight.readIcarianData(pLivingEntity);
        pLivingEntity.setDeltaMovement(vec);
        pLivingEntity.hasImpulse = true;

        return true;
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    public static void writeIcarianData(LivingEntity pLivingEntity) {
        Vec3 fwd = pLivingEntity.getForward().normalize().scale(5.0);
        pLivingEntity.getPersistentData().putDouble("icarianX", fwd.x);
        pLivingEntity.getPersistentData().putDouble("icarianY", Math.max(Math.abs(fwd.y), 1.0));
        pLivingEntity.getPersistentData().putDouble("icarianZ", fwd.z);
        if (!pLivingEntity.level().isClientSide()) {
            ServerMessageDispatcher.sendSetIcarianData(pLivingEntity, fwd);
        }
    }

    public static void writeIcarianData(LivingEntity pLivingEntity, Vec3 fwd) {
        pLivingEntity.getPersistentData().putDouble("icarianX", fwd.x);
        pLivingEntity.getPersistentData().putDouble("icarianY", Math.max(Math.abs(fwd.y), 1.0));
        pLivingEntity.getPersistentData().putDouble("icarianZ", fwd.z);
        if (!pLivingEntity.level().isClientSide()) {
            ServerMessageDispatcher.sendSetIcarianData(pLivingEntity, fwd);
        }
    }

    public static Vec3 readIcarianData(LivingEntity pLivingEntity) {
        return new Vec3(pLivingEntity.getPersistentData().getDouble("icarianX"), pLivingEntity.getPersistentData().getDouble("icarianY"), pLivingEntity.getPersistentData().getDouble("icarianZ"));
    }

    public static void clearIcarianData(LivingEntity pLivingEntity) {
        pLivingEntity.getPersistentData().remove("icarianX");
        pLivingEntity.getPersistentData().remove("icarianY");
        pLivingEntity.getPersistentData().remove("icarianZ");
        if (!pLivingEntity.level().isClientSide()) {
            ServerMessageDispatcher.sendSetIcarianData(pLivingEntity, Vec3.ZERO);
        }
    }
}

