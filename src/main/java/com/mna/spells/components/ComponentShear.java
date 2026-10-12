/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.ShearHelper;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ComponentShear
extends SpellEffect {
    public ComponentShear(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!source.isPlayerCaster()) {
            return ComponentApplicationResult.FAIL;
        }
        List<ItemStack> loot = null;
        if (target.isEntity()) {
            loot = ShearHelper.shearEntity(context.getServerLevel(), source.getPlayer(), target.getEntity(), source.getHand());
        } else if (target.isBlock()) {
            loot = ShearHelper.shearBlock(context.getServerLevel(), target.getBlock(), target.getBlockFace(this), source.getPlayer());
        }
        if (loot == null) {
            return ComponentApplicationResult.FAIL;
        }
        Pair<Boolean, Boolean> captureRedirect = InventoryUtilities.getCaptureAndRedirect(source.getPlayer());
        if (((Boolean)captureRedirect.getFirst()).booleanValue()) {
            InventoryUtilities.redirectCaptureOrDrop(source.getPlayer(), (Level)context.getServerLevel(), loot, (Boolean)captureRedirect.getSecond());
        } else {
            RandomSource rand = context.getLevel().getRandom();
            for (ItemStack d : loot) {
                ItemEntity ent = new ItemEntity(context.getLevel(), target.getPosition().x + Math.random(), target.getPosition().y, target.getPosition().z + Math.random(), d);
                ent.setDeltaMovement(ent.getDeltaMovement().add((double)((rand.nextFloat() - rand.nextFloat()) * 0.1f), (double)(rand.nextFloat() * 0.05f), (double)((rand.nextFloat() - rand.nextFloat()) * 0.1f)));
                context.getLevel().addFreshEntity((Entity)ent);
            }
        }
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SoundEvents.BEEHIVE_SHEAR;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.EARTH;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.EARTH, Affinity.WATER, Affinity.WIND);
    }
}

