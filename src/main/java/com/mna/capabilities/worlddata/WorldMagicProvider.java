 
package com.mna.capabilities.worlddata;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IRitualTeleportLocation;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.capabilities.worlddata.InterdictionData;
import com.mna.capabilities.worlddata.RitualTeleportLocation;
import com.mna.capabilities.worlddata.WorldMagic;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import java.util.function.Supplier;

public class WorldMagicProvider {
    private boolean isOverworld = false;
    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<com.mna.api.capabilities.IWorldMagic>> MAGIC = com.mna.capabilities.CapabilitiesInit.WORLD_MAGIC;
    IWorldMagic holder = () -> new WorldMagic(this.isOverworld);
    private final String KEY_TELEPORT_LOCATIONS = "teleport_locations";
    private final String KEY_TELEPORT_LOCATION_COUNT = "teleport_locations_size";
    private final String KEY_TELEPORT_LOCATION_PREFIX = "teleport_location_";
    private final String KEY_INTERDICTION_ZONES = "interdiction_zones";
    private final String KEY_RUNES = "runes";
    private final String KEY_WARDING_CANDLE_LOCATIONS = "warding_candle_locations";
    private final String KEY_WELLSPRINGS = "wellsprings";

    public WorldMagicProvider(boolean isOverworld) {
        this.isOverworld = isOverworld;
    }

    public Tag serializeNBT() {
        UUID[] playerIDs;
        IWorldMagic instance = (IWorldMagic)this.holder;
        CompoundTag nbt_main = new CompoundTag();
        IRitualTeleportLocation[] locations = instance.getAllTeleportLocations();
        CompoundTag teleport_locations = new CompoundTag();
        teleport_locations.putInt("teleport_locations_size", locations.length);
        for (int i = 0; i < locations.length; ++i) {
            CompoundTag teleport_location_nbt = new CompoundTag();
            ((RitualTeleportLocation)locations[i]).writeToNBT(teleport_location_nbt);
            teleport_locations.put("teleport_location_" + i, (Tag)teleport_location_nbt);
        }
        nbt_main.put("teleport_locations", (Tag)teleport_locations);
        HashMap<Long, InterdictionData> interdiction_zone_data = instance.getInterdictionZones();
        ListTag interdiction_zones = new ListTag();
        interdiction_zone_data.forEach((pos, data) -> {
            CompoundTag interdiction_zone_nbt = new CompoundTag();
            interdiction_zone_nbt.putLong("origin", pos.longValue());
            interdiction_zone_nbt.put("data", (Tag)data.save());
            interdiction_zones.add(interdiction_zone_nbt);
        });
        nbt_main.put("interdiction_zones", (Tag)interdiction_zones);
        ListTag runes = new ListTag();
        for (UUID playerID : playerIDs = instance.getKnownPlayersWithSigils()) {
            UUID[] known_runes;
            if (playerID == null || (known_runes = instance.getKnownSigilsForPlayer(playerID)).length == 0) continue;
            CompoundTag playerRuneData = new CompoundTag();
            playerRuneData.putString("playerID", playerID.toString());
            ListTag runeIDs = new ListTag();
            for (UUID runeID : known_runes) {
                runeIDs.add(StringTag.valueOf((String)runeID.toString()));
            }
            playerRuneData.put("runeIDs", (Tag)runeIDs);
            runes.add(playerRuneData);
        }
        nbt_main.put("runes", (Tag)runes);
        nbt_main.put("warding_candle_locations", (Tag)new LongArrayTag(instance.getAllWardingCandleLocations()));
        CompoundTag wellspringData = new CompoundTag();
        instance.getWellspringRegistry().writeToNBT(wellspringData);
        nbt_main.put("wellsprings", (Tag)wellspringData);
        return nbt_main;
    }

