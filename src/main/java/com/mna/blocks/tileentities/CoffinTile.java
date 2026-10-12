/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.mna.blocks.tileentities;

import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.init.TileEntityInit;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CoffinTile
extends BlockEntity {
    private Optional<UUID> player = Optional.empty();
    private Player cachedPlayer;

    public CoffinTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.COFFIN.get(), pos, state);
    }

    public void setRitualPlayer(Player player) {
        this.player = player == null ? Optional.empty() : Optional.of(player.getGameProfile().getId());
        this.cachedPlayer = player;
    }

    public Player getRitualPlayer() {
        if (this.cachedPlayer == null && this.player.isPresent()) {
            this.cachedPlayer = this.getLevel().getPlayerByUUID(this.player.get());
        }
        return this.cachedPlayer;
    }

    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        if (this.player.isPresent()) {
            compound.putString("player", this.player.get().toString());
        }
    }

    public void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("player")) {
            try {
                UUID playerId = UUID.fromString(nbt.getString("player"));
                this.player = Optional.of(playerId);
            }
            catch (Exception ex) {
                ManaAndArtifice.LOGGER.warn("Unable to load player UUID for ritual coffin; it will simply act as a normal coffin.  This shouldn't happen, the save file was likely edited incorrectly.");
            }
        }
    }
}

