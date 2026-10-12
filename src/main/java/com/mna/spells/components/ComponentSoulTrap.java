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

public class ComponentSoulTrap
extends PotionEffectComponent {
    public ComponentSoulTrap(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.SOUL_TRAP, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 2.0f));
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.UNDEAD;
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
    public float initialComplexity() {
        return 30.0f;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.SOUL_TRAP;
    }

    @Override
    public float getSoundVolume() {
        return 0.5f;
    }
}

