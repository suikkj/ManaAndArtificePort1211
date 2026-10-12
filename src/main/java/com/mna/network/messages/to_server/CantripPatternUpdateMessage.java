 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.mna.api.tools.MNACap;

public class CantripPatternUpdateMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CantripPatternUpdateMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("cantrip_pattern_update_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, CantripPatternUpdateMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> CantripPatternUpdateMessage.encode(v, b), CantripPatternUpdateMessage::decode);

    @Override
    public CustomPacketPayload.Type<CantripPatternUpdateMessage> type() { return TYPE; }

    private CompoundTag nbt;

    public CantripPatternUpdateMessage(CompoundTag nbt) {
        this.nbt = nbt;
        this.messageIsValid = true;
    }

    private CantripPatternUpdateMessage() {
        this.messageIsValid = false;
    }

    public CompoundTag getData() {
        return this.nbt;
    }

    public void setData(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public static final CantripPatternUpdateMessage decode(FriendlyByteBuf buf) {
        CantripPatternUpdateMessage msg = new CantripPatternUpdateMessage();
        try {
            msg.setData(buf.readNbt());
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(CantripPatternUpdateMessage msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.getData());
    }

    public static final CantripPatternUpdateMessage fromCapability(IPlayerMagic magic) {
        return new CantripPatternUpdateMessage(magic.getCantripData().writeToNBT(true));
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCantripData().readFromNBT(this.getData()));
    }
}

