 
package com.mna.blocks.tileentities.wizard_lab;

import com.mna.Registries;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.ISelectSpellComponents;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import com.mna.api.tools.MNACap;

public class SpellSpecializationTile
extends WizardLabTile
implements IEldrinConsumerTile,
ISelectSpellComponents {
    public static final int INVENTORY_SIZE = 0;
    private ISpellComponent selectedPart;
    private float ticksRequired = 3600.0f;
    private float powerPerTick = 0.5f;
    private float powerAccum = 0.0f;
    private PowerStatus powerConsumeStatus = PowerStatus.NOT_REQUESTING;

    public SpellSpecializationTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public SpellSpecializationTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.SPELL_SPECIALIZAITON.get(), pos, state, 0);
    }

    @Override
    public boolean canActivate(Player player) {
        if (this.selectedPart == null) {
            return false;
        }
        java.util.Optional<PlayerRoteSpellsProvider> rote = MNACap.get(player, PlayerRoteSpellsProvider.ROTE);
        if (!rote.isPresent()) {
            return false;
        }
        return !(((IPlayerRoteSpells)rote.get()).getMastery(this.selectedPart) >= 0.5f);
    }

    @Override
    protected boolean canContinue() {
        return this.selectedPart != null;
    }

    @Override
    public float getPctComplete() {
        return (float)this.getActiveTicks() / this.ticksRequired;
    }

    @Override
    protected List<Integer> getSyncedInventorySlots() {
        return Arrays.asList(new Integer[0]);
    }

    @Override
    protected boolean canActiveTick() {
        if (this.getCrafter() == null) {
            return false;
        }
        Affinity aff = this.getSelectedAffinity();
        float amount = this.consume(this.getCrafter(), this.getBlockPos(), Vec3.atCenterOf((Vec3i)this.getBlockPos()), aff, this.powerPerTick);
        this.powerConsumeStatus = this.calculatePowerStatus(this.powerPerTick, amount);
        this.powerAccum += Math.max(amount, 0.0f);
        if (this.powerAccum >= this.powerPerTick) {
            this.powerAccum = 0.0f;
            return true;
        }
        return false;
    }

    public Affinity getSelectedAffinity() {
        return this.selectedPart instanceof SpellEffect ? ((SpellEffect)this.selectedPart).getAffinity().getShiftAffinity() : Affinity.ARCANE;
    }

    @Override
    public ISpellComponent getSpellComponent() {
        return this.selectedPart;
    }

    @Override
    public void setSpellComponent(ISpellComponent part) {
        this.selectedPart = part;
    }

    @Override
    protected CompoundTag getMeta() {
        CompoundTag tag = new CompoundTag();
        if (this.selectedPart != null) {
            tag.putString("part", this.selectedPart.getRegistryName().toString());
        }
        this.writePowerConsumeStatus(this.powerConsumeStatus, tag);
        tag.putFloat("accum", this.powerAccum);
        return tag;
    }

    @Override
    protected void loadMeta(CompoundTag tag) {
        this.powerConsumeStatus = this.readPowerConsumeStatus(tag);
        if (tag.contains("accum")) {
            this.powerAccum = tag.getFloat("accum");
        }
        this.selectedPart = null;
        if (tag.contains("part")) {
            ResourceLocation part = ResourceLocation.parse(tag.getString("part"));
            if (Registries.Shape.containsKey(part)) {
                this.selectedPart = (ISpellComponent)Registries.Shape.getValue(part);
            } else if (Registries.SpellEffect.containsKey(part)) {
                this.selectedPart = (ISpellComponent)Registries.SpellEffect.getValue(part);
            } else if (Registries.Modifier.containsKey(part)) {
                this.selectedPart = (ISpellComponent)Registries.Modifier.getValue(part);
            }
        }
    }

    @Override
    protected void onComplete() {
        this.powerConsumeStatus = PowerStatus.NOT_REQUESTING;
        Player craft = this.getCrafter();
        if (craft == null) {
            return;
        }
        MNACap.get(craft, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
            r.addMastery(craft, this.selectedPart, 0.05f);
            if (!craft.level().isClientSide()) {
                craft.sendSystemMessage((Component)Component.translatable((String)"gui.mna.mastery_increased", (Object[])new Object[]{Component.translatable((String)this.selectedPart.getRegistryName().toString()).getString(), (int)(r.getMastery(this.selectedPart) * 100.0f)}));
            }
        });
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
    protected void tick() {
        super.tick();
        if (this.getLevel().isClientSide()) {
            this.spawnParticles();
        }
    }

    private void spawnParticles() {
        if (this.isActive()) {
            Vec3 particleStart;
            Vec3 particleEnd = Vec3.atCenterOf((Vec3i)this.getBlockPos());
            switch ((Direction)this.getBlockState().getValue((Property)HorizontalDirectionalBlock.FACING)) {
                case EAST: {
                    particleStart = new Vec3(-0.4, 0.75, 0.0);
                    particleEnd = particleEnd.add(0.1, 0.3, 0.0);
                    break;
                }
                case NORTH: {
                    particleStart = new Vec3(0.0, 0.75, 0.4);
                    particleEnd = particleEnd.add(0.0, 0.3, -0.2);
                    break;
                }
                case WEST: {
                    particleStart = new Vec3(0.4, 0.75, 0.0);
                    particleEnd = particleEnd.add(-0.1, 0.3, 0.0);
                    break;
                }
                default: {
                    particleStart = new Vec3(0.0, 0.75, -0.4);
                    particleEnd = particleEnd.add(0.0, 0.3, 0.2);
                }
            }
            this.getLevel().addParticle((ParticleOptions)ParticleTypes.ENCHANT, particleEnd.x, particleEnd.y, particleEnd.z, particleStart.x, particleStart.y, particleStart.z);
        }
    }

    @Override
    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        HashMap<Affinity, PowerStatus> reqs = new HashMap<Affinity, PowerStatus>();
        reqs.put(this.getSelectedAffinity(), this.powerConsumeStatus);
        return reqs;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends WizardLabTile> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.laboratory_mastery_armature.idle"));
    }
}


