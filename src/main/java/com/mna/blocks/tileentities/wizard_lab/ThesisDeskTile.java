 
package com.mna.blocks.tileentities.wizard_lab;

import com.mna.Registries;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.sound.SFX;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.blocks.artifice.ThesisDeskBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.ISelectSpellComponents;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.ItemTornJournalPage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import com.mna.api.tools.MNACap;

public class ThesisDeskTile
extends WizardLabTile
implements IEldrinConsumerTile,
ISelectSpellComponents {
    public static final int SLOT_PAPER = 0;
    public static final int SLOT_INK = 1;
    public static final int SLOT_BOOK = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int INVENTORY_SIZE = 4;
    private float ticksRequired = 1800.0f;
    private float powerPerTick = 0.4f;
    private float powerAccum = 0.0f;
    private ISpellComponent selectedComponent;
    private PowerStatus powerConsumeStatus = PowerStatus.NOT_REQUESTING;

    public ThesisDeskTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public ThesisDeskTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.THESIS_DESK.get(), pos, state, 4);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (!this.getLevel().isClientSide() && index == 2) {
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(ThesisDeskBlock.BOOK, Boolean.valueOf(!stack.isEmpty())), 2);
        }
        super.setItem(index, stack);
    }

    @Override
    public boolean canActivate(Player player) {
        boolean baseActivate = this.canContinue();
        if (!baseActivate) {
            return false;
        }
        if (!this.hasStack(0) || this.getItem(0).getCount() < this.getPaperCount() || !this.hasStack(1) || this.getItem(1).getMaxDamage() - this.getItem(1).getDamageValue() < this.getInkDamage()) {
            return false;
        }
        java.util.Optional<com.mna.api.capabilities.IPlayerRoteSpells> roteData = MNACap.get(player, PlayerRoteSpellsProvider.ROTE);
        return player.isCreative() || roteData.isPresent() && ((IPlayerRoteSpells)roteData.get()).isRote(this.selectedComponent);
    }

    @Override
    protected void onCraftStart(Player crafter) {
        if (crafter instanceof ServerPlayer) {
            com.mna.api.tools.MNAItemData.hurt(this.getItem(1), this.getInkDamage(), this.level.random);
            if (this.getItem(1).getDamageValue() >= this.getItem(1).getMaxDamage()) {
                this.setItem(1, ItemStack.EMPTY);
            }
            this.getItem(0).shrink(this.getPaperCount());
        }
    }

    private int getInkDamage() {
        if (this.selectedComponent == null) {
            return 0;
        }
        return this.selectedComponent.getTier(this.level) * 5;
    }

    private int getPaperCount() {
        if (this.selectedComponent == null) {
            return 0;
        }
        return this.selectedComponent.getTier(this.level) * 5;
    }

    @Override
    protected CompoundTag getMeta() {
        CompoundTag tag = new CompoundTag();
        if (this.selectedComponent != null) {
            tag.putString("part", this.selectedComponent.getRegistryName().toString());
        }
        this.writePowerConsumeStatus(this.powerConsumeStatus, tag);
        tag.putFloat("accum", this.powerAccum);
        return tag;
    }

    @Override
    protected void loadMeta(CompoundTag tag) {
        this.powerConsumeStatus = this.readPowerConsumeStatus(tag);
        this.selectedComponent = null;
        if (tag.contains("part")) {
            ResourceLocation part = ResourceLocation.parse(tag.getString("part"));
            if (Registries.Shape.containsKey(part)) {
                this.selectedComponent = (ISpellComponent)Registries.Shape.getValue(part);
            } else if (Registries.Modifier.containsKey(part)) {
                this.selectedComponent = (ISpellComponent)Registries.Modifier.getValue(part);
            } else if (Registries.SpellEffect.containsKey(part)) {
                this.selectedComponent = (ISpellComponent)Registries.SpellEffect.getValue(part);
            }
        }
        if (tag.contains("accum")) {
            this.powerAccum = tag.getFloat("accum");
        }
    }

    @Override
    protected boolean canContinue() {
        return this.selectedComponent != null && this.hasStack(2) && !this.hasStack(3);
    }

    @Override
    public float getPctComplete() {
        return (float)this.getActiveTicks() / this.ticksRequired;
    }

    @Override
    protected List<Integer> getSyncedInventorySlots() {
        return Arrays.asList(0, 1, 2);
    }

    public Affinity getSelectedAffinity() {
        return this.selectedComponent instanceof SpellEffect ? ((SpellEffect)this.selectedComponent).getAffinity().getShiftAffinity() : Affinity.ARCANE;
    }

    @Override
    protected boolean canActiveTick() {
        float amount = this.consume(this.getCrafter(), this.getBlockPos(), Vec3.atCenterOf((Vec3i)this.getBlockPos()), this.getSelectedAffinity(), this.powerPerTick);
        this.powerConsumeStatus = this.calculatePowerStatus(this.powerPerTick, amount);
        this.powerAccum += Math.max(amount, 0.0f);
        if (this.powerAccum >= this.powerPerTick) {
            this.powerAccum = 0.0f;
            if (this.getActiveTicks() == 1799 && !this.getLevel().isClientSide()) {
                this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SFX.Gui.CHARCOAL_SCRIBBLE, SoundSource.BLOCKS, 1.0f, (float)(0.95 + Math.random() * (double)0.1f));
            }
            return true;
        }
        return false;
    }

    @Override
    public ISpellComponent getSpellComponent() {
        return this.selectedComponent;
    }

    @Override
    public void setSpellComponent(ISpellComponent component) {
        if (!this.isActive()) {
            this.selectedComponent = component;
        }
    }

    @Override
    protected void onComplete() {
        ItemStack thesis = new ItemStack((ItemLike)ItemInit.SPELL_PART_THESIS.get());
        ((ItemTornJournalPage)ItemInit.SPELL_PART_THESIS.get()).setComponent(thesis, this.selectedComponent);
        this.setItem(3, thesis);
        this.powerConsumeStatus = PowerStatus.NOT_REQUESTING;
    }

    @Override
    protected void onDeactivated() {
        this.powerConsumeStatus = PowerStatus.NOT_REQUESTING;
    }

    @Override
    public int getXPCost(Player crafter) {
        return 100;
    }

    @Override
    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        HashMap<Affinity, PowerStatus> reqs = new HashMap<Affinity, PowerStatus>();
        reqs.put(this.getSelectedAffinity(), this.powerConsumeStatus);
        return reqs;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends WizardLabTile> state) {
        if (this.isActive()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.thesis.quillwrite"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.thesis.quillidle"));
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN || side == Direction.UP) {
            return new int[]{3};
        }
        return new int[]{1, 0};
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
        ItemStack existing = this.getItem(index);
        switch (index) {
            case 1: {
                return stack.getItem() == ItemInit.ARCANIST_INK.get() && existing.isEmpty();
            }
            case 0: {
                return stack.getItem() == ItemInit.VELLUM.get() && (existing.isEmpty() || existing.getCount() < existing.getMaxStackSize());
            }
        }
        return false;
    }
}


