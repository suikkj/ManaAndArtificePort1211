 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.wizard_lab.RunescribingTableTile;
import com.mna.network.messages.TileEntityServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RunescribingTableMutexChangeMessage
extends TileEntityServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RunescribingTableMutexChangeMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("runescribing_table_mutex_change_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RunescribingTableMutexChangeMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RunescribingTableMutexChangeMessage.encode(v, b), RunescribingTableMutexChangeMessage::decode);

    @Override
    public CustomPacketPayload.Type<RunescribingTableMutexChangeMessage> type() { return TYPE; }

    long hMutex;
    long vMutex;
    int playerTier;
    boolean isRemove;

    public RunescribingTableMutexChangeMessage() {
        super(null);
        this.messageIsValid = false;
    }

    public RunescribingTableMutexChangeMessage(BlockPos position, long hMutex, long vMutex, int playerTier, boolean isRemove) {
        super(position);
        this.hMutex = hMutex;
        this.vMutex = vMutex;
        this.playerTier = playerTier;
        this.isRemove = isRemove;
        this.messageIsValid = true;
    }

    public long getHMutex() {
        return this.hMutex;
    }

    public long getVMutex() {
        return this.vMutex;
    }

    public int getPlayerTier() {
        return this.playerTier;
    }

    public boolean getIsRemove() {
        return this.isRemove;
    }

    public static final RunescribingTableMutexChangeMessage decode(FriendlyByteBuf buf) {
        RunescribingTableMutexChangeMessage msg = new RunescribingTableMutexChangeMessage();
        try {
            msg.pos = buf.readBlockPos();
            msg.hMutex = buf.readLong();
            msg.vMutex = buf.readLong();
            msg.playerTier = buf.readInt();
            msg.isRemove = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(RunescribingTableMutexChangeMessage msg, FriendlyByteBuf buf) {
        try {
            buf.writeBlockPos(msg.pos);
            buf.writeLong(msg.hMutex);
            buf.writeLong(msg.vMutex);
            buf.writeInt(msg.playerTier);
            buf.writeBoolean(msg.isRemove);
        }
        catch (Exception e) {
            ManaAndArtifice.LOGGER.error("Exception while writing RunescribingTableMutexChangeMessage: " + e);
        }
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        RunescribingTableTile.handleMutexChangeMessage(player, this);
    }
}

