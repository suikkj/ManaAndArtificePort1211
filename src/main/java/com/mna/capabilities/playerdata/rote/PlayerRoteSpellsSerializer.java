package com.mna.capabilities.playerdata.rote;

import net.minecraft.core.HolderLookup;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.spells.base.ISpellComponent;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

public class PlayerRoteSpellsSerializer implements IAttachmentSerializer<Tag, com.mna.api.capabilities.IPlayerRoteSpells> {
    @Override
    public PlayerRoteSpells read(IAttachmentHolder holder, Tag nbt, HolderLookup.Provider provider) {
        PlayerRoteSpells instance = new PlayerRoteSpells();
        if (nbt instanceof CompoundTag) {
            CompoundTag entryData;
            CompoundTag cnbt = (CompoundTag) nbt;
            if (cnbt.contains("rote_spell_data")) {
                ListTag roteData = cnbt.getList("rote_spell_data", 10);
                for (Tag entryINBT : roteData) {
                    if (!(entryINBT instanceof CompoundTag) || !(entryData = (CompoundTag) entryINBT).contains("id") || !entryData.contains("xp")) continue;
                    instance.setRoteXP(ResourceLocation.parse(entryData.getString("id")), entryData.getFloat("xp"));
                }
            }
            if (cnbt.contains("mastery_spell_data")) {
                ListTag masteryData = cnbt.getList("mastery_spell_data", 10);
                for (Tag entryINBT : masteryData) {
                    if (!(entryINBT instanceof CompoundTag) || !(entryData = (CompoundTag) entryINBT).contains("id") || !entryData.contains("mastery")) continue;
                    instance.setMastery(ResourceLocation.parse(entryData.getString("id")), entryData.getFloat("mastery"));
                }
            }
        } else {
            ManaAndArtifice.LOGGER.error("Rote Spell NBT passed back not an instance of CompoundNBT - save data was NOT loaded!");
        }
        return instance;
    }

    @Override
    public Tag write(com.mna.api.capabilities.IPlayerRoteSpells instance, HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        ListTag roteData = new ListTag();
        ListTag masteryData = new ListTag();
        for (Map.Entry<ISpellComponent, Float> entry : instance.getRoteData().entrySet()) {
            CompoundTag entryData = new CompoundTag();
            entryData.putString("id", entry.getKey().getRegistryName().toString());
            entryData.putFloat("xp", entry.getValue());
            roteData.add(entryData);
        }
        for (Map.Entry<ISpellComponent, Float> entry : instance.getMasteryData().entrySet()) {
            CompoundTag entryData = new CompoundTag();
            entryData.putString("id", entry.getKey().getRegistryName().toString());
            entryData.putFloat("mastery", entry.getValue());
            masteryData.add(entryData);
        }
        nbt.put("rote_spell_data", roteData);
        nbt.put("mastery_spell_data", masteryData);
        return nbt;
    }
}
