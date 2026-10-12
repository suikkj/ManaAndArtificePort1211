 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RitualKitIndexSetMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RitualKitIndexSetMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("ritual_kit_index_set_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RitualKitIndexSetMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RitualKitIndexSetMessage.encode(v, b), RitualKitIndexSetMessage::decode);

    @Override
    public CustomPacketPayload.Type<RitualKitIndexSetMessage> type() { return TYPE; }

    private int patternIndex;
    private boolean offhand;

    public RitualKitIndexSetMessage(int patternIndex, boolean offhand) {
        this.patternIndex = patternIndex;
        this.offhand = offhand;
        this.messageIsValid = true;
    }

    public RitualKitIndexSetMessage() {
        this.messageIsValid = false;
    }

    public int getIndex() {
        return this.patternIndex;
    }

    public boolean isOffhand() {
        return this.offhand;
    }

    public static RitualKitIndexSetMessage decode(FriendlyByteBuf buf) {
        RitualKitIndexSetMessage msg = new RitualKitIndexSetMessage();
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

    public static void encode(RitualKitIndexSetMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getIndex());
        buf.writeBoolean(msg.offhand);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ItemStack stack = this.isOffhand() ? player.getOffhandItem() : player.getMainHandItem();
        PractitionersPouch.setIndex(stack, this.getIndex());
    }
}

