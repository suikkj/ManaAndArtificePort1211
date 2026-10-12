 
package com.mna.network.messages.to_client;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.blocks.tileentities.ParticleEmitterTile;
import com.mna.capabilities.particles.ParticleAura;
import com.mna.capabilities.particles.ParticleAuraProvider;
import com.mna.network.messages.BaseClientMessage;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mna.api.tools.MNACap;

public class AuraSyncMessageToClient
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AuraSyncMessageToClient> TYPE = new CustomPacketPayload.Type<>(RLoc.create("aura_sync_message_to_client"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, AuraSyncMessageToClient> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> AuraSyncMessageToClient.encode(v, b), AuraSyncMessageToClient::decode);

    @Override
    public CustomPacketPayload.Type<AuraSyncMessageToClient> type() { return TYPE; }

    private int entityID;
    private CompoundTag tag;
    private BlockPos pos;

    private AuraSyncMessageToClient() {
        this.messageIsValid = false;
    }

    private AuraSyncMessageToClient(int entityID, CompoundTag tag) {
        this();
        this.entityID = entityID;
        this.tag = tag;
        this.messageIsValid = true;
    }

    public AuraSyncMessageToClient(BlockPos pos, CompoundTag tag) {
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

    public static AuraSyncMessageToClient decode(FriendlyByteBuf buf) {
        AuraSyncMessageToClient msg = new AuraSyncMessageToClient();
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

    public static void encode(AuraSyncMessageToClient msg, FriendlyByteBuf buf) {
        if (msg.pos == null) {
            buf.writeBoolean(true);
            buf.writeInt(msg.getEntityID());
        } else {
            buf.writeBoolean(false);
            buf.writeLong(msg.pos.asLong());
        }
        buf.writeNbt(msg.tag);
    }

    public static AuraSyncMessageToClient fromPlayer(Player player) {
        java.util.Optional<ParticleAuraProvider> aura = MNACap.get(player, ParticleAuraProvider.AURA);
        CompoundTag tag = aura.isPresent() ? ((ParticleAura)aura.get()).save() : new CompoundTag();
        AuraSyncMessageToClient msg = new AuraSyncMessageToClient(player.getId(), tag);
        return msg;
    }

    public static AuraSyncMessageToClient fromTile(ParticleEmitterTile tile) {
        AuraSyncMessageToClient msg = new AuraSyncMessageToClient(tile.getBlockPos(), tile.getData().getTag());
        return msg;
    }

    @Override
    public void Handle(Level world, Player player) {
        if (this.isBlock() && world.isLoaded(this.getBlockPos())) {
            BlockEntity be = world.getBlockEntity(this.getBlockPos());
            if (be != null && be instanceof ParticleEmitterTile) {
                ParticleEmitterTile peTile = (ParticleEmitterTile)be;
                peTile.setData(this.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            }
        } else {
            Entity entity = world.getEntity(this.getEntityID());
            if (entity != null && entity instanceof Player) {
                Player resolvedPlayer = (Player)entity;
                MNACap.get(resolvedPlayer, ParticleAuraProvider.AURA).ifPresent(a -> a.load(this.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()));
            }
        }
    }
}


