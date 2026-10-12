 
package com.mna.items.artifice;

import com.mna.api.capabilities.CurioItemCapability;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.items.TieredItem;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import java.util.ArrayList;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class ArcaneCrown
extends TieredItem
implements IFactionSpecific {
    public ArcaneCrown() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        DispenserBlock.registerBehavior((ItemLike)this, (DispenseItemBehavior)ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack heldItem = playerIn.getItemInHand(handIn);
        EquipmentSlot slotType = EquipmentSlot.HEAD;
        ItemStack equippedItem = playerIn.getItemBySlot(slotType);
        if (equippedItem.isEmpty()) {
            playerIn.setItemSlot(slotType, heldItem.copy());
            heldItem.setCount(0);
            return InteractionResultHolder.success(heldItem);
        }
        return InteractionResultHolder.fail(heldItem);
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return entity instanceof Player && stack.getItem() == this && armorType == EquipmentSlot.HEAD;
    }

    public void onArmorTick(ItemStack stack, Level world, Player player) {
        this.tickEffect(player, world);
    }

    @Override
    public float getMaxIre() {
        return 5.0E-4f;
    }

    @Override
    public IFaction getFaction() {
        return Factions.COUNCIL;
    }

    private void tickEffect(Player player, Level world) {
        MobEffectInstance dispelExhaustion;
        MobEffectInstance manaBoost;
        if (!(world.isClientSide || (manaBoost = player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_BOOST.get()))) != null && manaBoost.getDuration() >= 10 && manaBoost.getAmplifier() >= 1)) {
            player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_BOOST.get()), 100, 1, false, false, false));
            this.usedByPlayer(player);
        }
        if ((dispelExhaustion = player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.DISPEL_EXHAUSTION.get()))) == null) {
            int effectsRemoved = 0;
            this.usedByPlayer(player);
            ArrayList<net.minecraft.core.Holder<MobEffect>> toRemove = new ArrayList<net.minecraft.core.Holder<MobEffect>>();
            for (MobEffectInstance inst : player.getActiveEffects()) {
                if (inst.getEffect().value().getCategory() != MobEffectCategory.HARMFUL) continue;
                toRemove.add(inst.getEffect());
            }
            for (net.minecraft.core.Holder<MobEffect> e : toRemove) {
                if (!player.removeEffect(e)) continue;
                ++effectsRemoved;
            }
            if (effectsRemoved > 0 && !world.isClientSide) {
                player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.DISPEL_EXHAUSTION.get()), 6000, 0, false, false, false));
            }
        }
    }
}


