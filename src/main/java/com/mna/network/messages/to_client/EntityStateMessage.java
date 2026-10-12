 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.entities.IAnimPacketSync;
import com.mna.network.messages.BaseClientMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityStateMessage
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EntityStateMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("entity_state_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, EntityStateMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> EntityStateMessage.encode(v, b), EntityStateMessage::decode);

    @Override
    public CustomPacketPayload.Type<EntityStateMessage> type() { return TYPE; }

    private CompoundTag data;
    private int entityID;

    private EntityStateMessage(int entityID, CompoundTag nbt) {
        this.data = nbt;
        this.entityID = entityID;
        this.messageIsValid = true;
    }

    private EntityStateMessage() {
        this.messageIsValid = false;
    }

    public CompoundTag getData() {
        return this.data;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public static EntityStateMessage decode(FriendlyByteBuf buf) {
        EntityStateMessage msg = new EntityStateMessage();
        try {
            msg.entityID = buf.readInt();
            msg.data = buf.readNbt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading EntityStateMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(EntityStateMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
        buf.writeNbt(msg.getData());
    }

    public static EntityStateMessage fromEntity(IAnimPacketSync<? extends Entity> entity) {
        EntityStateMessage msg = new EntityStateMessage(((Entity)entity).getId(), entity.getPacketData());
        return msg;
    }

    @Override
    public void Handle(Level world, Player player) {
        Entity entity = world.getEntity(this.getEntityID());
        if (entity != null && entity instanceof IAnimPacketSync) {
            ((IAnimPacketSync)entity).handlePacketData(this.getData());
        }
    }
}

