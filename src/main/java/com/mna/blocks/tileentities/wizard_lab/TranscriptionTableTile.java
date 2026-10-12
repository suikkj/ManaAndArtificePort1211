/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.bus.api.Event
 *  software.bernie.geckolib.animation.AnimationState
 *  software.bernie.geckolib.animation.RawAnimation
 *  software.bernie.geckolib.animation.PlayState
 */
package com.mna.blocks.tileentities.wizard_lab;

import net.minecraft.core.component.DataComponents;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.events.GenericProgressionEvent;
import com.mna.api.events.ProgressionEventIDs;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ICanContainSpell;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.tools.MATags;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.items.ItemInit;
import com.mna.items.artifice.charms.ContingencyCharm;
import com.mna.items.runes.StoneRune;
import com.mna.items.sorcery.BookOfRote;
import com.mna.spells.crafting.SpellRecipe;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class TranscriptionTableTile
extends WizardLabTile
implements IEldrinConsumerTile {
    public static final int SLOT_INK = 0;
    public static final int SLOT_LAPIS = 1;
    public static final int SLOT_BOOK = 2;
    public static final int SLOT_INPUT = 3;
    public static final int INVENTORY_SIZE = 4;
    private static final int LAPIS_REQUIRED_PER_TIER = 10;
    private static final int LAPIS_REQUIRED_PER_TIER_RECHARGE = 2;
    private static final int LAPIS_REQUIRED_PER_TIER_GLYPH = 1;
    private static final int INK_REQUIRED_PER_TIER = 1;
    private float ticksRequired = 100.0f;
    private SpellRecipe copyRecipe;
    private PowerStatus powerConsumeStatus = PowerStatus.NOT_REQUESTING;
    private float powerAccumulation = 0.0f;
    private Affinity requiredAffinity = Affinity.UNKNOWN;

    public TranscriptionTableTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public TranscriptionTableTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.TRANSCRIPTION_TABLE.get(), pos, state, 4);
    }

    @Override
    public boolean canActivate(Player player) {
        ItemStack spellSlotItem = this.getItem(3);
        ItemStack bookStack = this.getItem(2);
        if (bookStack.isEmpty() || !(bookStack.getItem() instanceof ICanContainSpell)) {
            return false;
        }
        ISpellDefinition spell = ((ICanContainSpell)bookStack.getItem()).getSpell(bookStack, player);
        return this.hasStack(3) && this.hasStack(0) && this.getItem(0).getDamageValue() + this.getInkRequired() <= this.getItem(0).getMaxDamage() && this.hasStack(1) && this.getItem(1).getCount() >= this.getLapisRequired(player) && this.hasStack(2) && spellSlotItem.getItem() instanceof ICanContainSpell && ((ICanContainSpell)spellSlotItem.getItem()).canAcceptSpell(spellSlotItem, spell);
    }

    @Override
    protected boolean canContinue() {
        ItemStack spellSlotItem = this.getItem(3);
        ItemStack bookStack = this.getItem(2);
        if (bookStack.isEmpty() || !(bookStack.getItem() instanceof ICanContainSpell)) {
            return false;
        }
        ISpellDefinition spell = ((ICanContainSpell)bookStack.getItem()).getSpell(bookStack, this.getCrafter());
        return this.copyRecipe != null && this.hasStack(3) && this.hasStack(2) && spellSlotItem.getItem() instanceof ICanContainSpell && ((ICanContainSpell)spellSlotItem.getItem()).canAcceptSpell(spellSlotItem, spell);
    }

    @Override
    public float getPctComplete() {
        return (float)this.getActiveTicks() / this.ticksRequired;
    }

    public int getLapisRequired(@Nullable Player player) {
        ISpellDefinition spell;
        if (this.copyRecipe == null || !this.hasStack(3)) {
            return 0;
        }
        ItemStack inputStack = this.getItem(3);
        if (inputStack.getItem() instanceof ContingencyCharm && this.copyRecipe.isSame(spell = ((ICanContainSpell)inputStack.getItem()).getSpell(inputStack, player), false, true, true)) {
            return this.copyRecipe.getTier(this.level) * 2;
        }
        if (MATags.isItemIn(inputStack.getItem(), MATags.Items.STONE_RUNES)) {
            return this.copyRecipe.getTier(this.level) * 1;
        }
        return this.copyRecipe.getTier(this.level) * 10;
    }

    public int getInkRequired() {
        if (this.copyRecipe == null || !this.hasStack(3)) {
            return 0;
        }
        return this.copyRecipe.getTier(this.level) * 1;
    }

    @Override
    protected List<Integer> getSyncedInventorySlots() {
        return Arrays.asList(0, 1, 2, 3);
    }

    @Override
    protected void onCraftStart(Player crafter) {
        ItemStack book = this.getItem(2);
        if (book.isEmpty() || !(book.getItem() instanceof BookOfRote)) {
            this.setInactive();
            return;
        }
        ItemStack input = this.getItem(3);
        if (input.isEmpty() || !(input.getItem() instanceof ICanContainSpell)) {
            this.setInactive();
            return;
        }
        ItemStack lapis = this.getItem(1);
        lapis.shrink(this.getLapisRequired(crafter));
        ItemStack ink = this.getItem(0);
        if (!this.getLevel().isClientSide()) {
            ink.hurt(this.getInkRequired(), this.level.random, (ServerPlayer)crafter);
            if (ink.getDamageValue() >= ink.getMaxDamage()) {
                this.setItem(0, ItemStack.EMPTY);
            }
        }
        if (!this.getLevel().isClientSide()) {
            CompoundTag spellTag = ((BookOfRote)ItemInit.ROTE_BOOK.get()).getSpellCompound(book, crafter);
            this.copyRecipe = SpellRecipe.fromNBT(spellTag);
            if (!this.copyRecipe.isValid()) {
                this.setInactive();
            } else {
                this.requiredAffinity = this.copyRecipe.getHighestAffinity().getShiftAffinity();
            }
        }
    }

    public float getPowerPerTick() {
        return this.getItem(3).getItem() instanceof StoneRune ? 0.2f : 0.75f;
    }

    @Override
    protected boolean canActiveTick() {
        if (this.copyRecipe == null || !this.copyRecipe.isValid()) {
            if (!this.getLevel().isClientSide()) {
                this.setInactive();
            }
            return false;
        }
        float amount = this.consume(this.getCrafter(), this.getBlockPos(), Vec3.atCenterOf((Vec3i)this.getBlockPos()), this.requiredAffinity, this.getPowerPerTick());
        this.powerConsumeStatus = this.calculatePowerStatus(this.getPowerPerTick(), amount);
        this.powerAccumulation += Math.max(amount, 0.0f);
        if (this.powerAccumulation < this.getPowerPerTick()) {
            return false;
        }
        this.powerAccumulation = 0.0f;
        return true;
    }

    @Override
    protected void onComplete() {
        Player crafter;
        ItemStack output = this.getItem(3).copy();
        if (output.isEmpty()) {
            return;
        }
        output = ((ICanContainSpell)output.getItem()).setSpell(output, this.copyRecipe);
        ((ICanContainSpell)output.getItem()).setTranscribedSpell(output);
        if (this.getItem(2).hasCustomHoverName()) {
            output.set(DataComponents.ITEM_NAME, this.getItem(2).getHoverName());
        }
        this.setItem(3, output);
        if (this.getCrafter() != null && !this.getLevel().isClientSide() && (crafter = this.getCrafter()) != null) {
            NeoForge.EVENT_BUS.post((Event)new GenericProgressionEvent(crafter, ProgressionEventIDs.TRANSCRIBE_SPELL));
            if (crafter instanceof ServerPlayer) {
                CustomAdvancementTriggers.TRANSCRIBE_SPELL.trigger((ServerPlayer)crafter, this.copyRecipe, output);
            }
        }
        if (!this.getLevel().isClientSide()) {
            this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SFX.Event.Eldrin.DRAW_IN_ITEM, SoundSource.BLOCKS, 1.0f, (float)(0.95 + Math.random() * (double)0.1f));
        }
    }

    @Override
    protected void onDeactivated() {
        this.powerConsumeStatus = PowerStatus.NOT_REQUESTING;
    }

    @Override
    protected CompoundTag getMeta() {
        CompoundTag tag = new CompoundTag();
        if (this.copyRecipe != null) {
            this.copyRecipe.writeToNBT(tag);
        }
        this.writePowerConsumeStatus(this.powerConsumeStatus, tag);
        tag.putFloat("powerAccum", this.powerAccumulation);
        tag.putInt("affinity", this.requiredAffinity.ordinal());
        return tag;
    }

    @Override
    protected void loadMeta(CompoundTag tag) {
        this.copyRecipe = SpellRecipe.fromNBT(tag);
        this.powerConsumeStatus = this.readPowerConsumeStatus(tag);
        if (tag.contains("powerAccum")) {
            this.powerAccumulation = tag.getFloat("powerAccum");
        }
        if (tag.contains("affinity")) {
            int aff = tag.getInt("affinity");
            this.requiredAffinity = Affinity.values()[aff % Affinity.values().length];
        }
    }

    @Override
    public int getXPCost(Player crafter) {
        return 20;
    }

    @Override
    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        HashMap<Affinity, PowerStatus> reqs = new HashMap<Affinity, PowerStatus>();
        reqs.put(this.requiredAffinity, this.powerConsumeStatus);
        return reqs;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends WizardLabTile> state) {
        if (this.isActive()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.laboratory_transcription_armature.active"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.laboratory_transcription_armature.idle"));
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 3};
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (this.isActive()) {
            return false;
        }
        if (index == 3) {
            return direction == Direction.UP || direction == Direction.DOWN;
        }
        return true;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (this.isActive()) {
            return false;
        }
        ItemStack existing = this.getItem(index);
        switch (index) {
            case 0: {
                return stack.getItem() == ItemInit.ARCANIST_INK.get() && (existing.isEmpty() || existing.getCount() < existing.getMaxStackSize());
            }
            case 1: {
                return stack.getItem() == Items.LAPIS_LAZULI && (existing.isEmpty() || existing.getCount() < existing.getMaxStackSize());
            }
            case 3: {
                ICanContainSpell inter;
                Item item = stack.getItem();
                return item instanceof ICanContainSpell && (inter = (ICanContainSpell)item).canAcceptSpell(stack, this.copyRecipe) && existing.isEmpty();
            }
        }
        return false;
    }
}


