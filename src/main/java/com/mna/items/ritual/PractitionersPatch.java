/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.mna.items.ritual;

import com.mna.items.ritual.PractitionersPouchPatches;
import java.util.HashMap;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class PractitionersPatch
extends Item {
    private static final HashMap<String, Supplier<ItemStack>> itemMap = new HashMap();
    private final PractitionersPouchPatches patch;
    private final int level;
    private final boolean countsTowardLimit;

    public PractitionersPatch(PractitionersPouchPatches patch, int level, boolean countsTowardsLimit) {
        super(new Item.Properties());
        this.patch = patch;
        this.level = level;
        this.countsTowardLimit = countsTowardsLimit;
        itemMap.put(patch.name() + level, () -> new ItemStack((ItemLike)this));
    }

    public int getLevel() {
        return this.level;
    }

    public PractitionersPouchPatches getPatch() {
        return this.patch;
    }

    public boolean countsTowardsLimit() {
        return this.countsTowardLimit;
    }

    public static ItemStack getItemFor(PractitionersPouchPatches patch, int level) {
        return itemMap.getOrDefault(patch.name() + Math.min(level, patch.getLevels()), () -> ItemStack.EMPTY).get();
    }
}

