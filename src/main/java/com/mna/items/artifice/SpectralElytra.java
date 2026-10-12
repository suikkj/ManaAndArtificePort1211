 
package com.mna.items.artifice;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.items.TieredItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.factions.Factions;
import com.mna.items.relic.CrystallizedWind;
import com.mna.tools.EntityUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class SpectralElytra
extends TieredItem
implements IFactionSpecific {
    private static final float MANA_COST_PER_TICK = 2.0f;
    private static final float FLIGHT_SPEED = 1.5f;

    public SpectralElytra() {
        super(new Item.Properties());
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return armorType == EquipmentSlot.CHEST;
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack heldItem = playerIn.getItemInHand(handIn);
        EquipmentSlot slotType = EquipmentSlot.CHEST;
        ItemStack equippedItem = playerIn.getItemBySlot(slotType);
        if (equippedItem.isEmpty()) {
            playerIn.setItemSlot(slotType, heldItem.copy());
            heldItem.setCount(0);
            return InteractionResultHolder.success(heldItem);
        }
        return InteractionResultHolder.fail(heldItem);
    }

    private float getManaCostPerTick(LivingEntity entity) {
        return CrystallizedWind.FindOn(entity) ? 0.0f : 2.0f;
    }

    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return EntityUtil.doElytraFlightTick(stack, entity, flightTicks, this.getManaCostPerTick(entity), 1.5f);
    }

    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return entity instanceof Player && !entity.isInWaterOrBubble() && !entity.isInLava() && MNACap.get((Player)entity, PlayerMagicProvider.MAGIC).isPresent() && (MNACap.get((IPlayerMagic)((Player)entity), PlayerMagicProvider.MAGIC).orElse(null)).getCastingResource().hasEnoughAbsolute(entity, this.getManaCostPerTick(entity));
    }

    @Override
    public IFaction getFaction() {
        return Factions.FEY;
    }
}

