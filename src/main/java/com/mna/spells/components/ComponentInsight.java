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

public class ComponentInsight
extends PotionEffectComponent {
    public ComponentInsight(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.INSIGHT, new AttributeValuePair(Attribute.DURATION, 30.0f, 10.0f, 600.0f, 30.0f, 1.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ARCANE;
    }

    @Override
    public float initialComplexity() {
        return 5.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}

