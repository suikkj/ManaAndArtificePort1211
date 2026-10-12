 
package com.mna.items.artifice;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.TieredItem;
import com.mna.gui.containers.providers.NamedFilterItem;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.base.IItemWithGui;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class Manifest
extends TieredItem
implements IItemWithGui<Manifest> {
    public static final int INVENTORY_SIZE = 24;

    public Manifest() {
        super(new Item.Properties().stacksTo(1));
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        if (IItemWithGui.super.openGuiIfModifierPressed(this.getDefaultInstance(), player, world)) {
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> tooltip, TooltipFlag flags) {
        IItemWithGui.super.appendHoverText(stack, world, tooltip, flags);
        this.addContentsDescription(stack, world.level(), tooltip);
    }

    public void addContentsDescription(ItemStack stack, Level world, List<Component> tooltip) {
        ItemInventoryBase inv = new ItemInventoryBase(stack);
        NonNullList<ItemStack> nonAir = inv.getNonAirItems();
        if (nonAir.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(nonAir.size(), 5); ++i) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(Component.translatable((String)((ItemStack)nonAir.get(i)).getDescriptionId()).getString());
            }
            if (nonAir.size() > 4) {
                sb.append("...");
            }
            tooltip.add((Component)Component.literal((String)sb.toString()).withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add((Component)Component.translatable((String)"item.mna.filter_item.empty").withStyle(ChatFormatting.RED));
        }
        tooltip.add((Component)Component.translatable((String)"item.mna.filter_item.match_durability").append((Component)Component.translatable((String)(this.getMatchDurability(stack) ? "item.mna.filter_item.yes" : "item.mna.filter_item.no")).withStyle(ChatFormatting.GOLD)));
        tooltip.add((Component)Component.translatable((String)"item.mna.filter_item.match_tag").append((Component)Component.translatable((String)(this.getMatchTag(stack) ? "item.mna.filter_item.yes" : "item.mna.filter_item.no")).withStyle(ChatFormatting.GOLD)));
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedFilterItem();
    }

    public void setItems(ItemStack filter, NonNullList<ItemStack> list, boolean matchDurability, boolean matchTag) {
        int i;
        ItemInventoryBase inv = new ItemInventoryBase(filter, 24);
        for (i = 0; i < inv.size(); ++i) {
            inv.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (i = 0; i < Math.min(inv.size(), list.size()); ++i) {
            inv.setStackInSlot(i, (ItemStack)list.get(i));
        }
        inv.writeItemStack();
        this.setMatchDurability(filter, matchDurability);
        this.setMatchTag(filter, matchTag);
    }

    public void setMatchDurability(ItemStack stack, boolean durability) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("match_durability", durability);
    }

    public boolean getMatchDurability(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("match_durability");
    }

    public void setMatchTag(ItemStack stack, boolean durability) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("match_tag", durability);
    }

    public boolean getMatchTag(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("match_tag");
    }
}

