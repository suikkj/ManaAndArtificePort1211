 
package com.mna.capabilities.playerdata.rote;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpells;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;

public class PlayerRoteSpellsProvider {
    final String KEY_ROTE_SPELL_DATA = "rote_spell_data";
    final String KEY_MASTERY_SPELL_DATA = "mastery_spell_data";
    final String KEY_ID = "id";
    final String KEY_XP = "xp";
    final String KEY_MASTERY = "mastery";
    public static final java.util.function.Supplier<net.neoforged.neoforge.attachment.AttachmentType<com.mna.api.capabilities.IPlayerRoteSpells>> ROTE = com.mna.capabilities.CapabilitiesInit.PLAYER_ROTE_SPELLS;
    private final IPlayerRoteSpells holder = new PlayerRoteSpells();

    public Tag serializeNBT() {
        CompoundTag entryData;
        IPlayerRoteSpells instance = (IPlayerRoteSpells)this.holder;
        CompoundTag nbt = new CompoundTag();
        ListTag roteData = new ListTag();
        ListTag masteryData = new ListTag();
        for (Map.Entry<ISpellComponent, Float> entry : instance.getRoteData().entrySet()) {
            entryData = new CompoundTag();
            entryData.putString("id", entry.getKey().getRegistryName().toString());
            entryData.putFloat("xp", entry.getValue().floatValue());
            roteData.add(entryData);
        }
        for (Map.Entry<ISpellComponent, Float> entry : instance.getMasteryData().entrySet()) {
            entryData = new CompoundTag();
            entryData.putString("id", entry.getKey().getRegistryName().toString());
            entryData.putFloat("mastery", entry.getValue().floatValue());
            masteryData.add(entryData);
        }
        nbt.put("rote_spell_data", (Tag)roteData);
        nbt.put("mastery_spell_data", (Tag)masteryData);
        return nbt;
    }

    public void deserializeNBT(Tag nbt) {
        IPlayerRoteSpells instance = (IPlayerRoteSpells)this.holder;
        if (nbt instanceof CompoundTag) {
            CompoundTag entryData;
            CompoundTag cnbt = (CompoundTag)nbt;
            if (cnbt.contains("rote_spell_data")) {
                ListTag roteData = cnbt.getList("rote_spell_data", 10);
                for (Tag entryINBT : roteData) {
                    if (!(entryINBT instanceof CompoundTag) || !(entryData = (CompoundTag)entryINBT).contains("id") || !entryData.contains("xp")) continue;
                    instance.setRoteXP(ResourceLocation.parse(entryData.getString("id")), entryData.getFloat("xp"));
                }
            }
            if (cnbt.contains("mastery_spell_data")) {
                ListTag masteryData = cnbt.getList("mastery_spell_data", 10);
                for (Tag entryINBT : masteryData) {
                    if (!(entryINBT instanceof CompoundTag) || !(entryData = (CompoundTag)entryINBT).contains("id") || !entryData.contains("mastery")) continue;
                    instance.setMastery(ResourceLocation.parse(entryData.getString("id")), entryData.getFloat("mastery"));
                }
            }
        } else {
            ManaAndArtifice.LOGGER.error("Rote Spell NBT passed back not an instance of CompoundNBT - save data was NOT loaded!");
        }
    }
}


