/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.FallingBlockEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.state.BlockState
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
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public class ComponentCollapse
extends SpellEffect {
    public ComponentCollapse(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isBlock()) {
            BlockState state;
            BlockPos pos = target.getBlock();
            if (context.getServerLevel().isEmptyBlock(pos.below()) && !((state = context.getServerLevel().getBlockState(pos)).getBlock() instanceof EntityBlock) && state.getBlock() != Blocks.BEDROCK) {
                Player player;
                Object object = player = source.isPlayerCaster() ? source.getPlayer() : FakePlayerFactory.getMinecraft((ServerLevel)context.getServerLevel());
                if (BlockUtils.canDestroyBlock((LivingEntity)player, (Level)context.getServerLevel(), target.getBlock(), (Tier)Tiers.IRON)) {
                    FallingBlockEntity fbe = FallingBlockEntity.fall((Level)context.getServerLevel(), (BlockPos)pos, (BlockState)state);
                    fbe.setStartPos(pos);
                    fbe.dropItem = false;
                    context.getServerLevel().addFreshEntity((Entity)fbe);
                    return ComponentApplicationResult.SUCCESS;
                }
            }
        }
        return ComponentApplicationResult.FAIL;
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
        return 10.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return true;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}


