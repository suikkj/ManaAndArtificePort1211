 
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

public class UpdateCodexUnlocksMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateCodexUnlocksMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("update_codex_unlocks_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, UpdateCodexUnlocksMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> UpdateCodexUnlocksMessage.encode(v, b), UpdateCodexUnlocksMessage::decode);

    @Override
    public CustomPacketPayload.Type<UpdateCodexUnlocksMessage> type() { return TYPE; }

    private long[] hashes;

    public UpdateCodexUnlocksMessage(long[] hashes) {
        this.hashes = hashes;
        this.messageIsValid = true;
    }

    public UpdateCodexUnlocksMessage() {
        this.messageIsValid = false;
    }

    public static UpdateCodexUnlocksMessage decode(FriendlyByteBuf buf) {
        UpdateCodexUnlocksMessage msg = new UpdateCodexUnlocksMessage();
        try {
            msg.hashes = buf.readLongArray();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading UpdateCodexUnlocksMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(UpdateCodexUnlocksMessage msg, FriendlyByteBuf buf) {
        buf.writeLongArray(msg.hashes);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> p.setEntriesUnlocked(this.hashes));
    }
}

