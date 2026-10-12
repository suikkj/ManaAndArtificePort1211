 
package com.mna.blocks.decoration;

import com.mna.blocks.BlockInit;
import com.mna.blocks.decoration.SimpleRotationalBlock;
import com.mna.blocks.tileentities.ParticleEmitterTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ParticleEmitterBlock
extends SimpleRotationalBlock
implements EntityBlock {
    public ParticleEmitterBlock() {
        super(BlockBehaviour.Properties.of().strength(3.0f, 100000.0f).noCollission().noOcclusion().sound(SoundType.GLASS).isSuffocating(BlockInit.NEVER));
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ParticleEmitterTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.PARTICLE_EMITTER.get() && level.isClientSide() ? (lvl, pos, state1, be) -> ParticleEmitterTile.ClientTick(lvl, pos, state1, (ParticleEmitterTile)be) : null;
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        BlockEntity tile;
        if (!worldIn.isClientSide && (tile = worldIn.getBlockEntity(pos)) != null && tile instanceof ParticleEmitterTile) {
            player.openMenu((MenuProvider)((ParticleEmitterTile)tile), (Consumer)((ParticleEmitterTile)tile));
        }
        return InteractionResult.sidedSuccess((boolean)worldIn.isClientSide);
    }
}


