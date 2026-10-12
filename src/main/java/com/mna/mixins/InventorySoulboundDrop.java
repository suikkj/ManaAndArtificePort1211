 
package com.mna.mixins;

import com.mna.enchantments.framework.EnchantmentInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(value={Inventory.class})
public class InventorySoulboundDrop {
    @Inject(at={@At(value="HEAD")}, method={"dropAll"}, cancellable=false)
    public void saveSoulboundItems(CallbackInfo callbackInfo) {
        Inventory self = (Inventory)this;
        CompoundTag inventory = new CompoundTag();
        CompoundTag curios = new CompoundTag();
        for (int i = 0; i < self.getContainerSize(); ++i) {
            ItemStack stackInSlot = self.getItem(i);
            if (stackInSlot.isEmpty() || com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.SOULBOUND, stackInSlot) <= 0) continue;
            CompoundTag item = new CompoundTag();
            stackInSlot.save(item);
            inventory.put("slot_" + i, (Tag)item);
            self.setItem(i, ItemStack.EMPTY);
        }
        CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity)self.player).ifPresent(p -> p.getCurios().forEach((identifier, stackHandler) -> {
            CompoundTag tagForIdentifier = new CompoundTag();
            for (int i = 0; i < stackHandler.getSlots(); ++i) {
                ItemStack stackInSlot = stackHandler.getStacks().getStackInSlot(i);
                if (stackInSlot.isEmpty() || com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.SOULBOUND, stackInSlot) <= 0) continue;
                CompoundTag item = new CompoundTag();
                stackInSlot.save(item);
                tagForIdentifier.put("slot_" + i, (Tag)item);
                stackHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
            }
            curios.put(identifier, (Tag)tagForIdentifier);
        }));
        self.player.getPersistentData().put("mna_soulbound_inventory", (Tag)inventory);
        self.player.getPersistentData().put("mna_soulbound_curios", (Tag)curios);
    }
}

