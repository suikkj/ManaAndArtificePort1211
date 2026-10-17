/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna.capabilities;

import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.blocks.tileentities.FluidJugTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import net.minecraft.core.Direction;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * [PORT 1.21.1] Exposição de capabilities de bloco.
 *
 * No 1.20.1 os tiles expunham capabilities sobrescrevendo {@code getCapability(...)}:
 *  - {@code TileEntityWithInventory} -> {@code ForgeCapabilities.ITEM_HANDLER} (SidedInvWrapper)
 *  - {@code FluidJugTile}            -> {@code ForgeCapabilities.FLUID_HANDLER} (self)
 * No 1.21.1 não existe mais override de {@code getCapability}; a exposição é feita
 * registrando um provider por BlockEntityType em {@link RegisterCapabilitiesEvent}.
 * Sem isto, hoppers/pipes/mods de automação não interagem com os tiles (regressão de parity).
 */
@EventBusSubscriber(modid = "mna", bus = EventBusSubscriber.Bus.MOD)
public class MACapabilityModEventHandlers {

    @net.neoforged.bus.api.SubscribeEvent
    public static void registerBlockCapabilities(RegisterCapabilitiesEvent event) {
        BlockCapability<IItemHandler, Direction> itemCap = Capabilities.ItemHandler.BLOCK;
        registerItemHandler(event, TileEntityInit.CHALK_RUNE, itemCap);
        registerItemHandler(event, TileEntityInit.PEDESTAL, itemCap);
        registerItemHandler(event, TileEntityInit.RUNEFORGE, itemCap);
        registerItemHandler(event, TileEntityInit.MANAWEAVING_ALTAR, itemCap);
        registerItemHandler(event, TileEntityInit.INSCRIPTION_TABLE, itemCap);
        registerItemHandler(event, TileEntityInit.RUNESCRIBING_TABLE, itemCap);
        registerItemHandler(event, TileEntityInit.RUNIC_ANVIL, itemCap);
        registerItemHandler(event, TileEntityInit.SEER_STONE, itemCap);
        registerItemHandler(event, TileEntityInit.BOOKSHELF, itemCap);
        registerItemHandler(event, TileEntityInit.BOOK_STAND, itemCap);
        registerItemHandler(event, TileEntityInit.SCROLLSHELF, itemCap);
        registerItemHandler(event, TileEntityInit.MAGICIANS_WORKBENCH, itemCap);

        // FLUID_HANDLER para o FluidJug (o próprio tile implementa IFluidHandler).
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            TileEntityInit.FLUID_JUG.get(),
            (FluidJugTile tile, Direction side) -> tile
        );

        com.mna.ManaAndArtifice.LOGGER.info("Mana And Artifice >> Registered block capabilities (item/fluid handlers)");
    }

    @SuppressWarnings("unchecked")
    private static void registerItemHandler(RegisterCapabilitiesEvent event,
                                            net.neoforged.neoforge.registries.DeferredHolder<
                                                net.minecraft.world.level.block.entity.BlockEntityType<?>,
                                                ? extends net.minecraft.world.level.block.entity.BlockEntityType<?>> holder,
                                            BlockCapability<IItemHandler, Direction> cap) {
        event.registerBlockEntity(cap,
            (net.minecraft.world.level.block.entity.BlockEntityType<TileEntityWithInventory>) holder.get(),
            (TileEntityWithInventory tile, Direction side) -> tile.getOrCreateSidedHandler(side));
    }
}


