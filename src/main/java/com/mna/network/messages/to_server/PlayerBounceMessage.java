 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class PlayerBounceMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerBounceMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("player_bounce_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PlayerBounceMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> PlayerBounceMessage.encode(v, b), PlayerBounceMessage::decode);

    @Override
    public CustomPacketPayload.Type<PlayerBounceMessage> type() { return TYPE; }

    private Vec3 velocity;

    public PlayerBounceMessage() {
        this.messageIsValid = false;
    }

    public PlayerBounceMessage(Vec3 velocity) {
        this.velocity = velocity;
        this.messageIsValid = true;
    }

    public Vec3 getVelocity() {
        return this.velocity;
    }

    public static final PlayerBounceMessage decode(FriendlyByteBuf buf) {
        PlayerBounceMessage msg = new PlayerBounceMessage();
        try {
            msg.velocity = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading PlayerBouncePacket: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(PlayerBounceMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.velocity.x);
        buf.writeDouble(message.velocity.y);
        buf.writeDouble(message.velocity.z);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        player.setDeltaMovement(this.getVelocity());
        player.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)player));
    }
}

