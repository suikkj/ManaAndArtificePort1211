/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  software.bernie.geckolib.animatable.GeoBlockEntity
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationController$SoundKeyframeHandler
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent
 *  software.bernie.geckolib.animation.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.mna.blocks.tileentities.wizard_lab;

import com.mna.api.sound.SFX;
import com.mna.blocks.artifice.FumeBellowsBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.EldrinFumeTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FumeBellowsTile
extends BlockEntity
implements GeoBlockEntity,
AnimationController.SoundKeyframeHandler<FumeBellowsTile> {
    protected static final RawAnimation BELLOW = RawAnimation.begin().thenLoop("animation.fume_filter_bellows.bellow");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public FumeBellowsTile(BlockPos pPos, BlockState pBlockState) {
        super((BlockEntityType)TileEntityInit.FUME_BELLOWS.get(), pPos, pBlockState);
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, this::animationControl);
        controller.setSoundKeyframeHandler((AnimationController.SoundKeyframeHandler)this);
        controllers.add(new AnimationController[]{controller});
    }

    protected <E extends FumeBellowsTile> PlayState animationControl(AnimationState<E> state) {
        if (this.isModifiedFumeActive()) {
            return state.setAndContinue(BELLOW);
        }
        return PlayState.STOP;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean isModifiedFumeActive() {
        BlockPos leftOffset;
        BlockEntity leftEntity;
        BlockState state = this.getBlockState();
        BlockPos pos = this.getBlockPos();
        Direction dir = (Direction)state.getValue((Property)HorizontalDirectionalBlock.FACING);
        if (((Boolean)state.getValue((Property)FumeBellowsBlock.FUME_RIGHT)).booleanValue()) {
            BlockPos rightOffset = pos.offset(dir.getClockWise().getNormal());
            BlockEntity rightEntity = this.level.getBlockEntity(rightOffset);
            if (rightEntity != null && rightEntity instanceof EldrinFumeTile) {
                EldrinFumeTile fume = (EldrinFumeTile)rightEntity;
                return fume.isActive();
            }
        } else if (((Boolean)state.getValue((Property)FumeBellowsBlock.FUME_LEFT)).booleanValue() && (leftEntity = this.level.getBlockEntity(leftOffset = pos.offset(dir.getCounterClockWise().getNormal()))) != null && leftEntity instanceof EldrinFumeTile) {
            EldrinFumeTile fume = (EldrinFumeTile)leftEntity;
            return fume.isActive();
        }
        return false;
    }

    public void handle(SoundKeyframeEvent<FumeBellowsTile> event) {
        if (event.getKeyframeData().getSound().equals("bellow")) {
            BlockPos pos = this.getBlockPos();
            double d0 = (double)pos.getX() + 0.5;
            double d1 = pos.getY();
            double d2 = (double)pos.getZ() + 0.5;
            this.level.playLocalSound(d0, d1, d2, SFX.Block.FUME_BELLOWS, SoundSource.BLOCKS, 0.25f, 1.0f, false);
        }
    }
}

