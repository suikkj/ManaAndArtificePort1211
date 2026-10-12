/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.common.util.FakePlayerFactory
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
import com.mna.tools.BlockUtils;
import com.mna.tools.InventoryUtilities;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public class ComponentFelledOak
extends SpellEffect {
    public ComponentFelledOak(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        ComponentApplicationResult result = ComponentApplicationResult.FAIL;
        if (!target.isBlock()) {
            return result;
        }
        Player player = source.isPlayerCaster() ? source.getPlayer() : FakePlayerFactory.getMinecraft((ServerLevel)context.getServerLevel());
        BlockPos sourceBlock = target.getBlock();
        Pair<Boolean, Boolean> captureRedirect = InventoryUtilities.getCaptureAndRedirect(player);
        if (BlockUtils.isLogOrLeaf(context.getServerLevel().getBlockState(sourceBlock)) == BlockUtils.TreeBlockTypes.LOG) {
            while (BlockUtils.isLogOrLeaf(context.getServerLevel().getBlockState(sourceBlock)) == BlockUtils.TreeBlockTypes.LOG && sourceBlock.getY() > 0) {
                sourceBlock = sourceBlock.below();
            }
            sourceBlock = sourceBlock.above();
            Pair<Boolean, List<ItemStack>> treeResult = BlockUtils.breakTreeRecursive(player, (Level)context.getServerLevel(), sourceBlock, (Boolean)captureRedirect.getFirst());
            if (((Boolean)treeResult.getFirst()).booleanValue()) {
                if (source.isPlayerCaster()) {
                    InventoryUtilities.redirectCaptureOrDrop(player, (Level)context.getServerLevel(), (List)treeResult.getSecond(), (Boolean)captureRedirect.getSecond());
                }
                result = ComponentApplicationResult.SUCCESS;
            }
        }
        return result;
    }

    @Override
    public boolean targetsBlocks() {
        return true;
    }

    @Override
    public boolean targetsEntities() {
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.EARTH;
    }

    @Override
    public float initialComplexity() {
        return 15.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}


