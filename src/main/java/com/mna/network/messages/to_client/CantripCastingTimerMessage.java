 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.gui.HUDOverlayRenderer;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CantripCastingTimerMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CantripCastingTimerMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("cantrip_casting_timer_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, CantripCastingTimerMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> CantripCastingTimerMessage.encode(v, b), CantripCastingTimerMessage::decode);

    @Override
    public CustomPacketPayload.Type<CantripCastingTimerMessage> type() { return TYPE; }

    private String cantripID;
    private int ticks;

    public CantripCastingTimerMessage(String cantrip, int ticks) {
        this.cantripID = cantrip;
        this.ticks = ticks;
        this.messageIsValid = true;
    }

    public CantripCastingTimerMessage() {
        this.messageIsValid = false;
    }

    public String getCantripId() {
        return this.cantripID;
    }

    public int getTicks() {
        return this.ticks;
    }

    public static CantripCastingTimerMessage decode(FriendlyByteBuf buf) {
        CantripCastingTimerMessage msg = new CantripCastingTimerMessage();
        try {
            msg.cantripID = buf.readUtf();
            msg.ticks = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ManaweavePatternDrawnMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(CantripCastingTimerMessage message, FriendlyByteBuf buf) {
        buf.writeUtf(message.getCantripId());
        buf.writeInt(message.getTicks());
    }

    @Override
    public void Handle(Level world, Player player) {
        HUDOverlayRenderer.instance.setCastingCantrip(this.getCantripId(), this.getTicks());
    }
}

