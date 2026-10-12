/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.blocks.BlockInit;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.spells.components.PotionEffectComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ComponentShrink
extends PotionEffectComponent {
    public ComponentShrink(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.REDUCE, new AttributeValuePair(Attribute.DURATION, 120.0f, 30.0f, 600.0f, 30.0f, 5.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 3.0f, 1.0f, 9.0f, 1.0f, 5.0f), new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 4.0f, 1.0f, 20.0f));
        this.addPermanencyReagent(new ItemStack((ItemLike)BlockInit.CERUBLOSSOM.get()), Factions.FEY);
        this.makePermanentFor(Factions.FEY);
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (source.getCaster() != target.getEntity()) {
            int range = (int)modificationData.getValue(Attribute.MAGNITUDE);
            if (!this.casterTeamCheck(source, target) || !this.magnitudeHealthCheck(source, target, range, 20)) {
                return ComponentApplicationResult.FAIL;
            }
        }
        return super.ApplyEffect(source, target, modificationData, context);
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
    public float initialComplexity() {
        return 40.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.SELF;
    }
}

