/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.biome.Biome
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package com.mna.capabilities.worlddata;

import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.capabilities.WellspringNode;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.tools.MATags;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.ServerMessageDispatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class WellspringNodeRegistry
implements IWellspringNodeRegistry {
    public static final TagKey<Biome> IS_VOID = TagKey.create((ResourceKey)Registries.BIOME, (ResourceLocation)ResourceLocation.fromNamespaceAndPath("forge", "is_void"));
    public static final float POWER_CAP = 5000.0f;
    private static final float STRENGTH_TO_MULTIPLIER_FACTOR = 0.1f;
    private HashMap<BlockPos, WellspringNode> _registry = new HashMap();
    private static final HashMap<UUID, HashMap<Affinity, Float>> player_strengths_cached = new HashMap();
    private static final HashMap<UUID, HashMap<Affinity, Float>> player_amounts_cached = new HashMap();
    private static final HashMap<UUID, HashMap<Affinity, Float>> player_diminishing_returns = new HashMap();
    boolean hasDoneInitialSync = false;
    boolean isOverworld = false;

    @Override
    public HashMap<BlockPos, WellspringNode> getNearbyNodes(BlockPos origin, int minDist, int maxDist) {
        int minDistSq = minDist * minDist;
        int maxDistSq = maxDist * maxDist;
        BlockPos testPos = new BlockPos(origin.getX(), 0, origin.getZ());
        return this._registry.entrySet().stream().filter(e -> {
            double dist = ((BlockPos)e.getKey()).distSqr((Vec3i)testPos);
            return dist >= (double)minDistSq && dist <= (double)maxDistSq;
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));
    }

    @Override
    public Optional<WellspringNode> getNodeAt(BlockPos blockPos) {
        BlockPos checkPos = new BlockPos(blockPos.getX(), 0, blockPos.getZ());
        return Optional.ofNullable(this._registry.getOrDefault(checkPos, null));
    }

    @Override
    public HashMap<Affinity, Float> getNodeNetworkStrengthFor(UUID playerID, Level world) {
        if (playerID == null) {
            return new HashMap<Affinity, Float>();
        }
        if (world.isClientSide() && !this.hasDoneInitialSync) {
            this.hasDoneInitialSync = true;
            ClientMessageDispatcher.sendRequestWellspringNetworkSyncMessage(false);
        }
        return this._getNodeNetworkStrengthFor(playerID);
    }

    @Override
    public HashMap<Affinity, Float> getNodeNetworkAmountFor(UUID player, Level world) {
        if (world.isClientSide && !this.hasDoneInitialSync) {
            this.hasDoneInitialSync = true;
            ClientMessageDispatcher.sendRequestWellspringNetworkSyncMessage(false);
        }
        return this._getAmountsForPlayer(player);
    }

    @Override
    public HashMap<Affinity, Float> getNodeNetworkAmountFor(Player player) {
        if (player == null || player.getGameProfile() == null || player.getGameProfile().getId() == null) {
            return new HashMap<Affinity, Float>();
        }
        return this.getNodeNetworkAmountFor(player.getGameProfile().getId(), player.level());
    }

    @Override
    public float getEldrinGenerationMultiplierFor(UUID playerID, Level world, Affinity affinity) {
        HashMap<Affinity, Float> networkStrength = this.getNodeNetworkStrengthFor(playerID, world);
        if (!networkStrength.containsKey(affinity)) {
            return 1.0f;
        }
        return 1.0f + networkStrength.get(affinity).floatValue() * 0.1f;
    }

    @Override
    public float consumePower(UUID player, Level world, Affinity type, float amount) {
        if (player == null) {
            return 0.0f;
        }
        HashMap<Affinity, Float> nodeNetworkPower = this.getNodeNetworkAmountFor(player, world);
        if (!nodeNetworkPower.containsKey(type)) {
            return 0.0f;
        }
        float existing = nodeNetworkPower.get(type).floatValue();
        float amountConsumed = Math.min(existing, amount);
        nodeNetworkPower.put(type, Float.valueOf(existing - amountConsumed));
        return amountConsumed;
    }

    @Override
    public float insertPower(UUID player, Level world, Affinity type, float amount) {
        if (player == null) {
            return 0.0f;
        }
        HashMap<Affinity, Float> nodeNetworkPower = this.getNodeNetworkAmountFor(player, world);
        if (!nodeNetworkPower.containsKey(type)) {
            return 0.0f;
        }
        float existing = nodeNetworkPower.get(type).floatValue();
        float amountInserted = Math.min(5000.0f - existing, amount);
        nodeNetworkPower.put(type, Float.valueOf(existing + amountInserted));
        return amountInserted;
    }

    @Override
    public float insertPowerDiminishing(UUID player, Level world, Affinity type, float amount, float diminish) {
        HashMap<Affinity, Float> diminishLevels = player_diminishing_returns.getOrDefault(player, new HashMap());
        float diminishFactor = diminishLevels.getOrDefault(type, Float.valueOf(1.0f)).floatValue();
        float inserted = this.insertPower(player, world, type, amount * diminishFactor);
        diminishLevels.put(type, Float.valueOf(diminishFactor * diminish));
        player_diminishing_returns.put(player, diminishLevels);
        return inserted;
    }

    @Override
    public void resetDiminishing() {
        player_diminishing_returns.clear();
    }

    private HashMap<Affinity, Float> _getNodeNetworkStrengthFor(Player player) {
        if (player == null) {
            return new HashMap<Affinity, Float>();
        }
        UUID playerProfileUUID = player.getUUID();
        return this._getNodeNetworkStrengthFor(playerProfileUUID);
    }

    private HashMap<Affinity, Float> _getAmountsForPlayer(Player player) {
        if (player == null) {
            return new HashMap<Affinity, Float>();
        }
        UUID playerProfileUUID = player.getUUID();
        return this._getAmountsForPlayer(playerProfileUUID);
    }

    private HashMap<Affinity, Float> _getNodeNetworkStrengthFor(UUID playerProfileUUID) {
        if (playerProfileUUID == null) {
            return new HashMap<Affinity, Float>();
        }
        if (!player_strengths_cached.containsKey(playerProfileUUID) || player_strengths_cached.get(playerProfileUUID) == null) {
            HashMap<Affinity, Float> affs = new HashMap<Affinity, Float>();
            for (Affinity aff : Affinity.values()) {
                if (affs.containsKey(aff.getShiftAffinity())) continue;
                affs.put(aff.getShiftAffinity(), Float.valueOf(0.0f));
            }
            player_strengths_cached.put(playerProfileUUID, affs);
        }
        return player_strengths_cached.get(playerProfileUUID);
    }

    private HashMap<Affinity, Float> _getAmountsForPlayer(UUID playerProfileUUID) {
        if (playerProfileUUID == null) {
            return new HashMap<Affinity, Float>();
        }
        if (!player_amounts_cached.containsKey(playerProfileUUID) || player_amounts_cached.get(playerProfileUUID) == null) {
            HashMap<Affinity, Float> affs = new HashMap<Affinity, Float>();
            for (Affinity aff : Affinity.values()) {
                if (affs.containsKey(aff.getShiftAffinity())) continue;
                affs.put(aff.getShiftAffinity(), Float.valueOf(0.0f));
            }
            player_amounts_cached.put(playerProfileUUID, affs);
        }
        return player_amounts_cached.get(playerProfileUUID);
    }

    private void syncNetworkStrengthFor(ServerLevel world, UUID playerId) {
        if (playerId == null) {
            return;
        }
        ServerPlayer player = (ServerPlayer)world.getPlayerByUUID(playerId);
        if (player != null) {
            ServerMessageDispatcher.sendWellspringPowerNetworkSyncMessage(world, player, true);
            ServerMessageDispatcher.sendWellspringSyncMessage(world, player, 128);
        }
    }

    @Override
    public boolean addRandomNode(Level world, BlockPos origin) {
        if (!world.getWorldBorder().isWithinBounds(origin)) {
            return false;
        }
        if (GeneralConfigValues.GenericWellsprings) {
            return this.addNode(world, origin, () -> new WellspringNode(Affinity.UNKNOWN, (float)(5.0 + Math.random() * 20.0)), false);
        }
        Holder biome = world.getBiome(origin);
        return this.addNode(world, origin, () -> new WellspringNode(this.randomAffinityFromBiome(world, (Holder<Biome>)biome, origin), (float)(5.0 + Math.random() * 20.0)), false);
    }

    @Override
    public boolean deleteNodeAt(BlockPos position) {
        if (this._registry.containsKey(position)) {
            this._registry.remove(position);
            return true;
        }
        return false;
    }

    private Affinity randomAffinityFromBiome(Level world, Holder<Biome> holder, BlockPos pos) {
        if (Math.random() < 0.15 || !holder.isBound()) {
            return this.randomAffinityIn(Affinity.values());
        }
        ArrayList<Affinity> validAffinities = new ArrayList<Affinity>();
        if (holder.is(MATags.Biomes.Wellspring.FIRE)) {
            validAffinities.add(Affinity.FIRE);
        }
        if (holder.is(MATags.Biomes.Wellspring.WATER)) {
            validAffinities.add(Affinity.WATER);
        }
        if (holder.is(MATags.Biomes.Wellspring.WIND)) {
            validAffinities.add(Affinity.WIND);
        }
        if (holder.is(MATags.Biomes.Wellspring.EARTH)) {
            validAffinities.add(Affinity.EARTH);
        }
        if (holder.is(MATags.Biomes.Wellspring.ARCANE)) {
            validAffinities.add(Affinity.ARCANE);
        }
        if (holder.is(MATags.Biomes.Wellspring.ENDER) && !holder.is(IS_VOID)) {
            validAffinities.add(Affinity.ENDER);
        }
        if (validAffinities.size() == 0) {
            return Affinity.UNKNOWN;
        }
        return this.randomAffinityIn(validAffinities.toArray(new Affinity[0]));
    }

    private Affinity randomAffinityIn(Affinity ... values) {
        return values[(int)(Math.random() * (double)values.length)];
    }

    @Override
    public boolean addNode(Level world, BlockPos origin, Supplier<WellspringNode> nodeData, boolean force) {
        if (!world.getWorldBorder().isWithinBounds(origin)) {
            return false;
        }
        int distance = GeneralConfigValues.WellspringDistance;
        BlockPos testPos = new BlockPos(origin.getX(), 0, origin.getZ());
        if (!force && this.getNearbyNodes(testPos, 0, distance).size() > 0) {
            return false;
        }
        this._registry.put(testPos, nodeData.get());
        return true;
    }

    @Override
    public boolean claimNode(ServerLevel world, UUID playerId, ResourceLocation playerFaction, BlockPos origin, Affinity affinity) {
        ServerPlayer player;
        if (playerId == null || playerFaction == null) {
            return false;
        }
        MutableBoolean claimed = new MutableBoolean(false);
        this.getNodeAt(origin).ifPresent(node -> {
            if (!node.isClaimed() && playerId != null) {
                if (playerFaction != null) {
                    node.setClaimedBy(playerId, playerFaction, origin.getY());
                    if (node.getAffinity() == Affinity.UNKNOWN) {
                        node.setAffinity(affinity);
                    }
                    HashMap<Affinity, Float> playerStr = this._getNodeNetworkStrengthFor(playerId);
                    playerStr.put(node.getAffinity().getShiftAffinity(), Float.valueOf(playerStr.getOrDefault(node.getAffinity().getShiftAffinity(), Float.valueOf(0.0f)).floatValue() + node.getStrength()));
                    this.syncNetworkStrengthFor(world, playerId);
                    claimed.setTrue();
                }
            } else if ((node.isClaimedBy(playerId) || node.isClaimedBy(playerFaction)) && node.getYLevel() == origin.getY()) {
                claimed.setTrue();
            }
        });
        if (!claimed.getValue().booleanValue() && (player = (ServerPlayer)world.getPlayerByUUID(playerId)) != null) {
            ServerMessageDispatcher.sendWellspringSyncMessage(world, player, 128);
        }
        return claimed.getValue();
    }

    @Override
    public boolean unclaimNode(ServerLevel world, BlockPos origin) {
        MutableBoolean unclaimed = new MutableBoolean(false);
        this.getNodeAt(origin).ifPresent(node -> {
            if (!node.isClaimed() || node.hasForcedYLevel() && node.getYLevel() != origin.getY()) {
                return;
            }
            UUID playerId = node.getClaimedBy();
            if (playerId != null) {
                HashMap<Affinity, Float> playerStr = this._getNodeNetworkStrengthFor(playerId);
                playerStr.put(node.getAffinity().getShiftAffinity(), Float.valueOf(Math.max(playerStr.getOrDefault(node.getAffinity().getShiftAffinity(), Float.valueOf(0.0f)).floatValue() - node.getStrength(), 0.0f)));
            }
            node.clearClaimedBy();
            if (playerId != null) {
                this.syncNetworkStrengthFor(world, playerId);
            }
            unclaimed.setTrue();
        });
        return unclaimed.getValue();
    }

    @Override
    public void serializeNetworkStrength(Player player, CompoundTag nbt, boolean fullSync) {
        if (player == null) {
            return;
        }
        nbt.putBoolean("fullSync", fullSync);
        if (fullSync) {
            CompoundTag playerStrength = new CompoundTag();
            CompoundTag playerAmounts = new CompoundTag();
            ListTag factionStrength = new ListTag();
            this.serializePlayerNetworkStrength(player, playerStrength);
            this.serializePlayerNetworkAmount(player, playerAmounts);
            nbt.put("player", (Tag)playerStrength);
            nbt.put("player_amounts", (Tag)playerAmounts);
            nbt.put("factions", (Tag)factionStrength);
        } else {
            CompoundTag playerStrength = new CompoundTag();
            CompoundTag playerAmounts = new CompoundTag();
            this.serializePlayerNetworkStrength(player, playerStrength);
            this.serializePlayerNetworkStrength(player, playerAmounts);
            nbt.put("player", (Tag)playerStrength);
            nbt.put("player_amounts", (Tag)playerStrength);
        }
    }

    @Override
    public void deserializeNetworkStrength(Player player, CompoundTag nbt) {
        if (player == null || player.getUUID() == null) {
            ManaAndArtifice.LOGGER.error("Received NULL player or player with no UUID to deserializeNetworkStrength");
            return;
        }
        if (nbt.getBoolean("fullSync")) {
            CompoundTag playerStrength = nbt.getCompound("player");
            CompoundTag playerAmounts = nbt.getCompound("player_amounts");
            this.deserializePlayerNetworkStrength(player, playerStrength);
            this.deserializePlayerNetworkAmount(player, playerAmounts);
        } else {
            this.deserializePlayerNetworkStrength(player, nbt.getCompound("player"));
            this.deserializePlayerNetworkAmount(player, nbt.getCompound("player_amounts"));
        }
    }

    private void serializePlayerNetworkAmount(Player player, CompoundTag nbt) {
        if (player == null) {
            return;
        }
        HashMap<Affinity, Float> strengths = this._getAmountsForPlayer(player);
        strengths.entrySet().forEach(e -> nbt.putFloat(((Affinity)((e.getKey()))).getShiftAffinity().name(), ((Float)e.getValue()).floatValue()));
    }

    private void deserializePlayerNetworkAmount(Player player, CompoundTag nbt) {
        if (player == null) {
            return;
        }
        HashMap<Affinity, Float> strengths = new HashMap<Affinity, Float>();
        for (Affinity aff : Affinity.values()) {
            strengths.put(aff.getShiftAffinity(), Float.valueOf(nbt.contains(aff.getShiftAffinity().name()) ? nbt.getFloat(aff.getShiftAffinity().name()) : 0.0f));
        }
        player_amounts_cached.put(player.getUUID(), strengths);
    }

    private void serializePlayerNetworkStrength(Player player, CompoundTag nbt) {
        if (player == null) {
            return;
        }
        HashMap<Affinity, Float> strengths = this._getNodeNetworkStrengthFor(player);
        strengths.entrySet().forEach(e -> nbt.putFloat(((Affinity)((e.getKey()))).getShiftAffinity().name(), ((Float)e.getValue()).floatValue()));
    }

    private void deserializePlayerNetworkStrength(Player player, CompoundTag nbt) {
        if (player == null) {
            return;
        }
        HashMap<Affinity, Float> strengths = new HashMap<Affinity, Float>();
        for (Affinity aff : Affinity.values()) {
            strengths.put(aff.getShiftAffinity(), Float.valueOf(nbt.contains(aff.getShiftAffinity().name()) ? nbt.getFloat(aff.getShiftAffinity().name()) : 0.0f));
        }
        player_strengths_cached.put(player.getUUID(), strengths);
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        ListTag wellspringData = new ListTag();
        this._registry.entrySet().stream().forEach(e -> {
            CompoundTag node = new CompoundTag();
            node.putLong("pos", ((BlockPos)e.getKey()).asLong());
            ((WellspringNode)e.getValue()).writeToNBT(node);
            wellspringData.add(node);
        });
        nbt.put("wellspringData", (Tag)wellspringData);
        CompoundTag wellspringGlobal = new CompoundTag();
        ListTag player_strengths = new ListTag();
        ListTag faction_strengths = new ListTag();
        ListTag player_amounts = new ListTag();
        if (this.isOverworld()) {
            player_strengths_cached.entrySet().forEach(e -> {
                CompoundTag playerStrength = new CompoundTag();
                playerStrength.putUUID("uuid", (UUID)e.getKey());
                ListTag strengths = new ListTag();
                ((HashMap<Affinity, Float>)e.getValue()).entrySet().forEach(str -> {
                    CompoundTag str_entry = new CompoundTag();
                    str_entry.putInt("affinity", ((Affinity)(((str.getKey())))).getShiftAffinity().ordinal());
                    str_entry.putFloat("strength", ((Float)str.getValue()).floatValue());
                    strengths.add(str_entry);
                });
                playerStrength.put("strengths", (Tag)strengths);
                player_strengths.add(playerStrength);
            });
            player_amounts_cached.entrySet().forEach(e -> {
                CompoundTag playerAmount = new CompoundTag();
                playerAmount.putString("uuid", ((UUID)e.getKey()).toString());
                ListTag strengths = new ListTag();
                ((HashMap<Affinity, Float>)e.getValue()).entrySet().forEach(str -> {
                    CompoundTag str_entry = new CompoundTag();
                    str_entry.putInt("affinity", ((Affinity)(((str.getKey())))).getShiftAffinity().ordinal());
                    str_entry.putFloat("strength", ((Float)str.getValue()).floatValue());
                    strengths.add(str_entry);
                });
                playerAmount.put("strengths", (Tag)strengths);
                player_amounts.add(playerAmount);
            });
            wellspringGlobal.put("players", (Tag)player_strengths);
            wellspringGlobal.put("factions", (Tag)faction_strengths);
            wellspringGlobal.put("player_amounts", (Tag)player_amounts);
            nbt.put("wellspringGlobal", (Tag)wellspringGlobal);
        }
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        if (nbt.contains("wellspringData")) {
            ListTag wellspringData = nbt.getList("wellspringData", 10);
            this._registry.clear();
            wellspringData.forEach(inbt -> {
                CompoundTag node = (CompoundTag)inbt;
                BlockPos pos = BlockPos.of((long)node.getLong("pos"));
                WellspringNode value = WellspringNode.fromNBT(node);
                this._registry.put(pos, value);
            });
        }
        if (this.isOverworld) {
            if (nbt.contains("wellspringGlobal")) {
                CompoundTag wellspringGlobal = nbt.getCompound("wellspringGlobal");
                ListTag player_strengths = wellspringGlobal.getList("players", 10);
                ListTag player_amounts = wellspringGlobal.getList("player_amounts", 10);
                player_strengths.forEach(inbt -> {
                    CompoundTag playerStrength = (CompoundTag)inbt;
                    UUID uuid = playerStrength.getUUID("uuid");
                    ListTag strengths = playerStrength.getList("strengths", 10);
                    strengths.forEach(strNbt -> {
                        CompoundTag str_entry = (CompoundTag)strNbt;
                        this._getNodeNetworkStrengthFor(uuid).put(Affinity.values()[str_entry.getInt("affinity")], Float.valueOf(str_entry.getFloat("strength")));
                    });
                });
                player_amounts.forEach(inbt -> {
                    CompoundTag playerAmount = (CompoundTag)inbt;
                    UUID uuid = null;
                    try {
                        uuid = playerAmount.getUUID("uuid");
                    }
                    catch (IllegalArgumentException ex) {
                        try {
                            uuid = UUID.fromString(playerAmount.getString("uuid"));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if (uuid == null) {
                        return;
                    }
                    ListTag strengths = playerAmount.getList("strengths", 10);
                    UUID toUse = uuid;
                    strengths.forEach(strNbt -> {
                        CompoundTag str_entry = (CompoundTag)strNbt;
                        this._getAmountsForPlayer(toUse).put(Affinity.values()[str_entry.getInt("affinity")], Float.valueOf(str_entry.getFloat("strength")));
                    });
                });
            } else if (!nbt.contains("sync")) {
                player_strengths_cached.clear();
                this._registry.entrySet().forEach(e -> {
                    if (((WellspringNode)e.getValue()).isClaimed()) {
                        UUID uuid = ((WellspringNode)e.getValue()).getClaimedBy();
                        HashMap<Affinity, Float> nnStrUUID = this._getNodeNetworkStrengthFor(uuid);
                        nnStrUUID.put(((WellspringNode)e.getValue()).getAffinity().getShiftAffinity(), Float.valueOf(nnStrUUID.get(((WellspringNode)e.getValue()).getAffinity().getShiftAffinity()).floatValue() + ((WellspringNode)e.getValue()).getStrength()));
                    }
                });
            }
        }
    }

    @Override
    public boolean writeToNBT(CompoundTag nbt, BlockPos center, int radius) {
        ListTag wellspringData = new ListTag();
        HashMap<BlockPos, WellspringNode> nearbyNodes = this.getNearbyNodes(center, 0, radius);
        if (nearbyNodes.size() == 0) {
            return false;
        }
        nearbyNodes.entrySet().stream().forEach(e -> {
            CompoundTag node = new CompoundTag();
            node.putLong("pos", ((BlockPos)e.getKey()).asLong());
            ((WellspringNode)e.getValue()).writeToNBT(node);
            wellspringData.add(node);
        });
        nbt.put("wellspringData", (Tag)wellspringData);
        nbt.putBoolean("sync", true);
        return true;
    }

    @Override
    public void setWellspringPower(ServerPlayer player, Affinity aff, float level) {
        if (!player_strengths_cached.containsKey(player.getUUID())) {
            player_strengths_cached.put(player.getUUID(), new HashMap());
        }
        player_strengths_cached.get(player.getUUID()).put(aff, Float.valueOf(level));
    }

    @Override
    public boolean isOverworld() {
        return this.isOverworld;
    }

    @Override
    public void setOverworld() {
        this.isOverworld = true;
    }
}

