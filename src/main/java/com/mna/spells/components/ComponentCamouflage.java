/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.effects.EffectInit;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.resources.ResourceLocation;

public class ComponentCamouflage
extends PotionEffectComponent {
    public ComponentCamouflage(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.CAMOUFLAGE, new AttributeValuePair(Attribute.DURATION, 30.0f, 30.0f, 300.0f, 30.0f, 2.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 1.0f, 1.0f, 2.0f, 1.0f, 10.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WATER;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }
}

