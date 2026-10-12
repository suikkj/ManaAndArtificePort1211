/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.effect.MobEffects
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.factions.Factions;
import com.mna.spells.components.PotionEffectComponent;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;

public class ComponentWither
extends PotionEffectComponent {
    public ComponentWither(ResourceLocation guiIcon) {
        super(guiIcon, MobEffects.WITHER, new AttributeValuePair(Attribute.DURATION, 10.0f, 10.0f, 30.0f, 1.0f, 0.75f), new AttributeValuePair(Attribute.MAGNITUDE, 2.0f, 2.0f, 5.0f, 1.0f, 0.75f));
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
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ENDER, Affinity.WATER, Affinity.EARTH, Affinity.ICE);
    }
}

