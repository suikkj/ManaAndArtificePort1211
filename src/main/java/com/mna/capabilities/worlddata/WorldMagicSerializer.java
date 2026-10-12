package com.mna.capabilities.worlddata;

import net.minecraft.core.HolderLookup;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.capabilities.IRitualTeleportLocation;
import com.mna.capabilities.worlddata.InterdictionData;
import com.mna.capabilities.worlddata.RitualTeleportLocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class WorldMagicSerializer implements IAttachmentSerializer<Tag, com.mna.api.capabilities.IWorldMagic> {
    @Override
    public WorldMagic read(IAttachmentHolder holder, Tag nbt, HolderLookup.Provider provider) {
        WorldMagic instance = new WorldMagic(false);
        if (!(nbt instanceof CompoundTag)) {
            ManaAndArtifice.LOGGER.error("World Magic NBT passed back not an instance of CompoundNBT - all save data was NOT loaded!");
            return instance;
        }
        CompoundTag nbt_main = (CompoundTag) nbt;
        if (nbt_main.contains("teleport_locations")) {
            CompoundTag teleport_locations = nbt_main.getCompound("teleport_locations");
            if (teleport_locations.contains("teleport_locations_size")) {
                int count = teleport_locations.getInt("teleport_locations_size");
                for (int i = 0; i < count; ++i) {
                    if (teleport_locations.contains("teleport_location_" + i)) {
                        RitualTeleportLocation loc = RitualTeleportLocation.fromNBT(teleport_locations.getCompound("teleport_location_" + i));
                        if (loc != null) {
                            instance.setRitualTeleportLocation(loc);
                        } else {
                            ManaAndArtifice.LOGGER.error("Teleport Location NBT failed to load at index " + i + " - this teleport location was NOT loaded!");
                        }
                    } else {
                        ManaAndArtifice.LOGGER.error("Teleport Locations NBT does not contain teleport location data key " + i + " - this teleport location was NOT loaded!");
                    }
                }
            } else {
                ManaAndArtifice.LOGGER.error("Teleport Locations NBT does not contain teleport locations count key - teleport location save data was NOT loaded!");
            }
        } else {
            ManaAndArtifice.LOGGER.error("World Magic NBT does not contain teleport locations key - teleport location save data was NOT loaded!");
        }
        if (nbt_main.contains("interdiction_zones")) {
            ListTag interdiction_zones = nbt_main.getList("interdiction_zones", 10);
            HashMap<Long, InterdictionData> loaded = new HashMap<>();
            interdiction_zones.forEach(tag -> {
                CompoundTag cTag = (CompoundTag) tag;
                if (cTag.contains("origin", 4) && cTag.contains("data", 10)) {
                    try {
                        loaded.put(cTag.getLong("origin"), InterdictionData.of(cTag.getCompound("data")));
                    } catch (Throwable t) {
                        ManaAndArtifice.LOGGER.error("Error loading Interdiction Zone NBT: ", t.getMessage());
                    }
                } else {
                    ManaAndArtifice.LOGGER.error("Interdiction Zone NBT does not contain required keys - one or more interdiction zones were NOT loaded!");
                }
            });
            instance.setInterdictionZones(loaded);
        }
        if (nbt_main.contains("runes")) {
            ListTag runes = nbt_main.getList("runes", 10);
            runes.forEach(inbt_player_runes -> {
                CompoundTag player_rune_nbt = (CompoundTag) inbt_player_runes;
                if (player_rune_nbt.contains("playerID") && player_rune_nbt.contains("runeIDs")) {
                    try {
                        UUID playerID = UUID.fromString(player_rune_nbt.getString("playerID"));
                        net.minecraft.nbt.ListTag player_known_runes = player_rune_nbt.getList("runeIDs", 8);
                        for (int i = 0; i < player_known_runes.size(); ++i) {
                            UUID runeID = null;
                            try { runeID = UUID.fromString(player_known_runes.getString(i)); }
                            catch (Exception ex) { ManaAndArtifice.LOGGER.error("Failed to load player rune data"); ManaAndArtifice.LOGGER.catching(ex); }
                            if (runeID == null) continue;
                            instance.addUnreferencedSigil(playerID, runeID);
                        }
                    } catch (Exception ex) {
                        ManaAndArtifice.LOGGER.error("Failed to load player rune data");
                        ManaAndArtifice.LOGGER.catching(ex);
                    }
                }
            });
        }
        if (nbt_main.contains("warding_candle_locations", 12)) {
            instance.setWardingCandleLocations(Arrays.stream(nbt_main.getLongArray("warding_candle_locations")).boxed().collect(Collectors.toList()));
        }
        if (nbt_main.contains("wellsprings")) {
            instance.getWellspringRegistry().readFromNBT(nbt_main.getCompound("wellsprings"));
        }
        return instance;
    }

    @Override
    public Tag write(com.mna.api.capabilities.IWorldMagic instance, HolderLookup.Provider provider) {
        CompoundTag nbt_main = new CompoundTag();
        IRitualTeleportLocation[] locations = instance.getAllTeleportLocations();
        CompoundTag teleport_locations = new CompoundTag();
        teleport_locations.putInt("teleport_locations_size", locations.length);
        for (int i = 0; i < locations.length; ++i) {
            CompoundTag teleport_location_nbt = new CompoundTag();
            ((RitualTeleportLocation) locations[i]).writeToNBT(teleport_location_nbt);
            teleport_locations.put("teleport_location_" + i, teleport_location_nbt);
        }
        nbt_main.put("teleport_locations", teleport_locations);
        HashMap<Long, InterdictionData> interdiction_zone_data = instance.getInterdictionZones();
        ListTag interdiction_zones = new ListTag();
        interdiction_zone_data.forEach((pos, data) -> {
            CompoundTag interdiction_zone_nbt = new CompoundTag();
            interdiction_zone_nbt.putLong("origin", pos);
            interdiction_zone_nbt.put("data", data.save());
            interdiction_zones.add(interdiction_zone_nbt);
        });
        nbt_main.put("interdiction_zones", interdiction_zones);
        ListTag runes = new ListTag();
        for (UUID playerID : instance.getKnownPlayersWithSigils()) {
            UUID[] known_runes;
            if (playerID == null || (known_runes = instance.getKnownSigilsForPlayer(playerID)).length == 0) continue;
            CompoundTag playerRuneData = new CompoundTag();
            playerRuneData.putString("playerID", playerID.toString());
            ListTag runeIDs = new ListTag();
            for (UUID runeID : known_runes) {
                runeIDs.add(StringTag.valueOf(runeID.toString()));
            }
            playerRuneData.put("runeIDs", runeIDs);
            runes.add(playerRuneData);
        }
        nbt_main.put("runes", runes);
        nbt_main.put("warding_candle_locations", new LongArrayTag(instance.getAllWardingCandleLocations()));
        CompoundTag wellspringData = new CompoundTag();
        instance.getWellspringRegistry().writeToNBT(wellspringData);
        nbt_main.put("wellsprings", wellspringData);
        return nbt_main;
    }
}
