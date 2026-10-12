/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  software.bernie.geckolib.animatable.GeoBlockEntity
 *  software.bernie.geckolib.animatable.GeoAnimatable
 *  software.bernie.geckolib.animatable.instance.AnimatableInstanceCache
 *  software.bernie.geckolib.animation.AnimatableManager$ControllerRegistrar
 *  software.bernie.geckolib.animation.AnimationController
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.PlayState
 *  software.bernie.geckolib.util.GeckoLibUtil
 */
package com.mna.blocks.tileentities.wizard_lab;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IPowerRequestStatus;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.mna.api.tools.MNAItemData;

public abstract class WizardLabTile
extends TileEntityWithInventory
implements GeoBlockEntity,
IPowerRequestStatus {
    private boolean active;
    private int stateFlags;
    private UUID crafterID;
    private Player crafterReference;
    private boolean tickContinuing;
    private int activeTicks;
    private boolean syncToggle;
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public WizardLabTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public static <T extends WizardLabTile> void Tick(Level level, BlockPos pos, BlockState state, T tile) {
        tile.tick();
    }

    protected void tick() {
        if (this.active) {
            if (!this.getLevel().isClientSide()) {
                if (!this.canContinue()) {
                    this.setInactive();
                    return;
                }
                if (this.canActiveTick()) {
                    if (!this.tickContinuing) {
                        this.tickContinuing = true;
                        this.syncAndSave();
                    }
                    this.tickActiveLogic();
                    ++this.activeTicks;
                    if (this.getPctComplete() >= 1.0f) {
                        this.onComplete();
                        if (this.getPctComplete() >= 1.0f) {
                            this.setInactive();
                        }
                    }
                } else if (this.tickContinuing) {
                    this.tickContinuing = false;
                    this.syncAndSave();
                }
                if (this.getLevel().getGameTime() % 20L == 0L) {
                    this.syncAndSave();
                }
            } else if (this.tickContinuing) {
                ++this.activeTicks;
                if (this.getPctComplete() >= 1.0f) {
                    this.onComplete();
                    if (this.getPctComplete() >= 1.0f) {
                        this.setInactive();
                    }
                }
            }
        }
    }

    protected boolean canActiveTick() {
        return true;
    }

    protected abstract boolean canContinue();

    public abstract float getPctComplete();

    protected void onComplete() {
    }

    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        return new HashMap<Affinity, PowerStatus>();
    }

    protected void onCraftStart(Player crafter) {
    }

    public int getXPCost(Player crafter) {
        return 0;
    }

    @Nullable
    public SoundEvent getAmbientSound() {
        return null;
    }

    protected final int getActiveTicks() {
        return this.activeTicks;
    }

    @Nullable
    public final Player getCrafter() {
        if (this.crafterReference == null && this.crafterID != null) {
            this.crafterReference = this.level.getPlayerByUUID(this.crafterID);
        }
        return this.crafterReference;
    }

    @Nullable
    public final UUID getCrafterID() {
        return this.crafterID;
    }

    protected void tickActiveLogic() {
    }

    protected boolean needsPower() {
        return true;
    }

    protected abstract boolean canActivate(Player var1);

    public boolean canStart(Player player) {
        if (this.isActive()) {
            return false;
        }
        if (player.totalExperience < this.getXPCost(player) && !player.isCreative()) {
            return false;
        }
        return this.canActivate(player);
    }

    public final boolean isActive() {
        return this.active;
    }

    public final boolean setActive(Player crafter) {
        if (crafter.getGameProfile() == null || crafter.getGameProfile().getId() == null) {
            return false;
        }
        if (!this.canActivate(crafter)) {
            return false;
        }
        if (!this.getLevel().isClientSide()) {
            this.active = true;
            this.tickContinuing = true;
            this.activeTicks = 0;
            this.crafterReference = crafter;
            this.crafterID = crafter.getGameProfile().getId();
            crafter.giveExperiencePoints(-this.getXPCost(crafter));
            this.onCraftStart(crafter);
            this.syncAndSave();
        }
        return true;
    }

    protected boolean shouldLoopingSoundPlay(String ID) {
        return this.isActive() && this.level.getBlockEntity(this.worldPosition) == this;
    }

    protected final boolean reactivate() {
        return this.reactivate(true);
    }

    protected final boolean reactivate(boolean resetActiveTicks) {
        if (this.crafterID == null) {
            return false;
        }
        this.active = true;
        if (resetActiveTicks) {
            this.activeTicks = 0;
        }
        this.onCraftStart(null);
        this.syncAndSave();
        return true;
    }

    public final void setInactive() {
        this.onDeactivated();
        this.active = false;
        this.activeTicks = 0;
        this.tickContinuing = false;
        this.crafterID = null;
        this.syncAndSave();
    }

    protected void onDeactivated() {
    }

    public final int getStateFlags() {
        return this.stateFlags;
    }

    public final void setStateFlag(int mask) {
        this.stateFlags |= mask;
    }

    public final void clearStateFlag(int mask) {
        this.stateFlags &= ~mask;
    }

    public final boolean isFlagSet(int mask) {
        return (this.stateFlags & mask) != 0;
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    protected abstract List<Integer> getSyncedInventorySlots();

    protected final ListTag saveSyncedInvData() {
        ListTag inventorySync = new ListTag();
        for (Integer i : this.getSyncedInventorySlots()) {
            CompoundTag sub = new CompoundTag();
            MNAItemData.save(this.getItem(i), sub);
            inventorySync.add(sub);
        }
        return inventorySync;
    }

    protected final void loadSyncedInvData(ListTag inventorySync) {
        List<Integer> slots = this.getSyncedInventorySlots();
        if (inventorySync.size() != slots.size()) {
            ManaAndArtifice.LOGGER.error("Failed to parse synced inventory data for tile " + this.getType().toString() + " at " + this.getBlockPos().toShortString());
            return;
        }
        for (int i = 0; i < slots.size(); ++i) {
            int slot = slots.get(i);
            CompoundTag item = inventorySync.getCompound(i);
            this.setItem(slot, ItemStack.parseOptional(this.getLevel().registryAccess(), (CompoundTag)item));
        }
    }

    protected CompoundTag getMeta() {
        return new CompoundTag();
    }

    protected void loadMeta(CompoundTag tag) {
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        base.put("invSync", (Tag)this.saveSyncedInvData());
        base.putBoolean("toggle", this.syncToggle);
        base.putBoolean("active", this.active);
        base.putBoolean("ticking", this.tickContinuing);
        base.putInt("activeTicks", this.activeTicks);
        base.putInt("flags", this.stateFlags);
        if (this.crafterID != null) {
            base.putUUID("crafter", this.crafterID);
        }
        base.put("meta", (Tag)this.getMeta());
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        boolean wasActive = this.active;
        this.loadSyncedInvData(tag.getList("invSync", 10));
        this.active = tag.getBoolean("active");
        this.tickContinuing = tag.getBoolean("ticking");
        this.stateFlags = tag.getInt("flags");
        this.activeTicks = tag.getInt("activeTicks");
        if (tag.contains("crafter")) {
            this.crafterID = tag.getUUID("crafter");
        }
        this.loadMeta(tag.getCompound("meta"));
        if (this.active && !wasActive && this.getAmbientSound() != null && this.shouldLoopingSoundPlay("active_loop")) {
            ManaAndArtifice.instance.proxy.playLoopingSound(this.getAmbientSound(), "active loop", this::shouldLoopingSoundPlay, 1.0f);
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        boolean wasActive = this.active;
        this.loadSyncedInvData(tag.getList("invSync", 10));
        this.active = tag.getBoolean("active");
        this.tickContinuing = tag.getBoolean("ticking");
        this.stateFlags = tag.getInt("flags");
        this.activeTicks = tag.getInt("activeTicks");
        this.loadMeta(tag.getCompound("meta"));
        if (this.active && !wasActive && this.getAmbientSound() != null && this.shouldLoopingSoundPlay("active_loop")) {
            ManaAndArtifice.instance.proxy.playLoopingSound(this.getAmbientSound(), "active loop", this::shouldLoopingSoundPlay, 1.0f, this.worldPosition);
        }
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        if (this.getSyncedInventorySlots().contains(index)) {
            this.syncAndSave();
        }
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack output = super.removeItem(index, count);
        if (this.getSyncedInventorySlots().contains(index)) {
            this.syncAndSave();
        }
        return output;
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.syncAndSave();
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putInt("flags", this.stateFlags);
        compound.putBoolean("active", this.active);
        compound.putBoolean("ticking", this.tickContinuing);
        compound.putInt("activeTicks", this.activeTicks);
        compound.put("meta", (Tag)this.getMeta());
        if (this.crafterID != null) {
            compound.putString("crafter", this.crafterID.toString());
        }
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        this.stateFlags = compound.getInt("flags");
        this.active = compound.getBoolean("active");
        if (compound.contains("ticking")) {
            this.tickContinuing = compound.getBoolean("ticking");
        }
        if (compound.contains("activeTicks")) {
            this.activeTicks = compound.getInt("activeTicks");
        }
        if (compound.contains("meta")) {
            this.loadMeta(compound.getCompound("meta"));
        }
        if (compound.contains("crafter")) {
            try {
                this.crafterID = UUID.fromString(compound.getString("crafter"));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> this.handleAnimState((AnimationState<? extends WizardLabTile>)state))});
    }

    protected PlayState handleAnimState(AnimationState<? extends WizardLabTile> state) {
        return PlayState.STOP;
    }

    public boolean clickMenuButton(Player player, int button) {
        if (button == 0 && this.canActivate(player)) {
            this.setActive(player);
            return true;
        }
        return false;
    }
}

