 
package com.mna.capabilities.playerdata.progression;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgression;
import com.mna.factions.Factions;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

public class PlayerProgressionProvider {
    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<com.mna.api.capabilities.IPlayerProgression>> PROGRESSION = com.mna.capabilities.CapabilitiesInit.PLAYER_PROGRESSION;
    private final IPlayerProgression holder = new PlayerProgression();
    final String KEY_TIER = "tier";
    final String KEY_FACTION = "faction";
    final String KEY_FACTION_STANDING = "faction_standing";
    final String KEY_ALLY_COOLDOWN = "ally_cooldown";
    final String KEY_PROGRESSION = "progression_completion";
    final String KEY_RAID_CHANCE = "raid_chance_";
    final String KEY_RAID_STATS = "raid_stats_";
    final String KEY_CODEX_UNLOCKS = "codex_unlocks";

    public Tag serializeNBT() {
        IPlayerProgression instance = (IPlayerProgression)this.holder;
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("tier", instance.getTier());
        nbt.putInt("ally_cooldown", instance.getAllyCooldown());
        if (instance.hasAlliedFaction()) {
            nbt.putString("faction", Registries.Factions.getKey(instance.getAlliedFaction()).toString());
            nbt.putInt("faction_standing", instance.getFactionStanding());
        }
        ListTag progressionSteps = new ListTag();
        for (ResourceLocation rLoc : instance.getCompletedProgressionSteps()) {
            progressionSteps.add(StringTag.valueOf((String)rLoc.toString()));
        }
        nbt.put("progression_completion", (Tag)progressionSteps);
        for (Map.Entry f : Registries.Factions.getEntries()) {
            String key = "raid_chance_" + ((ResourceKey)f.getKey()).location().toString();
            nbt.putDouble(key, instance.getRaidChance((IFaction)f.getValue()));
            key = "raid_stats_" + ((ResourceKey)f.getKey()).location().toString();
            nbt.put(key, (Tag)instance.getFactionDifficultyStats((IFaction)f.getValue()).writeToNBT());
        }
        nbt.put("codex_unlocks", (Tag)instance.saveCodexUnlocks());
        return nbt;
    }

    public void deserializeNBT(Tag nbt) {
        IPlayerProgression instance = (IPlayerProgression)this.holder;
        if (nbt instanceof CompoundTag) {
            CompoundTag cnbt = (CompoundTag)nbt;
            if (cnbt.contains("tier")) {
                instance.setTier(cnbt.getInt("tier"), null);
            }
            if (cnbt.contains("ally_cooldown")) {
                instance.setAllyCooldown(cnbt.getInt("ally_cooldown"));
            }
            if (cnbt.contains("faction")) {
                try {
                    ResourceLocation factionID = ResourceLocation.parse(cnbt.getString("faction"));
                    instance.setAlliedFaction((IFaction)Registries.Factions.getValue(factionID), null);
                }
                catch (ResourceLocationException rex) {
                    String oldFaction;
                    ManaAndArtifice.LOGGER.info("Detected old version data, upgrading player NBT.");
                    switch (oldFaction = cnbt.getString("faction")) {
                        case "ANCIENT_WIZARDS": {
                            instance.setAlliedFaction(Factions.COUNCIL, null);
                            break;
                        }
                        case "FEY_COURT": {
                            instance.setAlliedFaction(Factions.FEY, null);
                            break;
                        }
                        case "DEMONS": {
                            instance.setAlliedFaction(Factions.DEMONS, null);
                            break;
                        }
                        case "UNDEAD": {
                            instance.setAlliedFaction(Factions.UNDEAD, null);
                            break;
                        }
                        default: {
                            ManaAndArtifice.LOGGER.warn("NBT upgrade failed, clearing faction to prevent a crash.  Restore with commands.");
                            instance.setAlliedFaction(null, null);
                        }
                    }
                }
            }
            if (cnbt.contains("faction_standing")) {
                instance.setFactionStanding(cnbt.getInt("faction_standing"));
            }
            if (cnbt.contains("codex_unlocks", 10)) {
                instance.loadCodexUnlocks(cnbt.getCompound("codex_unlocks"));
            }
            if (cnbt.contains("progression_completion")) {
                ListTag progressionSteps = cnbt.getList("progression_completion", 8);
                Iterator it = progressionSteps.iterator();
                while (it.hasNext()) {
                    instance.addTierProgressionComplete(ResourceLocation.parse(((Tag)it.next()).getAsString()));
                }
            }
            for (Map.Entry f : Registries.Factions.getEntries()) {
                String key = "raid_chance_" + ((ResourceKey)f.getKey()).location().toString();
                if (cnbt.contains(key)) {
                    instance.setRaidChance((IFaction)f.getValue(), cnbt.getDouble(key));
                }
                if (!cnbt.contains(key = "raid_stats_" + ((ResourceKey)f.getKey()).location().toString())) continue;
                instance.getFactionDifficultyStats((IFaction)f.getValue()).readFromNBT(cnbt.getCompound(key));
            }
        } else {
            ManaAndArtifice.LOGGER.error("Progression NBT passed back not an instance of CompoundNBT - save data was NOT loaded!");
        }
    }
}


