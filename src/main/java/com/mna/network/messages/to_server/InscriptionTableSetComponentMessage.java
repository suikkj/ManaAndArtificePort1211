 
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

public class InscriptionTableSetComponentMessage
extends TileEntityServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InscriptionTableSetComponentMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("inscription_table_set_component_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, InscriptionTableSetComponentMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> InscriptionTableSetComponentMessage.encode(v, b), InscriptionTableSetComponentMessage::decode);

    @Override
    public CustomPacketPayload.Type<InscriptionTableSetComponentMessage> type() { return TYPE; }

    private ResourceLocation component;

    public InscriptionTableSetComponentMessage() {
        super(null);
        this.messageIsValid = false;
    }

    public InscriptionTableSetComponentMessage(BlockPos position, ResourceLocation component) {
        super(position);
        this.component = component;
        this.messageIsValid = true;
    }

    public final ResourceLocation getComponent() {
        return this.component;
    }

    public static final InscriptionTableSetComponentMessage decode(FriendlyByteBuf buf) {
        InscriptionTableSetComponentMessage msg = new InscriptionTableSetComponentMessage();
        try {
            msg.component = buf.readResourceLocation();
            msg.pos = buf.readBlockPos();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(InscriptionTableSetComponentMessage msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.component);
        buf.writeBlockPos(msg.pos);
    }

    public static final InscriptionTableSetComponentMessage fromInscriptionTable(InscriptionTableTile te) {
        ResourceLocation rLoc = ResourceLocation.parse("");
        if (te.getCurrentComponent() != null) {
            rLoc = te.getCurrentComponent().getPart().getRegistryName();
        }
        return new InscriptionTableSetComponentMessage(te.getBlockPos(), rLoc);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        InscriptionTableTile.handleComponentSet(player, this);
    }
}

