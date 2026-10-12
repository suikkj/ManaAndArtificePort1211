 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.network.messages.BaseClientMessage;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class ProgressionSyncMessageToClient
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ProgressionSyncMessageToClient> TYPE = new CustomPacketPayload.Type<>(RLoc.create("progression_sync_message_to_client"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ProgressionSyncMessageToClient> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> ProgressionSyncMessageToClient.encode(v, b), ProgressionSyncMessageToClient::decode);

    @Override
    public CustomPacketPayload.Type<ProgressionSyncMessageToClient> type() { return TYPE; }

    private int _tier;
    private IFaction _faction;
    private int _factionStanding;
    private List<ResourceLocation> completedProgressionSteps;
    private CompoundTag codex_unlocks;

    private ProgressionSyncMessageToClient() {
        this.completedProgressionSteps = new ArrayList<ResourceLocation>();
        this.messageIsValid = false;
    }

    public ProgressionSyncMessageToClient(int tier, IFaction faction, int factionStanding, List<ResourceLocation> progressionCompletion, @Nullable CompoundTag codex_unlocks) {
        this.completedProgressionSteps = progressionCompletion;
        this._tier = tier;
        this._faction = faction;
        this._factionStanding = factionStanding;
        this.codex_unlocks = codex_unlocks;
        this.messageIsValid = true;
    }

    public int getTier() {
        return this._tier;
    }

    public IFaction getFaction() {
        return this._faction;
    }

    public int getFactionStanding() {
        return this._factionStanding;
    }

    public List<ResourceLocation> getCompletedProgressionSteps() {
        return this.completedProgressionSteps;
    }

    public static ProgressionSyncMessageToClient decode(FriendlyByteBuf buf) {
        ProgressionSyncMessageToClient msg = new ProgressionSyncMessageToClient();
        try {
            msg._tier = buf.readInt();
            msg._factionStanding = buf.readInt();
            if (buf.readBoolean()) {
                msg._faction = (IFaction)Registries.Factions.getValue(ResourceLocation.parse(buf.readUtf()));
            }
            int count = buf.readInt();
            for (int i = 0; i < count; ++i) {
                msg.completedProgressionSteps.add(buf.readResourceLocation());
            }
            if (buf.readBoolean()) {
                msg.codex_unlocks = buf.readNbt();
            }
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading MagicSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(ProgressionSyncMessageToClient msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getTier());
        buf.writeInt(msg.getFactionStanding());
        buf.writeBoolean(msg.getFaction() != null);
        if (msg.getFaction() != null) {
            buf.writeUtf(Registries.Factions.getKey(msg.getFaction()).toString());
        }
        buf.writeInt(msg.getCompletedProgressionSteps().size());
        for (ResourceLocation rLoc : msg.getCompletedProgressionSteps()) {
            buf.writeResourceLocation(rLoc);
        }
        if (msg.codex_unlocks != null) {
            buf.writeBoolean(true);
            buf.writeNbt(msg.codex_unlocks);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static ProgressionSyncMessageToClient fromCapability(IPlayerProgression capability) {
        return new ProgressionSyncMessageToClient(capability.getTier(), capability.getAlliedFaction(), capability.getFactionStanding(), capability.getCompletedProgressionSteps(), capability.saveCodexUnlocks());
    }

    @Override
    public void Handle(Level world, Player player) {
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.performedInitialSync() && p.getTier() < this.getTier()) {
                ManaAndArtifice.instance.proxy.showCodexEntriesUnlocked();
            }
            p.setTier(this.getTier(), null);
            p.setFactionStanding(this.getFactionStanding());
            p.setAlliedFaction(this.getFaction(), player);
            p.setTierProgression(this.getCompletedProgressionSteps());
            if (this.codex_unlocks != null) {
                p.loadCodexUnlocks(this.codex_unlocks);
                ManaAndArtifice.instance.proxy.recalculateCodexUnlocks();
            }
            p.setHasSynced();
        });
    }
}

