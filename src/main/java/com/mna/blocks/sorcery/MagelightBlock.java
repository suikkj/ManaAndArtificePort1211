/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.item.DyeItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.pathfinder.PathType
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.Nullable
 */
package com.mna.blocks.sorcery;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.MagelightTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MagelightBlock
extends WaterloggableBlock
implements IDontCreateBlockItem,
EntityBlock {
    protected static final VoxelShape SHAPE = Block.box((double)5.0, (double)5.0, (double)5.0, (double)11.0, (double)11.0, (double)11.0);

    public MagelightBlock() {
        super(BlockBehaviour.Properties.of().strength(0.0f).noOcclusion().noCollission().lightLevel(state -> 15).noLootTable().isSuffocating(BlockInit.NEVER), false);
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return true;
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MagelightTile(pos, state);
    }

    @Nullable
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return PathType.OPEN;
    }

    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        for (int i = 0; i < 50; ++i) {
            level.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, -0.25 + Math.random() * 0.5, Math.random() * 0.25, -0.25 + Math.random() * 0.5);
        }
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (!world.isClientSide()) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (heldItem.getItem() instanceof DyeItem) {
                this.setLightColor(world, pos, ((DyeItem)heldItem.getItem()).getDyeColor());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    public void setLightColor(Level world, BlockPos pos, DyeColor color) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null && te instanceof MagelightTile) {
            MagelightTile tile = (MagelightTile)te;
            tile.setColor(color.getFireworkColor());
        }
    }

    public void setLightColor(Level world, BlockPos pos, int color) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null && te instanceof MagelightTile) {
            MagelightTile tile = (MagelightTile)te;
            tile.setColor(color);
        }
    }
}

