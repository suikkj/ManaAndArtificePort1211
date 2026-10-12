/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.spells;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.spells.SpellCastingResult;
import com.mna.api.spells.base.ISpellDefinition;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICanContainSpell {
    public static final String TRANSCRIBED = "isTranscribedSpellItem";

    default public boolean containsSpell(ItemStack stack) {
        if (ManaAndArtificeMod.getSpellHelper() == null) {
            return false;
        }
        return ManaAndArtificeMod.getSpellHelper().containsSpell(stack);
    }

    @Nonnull
    @Deprecated(forRemoval=true)
    default public ISpellDefinition getSpell(ItemStack stack) {
        return this.getSpell(stack, null);
    }

    @Nonnull
    default public ISpellDefinition getSpell(ItemStack stack, @Nullable Player player) {
        if (ManaAndArtificeMod.getSpellHelper() == null) {
            return ISpellDefinition.EMPTY;
        }
        return ManaAndArtificeMod.getSpellHelper().parseSpellDefinition(stack, player);
    }

    default public ItemStack setSpell(ItemStack stack, ISpellDefinition spell) {
        spell.writeToNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        return stack;
    }

    default public boolean canAcceptSpell(ItemStack stack) {
        return true;
    }

    default public boolean canAcceptSpell(ItemStack stack, ISpellDefinition spell) {
        return true;
    }

    default public void setTranscribedSpell(ItemStack stack) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean(TRANSCRIBED, true);
    }

    default public boolean isTranscribedSpell(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(TRANSCRIBED);
    }

    default public boolean castFromCurioSlot(ItemStack curioStack, Player caster) {
        if (caster.isUsingItem()) {
            return false;
        }
        ISpellDefinition spell = this.getSpell(curioStack, caster);
        if (!spell.isValid()) {
            return false;
        }
        if (caster.getCooldowns().isOnCooldown(curioStack.getItem())) {
            return false;
        }
        if (spell.getShape().getPart().replacesHeldItem()) {
            caster.sendSystemMessage((Component)Component.translatable((String)"gui.mna.curio-cast.no-bound"));
            return false;
        }
        if (spell.isChanneled()) {
            caster.sendSystemMessage((Component)Component.translatable((String)"gui.mna.curio-cast.no-channeled"));
            return false;
        }
        boolean hasCharges = spell.isChargedSpell(curioStack);
        caster.swing(InteractionHand.OFF_HAND, true);
        SpellCastingResult result = ManaAndArtificeMod.getSpellHelper().playerCast(curioStack, caster, InteractionHand.OFF_HAND, !hasCharges, true);
        if (result.getCode().isConsideredSuccess() && hasCharges) {
            spell.consumeCharges(curioStack, caster);
        }
        ManaAndArtificeMod.getSpellHelper().setCooldown(curioStack, caster, spell);
        return true;
    }
}

