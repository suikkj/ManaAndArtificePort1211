 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.entities.constructs.animated.Construct;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class AnimatedConstructSyncRequestMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AnimatedConstructSyncRequestMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("animated_construct_sync_request_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, AnimatedConstructSyncRequestMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> AnimatedConstructSyncRequestMessage.encode(v, b), AnimatedConstructSyncRequestMessage::decode);

    @Override
    public CustomPacketPayload.Type<AnimatedConstructSyncRequestMessage> type() { return TYPE; }

    private int entityID;
    private boolean diagnosticsOnly;

    private AnimatedConstructSyncRequestMessage() {
        this.messageIsValid = false;
    }

    public AnimatedConstructSyncRequestMessage(int entityID, boolean diagnosticsOnly) {
        this();
        this.entityID = entityID;
        this.diagnosticsOnly = diagnosticsOnly;
        this.messageIsValid = true;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public boolean getDiagnosticsOnly() {
        return this.diagnosticsOnly;
    }

    public static AnimatedConstructSyncRequestMessage decode(FriendlyByteBuf buf) {
        AnimatedConstructSyncRequestMessage msg = new AnimatedConstructSyncRequestMessage();
        try {
            msg.entityID = buf.readInt();
            msg.diagnosticsOnly = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading AnimatedConstructSyncMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(AnimatedConstructSyncRequestMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
        buf.writeBoolean(msg.diagnosticsOnly);
    }

    public static AnimatedConstructSyncRequestMessage fromConstruct(Construct construct, boolean diagnosticsOnly) {
        AnimatedConstructSyncRequestMessage msg = new AnimatedConstructSyncRequestMessage(construct.getId(), diagnosticsOnly);
        return msg;
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        Construct eac = (Construct)player.level().getEntity(this.getEntityID());
        if (eac != null) {
            eac.setRequestingDiagnostics(this.getDiagnosticsOnly());
            ServerMessageDispatcher.sendEntityStateMessage(eac, player);
            eac.setRequestingDiagnostics(false);
        }
    }
}

