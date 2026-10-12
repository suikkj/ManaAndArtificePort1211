 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.gui.containers.block.ContainerLodestar;
import com.mna.network.messages.TileEntityServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class LodestarLogicSetMessage
extends TileEntityServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LodestarLogicSetMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("lodestar_logic_set_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, LodestarLogicSetMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> LodestarLogicSetMessage.encode(v, b), LodestarLogicSetMessage::decode);

    @Override
    public CustomPacketPayload.Type<LodestarLogicSetMessage> type() { return TYPE; }

    private int index;
    private int numMessages;
    private int totalDataSize;
    private byte[] buffer;

    public LodestarLogicSetMessage(BlockPos pos, int index, int numMessages, int totalDataSize, byte[] buffer) {
        super(pos);
        this.index = index;
        this.numMessages = numMessages;
        this.totalDataSize = totalDataSize;
        this.buffer = buffer;
        this.messageIsValid = true;
    }

    public LodestarLogicSetMessage() {
        super(null);
        this.messageIsValid = false;
    }

    public static LodestarLogicSetMessage decode(FriendlyByteBuf buf) {
        LodestarLogicSetMessage msg = new LodestarLogicSetMessage();
        try {
            msg.pos = buf.readBlockPos();
            msg.index = buf.readInt();
            msg.numMessages = buf.readInt();
            msg.totalDataSize = buf.readInt();
            msg.buffer = buf.readByteArray();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading LodestarLogicSetMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(LodestarLogicSetMessage msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.getPosition());
        buf.writeInt(msg.index);
        buf.writeInt(msg.numMessages);
        buf.writeInt(msg.totalDataSize);
        buf.writeByteArray(msg.buffer);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        AbstractContainerMenu abstractContainerMenu;
        if (player.containerMenu != null && (abstractContainerMenu = player.containerMenu) instanceof ContainerLodestar) {
            ContainerLodestar lodestarContainer = (ContainerLodestar)abstractContainerMenu;
            lodestarContainer.updateTileLogic(this.index, this.numMessages, this.totalDataSize, this.buffer, true);
        }
    }
}

