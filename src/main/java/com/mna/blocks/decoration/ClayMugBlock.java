/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.decoration;

import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ClayMugBlock
extends HorizontalDirectionalBlock
implements IDontCreateBlockItem {
    public static final BooleanProperty HAS_LIQUID = BooleanProperty.create((String)"has_liquid");
    private static final VoxelShape BASE = Block.box((double)4.5, (double)0.0, (double)4.5, (double)11.5, (double)5.0, (double)11.5);

    public ClayMugBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion().isSuffocating(BlockInit.NEVER));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
        builder.add(new Property[]{HAS_LIQUID});
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (((Boolean)state.getValue((Property)HAS_LIQUID)).booleanValue()) {
            Vec3 pPos = Vec3.atCenterOf((Vec3i)pos);
            level.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get())).setGravity(0.0f).setColor(24, 24, 24).setMaxAge(40).setScale(0.05f), pPos.x - (double)0.1f + Math.random() * 0.2, pPos.y - (double)0.2f, pPos.z - (double)0.1f + Math.random() * 0.2, 0.0, 0.005, 0.0);
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)((BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)ctx.getHorizontalDirection())).setValue((Property)HAS_LIQUID, (Comparable)Boolean.valueOf(ctx.getItemInHand().getItem() == ItemInit.MANA_TEA.get()));
    }

    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }

    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return BASE;
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide) {
            return InteractionResult.CONSUME;
        }
        ItemStack stack = ItemStack.EMPTY;
        stack = (Boolean)state.getValue((Property)HAS_LIQUID) != false ? new ItemStack((ItemLike)ItemInit.MANA_TEA.get()) : new ItemStack((ItemLike)ItemInit.CLAY_MUG.get());
        if (player.addItem(stack)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        return InteractionResult.SUCCESS;
    }
}

