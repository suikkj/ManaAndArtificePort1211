 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.base.IRadialInventorySelect;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RadialInventorySlotChangeMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RadialInventorySlotChangeMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("radial_inventory_slot_change_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RadialInventorySlotChangeMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RadialInventorySlotChangeMessage.encode(v, b), RadialInventorySlotChangeMessage::decode);

    @Override
    public CustomPacketPayload.Type<RadialInventorySlotChangeMessage> type() { return TYPE; }

    private int slot;
    private boolean offhand;

    public RadialInventorySlotChangeMessage(int slot, boolean offhand) {
        this.slot = slot;
        this.offhand = offhand;
        this.messageIsValid = true;
    }

    public RadialInventorySlotChangeMessage() {
        this.messageIsValid = false;
    }

    public int getSlot() {
        return this.slot;
    }

    public boolean isOffhand() {
        return this.offhand;
    }

    public static RadialInventorySlotChangeMessage decode(FriendlyByteBuf buf) {
        RadialInventorySlotChangeMessage msg = new RadialInventorySlotChangeMessage();
        try {
            msg.slot = buf.readInt();
            msg.offhand = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RadialInventorySlotChangeMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(RadialInventorySlotChangeMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getSlot());
        buf.writeBoolean(msg.isOffhand());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ItemStack stack = this.isOffhand() ? player.getOffhandItem() : player.getMainHandItem();
        Item item = stack.getItem();
        if (item instanceof IRadialInventorySelect) {
            IRadialInventorySelect radial = (IRadialInventorySelect)item;
            radial.setSlot((Player)player, this.isOffhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, this.getSlot(), this.isOffhand(), false);
        }
    }
}

