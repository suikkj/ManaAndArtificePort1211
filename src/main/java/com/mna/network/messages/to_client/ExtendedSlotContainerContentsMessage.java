 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.ExtendedItemStackPacketBuffer;
import com.mna.network.messages.BaseClientMessage;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ExtendedSlotContainerContentsMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ExtendedSlotContainerContentsMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("extended_slot_container_contents_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ExtendedSlotContainerContentsMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> ExtendedSlotContainerContentsMessage.encode(v, b), ExtendedSlotContainerContentsMessage::decode);

    @Override
    public CustomPacketPayload.Type<ExtendedSlotContainerContentsMessage> type() { return TYPE; }

    private int containerId;
    private int stateId;
    private List<ItemStack> items;
    private ItemStack carriedItem;

    public ExtendedSlotContainerContentsMessage(int containerId, int stateId, NonNullList<ItemStack> items, ItemStack carriedItem) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.carriedItem = carriedItem.copy();
        this.items = NonNullList.withSize((int)items.size(), ItemStack.EMPTY);
        for (int i = 0; i < items.size(); ++i) {
            this.items.set(i, ((ItemStack)items.get(i)).copy());
        }
        this.messageIsValid = true;
    }

    public ExtendedSlotContainerContentsMessage() {
        this.messageIsValid = false;
    }

    public int getContainerID() {
        return this.containerId;
    }

    public int getStateID() {
        return this.stateId;
    }

    public ItemStack getCarried() {
        return this.carriedItem;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public static ExtendedSlotContainerContentsMessage decode(FriendlyByteBuf buf) {
        ExtendedSlotContainerContentsMessage msg = new ExtendedSlotContainerContentsMessage();
        ExtendedItemStackPacketBuffer bufferWrapper = new ExtendedItemStackPacketBuffer((ByteBuf)buf);
        try {
            msg.containerId = bufferWrapper.readUnsignedByte();
            msg.stateId = bufferWrapper.readVarInt();
            msg.items = (List)bufferWrapper.readExtendedCollection(NonNullList::createWithCapacity, ExtendedItemStackPacketBuffer::readExtendedItemStack);
            msg.carriedItem = bufferWrapper.readExtendedItemStack();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ExtendedSlotContentsMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(ExtendedSlotContainerContentsMessage msg, FriendlyByteBuf buf) {
        ExtendedItemStackPacketBuffer bufferWrapper = new ExtendedItemStackPacketBuffer((ByteBuf)buf);
        bufferWrapper.writeByte(msg.containerId);
        bufferWrapper.writeVarInt(msg.stateId);
        bufferWrapper.writeExtendedCollection(msg.items, ExtendedItemStackPacketBuffer::writeExtendedItemStack);
        bufferWrapper.writeExtendedItemStack(msg.carriedItem);
    }

    @Override
    public void Handle(Level world, Player player) {
        if (this.getContainerID() == 0) {
            player.inventoryMenu.initializeContents(this.getStateID(), this.getItems(), this.getCarried());
        } else if (this.getContainerID() == player.containerMenu.containerId) {
            player.containerMenu.initializeContents(this.getStateID(), this.getItems(), this.getCarried());
        }
    }
}

