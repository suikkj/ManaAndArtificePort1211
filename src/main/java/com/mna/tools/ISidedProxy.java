/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.tools;

import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.entities.constructs.animated.Construct;
import java.util.HashMap;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

public interface ISidedProxy {
    @Nullable
    public Player getClientPlayer();

    public Vec3 getClientLastTickPosition();

    @Nullable
    public Level getClientWorld();

    @Nullable
    public Construct getDummyConstructForRender();

    public void setRenderViewEntity(Entity var1);

    public void resetRenderViewEntity();

    public long getGameTicks();

    public void incrementTick();

    public HashMap<Long, Integer> getWardRenderers();

    public void addWardRenderer(long var1, int var3);

    public void openConstructDiagnostics(Construct var1);

    public boolean isUIModifierKeyDown();

    public boolean isGamePaused();

    public void setFlySpeed(Player var1, float var2);

    public void setFlightEnabled(Player var1, boolean var2);

    public boolean checkConstructDanceMixPlaying();

    public boolean playerHasAdvancement(Player var1, ResourceLocation var2);

    public Vec3 getCameraPosition();

    public void playSingleSound(SoundEvent var1);

    public void playLoopingSound(SoundEvent var1, String var2, Predicate<String> var3);

    public void playLoopingSound(SoundEvent var1, String var2, Predicate<String> var3, float var4);

    public void playLoopingSound(SoundEvent var1, String var2, Predicate<String> var3, float var4, BlockPos var5);

    public void playEntityAliveLoopingSound(SoundEvent var1, Entity var2);

    public void showDidYouKnow(Player var1, String var2);

    public void showCodexEntriesUnlocked();

    public void registerPylonRendererForType(BlockEntityType<? extends PylonTileBase> var1);

    public void recalculateCodexUnlocks();

    public float getPartialTick();
}

