/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.MenuScreens
 *  net.minecraft.world.inventory.MenuType
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
 */
package com.mna.gui;

import com.mna.ManaAndArtifice;
import com.mna.api.gui.GuiEldrinCapacitorPermissions;
import com.mna.gui.HUDOverlayRenderer;
import com.mna.gui.block.GuiAffinityTinker;
import com.mna.gui.block.GuiArcanaAltar;
import com.mna.gui.block.GuiBookshelf;
import com.mna.gui.block.GuiDisenchanter;
import com.mna.gui.block.GuiEldrinFume;
import com.mna.gui.block.GuiInscriptionTable;
import com.mna.gui.block.GuiLodestarV2;
import com.mna.gui.block.GuiMagiciansWorkbench;
import com.mna.gui.block.GuiOcculus;
import com.mna.gui.block.GuiParticleEmission;
import com.mna.gui.block.GuiPylon;
import com.mna.gui.block.GuiRunescribingTable;
import com.mna.gui.block.GuiScrollShelf;
import com.mna.gui.block.GuiSeerStone;
import com.mna.gui.block.GuiSpectralCraftingTable;
import com.mna.gui.block.GuiSpellSpecialization;
import com.mna.gui.block.GuiStudyDesk;
import com.mna.gui.block.GuiThesisDesk;
import com.mna.gui.block.GuiTranscriptionTable;
import com.mna.gui.containers.ContainerInit;
import com.mna.gui.entity.GuiRift;
import com.mna.gui.entity.GuiWanderingWizard;
import com.mna.gui.item.GuiAreaAdjust;
import com.mna.gui.item.GuiAstroBlade;
import com.mna.gui.item.GuiCantrips;
import com.mna.gui.item.GuiEnderDisc;
import com.mna.gui.item.GuiFilterItem;
import com.mna.gui.item.GuiGrimoire;
import com.mna.gui.item.GuiGuideBook;
import com.mna.gui.item.GuiMarkBook;
import com.mna.gui.item.GuiPhylacteryStaff;
import com.mna.gui.item.GuiPractitionersPouch;
import com.mna.gui.item.GuiRoteBook;
import com.mna.gui.item.GuiRunicMalus;
import com.mna.gui.item.GuiSpellAdjust;
import com.mna.gui.item.GuiSpellBook;
import com.mna.gui.item.GuiSpellNaming;
import com.mna.gui.item.GuiSpellRecipe;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class GuiInit {
    @SubscribeEvent
    @OnlyIn(value=Dist.CLIENT)
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        HUDOverlayRenderer.instance = new HUDOverlayRenderer();
        ManaAndArtifice.LOGGER.info("M&A -> Gui Screens Registered");
    }
    @SubscribeEvent(dist = Dist.CLIENT)
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register((MenuType)((MenuType)ContainerInit.RITUAL_KIT.get()), GuiPractitionersPouch::new);
        event.register((MenuType)((MenuType)ContainerInit.SPELL_BOOK.get()), GuiSpellBook::new);
        event.register((MenuType)((MenuType)ContainerInit.GRIMOIRE.get()), GuiGrimoire::new);
        event.register((MenuType)((MenuType)ContainerInit.ROTE_BOOK.get()), GuiRoteBook::new);
        event.register((MenuType)((MenuType)ContainerInit.MARK_BOOK.get()), GuiMarkBook::new);
        event.register((MenuType)((MenuType)ContainerInit.RUNIC_MALUS.get()), GuiRunicMalus::new);
        event.register((MenuType)((MenuType)ContainerInit.ASTRO_BLADE.get()), GuiAstroBlade::new);
        event.register((MenuType)((MenuType)ContainerInit.PHYLACTERY_STAFF.get()), GuiPhylacteryStaff::new);
        event.register((MenuType)((MenuType)ContainerInit.FILTER_ITEM.get()), GuiFilterItem::new);
        event.register((MenuType)((MenuType)ContainerInit.GUIDE_BOOK.get()), GuiGuideBook::new);
        event.register((MenuType)((MenuType)ContainerInit.ENDER_DISC.get()), GuiEnderDisc::new);
        event.register((MenuType)((MenuType)ContainerInit.SPELL_RECIPE_LIST.get()), GuiSpellRecipe::new);
        event.register((MenuType)((MenuType)ContainerInit.CANTRIPS.get()), GuiCantrips::new);
        event.register((MenuType)((MenuType)ContainerInit.SPELL_CUSTOMIZATION.get()), GuiSpellNaming::new);
        event.register((MenuType)((MenuType)ContainerInit.SPELL_ADJUSTMENTS.get()), GuiSpellAdjust::new);
        event.register((MenuType)((MenuType)ContainerInit.AREA_ADJUSTMENTS.get()), GuiAreaAdjust::new);
        event.register((MenuType)((MenuType)ContainerInit.RIFT.get()), GuiRift::new);
        event.register((MenuType)((MenuType)ContainerInit.WANDERING_WIZARD.get()), GuiWanderingWizard::new);
        event.register((MenuType)((MenuType)ContainerInit.SPECTRAL_CRAFTING_TABLE.get()), GuiSpectralCraftingTable::new);
        event.register((MenuType)((MenuType)ContainerInit.PARTICLE_EMITTER.get()), GuiParticleEmission::new);
        event.register((MenuType)((MenuType)ContainerInit.OCCULUS.get()), GuiOcculus::new);
        event.register((MenuType)((MenuType)ContainerInit.LODESTAR.get()), GuiLodestarV2::new);
        event.register((MenuType)((MenuType)ContainerInit.SEER_STONE.get()), GuiSeerStone::new);
        event.register((MenuType)((MenuType)ContainerInit.BOOKSHELF.get()), GuiBookshelf::new);
        event.register((MenuType)((MenuType)ContainerInit.SCROLLSHELF.get()), GuiScrollShelf::new);
        event.register((MenuType)((MenuType)ContainerInit.MAGICIANS_WORKBENCH.get()), GuiMagiciansWorkbench::new);
        event.register((MenuType)((MenuType)ContainerInit.INSCRIPTION_TABLE.get()), GuiInscriptionTable::new);
        event.register((MenuType)((MenuType)ContainerInit.RUNESCRIBING_TABLE.get()), GuiRunescribingTable::new);
        event.register((MenuType)((MenuType)ContainerInit.THESIS_DESK.get()), GuiThesisDesk::new);
        event.register((MenuType)((MenuType)ContainerInit.STUDY_DESK.get()), GuiStudyDesk::new);
        event.register((MenuType)((MenuType)ContainerInit.DISENCHANTER.get()), GuiDisenchanter::new);
        event.register((MenuType)((MenuType)ContainerInit.ELDRIN_FUME.get()), GuiEldrinFume::new);
        event.register((MenuType)((MenuType)ContainerInit.AFFINITY_TINKER.get()), GuiAffinityTinker::new);
        event.register((MenuType)((MenuType)ContainerInit.SPELL_SPECIALIZATION.get()), GuiSpellSpecialization::new);
        event.register((MenuType)((MenuType)ContainerInit.TRANSCRIPTION_TABLE.get()), GuiTranscriptionTable::new);
        event.register((MenuType)((MenuType)ContainerInit.ENSORCELLATION_STATION.get()), GuiArcanaAltar::new);
        event.register((MenuType)((MenuType)ContainerInit.ELDRIN_PERMISSIONS.get()), GuiEldrinCapacitorPermissions::new);
        event.register((MenuType)((MenuType)ContainerInit.PYLON.get()), GuiPylon::new);
    }
}


