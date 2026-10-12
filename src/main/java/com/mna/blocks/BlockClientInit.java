 
package com.mna.blocks;

import com.mna.ManaAndArtifice;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.models.AffinityTinkerModel;
import com.mna.blocks.tileentities.models.ArcanaAltarModel;
import com.mna.blocks.tileentities.models.DisenchanterModel;
import com.mna.blocks.tileentities.models.EldrinFumeModel;
import com.mna.blocks.tileentities.models.RunescribingTableModel;
import com.mna.blocks.tileentities.models.SpecializationDeskModel;
import com.mna.blocks.tileentities.models.ThesisDeskModel;
import com.mna.blocks.tileentities.models.TranscriptionTableModel;
import com.mna.blocks.tileentities.renderers.ConstructWorkbenchRenderer;
import com.mna.blocks.tileentities.renderers.EldrinMatrixRenderer;
import com.mna.blocks.tileentities.renderers.LodestarRenderer;
import com.mna.blocks.tileentities.renderers.ManaweaveCacheRenderer;
import com.mna.blocks.tileentities.renderers.PylonRenderer;
import com.mna.blocks.tileentities.renderers.RunicAnvilRenderer;
import com.mna.blocks.tileentities.renderers.RunicLightRenderer;
import com.mna.blocks.tileentities.renderers.ScrollShelfRenderer;
import com.mna.blocks.tileentities.renderers.SeerStoneRenderer;
import com.mna.blocks.tileentities.renderers.SpectralRenderer;
import com.mna.blocks.tileentities.renderers.wizard_lab.InscriptionTableRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class BlockClientInit {
    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        BlockInit.BLOCKS.getEntries().stream().map(h -> h.get()).forEach(block -> {
            if (block instanceof ICutoutBlock || block instanceof FlowerPotBlock) {
                ItemBlockRenderTypes.setRenderLayer((Block)block, (RenderType)RenderType.cutout());
            } else if (block instanceof ITranslucentBlock) {
                ItemBlockRenderTypes.setRenderLayer((Block)block, (RenderType)RenderType.translucent());
            }
        });
        ItemBlockRenderTypes.setRenderLayer((Block)((Block)BlockInit.SLIPSTREAM_GENERATOR.get()), (RenderType)RenderType.cutoutMipped());
        ManaAndArtifice.LOGGER.info("M&A -> Block Render Layers Set");
    }

    @SubscribeEvent
    public static void onRegisterSpecialModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(RLoc.create("block/pedestal")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/mana_resevoir/full")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/mana_crystal")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/manaweaving_altar_crystal_a")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/manaweaving_altar_crystal_b")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/manaweaving_altar_crystal_c")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/occulus_eye_shell_interior")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/occulus_eye_shell")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/eldrin/wellspring_pillar_cap")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/eldrin/supplier_lower_exterior")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/eldrin/supplier_lower_interior")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/eldrin/supplier_upper")));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/eldrin/receiver_crystal")));
        event.register(ModelResourceLocation.standalone(ScrollShelfRenderer.scroll));
        event.register(ModelResourceLocation.standalone(ScrollShelfRenderer.bottle));
        event.register(ModelResourceLocation.standalone(ConstructWorkbenchRenderer.hook_arm));
        event.register(ModelResourceLocation.standalone(ConstructWorkbenchRenderer.hook_head));
        event.register(ModelResourceLocation.standalone(ConstructWorkbenchRenderer.hook_leg));
        event.register(ModelResourceLocation.standalone(ConstructWorkbenchRenderer.hook_torso));
        event.register(ModelResourceLocation.standalone(RunicAnvilRenderer.ring_small));
        event.register(ModelResourceLocation.standalone(RunicAnvilRenderer.ring_large));
        event.register(ModelResourceLocation.standalone(InscriptionTableRenderer.ash));
        event.register(ModelResourceLocation.standalone(InscriptionTableRenderer.ink));
        event.register(ModelResourceLocation.standalone(InscriptionTableRenderer.vellum));
        event.register(ModelResourceLocation.standalone(RLoc.create("block/illusion_block")));
        event.register(ModelResourceLocation.standalone(SeerStoneRenderer.crystal));
        event.register(ModelResourceLocation.standalone(SeerStoneRenderer.crystal_band));
        event.register(ModelResourceLocation.standalone(LodestarRenderer.base));
        event.register(ModelResourceLocation.standalone(LodestarRenderer.delegation_station));
        event.register(ModelResourceLocation.standalone(LodestarRenderer.small_crystal));
        event.register(ModelResourceLocation.standalone(LodestarRenderer.big_crystal));
        event.register(ModelResourceLocation.standalone(LodestarRenderer.small_gear));
        event.register(ModelResourceLocation.standalone(LodestarRenderer.stabilizer));
        event.register(ModelResourceLocation.standalone(ManaweaveCacheRenderer.interior));
        event.register(ModelResourceLocation.standalone(ManaweaveCacheRenderer.exterior));
        event.register(ModelResourceLocation.standalone(SpectralRenderer.anvil_top));
        event.register(ModelResourceLocation.standalone(SpectralRenderer.floating_bot));
        event.register(ModelResourceLocation.standalone(SpectralRenderer.floating_mid));
        event.register(ModelResourceLocation.standalone(SpectralRenderer.floating_runes));
        event.register(ModelResourceLocation.standalone(SpectralRenderer.floating_top));
        event.register(ModelResourceLocation.standalone(SpectralRenderer.stonecutter_blade));
        event.register(ModelResourceLocation.standalone(SpectralRenderer.stonecutter_top));
        event.register(ModelResourceLocation.standalone(SpectralRenderer.workbench_top));
        event.register(ModelResourceLocation.standalone(ThesisDeskModel.ink));
        event.register(ModelResourceLocation.standalone(ThesisDeskModel.vellum));
        event.register(ModelResourceLocation.standalone(EldrinFumeModel.coal));
        event.register(ModelResourceLocation.standalone(SpecializationDeskModel.crystal_back));
        event.register(ModelResourceLocation.standalone(SpecializationDeskModel.crystal_left));
        event.register(ModelResourceLocation.standalone(SpecializationDeskModel.crystal_right));
        event.register(ModelResourceLocation.standalone(TranscriptionTableModel.ink));
        event.register(ModelResourceLocation.standalone(TranscriptionTableModel.lapis));
        event.register(ModelResourceLocation.standalone(RunescribingTableModel.water));
        event.register(ModelResourceLocation.standalone(RunescribingTableModel.recipe));
        event.register(ModelResourceLocation.standalone(DisenchanterModel.crystal));
        event.register(ModelResourceLocation.standalone(AffinityTinkerModel.scroll));
        event.register(ModelResourceLocation.standalone(ArcanaAltarModel.candle_short));
        event.register(ModelResourceLocation.standalone(ArcanaAltarModel.candle_long));
        event.register(ModelResourceLocation.standalone(PylonRenderer.crystal));
        event.register(ModelResourceLocation.standalone(PylonRenderer.ring));
        event.register(ModelResourceLocation.standalone(PylonRenderer.scroll));
        event.register(ModelResourceLocation.standalone(EldrinMatrixRenderer.crystal));
        event.register(ModelResourceLocation.standalone(EldrinMatrixRenderer.ring));
        event.register(ModelResourceLocation.standalone(RunicLightRenderer.cube));
    }
}


