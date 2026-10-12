 
package com.mna.blocks.ritual;

import com.mna.api.blocks.interfaces.IDontCreateBlockItem;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.utility.DisplayReagents;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import com.mna.api.tools.MNACap;

public class RitualTeleportLocationBlock
extends Block
implements IDontCreateBlockItem {
    public RitualTeleportLocationBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).noLootTable().randomTicks().strength(3.0f, 10000.0f));
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        java.util.Optional<WorldMagicProvider> worldMagicContainer = MNACap.get(worldIn, WorldMagicProvider.MAGIC);
        if (worldMagicContainer.isPresent()) {
            IWorldMagic worldMagic = (IWorldMagic)worldMagicContainer.get();
            List<ResourceLocation> reagents = worldMagic.getRitualTeleportBlockReagents(pos);
            Entity displayEntity = ((EntityType)EntityInit.REAGENT_ENTITY.get()).spawn((ServerLevel)worldIn, null, player, pos.above(), MobSpawnType.SPAWN_EGG, true, true);
            if (displayEntity != null && displayEntity instanceof DisplayReagents) {
                ((DisplayReagents)displayEntity).setResourceLocations(reagents);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        java.util.Optional worldMagicContainer;
        if (state.getBlock() != newState.getBlock() && (worldMagicContainer = MNACap.get(worldIn, WorldMagicProvider.MAGIC)).isPresent()) {
            IWorldMagic worldMagic = (IWorldMagic)worldMagicContainer.get();
            worldMagic.removeRitualTeleportLocation(pos);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (worldIn.isClientSide) {
            worldIn.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_VELOCITY.get())), (double)((float)pos.getX() + rand.nextFloat()), (double)pos.above().getY(), (double)((float)pos.getZ() + rand.nextFloat()), 0.0, (double)0.1f, 0.0);
        }
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}


