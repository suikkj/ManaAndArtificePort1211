/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.npc.VillagerTrades$ItemListing
 *  net.minecraft.world.item.trading.Merchant
 *  net.minecraft.world.item.trading.MerchantOffers
 *  net.neoforged.bus.api.Event
 */
package com.mna.api.events;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.Event;

public class WanderingWizardSelectingTradesEvent
extends Event {
    Merchant wandering_wizard;
    MerchantOffers offers;
    VillagerTrades.ItemListing[] newTrades;
    int maxNumbers;

    public WanderingWizardSelectingTradesEvent(Merchant wandering_wizard, MerchantOffers offers, VillagerTrades.ItemListing[] newTrades, int maxNumbers) {
        this.wandering_wizard = wandering_wizard;
        this.offers = offers;
        this.newTrades = newTrades;
        this.maxNumbers = maxNumbers;
    }

    public Merchant getWanderingWizard() {
        return this.wandering_wizard;
    }

    public MerchantOffers getOffers() {
        return this.offers;
    }

    public VillagerTrades.ItemListing[] getNewTrades() {
        return this.newTrades;
    }

    public int getMaxNumbers() {
        return this.maxNumbers;
    }

    public boolean isCancelable() {
        return false;
    }
}


