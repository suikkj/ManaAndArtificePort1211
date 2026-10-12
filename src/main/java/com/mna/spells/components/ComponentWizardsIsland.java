/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.common.util.FakePlayerFactory
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
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.entities.utility.RisingBlock;
import com.mna.tools.BlockUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public class ComponentWizardsIsland
extends SpellEffect {
    public ComponentWizardsIsland(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 3.0f, 1.0f, 5.0f, 1.0f, 1.0f), new AttributeValuePair(Attribute.SPEED, 1.0f, 1.0f, 20.0f, 1.0f, 1.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isBlock()) {
            BlockPos pos = target.getBlock();
            return this.raiseBlock(0, pos, source, target, modificationData, context);
        }
        return ComponentApplicationResult.FAIL;
    }

    private ComponentApplicationResult raiseBlock(int attempt, BlockPos pos, SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!context.getServerLevel().isEmptyBlock(pos) && context.getServerLevel().getBlockState(pos).getFluidState().isEmpty()) {
            if (!context.getServerLevel().isEmptyBlock(pos.above()) && context.getServerLevel().getFluidState(pos.above()).isEmpty()) {
                if (attempt < 2) {
                    DelayedEventQueue.pushEvent(context.getLevel(), new TimedDelayedEvent<Integer>("Delayed Earthsky", 2, attempt++, (s, i) -> this.raiseBlock((int)i, pos, source, target, modificationData, context)));
                }
                return ComponentApplicationResult.DELAYED;
            }
            BlockState state = context.getServerLevel().getBlockState(pos);
            if (!(state.getBlock() instanceof EntityBlock) && state.getBlock() != Blocks.BEDROCK) {
                Player player;
                Object object = player = source.isPlayerCaster() ? source.getPlayer() : FakePlayerFactory.getMinecraft((ServerLevel)context.getServerLevel());
                if (BlockUtils.canDestroyBlock((LivingEntity)player, (Level)context.getServerLevel(), target.getBlock(), (Tier)Tiers.IRON)) {
                    RisingBlock fbe = new RisingBlock((Level)context.getServerLevel(), (float)pos.getX() + 0.5f, pos.getY(), (float)pos.getZ() + 0.5f, state);
                    fbe.setStartPos(pos);
                    fbe.setMaxAge((int)(modificationData.getValue(Attribute.DURATION) * 20.0f));
                    fbe.setSpeed(modificationData.getValue(Attribute.SPEED) / 20.0f);
                    context.getServerLevel().addFreshEntity((Entity)fbe);
                }
            }
        }
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WIND;
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
    public boolean targetsEntities() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ARCANE, Affinity.WATER, Affinity.EARTH, Affinity.WIND, Affinity.ICE);
    }
}

