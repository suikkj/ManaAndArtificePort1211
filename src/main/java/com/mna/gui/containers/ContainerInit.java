 
package com.mna.gui.containers;

import com.mna.api.gui.ContainerPylon;
import com.mna.api.gui.EldrinCapacitorPermissionsContainer;
import com.mna.blocks.BlockInit;
import com.mna.gui.containers.block.ContainerAffinityTinker;
import com.mna.gui.containers.block.ContainerArcaneAltar;
import com.mna.gui.containers.block.ContainerBookshelf;
import com.mna.gui.containers.block.ContainerDisenchanter;
import com.mna.gui.containers.block.ContainerEldrinFume;
import com.mna.gui.containers.block.ContainerInscriptionTable;
import com.mna.gui.containers.block.ContainerLodestar;
import com.mna.gui.containers.block.ContainerMagiciansWorkbench;
import com.mna.gui.containers.block.ContainerOcculus;
import com.mna.gui.containers.block.ContainerRunescribingTable;
import com.mna.gui.containers.block.ContainerScrollShelf;
import com.mna.gui.containers.block.ContainerSeerStone;
import com.mna.gui.containers.block.ContainerSpectralAnvil;
import com.mna.gui.containers.block.ContainerSpectralCraftingTable;
import com.mna.gui.containers.block.ContainerSpectralStonecutter;
import com.mna.gui.containers.block.ContainerSpellSpecialization;
import com.mna.gui.containers.block.ContainerStudyDesk;
import com.mna.gui.containers.block.ContainerThesisDesk;
import com.mna.gui.containers.block.ContainerTranscriptionTable;
import com.mna.gui.containers.entity.ContainerRift;
import com.mna.gui.containers.entity.ContainerWanderingWizard;
import com.mna.gui.containers.item.ContainerAreaAdjust;
import com.mna.gui.containers.item.ContainerAstroBlade;
import com.mna.gui.containers.item.ContainerCantrips;
import com.mna.gui.containers.item.ContainerEnderDisc;
import com.mna.gui.containers.item.ContainerFilterItem;
import com.mna.gui.containers.item.ContainerGrimoire;
import com.mna.gui.containers.item.ContainerGuideBook;
import com.mna.gui.containers.item.ContainerMarkBook;
import com.mna.gui.containers.item.ContainerPhylacteryStaff;
import com.mna.gui.containers.item.ContainerPractitionersPouch;
import com.mna.gui.containers.item.ContainerRoteBook;
import com.mna.gui.containers.item.ContainerRunicMalus;
import com.mna.gui.containers.item.ContainerSpellAdjustments;
import com.mna.gui.containers.item.ContainerSpellBook;
import com.mna.gui.containers.item.ContainerSpellName;
import com.mna.gui.containers.item.ContainerSpellRecipe;
import com.mna.gui.containers.particle.ParticleEmissionContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ContainerInit {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, "mna");
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerInscriptionTable>> INSCRIPTION_TABLE = CONTAINERS.register(ContainerInit.of(BlockInit.INSCRIPTION_TABLE), () -> IMenuTypeExtension.create(ContainerInscriptionTable::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSeerStone>> SEER_STONE = CONTAINERS.register(ContainerInit.of(BlockInit.SEER_STONE), () -> IMenuTypeExtension.create(ContainerSeerStone::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerRunescribingTable>> RUNESCRIBING_TABLE = CONTAINERS.register(ContainerInit.of(BlockInit.RUNESCRIBING_TABLE), () -> IMenuTypeExtension.create(ContainerRunescribingTable::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerLodestar>> LODESTAR = CONTAINERS.register(ContainerInit.of(BlockInit.LODESTAR), () -> IMenuTypeExtension.create(ContainerLodestar::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSpectralCraftingTable>> SPECTRAL_CRAFTING_TABLE = CONTAINERS.register(ContainerInit.of(BlockInit.SPECTRAL_CRAFTING_TABLE), () -> IMenuTypeExtension.create(ContainerSpectralCraftingTable::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSpectralAnvil>> SPECTRAL_ANVIL = CONTAINERS.register(ContainerInit.of(BlockInit.SPECTRAL_ANVIL), () -> IMenuTypeExtension.create(ContainerSpectralAnvil::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSpectralStonecutter>> SPECTRAL_STONECUTTER = CONTAINERS.register(ContainerInit.of(BlockInit.SPECTRAL_STONECUTTER), () -> IMenuTypeExtension.create(ContainerSpectralStonecutter::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerMagiciansWorkbench>> MAGICIANS_WORKBENCH = CONTAINERS.register(ContainerInit.of(BlockInit.MAGICIANS_WORKBENCH), () -> IMenuTypeExtension.create(ContainerMagiciansWorkbench::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ParticleEmissionContainer>> PARTICLE_EMITTER = CONTAINERS.register(ContainerInit.of(BlockInit.PARTICLE_EMITTER), () -> IMenuTypeExtension.create(ParticleEmissionContainer::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerThesisDesk>> THESIS_DESK = CONTAINERS.register(ContainerInit.of(BlockInit.THESIS_DESK), () -> IMenuTypeExtension.create(ContainerThesisDesk::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerStudyDesk>> STUDY_DESK = CONTAINERS.register(ContainerInit.of(BlockInit.STUDY_DESK), () -> IMenuTypeExtension.create(ContainerStudyDesk::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerDisenchanter>> DISENCHANTER = CONTAINERS.register(ContainerInit.of(BlockInit.DISENCHANTER), () -> IMenuTypeExtension.create(ContainerDisenchanter::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerEldrinFume>> ELDRIN_FUME = CONTAINERS.register(ContainerInit.of(BlockInit.ELDRIN_FUME), () -> IMenuTypeExtension.create(ContainerEldrinFume::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerAffinityTinker>> AFFINITY_TINKER = CONTAINERS.register(ContainerInit.of(BlockInit.AFFINITY_TINKER), () -> IMenuTypeExtension.create(ContainerAffinityTinker::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSpellSpecialization>> SPELL_SPECIALIZATION = CONTAINERS.register(ContainerInit.of(BlockInit.SPELL_SPECIALIZATION), () -> IMenuTypeExtension.create(ContainerSpellSpecialization::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerTranscriptionTable>> TRANSCRIPTION_TABLE = CONTAINERS.register(ContainerInit.of(BlockInit.TRANSCRIPTION_TABLE), () -> IMenuTypeExtension.create(ContainerTranscriptionTable::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerArcaneAltar>> ENSORCELLATION_STATION = CONTAINERS.register(ContainerInit.of(BlockInit.ALTAR_OF_ARCANA), () -> IMenuTypeExtension.create(ContainerArcaneAltar::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerBookshelf>> BOOKSHELF = CONTAINERS.register(ContainerInit.of(BlockInit.BOOKSHELF), () -> IMenuTypeExtension.create(ContainerBookshelf::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerScrollShelf>> SCROLLSHELF = CONTAINERS.register(ContainerInit.of(BlockInit.SCROLLSHELF), () -> IMenuTypeExtension.create(ContainerScrollShelf::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<EldrinCapacitorPermissionsContainer>> ELDRIN_PERMISSIONS = CONTAINERS.register("eldrin_capacitor_permissions", () -> IMenuTypeExtension.create(EldrinCapacitorPermissionsContainer::new));
    public static DeferredHolder<MenuType<?>, ? extends MenuType<ContainerPylon>> PYLON = CONTAINERS.register("pylon", () -> IMenuTypeExtension.create(ContainerPylon::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerPractitionersPouch>> RITUAL_KIT = CONTAINERS.register("ritual_kit", () -> IMenuTypeExtension.create(ContainerPractitionersPouch::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSpellBook>> SPELL_BOOK = CONTAINERS.register("spell_book", () -> IMenuTypeExtension.create(ContainerSpellBook::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerGrimoire>> GRIMOIRE = CONTAINERS.register("grimoire", () -> IMenuTypeExtension.create(ContainerGrimoire::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerRoteBook>> ROTE_BOOK = CONTAINERS.register("book_of_rote", () -> IMenuTypeExtension.create(ContainerRoteBook::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerMarkBook>> MARK_BOOK = CONTAINERS.register("mark_book", () -> IMenuTypeExtension.create(ContainerMarkBook::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerRunicMalus>> RUNIC_MALUS = CONTAINERS.register("runic_malus", () -> IMenuTypeExtension.create(ContainerRunicMalus::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerAstroBlade>> ASTRO_BLADE = CONTAINERS.register("astro_blade", () -> IMenuTypeExtension.create(ContainerAstroBlade::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerPhylacteryStaff>> PHYLACTERY_STAFF = CONTAINERS.register("phylactery_book", () -> IMenuTypeExtension.create(ContainerPhylacteryStaff::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerFilterItem>> FILTER_ITEM = CONTAINERS.register("filter_item", () -> IMenuTypeExtension.create(ContainerFilterItem::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerRift>> RIFT = CONTAINERS.register("rift", () -> IMenuTypeExtension.create(ContainerRift::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerGuideBook>> GUIDE_BOOK = CONTAINERS.register("guide_book", () -> IMenuTypeExtension.create(ContainerGuideBook::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerEnderDisc>> ENDER_DISC = CONTAINERS.register("ender_disc", () -> IMenuTypeExtension.create(ContainerEnderDisc::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSpellName>> SPELL_CUSTOMIZATION = CONTAINERS.register("spell_customization", () -> IMenuTypeExtension.create(ContainerSpellName::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerOcculus>> OCCULUS = CONTAINERS.register("occulus", () -> IMenuTypeExtension.create(ContainerOcculus::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSpellRecipe>> SPELL_RECIPE_LIST = CONTAINERS.register("spell_recipe_list", () -> IMenuTypeExtension.create(ContainerSpellRecipe::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerCantrips>> CANTRIPS = CONTAINERS.register("cantrips", () -> IMenuTypeExtension.create(ContainerCantrips::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerSpellAdjustments>> SPELL_ADJUSTMENTS = CONTAINERS.register("spell_adjustment", () -> IMenuTypeExtension.create(ContainerSpellAdjustments::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerAreaAdjust>> AREA_ADJUSTMENTS = CONTAINERS.register("area_adjustment", () -> IMenuTypeExtension.create(ContainerAreaAdjust::new));
    public static final DeferredHolder<MenuType<?>, ? extends MenuType<ContainerWanderingWizard>> WANDERING_WIZARD = CONTAINERS.register("wandering_wizard", () -> IMenuTypeExtension.create(ContainerWanderingWizard::new));

    static <T extends Block> String of(DeferredHolder<T, ? extends T> block) {
        return block.getId().getPath();
    }
}


