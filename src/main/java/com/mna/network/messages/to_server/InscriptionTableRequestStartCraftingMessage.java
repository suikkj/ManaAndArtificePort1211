 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import com.mna.network.messages.TileEntityServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class InscriptionTableRequestStartCraftingMessage
extends TileEntityServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InscriptionTableRequestStartCraftingMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("inscription_table_request_start_crafting_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, InscriptionTableRequestStartCraftingMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> InscriptionTableRequestStartCraftingMessage.encode(v, b), InscriptionTableRequestStartCraftingMessage::decode);

    @Override
    public CustomPacketPayload.Type<InscriptionTableRequestStartCraftingMessage> type() { return TYPE; }

    public InscriptionTableRequestStartCraftingMessage() {
        super(null);
        this.messageIsValid = false;
    }

    public InscriptionTableRequestStartCraftingMessage(BlockPos position) {
        super(position);
        this.messageIsValid = true;
    }

    public static final InscriptionTableRequestStartCraftingMessage decode(FriendlyByteBuf buf) {
        InscriptionTableRequestStartCraftingMessage msg = new InscriptionTableRequestStartCraftingMessage();
        try {
            msg.pos = buf.readBlockPos();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(InscriptionTableRequestStartCraftingMessage msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static final InscriptionTableRequestStartCraftingMessage fromInscriptionTable(InscriptionTableTile te) {
        return new InscriptionTableRequestStartCraftingMessage(te.getBlockPos());
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        InscriptionTableTile.handleRequestStartCrafting(player, this);
    }
}

