 
package com.mna.tools;

import com.mna.ManaAndArtifice;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.tools.MATags;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNAtierSorting;

public class BlockUtils {
    private static final int blockBreakLimit = 25;

    public static Pair<Boolean, List<ItemStack>> breakTreeRecursive(Player player, Level world, BlockPos origin, boolean captureDrops) {
        return BlockUtils.breakTreeRecursive(player, world, origin, new ArrayList<Long>(), 0, captureDrops);
    }

    private static Pair<Boolean, List<ItemStack>> breakTreeRecursive(Player player, Level world, BlockPos origin, ArrayList<Long> checkedBlocks, int blockBreakCount, boolean captureDrops) {
        int radius = 1;
        int leafRadius = 2;
        boolean success = true;
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        for (int x = -radius; x <= radius; ++x) {
            for (int y = 0; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos pos = origin.offset((Vec3i)new BlockPos(x, y, z));
                    if (checkedBlocks.contains(pos.asLong())) continue;
                    checkedBlocks.add(pos.asLong());
                    TreeBlockTypes block = BlockUtils.isLogOrLeaf(world.getBlockState(pos));
                    if (block != TreeBlockTypes.LOG) continue;
                    ++blockBreakCount;
                    if (captureDrops) {
                        drops.addAll(BlockUtils.destroyBlockCaptureDrops((LivingEntity)player, world, pos, false, 0, (Tier)MNAtierSorting.getSortedTiers().get(MNAtierSorting.getSortedTiers().size() - 1)));
                    } else if (!BlockUtils.destroyBlock((LivingEntity)player, world, pos, !captureDrops, (Tier)MNAtierSorting.getSortedTiers().get(MNAtierSorting.getSortedTiers().size() - 1))) {
                        return new Pair(false, drops);
                    }
                    for (int lx = -leafRadius; lx <= leafRadius; ++lx) {
                        for (int ly = 0; ly <= leafRadius; ++ly) {
                            for (int lz = -leafRadius; lz <= leafRadius; ++lz) {
                                BlockPos leafPos = pos.offset((Vec3i)new BlockPos(lx, ly, lz));
                                block = BlockUtils.isLogOrLeaf(world.getBlockState(leafPos));
                                if (block != TreeBlockTypes.LEAF) continue;
                                if (captureDrops) {
                                    drops.addAll(BlockUtils.destroyBlockCaptureDrops((LivingEntity)player, world, leafPos, false, 0, (Tier)MNAtierSorting.getSortedTiers().get(MNAtierSorting.getSortedTiers().size() - 1)));
                                    continue;
                                }
                                BlockUtils.destroyBlock((LivingEntity)player, world, leafPos, !captureDrops, (Tier)MNAtierSorting.getSortedTiers().get(MNAtierSorting.getSortedTiers().size() - 1));
                            }
                        }
                    }
                    if (blockBreakCount > 25) continue;
                    Pair<Boolean, List<ItemStack>> sub = BlockUtils.breakTreeRecursive(player, world, pos, checkedBlocks, blockBreakCount, captureDrops);
                    drops.addAll((Collection)sub.getSecond());
                    success &= ((Boolean)sub.getFirst()).booleanValue();
                }
            }
        }
        return new Pair(success, drops);
    }

    public static TreeBlockTypes isLogOrLeaf(BlockState state) {
        if (MATags.isBlockIn(state.getBlock(), MATags.Blocks.LOGS)) {
            return TreeBlockTypes.LOG;
        }
        if (MATags.isBlockIn(state.getBlock(), MATags.Blocks.LEAVES) || state.is(BlockTags.WART_BLOCKS)) {
            return TreeBlockTypes.LEAF;
        }
        return TreeBlockTypes.INVALID;
    }

    public static boolean placeBlock(ServerLevel world, BlockPos pos, Direction face, BlockState state, @Nullable Player player) {
        if (world.isLoaded(pos) && true) {
            world.setBlockAndUpdate(pos, state);
            return true;
        }
        return false;
    }

    public static BlockPos[] getBlocksInFrontOfCharacter(LivingEntity entity, int numBlocks, BlockPos firstBlock) {
        float speed = 0.1f;
        float factor = (float)Math.PI / 180;
        float sinYawRadians = Mth.sin((float)(entity.getYRot() * factor));
        float cosYawRadians = Mth.cos((float)(entity.getYRot() * factor));
        float sinPitchRadians = Mth.sin((float)(entity.getXRot() * factor));
        float cosPitchRadians = Mth.cos((float)(entity.getXRot() * factor));
        double motionZ = cosYawRadians * cosPitchRadians * speed;
        double motionX = -sinYawRadians * cosPitchRadians * speed;
        double motionY = -sinPitchRadians * speed;
        double curX = firstBlock.getX();
        double curY = firstBlock.getY();
        double curZ = firstBlock.getZ();
        float minimum = 0.01f;
        if (Math.abs(motionX) < (double)minimum) {
            motionX = 0.0;
        }
        if (Math.abs(motionY) < (double)minimum) {
            motionY = 0.0;
        }
        if (Math.abs(motionZ) < (double)minimum) {
            motionZ = 0.0;
        }
        int lastX = firstBlock.getX();
        int lastY = firstBlock.getY();
        int lastZ = firstBlock.getZ();
        BlockPos[] list = new BlockPos[numBlocks];
        list[0] = new BlockPos((Vec3i)firstBlock);
        int count = 1;
        while (count < numBlocks) {
            if ((int)Math.round(curX += motionX) == lastX && (int)Math.round(curY += motionY) == lastY && (int)Math.round(curZ += motionZ) == lastZ) continue;
            lastX = (int)Math.round(curX);
            lastY = (int)Math.round(curY);
            lastZ = (int)Math.round(curZ);
            list[count++] = new BlockPos(lastX, lastY, lastZ);
        }
        return list;
    }

    public static void stepThroughBlocksLinear(BlockPos a, BlockPos b, Consumer<BlockPos> action) {
        float stepDist = 0.2f;
        Vec3 curPos = Vec3.atCenterOf((Vec3i)a);
        Vec3 endPos = Vec3.atCenterOf((Vec3i)b);
        Vec3 step = endPos.subtract(curPos).normalize().scale((double)stepDist);
        action.accept(BlockPos.containing((double)curPos.x(), (double)curPos.y(), (double)curPos.z()));
        while (!curPos.closerThan((Position)endPos, (double)stepDist)) {
            curPos = curPos.add(step);
            BlockPos cbp = BlockPos.containing((double)curPos.x(), (double)curPos.y(), (double)curPos.z());
            action.accept(cbp);
        }
    }

    public static BlockPos Vector3dToBlockPosRound(Vec3 vec) {
        return BlockPos.containing((double)Math.floor(vec.x), (double)Math.floor(vec.y), (double)Math.floor(vec.z));
    }

    public static void IterateBlocksInCube(int radius, BlockPos center, Consumer<BlockPos> callable) {
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                for (int k = -radius; k <= radius; ++k) {
                    BlockPos pos = new BlockPos(i, j, k).offset((Vec3i)center);
                    callable.accept(pos);
                }
            }
        }
    }

    public static void stepThroughBlocksInCube(Vec3 a, Vec3 b, Consumer<BlockPos> action) {
        Vec3 dimensions = b.subtract(a);
        Vec3 step = dimensions.normalize();
        ArrayList<BlockPos> processed = new ArrayList<BlockPos>();
        double x = a.x;
        while (b.x - x > step.x) {
            double y = a.y;
            while (b.y - y > step.y) {
                double z = a.z;
                while (b.z - z > step.z) {
                    BlockPos cur = BlockUtils.Vector3dToBlockPosRound(new Vec3(x, y, z));
                    if (!processed.contains(cur)) {
                        action.accept(cur);
                        processed.add(cur);
                    }
                    z += step.z;
                }
                y += step.y;
            }
            x += step.x;
        }
    }

    public static boolean destroyBlock(LivingEntity caster, Level world, BlockPos pos, boolean doDrops, Tier harvestLevel) {
        return BlockUtils.destroyBlock(caster, world, pos, doDrops, false, -1, harvestLevel);
    }

    public static boolean destroyBlock(LivingEntity caster, Level world, BlockPos pos, boolean doDrops, boolean silkTouch, int fortuneLevel, Tier harvestLevel) {
        int xp;
        if (!(world instanceof ServerLevel)) {
            return false;
        }
        Player playerEntity = caster instanceof Player ? (Player)caster : FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)world));
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed((BlockGetter)world, pos);
        if (hardness < 0.0f) {
            return false;
        }
        if (state.requiresCorrectToolForDrops() && !MNAtierSorting.isCorrectTierForDrops((Tier)harvestLevel, (BlockState)state)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, playerEntity);
        if (NeoForge.EVENT_BUS.post((Event)event)) {
            return false;
        }
        if (fortuneLevel > 0) {
            stack = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
            stack.enchant(Enchantments.BLOCK_FORTUNE, fortuneLevel);
            Block.dropResources((BlockState)world.getBlockState(pos), (Level)world, (BlockPos)pos, (BlockEntity)world.getBlockEntity(pos), (Entity)playerEntity, (ItemStack)stack);
            doDrops = false;
        } else if (silkTouch) {
            stack = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            Block.dropResources((BlockState)world.getBlockState(pos), (Level)world, (BlockPos)pos, (BlockEntity)world.getBlockEntity(pos), (Entity)playerEntity, (ItemStack)stack);
            doDrops = false;
        }
        if (!silkTouch && (xp = state.getExpDrop((LevelReader)world, world.getRandom(), pos, 0, 0)) > 0) {
            state.getBlock().popExperience((ServerLevel)world, pos, xp);
        }
        if (doDrops) {
            if (world.destroyBlock(pos, true, (Entity)playerEntity)) {
                int xp2;
                if (!silkTouch && (xp2 = state.getExpDrop((LevelReader)world, world.getRandom(), pos, 0, 0)) > 0) {
                    state.getBlock().popExperience((ServerLevel)world, pos, xp2);
                }
                return true;
            }
            return false;
        }
        return world.destroyBlock(pos, false);
    }

    public static List<ItemStack> destroyBlockCaptureDrops(LivingEntity caster, Level world, BlockPos pos, boolean silkTouch, int fortuneLevel, Tier harvestLevel) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        if (!(world instanceof ServerLevel)) {
            return drops;
        }
        Player playerEntity = caster instanceof Player ? (Player)caster : FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)world));
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed((BlockGetter)world, pos);
        if (hardness < 0.0f) {
            return drops;
        }
        if (state.requiresCorrectToolForDrops() && !MNAtierSorting.isCorrectTierForDrops((Tier)harvestLevel, (BlockState)state)) {
            return drops;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, playerEntity);
        if (NeoForge.EVENT_BUS.post((Event)event)) {
            return drops;
        }
        ItemStack stack = new ItemStack((ItemLike)Items.DIAMOND_PICKAXE);
        if (fortuneLevel > 0) {
            stack.enchant(Enchantments.BLOCK_FORTUNE, fortuneLevel);
            drops.addAll(Block.getDrops((BlockState)state, (ServerLevel)((ServerLevel)world), (BlockPos)pos, (BlockEntity)world.getBlockEntity(pos), (Entity)playerEntity, (ItemStack)stack));
        } else if (silkTouch) {
            stack.enchant(Enchantments.SILK_TOUCH, 1);
            drops.addAll(Block.getDrops((BlockState)state, (ServerLevel)((ServerLevel)world), (BlockPos)pos, (BlockEntity)world.getBlockEntity(pos), (Entity)playerEntity, (ItemStack)stack));
        } else {
            drops.addAll(Block.getDrops((BlockState)state, (ServerLevel)((ServerLevel)world), (BlockPos)pos, (BlockEntity)world.getBlockEntity(pos), (Entity)playerEntity, (ItemStack)stack));
        }
        if (world.destroyBlock(pos, false, (Entity)playerEntity)) {
            int xp;
            state.getBlock().playerWillDestroy(world, pos, state, playerEntity);
            if (!silkTouch && (xp = state.getExpDrop((LevelReader)world, world.getRandom(), pos, 0, 0)) > 0) {
                state.getBlock().popExperience((ServerLevel)world, pos, xp);
            }
            BlockUtils.updateBlockState(world, pos);
        } else {
            drops.clear();
        }
        return drops;
    }

    public static boolean canDestroyBlock(LivingEntity caster, Level world, BlockPos pos, Tier harvestLevel) {
        if (!(world instanceof ServerLevel)) {
            return false;
        }
        Player playerEntity = caster instanceof Player ? (Player)caster : FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)world));
        BlockState state = world.getBlockState(pos);
        float hardness = state.getDestroySpeed((BlockGetter)world, pos);
        if (hardness < 0.0f) {
            return false;
        }
        if (state.requiresCorrectToolForDrops() && !MNAtierSorting.isCorrectTierForDrops((Tier)harvestLevel, (BlockState)state)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, playerEntity);
        return !NeoForge.EVENT_BUS.post((Event)event);
    }

    public static void updateBlockState(Level world, BlockPos pos, BlockState state) {
        if (!world.isOutsideBuildHeight(pos)) {
            world.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public static void updateBlockState(Level world, BlockPos pos) {
        BlockUtils.updateBlockState(world, pos, world.getBlockState(pos));
    }

    public static boolean tryDestroyBlockSilent(Level world, BlockPos pos, boolean dropBlock) {
        return BlockUtils.destroyBlockSilent(world, pos, dropBlock, null);
    }

    public static boolean tryDestroyBlockSilent(Level world, BlockPos pos, boolean dropBlock, @Nullable LivingEntity caster) {
        Player playerEntity;
        if (!(world instanceof ServerLevel)) {
            return false;
        }
        Object object = playerEntity = caster instanceof Player ? (Player)caster : FakePlayerFactory.getMinecraft((ServerLevel)((ServerLevel)world));
        if (NeoForge.EVENT_BUS.post((Event)new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), playerEntity))) {
            return false;
        }
        return BlockUtils.destroyBlockSilent(world, pos, dropBlock);
    }

    private static boolean destroyBlockSilent(Level world, BlockPos pos, boolean dropBlock) {
        return BlockUtils.destroyBlockSilent(world, pos, dropBlock, null);
    }

    private static boolean destroyBlockSilent(Level world, BlockPos pos, boolean isMoving, @Nullable Entity entityIn) {
        BlockState blockstate = world.getBlockState(pos);
        if (world.isEmptyBlock(pos)) {
            return false;
        }
        FluidState ifluidstate = world.getFluidState(pos);
        if (isMoving) {
            BlockEntity tileentity = blockstate.hasBlockEntity() ? world.getBlockEntity(pos) : null;
            Block.dropResources((BlockState)blockstate, (Level)world, (BlockPos)pos, (BlockEntity)tileentity, (Entity)entityIn, (ItemStack)ItemStack.EMPTY);
        }
        return world.setBlock(pos, ifluidstate.createLegacyBlock(), 3);
    }

    public static Tier tierFromHarvestLevel(int harvestLevel) {
        ArrayList<? extends String> configuredTiers = new ArrayList<String>(GeneralConfigValues.BreakMagnitudeMapping);
        if (configuredTiers.size() == 0) {
            configuredTiers.add("minecraft:stone");
        }
        if (harvestLevel < 0) {
            harvestLevel = 0;
        }
        if (harvestLevel >= configuredTiers.size()) {
            harvestLevel = configuredTiers.size() - 1;
        }
        ResourceLocation rLoc = null;
        try {
            rLoc = ResourceLocation.parse(configuredTiers.get(harvestLevel));
        }
        catch (Throwable t) {
            rLoc = ResourceLocation.parse("minecraft:stone");
        }
        Tier resolved = TierSortingRegistry.byName((ResourceLocation)rLoc);
        if (resolved == null) {
            resolved = (Tier)MNAtierSorting.getSortedTiers().get(0);
        }
        return resolved;
    }

    public static BlockState readBlockState(CompoundTag pTag) {
        if (!pTag.contains("Name", 8)) {
            return Blocks.AIR.defaultBlockState();
        }
        ResourceLocation resourcelocation = ResourceLocation.parse(pTag.getString("Name"));
        Block block = (Block)BuiltInRegistries.BLOCK.get(resourcelocation);
        if (block == null) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState blockstate = block.defaultBlockState();
        if (pTag.contains("Properties", 10)) {
            CompoundTag compoundtag = pTag.getCompound("Properties");
            StateDefinition statedefinition = block.getStateDefinition();
            for (String s : compoundtag.getAllKeys()) {
                Property property = statedefinition.getProperty(s);
                if (property == null) continue;
                blockstate = BlockUtils.setValueHelper(blockstate, property, s, compoundtag, pTag);
            }
        }
        return blockstate;
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S pStateHolder, Property<T> pProperty, String pPropertyName, CompoundTag pPropertiesTag, CompoundTag pBlockStateTag) {
        Optional optional = pProperty.getValue(pPropertiesTag.getString(pPropertyName));
        if (optional.isPresent()) {
            return (S)((StateHolder)pStateHolder.setValue(pProperty, optional.get()));
        }
        ManaAndArtifice.LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", pPropertyName, pPropertiesTag.getString(pPropertyName), pBlockStateTag.toString());
        return pStateHolder;
    }

    public static enum TreeBlockTypes {
        LOG,
        LEAF,
        INVALID;

    }
}

