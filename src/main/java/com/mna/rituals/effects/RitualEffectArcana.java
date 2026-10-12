 
package com.mna.rituals.effects;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.sound.SFX;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.entities.utility.PresentItem;
import com.mna.events.EventDispatcher;
import com.mna.items.ItemInit;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import com.mna.api.tools.MNACap;

public class RitualEffectArcana
extends RitualEffect {
    public RitualEffectArcana(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Nullable
    private SpellRecipe getRecipe(IRitualContext context) {
        ItemStack recipeItem = ItemStack.EMPTY;
        for (ItemStack stack : context.getCollectedReagents()) {
            if (!SpellRecipe.isReagentContainer(stack)) continue;
            recipeItem = stack;
            break;
        }
        if (recipeItem == null) {
            return null;
        }
        SpellRecipe recipe = SpellRecipe.fromNBT(recipeItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (recipe.isValid()) {
            return recipe;
        }
        return null;
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        SpellRecipe recipe = this.getRecipe(context);
        if (recipe == null || context.getCaster() == null) {
            return null;
        }
        SpellCraftingContext spc = new SpellCraftingContext(context.getCaster());
        if (((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).isCraftable(spc) && recipe.getModifiers().stream().allMatch(m -> m.isCraftable(spc)) && recipe.getComponents().stream().allMatch(c -> ((SpellEffect)c.getPart()).isCraftable(spc))) {
            return null;
        }
        return Component.translatable((String)"mna:rituals/arcana.start_failed");
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        SpellRecipe recipe = this.getRecipe(context);
        if (recipe == null) {
            return false;
        }
        ItemStack spell = new ItemStack((ItemLike)ItemInit.SPELL.get());
        recipe.setMysterious(false);
        recipe.writeToNBT(spell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        context.getLevel().playSound(null, (double)context.getCenter().getX(), (double)context.getCenter().getY(), (double)context.getCenter().getZ(), SFX.Event.Player.SPELL_CREATED, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (context.getCaster() instanceof ServerPlayer) {
            CustomAdvancementTriggers.CRAFT_SPELL.trigger((ServerPlayer)context.getCaster(), recipe);
        }
        PresentItem item = new PresentItem(context.getLevel(), (float)context.getCenter().above().getX() + 0.5f, context.getCenter().above().getY(), (float)context.getCenter().above().getZ() + 0.5f, spell);
        context.getLevel().addFreshEntity((Entity)item);
        if (recipe.isValid()) {
            EventDispatcher.DispatchSpellCrafted(recipe, context.getCaster());
        }
        if (GeneralConfigValues.LiteMode) {
            MNACap.get(context.getCaster(), PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
                if (recipe.getShape() != null) {
                    r.addRoteXP(null, (ISpellComponent)((ModifiedSpellPart)recipe.getShape()).getPart(), ((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).requiredXPForRote());
                }
                recipe.iterateComponents(comp -> {
                    if (comp != null) {
                        r.addRoteXP(null, (ISpellComponent)comp.getPart(), ((SpellEffect)comp.getPart()).requiredXPForRote());
                    }
                });
                recipe.getModifiers().forEach(m -> r.addRoteXP(null, (ISpellComponent)m, m.requiredXPForRote()));
            });
        }
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }

    @Override
    protected boolean modifyRitualReagentsAndPatterns(ItemStack dataStack, IRitualContext context) {
        if (!SpellRecipe.isReagentContainer(dataStack)) {
            return false;
        }
        context.replaceReagents(ResourceLocation.parse("mna:dynamic-shape"), SpellRecipe.getShapeReagents(dataStack));
        context.replaceReagents(ResourceLocation.parse("mna:dynamic-component"), SpellRecipe.getComponentReagents(dataStack));
        context.replaceReagents(ResourceLocation.parse("mna:dynamic-modifier-1"), SpellRecipe.getModifierReagents(dataStack, 0));
        context.replaceReagents(ResourceLocation.parse("mna:dynamic-modifier-2"), SpellRecipe.getModifierReagents(dataStack, 1));
        context.replaceReagents(ResourceLocation.parse("mna:dynamic-modifier-3"), SpellRecipe.getModifierReagents(dataStack, 2));
        context.replacePatterns(SpellRecipe.getPatterns(dataStack));
        return true;
    }
}

