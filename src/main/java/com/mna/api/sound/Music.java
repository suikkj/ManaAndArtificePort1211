/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.neoforge.registries.NeoForgeRegistries$Keys
 *  net.neoforged.neoforge.registries.ObjectHolder
 *  net.neoforged.neoforge.registries.RegisterEvent
 *  net.neoforged.neoforge.registries.RegisterEvent$RegisterHelper
 */
package com.mna.api.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public final class Music {
    @SubscribeEvent
    public static void onRegisterSounds(RegisterEvent event) {
        event.register(net.minecraft.core.registries.Registries.SOUND_EVENT, helper -> Music.registerSound((RegisterEvent.RegisterHelper<SoundEvent>)helper, "music_big_car_theft"));
    }

    private static void registerSound(RegisterEvent.RegisterHelper<SoundEvent> registry, String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("mna", name);
        registry.register(id, SoundEvent.createVariableRangeEvent(id));
    }

    public static final class Construct {
        // [PORT 1.21.1] No 1.20.1 este campo usava @ObjectHolder("mna:music_big_car_theft")
        // (Forge injetava o SoundEvent em runtime); o NeoForge removeu @ObjectHolder,
        // entao a instancia e criada diretamente com a MESMA ResourceLocation registrada acima.
        public static final SoundEvent DANCE_MIX =
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("mna", "music_big_car_theft"));
    }
}


