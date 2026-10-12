 
package com.mna.entities.constructs.animated;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNAtierSorting;
import com.mna.api.tools.MNAItemData;

public class ConstructConstruction
implements IConstructConstruction {
    private static final String KEY_NBT = "animated_construct_composition";
    private static final String KEY_AFFINITY = "animated_construct_affinity";
    private static final String KEY_SPELLS = "animated_construct_spells";
    private static final String KEY_HAT = "animated_construct_hat";
    private static final String KEY_BANNER = "animated_construct_banner";
    private static final float WIND_SPEED_MODIFIER = 0.015f;
    private static final float WATER_BUOYANCY_MODIFIER = 4.0f;
    private HashMap<ConstructSlot, ItemConstructPart> partsList;
    private ArrayList<ConstructMaterial> composition;
    private ArrayList<ConstructCapability> enabledCapabilities;
    private HashMap<Affinity, Integer> affinityData;
    private HashMap<ConstructSlot, ISpellDefinition> castableSpells;
    private ItemStack hat = ItemStack.EMPTY;
    private ItemStack banner = ItemStack.EMPTY;

    public ConstructConstruction() {
        this.partsList = new HashMap();
        this.composition = new ArrayList();
        this.enabledCapabilities = new ArrayList();
        this.affinityData = new HashMap();
        this.castableSpells = new HashMap();
    }

    @Override
    public boolean isComplete() {
        for (ConstructSlot slot : ConstructSlot.values()) {
            if (this.partsList.containsKey(slot) && this.partsList.get(slot) != null) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        for (ConstructSlot slot : ConstructSlot.values()) {
            if (!this.partsList.containsKey(slot) || this.partsList.get(slot) == null) continue;
            return false;
        }
        return true;
    }

    private void calculateCompositionAndCapabilities() {
        this.composition.clear();
        this.enabledCapabilities.clear();
        for (ItemConstructPart part : this.partsList.values()) {
            if (!this.composition.contains(part.getMaterial())) {
                this.composition.add(part.getMaterial());
            }
            for (ConstructCapability cap : part.getEnabledCapabilities()) {
                if (this.enabledCapabilities.contains(cap)) continue;
                this.enabledCapabilities.add(cap);
            }
        }
    }

    @Override
    public float calculateExplosionResistance() {
        float resistance = 0.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            resistance += part.getMaterial().getExplosionResistance();
        }
        return resistance;
    }

    @Override
    public float calculateBuoyancy() {
        float buoyancy = 0.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            buoyancy += part.getMaterial().getBuoyancy();
        }
        return buoyancy += (float)this.getAffinityScore(Affinity.WATER) * 4.0f;
    }

    @Override
    public int calculateMaxHealth() {
        int maxHealth = 0;
        for (ItemConstructPart part : this.partsList.values()) {
            maxHealth += part.getMaterial().getHealth();
        }
        return maxHealth;
    }

    @Override
    public float calculateSpeed() {
        float speed = 0.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            speed += part.getMaterial().getSpeed() + part.getBonusSpeed();
        }
        return speed += (float)this.getAffinityScore(Affinity.WIND) * 0.015f;
    }

    @Override
    public int calculateAttackRate() {
        int attackRate = 20;
        for (ItemConstructPart part : this.partsList.values()) {
            attackRate += part.getAttackSpeedModifier();
        }
        return attackRate;
    }

    @Override
    public float calculateDamage() {
        float damage = 1.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            damage += part.getAttackDamage();
        }
        return damage += (float)this.getAffinityScore(Affinity.EARTH) * 0.5f;
    }

    @Override
    public int calculateIntelligence() {
        int intelligence = 8;
        for (ItemConstructPart part : this.partsList.values()) {
            intelligence += part.getIntelligenceBonus();
        }
        return intelligence;
    }

    @Override
    public int calculatePerception() {
        int perception = 12 + this.getAffinityScore(Affinity.WATER) * 4;
        for (ItemConstructPart part : this.partsList.values()) {
            perception += part.getPerceptionDistanceBonus();
        }
        return perception;
    }

    @Override
    public float calculateKnockback() {
        float knockback = 0.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            knockback += part.getKnockbackBonus();
        }
        knockback = (float)((double)knockback + 0.25 * (double)this.getAffinityScore(Affinity.WIND));
        return knockback;
    }

    @Override
    public float calculateKnockbackResistance() {
        float kbResist = 0.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            kbResist += part.getMaterial().getKnockbackResistance();
        }
        return MathUtils.clamp01(kbResist);
    }

    @Override
    public float calculateMana() {
        float mana = 0.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            mana += (float)part.getManaCapacity();
        }
        mana = (float)((double)mana + (double)mana * 0.25 * (double)this.getAffinityScore(Affinity.ARCANE));
        return mana;
    }

    @Override
    public int calculateFluidCapacity() {
        int cap = 0;
        for (ItemConstructPart part : this.partsList.values()) {
            cap += part.getFluidCapacity();
        }
        cap = (int)((double)cap + (double)cap * 0.25 * (double)this.getAffinityScore(Affinity.WATER));
        return cap;
    }

    @Override
    public int calculateArmor() {
        int armor = this.getAffinityScore(Affinity.EARTH);
        for (ItemConstructPart part : this.partsList.values()) {
            armor += part.getArmor();
        }
        return armor;
    }

    @Override
    public int calculateToughness() {
        int toughness = this.getAffinityScore(Affinity.EARTH) / 2;
        for (ItemConstructPart part : this.partsList.values()) {
            toughness += part.getToughness();
        }
        return toughness;
    }

    @Override
    public float calculateRangedDamage() {
        float damage = 0.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            damage += part.getRangedAttackDamage();
        }
        return damage += (float)this.getAffinityScore(Affinity.WIND) * 0.5f;
    }

    @Override
    public int calculateInventorySize() {
        int inventorySize = 0;
        for (ItemConstructPart part : this.partsList.values()) {
            inventorySize += part.getInventorySizeBonus();
        }
        return inventorySize;
    }

    @Override
    public int calculateInventoryStackLimit() {
        int limit = 0;
        for (ItemConstructPart part : this.partsList.values()) {
            limit += part.getBackpackCapacityBoost();
        }
        int enderAffinity = this.getAffinityScore(Affinity.ENDER);
        return Mth.clamp((int)(limit *= enderAffinity + 1), (int)1, (int)64);
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return this.enabledCapabilities.toArray(new ConstructCapability[0]);
    }

    @Override
    public boolean isCapabilityEnabled(ConstructCapability cap) {
        return this.enabledCapabilities.contains(cap);
    }

    @Override
    public boolean areCapabilitiesEnabled(ConstructCapability ... caps) {
        for (ConstructCapability cap : caps) {
            if (this.enabledCapabilities.contains(cap)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isAnyCapabilityEnabled(ConstructCapability ... caps) {
        for (ConstructCapability cap : caps) {
            if (!this.enabledCapabilities.contains(cap)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void resetParts() {
        this.partsList.clear();
        this.calculateCompositionAndCapabilities();
    }

    @Override
    public ItemStack setPart(ItemStack part) {
        return this.setPart(part, true);
    }

    @Override
    public ItemStack setPart(ItemStack part, boolean recalculate) {
        ISpellDefinition recipe;
        ItemConstructPart partItem = (ItemConstructPart)part.getItem();
        ItemStack output = ItemStack.EMPTY;
        if (this.partsList.containsKey(partItem.getSlot()) && this.partsList.get(partItem.getSlot()) != null) {
            output = new ItemStack((ItemLike)this.partsList.get(partItem.getSlot()));
        }
        this.partsList.put(partItem.getSlot(), partItem);
        if (output.getItem() instanceof ICanContainSpell && (recipe = (ISpellDefinition)this.castableSpells.getOrDefault(partItem.getSlot(), null)) != null && recipe.isValid()) {
            recipe.writeToNBT(output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        }
        if (partItem instanceof ICanContainSpell && ((SpellRecipe)(recipe = SpellRecipe.fromNBT(part.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()))).isValid()) {
            this.castableSpells.put(partItem.getSlot(), recipe);
        }
        if (recalculate) {
            this.calculateCompositionAndCapabilities();
        }
        return output;
    }

    @Override
    public ItemStack removePart(ConstructSlot slot) {
        if (this.partsList.containsKey(slot)) {
            ISpellDefinition spell = this.castableSpells.remove(slot);
            ItemStack removed = new ItemStack((ItemLike)this.partsList.remove(slot));
            if (spell != null) {
                spell.writeToNBT(removed.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            }
            return removed;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setHat(ItemStack stack) {
        this.hat = stack.copy();
    }

    @Override
    public void setBanner(ItemStack stack) {
        this.banner = stack.copy();
    }

    @Override
    public ItemStack getHat() {
        return this.hat;
    }

    @Override
    public ItemStack getBanner() {
        return this.banner;
    }

    @Override
    public Optional<ItemConstructPart> getPart(ConstructSlot slot) {
        if (this.partsList.containsKey(slot)) {
            return Optional.of(this.partsList.get(slot));
        }
        return Optional.empty();
    }

    @Override
    public List<ConstructMaterial> getComposition() {
        return (List)this.composition.clone();
    }

    @Override
    public List<ItemConstructPart> getPartsForMaterial(ConstructMaterial material) {
        return this.partsList.values().stream().filter(p -> p.getMaterial() == material).collect(Collectors.toList());
    }

    @Override
    public int getAffinityScore(Affinity aff) {
        Affinity affinityShifted = aff.getShiftAffinity();
        int total = 0;
        for (Affinity a : Affinity.values()) {
            if (affinityShifted != a.getShiftAffinity() || !this.affinityData.containsKey(a)) continue;
            total += this.affinityData.get(a).intValue();
        }
        return total;
    }

    @Override
    public void setAffinityScore(Affinity aff, int value) {
        this.affinityData.put(aff, Mth.clamp((int)value, (int)0, (int)8));
    }

    @Override
    public Affinity getRandomContainedAffinity() {
        Affinity[] contained = this.affinityData.keySet().toArray(new Affinity[0]);
        if (contained.length == 0) {
            return Affinity.UNKNOWN;
        }
        return contained[(int)(Math.random() * (double)contained.length)];
    }

    @Override
    public ConstructConstruction copy() {
        ConstructConstruction clone = new ConstructConstruction();
        for (ItemConstructPart itemConstructPart : this.partsList.values()) {
            clone.setPart(new ItemStack((ItemLike)itemConstructPart), false);
        }
        for (Map.Entry entry : this.affinityData.entrySet()) {
            clone.setAffinityScore((Affinity)(entry.getKey()), (Integer)entry.getValue());
        }
        clone.castableSpells.clear();
        this.castableSpells.entrySet().forEach(e -> {
            CompoundTag tag = new CompoundTag();
            ((ISpellDefinition)e.getValue()).writeToNBT(tag);
            clone.castableSpells.put((ConstructSlot)(((e.getKey()))), ManaAndArtificeMod.getSpellHelper().parseSpellDefinition(tag));
        });
        clone.calculateCompositionAndCapabilities();
        clone.setHat(this.getHat());
        clone.setBanner(this.getBanner());
        return clone;
    }

    @Override
    public void ReadNBT(CompoundTag compound) {
        if (!compound.contains(KEY_NBT)) {
            return;
        }
        this.partsList.clear();
        CompoundTag constructData = compound.getCompound(KEY_NBT);
        for (ConstructSlot slot : ConstructSlot.values()) {
            Item item;
            ResourceLocation rLoc;
            if (!constructData.contains(slot.toString()) || (rLoc = ResourceLocation.parse(constructData.getString(slot.toString()))).getPath().isEmpty() || (item = (Item)BuiltInRegistries.ITEM.get(rLoc)) == null || !(item instanceof ItemConstructPart)) continue;
            this.partsList.put(((ItemConstructPart)item).getSlot(), (ItemConstructPart)item);
        }
        if (constructData.contains(KEY_AFFINITY)) {
            CompoundTag affinity = constructData.getCompound(KEY_AFFINITY);
            for (Affinity aff : Affinity.values()) {
                if (!affinity.contains(aff.toString())) continue;
                this.affinityData.put(aff, affinity.getInt(aff.toString()));
            }
        }
        if (constructData.contains(KEY_SPELLS)) {
            ListTag spells = constructData.getList(KEY_SPELLS, 10);
            this.castableSpells.clear();
            spells.forEach(tag -> {
                if (((CompoundTag)tag).contains("slot")) {
                    SpellRecipe recipe = SpellRecipe.fromNBT((CompoundTag)tag);
                    ConstructSlot slot = ConstructSlot.values()[((CompoundTag)tag).getInt("slot")];
                    if (recipe.isValid()) {
                        this.castableSpells.put(slot, recipe);
                    }
                }
            });
        }
        if (constructData.contains(KEY_HAT)) {
            this.setHat(ItemStack.parseOptional(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess(), (CompoundTag)constructData.getCompound(KEY_HAT)));
        }
        if (constructData.contains(KEY_BANNER)) {
            this.setBanner(ItemStack.parseOptional(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess(), (CompoundTag)constructData.getCompound(KEY_BANNER)));
        }
        this.calculateCompositionAndCapabilities();
    }

    @Override
    public void WriteNBT(CompoundTag compound) {
        CompoundTag data = new CompoundTag();
        if (this.partsList != null) {
            this.partsList.forEach((k, v) -> data.putString(k.toString(), v == null ? "" : BuiltInRegistries.ITEM.getKey(v).toString()));
        }
        CompoundTag affinity = new CompoundTag();
        if (this.affinityData != null) {
            for (Affinity aff : this.affinityData.keySet()) {
                affinity.putInt(aff.toString(), this.affinityData.get(aff).intValue());
            }
        }
        data.put(KEY_AFFINITY, (Tag)affinity);
        ListTag spells = new ListTag();
        this.castableSpells.entrySet().forEach(e -> {
            CompoundTag tag = new CompoundTag();
            ((ISpellDefinition)e.getValue()).writeToNBT(tag);
            tag.putInt("slot", ((ConstructSlot)((e.getKey()))).ordinal());
            spells.add(tag);
        });
        data.put(KEY_SPELLS, (Tag)spells);
        if (!this.hat.isEmpty()) {
            data.put(KEY_HAT, (Tag)MNAItemData.save(this.hat, new CompoundTag()));
        }
        if (!this.banner.isEmpty()) {
            data.put(KEY_BANNER, (Tag)MNAItemData.save(this.banner, new CompoundTag()));
        }
        compound.put(KEY_NBT, (Tag)data);
    }

    @Override
    @Nullable
    public ConstructMaterial getLowestMaterialCooldownMultiplierForCapability(ConstructCapability capability) {
        List possibleParts = this.partsList.values().stream().filter(p -> this.arrayContains(p.getEnabledCapabilities(), capability)).collect(Collectors.toList());
        ConstructMaterial highest = ConstructMaterial.UNKNOWN;
        for (ItemConstructPart p2 : (java.util.List<ItemConstructPart>)possibleParts) {
            if (!(p2.getMaterial().getCooldownMultiplierFor(capability) < highest.getCooldownMultiplierFor(capability))) continue;
            highest = p2.getMaterial();
        }
        return highest == ConstructMaterial.UNKNOWN ? null : highest;
    }

    @Override
    public ConstructMaterial getSlotMaterial(ConstructSlot head) {
        ItemConstructPart part = this.getPart(head).orElse(null);
        if (part == null) {
            return ConstructMaterial.WOOD;
        }
        return part.getMaterial();
    }

    @Override
    public ConstructMaterial getHighestMatieralForCapability(ConstructCapability cap) {
        ArrayList mats = new ArrayList();
        this.partsList.values().forEach(part -> {
            if (part != null) {
                for (ConstructCapability pCap : part.getEnabledCapabilities()) {
                    if (pCap != cap) continue;
                    mats.add(part.getMaterial());
                }
            }
        });
        if (mats.size() == 0) {
            return ConstructMaterial.UNKNOWN;
        }
        java.util.List<net.minecraft.world.item.Tier> sortedTiers = MNAtierSorting.getSortedTiers();
        Collections.sort(mats, (a, b) -> Integer.compare(sortedTiers.indexOf(a.getEquivalentTier()), sortedTiers.indexOf(b.getEquivalentTier())));
        return (ConstructMaterial)mats.get(0);
    }

    private <T> boolean arrayContains(T[] arr, T search) {
        for (T elem : arr) {
            if (!elem.equals(search)) continue;
            return true;
        }
        return false;
    }

    public void copyFrom(ConstructConstruction construct) {
        for (ItemConstructPart part : construct.partsList.values()) {
            this.setPart(new ItemStack((ItemLike)part), false);
        }
        this.castableSpells.clear();
        construct.castableSpells.forEach((k, v) -> {
            CompoundTag tag = new CompoundTag();
            v.writeToNBT(tag);
            this.castableSpells.put((ConstructSlot)(k), ManaAndArtificeMod.getSpellHelper().parseSpellDefinition(tag));
        });
        this.calculateCompositionAndCapabilities();
        this.setHat(construct.getHat());
        this.setBanner(construct.getBanner());
    }

    @Override
    public ISpellDefinition[] getCastableSpells() {
        ISpellDefinition[] output = new ISpellDefinition[this.castableSpells.size()];
        int idx = 0;
        for (ConstructSlot slot : ConstructSlot.values()) {
            if (!this.castableSpells.containsKey(slot)) continue;
            output[idx++] = this.castableSpells.get(slot);
        }
        return output;
    }

    @Override
    @Nullable
    public ISpellDefinition getCastableSpell(ConstructSlot slot) {
        return this.castableSpells.getOrDefault(slot, null);
    }

    @Override
    public int getMaximumTier() {
        int numParts = this.partsList.values().size();
        if (numParts == 0) {
            return 0;
        }
        float totalTier = 0.0f;
        for (ItemConstructPart part : this.partsList.values()) {
            totalTier += (float)part.getMaterial().getCastingTierEquivalent();
        }
        return (int)Math.floor(totalTier / (float)numParts);
    }
}


