 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.artifice.EnderDisk;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class EnderDiscIndexSetMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EnderDiscIndexSetMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("ender_disc_index_set_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, EnderDiscIndexSetMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> EnderDiscIndexSetMessage.encode(v, b), EnderDiscIndexSetMessage::decode);

    @Override
    public CustomPacketPayload.Type<EnderDiscIndexSetMessage> type() { return TYPE; }

    private int patternIndex;
    private boolean offhand;

    public EnderDiscIndexSetMessage(int patternIndex, boolean offhand) {
        this.patternIndex = patternIndex;
        this.offhand = offhand;
        this.messageIsValid = true;
    }

    public EnderDiscIndexSetMessage() {
        this.messageIsValid = false;
    }

    public int getIndex() {
        return this.patternIndex;
    }

    public boolean isOffhand() {
        return this.offhand;
    }

    public static EnderDiscIndexSetMessage decode(FriendlyByteBuf buf) {
        EnderDiscIndexSetMessage msg = new EnderDiscIndexSetMessage();
        try {
            msg.patternIndex = buf.readInt();
            msg.offhand = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading EnderDiscIndexSetMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(EnderDiscIndexSetMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getIndex());
        buf.writeBoolean(msg.isOffhand());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ItemStack stack = this.isOffhand() ? player.getOffhandItem() : player.getMainHandItem();
        EnderDisk.setIndex(stack, this.getIndex());
    }
}

