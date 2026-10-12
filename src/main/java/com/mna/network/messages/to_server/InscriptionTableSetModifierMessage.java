 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import com.mna.network.messages.TileEntityServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class InscriptionTableSetModifierMessage
extends TileEntityServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InscriptionTableSetModifierMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("inscription_table_set_modifier_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, InscriptionTableSetModifierMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> InscriptionTableSetModifierMessage.encode(v, b), InscriptionTableSetModifierMessage::decode);

    @Override
    public CustomPacketPayload.Type<InscriptionTableSetModifierMessage> type() { return TYPE; }

    private ResourceLocation modifier;
    private int index;

    public InscriptionTableSetModifierMessage() {
        super(null);
        this.messageIsValid = false;
    }

    public InscriptionTableSetModifierMessage(BlockPos position, ResourceLocation modifier, int index) {
        super(position);
        this.modifier = modifier;
        this.index = index;
        this.messageIsValid = true;
    }

    public final ResourceLocation getModifier() {
        return this.modifier;
    }

    public final int getIndex() {
        return this.index;
    }

    public static final InscriptionTableSetModifierMessage decode(FriendlyByteBuf buf) {
        InscriptionTableSetModifierMessage msg = new InscriptionTableSetModifierMessage();
        try {
            msg.modifier = buf.readResourceLocation();
            msg.index = buf.readInt();
            msg.pos = buf.readBlockPos();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(InscriptionTableSetModifierMessage msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.modifier);
        buf.writeInt(msg.index);
        buf.writeBlockPos(msg.pos);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        InscriptionTableTile.handleModifierSet(player, this);
    }
}

