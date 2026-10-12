 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PossessionMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PossessionMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("possession_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PossessionMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> PossessionMessage.encode(v, b), PossessionMessage::decode);

    @Override
    public CustomPacketPayload.Type<PossessionMessage> type() { return TYPE; }

    int entityID;

    public PossessionMessage(int entityID) {
        this.entityID = entityID;
        this.messageIsValid = true;
    }

    public PossessionMessage() {
        this.messageIsValid = false;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public static PossessionMessage decode(FriendlyByteBuf buf) {
        PossessionMessage msg = new PossessionMessage();
        try {
            msg.entityID = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading getEntityID: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(PossessionMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
    }

    @Override
    public void Handle(Level world, Player player) {
        Entity e = world.getEntity(this.getEntityID());
        if (e != null) {
            ManaAndArtifice.instance.proxy.setRenderViewEntity(e);
        }
    }
}

