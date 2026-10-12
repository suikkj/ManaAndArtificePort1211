/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package com.mna.api.capabilities.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface ICastingResourceGuiProvider {
    public ResourceLocation getTexture();

    public int getFrameU();

    public int getFrameV();

    public int getFrameWidth();

    public int getFrameHeight();

    public int getBadgeSize();

    default public ItemStack getBadgeItem() {
        return ItemStack.EMPTY;
    }

    default public int getBadgeItemOffsetX() {
        return 14;
    }

    default public int getBadgeItemOffsetY() {
        return 8;
    }

    public int getBarColor();

    public int getBarManaCostEstimateColor();

    public int getResourceNumericTextColor();

    public int getXPBarColor();

    default public int getFillStartX() {
        return 19;
    }

    default public int getFillStartY() {
        return 6;
    }

    default public int getFillWidth() {
        return 128;
    }

    default public int getFillHeight() {
        return this.getFrameHeight() - 12;
    }

    default public int getXPBarOffsetX() {
        return 18;
    }

    default public int getXPBarOffsetY() {
        return 15;
    }

    default public int getXPBarHeight() {
        return 2;
    }

    default public int getResourceNumericOffsetX() {
        return 19;
    }

    default public int getResourceNumericOffsetY() {
        return 8;
    }

    default public int getLevelDisplayX() {
        return 1;
    }

    default public int getLevelDisplayY() {
        return this.getFrameHeight() - 4;
    }

    default public int getLowChargeOffsetY() {
        return -3;
    }
}

