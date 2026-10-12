 
package com.mna.network.messages.to_client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.mna.api.tools.RLoc;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.network.messages.BaseClientMessage;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class RoteProgressSyncMessageToClient
extends BaseClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RoteProgressSyncMessageToClient> TYPE = new CustomPacketPayload.Type<>(RLoc.create("rote_progress_sync_message_to_client"));
    public static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RoteProgressSyncMessageToClient> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((b, v) -> RoteProgressSyncMessageToClient.encode(v, b), RoteProgressSyncMessageToClient::decode);

    @Override
    public CustomPacketPayload.Type<RoteProgressSyncMessageToClient> type() { return TYPE; }

    private HashMap<ResourceLocation, Float> roteSpellProgress = new HashMap();
    private HashMap<ResourceLocation, Float> spellMastery = new HashMap();

    private RoteProgressSyncMessageToClient() {
        this.messageIsValid = false;
    }

    public RoteProgressSyncMessageToClient(HashMap<ISpellComponent, Float> roteSpellProgress, HashMap<ISpellComponent, Float> masteryProgress) {
        this();
        for (Map.Entry<ISpellComponent, Float> e : roteSpellProgress.entrySet()) {
            if (e == null) continue;
            this.roteSpellProgress.put(e.getKey().getRegistryName(), e.getValue());
        }
        for (Map.Entry<ISpellComponent, Float> e : masteryProgress.entrySet()) {
            if (e == null) continue;
            this.spellMastery.put(e.getKey().getRegistryName(), e.getValue());
        }
        this.messageIsValid = true;
    }

    public HashMap<ResourceLocation, Float> getRoteProgress() {
        return this.roteSpellProgress;
    }

    public HashMap<ResourceLocation, Float> getSpellMastery() {
        return this.spellMastery;
    }

    public static RoteProgressSyncMessageToClient decode(FriendlyByteBuf buf) {
        RoteProgressSyncMessageToClient msg = new RoteProgressSyncMessageToClient();
        try {
            int roteCount = buf.readInt();
            for (int i = 0; i < roteCount; ++i) {
                ResourceLocation rLoc = buf.readResourceLocation();
                float xp = buf.readFloat();
                msg.getRoteProgress().put(rLoc, Float.valueOf(xp));
            }
            int masteryCount = buf.readInt();
            for (int i = 0; i < masteryCount; ++i) {
                ResourceLocation rLoc = buf.readResourceLocation();
                float mastery = buf.readFloat();
                msg.getSpellMastery().put(rLoc, Float.valueOf(mastery));
            }
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            ManaAndArtifice.LOGGER.error("Exception while reading RoteSyncMessageToClient: " + e);
            return msg;
        }
        msg.messageIsValid = true;
        return msg;
    }

    public static void encode(RoteProgressSyncMessageToClient msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.getRoteProgress().size());
        for (Map.Entry<ResourceLocation, Float> e : msg.getRoteProgress().entrySet()) {
            buf.writeResourceLocation(e.getKey());
            buf.writeFloat(e.getValue().floatValue());
        }
        buf.writeInt(msg.getSpellMastery().size());
        for (Map.Entry<ResourceLocation, Float> e : msg.getSpellMastery().entrySet()) {
            buf.writeResourceLocation(e.getKey());
            buf.writeFloat(e.getValue().floatValue());
        }
    }

    public static RoteProgressSyncMessageToClient fromCapability(IPlayerRoteSpells capability) {
        return new RoteProgressSyncMessageToClient(capability.getRoteData(), capability.getMasteryData());
    }

    @Override
    public void Handle(Level world, Player player) {
        MNACap.get(player, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
            r.resetMastery();
            r.resetRote();
            for (Map.Entry<ResourceLocation, Float> e : this.getRoteProgress().entrySet()) {
                r.setRoteXP(e.getKey(), e.getValue().floatValue());
            }
            for (Map.Entry<ResourceLocation, Float> e : this.getSpellMastery().entrySet()) {
                r.setMastery(e.getKey(), e.getValue().floatValue());
            }
        });
    }
}

