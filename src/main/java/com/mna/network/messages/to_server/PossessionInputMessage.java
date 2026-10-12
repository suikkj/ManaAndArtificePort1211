 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.entities.utility.EldrinFlight;
import com.mna.network.messages.BaseServerMessage;
import com.mna.tools.PossessionHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class PossessionInputMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PossessionInputMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("possession_input_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PossessionInputMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> PossessionInputMessage.encode(v, b), PossessionInputMessage::decode);

    @Override
    public CustomPacketPayload.Type<PossessionInputMessage> type() { return TYPE; }

    private MessageType type;
    private float yaw;
    private float yawHead;
    private float pitch;
    private float forward;
    private float strafe;
    private boolean leftclick;
    private boolean jump;
    private boolean sneak;

    private PossessionInputMessage(MessageType type, float forward, float strafe, float yaw, float yawHead, float pitch, boolean leftClick, boolean jump, boolean sneak) {
        this.type = type;
        this.forward = forward;
        this.strafe = strafe;
        this.yaw = yaw;
        this.yawHead = yawHead;
        this.pitch = pitch;
        this.leftclick = leftClick;
        this.jump = jump;
        this.sneak = sneak;
        this.messageIsValid = true;
    }

    public PossessionInputMessage() {
        this.messageIsValid = false;
    }

    public MessageType getType() {
        return this.type;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getYawHead() {
        return this.yawHead;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getForward() {
        return this.forward;
    }

    public float getStrafe() {
        return this.strafe;
    }

    public boolean getLeftClick() {
        return this.leftclick;
    }

    public boolean getJump() {
        return this.jump;
    }

    public boolean getSneak() {
        return this.sneak;
    }

    public static PossessionInputMessage decode(FriendlyByteBuf buf) {
        PossessionInputMessage msg = new PossessionInputMessage();
        try {
            msg.type = MessageType.values()[buf.readInt()];
            switch (msg.type) {
                case CLICK: {
                    msg.leftclick = buf.readBoolean();
                    break;
                }
                case MOVEMENT: {
                    msg.forward = buf.readFloat();
                    msg.strafe = buf.readFloat();
                    msg.yaw = buf.readFloat();
                    msg.yawHead = buf.readFloat();
                    msg.pitch = buf.readFloat();
                    msg.jump = buf.readBoolean();
                    msg.sneak = buf.readBoolean();
                    break;
                }
            }
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading PosessionInputMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(PossessionInputMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getType().ordinal());
        switch (msg.getType()) {
            case CLICK: {
                buf.writeBoolean(msg.getLeftClick());
                break;
            }
            case MOVEMENT: {
                buf.writeFloat(msg.getForward());
                buf.writeFloat(msg.getStrafe());
                buf.writeFloat(msg.getYaw());
                buf.writeFloat(msg.getYawHead());
                buf.writeFloat(msg.getPitch());
                buf.writeBoolean(msg.getJump());
                buf.writeBoolean(msg.getSneak());
            }
        }
    }

    public static PossessionInputMessage click() {
        return new PossessionInputMessage(MessageType.CLICK, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, true, false, false);
    }

    public static PossessionInputMessage movement(float forward, float strafe, boolean jump, boolean sneak, float yaw, float yawHead, float pitch) {
        return new PossessionInputMessage(MessageType.MOVEMENT, forward, strafe, yaw, yawHead, pitch, false, jump, sneak);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        Entity e;
        int id;
        if (player.getPersistentData().contains("posessed_entity_id")) {
            id = player.getPersistentData().getInt("posessed_entity_id");
            e = player.level().getEntity(id);
            if (e != null && e instanceof Mob) {
                PossessionHelper.handleRemoteInput(this, (Player)player, (Mob)e);
            }
        }
        if (player.getPersistentData().contains("eldrin_flight_entity_id")) {
            id = player.getPersistentData().getInt("eldrin_flight_entity_id");
            e = player.level().getEntity(id);
            if (e != null && e instanceof EldrinFlight) {
                e.setYRot(this.getYaw());
                e.setXRot(this.getPitch());
            }
        }
    }

    public static enum MessageType {
        CLICK,
        MOVEMENT;

    }
}

