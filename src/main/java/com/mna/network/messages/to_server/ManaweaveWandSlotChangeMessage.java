 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.manaweaving.ItemManaweaverWand;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ManaweaveWandSlotChangeMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ManaweaveWandSlotChangeMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("manaweave_wand_slot_change_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ManaweaveWandSlotChangeMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> ManaweaveWandSlotChangeMessage.encode(v, b), ManaweaveWandSlotChangeMessage::decode);

    @Override
    public CustomPacketPayload.Type<ManaweaveWandSlotChangeMessage> type() { return TYPE; }

    private ResourceLocation selected;
    private boolean offhand;

    public ManaweaveWandSlotChangeMessage(ResourceLocation selected, boolean offhand) {
        this.selected = selected;
        this.offhand = offhand;
        this.messageIsValid = true;
    }

    public ManaweaveWandSlotChangeMessage() {
        this.messageIsValid = false;
    }

    public ResourceLocation getSelected() {
        return this.selected;
    }

    public boolean isOffhand() {
        return this.offhand;
    }

    public static ManaweaveWandSlotChangeMessage decode(FriendlyByteBuf buf) {
        ManaweaveWandSlotChangeMessage msg = new ManaweaveWandSlotChangeMessage();
        try {
            if (buf.readBoolean()) {
                msg.selected = buf.readResourceLocation();
            }
            msg.offhand = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ManaweaveWandSlotChangeMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(ManaweaveWandSlotChangeMessage msg, FriendlyByteBuf buf) {
        if (msg.getSelected() != null) {
            buf.writeBoolean(true);
            buf.writeResourceLocation(msg.getSelected());
        } else {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(msg.isOffhand());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ItemStack stack;
        ItemStack itemStack = stack = this.isOffhand() ? player.getOffhandItem() : player.getMainHandItem();
        if (stack.getItem() instanceof ItemManaweaverWand) {
            ItemManaweaverWand.setStoredPattern(stack, this.getSelected());
        }
    }
}

