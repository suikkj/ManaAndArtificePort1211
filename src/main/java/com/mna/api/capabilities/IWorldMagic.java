/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.api.capabilities;

import com.mna.api.capabilities.IRitualTeleportLocation;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.spells.base.ISpellSigil;
import com.mna.capabilities.worlddata.InterdictionData;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IWorldMagic {
    @Nullable
    public IRitualTeleportLocation getRitualTeleportBlockLocation(List<ResourceLocation> var1, ResourceKey<Level> var2);

    @Nullable
    public List<ResourceLocation> getRitualTeleportBlockReagents(BlockPos var1);

    public boolean setRitualTeleportLocation(ServerLevel var1, BlockPos var2, List<ResourceLocation> var3, Direction var4);

    public boolean setRitualTeleportLocation(IRitualTeleportLocation var1);

    public void removeRitualTeleportLocation(BlockPos var1);

    public IRitualTeleportLocation[] getAllTeleportLocations();

    public boolean canPlayerUnlock(BlockPos var1, Player var2);

    public boolean wardBlock(BlockPos var1, Player var2, Collection<ResourceLocation> var3);

    public void addSigil(Player var1, ISpellSigil<?> var2, byte var3);

    public void addUnreferencedSigil(UUID var1, UUID var2);

    public void removeSigil(@Nullable UUID var1, UUID var2);

    public boolean isSigilKnown(UUID var1);

    public boolean wasSigilRemoved(UUID var1, UUID var2);

    public UUID[] getKnownPlayersWithSigils();

    public UUID[] getKnownSigilsForPlayer(UUID var1);

    public List<Long> getAllWardingCandleLocations();

    public void setWardingCandleLocations(List<Long> var1);

    public void addWardingCandleLocation(BlockPos var1);

    public void removeWardingCandleLocation(BlockPos var1);

    public boolean isWithinWardingCandle(BlockPos var1);

    public boolean isInterdicted(LivingEntity var1, Vec3 var2, Vec3 var3);

    public void addInterdictionZone(BlockPos var1, InterdictionData var2);

    public void removeInterdictionZone(BlockPos var1);

    public HashMap<Long, InterdictionData> getInterdictionZones();

    public void setInterdictionZones(HashMap<Long, InterdictionData> var1);

    public IWellspringNodeRegistry getWellspringRegistry();
}

