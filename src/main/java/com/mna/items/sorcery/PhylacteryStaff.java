 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.IPhylacteryItem;
import com.mna.api.tools.MATags;
import com.mna.gui.containers.providers.NamedPhylacteryBook;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.base.IRadialInventorySelect;
import com.mna.items.base.ItemBagBase;
import com.mna.items.filters.ItemFilterGroup;
import com.mna.items.sorcery.CrystalPhylactery;
import com.mna.tools.math.MathUtils;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class PhylacteryStaff
extends ItemBagBase
implements IRadialInventorySelect {
    public static final int CAPACITY = 24;
    public static final int RADIAL_CAPACITY = 12;

    @Override
    public ItemFilterGroup filterGroup() {
        return ItemFilterGroup.ANY_NON_EMPTY_PHYLACTERY;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedPhylacteryBook(stack);
    }

    @Override
    public int getIndex(ItemStack stack) {
        if (stack.getItem() == ItemInit.STAFF_PHYLACTERY.get() && stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains("index")) {
            return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("index");
        }
        return 0;
    }

    @Override
    public void setIndex(ItemStack stack, int index) {
        if (stack.getItem() == ItemInit.STAFF_PHYLACTERY.get()) {
            stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("index", index);
        }
    }

    public static boolean isFilled(ItemStack stack) {
        if (stack.getItem() == ItemInit.CRYSTAL_PHYLACTERY.get()) {
            return CrystalPhylactery.isFilled(stack);
        }
        if (stack.getItem() == ItemInit.STAFF_PHYLACTERY.get()) {
            int index = ((PhylacteryStaff)ItemInit.STAFF_PHYLACTERY.get()).getIndex(stack);
            ItemInventoryBase inv = new ItemInventoryBase(stack);
            ItemStack invStack = inv.getStackInSlot(index);
            return CrystalPhylactery.isFilled(invStack);
        }
        return false;
    }

    @Nullable
    public static EntityType<? extends Mob> getEntityType(ItemStack stack) {
        if (stack.getItem() == ItemInit.CRYSTAL_PHYLACTERY.get()) {
            return CrystalPhylactery.getEntityType(stack);
        }
        if (stack.getItem() == ItemInit.STAFF_PHYLACTERY.get()) {
            int index = ((PhylacteryStaff)ItemInit.STAFF_PHYLACTERY.get()).getIndex(stack);
            ItemInventoryBase inv = new ItemInventoryBase(stack);
            ItemStack invStack = inv.getStackInSlot(index);
            return CrystalPhylactery.getEntityType(invStack);
        }
        return null;
    }

    public static boolean addToPhylactery(ItemStack staffStack, EntityType<? extends Mob> type, float amount, Level world) {
        ItemStack matching;
        if (MATags.isEntityIn(type, MATags.EntityTypes.SUMMON_BLACKLIST)) {
            return false;
        }
        ItemInventoryBase inv = new ItemInventoryBase(staffStack);
        int matching_slot = -1;
        for (int i = 0; i < inv.getSlots(); ++i) {
            float containedAmount;
            EntityType<? extends Mob> containedType;
            ItemStack stack = inv.getStackInSlot(i);
            if (!(stack.getItem() instanceof IPhylacteryItem) || (containedType = ((IPhylacteryItem)stack.getItem()).getContainedEntity(stack)) != type || !((containedAmount = ((IPhylacteryItem)stack.getItem()).getFillPct(stack)) < (float)((IPhylacteryItem)stack.getItem()).getMaximumFill())) continue;
            matching_slot = i;
        }
        if (matching_slot > -1 && (matching = inv.getStackInSlot(matching_slot)).getItem() instanceof IPhylacteryItem) {
            float existing = ((IPhylacteryItem)matching.getItem()).getContainedSouls(matching);
            existing = MathUtils.clamp(existing + amount, 0.0f, (float)((IPhylacteryItem)matching.getItem()).getMaximumFill());
            ((IPhylacteryItem)matching.getItem()).setContainedSouls(matching, existing);
            inv.writeItemStack();
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ItemInventoryBase inv = new ItemInventoryBase(stack);
        int index = ((PhylacteryStaff)ItemInit.STAFF_PHYLACTERY.get()).getIndex(stack);
        ItemStack invStack = inv.getStackInSlot(index);
        if (invStack.getItem() instanceof IPhylacteryItem) {
            ((CrystalPhylactery)ItemInit.CRYSTAL_PHYLACTERY.get()).appendHoverText(invStack, worldIn, tooltip, flagIn);
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        IRadialInventorySelect.super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int capacity() {
        return 12;
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack book = player.getItemInHand(hand);
        player.swing(hand);
        if (!world.isClientSide && hand == InteractionHand.MAIN_HAND) {
            this.openGuiIfModifierPressed(book, player, world);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}

