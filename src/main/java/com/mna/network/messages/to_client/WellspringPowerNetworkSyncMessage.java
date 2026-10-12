 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class WellspringPowerNetworkSyncMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WellspringPowerNetworkSyncMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("wellspring_power_network_sync_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, WellspringPowerNetworkSyncMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> WellspringPowerNetworkSyncMessage.encode(v, b), WellspringPowerNetworkSyncMessage::decode);

    @Override
    public CustomPacketPayload.Type<WellspringPowerNetworkSyncMessage> type() { return TYPE; }

    CompoundTag wellspringData;

    public WellspringPowerNetworkSyncMessage(CompoundTag nbt) {
        this.wellspringData = nbt;
        this.messageIsValid = true;
    }

    private WellspringPowerNetworkSyncMessage() {
        this.messageIsValid = false;
    }

    public static WellspringPowerNetworkSyncMessage decode(FriendlyByteBuf buf) {
        WellspringPowerNetworkSyncMessage msg = new WellspringPowerNetworkSyncMessage();
        try {
            msg.wellspringData = buf.readNbt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading WellspringPowerNetworkSyncMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(WellspringPowerNetworkSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.wellspringData);
    }

    public static WellspringPowerNetworkSyncMessage from(ServerLevel world, ServerPlayer player, boolean fullSync) {
        CompoundTag nbt = new CompoundTag();
        MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(m -> m.getWellspringRegistry().serializeNetworkStrength((Player)player, nbt, fullSync));
        return new WellspringPowerNetworkSyncMessage(nbt);
    }

    @Override
    public void Handle(Level world, Player player) {
        MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(m -> m.getWellspringRegistry().deserializeNetworkStrength(ManaAndArtifice.instance.proxy.getClientPlayer(), this.wellspringData));
    }
}

