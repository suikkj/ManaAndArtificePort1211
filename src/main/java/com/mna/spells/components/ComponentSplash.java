 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.config.GeneralConfigValues;
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
import com.mna.effects.EffectInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public class ComponentSplash
extends SpellEffect {
    public ComponentSplash(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 20.0f, 5.0f, 60.0f, 5.0f, 5.0f), new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f, 5.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target.isLivingEntity()) {
            target.getLivingEntity().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()), (int)modificationData.getValue(Attribute.DURATION) * 20, 0, false, true));
        } else if (target.isBlock()) {
            if (context.countAffectedBlocks(this) > 0) {
                return ComponentApplicationResult.FAIL;
            }
            BlockPos block = target.getBlock();
            BlockState targetState = context.getServerLevel().getBlockState(block);
            if (targetState.getBlock() == Blocks.CAULDRON) {
                context.getLevel().setBlock(block, (BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue((Property)LayeredCauldronBlock.LEVEL, (Comparable)Integer.valueOf(3)), 3);
                context.addAffectedBlock(this, block);
                return ComponentApplicationResult.SUCCESS;
            }
            if (targetState.getBlock() == Blocks.WATER_CAULDRON) {
                context.getLevel().setBlock(block, (BlockState)targetState.setValue((Property)LayeredCauldronBlock.LEVEL, (Comparable)Integer.valueOf(3)), 3);
                context.addAffectedBlock(this, block);
                return ComponentApplicationResult.SUCCESS;
            }
            if (modificationData.getValue(Attribute.PRECISION) == 1.0f) {
                return ComponentApplicationResult.FAIL;
            }
            if (context.getServerLevel().dimensionType().ultraWarm()) {
                if (context.countAffectedBlocks(this) == 0) {
                    context.addAffectedBlock(this, block);
                    context.getServerLevel().playSound(null, block, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 2.6f + (context.getServerLevel().getRandom().nextFloat() - context.getServerLevel().getRandom().nextFloat()) * 0.8f);
                }
                return ComponentApplicationResult.FAIL;
            }
            context.addAffectedBlock(this, block);
            if (!this.setBlockWaterlogged((Level)context.getServerLevel(), block, targetState)) {
                block = block.offset(target.getBlockFace(this).getNormal());
                if (context.getServerLevel().isEmptyBlock(block)) {
                    context.addAffectedBlock(this, block);
                    context.getServerLevel().setBlock(block, Blocks.WATER.defaultBlockState(), 3);
                    if (!GeneralConfigValues.SplashCreatesSources) {
                        DelayedEventQueue.pushEvent(context.getLevel(), new TimedDelayedEvent<BlockPos>("water_fix", 20, block, (s, i) -> context.getServerLevel().setBlock(i, (BlockState)Blocks.WATER.defaultBlockState().setValue((Property)LiquidBlock.LEVEL, (Comparable)Integer.valueOf(15)), 3)));
                    }
                    context.getServerLevel().scheduleTick(block, Blocks.WATER, 3);
                } else if (!this.setBlockWaterlogged((Level)context.getServerLevel(), block, targetState)) {
                    return ComponentApplicationResult.FAIL;
                }
            }
        }
        return ComponentApplicationResult.SUCCESS;
    }

    private boolean setBlockWaterlogged(Level world, BlockPos pos, BlockState state) {
        if (state.hasProperty((Property)BlockStateProperties.WATERLOGGED) && !((Boolean)state.getValue((Property)BlockStateProperties.WATERLOGGED)).booleanValue()) {
            world.setBlock(pos, (BlockState)state.setValue((Property)BlockStateProperties.WATERLOGGED, (Comparable)Boolean.valueOf(true)), 3);
            return true;
        }
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WATER;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }
}

