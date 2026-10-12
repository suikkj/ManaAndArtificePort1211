 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class WellspringSyncMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WellspringSyncMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("wellspring_sync_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, WellspringSyncMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> WellspringSyncMessage.encode(v, b), WellspringSyncMessage::decode);

    @Override
    public CustomPacketPayload.Type<WellspringSyncMessage> type() { return TYPE; }

    CompoundTag wellspringData;

    public WellspringSyncMessage(CompoundTag nbt) {
        this.wellspringData = nbt;
        this.messageIsValid = true;
    }

    private WellspringSyncMessage() {
        this.messageIsValid = false;
    }

    public static WellspringSyncMessage decode(FriendlyByteBuf buf) {
        WellspringSyncMessage msg = new WellspringSyncMessage();
        try {
            msg.wellspringData = buf.readNbt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading WellspringSyncMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(WellspringSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.wellspringData);
    }

    @Override
    public void Handle(Level world, Player player) {
        MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(m -> m.getWellspringRegistry().readFromNBT(this.wellspringData));
    }
}

