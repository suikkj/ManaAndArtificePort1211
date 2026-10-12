 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MindVisionMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MindVisionMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("mind_vision_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, MindVisionMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> MindVisionMessage.encode(v, b), MindVisionMessage::decode);

    @Override
    public CustomPacketPayload.Type<MindVisionMessage> type() { return TYPE; }

    int entityID;

    public MindVisionMessage(int entityID) {
        this.entityID = entityID;
        this.messageIsValid = true;
    }

    public MindVisionMessage() {
        this.messageIsValid = false;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public static MindVisionMessage decode(FriendlyByteBuf buf) {
        MindVisionMessage msg = new MindVisionMessage();
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

    public static void encode(MindVisionMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
    }

    @Override
    public void Handle(Level world, Player player) {
        if (this.getEntityID() == -1) {
            ManaAndArtifice.instance.proxy.resetRenderViewEntity();
        } else {
            Entity e = world.getEntity(this.getEntityID());
            if (e != null) {
                ManaAndArtifice.instance.proxy.setRenderViewEntity(e);
            }
        }
    }
}

