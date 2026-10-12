 
package com.mna.entities.constructs.ai;

import com.mna.Registries;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.entities.construct.ai.ConstructBlockAreaTask;
import com.mna.api.tools.MATags;
import com.mna.api.tools.RLoc;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.tools.BlockUtils;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public class ConstructHarvest
extends ConstructBlockAreaTask<ConstructHarvest> {
    public static final ResourceLocation construct_harvestable = RLoc.create("blocks/construct_harvestables");
    private static final ConstructCapability[] requiredCaps = new ConstructCapability[]{ConstructCapability.HARVEST};

    public ConstructHarvest(IConstruct<?> construct, ResourceLocation guiIcon) {
        super(construct, guiIcon);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.currentTarget == null) {
            this.findBlockTarget();
            return;
        }
        if (this.moveToCurrentTarget()) {
            this.harvest();
        }
    }

    private void harvest() {
        AbstractGolem c = this.getConstructAsEntity();
        BlockState state = c.level().getBlockState(this.currentTarget);
        this.pushDiagnosticMessage(this.translate("mna.constructs.feedback.harvesting", this.translate(state), this.currentTarget.getX(), this.currentTarget.getY(), this.currentTarget.getZ()));
        boolean success = false;
        if (this.construct.getIntelligence() >= 16) {
            FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)this.construct.asEntity().level()));
            player.setPos(this.construct.asEntity().position());
            success = this.harvestAndReplant(this.construct.asEntity().level(), this.currentTarget, state, (Player)player);
        } else {
            success = BlockUtils.destroyBlock((LivingEntity)(this.construct.getOwner() != null ? this.construct.getOwner() : c), c.level(), this.currentTarget, true, (Tier)Tiers.NETHERITE);
        }
        this.knownTargets.remove(this.currentTarget);
        this.lastLocation = 0;
        this.currentTarget = null;
        this.exitCode = success ? 0 : 1;
    }

    private boolean harvestAndReplant(Level world, BlockPos pos, BlockState inWorld, Player player) {
        if (!(world instanceof ServerLevel)) {
            return false;
        }
        AbstractGolem c = this.getConstructAsEntity();
        Optional<IntegerProperty> ageProp = inWorld.getProperties().stream().filter(p -> p.getName() == "age").map(p -> (IntegerProperty)p).findFirst();
        if (!ageProp.isPresent() || MATags.isBlockIn(inWorld.getBlock(), MATags.Blocks.CONSTRUCT_HARVESTABLES_NO_AGE)) {
            return BlockUtils.destroyBlock((LivingEntity)(this.construct.getOwner() != null ? this.construct.getOwner() : c), c.level(), this.currentTarget, true, (Tier)Tiers.NETHERITE);
        }
        if (inWorld.getValue((Property)ageProp.get()) != ageProp.get().getPossibleValues().stream().max(Integer::compare).get()) {
            return false;
        }
        Item blockItem = inWorld.getBlock().asItem();
        Block.getDrops((BlockState)inWorld, (ServerLevel)((ServerLevel)world), (BlockPos)pos, (BlockEntity)world.getBlockEntity(pos), (Entity)player, (ItemStack)ItemStack.EMPTY).forEach(stack -> {
            if (stack.getItem() == blockItem) {
                stack.shrink(1);
            }
            if (!stack.isEmpty()) {
                Block.popResource((Level)world, (BlockPos)pos, (ItemStack)stack);
            }
        });
        inWorld.spawnAfterBreak((ServerLevel)world, pos, ItemStack.EMPTY, true);
        if (!world.isClientSide) {
            BlockState newBlock = (BlockState)inWorld.setValue((Property)ageProp.get(), (Comparable)Integer.valueOf(0));
            world.levelEvent(2001, pos, Block.getId((BlockState)newBlock));
            world.setBlockAndUpdate(pos, newBlock);
        }
        return true;
    }

    @Override
    public boolean isValidBlock(BlockState state, BlockPos pos) {
        return ConstructHarvest.validBlock(state);
    }

    public static boolean validBlock(BlockState state) {
        boolean valid;
        if (MATags.isBlockIn(state.getBlock(), MATags.Blocks.CONSTRUCT_HARVESTABLE_EXCLUDE) || state.isAir()) {
            return false;
        }
        boolean no_age = MATags.isBlockIn(state.getBlock(), RLoc.create("construct_harvestables_no_age"));
        boolean bl = valid = no_age || MATags.isBlockIn(state.getBlock(), RLoc.create("construct_harvestables"));
        if (state.getBlock() instanceof CropBlock) {
            valid &= ((CropBlock)state.getBlock()).isMaxAge(state);
        } else {
            Optional<IntegerProperty> ageProp = state.getProperties().stream().filter(p -> p.getName() == "age").map(p -> (IntegerProperty)p).findFirst();
            if (ageProp.isPresent() && !no_age) {
                int maximum = (Integer)ageProp.get().getPossibleValues().stream().max(Integer::compare).get();
                int value = (Integer)state.getValue((Property)ageProp.get());
                valid &= value == maximum;
            }
        }
        return valid;
    }

    @Override
    protected int getInteractTimer() {
        return this.getInteractTime(ConstructCapability.HARVEST);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.interactTimer = this.getInteractTime(ConstructCapability.HARVEST);
    }

    @Override
    public ResourceLocation getType() {
        return Registries.ConstructTasks.getKey(ConstructTasks.HARVEST);
    }

    @Override
    public ConstructHarvest duplicate() {
        return new ConstructHarvest(this.construct, this.guiIcon).copyFrom((ConstructAITask)this);
    }

    @Override
    public ConstructHarvest copyFrom(ConstructAITask<?> other) {
        super.copyFrom((ConstructAITask)other);
        return this;
    }

    @Override
    public ConstructCapability[] requiredCapabilities() {
        return requiredCaps;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
    }

    @Override
    public CompoundTag writeInternal(CompoundTag nbt) {
        return nbt;
    }

    @Override
    protected String getAreaIdentifier() {
        return "harvest.area";
    }
}


