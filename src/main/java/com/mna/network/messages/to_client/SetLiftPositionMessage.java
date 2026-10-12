 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class SetLiftPositionMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetLiftPositionMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("set_lift_position_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SetLiftPositionMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SetLiftPositionMessage.encode(v, b), SetLiftPositionMessage::decode);

    @Override
    public CustomPacketPayload.Type<SetLiftPositionMessage> type() { return TYPE; }

    Vec3 position;

    public SetLiftPositionMessage(double x, double y, double z) {
        this.position = new Vec3(x, y, z);
        this.messageIsValid = true;
    }

    public SetLiftPositionMessage() {
        this.messageIsValid = false;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public static SetLiftPositionMessage decode(FriendlyByteBuf buf) {
        SetLiftPositionMessage msg = new SetLiftPositionMessage();
        try {
            msg.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ManaweavePatternDrawnMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SetLiftPositionMessage message, FriendlyByteBuf buf) {
        buf.writeDouble(message.getPosition().x);
        buf.writeDouble(message.getPosition().y);
        buf.writeDouble(message.getPosition().z);
    }

    @Override
    public void Handle(Level world, Player player) {
        Player clientPlayer = ManaAndArtifice.instance.proxy.getClientPlayer();
        java.util.Optional<PlayerMagicProvider> magicCap = MNACap.get(clientPlayer, PlayerMagicProvider.MAGIC);
        if (!magicCap.isPresent()) {
            return;
        }
        IPlayerMagic magicProperties = (IPlayerMagic)magicCap;
        if (magicProperties == null) {
            return;
        }
        magicProperties.setLiftPosition(this.getPosition());
    }
}


