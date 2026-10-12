 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.spells.attributes.Attribute;
import com.mna.blocks.tileentities.wizard_lab.InscriptionTableTile;
import com.mna.network.messages.TileEntityServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class InscriptionTableAttributeChangeMessage
extends TileEntityServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InscriptionTableAttributeChangeMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("inscription_table_attribute_change_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, InscriptionTableAttributeChangeMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> InscriptionTableAttributeChangeMessage.encode(v, b), InscriptionTableAttributeChangeMessage::decode);

    @Override
    public CustomPacketPayload.Type<InscriptionTableAttributeChangeMessage> type() { return TYPE; }

    private Attribute attr;
    private float value;
    private ChangeType type;

    public InscriptionTableAttributeChangeMessage() {
        super(null);
        this.messageIsValid = false;
    }

    public InscriptionTableAttributeChangeMessage(BlockPos position, Attribute attr, float value, ChangeType type) {
        super(position);
        this.attr = attr;
        this.value = value;
        this.type = type;
        this.messageIsValid = true;
    }

    public final Attribute getAttribute() {
        return this.attr;
    }

    public final float getValue() {
        return this.value;
    }

    public final ChangeType getChangeType() {
        return this.type;
    }

    public static final InscriptionTableAttributeChangeMessage decode(FriendlyByteBuf buf) {
        InscriptionTableAttributeChangeMessage msg = new InscriptionTableAttributeChangeMessage();
        try {
            msg.attr = Attribute.valueOf(buf.readUtf(Short.MAX_VALUE));
            msg.value = buf.readFloat();
            msg.type = ChangeType.valueOf(buf.readUtf(512));
            msg.pos = buf.readBlockPos();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static final void encode(InscriptionTableAttributeChangeMessage msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.attr.name());
        buf.writeFloat(msg.value);
        buf.writeUtf(msg.type.name());
        buf.writeBlockPos(msg.pos);
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        InscriptionTableTile.handleAttributeValueChange(player, this);
    }

    public static enum ChangeType {
        SHAPE,
        COMPONENT;

    }
}

