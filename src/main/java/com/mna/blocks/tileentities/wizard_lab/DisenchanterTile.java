/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.EnchantedBookItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.level.ItemLike
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

import com.mna.tools.EnchantmentUtils;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.events.GenericProgressionEvent;
import com.mna.api.events.ProgressionEventIDs;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.tools.MATags;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.items.ItemInit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;

public class DisenchanterTile
extends WizardLabTile
implements IEldrinConsumerTile {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int INVENTORY_SIZE = 2;
    private float ticksRequired = 100.0f;
    private float powerPerTick = 1.0f;
    private PowerStatus fireConsumeStatus = PowerStatus.NOT_REQUESTING;
    private PowerStatus windConsumeStatus = PowerStatus.NOT_REQUESTING;
    private float firePowerAccum = 0.0f;
    private float windPowerAccum = 0.0f;

    public DisenchanterTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public DisenchanterTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.DISENCHANTER.get(), pos, state, 2);
    }

    @Override
    public boolean canActivate(Player player) {
        boolean baseActivate = this.canContinue();
        if (!baseActivate) {
            return false;
        }
        ItemStack input = this.getItem(0);
        if (EnchantmentUtils.enchantmentMap((ItemStack)input).size() == 0) {
            return false;
        }
        ItemStack output = this.getItem(1);
        if (EnchantmentUtils.enchantmentMap((ItemStack)output).size() > 0) {
            return false;
        }
        return !MATags.isItemIn(input.getItem(), MATags.Items.DISENCHANT_BLACKLIST);
    }

    @Override
    protected boolean canContinue() {
        return this.hasStack(0) && this.hasStack(1) && !MATags.isItemIn(this.getItem(0).getItem(), MATags.Items.DISENCHANT_BLACKLIST);
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
        float amount;
        this.fireConsumeStatus = PowerStatus.NOT_REQUESTING;
        this.windConsumeStatus = PowerStatus.NOT_REQUESTING;
        if (this.firePowerAccum < this.powerPerTick) {
            amount = this.consume(this.getCrafter(), this.getBlockPos(), Vec3.atCenterOf((Vec3i)this.getBlockPos()), Affinity.FIRE, this.powerPerTick);
            this.firePowerAccum += Math.max(amount, 0.0f);
            this.fireConsumeStatus = this.calculatePowerStatus(this.powerPerTick, amount);
        } else if (this.fireConsumeStatus != PowerStatus.SUPPLIED) {
            this.fireConsumeStatus = PowerStatus.SUPPLIED;
        }
        if (this.windPowerAccum < this.powerPerTick) {
            amount = this.consume(this.getCrafter(), this.getBlockPos(), Vec3.atCenterOf((Vec3i)this.getBlockPos()), Affinity.WIND, this.powerPerTick);
            this.windPowerAccum += Math.max(amount, 0.0f);
            this.windConsumeStatus = this.calculatePowerStatus(this.powerPerTick, amount);
        } else if (this.windConsumeStatus != PowerStatus.SUPPLIED) {
            this.windConsumeStatus = PowerStatus.SUPPLIED;
        }
        if (!this.fireConsumeStatus.allowMachineOperation() || !this.windConsumeStatus.allowMachineOperation()) {
            return false;
        }
        this.windPowerAccum = 0.0f;
        this.firePowerAccum = 0.0f;
        if (this.getActiveTicks() == 90 && !this.getLevel().isClientSide()) {
            this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SFX.Spell.Buff.ARCANE, SoundSource.BLOCKS, 1.0f, (float)(0.95 + Math.random() * (double)0.1f));
        }
        return true;
    }

    @Override
    protected CompoundTag getMeta() {
        CompoundTag tag = new CompoundTag();
        this.writePowerConsumeStatus(this.fireConsumeStatus, tag, "firePowerStatus");
        tag.putFloat("firePowerAccum", this.firePowerAccum);
        this.writePowerConsumeStatus(this.windConsumeStatus, tag, "windPowerStatus");
        tag.putFloat("windPowerAccum", this.windPowerAccum);
        return tag;
    }

    @Override
    protected void loadMeta(CompoundTag tag) {
        this.fireConsumeStatus = this.readPowerConsumeStatus(tag, "firePowerStatus");
        this.windConsumeStatus = this.readPowerConsumeStatus(tag, "windPowerStatus");
        if (tag.contains("firePowerAccum")) {
            this.firePowerAccum = tag.getFloat("firePowerAccum");
        }
        if (tag.contains("windPowerAccum")) {
            this.windPowerAccum = tag.getFloat("windPowerAccum");
        }
    }

    @Override
    protected void onComplete() {
        Player crafter;
        ItemStack input = this.getItem(0);
        ItemStack output = this.getItem(1);
        java.util.Map<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)input);
        int indexToMove = (int)((double)enchantments.size() * Math.random());
        enchantments.entrySet().stream().skip(indexToMove).findFirst().ifPresent(e -> {
            HashMap<net.minecraft.core.Holder<Enchantment>, Integer> moved = new HashMap<>();
            moved.put(e.getKey(), (Integer)e.getValue());
            net.minecraft.world.item.enchantment.ItemEnchantments.Mutable movedMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
            moved.forEach(movedMut::set);
            ((ItemStack)output).set(DataComponents.ENCHANTMENTS, movedMut.toImmutable());
            enchantments.remove(e.getKey());
        });
        Item item = input.getItem();
        if (item instanceof EnchantedBookItem) {
            EnchantedBookItem enchBook = (EnchantedBookItem)item;
            if (input.has(DataComponents.CUSTOM_DATA)) {
                net.minecraft.world.item.enchantment.ItemEnchantments bookChants = ((ItemStack)input).getOrDefault(DataComponents.STORED_ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                java.util.List<net.minecraft.core.Holder<Enchantment>> bookKeys = new java.util.ArrayList<>(bookChants.keySet());
                if (!bookKeys.isEmpty()) {
                    bookKeys.remove(indexToMove % bookKeys.size());
                    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable bookMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                    bookKeys.forEach(k -> bookMut.set(k, bookChants.getLevel(k)));
                    ((ItemStack)input).set(DataComponents.STORED_ENCHANTMENTS, bookMut.toImmutable());
                    if (bookKeys.isEmpty()) {
                        this.setItem(0, new ItemStack((ItemLike)Items.BOOK));
                    }
                }
            }
        }
        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable inMut = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
        enchantments.forEach(inMut::set);
        ((ItemStack)input).set(DataComponents.ENCHANTMENTS, inMut.toImmutable());
        if (this.getCrafter() != null && !this.getLevel().isClientSide() && (crafter = this.getCrafter()) != null) {
            NeoForge.EVENT_BUS.post(new GenericProgressionEvent(crafter, ProgressionEventIDs.REMOVE_ENCHANTMENT));
            if (crafter instanceof ServerPlayer) {
                CustomAdvancementTriggers.DISENCHANT.trigger((ServerPlayer)crafter, output);
            }
        }
    }

    @Override
    protected void onDeactivated() {
        this.fireConsumeStatus = PowerStatus.NOT_REQUESTING;
        this.windConsumeStatus = PowerStatus.NOT_REQUESTING;
    }

    @Override
    public int getXPCost(Player crafter) {
        return 100;
    }

    @Override
    protected void tick() {
        super.tick();
        if (this.getLevel().isClientSide()) {
            this.spawnParticles();
        }
    }

    private void spawnParticles() {
        if (this.isActive()) {
            int i;
            RandomSource rnd = this.level.random;
            float radius = 1.25f - this.getPctComplete();
            Vec3 pos = Vec3.upFromBottomCenterOf((Vec3i)this.getBlockPos(), (double)2.0);
            Vec3 offset = new Vec3(rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian()).normalize().scale((double)radius);
            this.level.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_LERP.get())), pos.x + offset.x, pos.y + offset.y, pos.z + offset.z, pos.x, pos.y, pos.z);
            int numGlows = (int)(5.0f * this.getPctComplete());
            for (i = 0; i < numGlows; ++i) {
                this.level.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get())), pos.x + offset.x, pos.y + offset.y, pos.z + offset.z, 0.0, 0.0, 0.0);
            }
            if ((double)this.getPctComplete() > 0.95) {
                for (i = 0; i < 150; ++i) {
                    this.level.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), pos.x(), pos.y() - 0.8, pos.z(), -0.5 + Math.random(), 0.01, -0.5 + Math.random());
                }
            }
        }
    }

    @Override
    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        HashMap<Affinity, PowerStatus> reqs = new HashMap<Affinity, PowerStatus>();
        reqs.put(Affinity.FIRE, this.fireConsumeStatus);
        reqs.put(Affinity.WIND, this.windConsumeStatus);
        return reqs;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends WizardLabTile> state) {
        if (this.isActive()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.laboratory_disenchantment_armature.active"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.laboratory_disenchantment_armature.idle"));
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN || side == Direction.UP) {
            return new int[]{1};
        }
        return new int[]{0, 1};
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
                return this.getItem(index).isEmpty() && EnchantmentUtils.enchantmentMap((ItemStack)stack).size() > 0;
            }
            case 1: {
                return this.getItem(index).isEmpty() && stack.getItem() == ItemInit.RUNE_PROJECTION.get();
            }
        }
        return false;
    }
}


