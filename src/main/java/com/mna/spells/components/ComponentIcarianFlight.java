/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.effects.EffectInit;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.resources.ResourceLocation;

public class ComponentIcarianFlight
extends PotionEffectComponent {
    public ComponentIcarianFlight(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.ICARIAN_FLIGHT, new AttributeValuePair(Attribute.DURATION, 1.0f, 1.0f, 5.0f, 50.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WIND;
    }

    @Override
    public float initialComplexity() {
        return 50.0f;
    }

    @Override
    public boolean canBeOnRandomStaff() {
        return false;
    }

    @Override
    public boolean isCraftable(SpellCraftingContext context) {
        return false;
    }
}

