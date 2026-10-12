/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.gui.containers.particle.ParticleEmissionContainer;
import com.mna.particles.emitter.EmitterData;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ParticleEmitterTile
extends BlockEntity
implements MenuProvider,
Consumer<FriendlyByteBuf> {
    private EmitterData _data = new EmitterData();

    public ParticleEmitterTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.PARTICLE_EMITTER.get(), pos, state);
    }

    public static void ClientTick(Level level, BlockPos pos, BlockState state, ParticleEmitterTile tile) {
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        tile._data.spawn(level, Vec3.atCenterOf((Vec3i)pos), new Vec3(1.0, 0.0, 0.0));
    }

    public EmitterData getData() {
        return this._data;
    }

    public void setData(CompoundTag data) {
        this._data = EmitterData.fromTag(data);
        this.setChanged();
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        base.put("data", (Tag)this._data.getTag());
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        this._data = EmitterData.fromTag(tag.getCompound("data"));
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag data = pkt.getTag();
        this._data = EmitterData.fromTag(data.getCompound("data"));
    }

    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        compound.put("data", (Tag)this._data.getTag());
    }

    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        if (compound.contains("data")) {
            this._data = EmitterData.fromTag(compound.getCompound("data"));
        }
        super.loadAdditional(compound, registries);
    }

    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ParticleEmissionContainer(i, inventory, this._data, this.getBlockPos());
    }

    public Component getDisplayName() {
        return Component.literal((String)"");
    }

    @Override
    public void accept(FriendlyByteBuf t) {
        t.writeNbt(this._data.getTag());
        t.writeLong(this.getBlockPos().asLong());
    }
}

