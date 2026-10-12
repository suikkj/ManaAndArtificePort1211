 
package com.mna.rituals.effects;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.parts.Modifier;
import com.mna.blocks.tileentities.ChalkRuneTile;
import com.mna.entities.utility.PresentItem;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.ItemModifierBook;
import com.mna.items.sorcery.ItemSpell;
import com.mna.items.sorcery.SpellBook;
import com.mna.spells.crafting.SpellRecipe;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

public class RitualEffectAlteration
extends RitualEffect {
    public RitualEffectAlteration(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public boolean applyStartCheckInCreative() {
        return true;
    }

    @Override
    public Component canRitualStart(IRitualContext context) {
        MutableObject output = new MutableObject();
        context.getAllPositions().stream().forEach(pos -> {
            BlockEntity te = context.getLevel().getBlockEntity(pos.getBlockPos());
            if (te != null && te instanceof ChalkRuneTile) {
                Optional<Modifier> selectedModifier;
                ChalkRuneTile tecr = (ChalkRuneTile)te;
                ItemStack reagentStack = tecr.getItem(0);
                if (reagentStack.getItem() instanceof ItemSpell && !(reagentStack.getItem() instanceof SpellBook)) {
                    if (((ItemSpell)reagentStack.getItem()).isTranscribedSpell(reagentStack)) {
                        output.setValue(Component.translatable((String)"ritual.mna.spell_modification.transcribed"));
                    } else {
                        SpellRecipe recipe = SpellRecipe.fromNBT(reagentStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                        if (recipe.countModifiers() >= 3) {
                            output.setValue(Component.translatable((String)"ritual.mna.spell_modification.at-maximum"));
                        }
                    }
                } else if (reagentStack.getItem() == ItemInit.MODIFIER_BOOK.get() && !(selectedModifier = ItemModifierBook.getModifier(reagentStack)).isPresent()) {
                    output.setValue(Component.translatable((String)"ritual.mna.spell_modification.not-selected"));
                }
            }
        });
        return (Component)output.getValue();
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        ItemStack book = context.getCollectedReagents().get(9);
        ItemStack spell = context.getCollectedReagents().get(8);
        if (book.isEmpty() || spell.isEmpty() || book.getItem() != ItemInit.MODIFIER_BOOK.get() || !(spell.getItem() instanceof ICanContainSpell)) {
            return false;
        }
        Optional<Modifier> m = ItemModifierBook.getModifier(book);
        SpellRecipe recipe = SpellRecipe.fromNBT(spell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        boolean didAdd = false;
        if (m.isPresent()) {
            didAdd = recipe.addModifier(m.get());
        }
        ItemStack outputStack = spell.copy();
        recipe.writeToNBT(outputStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        PresentItem epi = new PresentItem(context.getLevel(), (double)context.getCenter().getX() + 0.5, context.getCenter().getY() + 1, (double)context.getCenter().getZ() + 0.5, outputStack);
        context.getLevel().addFreshEntity((Entity)epi);
        return didAdd;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }

    @Override
    protected boolean modifyRitualReagentsAndPatterns(ItemStack dataStack, IRitualContext context) {
        if (dataStack.getItem() != ItemInit.MODIFIER_BOOK.get()) {
            return false;
        }
        MutableBoolean successfullyReplaced = new MutableBoolean(false);
        ItemModifierBook.getModifier(dataStack).ifPresent(m -> ItemModifierBook.getRecipe(m, context.getLevel()).ifPresent(r -> {
            context.replaceReagents(ResourceLocation.parse("mna:dynamic-modifier-1"), (NonNullList<ResourceLocation>)NonNullList.of(ResourceLocation.parse(""), (Object[])r.getRequiredItems()));
            context.replacePatterns((NonNullList<ResourceLocation>)NonNullList.of(ResourceLocation.parse(""), (Object[])r.getRequiredPatterns()));
            successfullyReplaced.setTrue();
        }));
        return successfullyReplaced.booleanValue();
    }

    @Override
    public SoundEvent getLoopSound(IRitualContext context) {
        return super.getLoopSound(context);
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        return super.spawnRitualParticles(context);
    }
}

