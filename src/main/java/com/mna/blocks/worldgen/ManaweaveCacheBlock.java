/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.NoteBlockInstrument
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.phys.BlockHitResult
 *  net.neoforged.fml.ModList
 */
package com.mna.blocks.worldgen;

import com.mna.api.blocks.IManaweaveNotifiable;
import com.mna.api.blocks.WaterloggableBlockWithOffset;
import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.ManaweaveCacheTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.interop.lootr.LootrIntegration;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.ModList;

public class ManaweaveCacheBlock
extends WaterloggableBlockWithOffset
implements EntityBlock,
IManaweaveNotifiable,
IDontCreateBlockItem {
    public ManaweaveCacheBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().explosionResistance(200.0f).destroyTime(200.0f).isSuffocating(BlockInit.NEVER), false, new BlockPos(0, 1, 0), new BlockPos(0, 2, 0));
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return 10;
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ManaweaveCacheTile cache;
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null && tile instanceof ManaweaveCacheTile && (cache = (ManaweaveCacheTile)tile).isOpen() && !cache.isBuff()) {
            if (!world.isClientSide) {
                if (ModList.get().isLoaded("lootr")) {
                    LootrIntegration.openCache(cache, (ServerPlayer)player);
                } else {
                    cache.unpackLootTable(player);
                    player.openMenu((MenuProvider)cache);
                }
            }
            return InteractionResult.sidedSuccess((boolean)world.isClientSide);
        }
        return InteractionResult.FAIL;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaweaveCacheTile(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityInit.MANAWEAVE_CACHE.get() ? (lvl, pos, state1, be) -> ManaweaveCacheTile.Tick(lvl, pos, state1, (ManaweaveCacheTile)be) : null;
    }

    public RenderShape getRenderShape(BlockState p_60550_) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean notify(Level world, BlockPos pos, BlockState state, List<IManaweavePattern> patterns, @Nullable LivingEntity caster) {
        if (!world.isClientSide && caster instanceof Player) {
            Player player = (Player)caster;
            ManaweaveCacheTile te = (ManaweaveCacheTile)world.getBlockEntity(pos);
            return te.notifyPattern(player, patterns.get(0).getRegistryId());
        }
        return false;
    }

    @Override
    public void onRemove(BlockState oldState, Level world, BlockPos pos, BlockState newState, boolean drops) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity blockentity = world.getBlockEntity(pos);
            if (blockentity instanceof ManaweaveCacheTile && ((ManaweaveCacheTile)blockentity).isOpen() && !((ManaweaveCacheTile)blockentity).isBuff()) {
                Containers.dropContents((Level)world, (BlockPos)pos, (Container)((ManaweaveCacheTile)blockentity));
                world.updateNeighbourForOutputSignal(pos, (Block)this);
            }
            super.onRemove(oldState, world, pos, newState, drops);
        }
    }

    @Override
    protected void spawnDestroyParticles(Level world, Player player, BlockPos pos, BlockState state) {
        for (int i = 0; i < 20; ++i) {
            world.addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, ((Block)BlockInit.CHIMERITE_ARCANE_STONE_STRAIGHT.get()).defaultBlockState()), (double)pos.getX() + Math.random(), (double)pos.getY() + Math.random(), (double)pos.getZ() + Math.random(), 0.0, 0.05 * Math.random(), 0.0);
        }
    }
}


