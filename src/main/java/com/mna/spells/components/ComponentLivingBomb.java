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

public class ComponentLivingBomb
extends PotionEffectComponent {
    public ComponentLivingBomb(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.LIVING_BOMB, new AttributeValuePair(Attribute.DURATION, 1.0f, 1.0f, 5.0f, 1.0f, 2.25f), new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 5.0f, 1.0f, 2.25f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.FIRE;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.DEMONS;
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.FIRE, Affinity.LIGHTNING);
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}

