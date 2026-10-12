 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.sorcery.SpellBook;
import com.mna.network.messages.BaseServerMessage;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import top.theillusivec4.curios.api.SlotContext;

public class SpellBookSlotChangeMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpellBookSlotChangeMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("spell_book_slot_change_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SpellBookSlotChangeMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SpellBookSlotChangeMessage.encode(v, b), SpellBookSlotChangeMessage::decode);

    @Override
    public CustomPacketPayload.Type<SpellBookSlotChangeMessage> type() { return TYPE; }

    private int slot;
    private InteractionHand hand;
    private SlotContext context;

    public SpellBookSlotChangeMessage(int slot, InteractionHand hand, @Nullable SlotContext context) {
        this.slot = slot;
        this.hand = hand;
        this.context = context;
        this.messageIsValid = true;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    @Nullable
    public SlotContext getContext() {
        return this.context;
    }

    public int getSlot() {
        return this.slot;
    }

    public SpellBookSlotChangeMessage() {
        this.messageIsValid = false;
    }

    public static SpellBookSlotChangeMessage decode(FriendlyByteBuf buf) {
        SpellBookSlotChangeMessage msg = new SpellBookSlotChangeMessage();
        try {
            msg.slot = buf.readInt();
            msg.hand = InteractionHand.values()[buf.readInt() % InteractionHand.values().length];
            if (buf.readBoolean()) {
                msg.context = new SlotContext(buf.readUtf(), null, buf.readInt(), false, false);
            }
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ManaweavePatternDrawnMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SpellBookSlotChangeMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.slot);
        buf.writeInt(msg.hand.ordinal());
        if (msg.context != null) {
            buf.writeBoolean(true);
            buf.writeUtf(msg.context.getIdentifier());
            buf.writeInt(msg.context.getIndex());
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        SpellBook.handleSlotChangeMessage(this, player);
    }
}

