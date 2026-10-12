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
import net.minecraft.resources.ResourceLocation;

public class ComponentFrailty
extends PotionEffectComponent {
    public ComponentFrailty(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.FRAILTY, new AttributeValuePair(Attribute.DURATION, 5.0f, 5.0f, 30.0f, 5.0f, 2.0f), new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 5.0f, 1.0f, 20.0f));
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.FEY;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public float initialComplexity() {
        return 30.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public boolean isSilverSpell() {
        return true;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}

