 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import com.mna.spells.components.ComponentTrueInvisibility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PlayerGreaterInvisibilityDisabledMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerGreaterInvisibilityDisabledMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("player_greater_invisibility_disabled_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PlayerGreaterInvisibilityDisabledMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> PlayerGreaterInvisibilityDisabledMessage.encode(v, b), PlayerGreaterInvisibilityDisabledMessage::decode);

    @Override
    public CustomPacketPayload.Type<PlayerGreaterInvisibilityDisabledMessage> type() { return TYPE; }

    private int duration;
    private int entityID;

    public PlayerGreaterInvisibilityDisabledMessage(int entityID, int duration) {
        this.entityID = entityID;
        this.duration = duration;
        this.messageIsValid = true;
    }

    public PlayerGreaterInvisibilityDisabledMessage() {
        this.messageIsValid = false;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public static PlayerGreaterInvisibilityDisabledMessage decode(FriendlyByteBuf buf) {
        PlayerGreaterInvisibilityDisabledMessage msg = new PlayerGreaterInvisibilityDisabledMessage();
        try {
            msg.duration = buf.readInt();
            msg.entityID = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading PlayerGreaterInvisibilityDisabledMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(PlayerGreaterInvisibilityDisabledMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getDuration());
        buf.writeInt(msg.getEntityID());
    }

    @Override
    public void Handle(Level world, Player player) {
        Entity e = world.getEntity(this.getEntityID());
        if (e != null && e instanceof Player) {
            Player locatedPlayer = (Player)e;
            ComponentTrueInvisibility.DisableForSeconds((LivingEntity)locatedPlayer, this.duration);
        }
    }
}

