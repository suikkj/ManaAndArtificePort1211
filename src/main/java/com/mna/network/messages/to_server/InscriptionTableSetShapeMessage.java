 
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

public class InscriptionTableSetShapeMessage
extends TileEntityServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InscriptionTableSetShapeMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("inscription_table_set_shape_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, InscriptionTableSetShapeMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> InscriptionTableSetShapeMessage.encode(v, b), InscriptionTableSetShapeMessage::decode);

    @Override
    public CustomPacketPayload.Type<InscriptionTableSetShapeMessage> type() { return TYPE; }

    private ResourceLocation shape;

    public InscriptionTableSetShapeMessage() {
        super(null);
        this.messageIsValid = false;
    }

    public InscriptionTableSetShapeMessage(BlockPos position, ResourceLocation shape) {
        super(position);
        this.shape = shape;
        this.messageIsValid = true;
    }

    public final ResourceLocation getShape() {
        return this.shape;
    }

    public static final InscriptionTableSetShapeMessage decode(FriendlyByteBuf buf) {
        InscriptionTableSetShapeMessage msg = new InscriptionTableSetShapeMessage();
        try {
            msg.shape = buf.readResourceLocation();
            msg.pos = buf.readBlockPos();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(InscriptionTableSetShapeMessage msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.shape);
        buf.writeBlockPos(msg.pos);
    }

    public static final InscriptionTableSetShapeMessage fromInscriptionTable(InscriptionTableTile te) {
        ResourceLocation rLoc = ResourceLocation.parse("");
        if (te.getCurrentShape() != null) {
            rLoc = te.getCurrentShape().getPart().getRegistryName();
        }
        return new InscriptionTableSetShapeMessage(te.getBlockPos(), rLoc);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        InscriptionTableTile.handleShapeSet(player, this);
    }
}

