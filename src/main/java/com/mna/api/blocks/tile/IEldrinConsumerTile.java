 
package com.mna.api.blocks.tile;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.PlayerOwnershipRecord;
import com.mna.api.blocks.tile.IEldrinCapacitorTile;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;
import com.mna.api.tools.MNACap;

public interface IEldrinConsumerTile {
    @Deprecated
    default public float consume(Player player, BlockPos blockPos, Vec3 destPos, Affinity type, float amount, float minimumSupply) {
        if (player == null || player.getGameProfile() == null || player.getGameProfile().getId() == null) {
            return 0.0f;
        }
        return this.consume(PlayerOwnershipRecord.of(player), player.level(), blockPos, destPos, type, amount, minimumSupply);
    }

    @Deprecated
    default public float consume(Player player, BlockPos blockPos, Vec3 destPos, Affinity type, float amount) {
        if (player == null || player.getGameProfile() == null || player.getGameProfile().getId() == null) {
            return 0.0f;
        }
        return this.consume(PlayerOwnershipRecord.of(player), player.level(), blockPos, destPos, type, amount, -1.0f);
    }

    default public float consume(PlayerOwnershipRecord owner, Level world, BlockPos blockPos, Vec3 destPos, Affinity type, float amount) {
        return this.consume(owner, world, blockPos, destPos, type, amount, -1.0f);
    }

    default public float consume(PlayerOwnershipRecord owner, Level world, BlockPos blockPos, Vec3 destPos, Affinity type, float amount, float minimumSupply) {
        if (owner == null) {
            return 0.0f;
        }
        float supplied = 0.0f;
        LevelChunk chunkAccess = world.getChunkAt(blockPos);
        ChunkPos chunkPos = chunkAccess.getPos();
        ArrayList suppliers = new ArrayList();
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                chunkAccess = world.getChunk(chunkPos.x + i, chunkPos.z + j);
                MNACap.get(chunkAccess, ChunkMagicProvider.MAGIC).ifPresent(c -> suppliers.addAll(c.getKnownEldrinSuppliers().stream().map(l -> BlockPos.of((long)l)).collect(Collectors.toList())));
            }
        }
        boolean foundOneSupplier = false;
        for (BlockPos pos : (java.util.Set<BlockPos>)suppliers) {
            IEldrinCapacitorTile supplier;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity == null || !(blockEntity instanceof IEldrinCapacitorTile) || !(supplier = (IEldrinCapacitorTile)blockEntity).canSupply(type, owner) || !(pos.distSqr((Vec3i)blockPos) <= (double)(supplier.getDispersalRadius() * supplier.getDispersalRadius()))) continue;
            foundOneSupplier = true;
            if (minimumSupply > 0.0f && supplier.getChargeRate() < minimumSupply || !((supplied += supplier.supply(owner, destPos, type, amount - supplied, false)) >= amount)) continue;
            break;
        }
        if (!foundOneSupplier) {
            return -1.0f;
        }
        IChunkMagic chunkMagic = MNACap.get((IChunkMagic)world.getChunkAt(blockPos), ChunkMagicProvider.MAGIC).orElse(null);
        if (chunkMagic != null) {
            chunkMagic.addResidualMagic(supplied * 0.1f);
        }
        return supplied;
    }

    default public float consumeDirect(UUID owner, Level world, Affinity type, float amount) {
        MutableFloat consumed = new MutableFloat(0.0f);
        MNACap.get(world, ManaAndArtificeMod.getWorldMagicCapability()).ifPresent(magic -> {
            IWellspringNodeRegistry wellsprings = magic.getWellspringRegistry();
            if (wellsprings != null) {
                consumed.setValue(wellsprings.consumePower(owner, world, type, amount));
            }
        });
        return consumed.getValue().floatValue();
    }
}

