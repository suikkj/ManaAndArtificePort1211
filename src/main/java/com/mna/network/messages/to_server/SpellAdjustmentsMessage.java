 
package com.mna.network.messages.to_server;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.sorcery.ItemSpell;
import com.mna.items.sorcery.SpellBook;
import com.mna.network.messages.BaseServerMessage;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SpellAdjustmentsMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpellAdjustmentsMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("spell_adjustments_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SpellAdjustmentsMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SpellAdjustmentsMessage.encode(v, b), SpellAdjustmentsMessage::decode);

    @Override
    public CustomPacketPayload.Type<SpellAdjustmentsMessage> type() { return TYPE; }

    private CompoundTag spellValues;
    private InteractionHand hand;

    public SpellAdjustmentsMessage(CompoundTag spellValues, InteractionHand hand) {
        this.spellValues = spellValues;
        this.hand = hand;
        this.messageIsValid = true;
    }

    public SpellAdjustmentsMessage() {
        this.messageIsValid = false;
    }

    public CompoundTag getData() {
        return this.spellValues;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public static SpellAdjustmentsMessage decode(FriendlyByteBuf buf) {
        SpellAdjustmentsMessage msg = new SpellAdjustmentsMessage();
        try {
            msg.spellValues = buf.readNbt();
            msg.hand = InteractionHand.values()[buf.readInt()];
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading SpellAdjustmentsMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SpellAdjustmentsMessage msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.getData());
        buf.writeInt(msg.hand.ordinal());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        SpellRecipe recipe = SpellRecipe.fromNBT(this.getData());
        ItemStack heldStack = player.getItemInHand(this.getHand());
        if (recipe.isValid() && heldStack.getItem() instanceof ItemSpell && !(heldStack.getItem() instanceof SpellBook)) {
            recipe.writeToNBT(heldStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        } else {
            player.sendSystemMessage((Component)Component.literal((String)"Error setting spell data values; serverside validation failed!").withStyle(ChatFormatting.RED));
        }
    }
}

