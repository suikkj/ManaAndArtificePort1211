 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.items.artifice.PatterningPrism;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PatterningPrismPasteMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PatterningPrismPasteMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("patterning_prism_paste_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, PatterningPrismPasteMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> PatterningPrismPasteMessage.encode(v, b), PatterningPrismPasteMessage::decode);

    @Override
    public CustomPacketPayload.Type<PatterningPrismPasteMessage> type() { return TYPE; }

    private CompoundTag logic;
    private InteractionHand hand;
    private boolean isParticleEmitter;

    public PatterningPrismPasteMessage(CompoundTag logic, InteractionHand hand, boolean isParticleEmitter) {
        this.logic = logic;
        this.hand = hand;
        this.isParticleEmitter = isParticleEmitter;
        this.messageIsValid = true;
    }

    public PatterningPrismPasteMessage() {
        this.messageIsValid = false;
    }

    public CompoundTag getLogic() {
        return this.logic;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public boolean isParticleEmitter() {
        return this.isParticleEmitter;
    }

    public static PatterningPrismPasteMessage decode(FriendlyByteBuf buf) {
        PatterningPrismPasteMessage msg = new PatterningPrismPasteMessage();
        try {
            msg.logic = buf.readAnySizeNbt();
            msg.hand = InteractionHand.values()[buf.readInt()];
            msg.isParticleEmitter = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading PatterningPrismPasteMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(PatterningPrismPasteMessage msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.getLogic());
        buf.writeInt(msg.hand.ordinal());
        buf.writeBoolean(msg.isParticleEmitter());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        ItemStack stack = player.getItemInHand(this.getHand());
        Item item = stack.getItem();
        if (item instanceof PatterningPrism) {
            PatterningPrism copier = (PatterningPrism)item;
            copier.setCopiedLogic(this.getLogic(), stack, this.isParticleEmitter());
        }
    }
}

