 
package com.mna.blocks.artifice;

import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.TableBlock;
import com.mna.blocks.tileentities.OcculusTile;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.providers.NamedOcculus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OcculusBlock
extends WaterloggableBlock
implements ITranslucentBlock,
EntityBlock {
    private static final VoxelShape SHAPE = Shapes.or((VoxelShape)TableBlock.STANDALONE, (VoxelShape)Block.box((double)4.0, (double)16.0, (double)4.0, (double)12.0, (double)18.0, (double)12.0));

    public OcculusBlock() {
        super(BlockBehaviour.Properties.of().pushReaction(PushReaction.BLOCK).noOcclusion().strength(3.0f).isSuffocating(BlockInit.NEVER), false);
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        if (magic == null || !magic.isMagicUnlocked()) {
            player.sendSystemMessage((Component)Component.translatable((String)"block.mna.occulus.confusion"));
        } else {
            player.openMenu((MenuProvider)new NamedOcculus());
        }
        return InteractionResult.SUCCESS;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OcculusTile(pos, state);
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }
}