    public void deserializeNBT(Tag nbt) {
        IWorldMagic instance = (IWorldMagic)this.holder;
        if (!(nbt instanceof CompoundTag)) {
            ManaAndArtifice.LOGGER.error("World Magic NBT passed back not an instance of CompoundNBT - all save data was NOT loaded!");
            return;
        }
        CompoundTag nbt_main = (CompoundTag)nbt;
        if (nbt_main.contains("teleport_locations")) {
            CompoundTag teleport_locations = nbt_main.getCompound("teleport_locations");
            if (teleport_locations.contains("teleport_locations_size")) {
                int count = teleport_locations.getInt("teleport_locations_size");
                for (int i = 0; i < count; ++i) {
                    if (teleport_locations.contains("teleport_location_" + i)) {
                        CompoundTag teleport_location_nbt = teleport_locations.getCompound("teleport_location_" + i);
                        RitualTeleportLocation loc = RitualTeleportLocation.fromNBT(teleport_location_nbt);
                        if (loc != null) {
                            instance.setRitualTeleportLocation(loc);
                            continue;
                        }
                        ManaAndArtifice.LOGGER.error("Teleport Location NBT failed to load at index " + i + " - this teleport location was NOT loaded!");
                        continue;
                    }
                    ManaAndArtifice.LOGGER.error("Teleport Locations NBT does not contain teleport location data key " + i + " - this teleport location was NOT loaded!");
                }
            } else {
                ManaAndArtifice.LOGGER.error("Teleport Locations NBT does not contain teleport locations count key - teleport location save data was NOT loaded!");
            }
        } else {
            ManaAndArtifice.LOGGER.error("World Magic NBT does not contain teleport locations key - teleport location save data was NOT loaded!");
        }
        if (nbt_main.contains("interdiction_zones")) {
            ListTag interdiction_zones = nbt_main.getList("interdiction_zones", 10);
            HashMap<Long, InterdictionData> loadedInterdictionZones = new HashMap<Long, InterdictionData>();
            interdiction_zones.forEach(tag -> {
                CompoundTag cTag = (CompoundTag)tag;
                if (cTag.contains("origin", 4) && cTag.contains("data", 10)) {
                    long origin = cTag.getLong("origin");
                    CompoundTag data = cTag.getCompound("data");
                    try {
                        InterdictionData interdictionData = InterdictionData.of(data);
                        loadedInterdictionZones.put(origin, interdictionData);
                    }
                    catch (Throwable t) {
                        ManaAndArtifice.LOGGER.error("Error loading Interdiction Zone NBT: ", t.getMessage());
                    }
                } else {
                    ManaAndArtifice.LOGGER.error("Interdiction Zone NBT does not contain required keys - one or more interdiction zones were NOT loaded!");
                }
            });
            instance.setInterdictionZones(loadedInterdictionZones);
        } else {
            ManaAndArtifice.LOGGER.error("World Magic NBT does not contain interdiction zones key - interdiction zone save data was NOT loaded!");
        }
        if (nbt_main.contains("runes")) {
            ListTag runes = nbt_main.getList("runes", 10);
            runes.forEach(inbt_player_runes -> {
                CompoundTag player_rune_nbt = (CompoundTag)inbt_player_runes;
                if (player_rune_nbt.contains("playerID") && player_rune_nbt.contains("runeIDs")) {
                    try {
                        UUID playerID = UUID.fromString(player_rune_nbt.getString("playerID"));
                        ListTag player_known_runes = player_rune_nbt.getList("runeIDs", 8);
                        for (int i = 0; i < player_known_runes.size(); ++i) {
                            UUID runeID = null;
                            try {
                                runeID = UUID.fromString(player_known_runes.getString(i));
                            }
                            catch (Exception ex) {
                                ManaAndArtifice.LOGGER.error("Failed to load player rune data");
                                ManaAndArtifice.LOGGER.catching((Throwable)ex);
                            }
                            if (runeID == null) continue;
                            instance.addUnreferencedSigil(playerID, runeID);
                        }
                    }
                    catch (Exception ex) {
                        ManaAndArtifice.LOGGER.error("Failed to load player rune data");
                        ManaAndArtifice.LOGGER.catching((Throwable)ex);
                    }
                }
            });
        } else {
            ManaAndArtifice.LOGGER.error("World Magic NBT does not contain known runes key - known rune save data was NOT loaded!  This is expected if you're upgrading your world from an M&A version less than 1.0.8.2.");
        }
        if (nbt_main.contains("warding_candle_locations", 12)) {
            instance.setWardingCandleLocations(Arrays.stream(nbt_main.getLongArray("warding_candle_locations")).boxed().collect(Collectors.toList()));
        }
        if (nbt_main.contains("wellsprings")) {
            instance.getWellspringRegistry().readFromNBT(nbt_main.getCompound("wellsprings"));
        }
    }
}


