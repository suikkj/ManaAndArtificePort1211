 
package com.mna.blocks.worldgen;

import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MAFlowerBlock
extends BushBlock
implements ICutoutBlock,
IDontCreateBlockItem {
    protected static final VoxelShape SHAPE = Block.box((double)5.0, (double)0.0, (double)5.0, (double)11.0, (double)10.0, (double)11.0);

    public MAFlowerBlock(BlockBehaviour.Properties properties) {
        super(properties.noCollission().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ));
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Vec3 vec3d = state.getOffset(worldIn, pos);
        return SHAPE.move(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    protected com.mojang.serialization.MapCodec<MAFlowerBlock> codec() {
        return simpleCodec(MAFlowerBlock::new);
    }
}


