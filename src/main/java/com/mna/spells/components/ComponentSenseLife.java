/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ComponentSenseLife
extends PotionEffectComponent {
    public ComponentSenseLife(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.SENSE_LIFE, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 2.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 1.0f, 1.0f, 4.0f, 1.0f, 4.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 90;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BLOOD;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
    }

    @Override
    public float initialComplexity() {
        return 30.0f;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Buff.BLOOD;
    }
}

