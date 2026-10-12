 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.gui.containers.entity.ContainerWanderingWizard;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class TradeSelectedMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TradeSelectedMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("trade_selected_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, TradeSelectedMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> TradeSelectedMessage.encode(v, b), TradeSelectedMessage::decode);

    @Override
    public CustomPacketPayload.Type<TradeSelectedMessage> type() { return TYPE; }

    private int index;

    public TradeSelectedMessage() {
        this.messageIsValid = false;
    }

    public TradeSelectedMessage(int index) {
        this.index = index;
        this.messageIsValid = true;
    }

    public static final TradeSelectedMessage decode(FriendlyByteBuf buf) {
        TradeSelectedMessage msg = new TradeSelectedMessage();
        try {
            msg.index = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading TradeSelectedMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(TradeSelectedMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getIndex());
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        int i = this.getIndex();
        AbstractContainerMenu container = player.containerMenu;
        if (container instanceof ContainerWanderingWizard) {
            ContainerWanderingWizard merchantcontainer = (ContainerWanderingWizard)container;
            merchantcontainer.setCurrentRecipeIndex(i);
            merchantcontainer.setCurrentTradeRecipeItems(i);
        }
    }
}

