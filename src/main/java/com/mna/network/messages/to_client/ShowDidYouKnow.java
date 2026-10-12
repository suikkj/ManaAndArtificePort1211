 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ShowDidYouKnow
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShowDidYouKnow> TYPE = new CustomPacketPayload.Type<>(RLoc.create("show_did_you_know"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ShowDidYouKnow> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> ShowDidYouKnow.encode(v, b), ShowDidYouKnow::decode);

    @Override
    public CustomPacketPayload.Type<ShowDidYouKnow> type() { return TYPE; }

    private String messageID;

    public ShowDidYouKnow(String messageID) {
        this.messageID = messageID;
        this.messageIsValid = true;
    }

    public ShowDidYouKnow() {
        this.messageIsValid = false;
    }

    public static ShowDidYouKnow decode(FriendlyByteBuf buf) {
        ShowDidYouKnow msg = new ShowDidYouKnow();
        try {
            msg.messageID = buf.readUtf();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ShowDidYouKnow: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(ShowDidYouKnow msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.messageID);
    }

    @Override
    public void Handle(Level world, Player player) {
        ManaAndArtifice.instance.proxy.showDidYouKnow(null, this.messageID);
    }

    public static class Messages {
        public static String LENS_CAPTURE = "helptip.mna.wellspring_lens";
        public static String BUFF_CACHE = "helptip.mna.buff_cache";
        public static String MODIFIER_SHIFT = "helptip.mna.modifier_shift_click";
    }
}

