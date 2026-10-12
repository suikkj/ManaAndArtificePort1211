 
package com.mna.items;

import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.models.RunescribingTableModel;
import com.mna.items.ItemInit;
import com.mna.items.SpellIconList;
import com.mna.items.renderers.AstroBladeRenderer;
import com.mna.items.renderers.CrystallizedWindRenderer;
import com.mna.items.renderers.ItemSpellRenderer;
import com.mna.items.renderers.MarkingRuneRenderer;
import com.mna.items.renderers.fluid_jugs.FluidJugItemRenderer;
import com.mna.items.renderers.obj_gecko.EnderDiscRenderer;
import com.mna.items.renderers.obj_gecko.RunicMalusRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class ItemClientInit {
    @SubscribeEvent
    public static void onRegisterSpecialModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(ItemSpellRenderer.location_spell));
        event.register(ModelResourceLocation.standalone(ItemSpellRenderer.location_bangle));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/manaweave_bottle_texture")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/spell_book_texture")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/rote_book_texture")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/grimoire_texture")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/thaumaturgic_link_biome")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/guide_book_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/guide_book_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/alteration_book_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/alteration_book_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/mark_book_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/mark_book_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/flat_lands_book_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/flat_lands_book_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/spell_book_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/spell_book_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/spell_book_open_2")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/spell_book_closed_2")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/rote_book_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/rote_book_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/rote_book_open_2")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/rote_book_closed_2")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_basic_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_basic_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_basic_open_2")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_basic_closed_2")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_council_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_council_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_fey_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_fey_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_undead_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_undead_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_demon_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/grimoire_demon_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/recipe_copy_book_open")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/recipe_copy_book_closed")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/recipe_copy_book_open_2")));
        event.register(ModelResourceLocation.standalone(RLoc.create("item/special/recipe_copy_book_closed_2")));
        event.register(ModelResourceLocation.standalone(MarkingRuneRenderer.markingRuneItemModel));
        event.register(ModelResourceLocation.standalone(EnderDiscRenderer.disk_model));
        event.register(ModelResourceLocation.standalone(RunicMalusRenderer.hammer_model));
        event.register(ModelResourceLocation.standalone(AstroBladeRenderer.blade_model));
        event.register(ModelResourceLocation.standalone(AstroBladeRenderer.handle_model));
        event.register(ModelResourceLocation.standalone(AstroBladeRenderer.blade_model_warden));
        event.register(ModelResourceLocation.standalone(AstroBladeRenderer.handle_model_warden));
        event.register(ModelResourceLocation.standalone(FluidJugItemRenderer.jug_artifact));
        event.register(ModelResourceLocation.standalone(FluidJugItemRenderer.jug_base));
        event.register(ModelResourceLocation.standalone(RunescribingTableModel.hammer));
        event.register(ModelResourceLocation.standalone(RunescribingTableModel.chisel));
        event.register(ModelResourceLocation.standalone(CrystallizedWindRenderer.gem_model));
        event.register(ModelResourceLocation.standalone(CrystallizedWindRenderer.wisps_model));
        for (ResourceLocation spell : SpellIconList.ALL) {
            event.register(spell);
        }
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer > 0 ? -1 : ((MNADyeableItem)stack.getItem()).getColor(stack), new ItemLike[]{(ItemLike)ItemInit.MAGE_BOOTS.get(), (ItemLike)ItemInit.MAGE_ROBES.get(), (ItemLike)ItemInit.MAGE_LEGGINGS.get(), (ItemLike)ItemInit.MAGE_HOOD.get(), (ItemLike)ItemInit.PRACTITIONERS_POUCH.get(), (ItemLike)ItemInit.SPELL_BOOK.get(), (ItemLike)ItemInit.RECIPE_COPY_BOOK.get(), (ItemLike)ItemInit.GRIMOIRE.get(), (ItemLike)ItemInit.ROTE_BOOK.get(), (ItemLike)ItemInit.BANGLE.get(), (ItemLike)ItemInit.RUNE_MARKING.get(), (ItemLike)ItemInit.RUNE_MARKING_PAIR.get()});
    }
}


