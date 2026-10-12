 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.resource.SyncStatus;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.network.messages.BaseClientMessage;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class MagicSyncMessageToClient
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MagicSyncMessageToClient> TYPE = new CustomPacketPayload.Type<>(RLoc.create("magic_sync_message_to_client"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, MagicSyncMessageToClient> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> MagicSyncMessageToClient.encode(v, b), MagicSyncMessageToClient::decode);

    @Override
    public CustomPacketPayload.Type<MagicSyncMessageToClient> type() { return TYPE; }

    private int _level;
    private int _xp;
    private float[] _affinities;
    private NonNullList<ItemStack> _grimoireInventory;
    private NonNullList<ItemStack> _roteInventory;
    private CompoundTag _cantripData;
    private CompoundTag castingResourceData;
    private ResourceLocation castingResourceType;
    private boolean _syncGrimoire;
    private boolean _syncRote;
    private boolean _syncCastingResource;
    private boolean _syncCantrips;
    private boolean _teleporting;
    private int _teleport_ticks_elapsed;
    private int _teleport_ticks_total;

    private MagicSyncMessageToClient() {
        this.messageIsValid = false;
        this._grimoireInventory = NonNullList.create();
        this._roteInventory = NonNullList.create();
    }

    public MagicSyncMessageToClient(int level, int xp, float[] affinities) {
        this();
        this._level = level;
        this._xp = xp;
        this._affinities = affinities;
        this.messageIsValid = true;
    }

    @Nullable
    public CompoundTag getCastingResourceData() {
        return this.castingResourceData;
    }

    public int getLevel() {
        return this._level;
    }

    public int getXP() {
        return this._xp;
    }

    public boolean getIsTeleporting() {
        return this._teleporting;
    }

    public int getTeleportElapsed() {
        return this._teleport_ticks_elapsed;
    }

    public int getTeleportTotal() {
        return this._teleport_ticks_total;
    }

    public float[] getAffinities() {
        return this._affinities;
    }

    public boolean syncGrimoire() {
        return this._syncGrimoire;
    }

    public boolean syncRote() {
        return this._syncRote;
    }

    public boolean syncCastingResource() {
        return this._syncCastingResource;
    }

    public boolean syncCantrips() {
        return this._syncCantrips;
    }

    @Nullable
    public NonNullList<ItemStack> getGrimoireInventory() {
        return this._grimoireInventory;
    }

    @Nullable
    public NonNullList<ItemStack> getRoteInventory() {
        return this._roteInventory;
    }

    @Nullable
    public CompoundTag getCantripData() {
        return this._cantripData;
    }

    public static MagicSyncMessageToClient decode(FriendlyByteBuf buf) {
        MagicSyncMessageToClient msg = new MagicSyncMessageToClient();
        try {
            int i;
            int count;
            msg._level = buf.readInt();
            msg._xp = buf.readInt();
            int affC = buf.readInt();
            msg._affinities = new float[affC];
            for (int i2 = 0; i2 < affC; ++i2) {
                msg._affinities[i2] = buf.readFloat();
            }
            msg._syncCastingResource = buf.readBoolean();
            if (msg._syncCastingResource) {
                msg.castingResourceType = buf.readResourceLocation();
                msg.castingResourceData = buf.readNbt();
            }
            msg._syncGrimoire = buf.readBoolean();
            if (msg._syncGrimoire) {
                count = buf.readInt();
                for (i = 0; i < count; ++i) {
                    msg._grimoireInventory.add(buf.readItem());
                }
            }
            msg._syncRote = buf.readBoolean();
            if (msg._syncRote) {
                count = buf.readInt();
                for (i = 0; i < count; ++i) {
                    msg._roteInventory.add(buf.readItem());
                }
            }
            msg._syncCantrips = buf.readBoolean();
            if (msg._syncCantrips) {
                msg._cantripData = buf.readNbt();
            }
            msg._teleporting = buf.readBoolean();
            msg._teleport_ticks_elapsed = buf.readInt();
            msg._teleport_ticks_total = buf.readInt();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(MagicSyncMessageToClient msg, FriendlyByteBuf buf) {
        int i;
        buf.writeInt(msg._level);
        buf.writeInt(msg._xp);
        buf.writeInt(msg._affinities.length);
        for (i = 0; i < msg._affinities.length; ++i) {
            buf.writeFloat(msg._affinities[i]);
        }
        buf.writeBoolean(msg.syncCastingResource());
        if (msg.syncCastingResource()) {
            buf.writeResourceLocation(msg.getCastingResourceType());
            buf.writeNbt(msg.castingResourceData);
        }
        buf.writeBoolean(msg.syncGrimoire());
        if (msg.syncGrimoire()) {
            buf.writeInt(msg._grimoireInventory.size());
            for (i = 0; i < msg._grimoireInventory.size(); ++i) {
                buf.writeItem((ItemStack)msg._grimoireInventory.get(i));
            }
        }
        buf.writeBoolean(msg.syncRote());
        if (msg.syncRote()) {
            buf.writeInt(msg._roteInventory.size());
            for (i = 0; i < msg._roteInventory.size(); ++i) {
                buf.writeItem((ItemStack)msg._roteInventory.get(i));
            }
        }
        buf.writeBoolean(msg.syncCantrips());
        if (msg.syncCantrips()) {
            buf.writeNbt(msg._cantripData);
        }
        buf.writeBoolean(msg._teleporting);
        buf.writeInt(msg._teleport_ticks_elapsed);
        buf.writeInt(msg._teleport_ticks_total);
    }

    public static MagicSyncMessageToClient fromCapability(IPlayerMagic capability) {
        int i;
        float[] f = new float[Affinity.values().length];
        if (capability == null) {
            return new MagicSyncMessageToClient(0, 0, f);
        }
        int count = 0;
        for (Affinity aff : Affinity.values()) {
            f[count++] = capability.getAffinityDepth(aff);
        }
        MagicSyncMessageToClient msg = new MagicSyncMessageToClient(capability.getMagicLevel(), capability.getMagicXP(), f);
        if (capability.getCastingResource().getSyncStatus() != SyncStatus.NOT_NEEDED) {
            msg._syncCastingResource = true;
            msg.castingResourceType = capability.getCastingResource().getRegistryName();
            msg.castingResourceData = new CompoundTag();
            capability.getCastingResource().writeNBT(msg.castingResourceData);
        }
        if (capability.shouldSyncGrimoire()) {
            msg._syncGrimoire = true;
            for (i = 0; i < capability.getGrimoireInventory().getContainerSize(); ++i) {
                msg._grimoireInventory.add(capability.getGrimoireInventory().getItem(i));
            }
        }
        if (capability.shouldSyncRote()) {
            msg._syncRote = true;
            for (i = 0; i < capability.getRoteInventory().getContainerSize(); ++i) {
                msg._roteInventory.add(capability.getRoteInventory().getItem(i));
            }
        }
        if (capability.getCantripData().needsSync()) {
            msg._syncCantrips = true;
            msg._cantripData = capability.getCantripData().writeToNBT(false);
        }
        msg._teleporting = capability.getIsTeleporting();
        msg._teleport_ticks_elapsed = capability.getTeleportElapsedTicks();
        msg._teleport_ticks_total = capability.getTeleportTotalTicks();
        return msg;
    }

    public ResourceLocation getCastingResourceType() {
        return this.castingResourceType;
    }

    @Override
    public void Handle(Level level, Player player) {
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            int i;
            m.setMagicLevel(player, this.getLevel());
            m.setMagicXP(this.getXP());
            if (this.syncCastingResource()) {
                m.setCastingResourceType(this.getCastingResourceType());
                m.getCastingResource().readNBT(this.getCastingResourceData());
            }
            for (i = 0; i < Math.min(this.getAffinities().length, Affinity.values().length); ++i) {
                m.setAffinityDepth(Affinity.values()[i], this.getAffinities()[i]);
            }
            if (this.syncGrimoire()) {
                m.getGrimoireInventory().clearContent();
                for (i = 0; i < this.getGrimoireInventory().size(); ++i) {
                    m.getGrimoireInventory().setItem(i, (ItemStack)this.getGrimoireInventory().get(i));
                }
            }
            if (this.syncRote()) {
                m.getRoteInventory().clearContent();
                for (i = 0; i < this.getRoteInventory().size(); ++i) {
                    m.getRoteInventory().setItem(i, (ItemStack)this.getRoteInventory().get(i));
                }
            }
            if (this.syncCantrips()) {
                m.getCantripData().readFromNBT(this.getCantripData());
            }
            m.updateClientsideTeleportData(this.getIsTeleporting(), this.getTeleportElapsed(), this.getTeleportTotal());
        });
    }
}

