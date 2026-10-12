/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.spells.adjusters;

import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SpellAdjustingContext {
    public final ItemStack stack;
    public final ISpellDefinition spell;
    public final LivingEntity caster;
    public final SpellCastStage stage;
    public final InteractionHand castHand;
    public final boolean curioCast;

    public SpellAdjustingContext(ItemStack stack, ISpellDefinition spell, LivingEntity caster, InteractionHand hand, boolean curioCast, SpellCastStage stage) {
        this.stack = stack;
        this.caster = caster;
        this.stage = stage;
        this.spell = spell;
        this.castHand = hand;
        this.curioCast = curioCast;
    }
}

