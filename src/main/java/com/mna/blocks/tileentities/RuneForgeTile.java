 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.tools.MATags;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.runeforging.PedestalBlock;
import com.mna.blocks.runeforging.RuneforgeBlock;
import com.mna.blocks.tileentities.PedestalTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.arcanefurnace.ArcaneFurnaceRecipe;
import com.mna.tools.ContainerTools;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import com.mna.api.tools.MNAItemData;

public class RuneForgeTile
extends TileEntityWithInventory
implements IBlockEntityExtension {
    private static final ResourceLocation tag_blacklist = RLoc.create("arcane_furnace_doubling_blacklist");
    private static final int MAX_ITEMS = 64;
    public static final int INVENTORY_SLOT_INDEX = 0;
    int smeltTicks = 0;
    int burnTime = 0;
    private boolean ignoreRecipeCheck = false;
    private boolean isRepairing = false;
    private ItemStack __cachedRecipeOutput;
    private ArcaneFurnaceRecipe __cachedRecipe;

    public RuneForgeTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state, 64);
    }

    public RuneForgeTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.RUNEFORGE.get(), pos, state);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        if (!this.getLevel().isClientSide()) {
            boolean active = false;
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)((BlockState)((BlockState)((BlockState)this.getBlockState().setValue(RuneforgeBlock.ACTIVE, Boolean.valueOf(false))).setValue(RuneforgeBlock.ORE_DOUBLING, Boolean.valueOf(this.hasMultiUpgrade()))).setValue(RuneforgeBlock.REPAIR, Boolean.valueOf(this.hasRepairUpgrade()))).setValue(RuneforgeBlock.SPEED, Boolean.valueOf(this.hasSpeedUpgrade())), 1);
            if (!this.ignoreRecipeCheck) {
                active = this.cacheRecipe();
            }
            if (active) {
                this.smeltTicks = 0;
                this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RuneforgeBlock.ACTIVE, Boolean.valueOf(true)), 3);
            }
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack output = super.removeItem(index, count);
        if (!output.isEmpty()) {
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RuneforgeBlock.ACTIVE, Boolean.valueOf(false)), 3);
        } else {
            this.getLevel().setBlock(this.getBlockPos(), this.getBlockState(), 3);
        }
        return output;
    }

    public void setChanged() {
        if (this.level != null) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
        super.setChanged();
    }

    private boolean cacheRecipe() {
        CraftingContainer inv = ContainerTools.createTemporaryContainer(this.getItem(0));
        ArcaneFurnaceRecipe smeltRecipe = this.getLevel().getRecipeManager().getRecipeFor((RecipeType)RecipeInit.ARCANE_FURNACE_TYPE.get(), (Container)inv, this.level).orElse(null);
        this.__cachedRecipeOutput = null;
        ItemStack ingredient = this.getItem(0);
        if (this.hasRepairUpgrade()) {
            if (!MATags.isItemEqual(ingredient, tag_blacklist)) {
                this.isRepairing = true;
                return true;
            }
            return false;
        }
        boolean ret = false;
        if (smeltRecipe != null) {
            this.__cachedRecipe = smeltRecipe;
            this.__cachedRecipeOutput = smeltRecipe.getResultItem();
            this.__cachedRecipeOutput.setCount(this.__cachedRecipeOutput.getCount() * this.getItem(0).getCount());
            this.burnTime = smeltRecipe.getBurnTime() * this.getItem(0).getCount();
            ret = true;
        } else {
            Optional cook_recipe = this.getLevel().getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, (Container)inv, this.level);
            if (cook_recipe.isPresent()) {
                this.__cachedRecipeOutput = ((CampfireCookingRecipe)cook_recipe.get()).assemble((Container)inv, this.level.registryAccess());
                this.__cachedRecipeOutput.setCount(this.__cachedRecipeOutput.getCount() * this.getItem(0).getCount());
                this.burnTime = ((CampfireCookingRecipe)cook_recipe.get()).getCookingTime() / 2 * this.getItem(0).getCount();
                ret = true;
            } else {
                Optional furnace_recipe = this.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, (Container)inv, this.level);
                if (furnace_recipe.isPresent()) {
                    this.__cachedRecipeOutput = ((SmeltingRecipe)furnace_recipe.get()).assemble((Container)inv, this.level.registryAccess());
                    this.__cachedRecipeOutput.setCount(this.__cachedRecipeOutput.getCount() * this.getItem(0).getCount());
                    this.burnTime = ((SmeltingRecipe)furnace_recipe.get()).getCookingTime() * 2 * this.getItem(0).getCount();
                    ret = true;
                } else {
                    Optional blasting_recipe = this.getLevel().getRecipeManager().getRecipeFor(RecipeType.BLASTING, (Container)inv, this.level);
                    if (blasting_recipe.isPresent()) {
                        this.__cachedRecipeOutput = ((BlastingRecipe)blasting_recipe.get()).assemble((Container)inv, this.level.registryAccess());
                        this.__cachedRecipeOutput.setCount(this.__cachedRecipeOutput.getCount() * this.getItem(0).getCount());
                        this.burnTime = ((BlastingRecipe)blasting_recipe.get()).getCookingTime() * 2 * this.getItem(0).getCount();
                        ret = true;
                    }
                }
            }
        }
        if ((Integer)this.getBlockState().getValue((Property)RuneforgeBlock.MATERIAL) == 1) {
            this.burnTime /= 8;
        }
        if (((Boolean)this.getBlockState().getValue((Property)RuneforgeBlock.SPEED)).booleanValue()) {
            this.burnTime /= 6;
        }
        if (ret && this.burnTime < 1) {
            this.burnTime = 1;
        }
        if (!MATags.isItemEqual(ingredient, tag_blacklist) && ret && this.hasMultiUpgrade()) {
            this.__cachedRecipeOutput.setCount(Math.min(this.__cachedRecipeOutput.getCount() * 2, 64));
        }
        return ret;
    }

    private boolean hasMultiUpgrade() {
        switch ((Direction)this.getBlockState().getValue((Property)RuneforgeBlock.FACING)) {
            case EAST: 
            case WEST: {
                return this.getPedestalUpgradeType(new BlockPos(Direction.NORTH.getStepX(), Direction.NORTH.getStepY(), Direction.NORTH.getStepZ())) == CrystalUpgrade.DOUBLE && this.getPedestalUpgradeType(new BlockPos(Direction.SOUTH.getStepX(), Direction.SOUTH.getStepY(), Direction.SOUTH.getStepZ())) == CrystalUpgrade.DOUBLE;
            }
        }
        return this.getPedestalUpgradeType(new BlockPos(Direction.EAST.getStepX(), Direction.EAST.getStepY(), Direction.EAST.getStepZ())) == CrystalUpgrade.DOUBLE && this.getPedestalUpgradeType(new BlockPos(Direction.WEST.getStepX(), Direction.WEST.getStepY(), Direction.WEST.getStepZ())) == CrystalUpgrade.DOUBLE;
    }

    private boolean hasRepairUpgrade() {
        switch ((Direction)this.getBlockState().getValue((Property)RuneforgeBlock.FACING)) {
            case EAST: 
            case WEST: {
                return this.getPedestalUpgradeType(new BlockPos(Direction.NORTH.getStepX(), Direction.NORTH.getStepY(), Direction.NORTH.getStepZ())) == CrystalUpgrade.REPAIR && this.getPedestalUpgradeType(new BlockPos(Direction.SOUTH.getStepX(), Direction.SOUTH.getStepY(), Direction.SOUTH.getStepZ())) == CrystalUpgrade.REPAIR;
            }
        }
        return this.getPedestalUpgradeType(new BlockPos(Direction.EAST.getStepX(), Direction.EAST.getStepY(), Direction.EAST.getStepZ())) == CrystalUpgrade.REPAIR && this.getPedestalUpgradeType(new BlockPos(Direction.WEST.getStepX(), Direction.WEST.getStepY(), Direction.WEST.getStepZ())) == CrystalUpgrade.REPAIR;
    }

    private boolean hasSpeedUpgrade() {
        switch ((Direction)this.getBlockState().getValue((Property)RuneforgeBlock.FACING)) {
            case EAST: 
            case WEST: {
                return this.getPedestalUpgradeType(new BlockPos(Direction.NORTH.getStepX(), Direction.NORTH.getStepY(), Direction.NORTH.getStepZ())) == CrystalUpgrade.SPEED && this.getPedestalUpgradeType(new BlockPos(Direction.SOUTH.getStepX(), Direction.SOUTH.getStepY(), Direction.SOUTH.getStepZ())) == CrystalUpgrade.SPEED;
            }
        }
        return this.getPedestalUpgradeType(new BlockPos(Direction.EAST.getStepX(), Direction.EAST.getStepY(), Direction.EAST.getStepZ())) == CrystalUpgrade.SPEED && this.getPedestalUpgradeType(new BlockPos(Direction.WEST.getStepX(), Direction.WEST.getStepY(), Direction.WEST.getStepZ())) == CrystalUpgrade.SPEED;
    }

    private CrystalUpgrade getPedestalUpgradeType(BlockPos offset) {
        PedestalTile pedestal;
        ItemStack stack;
        if (this.getLevel().getBlockState(this.worldPosition.offset((Vec3i)offset)).getBlock() instanceof PedestalBlock && !(stack = (pedestal = (PedestalTile)this.level.getBlockEntity(this.worldPosition.offset((Vec3i)offset))).getItem(0)).isEmpty() && stack.getItem() instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)stack.getItem();
            if (blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_ORANGE.get() || blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_RED.get() || blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_YELLOW.get()) {
                return CrystalUpgrade.DOUBLE;
            }
            if (blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_BLUE.get() || blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_CYAN.get() || blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_LIGHT_BLUE.get() || blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_WHITE.get()) {
                return CrystalUpgrade.REPAIR;
            }
            if (blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_PURPLE.get() || blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_MAGENTA.get() || blockItem.getBlock() == BlockInit.CHIMERITE_CRYSTAL_PINK.get()) {
                return CrystalUpgrade.SPEED;
            }
        }
        return CrystalUpgrade.NONE;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack ret = super.removeItemNoUpdate(index);
        if (!this.getLevel().isClientSide()) {
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RuneforgeBlock.ACTIVE, Boolean.valueOf(false)), 3);
            this.smeltTicks = 0;
        }
        return ret;
    }

    @Override
    public int getContainerSize() {
        return 64;
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        CompoundTag sub = new CompoundTag();
        MNAItemData.save(this.getItem(0), sub);
        base.put("invSync", (Tag)sub);
        base.putBoolean("repairing", this.isRepairing);
        base.putInt("burnTime", this.burnTime);
        base.putInt("smeltTicks", this.smeltTicks);
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        CompoundTag sub = tag.getCompound("invSync");
        this.setItem(0, ItemStack.parseOptional(this.getLevel().registryAccess(), sub));
        this.isRepairing = tag.getBoolean("repairing");
        this.burnTime = tag.getInt("burnTime");
        this.smeltTicks = tag.getInt("smeltTicks");
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag data = pkt.getTag();
        CompoundTag sub = data.getCompound("invSync");
        this.setItem(0, ItemStack.parseOptional(this.getLevel().registryAccess(), sub));
        this.isRepairing = data.getBoolean("repairing");
        this.burnTime = data.getInt("burnTime");
        this.smeltTicks = data.getInt("smeltTicks");
    }

    public int getMaxStackSize() {
        return 64;
    }

    public ItemStack getDisplayedItem() {
        return this.getItem(0);
    }

    public boolean MatchesReagent(ResourceLocation rLoc) {
        if (this.getItem(0).isEmpty()) {
            return false;
        }
        return MATags.isItemEqual(this.getItem(0), rLoc);
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putInt("smelt_ticks", this.smeltTicks);
        compound.putInt("burn_ticks", this.burnTime);
        compound.putBoolean("repairing", this.isRepairing);
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        if (compound.contains("smelt_ticks")) {
            this.smeltTicks = compound.getInt("smelt_ticks");
        }
        if (compound.contains("burn_ticks")) {
            this.burnTime = compound.getInt("burn_ticks");
        }
        if (compound.contains("repairing")) {
            this.isRepairing = compound.getBoolean("repairing");
        }
        super.loadAdditional(compound, registries);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, RuneForgeTile tile) {
        if (((Boolean)state.getValue((Property)RuneforgeBlock.ACTIVE)).booleanValue()) {
            if (!level.isClientSide()) {
                if (tile.hasRepairUpgrade()) {
                    tile.tickLogic_repair();
                } else {
                    tile.tickLogic_smelt();
                }
            } else {
                if (tile.hasRepairUpgrade()) {
                    tile.tickLogic_repair();
                } else {
                    ++tile.smeltTicks;
                }
                tile.spawnParticles();
            }
        }
    }

    private void tickLogic_repair() {
        int repair_rate;
        ItemStack stack = this.getItem(0);
        if (stack.isEmpty() || !stack.isRepairable() || !stack.isDamaged() || MATags.isItemIn(stack.getItem(), MATags.Items.REPAIR_BLACKLIST)) {
            this.resetSelf();
        }
        ++this.smeltTicks;
        int n = repair_rate = (Integer)this.getBlockState().getValue((Property)RuneforgeBlock.MATERIAL) == 1 ? 1 : 5;
        if (this.smeltTicks % repair_rate == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    private void tickLogic_smelt() {
        if (this.__cachedRecipeOutput == null && !this.cacheRecipe()) {
            this.resetSelf();
            return;
        }
        ++this.smeltTicks;
        if (this.smeltTicks >= this.burnTime) {
            this.ignoreRecipeCheck = true;
            this.setItem(0, this.__cachedRecipeOutput.copy());
            if (this.__cachedRecipe != null) {
                ArrayList<ItemStack> byproducts = this.__cachedRecipe.rollByproducts(this.level.getRandom());
                for (ItemStack byproduct : byproducts) {
                    InventoryUtilities.DropItemAt(byproduct, Vec3.atCenterOf((Vec3i)this.getBlockPos().above()), this.level, true);
                }
            }
            this.__cachedRecipe = null;
            this.ignoreRecipeCheck = false;
            this.resetSelf();
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RuneforgeBlock.ACTIVE, Boolean.valueOf(false)), 2);
        }
    }

    private void resetSelf() {
        this.smeltTicks = 0;
        this.burnTime = 0;
        this.isRepairing = false;
        if (!this.getLevel().isClientSide()) {
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue(RuneforgeBlock.ACTIVE, Boolean.valueOf(false)), 2);
        }
    }

    private void spawnParticles() {
        BlockState state = this.getBlockState();
        if (((Boolean)state.getValue((Property)RuneforgeBlock.ACTIVE)).booleanValue()) {
            this.spawnActiveParticles(state);
            if (((Boolean)state.getValue((Property)RuneforgeBlock.ORE_DOUBLING)).booleanValue()) {
                this.spawnDoublingParticles(state);
            }
            if (((Boolean)state.getValue((Property)RuneforgeBlock.SPEED)).booleanValue()) {
                this.spawnSpeedParticles(state);
            }
            if (((Boolean)state.getValue((Property)RuneforgeBlock.REPAIR)).booleanValue()) {
                this.spawnRepairParticles(state);
            }
        }
    }

    private void spawnActiveParticles(BlockState state) {
        float xOffset = 0.5f;
        float zOffset = 0.5f;
        float yOffset = 0.95f;
        float spread = 0.15f;
        float upVel = this.isRepairing ? 0.03f : 0.01f;
        MAParticleType particle = new MAParticleType((ParticleType<MAParticleType>)(this.isRepairing ? (ParticleType)ParticleInit.ARCANE.get() : (ParticleType)ParticleInit.FLAME.get()));
        if ((Integer)state.getValue((Property)RuneforgeBlock.MATERIAL) == 0) {
            particle.setColor(20, 40, 60);
        } else {
            particle.setScale(0.06f);
            particle.setMaxAge(8);
            upVel = 0.06f;
            if (Math.random() < (double)0.6f) {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setColor(156, 60, 30).setGravity(0.01f).setMaxAge(20), (double)((float)this.worldPosition.getX() + xOffset + (-spread + this.level.getRandom().nextFloat() * spread * 2.0f)), (double)((float)this.worldPosition.getY() + yOffset), (double)((float)this.worldPosition.getZ() + zOffset + (-spread + this.level.getRandom().nextFloat() * spread * 2.0f)), -0.05 + Math.random() * 0.1, 0.06, -0.05 + Math.random() * 0.1);
            }
        }
        for (int i = 0; i < 2; ++i) {
            this.getLevel().addParticle((ParticleOptions)particle, (double)((float)this.worldPosition.getX() + xOffset + (-spread + this.level.getRandom().nextFloat() * spread * 2.0f)), (double)((float)this.worldPosition.getY() + yOffset), (double)((float)this.worldPosition.getZ() + zOffset + (-spread + this.level.getRandom().nextFloat() * spread * 2.0f)), 0.0, (double)upVel, 0.0);
        }
    }

    private void spawnDoublingParticles(BlockState state) {
        BlockPos offset_1 = null;
        BlockPos offset_2 = null;
        switch ((Direction)this.getBlockState().getValue((Property)RuneforgeBlock.FACING)) {
            case EAST: 
            case WEST: {
                offset_1 = new BlockPos(Direction.NORTH.getStepX(), Direction.NORTH.getStepY(), Direction.NORTH.getStepZ());
                offset_2 = new BlockPos(Direction.SOUTH.getStepX(), Direction.SOUTH.getStepY(), Direction.SOUTH.getStepZ());
                break;
            }
            default: {
                offset_1 = new BlockPos(Direction.EAST.getStepX(), Direction.EAST.getStepY(), Direction.EAST.getStepZ());
                offset_2 = new BlockPos(Direction.WEST.getStepX(), Direction.WEST.getStepY(), Direction.WEST.getStepZ());
            }
        }
        if (offset_1 != null && offset_2 != null) {
            for (int i = 0; i < 5; ++i) {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_ORBIT.get())), (double)((float)(this.worldPosition.getX() + offset_1.getX()) + 0.5f), (double)((float)(this.worldPosition.getY() + offset_1.getY()) + 0.75f), (double)((float)(this.worldPosition.getZ() + offset_1.getZ()) + 0.5f), (double)0.1f, (double)0.02f, (double)0.1f);
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_ORBIT.get())), (double)((float)(this.worldPosition.getX() + offset_2.getX()) + 0.5f), (double)((float)(this.worldPosition.getY() + offset_2.getY()) + 0.75f), (double)((float)(this.worldPosition.getZ() + offset_2.getZ()) + 0.5f), (double)0.1f, (double)0.02f, (double)0.1f);
            }
        }
    }

    private void spawnSpeedParticles(BlockState state) {
        BlockPos offset_1 = null;
        BlockPos offset_2 = null;
        switch ((Direction)this.getBlockState().getValue((Property)RuneforgeBlock.FACING)) {
            case EAST: 
            case WEST: {
                offset_1 = new BlockPos(Direction.NORTH.getStepX(), Direction.NORTH.getStepY(), Direction.NORTH.getStepZ());
                offset_2 = new BlockPos(Direction.SOUTH.getStepX(), Direction.SOUTH.getStepY(), Direction.SOUTH.getStepZ());
                break;
            }
            default: {
                offset_1 = new BlockPos(Direction.EAST.getStepX(), Direction.EAST.getStepY(), Direction.EAST.getStepZ());
                offset_2 = new BlockPos(Direction.WEST.getStepX(), Direction.WEST.getStepY(), Direction.WEST.getStepZ());
            }
        }
        if (offset_1 != null && offset_2 != null) {
            for (int i = 0; i < 5; ++i) {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_ORBIT.get())).setColor(91, 36, 143), (double)((float)(this.worldPosition.getX() + offset_1.getX()) + 0.5f), (double)((float)(this.worldPosition.getY() + offset_1.getY()) + 0.75f), (double)((float)(this.worldPosition.getZ() + offset_1.getZ()) + 0.5f), (double)0.1f, (double)0.02f, (double)0.1f);
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_ORBIT.get())).setColor(91, 36, 143), (double)((float)(this.worldPosition.getX() + offset_2.getX()) + 0.5f), (double)((float)(this.worldPosition.getY() + offset_2.getY()) + 0.75f), (double)((float)(this.worldPosition.getZ() + offset_2.getZ()) + 0.5f), (double)0.1f, (double)0.02f, (double)0.1f);
            }
        }
    }

    private void spawnRepairParticles(BlockState state) {
        BlockPos offset_1 = null;
        BlockPos offset_2 = null;
        switch ((Direction)this.getBlockState().getValue((Property)RuneforgeBlock.FACING)) {
            case EAST: 
            case WEST: {
                offset_1 = new BlockPos(Direction.NORTH.getStepX(), Direction.NORTH.getStepY(), Direction.NORTH.getStepZ());
                offset_2 = new BlockPos(Direction.SOUTH.getStepX(), Direction.SOUTH.getStepY(), Direction.SOUTH.getStepZ());
                break;
            }
            default: {
                offset_1 = new BlockPos(Direction.EAST.getStepX(), Direction.EAST.getStepY(), Direction.EAST.getStepZ());
                offset_2 = new BlockPos(Direction.WEST.getStepX(), Direction.WEST.getStepY(), Direction.WEST.getStepZ());
            }
        }
        if (offset_1 != null && offset_2 != null) {
            for (int i = 0; i < 5; ++i) {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_ORBIT.get())), (double)((float)(this.worldPosition.getX() + offset_1.getX()) + 0.5f), (double)((float)(this.worldPosition.getY() + offset_1.getY()) + 0.75f), (double)((float)(this.worldPosition.getZ() + offset_1.getZ()) + 0.5f), (double)0.1f, (double)0.02f, (double)0.1f);
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_ORBIT.get())), (double)((float)(this.worldPosition.getX() + offset_2.getX()) + 0.5f), (double)((float)(this.worldPosition.getY() + offset_2.getY()) + 0.75f), (double)((float)(this.worldPosition.getZ() + offset_2.getZ()) + 0.5f), (double)0.1f, (double)0.02f, (double)0.1f);
            }
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        if (index == 0) {
            return this.getItem(0).isEmpty();
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        if (!ItemStack.matches((ItemStack)this.getItem(index), (ItemStack)stack) || ((Boolean)this.getBlockState().getValue((Property)RuneforgeBlock.ACTIVE)).booleanValue()) {
            return false;
        }
        if (index == 0) {
            return !this.getItem(0).isEmpty();
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    public float getBurnPct() {
        if (this.burnTime == 0) {
            return 0.0f;
        }
        return MathUtils.clamp01((float)this.smeltTicks / (float)this.burnTime);
    }

    private static enum CrystalUpgrade {
        NONE,
        DOUBLE,
        REPAIR,
        SPEED;

    }
}


