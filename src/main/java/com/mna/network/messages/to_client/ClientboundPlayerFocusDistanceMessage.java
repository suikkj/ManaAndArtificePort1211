 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class ClientboundPlayerFocusDistanceMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundPlayerFocusDistanceMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("clientbound_player_focus_distance_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ClientboundPlayerFocusDistanceMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> ClientboundPlayerFocusDistanceMessage.encode(v, b), ClientboundPlayerFocusDistanceMessage::decode);

    @Override
    public CustomPacketPayload.Type<ClientboundPlayerFocusDistanceMessage> type() { return TYPE; }

    private float delta;
    private float maximum;

    public ClientboundPlayerFocusDistanceMessage() {
        this.messageIsValid = false;
    }

    public ClientboundPlayerFocusDistanceMessage(float delta, float maximum) {
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

    public static final ClientboundPlayerFocusDistanceMessage decode(FriendlyByteBuf buf) {
        ClientboundPlayerFocusDistanceMessage msg = new ClientboundPlayerFocusDistanceMessage();
        try {
            msg.delta = buf.readFloat();
            msg.maximum = buf.readFloat();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ClientboundPlayerFocusDistanceMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(ClientboundPlayerFocusDistanceMessage message, FriendlyByteBuf buf) {
        buf.writeFloat(message.getDelta());
        buf.writeFloat(message.getMaximum());
    }

    @Override
    public void Handle(Level world, Player player) {
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.setFocusDistance(this.getDelta()));
    }
}

