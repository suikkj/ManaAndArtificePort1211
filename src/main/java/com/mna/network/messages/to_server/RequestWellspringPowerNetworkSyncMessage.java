 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RequestWellspringPowerNetworkSyncMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestWellspringPowerNetworkSyncMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("request_wellspring_power_network_sync_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RequestWellspringPowerNetworkSyncMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RequestWellspringPowerNetworkSyncMessage.encode(v, b), RequestWellspringPowerNetworkSyncMessage::decode);

    @Override
    public CustomPacketPayload.Type<RequestWellspringPowerNetworkSyncMessage> type() { return TYPE; }

    private boolean nearbyNodes;

    public RequestWellspringPowerNetworkSyncMessage(boolean nearbyNodes) {
        this.nearbyNodes = nearbyNodes;
        this.messageIsValid = true;
    }

    public RequestWellspringPowerNetworkSyncMessage() {
        this.messageIsValid = false;
    }

    public static final RequestWellspringPowerNetworkSyncMessage decode(FriendlyByteBuf buf) {
        RequestWellspringPowerNetworkSyncMessage msg = new RequestWellspringPowerNetworkSyncMessage();
        try {
            msg.nearbyNodes = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RequestWellspringPowerNetworkSyncMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public boolean getNearbyNodes() {
        return this.nearbyNodes;
    }

    public static final void encode(RequestWellspringPowerNetworkSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.getNearbyNodes());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ServerMessageDispatcher.sendWellspringPowerNetworkSyncMessage(world, player, true);
        if (this.getNearbyNodes()) {
            ServerMessageDispatcher.sendWellspringSyncMessage(world, player, 512);
        }
    }
}

