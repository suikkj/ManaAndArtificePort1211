 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.ItemModifierBook;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SelectedModifierMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectedModifierMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("selected_modifier_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SelectedModifierMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SelectedModifierMessage.encode(v, b), SelectedModifierMessage::decode);

    @Override
    public CustomPacketPayload.Type<SelectedModifierMessage> type() { return TYPE; }

    private ResourceLocation modifierRLoc;
    private boolean offhand;

    public SelectedModifierMessage(ResourceLocation modifierRLoc, boolean offhand) {
        this.modifierRLoc = modifierRLoc;
        this.offhand = offhand;
        this.messageIsValid = true;
    }

    public SelectedModifierMessage() {
        this.messageIsValid = false;
    }

    public ResourceLocation getModifierRLoc() {
        return this.modifierRLoc;
    }

    public boolean isOffhand() {
        return this.offhand;
    }

    public static SelectedModifierMessage decode(FriendlyByteBuf buf) {
        SelectedModifierMessage msg = new SelectedModifierMessage();
        try {
            msg.modifierRLoc = buf.readResourceLocation();
            msg.offhand = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading SelectedModifierMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SelectedModifierMessage msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.modifierRLoc);
        buf.writeBoolean(msg.isOffhand());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ItemStack stack;
        ItemStack itemStack = stack = this.isOffhand() ? player.getOffhandItem() : player.getMainHandItem();
        if (stack.getItem() == ItemInit.MODIFIER_BOOK.get()) {
            ItemModifierBook.setModifier(stack, this.getModifierRLoc());
        } else {
            player.sendSystemMessage((Component)Component.literal((String)"Error setting selected modifier, couldn't verify held item"));
        }
    }
}

