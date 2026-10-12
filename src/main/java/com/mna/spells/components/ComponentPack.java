/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.state.BlockState
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
import com.mna.entities.utility.FillHole;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ComponentPack
extends SpellEffect {
    public ComponentPack(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RADIUS, 3.0f, 1.0f, 7.0f, 1.0f, 1.0f), new AttributeValuePair(Attribute.HEIGHT, 3.0f, 1.0f, 7.0f, 1.0f, 1.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 50;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (source.isPlayerCaster() && target.isBlock()) {
            BlockPos targetPos = target.getBlock().relative(target.getBlockFace(this));
            Player caster = source.getPlayer();
            Block toPlace = null;
            ItemStack offhandItem = caster.getItemInHand(source.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!offhandItem.isEmpty() && offhandItem.getItem() instanceof BlockItem) {
                toPlace = ((BlockItem)offhandItem.getItem()).getBlock();
            } else {
                BlockState impactState = context.getServerLevel().getBlockState(target.getBlock());
                if (impactState != null && (toPlace = impactState.getBlock()) instanceof EntityBlock) {
                    return ComponentApplicationResult.FAIL;
                }
            }
            if (toPlace == null) {
                return ComponentApplicationResult.FAIL;
            }
            byte height = (byte)modificationData.getValue(Attribute.HEIGHT);
            byte radius = (byte)modificationData.getValue(Attribute.RADIUS);
            FillHole efh = new FillHole((Level)context.getServerLevel(), toPlace, targetPos, caster, height, radius);
            context.getServerLevel().addFreshEntity((Entity)efh);
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
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
}

