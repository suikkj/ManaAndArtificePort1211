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
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class ComponentWindWall
extends PotionEffectComponent {
    public ComponentWindWall(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.WIND_WALL, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 10.0f), new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f, 20.0f));
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
        return 20.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.FRIENDLY;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.WATER, Affinity.WIND, Affinity.LIGHTNING, Affinity.ICE);
    }
}

