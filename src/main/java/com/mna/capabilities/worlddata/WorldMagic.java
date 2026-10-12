/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.capabilities.worlddata;

import com.mna.api.capabilities.IRitualTeleportLocation;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.spells.base.ISpellSigil;
import com.mna.capabilities.worlddata.InterdictionData;
import com.mna.capabilities.worlddata.RitualTeleportLocation;
import com.mna.capabilities.worlddata.WellspringNodeRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WorldMagic
implements IWorldMagic {
    private NonNullList<IRitualTeleportLocation> teleport_locations = NonNullList.create();
    private WellspringNodeRegistry wellsprings;
    private ArrayList<BlockPos> warding_candle_locations;
    private HashMap<Long, InterdictionData> interdiction_zones;
    private Map<UUID, LinkedList<UUID>> playerRunes = new HashMap<UUID, LinkedList<UUID>>();
    private Map<UUID, Entity> knownRunes = new HashMap<UUID, Entity>();

    public WorldMagic() {
        this(false);
    }

    public WorldMagic(boolean isOverworld) {
        this.interdiction_zones = new HashMap();
        this.warding_candle_locations = new ArrayList();
        this.wellsprings = new WellspringNodeRegistry();
        this.wellsprings.isOverworld = isOverworld;
    }

    @Override
    @Nullable
    public IRitualTeleportLocation getRitualTeleportBlockLocation(List<ResourceLocation> reagents, ResourceKey<Level> world) {
        for (int i = 0; i < this.teleport_locations.size(); ++i) {
            if (!((IRitualTeleportLocation)this.teleport_locations.get(i)).matches(reagents)) continue;
            IRitualTeleportLocation location = (IRitualTeleportLocation)this.teleport_locations.get(i);
            location.tryCorrectWorldKey(world);
            return location;
        }
        return null;
    }

    @Override
    @Nullable
    public List<ResourceLocation> getRitualTeleportBlockReagents(BlockPos pos) {
        for (int i = 0; i < this.teleport_locations.size(); ++i) {
            if (!((IRitualTeleportLocation)this.teleport_locations.get(i)).matches(pos)) continue;
            return ((IRitualTeleportLocation)this.teleport_locations.get(i)).getReagents();
        }
        return null;
    }

    @Override
    public IRitualTeleportLocation[] getAllTeleportLocations() {
        Object[] locations = new IRitualTeleportLocation[this.teleport_locations.size()];
        return (IRitualTeleportLocation[])this.teleport_locations.toArray(locations);
    }

    @Override
    public boolean setRitualTeleportLocation(ServerLevel world, BlockPos pos, List<ResourceLocation> reagents, Direction direction) {
        return this.setRitualTeleportLocation(new RitualTeleportLocation((ResourceKey<Level>)world.dimension(), pos, reagents, direction));
    }

    @Override
    public boolean setRitualTeleportLocation(IRitualTeleportLocation location) {
        if (this.getRitualTeleportBlockLocation((List<ResourceLocation>)location.getReagents(), location.getWorldType()) != null) {
            return false;
        }
        this.teleport_locations.add(location);
        return true;
    }

    @Override
    public void removeRitualTeleportLocation(BlockPos pos) {
        for (int i = 0; i < this.teleport_locations.size(); ++i) {
            if (!((IRitualTeleportLocation)this.teleport_locations.get(i)).matches(pos)) continue;
            this.teleport_locations.remove(i);
            break;
        }
    }

    private boolean isBlockWarded(BlockPos pos) {
        return false;
    }

    @Override
    public boolean canPlayerUnlock(BlockPos pos, Player player) {
        if (!this.isBlockWarded(pos)) {
            return true;
        }
        return player.isCreative();
    }

    @Override
    public boolean wardBlock(BlockPos pos, Player caster, Collection<ResourceLocation> patterns) {
        return false;
    }

    @Override
    public void addSigil(Player player, ISpellSigil<?> rune, byte cast_with_bonus) {
        LinkedList<UUID> player_owned_runes;
        if (this.isSigilKnown(rune.getID())) {
            return;
        }
        if (!this.playerRunes.containsKey(player.getUUID())) {
            this.playerRunes.put(player.getUUID(), new LinkedList());
        }
        if (!(player_owned_runes = this.playerRunes.get(player.getUUID())).contains(rune.getID())) {
            while (player_owned_runes.size() >= GeneralConfigValues.SigilLimit + rune.getCountBonus()) {
                UUID first = player_owned_runes.poll();
                this.removeSigil(player.getUUID(), first);
            }
            player_owned_runes.add(rune.getID());
        }
        this.knownRunes.put(rune.getID(), (Entity)rune);
    }

    @Override
    public void addUnreferencedSigil(UUID player, UUID rune) {
        if (!this.playerRunes.containsKey(player)) {
            this.playerRunes.put(player, new LinkedList());
        }
        LinkedList<UUID> player_owned_runes = this.playerRunes.get(player);
        player_owned_runes.add(rune);
    }

    @Override
    public void removeSigil(UUID player, UUID rune) {
        if (this.playerRunes.containsKey(player)) {
            this.playerRunes.get(player).remove(rune);
        }
        if (this.knownRunes.containsKey(rune)) {
            Entity runeEntity = this.knownRunes.get(rune);
            if (runeEntity != null && runeEntity.isAddedToWorld()) {
                runeEntity.remove(Entity.RemovalReason.DISCARDED);
            }
            this.knownRunes.remove(rune);
        }
    }

    @Override
    public boolean isSigilKnown(UUID rune) {
        if (rune == null) {
            return false;
        }
        return this.knownRunes.containsKey(rune);
    }

    @Override
    public boolean wasSigilRemoved(UUID player, UUID rune) {
        return !this.playerRunes.containsKey(player) || !this.playerRunes.get(player).contains(rune);
    }

    @Override
    public UUID[] getKnownPlayersWithSigils() {
        return this.playerRunes.keySet().toArray(new UUID[0]);
    }

    @Override
    public UUID[] getKnownSigilsForPlayer(UUID player) {
        if (!this.playerRunes.containsKey(player)) {
            return new UUID[0];
        }
        return this.playerRunes.get(player).toArray(new UUID[0]);
    }

    @Override
    public List<Long> getAllWardingCandleLocations() {
        return this.warding_candle_locations.stream().map(bp -> bp.asLong()).collect(Collectors.toList());
    }

    @Override
    public void setWardingCandleLocations(List<Long> locations) {
        this.warding_candle_locations.clear();
        this.warding_candle_locations.addAll(locations.stream().map(l -> BlockPos.of((long)l)).collect(Collectors.toList()));
    }

    @Override
    public void addWardingCandleLocation(BlockPos location) {
        long posLong = location.asLong();
        if (!this.warding_candle_locations.stream().anyMatch(pos -> pos.asLong() == posLong)) {
            this.warding_candle_locations.add(location);
        }
    }

    @Override
    public void removeWardingCandleLocation(BlockPos location) {
        long posLong = location.asLong();
        this.warding_candle_locations.removeIf(pos -> pos.asLong() == posLong);
    }

    @Override
    public boolean isWithinWardingCandle(BlockPos pos) {
        return this.warding_candle_locations.stream().anyMatch(p -> AABB.ofSize((Vec3)Vec3.atCenterOf((Vec3i)p), (double)64.0, (double)64.0, (double)64.0).contains(Vec3.atCenterOf((Vec3i)pos)));
    }

    @Override
    public boolean isInterdicted(LivingEntity teleporter, Vec3 source, Vec3 dest) {
        return this.interdiction_zones.values().stream().anyMatch(zone -> {
            if (zone.isAlly(teleporter)) {
                return false;
            }
            return zone.getDirection() ? zone.includes(dest) : zone.includes(source);
        });
    }

    @Override
    public void addInterdictionZone(BlockPos pos, InterdictionData data) {
        this.interdiction_zones.put(pos.asLong(), data);
    }

    @Override
    public void removeInterdictionZone(BlockPos pos) {
        this.interdiction_zones.remove(pos.asLong());
    }

    @Override
    public HashMap<Long, InterdictionData> getInterdictionZones() {
        return this.interdiction_zones;
    }

    @Override
    public void setInterdictionZones(HashMap<Long, InterdictionData> data) {
        this.interdiction_zones = data;
    }

    @Override
    public IWellspringNodeRegistry getWellspringRegistry() {
        return this.wellsprings;
    }
}

