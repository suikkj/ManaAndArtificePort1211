 
package com.mna.items.armor;

import com.mna.api.items.ITieredItem;
import com.mna.api.tools.RLoc;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.armor.IBrokenArmorReplaceable;
import com.mna.items.armor.ISetItem;
import com.mna.items.base.IManaRepairable;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import com.mna.api.tools.MNADyeableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class DyeableMageArmor
extends ArmorItem
implements MNADyeableItem, ISetItem,
ITieredItem<DyeableMageArmor>,
IBrokenArmorReplaceable<DyeableMageArmor>,
IManaRepairable {
    private static final ResourceLocation mage_armor_set_bonus = RLoc.create("mage_armor_set_bonus");
    private static final String mage_armor_set_bonus_key = "mage_armor_set_bonus";
    private final float manaPerRepairTick;
    private final float repairPerTick;
    private int _tier = -1;

    public DyeableMageArmor(net.minecraft.core.Holder<ArmorMaterial> armorMaterial, ArmorItem.Type slot, Item.Properties properties, float manaPerRepairTick, float repairPerTick) {
        super(armorMaterial, slot, properties.durability(slot.getDurability(5)));
        this.manaPerRepairTick = manaPerRepairTick;
        this.repairPerTick = repairPerTick;
    }

    @Override
    public float manaPerRepairTick() {
        return this.manaPerRepairTick;
    }

    @Override
    public float repairPerTick() {
        return this.repairPerTick;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        IManaRepairable.super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return IBrokenArmorReplaceable.super.damageItem(stack, amount * 3, entity, onBroken);
    }

    public int getColor(ItemStack stack) {
        net.minecraft.world.item.component.DyedItemColor c = stack.get(net.minecraft.core.component.DataComponents.DYED_COLOR);
        return c != null ? c.rgb() : -13495757;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ISetItem.super.addSetTooltip(tooltip);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public ResourceLocation getSetIdentifier() {
        return mage_armor_set_bonus;
    }

    @Override
    public void applySetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
        if (living instanceof Player) {
            MNACap.get((Player)living, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                m.getCastingResource().addModifier(mage_armor_set_bonus_key, 200.0f);
                m.getCastingResource().addRegenerationModifier(mage_armor_set_bonus_key, -0.25f);
            });
        }
    }

    @Override
    public void removeSetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
        if (living instanceof Player) {
            MNACap.get((Player)living, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                m.getCastingResource().removeModifier(mage_armor_set_bonus_key);
                m.getCastingResource().removeRegenerationModifier(mage_armor_set_bonus_key);
            });
        }
    }

    @Override
    public int itemsForSetBonus() {
        return 3;
    }

    @Override
    public int getCachedTier() {
        return this._tier;
    }

    @Override
    public void setCachedTier(int tier) {
        this._tier = tier;
    }
}

