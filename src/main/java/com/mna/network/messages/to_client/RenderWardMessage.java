 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class RenderWardMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RenderWardMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("render_ward_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RenderWardMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RenderWardMessage.encode(v, b), RenderWardMessage::decode);

    @Override
    public CustomPacketPayload.Type<RenderWardMessage> type() { return TYPE; }

    public static final int RENDER_DURATION = 100;
    private long[] positions;

    public RenderWardMessage(long ... blockPositions) {
        this.positions = blockPositions;
        this.messageIsValid = true;
    }

    public RenderWardMessage() {
        this.messageIsValid = false;
    }

    public static RenderWardMessage decode(FriendlyByteBuf buf) {
        RenderWardMessage msg = new RenderWardMessage();
        try {
            msg.positions = buf.readLongArray();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RenderWardMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(RenderWardMessage msg, FriendlyByteBuf buf) {
        buf.writeLongArray(msg.positions);
    }

    @Override
    public void Handle(Level world, Player player) {
        if (this.positions == null) {
            return;
        }
        for (int i = 0; i < this.positions.length; ++i) {
            ManaAndArtifice.instance.proxy.addWardRenderer(this.positions[i], 100);
        }
    }
}

