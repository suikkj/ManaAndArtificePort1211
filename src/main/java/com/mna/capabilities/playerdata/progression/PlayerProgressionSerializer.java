package com.mna.capabilities.playerdata.progression;

import net.minecraft.core.HolderLookup;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.factions.Factions;
import com.mna.Registries;


import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceLocationException;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class PlayerProgressionSerializer implements IAttachmentSerializer<Tag, com.mna.api.capabilities.IPlayerProgression> {
    @Override
    public PlayerProgression read(IAttachmentHolder holder, Tag nbt, HolderLookup.Provider provider) {
        PlayerProgression instance = new PlayerProgression();
        if (nbt instanceof CompoundTag) {
            CompoundTag cnbt = (CompoundTag) nbt;
            if (cnbt.contains("tier")) {
                instance.setTier(cnbt.getInt("tier"), null);
            }
            if (cnbt.contains("ally_cooldown")) {
                instance.setAllyCooldown(cnbt.getInt("ally_cooldown"));
            }
            if (cnbt.contains("faction")) {
                try {
                    ResourceLocation factionID = ResourceLocation.parse(cnbt.getString("faction"));
                    instance.setAlliedFaction((IFaction) Registries.Factions.getValue(factionID), null);
                } catch (ResourceLocationException rex) {
                    String oldFaction;
                    ManaAndArtifice.LOGGER.info("Detected old version data, upgrading player NBT.");
                    switch (oldFaction = cnbt.getString("faction")) {
                        case "ANCIENT_WIZARDS": instance.setAlliedFaction(Factions.COUNCIL, null); break;
                        case "FEY_COURT": instance.setAlliedFaction(Factions.FEY, null); break;
                        case "DEMONS": instance.setAlliedFaction(Factions.DEMONS, null); break;
                        case "UNDEAD": instance.setAlliedFaction(Factions.UNDEAD, null); break;
                        default: ManaAndArtifice.LOGGER.warn("NBT upgrade failed, clearing faction to prevent a crash.  Restore with commands."); instance.setAlliedFaction(null, null);
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
                java.util.List<net.minecraft.nbt.Tag> progressionSteps = cnbt.getList("progression_completion", 8);
                for (net.minecraft.nbt.Tag it : progressionSteps) {
                    instance.addTierProgressionComplete(ResourceLocation.parse(it.getAsString()));
                }
            }
            for (Map.Entry f : Registries.Factions.getEntries()) {
                String key = "raid_chance_" + ((ResourceKey) f.getKey()).location().toString();
                if (cnbt.contains(key)) {
                    instance.setRaidChance((IFaction) f.getValue(), cnbt.getDouble(key));
                }
                if (cnbt.contains(key = "raid_stats_" + ((ResourceKey) f.getKey()).location().toString())) {
                    instance.getFactionDifficultyStats((IFaction) f.getValue()).readFromNBT(cnbt.getCompound(key));
                }
            }
        } else {
            ManaAndArtifice.LOGGER.error("Progression NBT passed back not an instance of CompoundNBT - save data was NOT loaded!");
        }
        return instance;
    }

    @Override
    public Tag write(com.mna.api.capabilities.IPlayerProgression instance, HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("tier", instance.getTier());
        nbt.putInt("ally_cooldown", instance.getAllyCooldown());
        if (instance.hasAlliedFaction()) {
            nbt.putString("faction", Registries.Factions.getKey(instance.getAlliedFaction()).toString());
            nbt.putInt("faction_standing", instance.getFactionStanding());
        }
        net.minecraft.nbt.ListTag progressionSteps = new net.minecraft.nbt.ListTag();
        for (ResourceLocation rLoc : instance.getCompletedProgressionSteps()) {
            progressionSteps.add(StringTag.valueOf(rLoc.toString()));
        }
        nbt.put("progression_completion", progressionSteps);
        for (Map.Entry f : Registries.Factions.getEntries()) {
            String key = "raid_chance_" + ((ResourceKey) f.getKey()).location().toString();
            nbt.putDouble(key, instance.getRaidChance((IFaction) f.getValue()));
            key = "raid_stats_" + ((ResourceKey) f.getKey()).location().toString();
            nbt.put(key, instance.getFactionDifficultyStats((IFaction) f.getValue()).writeToNBT());
        }
        nbt.put("codex_unlocks", instance.saveCodexUnlocks());
        return nbt;
    }
}
