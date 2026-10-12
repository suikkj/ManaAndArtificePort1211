 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SetLiftSpeedMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetLiftSpeedMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("set_lift_speed_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SetLiftSpeedMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SetLiftSpeedMessage.encode(v, b), SetLiftSpeedMessage::decode);

    @Override
    public CustomPacketPayload.Type<SetLiftSpeedMessage> type() { return TYPE; }

    private int entityID;
    private float liftSpeed;

    public SetLiftSpeedMessage(int entityID, float liftSpeed) {
        this.entityID = entityID;
        this.liftSpeed = liftSpeed;
        this.messageIsValid = true;
    }

    public SetLiftSpeedMessage() {
        this.messageIsValid = false;
    }

    public static SetLiftSpeedMessage decode(FriendlyByteBuf buf) {
        SetLiftSpeedMessage msg = new SetLiftSpeedMessage();
        try {
            msg.entityID = buf.readInt();
            msg.liftSpeed = buf.readFloat();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading CloudstepJumpMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SetLiftSpeedMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityID);
        buf.writeFloat(msg.liftSpeed);
    }

    @Override
    public void Handle(Level world, Player player) {
        Entity e = world.getEntity(this.entityID);
        if (e != null) {
            e.getPersistentData().putFloat("lift_speed", this.liftSpeed);
        }
    }
}

