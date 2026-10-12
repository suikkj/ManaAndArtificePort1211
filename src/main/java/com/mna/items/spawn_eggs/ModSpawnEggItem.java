/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.dispenser.DefaultDispenseItemBehavior
 *  net.minecraft.core.dispenser.DispenseItemBehavior
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.SpawnEggItem
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.neoforged.neoforge.common.util.Lazy
 *  net.neoforged.neoforge.common.util.NonNullSupplier
 *  net.neoforged.fml.util.ObfuscationReflectionHelper
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.items.spawn_eggs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.NonNullSupplier;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSpawnEggItem
extends SpawnEggItem {
    protected static final List<ModSpawnEggItem> UNADDED_EGGS = new ArrayList<ModSpawnEggItem>();
    private final Lazy<? extends EntityType<?>> entityTypeSupplier;

    public ModSpawnEggItem(NonNullSupplier<? extends EntityType<?>> entityTypeSupplier, int color_a, int color_b, Item.Properties properties) {
        super(null, color_a, color_b, properties);
        this.entityTypeSupplier = Lazy.of(() -> entityTypeSupplier.get());
        UNADDED_EGGS.add(this);
    }

    public ModSpawnEggItem(RegistryObject<? extends EntityType<?>> entityTypeSupplier, int color_a, int color_b, Item.Properties properties) {
        super(null, color_a, color_b, properties);
        this.entityTypeSupplier = Lazy.of(entityTypeSupplier);
        UNADDED_EGGS.add(this);
    }

    public static void initUnaddedEggs() {
        Map EGGS = (Map)ObfuscationReflectionHelper.getPrivateValue(SpawnEggItem.class, null, (String)"BY_ID");
        DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior(){};
        for (SpawnEggItem spawnEggItem : UNADDED_EGGS) {
            EGGS.put(spawnEggItem.getType(null), spawnEggItem);
            DispenserBlock.registerBehavior((ItemLike)spawnEggItem, (DispenseItemBehavior)defaultDispenseItemBehavior);
        }
        UNADDED_EGGS.clear();
    }

    public EntityType<?> getType(@Nullable CompoundTag p_208076_1_) {
        return (EntityType)this.entityTypeSupplier.get();
    }
}


