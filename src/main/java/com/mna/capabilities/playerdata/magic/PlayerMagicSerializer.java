package com.mna.capabilities.playerdata.magic;

import net.minecraft.core.HolderLookup;
import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import com.mna.api.tools.MNAItemData;

/**
 * Attachments serializer para PlayerMagic.
 * (Lógica movida de PlayerMagicProvider; dados serao persistidos via AttachmentType.)
 */
public class PlayerMagicSerializer implements IAttachmentSerializer<Tag, com.mna.api.capabilities.IPlayerMagic> {

    @Override
    public PlayerMagic read(IAttachmentHolder holder, Tag nbt, HolderLookup.Provider provider) {
        PlayerMagic instance = new PlayerMagic();
        if (nbt instanceof CompoundTag) {
            CompoundTag cnbt = (CompoundTag) nbt;
            if (cnbt.contains("magic_level")) {
                instance.setMagicLevel(null, cnbt.getInt("magic_level"));
            }
            if (cnbt.contains("casting_resource_id")) {
                instance.setCastingResourceType(ResourceLocation.parse(cnbt.getString("casting_resource_id")));
            }
            instance.getCastingResource().readNBT(cnbt);
            instance.getCastingResource().setNeedsSync();
            if (cnbt.contains("teleport_salt")) {
                instance.setTeleportSalt(cnbt.getInt("teleport_salt"));
            }
            if (cnbt.contains("portal_cooldown")) {
                instance.setPortalCooldown(cnbt.getInt("portal_cooldown"));
            }
            if (cnbt.contains("spell_color_override")) {
                instance.setParticleColorOverride(cnbt.getInt("spell_color_override"));
            }
            if (cnbt.contains("air_casts")) {
                instance.setAirCasts(cnbt.getInt("air_casts"));
            }
            if (cnbt.contains("air_jumps")) {
                instance.setAirJumps(cnbt.getInt("air_jumps"));
            }
            if (cnbt.contains("ember_cooldown")) {
                instance.setEmberCooldown(cnbt.getInt("ember_cooldown"));
            }
            if (cnbt.contains("did_allow_flying")) {
                instance.setDidAllowFlying(cnbt.getBoolean("did_allow_flying"));
            }
            if (cnbt.contains("rift")) {
                this.readInventory(instance.getRiftInventory(), cnbt.getCompound("rift"), provider);
            }
            if (cnbt.contains("grimoire")) {
                this.readInventory(instance.getGrimoireInventory(), cnbt.getCompound("grimoire"), provider);
            }
            if (cnbt.contains("rote")) {
                this.readInventory(instance.getRoteInventory(), cnbt.getCompound("rote"), provider);
            }
            if (cnbt.contains("magic_xp")) {
                instance.setMagicXP(cnbt.getInt("magic_xp"));
            }
            for (Affinity aff : Affinity.values()) {
                if (!cnbt.contains("affinity_" + aff.toString().toLowerCase())) continue;
                instance.setAffinityDepth(aff, cnbt.getFloat("affinity_" + aff.toString().toLowerCase()));
            }
            instance.getChronoAnchorData().readFromNBT(cnbt);
            if (cnbt.contains("cantrips")) {
                instance.getCantripData().readFromNBT(cnbt.getCompound("cantrips"));
            }
            if (cnbt.contains("banked_armor_repair")) {
                ListTag banked_armor = cnbt.getList("banked_armor_repair", 10);
                HashMap<Integer, Float> parsed = new HashMap<>();
                for (int i = 0; i < banked_armor.size(); ++i) {
                    CompoundTag bank = banked_armor.getCompound(i);
                    if (!bank.contains("key") || !bank.contains("value")) continue;
                    parsed.put(bank.getInt("key"), bank.getFloat("value"));
                }
                instance.setBankedArmorRepair(parsed);
            }
            instance.validate();
            instance.forceSync();
        } else {
            ManaAndArtifice.LOGGER.error("Mana NBT passed back not an instance of CompoundNBT - save data was NOT loaded!");
        }
        return instance;
    }

    @Override
    public Tag write(com.mna.api.capabilities.IPlayerMagic instance, HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("casting_resource_id", instance.getCastingResource().getRegistryName().toString());
        instance.getCastingResource().writeNBT(nbt);
        nbt.putInt("teleport_salt", instance.getTeleportSalt());
        nbt.putInt("air_casts", instance.getAirCasts());
        nbt.putInt("air_jumps", instance.getAirJumps());
        nbt.putInt("ember_cooldown", instance.getEmberCooldown());
        nbt.putInt("spell_color_override", instance.getParticleColorOverride());
        nbt.put("rift", this.saveInventory(instance.getRiftInventory(), provider));
        nbt.put("grimoire", this.saveInventory(instance.getGrimoireInventory(), provider));
        nbt.put("rote", this.saveInventory(instance.getRoteInventory(), provider));
        nbt.putBoolean("did_allow_flying", instance.didAllowFlying());
        for (Affinity aff : Affinity.values()) {
            nbt.putFloat("affinity_" + aff.toString().toLowerCase(), instance.getAffinityDepth(aff));
        }
        nbt.putInt("magic_level", instance.getMagicLevel());
        nbt.putInt("magic_xp", instance.getMagicXP());
        instance.getChronoAnchorData().writeToNBT(nbt);
        nbt.put("cantrips", instance.getCantripData().writeToNBT(false));
        ListTag armor_repair = new ListTag();
        for (Map.Entry<Integer, Float> bank_slot : instance.getBankedArmorRepair().entrySet()) {
            CompoundTag bank = new CompoundTag();
            bank.putInt("key", bank_slot.getKey());
            bank.putFloat("value", bank_slot.getValue());
            armor_repair.add(bank);
        }
        nbt.put("banked_armor_repair", armor_repair);
        return nbt;
    }

    private CompoundTag saveInventory(Container inventory, HolderLookup.Provider provider) {
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            items.add(inventory.getItem(i));
        }
        CompoundTag inv = new CompoundTag();
        MNAItemData.saveItems(inv, items);
        return inv;
    }

    private void readInventory(Container inventory, CompoundTag nbt, HolderLookup.Provider provider) {
        NonNullList<ItemStack> items = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        MNAItemData.loadItems(nbt, items);
        for (int i = 0; i < items.size(); ++i) {
            if (inventory.getContainerSize() <= i) continue;
            inventory.setItem(i, items.get(i));
        }
    }
}
