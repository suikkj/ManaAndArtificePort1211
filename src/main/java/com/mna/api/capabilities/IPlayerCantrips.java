/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.crafting.Recipe
 */
package com.mna.api.capabilities;

import com.mna.api.cantrips.ICantrip;
import com.mna.api.capabilities.IPlayerCantrip;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;

public interface IPlayerCantrips {
    public ICantrip matchAndCastCantrip(Player var1, InteractionHand var2, List<Recipe<?>> var3);

    public CompoundTag writeToNBT(boolean var1);

    public void readFromNBT(CompoundTag var1);

    public void setPattern(ResourceLocation var1, List<ResourceLocation> var2);

    public Container getAsInventory();

    public boolean needsSync();

    public void clearSync();

    public void setNeedsSync();

    public Optional<IPlayerCantrip> getCantrip(ResourceLocation var1);

    public List<IPlayerCantrip> getCantrips();
}

