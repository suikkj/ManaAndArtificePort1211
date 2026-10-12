/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ComponentCurseOfAgony
extends PotionEffectComponent {
    public ComponentCurseOfAgony(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.CURSE_OF_AGONY, new AttributeValuePair[0]);
        this.addReagent(new ItemStack((ItemLike)Items.CHORUS_FRUIT), new IFaction[0]);
    }

    @Override
    protected boolean checkPermanencyReagents(SpellSource source, SpellTarget target, SpellContext context) {
        return true;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.DEMONS;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BLOOD;
    }

    @Override
    public float initialComplexity() {
        return 50.0f;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.CURSE_OF_AGONY;
    }

    @Override
    public float getSoundVolume() {
        return 0.75f;
    }
}

