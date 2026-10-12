 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.LodestarTile;
import com.mna.network.messages.TileEntityClientMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LodestarLogicS2CMessage
extends TileEntityClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LodestarLogicS2CMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("lodestar_logic_s2_c_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, LodestarLogicS2CMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> LodestarLogicS2CMessage.encode(v, b), LodestarLogicS2CMessage::decode);

    @Override
    public CustomPacketPayload.Type<LodestarLogicS2CMessage> type() { return TYPE; }

    private int index;
    private int numMessages;
    private int totalDataSize;
    private byte[] buffer;

    public LodestarLogicS2CMessage(BlockPos pos, int index, int numMessages, int totalDataSize, byte[] buffer) {
        super(pos);
        this.index = index;
        this.numMessages = numMessages;
        this.totalDataSize = totalDataSize;
        this.buffer = buffer;
        this.messageIsValid = true;
    }

    public LodestarLogicS2CMessage() {
        super(null);
        this.messageIsValid = false;
    }

    public static LodestarLogicS2CMessage decode(FriendlyByteBuf buf) {
        LodestarLogicS2CMessage msg = new LodestarLogicS2CMessage();
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

    public static void encode(LodestarLogicS2CMessage msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.getPosition());
        buf.writeInt(msg.index);
        buf.writeInt(msg.numMessages);
        buf.writeInt(msg.totalDataSize);
        buf.writeByteArray(msg.buffer);
    }

    @Override
    public void Handle(Level world, Player player) {
        BlockEntity blockEntity;
        if (world.isLoaded(this.pos) && (blockEntity = world.getBlockEntity(this.pos)) instanceof LodestarTile) {
            LodestarTile tile = (LodestarTile)blockEntity;
            tile.setLogicPartial(this.index, this.numMessages, this.totalDataSize, this.buffer, false);
        }
    }
}

