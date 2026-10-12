 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.ExtendedItemStackPacketBuffer;
import com.mna.network.messages.BaseClientMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExtendedSlotContentsMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ExtendedSlotContentsMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("extended_slot_contents_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ExtendedSlotContentsMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> ExtendedSlotContentsMessage.encode(v, b), ExtendedSlotContentsMessage::decode);

    @Override
    public CustomPacketPayload.Type<ExtendedSlotContentsMessage> type() { return TYPE; }

    private int screenId = 0;
    private int slot = 0;
    private int stateID = 0;
    private ItemStack stack = ItemStack.EMPTY;

    public ExtendedSlotContentsMessage(int screenId, int stateId, int slot, ItemStack stack) {
        this.screenId = screenId;
        this.slot = slot;
        this.stack = stack.copy();
        this.stateID = stateId;
        this.messageIsValid = true;
    }

    public ExtendedSlotContentsMessage() {
        this.messageIsValid = false;
    }

    public int getScreenID() {
        return this.screenId;
    }

    public int getSlotIndex() {
        return this.slot;
    }

    public int getStateID() {
        return this.stateID;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public static ExtendedSlotContentsMessage decode(FriendlyByteBuf buf) {
        ExtendedSlotContentsMessage msg = new ExtendedSlotContentsMessage();
        ExtendedItemStackPacketBuffer bufferWrapper = new ExtendedItemStackPacketBuffer((ByteBuf)buf);
        try {
            msg.screenId = bufferWrapper.readInt();
            msg.slot = bufferWrapper.readInt();
            msg.stateID = bufferWrapper.readInt();
            msg.stack = bufferWrapper.readExtendedItemStack();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ExtendedSlotContentsMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(ExtendedSlotContentsMessage msg, FriendlyByteBuf buf) {
        ExtendedItemStackPacketBuffer bufferWrapper = new ExtendedItemStackPacketBuffer((ByteBuf)buf);
        bufferWrapper.writeInt(msg.getScreenID());
        bufferWrapper.writeInt(msg.getSlotIndex());
        bufferWrapper.writeInt(msg.getStateID());
        bufferWrapper.writeExtendedItemStack(msg.getStack());
    }

    @Override
    public void Handle(Level world, Player player) {
        if (player.containerMenu.containerId == this.getScreenID()) {
            player.containerMenu.setItem(this.getSlotIndex(), this.getStateID(), this.getStack());
        }
    }
}

