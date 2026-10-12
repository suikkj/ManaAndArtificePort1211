/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.spells.shapes;

import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.bound.BoundAxe;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapeBoundAxe
extends Shape {
    public ShapeBoundAxe(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DAMAGE, 9.0f, 0.0f, 13.0f, 0.5f, 3.0f), new AttributeValuePair(Attribute.RANGE, 0.0f, 0.0f, 3.0f, 1.0f, 8.0f));
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modifiedData, ISpellDefinition recipe) {
        if (source.getCaster() == null || !source.isPlayerCaster()) {
            return Arrays.asList(SpellTarget.NONE);
        }
        ItemStack held = source.getPlayer().getItemInHand(source.getHand());
        ItemStack replacement = ((BoundAxe)ItemInit.BOUND_AXE.get()).createFromSpell(source.getCaster(), held, recipe);
        int slot = source.getHand() == InteractionHand.MAIN_HAND ? source.getPlayer().getInventory().selected : 40;
        source.getPlayer().getInventory().setItem(slot, replacement);
        return Arrays.asList(new SpellTarget((Entity)source.getCaster()));
    }

    @Override
    public float initialComplexity() {
        return 5.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public boolean spawnsTargetEntity() {
        return true;
    }

    @Override
    public boolean canBeOnRandomStaff() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.FRIENDLY;
    }

    @Override
    public boolean grantComponentRoteXP() {
        return false;
    }

    @Override
    public boolean replacesHeldItem() {
        return true;
    }
}

