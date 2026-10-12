 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.mna.api.tools.MNACap;

public class SendCodexEntryReadMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SendCodexEntryReadMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("send_codex_entry_read_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SendCodexEntryReadMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SendCodexEntryReadMessage.encode(v, b), SendCodexEntryReadMessage::decode);

    @Override
    public CustomPacketPayload.Type<SendCodexEntryReadMessage> type() { return TYPE; }

    long hash;

    public SendCodexEntryReadMessage(long hash) {
        this.hash = hash;
        this.messageIsValid = true;
    }

    public SendCodexEntryReadMessage() {
        this.messageIsValid = false;
    }

    public static SendCodexEntryReadMessage decode(FriendlyByteBuf buf) {
        SendCodexEntryReadMessage msg = new SendCodexEntryReadMessage();
        try {
            msg.hash = buf.readLong();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading SendCodexEntryReadMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SendCodexEntryReadMessage msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.hash);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> p.setCodexEntryRead(this.hash));
    }
}

