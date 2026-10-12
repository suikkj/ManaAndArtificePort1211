 
package com.mna.blocks;

import com.mna.ManaAndArtifice;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.blocks.interfaces.IOffsetPlace;
import com.mna.api.items.OffsetPlacerItem;
import com.mna.api.items.TieredBlockItem;
import com.mna.blocks.BlockInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.resources.model.ModelResourceLocation;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class BlockItemRegistration {
    @SubscribeEvent
    public static void onRegisterItems(RegisterEvent event) {
        event.register(BuiltInRegistries.ITEM.getRegistryKey(), helper -> {
            BlockInit.BLOCKS.getEntries().stream().filter(b -> !(b.get() instanceof IDontCreateBlockItem) && !(b.get() instanceof FlowerPotBlock)).map(RegistryObject::get).forEach(block -> {
                Item.Properties properties = new Item.Properties();
                if (block instanceof IOffsetPlace) {
                    OffsetPlacerItem blockItem = new OffsetPlacerItem((Block)block, properties, ((IOffsetPlace)block)::adjustPlacement);
                    helper.register(BuiltInRegistries.BLOCK.getKey(block), blockItem);
                } else {
                    TieredBlockItem blockItem = new TieredBlockItem((Block)block, properties);
                    helper.register(BuiltInRegistries.BLOCK.getKey(block), blockItem);
                }
            });
            helper.register(ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("mna", "aum"))), ResourceLocation.fromNamespaceAndPath("mna", "aum"), () -> new BlockItem((Block)BlockInit.AUM.get(), new Item.Properties()));
            helper.register(ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("mna", "cerublossom"))), ResourceLocation.fromNamespaceAndPath("mna", "cerublossom"), () -> new BlockItem((Block)BlockInit.CERUBLOSSOM.get(), new Item.Properties()));
            helper.register(ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("mna", "tarma_root"))), ResourceLocation.fromNamespaceAndPath("mna", "tarma_root"), () -> new BlockItem((Block)BlockInit.TARMA_ROOT.get(), new Item.Properties()));
            helper.register(ResourceKey.create(ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("mna", "desert_nova"))), ResourceLocation.fromNamespaceAndPath("mna", "desert_nova"), () -> new BlockItem((Block)BlockInit.DESERT_NOVA.get(), new Item.Properties()));
        });
        ManaAndArtifice.LOGGER.info("M&A -> Block Items Registered");
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(BlockInit.AUM.getId(), BlockInit.POTTED_AUM);
        ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(BlockInit.CERUBLOSSOM.getId(), BlockInit.POTTED_CERUBLOSSOM);
        ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(BlockInit.WAKEBLOOM.getId(), BlockInit.POTTED_WAKEBLOOM);
        ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(BlockInit.TARMA_ROOT.getId(), BlockInit.POTTED_TARMA_ROOT);
        ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(BlockInit.DESERT_NOVA.getId(), BlockInit.POTTED_DESERT_NOVA);
        ManaAndArtifice.LOGGER.info("M&A -> Flower Pots Registered");
    }
}


