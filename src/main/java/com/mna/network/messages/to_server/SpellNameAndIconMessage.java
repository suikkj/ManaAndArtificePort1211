 
package com.mna.network.messages.to_server;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.BookOfRote;
import com.mna.items.sorcery.ItemSpell;
import com.mna.items.sorcery.MagicStaff;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.mna.api.tools.MNACap;

public class SpellNameAndIconMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpellNameAndIconMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("spell_name_and_icon_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, SpellNameAndIconMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> SpellNameAndIconMessage.encode(v, b), SpellNameAndIconMessage::decode);

    @Override
    public CustomPacketPayload.Type<SpellNameAndIconMessage> type() { return TYPE; }

    private int iconIndex;
    private String name;
    private int itemIndex = -1;
    private InteractionHand hand;

    public SpellNameAndIconMessage(String name, int iconIndex, InteractionHand hand) {
        this.name = name;
        this.iconIndex = iconIndex;
        this.hand = hand;
        this.messageIsValid = true;
    }

    public SpellNameAndIconMessage() {
        this.messageIsValid = false;
    }

    public void setItemIndex(int index) {
        this.itemIndex = index;
    }

    public int getItemIndex() {
        return this.itemIndex;
    }

    public int getIconIndex() {
        return this.iconIndex;
    }

    public String getName() {
        return this.name;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public static SpellNameAndIconMessage decode(FriendlyByteBuf buf) {
        SpellNameAndIconMessage msg = new SpellNameAndIconMessage();
        try {
            msg.iconIndex = buf.readInt();
            msg.name = buf.readUtf(Short.MAX_VALUE);
            msg.hand = InteractionHand.values()[buf.readByte()];
            msg.itemIndex = buf.readBoolean() ? buf.readInt() : -1;
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading ManaweavePatternDrawnMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(SpellNameAndIconMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.iconIndex);
        buf.writeUtf(msg.name);
        buf.writeByte(msg.hand.ordinal());
        if (msg.itemIndex > -1) {
            buf.writeBoolean(true);
            buf.writeInt(msg.itemIndex);
        } else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ItemStack stack = player.getItemInHand(this.getHand());
        if (stack.getItem() == ItemInit.SPELL.get() || stack.getItem() instanceof MagicStaff) {
            stack.set(DataComponents.ITEM_NAME, (Component)Component.literal((String)this.getName()));
            ItemSpell.setCustomIcon(stack, this.getIconIndex());
        } else if (stack.getItem() instanceof BookOfRote) {
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                ItemStack roteStack = m.getRoteInventory().getItem(this.getItemIndex());
                roteStack.set(DataComponents.ITEM_NAME, (Component)Component.literal((String)this.getName()));
                ItemSpell.setCustomIcon(roteStack, this.getIconIndex());
            });
        }
    }
}

