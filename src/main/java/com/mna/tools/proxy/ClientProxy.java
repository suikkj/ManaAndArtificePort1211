/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.Advancement
 *  net.minecraft.advancements.AdvancementProgress
 *  net.minecraft.client.Camera
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ClientAdvancements
 *  net.minecraft.client.multiplayer.ClientAdvancements$Listener
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderers
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.sounds.SoundEventListener
 *  net.minecraft.client.sounds.WeighedSoundEvents
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.tools.proxy;

import com.mna.KeybindInit;
import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.api.sound.Music;
import com.mna.blocks.tileentities.renderers.PylonRenderer;
import com.mna.entities.constructs.animated.Construct;
import com.mna.gui.HUDOverlayRenderer;
import com.mna.gui.constructs.GuiConstructDiagnostics;
import com.mna.guide.GuideBookEntries;
import com.mna.sound.EntityAliveLoopingSound;
import com.mna.sound.PredicateLoopingSound;
import com.mna.tools.DidYouKnowHelper;
import com.mna.tools.ISidedProxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

public class ClientProxy
implements ISidedProxy,
SoundEventListener,
ClientAdvancements.Listener {
    private Minecraft mc = Minecraft.getInstance();
    private long clientTickCounter;
    private Entity queuedRenderViewEntity;
    private Vec3 lastTickPosition;
    private SoundInstance activeDanceMix;
    private boolean initialized = false;
    private Advancement queryAdvancement = null;
    private AdvancementProgress progress = null;
    private HashMap<Long, Integer> wardRenderers = new HashMap();
    private boolean lockWardRenderers = false;
    private Construct __dummyconstruct;

    @Override
    public Player getClientPlayer() {
        return this.mc.player;
    }

    @Override
    public Level getClientWorld() {
        return this.mc.level;
    }

    @Override
    public long getGameTicks() {
        return this.clientTickCounter;
    }

    @Override
    public void incrementTick() {
        if (!this.initialized) {
            this.mc.getSoundManager().addListener((SoundEventListener)this);
            this.initialized = true;
        }
        ++this.clientTickCounter;
        if (this.queuedRenderViewEntity != null) {
            this.mc.cameraEntity = this.queuedRenderViewEntity;
            this.mc.gameRenderer.checkEntityPostEffect(this.queuedRenderViewEntity);
            this.queuedRenderViewEntity = null;
        }
        this.lastTickPosition = this.mc.player != null ? this.mc.player.position() : Vec3.ZERO;
        HashSet toRemove = new HashSet();
        this.wardRenderers.keySet().forEach(l -> {
            int duration = this.wardRenderers.get(l) - 1;
            if (duration <= 0) {
                toRemove.add(l);
            } else {
                this.wardRenderers.put((Long)l, duration);
            }
        });
        this.lockWardRenderers = true;
        toRemove.forEach(l -> this.wardRenderers.remove(l));
        this.lockWardRenderers = false;
    }

    @Override
    public Vec3 getClientLastTickPosition() {
        return this.lastTickPosition;
    }

    @Override
    public void openConstructDiagnostics(Construct target) {
        Minecraft.getInstance().setScreen((Screen)new GuiConstructDiagnostics(target));
    }

    @Override
    public boolean isUIModifierKeyDown() {
        return ((KeyMapping)KeybindInit.InventoryItemOpen.get()).isDown();
    }

    @Override
    public Construct getDummyConstructForRender() {
        if (this.__dummyconstruct == null && this.getClientWorld() != null) {
            this.__dummyconstruct = new Construct(this.getClientWorld());
        }
        return this.__dummyconstruct;
    }

    @Override
    public void resetRenderViewEntity() {
        this.setRenderViewEntity((Entity)this.mc.player);
    }

    @Override
    public void setRenderViewEntity(Entity e) {
        this.queuedRenderViewEntity = e;
    }

    @Override
    public boolean isGamePaused() {
        return Minecraft.getInstance().isPaused();
    }

    @Override
    public void setFlySpeed(Player player, float speed) {
        player.getAbilities().setFlyingSpeed(speed);
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
            } else {
                player.getAbilities().setFlyingSpeed(0.05f);
            }
        }
        if (player instanceof ServerPlayer) {
            ((ServerPlayer)player).onUpdateAbilities();
        }
    }

    @Override
    public boolean checkConstructDanceMixPlaying() {
        if (this.activeDanceMix != null) {
            if (!Minecraft.getInstance().getSoundManager().isActive(this.activeDanceMix)) {
                this.activeDanceMix = null;
                return false;
            }
            return true;
        }
        return false;
    }

    public void onPlaySound(SoundInstance inst, WeighedSoundEvents weight, float range) {
        if (inst.getLocation().equals(Music.Construct.DANCE_MIX.getLocation())) {
            this.activeDanceMix = inst;
        }
    }

    @Override
    public Vec3 getCameraPosition() {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.getPosition();
    }

    @Override
    public void playSingleSound(SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)sound, (float)((float)(0.8 + Math.random() * 0.4))));
    }

    @Override
    public void playLoopingSound(SoundEvent sound, String identifier, Predicate<String> predicate) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new PredicateLoopingSound(sound, "active_loop", predicate));
    }

    @Override
    public void playLoopingSound(SoundEvent sound, String identifier, Predicate<String> predicate, float volume) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new PredicateLoopingSound(sound, "active_loop", predicate).setVolume(volume));
    }

    @Override
    public void playLoopingSound(SoundEvent sound, String identifier, Predicate<String> predicate, float volume, BlockPos position) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new PredicateLoopingSound(sound, "active_loop", predicate).setVolume(volume).setPosition(position));
    }

    @Override
    public void playEntityAliveLoopingSound(SoundEvent sound, Entity entity) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new EntityAliveLoopingSound(sound, entity));
    }

    @Override
    public void showDidYouKnow(Player player, String msg) {
        DidYouKnowHelper.CheckAndShowDidYouKnow(player, msg);
    }

    @Override
    public boolean playerHasAdvancement(Player player, ResourceLocation advancementID) {
        Minecraft mc = Minecraft.getInstance();
        ClientAdvancements advancements = mc.player.connection.getAdvancements();
        this.queryAdvancement = advancements.get(advancementID);
        if (this.queryAdvancement == null) {
            return false;
        }
        this.progress = null;
        advancements.setListener((ClientAdvancements.Listener)this);
        if (this.progress != null) {
            advancements.setListener(null);
            return this.progress.isDone();
        }
        return false;
    }

    @Override
    public void showCodexEntriesUnlocked() {
        HUDOverlayRenderer.instance.showCodexEntriesUnlocked();
    }

    public void onAddAdvancementRoot(Advancement pAdvancement) {
    }

    public void onAddAdvancementTask(Advancement pAdvancement) {
    }

    public void onAdvancementsCleared() {
    }

    public void onRemoveAdvancementRoot(Advancement pAdvancement) {
    }

    public void onRemoveAdvancementTask(Advancement pAdvancement) {
    }

    public void onSelectedTabChanged(Advancement pAdvancement) {
    }

    public void onUpdateAdvancementProgress(Advancement pAdvancement, AdvancementProgress pProgress) {
        if (this.queryAdvancement == pAdvancement) {
            this.progress = pProgress;
        }
    }

    @Override
    public void registerPylonRendererForType(BlockEntityType<? extends PylonTileBase> pylonTile) {
        BlockEntityRenderers.register(pylonTile, ctx -> new PylonRenderer(ctx));
    }

    @Override
    public void recalculateCodexUnlocks() {
        GuideBookEntries.INSTANCE.updateUnlockedEntries(null);
    }

    @Override
    public float getPartialTick() {
        return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
    }

    @Override
    public HashMap<Long, Integer> getWardRenderers() {
        return this.wardRenderers;
    }

    @Override
    public void addWardRenderer(long blockPos, int duration) {
        if (this.lockWardRenderers) {
            return;
        }
        this.wardRenderers.put(blockPos, duration);
    }
}

