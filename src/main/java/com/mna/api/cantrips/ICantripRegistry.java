/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.apache.logging.log4j.util.TriConsumer
 */
package com.mna.api.cantrips;

import com.mna.api.cantrips.ICantrip;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;

public interface ICantripRegistry {
    @Nullable
    public ICantrip registerCantrip(ResourceLocation var1, ResourceLocation var2, int var3, TriConsumer<Player, ICantrip, InteractionHand> var4, ItemStack var5, ResourceLocation ... var6);

    public int countRegisteredCantrips();

    public Optional<ICantrip> getCantrip(ResourceLocation var1);

    public List<ICantrip> getCantrips();

    public List<ICantrip> getCantrips(Player var1);

    @Deprecated(forRemoval=true)
    public List<ICantrip> getCantrips(int var1);
}

