/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.events;

import com.mna.effects.interfaces.IInputDisable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class InputDisabler {
    public static int getDisableInputMask(LivingEntity entity) {
        int mask = 0;
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            if (!(effect.getEffect() instanceof IInputDisable)) continue;
            mask |= ((IInputDisable)effect.getEffect()).getDisableMask();
        }
        return mask;
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        int mask = InputDisabler.getDisableInputMask((LivingEntity)event.getEntity());
        if ((mask & IInputDisable.InputMask.LEFT_CLICK.mask()) != 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        int mask = InputDisabler.getDisableInputMask((LivingEntity)event.getEntity());
        if ((mask & IInputDisable.InputMask.RIGHT_CLICK.mask()) != 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        int mask = InputDisabler.getDisableInputMask((LivingEntity)event.getEntity());
        if ((mask & IInputDisable.InputMask.RIGHT_CLICK.mask()) != 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        int mask = InputDisabler.getDisableInputMask((LivingEntity)event.getEntity());
        if ((mask & IInputDisable.InputMask.RIGHT_CLICK.mask()) != 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        int mask = InputDisabler.getDisableInputMask((LivingEntity)event.getEntity());
        if ((mask & IInputDisable.InputMask.RIGHT_CLICK.mask()) != 0) {
            event.setCanceled(true);
        }
    }
}


