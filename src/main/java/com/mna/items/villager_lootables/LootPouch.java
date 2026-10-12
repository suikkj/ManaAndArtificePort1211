 
package com.mna.items.villager_lootables;

import com.mna.api.tools.MATags;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class LootPouch
extends Item {
    public final ResourceLocation POUCH_ITEMS;
    private final Consumer<ItemStack> randomizer;

    public LootPouch(ResourceLocation itemTag, Consumer<ItemStack> randomizerFunction) {
        super(new Item.Properties());
        this.POUCH_ITEMS = itemTag;
        this.randomizer = randomizerFunction;
    }

    public LootPouch(ResourceLocation itemTag) {
        this(itemTag, null);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.level().isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        this.addStackToPlayer(player, new ItemStack((ItemLike)Items.LEATHER));
        this.addStackToPlayer(player, new ItemStack((ItemLike)Items.STRING));
        int numTypes = 2;
        switch ((int)(Math.random() * 10.0)) {
            case 8: 
            case 9: {
                numTypes = 3;
                break;
            }
            case 6: 
            case 7: {
                numTypes = 1;
            }
        }
        for (int i = 0; i < numTypes; ++i) {
            this.addStackToPlayer(player, this.getRandomLootStack());
        }
        stack.shrink(1);
        return InteractionResultHolder.consume(stack);
    }

    private ItemStack getRandomLootStack() {
        List<Item> items = MATags.getItemTagContents(this.POUCH_ITEMS);
        if (items.size() == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack((ItemLike)items.get((int)(Math.random() * (double)items.size())));
        if (this.randomizer != null) {
            this.randomizer.accept(stack);
        }
        return stack;
    }

    private void addStackToPlayer(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, true);
        }
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(stack, world, list, flag);
        list.add((Component)Component.translatable((String)(this.getDescriptionId() + ".desc")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}

