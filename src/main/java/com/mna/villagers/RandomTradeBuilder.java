/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.npc.VillagerTrades$ItemListing
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.trading.MerchantOffer
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.villagers;

import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

public class RandomTradeBuilder {
    protected Function<Random, ItemStack> price = null;
    protected Function<Random, ItemStack> price2 = random -> ItemStack.EMPTY;
    protected Function<Random, ItemStack> forSale = null;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public RandomTradeBuilder(int maxTrades, int xp, float priceMult) {
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }

    public RandomTradeBuilder setPrice(Function<Random, ItemStack> price) {
        this.price = price;
        return this;
    }

    public RandomTradeBuilder setPrice(Item item, int min, int max) {
        return this.setPrice(RandomTradeBuilder.createFunction(item, min, max));
    }

    public RandomTradeBuilder setPrice2(Function<Random, ItemStack> price2) {
        this.price2 = price2;
        return this;
    }

    public RandomTradeBuilder setPrice2(Item item, int min, int max) {
        return this.setPrice2(RandomTradeBuilder.createFunction(item, min, max));
    }

    public RandomTradeBuilder setForSale(Function<Random, ItemStack> forSale) {
        this.forSale = forSale;
        return this;
    }

    public RandomTradeBuilder setForSale(Item item, int min, int max) {
        return this.setForSale(RandomTradeBuilder.createFunction(item, min, max));
    }

    public RandomTradeBuilder setEmeraldPrice(int emeralds) {
        return this.setPrice(random -> new ItemStack((ItemLike)Items.EMERALD, emeralds));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int emeralds, Item item, int amt) {
        this.setEmeraldPrice(emeralds);
        return this.setForSale(random -> new ItemStack((ItemLike)item, amt));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int emeralds, Item item) {
        return this.setEmeraldPriceFor(emeralds, item, 1);
    }

    public RandomTradeBuilder setEmeraldPrice(int min, int max) {
        return this.setPrice(Items.EMERALD, min, max);
    }

    public RandomTradeBuilder setEmeraldPriceFor(int min, int max, Item item, int amt) {
        this.setEmeraldPrice(min, max);
        return this.setForSale(random -> new ItemStack((ItemLike)item, amt));
    }

    public RandomTradeBuilder setEmeraldPriceFor(int min, int max, Item item) {
        return this.setEmeraldPriceFor(min, max, item, 1);
    }

    public boolean canBuild() {
        return this.price != null && this.forSale != null;
    }

    public VillagerTrades.ItemListing build() {
        Random rnd = new Random();
        return (entity, random) -> {
            if (!this.canBuild()) {
                return null;
            }
            // [PORT 1.21.1] MerchantOffer agora usa Optional<ItemCost> para o 2o custo; EMPTY = sem 2o custo.
            // Antes: Optional.of(ItemCost(AIR, 0)) quando price2 e o default vazio -> save do villager
            // crashava ("Item must not be minecraft:air; Value must be positive: 0"). Aplicar cada
            // function UMA vez (o original aplicava 1x; aqui aplicava 2x -> RNG dobrado).
            ItemStack priceStack = this.price.apply(rnd);
            ItemStack price2Stack = this.price2.apply(rnd);
            ItemStack forSaleStack = this.forSale.apply(rnd);
            java.util.Optional<net.minecraft.world.item.trading.ItemCost> second = price2Stack.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(new net.minecraft.world.item.trading.ItemCost(price2Stack.getItem(), price2Stack.getCount()));
            return new MerchantOffer(new net.minecraft.world.item.trading.ItemCost(priceStack.getItem(), priceStack.getCount()), second, forSaleStack, this.maxTrades, this.xp, this.priceMult);
        };
    }

    public static Function<Random, ItemStack> createFunction(Item item, int min, int max) {
        return random -> new ItemStack((ItemLike)item, random.nextInt(max) + min);
    }
}

