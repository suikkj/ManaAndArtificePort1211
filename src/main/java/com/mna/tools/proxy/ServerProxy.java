/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.Advancement
 *  net.minecraft.advancements.AdvancementProgress
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.PlayerAdvancements
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.tools.proxy;

import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.entities.constructs.animated.Construct;
import com.mna.tools.DidYouKnowHelper;
import com.mna.tools.ISidedProxy;
import java.util.HashMap;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

public class ServerProxy
implements ISidedProxy {
    @Override
    public Level getClientWorld() {
        return null;
    }

    @Override
    public Player getClientPlayer() {
        return null;
    }

    @Override
    public long getGameTicks() {
        return 0L;
    }

    @Override
    public void incrementTick() {
    }

    @Override
    public void openConstructDiagnostics(Construct target) {
    }

    @Override
    public boolean isUIModifierKeyDown() {
        return false;
    }

    @Override
    public Construct getDummyConstructForRender() {
        return null;
    }

    @Override
    public void setRenderViewEntity(Entity e) {
    }

    @Override
    public void resetRenderViewEntity() {
    }

    @Override
    public boolean isGamePaused() {
        return false;
    }

    @Override
    public void setFlySpeed(Player player, float speed) {
    }

    @Override
    public void showCodexEntriesUnlocked() {
    }

    @Override
    public void setFlightEnabled(Player player, boolean enabled) {
        if (player.getAbilities().mayfly == enabled) {
            return;
        }
        if (enabled) {
            player.getAbilities().mayfly = true;
        } else {
            boolean creative;
            player.getAbilities().mayfly = creative = player.isCreative() || player.isSpectator();
            if (!creative) {
                player.getAbilities().flying = false;
            }
        }
        if (player instanceof ServerPlayer) {
            ((ServerPlayer)player).onUpdateAbilities();
        }
    }

    @Override
    public Vec3 getClientLastTickPosition() {
        return Vec3.ZERO;
    }

    @Override
    public boolean checkConstructDanceMixPlaying() {
        return false;
    }

    @Override
    public Vec3 getCameraPosition() {
        return Vec3.ZERO;
    }

    @Override
    public void playSingleSound(SoundEvent sound) {
    }

    @Override
    public void playLoopingSound(SoundEvent sound, String identifier, Predicate<String> predicate) {
    }

    @Override
    public void playLoopingSound(SoundEvent sound, String identifier, Predicate<String> predicate, float volume) {
    }

    @Override
    public void playLoopingSound(SoundEvent sound, String identifier, Predicate<String> predicate, float volume, BlockPos position) {
    }

    @Override
    public void playEntityAliveLoopingSound(SoundEvent sound, Entity entity) {
    }

    @Override
    public void showDidYouKnow(Player player, String msg) {
        DidYouKnowHelper.CheckAndShowDidYouKnow(player, msg);
    }

    @Override
    public boolean playerHasAdvancement(Player player, ResourceLocation advancementID) {
        ServerPlayer sp;
        Advancement advancement;
        if (player instanceof ServerPlayer && (advancement = (sp = (ServerPlayer)player).level().getServer().getAdvancements().getAdvancement(advancementID)) != null) {
            PlayerAdvancements advancements = sp.getAdvancements();
            AdvancementProgress progress = advancements.getOrStartProgress(advancement);
            return progress.isDone();
        }
        return false;
    }

    @Override
    public void registerPylonRendererForType(BlockEntityType<? extends PylonTileBase> pylonTile) {
    }

    @Override
    public void recalculateCodexUnlocks() {
    }

    @Override
    public float getPartialTick() {
        return 0.0f;
    }

    @Override
    public void addWardRenderer(long blockPos, int duration) {
    }

    @Override
    public HashMap<Long, Integer> getWardRenderers() {
        return null;
    }
}

