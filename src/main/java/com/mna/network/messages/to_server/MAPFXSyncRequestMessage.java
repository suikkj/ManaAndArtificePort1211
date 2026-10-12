 
package com.mna.network.messages.to_server;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.BaseServerMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MAPFXSyncRequestMessage
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MAPFXSyncRequestMessage> TYPE = new CustomPacketPayload.Type<>(RLoc.create("m_a_p_f_x_sync_request_message"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, MAPFXSyncRequestMessage> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> MAPFXSyncRequestMessage.encode(v, b), MAPFXSyncRequestMessage::decode);

    @Override
    public CustomPacketPayload.Type<MAPFXSyncRequestMessage> type() { return TYPE; }

    private int entityID;
    private boolean aura;

    private MAPFXSyncRequestMessage() {
        this.messageIsValid = false;
    }

    public MAPFXSyncRequestMessage(int entityID, boolean aura) {
        this();
        this.entityID = entityID;
        this.aura = aura;
        this.messageIsValid = true;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public boolean isForAura() {
        return this.aura;
    }

    public static MAPFXSyncRequestMessage decode(FriendlyByteBuf buf) {
        MAPFXSyncRequestMessage msg = new MAPFXSyncRequestMessage();
        try {
            msg.entityID = buf.readInt();
            msg.aura = buf.readBoolean();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MAPFXSyncRequestMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(MAPFXSyncRequestMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getEntityID());
        buf.writeBoolean(msg.isForAura());
    }

    public static MAPFXSyncRequestMessage fromConstruct(LivingEntity entity, boolean diagnosticsOnly) {
        MAPFXSyncRequestMessage msg = new MAPFXSyncRequestMessage(entity.getId(), false);
        return msg;
    }

    public static MAPFXSyncRequestMessage forAura(Player player) {
        MAPFXSyncRequestMessage msg = new MAPFXSyncRequestMessage(player.getId(), true);
        return msg;
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        Entity entity = player.level().getEntity(this.getEntityID());
        if (this.isForAura()) {
            if (entity instanceof ServerPlayer && (ManaAndArtifice.instance.isDebug || ManaAndArtifice.instance.enabled_auras.contains(((ServerPlayer)entity).getGameProfile().getId()))) {
                ServerMessageDispatcher.sendAuraSyncMessage(player, (ServerPlayer)entity);
            }
        } else if (entity != null && entity instanceof LivingEntity) {
            ServerMessageDispatcher.sendMAPFXMessage((LivingEntity)entity, player);
        }
    }
}

