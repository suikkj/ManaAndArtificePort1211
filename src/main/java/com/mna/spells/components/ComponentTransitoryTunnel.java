/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
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
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.TransitoryTunnelBlock;
import com.mna.blocks.tileentities.TransitoryTunnelTile;
import com.mna.tools.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ComponentTransitoryTunnel
extends SpellEffect {
    public ComponentTransitoryTunnel(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 5.0f, 1.0f, 15.0f, 1.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 2000;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isBlock()) {
            BlockState state;
            int duration = (int)MathUtils.clamp(modificationData.getValue(Attribute.DURATION), 1.0f, 15.0f);
            BlockPos pos = target.getBlock();
            if (!context.getServerLevel().isEmptyBlock(pos) && context.getServerLevel().getBlockState(pos).getFluidState().isEmpty() && !((state = context.getServerLevel().getBlockState(pos)).getBlock() instanceof EntityBlock) && state.getBlock() != Blocks.BEDROCK) {
                context.getServerLevel().setBlock(pos, ((TransitoryTunnelBlock)BlockInit.TRANSITORY_TUNNEL.get()).defaultBlockState(), 1);
                BlockEntity te = context.getServerLevel().getBlockEntity(pos);
                if (te != null && te instanceof TransitoryTunnelTile) {
                    ((TransitoryTunnelTile)te).setDurationAndPreviousState(duration * 20, state);
                }
                return ComponentApplicationResult.SUCCESS;
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
        return Affinity.ENDER;
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

