 
package com.mna.blocks.worldgen;

import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MATarmaRootBlock
extends BushBlock
implements ICutoutBlock,
IDontCreateBlockItem {
    protected static final VoxelShape SHAPE = Block.box((double)5.0, (double)0.0, (double)5.0, (double)11.0, (double)10.0, (double)11.0);

    public MATarmaRootBlock(BlockBehaviour.Properties properties) {
        super(properties.noCollission().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ));
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Vec3 vec3d = state.getOffset(worldIn, pos);
        return SHAPE.move(vec3d.x, vec3d.y, vec3d.z);
    }

    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(worldIn, pos, state, player);
        return state;
        super.playerWillDestroy(worldIn, pos, state, player);
        if (player.getMainHandItem().isEmpty()) {
            player.hurt(player.damageSources().cactus(), 1.0f);
        }
    }

    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (entity instanceof LivingEntity && !(entity instanceof Bee)) {
            entity.hurt(entity.damageSources().cactus(), 1.0f);
        }
    }

    @Override
    protected com.mojang.serialization.MapCodec<MATarmaRootBlock> codec() {
        return simpleCodec(MATarmaRootBlock::new);
    }
}


