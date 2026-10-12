/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package com.mna.api.capabilities;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.WellspringNode;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IWellspringNodeRegistry {
    public HashMap<BlockPos, WellspringNode> getNearbyNodes(BlockPos var1, int var2, int var3);

    public boolean addRandomNode(Level var1, BlockPos var2);

    public boolean addNode(Level var1, BlockPos var2, Supplier<WellspringNode> var3, boolean var4);

    default public HashMap<Affinity, Float> getNodeNetworkStrengthFor(Player player) {
        return this.getNodeNetworkStrengthFor(player.getUUID(), player.level());
    }

    public HashMap<Affinity, Float> getNodeNetworkStrengthFor(UUID var1, Level var2);

    default public float getEldrinGenerationMultiplierFor(Player player, Affinity affinity) {
        return this.getEldrinGenerationMultiplierFor(player.getUUID(), player.level(), affinity);
    }

    public float getEldrinGenerationMultiplierFor(UUID var1, Level var2, Affinity var3);

    default public HashMap<Affinity, Float> getNodeNetworkAmountFor(Player player) {
        return this.getNodeNetworkAmountFor(player.getUUID(), player.level());
    }

    public HashMap<Affinity, Float> getNodeNetworkAmountFor(UUID var1, Level var2);

    public float insertPower(UUID var1, Level var2, Affinity var3, float var4);

    public float insertPowerDiminishing(UUID var1, Level var2, Affinity var3, float var4, float var5);

    public void resetDiminishing();

    public float consumePower(UUID var1, Level var2, Affinity var3, float var4);

    public boolean claimNode(ServerLevel var1, UUID var2, ResourceLocation var3, BlockPos var4, Affinity var5);

    public boolean unclaimNode(ServerLevel var1, BlockPos var2);

    public Optional<WellspringNode> getNodeAt(BlockPos var1);

    public void serializeNetworkStrength(Player var1, CompoundTag var2, boolean var3);

    public void deserializeNetworkStrength(Player var1, CompoundTag var2);

    public void writeToNBT(CompoundTag var1);

    public boolean writeToNBT(CompoundTag var1, BlockPos var2, int var3);

    public void readFromNBT(CompoundTag var1);

    public boolean isOverworld();

    public void setOverworld();

    public void setWellspringPower(ServerPlayer var1, Affinity var2, float var3);

    public boolean deleteNodeAt(BlockPos var1);
}

