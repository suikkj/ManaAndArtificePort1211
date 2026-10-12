/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.effects.EffectInit;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.resources.ResourceLocation;

public class ComponentDisjunction
extends PotionEffectComponent {
    public ComponentDisjunction(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.DISJUNCTION, new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 10.0f, 1.0f, 50.0f), new AttributeValuePair(Attribute.DURATION, 15.0f, 15.0f, 600.0f, 15.0f, 10.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public float initialComplexity() {
        return 50.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}

