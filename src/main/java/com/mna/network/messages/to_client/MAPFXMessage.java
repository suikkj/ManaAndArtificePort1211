 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.capabilities.entity.MAPFXProvider;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class MAPFXMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MAPFXMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("m_a_p_f_x_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, MAPFXMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> MAPFXMessage.encode(v, b), MAPFXMessage::decode);

    @Override
    public CustomPacketPayload.Type<MAPFXMessage> type() { return TYPE; }

    private long flags;
    private int entityID;

    public MAPFXMessage(int entityID, long flags) {
        this.flags = flags;
        this.entityID = entityID;
        this.messageIsValid = true;
    }

    public MAPFXMessage() {
        this.messageIsValid = false;
    }

    public long getFlags() {
        return this.flags;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public static MAPFXMessage decode(FriendlyByteBuf buf) {
        MAPFXMessage msg = new MAPFXMessage();
        try {
            msg.flags = buf.readLong();
            msg.entityID = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MAPFXMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(MAPFXMessage msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.getFlags());
        buf.writeInt(msg.getEntityID());
    }

    @Override
    public void Handle(Level world, Player player) {
        Entity e = world.getEntity(this.getEntityID());
        if (e != null && e instanceof LivingEntity) {
            MNACap.get((LivingEntity)e, MAPFXProvider.MAPFX).ifPresent(p -> p.setFlags(this.getFlags()));
        }
    }
}

