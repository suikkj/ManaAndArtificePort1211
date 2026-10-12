 
package com.mna.blocks.tileentities.wizard_lab;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.events.GenericProgressionEvent;
import com.mna.api.events.ProgressionEventIDs;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.artifice.FumeBellowsBlock;
import com.mna.blocks.artifice.FumePrismBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.items.ritual.Mote;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.eldrin.FumeFilterRecipe;
import com.mna.tools.ContainerTools;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import org.apache.commons.lang3.mutable.MutableFloat;
import com.mna.api.tools.MNACap;

public class EldrinFumeTile
extends WizardLabTile {
    public static final float GENERATION_RATE_PER_TICK = 0.02f;
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_MOTE = 1;
    public static final int INVENTORY_SIZE = 2;
    private float fuelBurnTicks = 0.0f;
    private float moteBurnTicks = 0.0f;
    private float moteBurnTicksRemaining = 0.0f;
    private Affinity selectedAffinity = Affinity.UNKNOWN;
    private float fuelBurnMultiplier = 1.0f;
    private float outputMultiplier = 1.0f;
    private boolean prismUpgrade = false;

    public EldrinFumeTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public EldrinFumeTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.ELDRIN_FUME.get(), pos, state, 2);
    }

    public FuelStatus getFuelValues(Level level, ItemStack stack) {
        Affinity affinity;
        float amount = this.moteBurnTicksRemaining;
        Affinity affinity2 = affinity = amount > 0.0f ? this.selectedAffinity : Affinity.UNKNOWN;
        if (stack.isEmpty()) {
            return new FuelStatus(affinity, amount, false);
        }
        Optional<FumeFilterRecipe> recipe = level.getRecipeManager().getAllRecipesFor(RecipeInit.FUME_FILTER_TYPE.get()).stream().filter(r -> r.value().matches(ContainerTools.createTemporaryContainer(stack), level)).map(r -> r.value()).findFirst();
        if (recipe.isEmpty() || amount > 0.0f && recipe.get().getAffinity() == affinity) {
            return new FuelStatus(affinity, amount, false);
        }
        return new FuelStatus(recipe.get().getAffinity(), recipe.get().getTotalGeneration() / 0.02f, true);
    }

    public void onNeighborChanged() {
        this.calculateMultipliers();
    }

    @Override
    public boolean canActivate(Player player) {
        if (this.isActive()) {
            return false;
        }
        FuelStatus fuelValues = this.getFuelValues(this.level, this.getItem(1));
        if (fuelValues.getAffinity() != Affinity.UNKNOWN && fuelValues.getMoteBurnTime() > 0.0f && CommonHooks.getBurnTime((ItemStack)this.getItem(0), null) > 0) {
            IWorldMagic worldMagic = MNACap.get((IWorldMagic)player.level(), WorldMagicProvider.MAGIC).orElse(null);
            if (worldMagic == null) {
                return false;
            }
            IWellspringNodeRegistry wellspringNetwork = worldMagic.getWellspringRegistry();
            if (wellspringNetwork == null) {
                return false;
            }
            HashMap<Affinity, Float> curAmt = wellspringNetwork.getNodeNetworkAmountFor(player.getUUID(), this.level);
            return !(curAmt.getOrDefault(fuelValues.getAffinity(), Float.valueOf(0.0f)).floatValue() >= 5000.0f);
        }
        return false;
    }

    @Override
    protected boolean canContinue() {
        return true;
    }

    @Override
    public float getPctComplete() {
        return this.getFuelPctRemaining();
    }

    @Override
    protected boolean canActiveTick() {
        return true;
    }

    @Override
    protected void tickActiveLogic() {
        if (this.moteBurnTicksRemaining > 0.0f) {
            FuelStatus values;
            this.injectPower();
            this.moteBurnTicksRemaining -= 1.0f;
            if (this.moteBurnTicksRemaining < 1.0f && this.itemsPresent() && (values = this.getFuelValues(this.level, this.getItem(1))).isConsumeMote()) {
                this.reactivate(false);
            }
        }
    }

    @Override
    protected void onCraftStart(Player crafter) {
        FuelStatus values;
        ItemStack burnableStack;
        this.calculateMultipliers();
        if (this.getActiveTicks() <= 0) {
            ItemStack potentialFuel = this.getItem(0);
            int fuelBurnTicks = CommonHooks.getBurnTime((ItemStack)potentialFuel, null);
            if (fuelBurnTicks == 0) {
                this.setInactive();
                return;
            }
            this.fuelBurnTicks = (float)fuelBurnTicks / this.fuelBurnMultiplier;
            if (potentialFuel.hasCraftingRemainingItem()) {
                this.setItem(0, potentialFuel.getCraftingRemainingItem());
            } else {
                potentialFuel.shrink(1);
            }
        }
        if (!(burnableStack = this.getItem(1)).isEmpty() && (values = this.getFuelValues(this.level, burnableStack)).isConsumeMote()) {
            this.selectedAffinity = values.getAffinity();
            this.moteBurnTicksRemaining = this.moteBurnTicks = values.getMoteBurnTime() / this.fuelBurnMultiplier;
            burnableStack.shrink(1);
        }
        if (crafter != null && !crafter.level().isClientSide()) {
            NeoForge.EVENT_BUS.post((Event)new GenericProgressionEvent(crafter, ProgressionEventIDs.ELDRIN_FUME_LIT));
            if (crafter instanceof ServerPlayer) {
                CustomAdvancementTriggers.LIGHT_FUME.trigger((ServerPlayer)crafter, this.selectedAffinity);
            }
        }
    }

    private void calculateMultipliers() {
        Direction dir = (Direction)this.getBlockState().getValue((Property)HorizontalDirectionalBlock.FACING);
        Direction left = dir.getCounterClockWise();
        Direction right = dir.getClockWise();
        BlockState bsLeft = this.level.getBlockState(this.getBlockPos().offset(left.getNormal()));
        BlockState bsRight = this.level.getBlockState(this.getBlockPos().offset(right.getNormal()));
        BlockState bsAbove = this.level.getBlockState(this.getBlockPos().above(2));
        this.prismUpgrade = bsAbove.getBlock() instanceof FumePrismBlock;
        int numBellows = 0;
        if (bsLeft.getBlock() instanceof FumeBellowsBlock && bsLeft.getValue((Property)HorizontalDirectionalBlock.FACING) == dir && ((Boolean)bsLeft.getValue((Property)FumeBellowsBlock.FUME_RIGHT)).booleanValue()) {
            ++numBellows;
        }
        if (bsRight.getBlock() instanceof FumeBellowsBlock && bsRight.getValue((Property)HorizontalDirectionalBlock.FACING) == dir && ((Boolean)bsRight.getValue((Property)FumeBellowsBlock.FUME_LEFT)).booleanValue()) {
            ++numBellows;
        }
        float prevBurnMultiplier = this.fuelBurnMultiplier;
        if (numBellows == 0) {
            this.fuelBurnMultiplier = 1.0f;
            this.outputMultiplier = 1.0f;
        } else if (numBellows == 1) {
            this.fuelBurnMultiplier = 2.0f;
            this.outputMultiplier = 2.0f;
        } else if (numBellows == 2) {
            this.fuelBurnMultiplier = 5.0f;
            this.outputMultiplier = 5.0f;
        }
        if (this.prismUpgrade) {
            this.outputMultiplier /= 3.0f;
        }
        if (prevBurnMultiplier != this.fuelBurnMultiplier && this.isActive()) {
            this.fuelBurnTicks *= prevBurnMultiplier;
            this.fuelBurnTicks /= this.fuelBurnMultiplier;
            this.moteBurnTicks *= prevBurnMultiplier;
            this.moteBurnTicks /= this.fuelBurnMultiplier;
            this.moteBurnTicksRemaining *= prevBurnMultiplier;
            this.moteBurnTicksRemaining /= this.fuelBurnMultiplier;
        }
    }

    @Override
    protected void onComplete() {
        if ((this.hasStack(0) || this.getFuelPctRemaining() > 0.0f) && (this.hasStack(1) || this.getMotePctRemaining() > 0.0f)) {
            Mote mote;
            Item item;
            ItemStack moteStack = this.getItem(1);
            if (!moteStack.isEmpty() && (item = moteStack.getItem()) instanceof Mote && this.selectedAffinity != (mote = (Mote)item).getRelatedAffinity()) {
                return;
            }
            MNACap.get(this.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> {
                Affinity[] affinities = this.getGeneratingAffinities();
                HashMap<Affinity, Float> nodeNetwork = m.getWellspringRegistry().getNodeNetworkAmountFor(this.getCrafterID(), this.level);
                for (Affinity affinity : affinities) {
                    if (!(nodeNetwork.getOrDefault(affinity, Float.valueOf(0.0f)).floatValue() < 5000.0f)) continue;
                    this.reactivate();
                    break;
                }
            });
        }
    }

    private void injectPower() {
        if (!this.getLevel().isClientSide() && this.selectedAffinity != null) {
            MNACap.get(this.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> {
                for (Affinity toGenerate : this.getGeneratingAffinities()) {
                    float amount = this.getPowerPerTick(toGenerate);
                    m.getWellspringRegistry().insertPower(this.getCrafterID(), this.level, toGenerate, amount);
                }
            });
        }
    }

    public float getMotePctRemaining() {
        return this.moteBurnTicksRemaining / this.moteBurnTicks;
    }

    public float getFuelPctRemaining() {
        return (float)this.getActiveTicks() / this.fuelBurnTicks;
    }

    @Override
    protected boolean needsPower() {
        return false;
    }

    public Affinity[] getGeneratingAffinities() {
        if (this.selectedAffinity == Affinity.UNKNOWN) {
            FuelStatus fuelValues = this.getFuelValues(this.level, this.getItem(1));
            this.selectedAffinity = fuelValues.getAffinity();
        }
        if (this.prismUpgrade) {
            return new Affinity[]{this.selectedAffinity, this.selectedAffinity.getAdjacentLeft(), this.selectedAffinity.getAdjacentRight()};
        }
        return new Affinity[]{this.selectedAffinity};
    }

    public float getPowerPerTick(Affinity aff) {
        MutableFloat powerPerTick = new MutableFloat(0.0f);
        MNACap.get(this.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> {
            float amount = 0.02f * this.outputMultiplier;
            float multiplier = m.getWellspringRegistry().getEldrinGenerationMultiplierFor(this.getCrafterID(), this.level, aff);
            powerPerTick.setValue(amount * multiplier);
        });
        return powerPerTick.floatValue();
    }

    private boolean itemsPresent() {
        return this.hasStack(0) && this.hasStack(1);
    }

    @Override
    protected List<Integer> getSyncedInventorySlots() {
        return Arrays.asList(0, 1);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
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
        ItemStack existing;
        if (index == 1) {
            FuelStatus burnableData = this.getFuelValues(this.level, stack);
            if (burnableData.getAffinity() == Affinity.UNKNOWN || burnableData.getMoteBurnTime() == 0.0f) {
                return false;
            }
            if (this.isActive() && burnableData.getAffinity() != this.selectedAffinity) {
                return false;
            }
        } else if (index == 0) {
            if (CommonHooks.getBurnTime((ItemStack)stack, null) == 0) {
                return false;
            }
        } else {
            return false;
        }
        return (existing = this.getItem(index)).isEmpty() || existing.getCount() < existing.getMaxStackSize() && ItemStack.isSameItemSameComponents((ItemStack)stack, (ItemStack)existing);
    }

    @Override
    protected CompoundTag getMeta() {
        CompoundTag meta = new CompoundTag();
        meta.putFloat("burnTime", this.fuelBurnTicks);
        meta.putFloat("moteBurnTime", this.moteBurnTicks);
        meta.putFloat("moteBurnTimeRemaining", this.moteBurnTicksRemaining);
        meta.putInt("affinity", this.selectedAffinity.ordinal());
        meta.putFloat("fuelMultiplier", this.fuelBurnMultiplier);
        meta.putFloat("outputMultiplier", this.outputMultiplier);
        meta.putBoolean("prism", this.prismUpgrade);
        return meta;
    }

    @Override
    protected void loadMeta(CompoundTag tag) {
        if (tag.contains("burnTime")) {
            this.fuelBurnTicks = tag.getFloat("burnTime");
        }
        if (tag.contains("moteBurnTime")) {
            this.moteBurnTicks = tag.getFloat("moteBurnTime");
        }
        if (tag.contains("moteBurnTimeRemaining")) {
            this.moteBurnTicksRemaining = tag.getFloat("moteBurnTimeRemaining");
        }
        if (tag.contains("affinity")) {
            this.selectedAffinity = Affinity.values()[tag.getInt("affinity")];
        }
        this.fuelBurnMultiplier = tag.contains("fuelMultiplier") ? tag.getFloat("fuelMultiplier") : 1.0f;
        this.outputMultiplier = tag.contains("outputMultiplier") ? tag.getFloat("outputMultiplier") : 1.0f;
        this.prismUpgrade = tag.contains("prism") && tag.getBoolean("prism");
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
            int[] color = this.selectedAffinity.getColor();
            float colorMultiplier = 0.25f;
            float secondaryColorMultiplier = 0.075f;
            Vec3 particlePos = Vec3.upFromBottomCenterOf((Vec3i)this.getBlockPos(), (double)1.2);
            this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setColor((int)((float)color[0] * colorMultiplier), (int)((float)color[1] * colorMultiplier), (int)((float)color[2] * colorMultiplier)), particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
            float particleVelocity = 0.01f * this.outputMultiplier;
            if (this.prismUpgrade) {
                particleVelocity *= 3.0f;
            }
            this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setColor((int)((float)color[0] * secondaryColorMultiplier), (int)((float)color[1] * secondaryColorMultiplier), (int)((float)color[2] * secondaryColorMultiplier)).setMaxAge(40), particlePos.x, particlePos.y, particlePos.z, 0.0, (double)particleVelocity, 0.0);
        }
    }

    class FuelStatus {
        private Affinity affinity;
        private float moteBurnTime;
        private boolean consumeMote;

        public FuelStatus(Affinity affinity, float moteBurnTime, boolean consumeMote) {
            this.affinity = affinity;
            this.moteBurnTime = moteBurnTime;
            this.consumeMote = consumeMote;
        }

        public Affinity getAffinity() {
            return this.affinity;
        }

        public float getMoteBurnTime() {
            return this.moteBurnTime;
        }

        public boolean isConsumeMote() {
            return this.consumeMote;
        }
    }
}


