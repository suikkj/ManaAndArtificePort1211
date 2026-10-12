 
package com.mna.network.messages.to_server;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.ParticleEmitterTile;
import com.mna.capabilities.particles.ParticleAura;
import com.mna.capabilities.particles.ParticleAuraProvider;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.BaseServerMessage;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mna.api.tools.MNACap;

public class AuraSyncMessageToServer
extends BaseServerMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AuraSyncMessageToServer> TYPE = new CustomPacketPayload.Type<>(RLoc.create("aura_sync_message_to_server"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, AuraSyncMessageToServer> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> AuraSyncMessageToServer.encode(v, b), AuraSyncMessageToServer::decode);

    @Override
    public CustomPacketPayload.Type<AuraSyncMessageToServer> type() { return TYPE; }

    private int entityID;
    private CompoundTag tag;
    private BlockPos pos;

    private AuraSyncMessageToServer() {
        this.messageIsValid = false;
    }

    private AuraSyncMessageToServer(int entityID, CompoundTag tag) {
        this();
        this.entityID = entityID;
        this.tag = tag;
        this.messageIsValid = true;
    }

    public AuraSyncMessageToServer(BlockPos pos, CompoundTag tag) {
        this();
        this.tag = tag;
        this.pos = pos;
        this.messageIsValid = true;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public CompoundTag getTag() {
        return this.tag;
    }

    @Nullable
    public BlockPos getBlockPos() {
        return this.pos;
    }

    public boolean isBlock() {
        return this.pos != null;
    }

    public static AuraSyncMessageToServer decode(FriendlyByteBuf buf) {
        AuraSyncMessageToServer msg = new AuraSyncMessageToServer();
        try {
            if (buf.readBoolean()) {
                msg.entityID = buf.readInt();
            } else {
                msg.pos = BlockPos.of((long)buf.readLong());
            }
            msg.tag = buf.readNbt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MAPFXSyncRequestMessage: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(AuraSyncMessageToServer msg, FriendlyByteBuf buf) {
        if (msg.pos == null) {
            buf.writeBoolean(true);
            buf.writeInt(msg.getEntityID());
        } else {
            buf.writeBoolean(false);
            buf.writeLong(msg.pos.asLong());
        }
        buf.writeNbt(msg.tag);
    }

    public static AuraSyncMessageToServer fromPlayer(Player player) {
        java.util.Optional<com.mna.capabilities.particles.ParticleAura> aura = MNACap.get(player, ParticleAuraProvider.AURA);
        CompoundTag tag = aura.isPresent() ? aura.get().save() : new CompoundTag();
        AuraSyncMessageToServer msg = new AuraSyncMessageToServer(player.getId(), tag);
        return msg;
    }

    public static AuraSyncMessageToServer fromTile(ParticleEmitterTile tile) {
        AuraSyncMessageToServer msg = new AuraSyncMessageToServer(tile.getBlockPos(), tile.getData().getTag());
        return msg;
    }

    @Override
    public void Handle(ServerLevel world, ServerPlayer player) {
        if (this.isBlock()) {
            BlockEntity be = player.level().getBlockEntity(this.getBlockPos());
            if (be != null && be instanceof ParticleEmitterTile) {
                ParticleEmitterTile pEmitter = (ParticleEmitterTile)be;
                pEmitter.setData(this.tag.copy());
                ServerMessageDispatcher.sendAuraSyncMessage(pEmitter);
            }
        } else if (player.getGameProfile() != null && player.getGameProfile().getId() != null && (ManaAndArtifice.instance.isDebug || ManaAndArtifice.instance.enabled_auras.contains(player.getGameProfile().getId()))) {
            MNACap.get(player, ParticleAuraProvider.AURA).ifPresent(a -> {
                a.load(this.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                ServerMessageDispatcher.sendAuraSyncMessage(player);
            });
        }
    }
}


