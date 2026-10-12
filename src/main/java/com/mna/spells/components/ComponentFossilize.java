/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.spells.components.PotionEffectComponent;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class ComponentFossilize
extends PotionEffectComponent {
    public ComponentFossilize(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.FORTIFICATION, new AttributeValuePair(Attribute.DURATION, 30.0f, 30.0f, 600.0f, 30.0f, 10.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.EARTH;
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
    }

    @Override
    public float ire() {
        return 0.05f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.NEUTRAL;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.EARTH, Affinity.ICE, Affinity.ARCANE, Affinity.FIRE);
    }
}

