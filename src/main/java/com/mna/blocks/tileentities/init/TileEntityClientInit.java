/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderers
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
 */
package com.mna.blocks.tileentities.init;

import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.IceSpikeTile;
import com.mna.blocks.tileentities.ImpaleSpikeTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.models.AffinityTinkerModel;
import com.mna.blocks.tileentities.models.EldrinFumeModel;
import com.mna.blocks.tileentities.models.ModelIceSpike;
import com.mna.blocks.tileentities.models.ModelImpaleSpike;
import com.mna.blocks.tileentities.models.RunescribingTableModel;
import com.mna.blocks.tileentities.models.SpecializationDeskModel;
import com.mna.blocks.tileentities.models.ThesisDeskModel;
import com.mna.blocks.tileentities.models.TranscriptionTableModel;
import com.mna.blocks.tileentities.renderers.ArcaneSentryRenderer;
import com.mna.blocks.tileentities.renderers.BookshelfRenderer;
import com.mna.blocks.tileentities.renderers.ChalkRuneRenderer;
import com.mna.blocks.tileentities.renderers.ConstructWorkbenchRenderer;
import com.mna.blocks.tileentities.renderers.EldrinAltarRenderer;
import com.mna.blocks.tileentities.renderers.EldrinConduitRenderer;
import com.mna.blocks.tileentities.renderers.EldrinMatrixRenderer;
import com.mna.blocks.tileentities.renderers.FluidJugRenderer;
import com.mna.blocks.tileentities.renderers.IllusionBlockRenderer;
import com.mna.blocks.tileentities.renderers.LodestarRenderer;
import com.mna.blocks.tileentities.renderers.MageLightRenderer;
import com.mna.blocks.tileentities.renderers.ManaweaveCacheRenderer;
import com.mna.blocks.tileentities.renderers.ManaweaveProjectorRenderer;
import com.mna.blocks.tileentities.renderers.ManaweavingAltarRenderer;
import com.mna.blocks.tileentities.renderers.OcculusRenderer;
import com.mna.blocks.tileentities.renderers.PedestalRenderer;
import com.mna.blocks.tileentities.renderers.PylonRenderer;
import com.mna.blocks.tileentities.renderers.RuneforgeRenderer;
import com.mna.blocks.tileentities.renderers.RunicAnvilRenderer;
import com.mna.blocks.tileentities.renderers.RunicLightRenderer;
import com.mna.blocks.tileentities.renderers.ScrollShelfRenderer;
import com.mna.blocks.tileentities.renderers.SeerStoneRenderer;
import com.mna.blocks.tileentities.renderers.SimpleGeoBlockRenderer;
import com.mna.blocks.tileentities.renderers.SpectralRenderer;
import com.mna.blocks.tileentities.renderers.TransitoryTileRenderer;
import com.mna.blocks.tileentities.renderers.WellspringPillarRenderer;
import com.mna.blocks.tileentities.renderers.wizard_lab.ArcanaAltarRenderer;
import com.mna.blocks.tileentities.renderers.wizard_lab.DisenchanterRenderer;
import com.mna.blocks.tileentities.renderers.wizard_lab.FumeBellowsRenderer;
import com.mna.blocks.tileentities.renderers.wizard_lab.FumePrismRenderer;
import com.mna.blocks.tileentities.renderers.wizard_lab.InscriptionTableRenderer;
import com.mna.blocks.tileentities.renderers.wizard_lab.WizardLabRenderer;
import com.mna.blocks.tileentities.wizard_lab.AffinityTinkerTile;
import com.mna.blocks.tileentities.wizard_lab.EldrinFumeTile;
import com.mna.blocks.tileentities.wizard_lab.RunescribingTableTile;
import com.mna.blocks.tileentities.wizard_lab.SpellSpecializationTile;
import com.mna.blocks.tileentities.wizard_lab.ThesisDeskTile;
import com.mna.blocks.tileentities.wizard_lab.TranscriptionTableTile;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class TileEntityClientInit {
    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.CHALK_RUNE.get()), ChalkRuneRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.PEDESTAL.get()), PedestalRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.MANAWEAVING_ALTAR.get()), ManaweavingAltarRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.RUNEFORGE.get()), RuneforgeRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.RUNIC_ANVIL.get()), RunicAnvilRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.MANAWEAVE_PROJECTOR.get()), ManaweaveProjectorRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.OCCULUS.get()), OcculusRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.ELEMENTAL_SENTRY.get()), ArcaneSentryRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.CONSTRUCT_WORKBENCH.get()), ConstructWorkbenchRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.LODESTAR.get()), LodestarRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.INSCRIPTION_TABLE.get()), InscriptionTableRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.MAGE_LIGHT.get()), MageLightRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.TRANSITORY_TILE.get()), TransitoryTileRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.WELLSPRING_PILLAR.get()), WellspringPillarRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.ELDRIN_CONDUIT_TILE.get()), EldrinConduitRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.ELDRIN_ALTAR_TILE.get()), EldrinAltarRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.ILLUSION_BLOCK.get()), IllusionBlockRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.SPECTRAL_TILE.get()), SpectralRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.MANAWEAVE_CACHE.get()), ManaweaveCacheRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.FLUID_JUG.get()), FluidJugRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.SEER_STONE.get()), SeerStoneRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.BOOKSHELF.get()), BookshelfRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.SCROLLSHELF.get()), ScrollShelfRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.ICE_SPIKE.get()), ctx -> new SimpleGeoBlockRenderer<IceSpikeTile>(ctx, new ModelIceSpike()));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.IMPALE_SPIKE.get()), ctx -> new SimpleGeoBlockRenderer<ImpaleSpikeTile>(ctx, new ModelImpaleSpike()));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.RUNIC_LIGHTS.get()), ctx -> new RunicLightRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.POTION_PYLON.get()), ctx -> new PylonRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.RECHARGE_PYLON.get()), ctx -> new PylonRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.MANA_PYLON.get()), ctx -> new PylonRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.INTERDICTION_PYLON.get()), ctx -> new PylonRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.LIGHT_PYLON.get()), ctx -> new PylonRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.FREEZE_PYLON.get()), ctx -> new PylonRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.THESIS_DESK.get()), ctx -> new WizardLabRenderer<ThesisDeskTile>(ctx, new ThesisDeskModel()));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.ELDRIN_FUME.get()), ctx -> new WizardLabRenderer<EldrinFumeTile>(ctx, new EldrinFumeModel()));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.SPELL_SPECIALIZAITON.get()), ctx -> new WizardLabRenderer<SpellSpecializationTile>(ctx, new SpecializationDeskModel()));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.TRANSCRIPTION_TABLE.get()), ctx -> new WizardLabRenderer<TranscriptionTableTile>(ctx, new TranscriptionTableModel()));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.RUNESCRIBING_TABLE.get()), ctx -> new WizardLabRenderer<RunescribingTableTile>(ctx, new RunescribingTableModel()));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.DISENCHANTER.get()), ctx -> new DisenchanterRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.AFFINITY_TINKER.get()), ctx -> new WizardLabRenderer<AffinityTinkerTile>(ctx, new AffinityTinkerModel()));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.ALTAR_OF_ARCANA.get()), ctx -> new ArcanaAltarRenderer(ctx));
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.FUME_BELLOWS.get()), FumeBellowsRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.FUME_PRISM.get()), FumePrismRenderer::new);
        BlockEntityRenderers.register((BlockEntityType)((BlockEntityType)TileEntityInit.ELDRIN_MATRIX.get()), EldrinMatrixRenderer::new);
        ManaAndArtifice.LOGGER.info("M&A -> Tile Entity Renderers Registered");
    }
}


