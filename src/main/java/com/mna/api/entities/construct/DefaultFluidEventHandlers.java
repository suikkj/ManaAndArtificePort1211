/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.level.material.Fluids
 *  net.neoforged.neoforge.common.NeoForgeMod
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.api.entities.construct;

import com.mna.api.effects.MAEffects;
import com.mna.api.entities.DamageHelper;
import com.mna.api.events.construct.ConstructSprayEffectEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class DefaultFluidEventHandlers {
    @SubscribeEvent
    public static void onConstructSprayTarget(ConstructSprayEffectEvent event) {
        if (event.getFluid().isSame((Fluid)Fluids.LAVA)) {
            event.getTarget().hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.LAVA, event.getConstruct().level().registryAccess(), (Entity)event.getConstruct()), 4.0f);
            event.getTarget().igniteForSeconds((float)(15));
        } else if (event.getFluid().isSame((Fluid)Fluids.WATER)) {
            event.getTarget().clearFire();
            if (!event.getTarget().hasEffect(MAEffects.SOAKED)) {
                event.getTarget().addEffect(new MobEffectInstance(MAEffects.SOAKED, 200));
            }
            if (!event.isTargetFriendly()) {
                event.getTarget().hurt(DamageHelper.createSourcedType(DamageHelper.FROST, event.getConstruct().level().registryAccess(), (Entity)event.getConstruct()), 2.0f);
            }
            event.getTarget().knockback(0.5, event.getConstruct().getX() - event.getTarget().getX(), event.getConstruct().getZ() - event.getTarget().getZ());
        } else if (event.getFluid().isSame((Fluid)NeoForgeMod.MILK.get())) {
            // [PORT 1.21.1] curePotionEffects removido
        
            return;
        }
    }
}


