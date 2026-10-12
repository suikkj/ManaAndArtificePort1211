/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.bus.api.Event
 */
package com.mna.blocks.tileentities.wizard_lab;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.events.GenericProgressionEvent;
import com.mna.api.events.ProgressionEventIDs;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.items.ItemInit;
import com.mna.spells.crafting.SpellRecipe;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

public class AffinityTinkerTile
extends WizardLabTile
implements IEldrinConsumerTile {
    public static final int SLOT_SPELL = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int INVENTORY_SIZE = 2;
    private float ticksRequired = 100.0f;
    private float powerPerTick = 0.25f;
    private float powerAccumulation = 0.0f;
    private Affinity selectedAffinity = Affinity.UNKNOWN;
    private PowerStatus powerConsumeStatus = PowerStatus.NO_CONDUIT;

    public AffinityTinkerTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public AffinityTinkerTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.AFFINITY_TINKER.get(), pos, state, 2);
    }

    @Override
    public boolean canActivate(Player player) {
        if (this.selectedAffinity == Affinity.UNKNOWN) {
            return false;
        }
        return this.canContinue();
    }

    @Override
    protected boolean canContinue() {
        return this.selectedAffinity != null && this.selectedAffinity != Affinity.UNKNOWN && this.hasStack(0) && !this.hasStack(1);
    }

    @Override
    public float getPctComplete() {
        return (float)this.getActiveTicks() / this.ticksRequired;
    }

    @Override
    protected List<Integer> getSyncedInventorySlots() {
        return Arrays.asList(0, 1);
    }

    @Override
    protected boolean canActiveTick() {
        float amount = this.consume(this.getCrafter(), this.getBlockPos(), Vec3.atCenterOf((Vec3i)this.getBlockPos()), this.selectedAffinity.getShiftAffinity(), this.powerPerTick);
        this.powerConsumeStatus = this.calculatePowerStatus(this.powerPerTick, amount);
        this.powerAccumulation += Math.max(amount, 0.0f);
        if (this.powerAccumulation < this.powerPerTick) {
            return false;
        }
        this.powerAccumulation = 0.0f;
        return true;
    }

    @Override
    protected CompoundTag getMeta() {
        CompoundTag tag = new CompoundTag();
        this.writePowerConsumeStatus(this.powerConsumeStatus, tag);
        tag.putFloat("powerAccum", this.powerAccumulation);
        tag.putInt("affinity", this.selectedAffinity.ordinal());
        return tag;
    }

    @Override
    protected void loadMeta(CompoundTag tag) {
        this.powerConsumeStatus = this.readPowerConsumeStatus(tag);
        if (tag.contains("powerAccum")) {
            this.powerAccumulation = tag.getFloat("powerAccum");
        }
        if (tag.contains("affinity")) {
            this.selectedAffinity = Affinity.values()[tag.getInt("affinity")];
        }
    }

    public Affinity getAffinity() {
        return this.selectedAffinity;
    }

    public void setAffinity(Affinity affinity) {
        this.selectedAffinity = affinity;
    }

    @Override
    protected void onComplete() {
        Player crafter;
        ItemStack output = this.getItem(0);
        SpellRecipe recipe = SpellRecipe.fromNBT(output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (!recipe.isValid()) {
            return;
        }
        Affinity original = recipe.getHighestAffinity();
        recipe.setOverrideAffinity(this.selectedAffinity);
        recipe.writeToNBT(output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        this.setItem(0, ItemStack.EMPTY);
        this.setItem(1, output);
        if (this.getCrafter() != null && !this.getLevel().isClientSide() && (crafter = this.getCrafter()) != null) {
            NeoForge.EVENT_BUS.post(new GenericProgressionEvent(crafter, ProgressionEventIDs.SPELL_AFFINITY_TINKERED));
            if (crafter instanceof ServerPlayer) {
                CustomAdvancementTriggers.AFFINITY_TINKER.trigger((ServerPlayer)crafter, recipe, original, this.selectedAffinity);
            }
        }
    }

    @Override
    protected void onDeactivated() {
        this.powerConsumeStatus = PowerStatus.NOT_REQUESTING;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!super.clickMenuButton(player, button)) {
            switch (button) {
                case 1: {
                    this.selectedAffinity = Affinity.ARCANE;
                    return true;
                }
                case 2: {
                    this.selectedAffinity = Affinity.EARTH;
                    return true;
                }
                case 3: {
                    this.selectedAffinity = Affinity.ENDER;
                    return true;
                }
                case 4: {
                    this.selectedAffinity = Affinity.FIRE;
                    return true;
                }
                case 5: {
                    this.selectedAffinity = Affinity.WATER;
                    return true;
                }
                case 6: {
                    this.selectedAffinity = Affinity.WIND;
                    return true;
                }
                case 7: {
                    this.selectedAffinity = Affinity.ICE;
                    return true;
                }
                case 8: {
                    this.selectedAffinity = Affinity.LIGHTNING;
                    return true;
                }
                case 9: {
                    this.selectedAffinity = Affinity.BLOOD;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getXPCost(Player crafter) {
        return 20;
    }

    @Override
    protected void tick() {
        super.tick();
        if (this.getLevel().isClientSide()) {
            this.spawnParticles();
        }
    }

    private void spawnParticles() {
    }

    @Override
    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        HashMap<Affinity, PowerStatus> reqs = new HashMap<Affinity, PowerStatus>();
        if (this.selectedAffinity != null) {
            reqs.put(this.selectedAffinity.getShiftAffinity(), this.isActive() ? this.powerConsumeStatus : PowerStatus.NOT_REQUESTING);
        }
        return reqs;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN || side == Direction.UP) {
            return new int[]{1};
        }
        return new int[]{0};
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return !this.isActive();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (this.isActive()) {
            return false;
        }
        switch (index) {
            case 0: {
                return this.getItem(index).isEmpty() && stack.getItem() == ItemInit.SPELL.get();
            }
        }
        return false;
    }
}


