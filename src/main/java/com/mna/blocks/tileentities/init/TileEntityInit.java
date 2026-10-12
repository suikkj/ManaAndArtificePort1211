/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  net.neoforged.neoforge.registries.IForgeRegistry
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.blocks.tileentities.init;

import com.mna.api.blocks.tile.PotionEffectPylonTile;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.BookshelfTile;
import com.mna.blocks.tileentities.BrazierTile;
import com.mna.blocks.tileentities.ChalkRuneTile;
import com.mna.blocks.tileentities.CoffinTile;
import com.mna.blocks.tileentities.ConstructWorkbenchTile;
import com.mna.blocks.tileentities.EldrinAltarTile;
import com.mna.blocks.tileentities.EldrinConduitTile;
import com.mna.blocks.tileentities.EldrinMatrixTile;
import com.mna.blocks.tileentities.ElementalSentryTile;
import com.mna.blocks.tileentities.FluidJugTile;
import com.mna.blocks.tileentities.IceSpikeTile;
import com.mna.blocks.tileentities.IllusionBlockTile;
import com.mna.blocks.tileentities.ImpaleSpikeTile;
import com.mna.blocks.tileentities.LodestarTile;
import com.mna.blocks.tileentities.MagelightTile;
import com.mna.blocks.tileentities.ManaResevoirTile;
import com.mna.blocks.tileentities.ManaweaveCacheTile;
import com.mna.blocks.tileentities.ManaweaveProjectorTile;
import com.mna.blocks.tileentities.ManaweavingAltarTile;
import com.mna.blocks.tileentities.OcculusTile;
import com.mna.blocks.tileentities.OffsetBlockTile;
import com.mna.blocks.tileentities.ParticleEmitterTile;
import com.mna.blocks.tileentities.PedestalTile;
import com.mna.blocks.tileentities.RefractionLensTile;
import com.mna.blocks.tileentities.RuneForgeTile;
import com.mna.blocks.tileentities.RunicAnvilTile;
import com.mna.blocks.tileentities.RunicLightTile;
import com.mna.blocks.tileentities.SanctumTile;
import com.mna.blocks.tileentities.ScrollShelfTile;
import com.mna.blocks.tileentities.SeerStoneTile;
import com.mna.blocks.tileentities.SimpleSpectralTile;
import com.mna.blocks.tileentities.SlipstreamGeneratorTile;
import com.mna.blocks.tileentities.SpectralTile;
import com.mna.blocks.tileentities.TransitoryTile;
import com.mna.blocks.tileentities.TransitoryTunnelTile;
import com.mna.blocks.tileentities.WellspringPillarTile;
import com.mna.blocks.tileentities.pylon.FreezePylonTile;
import com.mna.blocks.tileentities.pylon.InterdictionPylonTile;
import com.mna.blocks.tileentities.pylon.LightPylonTile;
import com.mna.blocks.tileentities.pylon.ManaPylonTile;
import com.mna.blocks.tileentities.pylon.RechargePylonTile;
import com.mna.blocks.tileentities.wizard_lab.AffinityTinkerTile;
import com.mna.blocks.tileentities.wizard_lab.ArcanaAltarTile;
import com.mna.blocks.tileentities.wizard_lab.BookStandTile;
import com.mna.blocks.tileentities.wizard_lab.DisenchanterTile;
import com.mna.blocks.tileentities.wizard_lab.EldrinFumeTile;
import com.mna.blocks.tileentities.wizard_lab.FumeBellowsTile;
import com.mna.blocks.tileentities.wizard_lab.FumePrismTile;
import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import com.mna.blocks.tileentities.wizard_lab.MagiciansWorkbenchTile;
import com.mna.blocks.tileentities.wizard_lab.RunescribingTableTile;
import com.mna.blocks.tileentities.wizard_lab.SpellSpecializationTile;
import com.mna.blocks.tileentities.wizard_lab.StudyDeskTile;
import com.mna.blocks.tileentities.wizard_lab.ThesisDeskTile;
import com.mna.blocks.tileentities.wizard_lab.TranscriptionTableTile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TileEntityInit {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "mna");
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ChalkRuneTile>> CHALK_RUNE = TILE_ENTITY_TYPES.register("chalk_rune_tile_entity", () -> BlockEntityType.Builder.of(ChalkRuneTile::new, (Block[])new Block[]{(Block)BlockInit.CHALK_RUNE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<PedestalTile>> PEDESTAL = TILE_ENTITY_TYPES.register("pedestal_tile_entity", () -> BlockEntityType.Builder.of(PedestalTile::new, (Block[])new Block[]{(Block)BlockInit.PEDESTAL.get(), (Block)BlockInit.PEDESTAL_WITH_SIGN.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<RuneForgeTile>> RUNEFORGE = TILE_ENTITY_TYPES.register("runeforge_tile_entity", () -> BlockEntityType.Builder.of(RuneForgeTile::new, (Block[])new Block[]{(Block)BlockInit.RUNEFORGE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ManaweavingAltarTile>> MANAWEAVING_ALTAR = TILE_ENTITY_TYPES.register("manaweaving_altar_tile_entity", () -> BlockEntityType.Builder.of(ManaweavingAltarTile::new, (Block[])new Block[]{(Block)BlockInit.MANAWEAVING_ALTAR.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ManaweaveCacheTile>> MANAWEAVE_CACHE = TILE_ENTITY_TYPES.register("manaweave_cache_tile", () -> BlockEntityType.Builder.of(ManaweaveCacheTile::new, (Block[])new Block[]{(Block)BlockInit.MANAWEAVE_CACHE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<InscriptionTableTile>> INSCRIPTION_TABLE = TILE_ENTITY_TYPES.register("inscription_table_tile_entity", () -> BlockEntityType.Builder.of(InscriptionTableTile::new, (Block[])new Block[]{(Block)BlockInit.INSCRIPTION_TABLE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<RunescribingTableTile>> RUNESCRIBING_TABLE = TILE_ENTITY_TYPES.register("runescribing_table_tile_entity", () -> BlockEntityType.Builder.of(RunescribingTableTile::new, (Block[])new Block[]{(Block)BlockInit.RUNESCRIBING_TABLE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<RunicAnvilTile>> RUNIC_ANVIL = TILE_ENTITY_TYPES.register("runic_anvil_tile_entity", () -> BlockEntityType.Builder.of(RunicAnvilTile::new, (Block[])new Block[]{(Block)BlockInit.RUNIC_ANVIL.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ParticleEmitterTile>> PARTICLE_EMITTER = TILE_ENTITY_TYPES.register("particle_emitter_tile_entity", () -> BlockEntityType.Builder.of(ParticleEmitterTile::new, (Block[])new Block[]{(Block)BlockInit.PARTICLE_EMITTER.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ManaweaveProjectorTile>> MANAWEAVE_PROJECTOR = TILE_ENTITY_TYPES.register("manaweave_projector_tile_entity", () -> BlockEntityType.Builder.of(ManaweaveProjectorTile::new, (Block[])new Block[]{(Block)BlockInit.MANAWEAVE_PROJECTOR.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ManaResevoirTile>> MANA_RESEVOIR = TILE_ENTITY_TYPES.register("mana_resevoir_tile_entity", () -> BlockEntityType.Builder.of(ManaResevoirTile::new, (Block[])new Block[]{(Block)BlockInit.MANA_RESEVOIR.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<TransitoryTunnelTile>> TRANSITORY_TUNNEL = TILE_ENTITY_TYPES.register("transitory_tunnel_tile_entity", () -> BlockEntityType.Builder.of(TransitoryTunnelTile::new, (Block[])new Block[]{(Block)BlockInit.TRANSITORY_TUNNEL.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<TransitoryTile>> TRANSITORY_TILE = TILE_ENTITY_TYPES.register("transitory_tile_tile_entity", () -> BlockEntityType.Builder.of(TransitoryTile::new, (Block[])new Block[]{(Block)BlockInit.TRANSITORY_TILE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<OcculusTile>> OCCULUS = TILE_ENTITY_TYPES.register("occulus_tile_entity", () -> BlockEntityType.Builder.of(OcculusTile::new, (Block[])new Block[]{(Block)BlockInit.OCCULUS.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<IceSpikeTile>> ICE_SPIKE = TILE_ENTITY_TYPES.register("ice_spike_tile", () -> BlockEntityType.Builder.of(IceSpikeTile::new, (Block[])new Block[]{(Block)BlockInit.ICE_SPIKE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ImpaleSpikeTile>> IMPALE_SPIKE = TILE_ENTITY_TYPES.register("impale_spike_tile", () -> BlockEntityType.Builder.of(ImpaleSpikeTile::new, (Block[])new Block[]{(Block)BlockInit.IMPALE_SPIKE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ElementalSentryTile>> ELEMENTAL_SENTRY = TILE_ENTITY_TYPES.register("arcane_sentry_tile_entity", () -> BlockEntityType.Builder.of(ElementalSentryTile::new, (Block[])new Block[]{(Block)BlockInit.ARCANE_SENTRY.get(), (Block)BlockInit.ENDER_SENTRY.get(), (Block)BlockInit.FIRE_SENTRY.get(), (Block)BlockInit.WATER_SENTRY.get(), (Block)BlockInit.EARTH_SENTRY.get(), (Block)BlockInit.WIND_SENTRY.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<SeerStoneTile>> SEER_STONE = TILE_ENTITY_TYPES.register("seer_stone_tile_entity", () -> BlockEntityType.Builder.of(SeerStoneTile::new, (Block[])new Block[]{(Block)BlockInit.SEER_STONE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ConstructWorkbenchTile>> CONSTRUCT_WORKBENCH = TILE_ENTITY_TYPES.register("construct_workbench", () -> BlockEntityType.Builder.of(ConstructWorkbenchTile::new, (Block[])new Block[]{(Block)BlockInit.CONSTRUCT_WORKBENCH.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<LodestarTile>> LODESTAR = TILE_ENTITY_TYPES.register("lodestar", () -> BlockEntityType.Builder.of(LodestarTile::new, (Block[])new Block[]{(Block)BlockInit.LODESTAR.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<MagelightTile>> MAGE_LIGHT = TILE_ENTITY_TYPES.register("magelight", () -> BlockEntityType.Builder.of(MagelightTile::new, (Block[])new Block[]{(Block)BlockInit.MAGE_LIGHT.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<BrazierTile>> BRAZIER = TILE_ENTITY_TYPES.register("brazier", () -> BlockEntityType.Builder.of(BrazierTile::new, (Block[])new Block[]{(Block)BlockInit.BRAZIER.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<SlipstreamGeneratorTile>> SLIPSTREAM_GENERATOR = TILE_ENTITY_TYPES.register("slipstream_generator", () -> BlockEntityType.Builder.of(SlipstreamGeneratorTile::new, (Block[])new Block[]{(Block)BlockInit.SLIPSTREAM_GENERATOR.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<SimpleSpectralTile>> SIMPLE_SPECTRAL_TILE = TILE_ENTITY_TYPES.register("simple_spectral_tile", () -> BlockEntityType.Builder.of(SimpleSpectralTile::new, (Block[])new Block[]{(Block)BlockInit.SPECTRAL_LADDER.get(), (Block)BlockInit.SPECTRAL_WEB.get(), (Block)BlockInit.SPECTRAL_FENCE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<SpectralTile>> SPECTRAL_TILE = TILE_ENTITY_TYPES.register("spectral_tile", () -> BlockEntityType.Builder.of(SpectralTile::new, (Block[])new Block[]{(Block)BlockInit.SPECTRAL_CRAFTING_TABLE.get(), (Block)BlockInit.SPECTRAL_ANVIL.get(), (Block)BlockInit.SPECTRAL_STONECUTTER.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<SanctumTile>> CIRCLE_OF_POWER = TILE_ENTITY_TYPES.register("circle_of_power", () -> BlockEntityType.Builder.of(SanctumTile::new, (Block[])new Block[]{(Block)BlockInit.CIRCLE_OF_POWER.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<CoffinTile>> COFFIN = TILE_ENTITY_TYPES.register("coffin", () -> BlockEntityType.Builder.of(CoffinTile::new, (Block[])new Block[]{(Block)BlockInit.COFFIN.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<OffsetBlockTile>> FILLER_TILE = TILE_ENTITY_TYPES.register("filler_tile", () -> BlockEntityType.Builder.of(OffsetBlockTile::new, (Block[])new Block[]{(Block)BlockInit.EMPTY_FILLER_BLOCK.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<EldrinConduitTile>> ELDRIN_CONDUIT_TILE = TILE_ENTITY_TYPES.register("eldrin_conduit_tile", () -> BlockEntityType.Builder.of(EldrinConduitTile::new, (Block[])new Block[]{(Block)BlockInit.ELDRIN_CONDUIT_FIRE.get(), (Block)BlockInit.ELDRIN_CONDUIT_WATER.get(), (Block)BlockInit.ELDRIN_CONDUIT_EARTH.get(), (Block)BlockInit.ELDRIN_CONDUIT_AIR.get(), (Block)BlockInit.ELDRIN_CONDUIT_ENDER.get(), (Block)BlockInit.ELDRIN_CONDUIT_ARCANE.get(), (Block)BlockInit.LESSER_ELDRIN_CONDUIT_FIRE.get(), (Block)BlockInit.LESSER_ELDRIN_CONDUIT_WATER.get(), (Block)BlockInit.LESSER_ELDRIN_CONDUIT_EARTH.get(), (Block)BlockInit.LESSER_ELDRIN_CONDUIT_AIR.get(), (Block)BlockInit.LESSER_ELDRIN_CONDUIT_ENDER.get(), (Block)BlockInit.LESSER_ELDRIN_CONDUIT_ARCANE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<EldrinAltarTile>> ELDRIN_ALTAR_TILE = TILE_ENTITY_TYPES.register("runic_altar_tile", () -> BlockEntityType.Builder.of(EldrinAltarTile::new, (Block[])new Block[]{(Block)BlockInit.ELDRIN_ALTAR.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<WellspringPillarTile>> WELLSPRING_PILLAR = TILE_ENTITY_TYPES.register("wellspring_pillar", () -> BlockEntityType.Builder.of(WellspringPillarTile::new, (Block[])new Block[]{(Block)BlockInit.WELLSPRING_PILLAR.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<MagiciansWorkbenchTile>> MAGICIANS_WORKBENCH = TILE_ENTITY_TYPES.register("magicians_workbench", () -> BlockEntityType.Builder.of(MagiciansWorkbenchTile::new, (Block[])new Block[]{(Block)BlockInit.MAGICIANS_WORKBENCH.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<FluidJugTile>> FLUID_JUG = TILE_ENTITY_TYPES.register("fluid_jug", () -> BlockEntityType.Builder.of(FluidJugTile::new, (Block[])new Block[]{(Block)BlockInit.FLUID_JUG.get(), (Block)BlockInit.FLUID_JUG_INFINITE_LAVA.get(), (Block)BlockInit.FLUID_JUG_INFINITE_WATER.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<IllusionBlockTile>> ILLUSION_BLOCK = TILE_ENTITY_TYPES.register("illusion_block", () -> BlockEntityType.Builder.of(IllusionBlockTile::new, (Block[])new Block[]{(Block)BlockInit.ILLUSION_BLOCK.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ThesisDeskTile>> THESIS_DESK = TILE_ENTITY_TYPES.register("thesis_desk", () -> BlockEntityType.Builder.of(ThesisDeskTile::new, (Block[])new Block[]{(Block)BlockInit.THESIS_DESK.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<TranscriptionTableTile>> TRANSCRIPTION_TABLE = TILE_ENTITY_TYPES.register("transcription_table", () -> BlockEntityType.Builder.of(TranscriptionTableTile::new, (Block[])new Block[]{(Block)BlockInit.TRANSCRIPTION_TABLE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<StudyDeskTile>> STUDY_DESK = TILE_ENTITY_TYPES.register("study_desk", () -> BlockEntityType.Builder.of(StudyDeskTile::new, (Block[])new Block[]{(Block)BlockInit.STUDY_DESK.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<SpellSpecializationTile>> SPELL_SPECIALIZAITON = TILE_ENTITY_TYPES.register("spell_specialization", () -> BlockEntityType.Builder.of(SpellSpecializationTile::new, (Block[])new Block[]{(Block)BlockInit.SPELL_SPECIALIZATION.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ArcanaAltarTile>> ALTAR_OF_ARCANA = TILE_ENTITY_TYPES.register("altar_of_arcana", () -> BlockEntityType.Builder.of(ArcanaAltarTile::new, (Block[])new Block[]{(Block)BlockInit.ALTAR_OF_ARCANA.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<FumeBellowsTile>> FUME_BELLOWS = TILE_ENTITY_TYPES.register("fume_bellows", () -> BlockEntityType.Builder.of(FumeBellowsTile::new, (Block[])new Block[]{(Block)BlockInit.FUME_BELLOWS.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<FumePrismTile>> FUME_PRISM = TILE_ENTITY_TYPES.register("fume_prism", () -> BlockEntityType.Builder.of(FumePrismTile::new, (Block[])new Block[]{(Block)BlockInit.FUME_PRISM.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<DisenchanterTile>> DISENCHANTER = TILE_ENTITY_TYPES.register("disenchanter", () -> BlockEntityType.Builder.of(DisenchanterTile::new, (Block[])new Block[]{(Block)BlockInit.DISENCHANTER.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<AffinityTinkerTile>> AFFINITY_TINKER = TILE_ENTITY_TYPES.register("affinity_tinker", () -> BlockEntityType.Builder.of(AffinityTinkerTile::new, (Block[])new Block[]{(Block)BlockInit.AFFINITY_TINKER.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<BookshelfTile>> BOOKSHELF = TILE_ENTITY_TYPES.register("bookshelf", () -> BlockEntityType.Builder.of(BookshelfTile::new, (Block[])new Block[]{(Block)BlockInit.BOOKSHELF.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<BookStandTile>> BOOK_STAND = TILE_ENTITY_TYPES.register("book_stand", () -> BlockEntityType.Builder.of(BookStandTile::new, (Block[])new Block[]{(Block)BlockInit.BOOK_STAND.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ScrollShelfTile>> SCROLLSHELF = TILE_ENTITY_TYPES.register("scrollshelf", () -> BlockEntityType.Builder.of(ScrollShelfTile::new, (Block[])new Block[]{(Block)BlockInit.SCROLLSHELF.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<EldrinFumeTile>> ELDRIN_FUME = TILE_ENTITY_TYPES.register("eldrin_fume", () -> BlockEntityType.Builder.of(EldrinFumeTile::new, (Block[])new Block[]{(Block)BlockInit.ELDRIN_FUME.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<EldrinMatrixTile>> ELDRIN_MATRIX = TILE_ENTITY_TYPES.register("eldrin_matrix", () -> BlockEntityType.Builder.of(EldrinMatrixTile::new, (Block[])new Block[]{(Block)BlockInit.ELDRIN_MATRIX_ARCANE.get(), (Block)BlockInit.ELDRIN_MATRIX_ENDER.get(), (Block)BlockInit.ELDRIN_MATRIX_WATER.get(), (Block)BlockInit.ELDRIN_MATRIX_FIRE.get(), (Block)BlockInit.ELDRIN_MATRIX_EARTH.get(), (Block)BlockInit.ELDRIN_MATRIX_WIND.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<RefractionLensTile>> REFRACTION_LENS = TILE_ENTITY_TYPES.register("refraction_lens", () -> BlockEntityType.Builder.of(RefractionLensTile::new, (Block[])new Block[]{(Block)BlockInit.REFRACTION_LENS_ARCANE.get(), (Block)BlockInit.REFRACTION_LENS_ENDER.get(), (Block)BlockInit.REFRACTION_LENS_FIRE.get(), (Block)BlockInit.REFRACTION_LENS_WATER.get(), (Block)BlockInit.REFRACTION_LENS_WIND.get(), (Block)BlockInit.REFRACTION_LENS_EARTH.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<PotionEffectPylonTile>> POTION_PYLON = TILE_ENTITY_TYPES.register("pylon_potion", () -> BlockEntityType.Builder.of(PotionEffectPylonTile::new, (Block[])new Block[]{(Block)BlockInit.PYLON_BIND_WOUNDS.get(), (Block)BlockInit.PYLON_CAMOUFLAGE.get(), (Block)BlockInit.PYLON_ELYTRA_FLIGHT.get(), (Block)BlockInit.PYLON_GROW.get(), (Block)BlockInit.PYLON_HASTE.get(), (Block)BlockInit.PYLON_INSIGHT.get(), (Block)BlockInit.PYLON_LEAP.get(), (Block)BlockInit.PYLON_LEVITATE.get(), (Block)BlockInit.PYLON_LEVITATE_2.get(), (Block)BlockInit.PYLON_NIGHT_VISION.get(), (Block)BlockInit.PYLON_PILGRIM.get(), (Block)BlockInit.PYLON_REPAIR.get(), (Block)BlockInit.PYLON_SHRINK.get(), (Block)BlockInit.PYLON_SLOWFALL.get(), (Block)BlockInit.PYLON_SPIDER_CLIMB.get(), (Block)BlockInit.PYLON_SWIFT_SWIM.get(), (Block)BlockInit.PYLON_WATER_BREATHING.get(), (Block)BlockInit.PYLON_WATER_WALKING.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<RechargePylonTile>> RECHARGE_PYLON = TILE_ENTITY_TYPES.register("pylon_recharge", () -> BlockEntityType.Builder.of(RechargePylonTile::new, (Block[])new Block[]{(Block)BlockInit.PYLON_RECHARGE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<ManaPylonTile>> MANA_PYLON = TILE_ENTITY_TYPES.register("pylon_mana", () -> BlockEntityType.Builder.of(ManaPylonTile::new, (Block[])new Block[]{(Block)BlockInit.PYLON_MANA.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<InterdictionPylonTile>> INTERDICTION_PYLON = TILE_ENTITY_TYPES.register("pylon_interdiction", () -> BlockEntityType.Builder.of(InterdictionPylonTile::new, (Block[])new Block[]{(Block)BlockInit.PYLON_INTERDICTION_IN.get(), (Block)BlockInit.PYLON_INTERDICTION_OUT.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<LightPylonTile>> LIGHT_PYLON = TILE_ENTITY_TYPES.register("pylon_light", () -> BlockEntityType.Builder.of(LightPylonTile::new, (Block[])new Block[]{(Block)BlockInit.PYLON_LIGHT.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<FreezePylonTile>> FREEZE_PYLON = TILE_ENTITY_TYPES.register("pylon_freeze", () -> BlockEntityType.Builder.of(FreezePylonTile::new, (Block[])new Block[]{(Block)BlockInit.PYLON_FREEZE.get()}).build(null));
    public static final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<RunicLightTile>> RUNIC_LIGHTS = TILE_ENTITY_TYPES.register("runic_light", () -> BlockEntityType.Builder.of(RunicLightTile::new, (Block[])new Block[]{(Block)BlockInit.RUNIC_LIGHT.get(), (Block)BlockInit.RUNIC_TORCH.get()}).build(null));
}


