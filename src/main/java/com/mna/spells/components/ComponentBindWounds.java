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

public class ComponentBindWounds
extends PotionEffectComponent {
    public ComponentBindWounds(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.BIND_WOUNDS, new AttributeValuePair(Attribute.DURATION, 30.0f, 30.0f, 180.0f, 30.0f, 12.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 10.0f));
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WATER;
    }

    @Override
    public float initialComplexity() {
        return 30.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 80;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.SELF;
    }
}

