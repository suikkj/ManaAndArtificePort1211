 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.entities.constructs.animated.Construct;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ConstructHornMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConstructHornMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("construct_horn_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ConstructHornMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> ConstructHornMessage.encode(v, b), ConstructHornMessage::decode);

    @Override
    public CustomPacketPayload.Type<ConstructHornMessage> type() { return TYPE; }

    private int entityID;

    public ConstructHornMessage() {
        this.messageIsValid = false;
    }

    public ConstructHornMessage(int entityID) {
        this.entityID = entityID;
        this.messageIsValid = true;
    }

    public int getEntityId() {
        return this.entityID;
    }

    public static ConstructHornMessage decode(FriendlyByteBuf buf) {
        ConstructHornMessage msg = new ConstructHornMessage();
        try {
            msg.entityID = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ConstructHornMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(ConstructHornMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityId());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        Entity e = player.level().getEntity(this.getEntityId());
        if (e != null && e instanceof Construct) {
            Construct construct = (Construct)e;
            construct.soundHorn();
        }
    }
}

