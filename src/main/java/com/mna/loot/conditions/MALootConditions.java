/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.neoforge.registries.RegisterEvent
 */
package com.mna.loot.conditions;

import com.mna.api.tools.RLoc;
import com.mna.loot.conditions.PlayerFactionCheck;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class MALootConditions {
    @SubscribeEvent
    public static void registerLootData(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.LOOT_CONDITION_TYPE)) {
            Registry.register((Registry)BuiltInRegistries.LOOT_CONDITION_TYPE, (ResourceLocation)RLoc.create("player-faction"), PlayerFactionCheck.PLAYER_FACTION);
        }
    }
}


