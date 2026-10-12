 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.mna.api.tools.MNACap;

public class PlayerFocusDistanceMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerFocusDistanceMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("player_focus_distance_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PlayerFocusDistanceMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> PlayerFocusDistanceMessage.encode(v, b), PlayerFocusDistanceMessage::decode);

    @Override
    public CustomPacketPayload.Type<PlayerFocusDistanceMessage> type() { return TYPE; }

    private float delta;
    private float maximum;

    public PlayerFocusDistanceMessage() {
        this.messageIsValid = false;
    }

    public PlayerFocusDistanceMessage(float delta, float maximum) {
        this.messageIsValid = true;
        this.delta = delta;
        this.maximum = maximum;
    }

    public float getDelta() {
        return this.delta;
    }

    public float getMaximum() {
        return this.maximum;
    }

    public static final PlayerFocusDistanceMessage decode(FriendlyByteBuf buf) {
        PlayerFocusDistanceMessage msg = new PlayerFocusDistanceMessage();
        try {
            msg.delta = buf.readFloat();
            msg.maximum = buf.readFloat();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading PlayerBouncePacket: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(PlayerFocusDistanceMessage message, FriendlyByteBuf buf) {
        buf.writeFloat(message.getDelta());
        buf.writeFloat(message.getMaximum());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.offsetFocusDistance(this.getDelta(), this.getMaximum()));
    }
}

