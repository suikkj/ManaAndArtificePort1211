 
package com.mna.api.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.resources.model.ModelResourceLocation;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class MACreativeTabs {
    public static CreativeModeTab GENERAL = CreativeModeTab.builder().icon(() -> new ItemStack((ItemLike)BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mna", "guide_book")))).title((Component)Component.translatable((String)"itemGroup.mna.items")).build();

    @SubscribeEvent
    public static void onRegistrCreativeTabs(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(ResourceLocation.fromNamespaceAndPath("mna", "tab_general"), GENERAL));
    }
}


