 
package com.mna.recipes.multiblock;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.api.blocks.tile.IMultiblockDefinition;
import com.mna.network.ClientMessageDispatcher;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.multiblock.MultiblockConfiguration;
import com.mna.recipes.multiblock.block_matchers.ChalkBlockMatcher;
import com.mna.recipes.multiblock.block_matchers.ExactBlockMatcher;
import com.mna.recipes.multiblock.block_matchers.IBlockMatcher;
import com.mna.recipes.multiblock.block_matchers.PedestalBlockMatcher;
import com.mna.recipes.multiblock.block_matchers.RefractionLensBlockMatcher;
import com.mna.recipes.multiblock.block_matchers.StairsBlockMatcher;
import com.mna.recipes.multiblock.block_matchers.StatelessBlockMatcher;
import com.mna.tools.BlockUtils;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class MultiblockDefinition
extends AMRecipeBase
implements IMultiblockDefinition {
    private static final ArrayList<IBlockMatcher> blockMatchers;
    public static final IBlockMatcher defaultMatcher;
    public static final IBlockMatcher stairsMatcher;
    public static final IBlockMatcher statelessMatcher;
    public static final IBlockMatcher chalkMatcher;
    public static final IBlockMatcher refractionLensMatcher;
    public static final IBlockMatcher pedestalMatcher;
    private ItemStack guiStack = ItemStack.EMPTY;
    boolean isValid = false;
    boolean hasLoaded = false;
    boolean hasRequested = false;
    boolean isSymmetrical = false;
    private ResourceLocation structure;
    List<BlockState> blockStates = new ArrayList<BlockState>();
    MultiblockConfiguration structureBlocks;
    ArrayList<MultiblockConfiguration> variations = new ArrayList();
    HashMap<ResourceLocation, Integer> specialBlockMatchersByBlock = new HashMap();
    HashMap<Long, Integer> specialBlockMatchersByOffset = new HashMap();

    public MultiblockDefinition(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    protected void parseExtraJson(JsonObject object) {
        this.isValid = true;
        if (!object.has("structure")) {
            this.isValid = false;
            return;
        }
        this.structure = ResourceLocation.parse(object.get("structure").getAsString());
        if (object.has("matchers")) {
            JsonArray rawBlockChecks = object.get("matchers").getAsJsonArray();
            rawBlockChecks.forEach(e -> {
                if (e.isJsonObject()) {
                    JsonObject elem = e.getAsJsonObject();
                    if (elem.has("matcher") && (elem.has("offset") || elem.has("block"))) {
                        ResourceLocation matcher = ResourceLocation.parse(elem.get("matcher").getAsString());
                        int index = -1;
                        for (int i = 0; i < blockMatchers.size(); ++i) {
                            if (!blockMatchers.get(i).getId().toString().equals(matcher.toString())) continue;
                            index = i;
                            break;
                        }
                        if (index > -1) {
                            if (elem.has("offset")) {
                                long offset = elem.get("offset").getAsLong();
                                this.specialBlockMatchersByOffset.put(offset, index);
                            } else {
                                ResourceLocation block = ResourceLocation.parse(elem.get("block").getAsString());
                                this.specialBlockMatchersByBlock.put(block, index);
                            }
                        } else {
                            ManaAndArtifice.LOGGER.warn("Misconfigured special block match in " + this.getId().toString() + ": matcher '" + matcher.toString() + "' could not be resolved.");
                        }
                    } else {
                        ManaAndArtifice.LOGGER.warn("Misconfigured special block match in " + this.getId().toString() + ": missing matcher and (offset or block id)");
                    }
                }
            });
        }
        if (object.has("symmetrical")) {
            this.isSymmetrical = object.get("symmetrical").getAsBoolean();
        }
        if (object.has("replacements")) {
            JsonArray replacements = object.get("replacements").getAsJsonArray();
            replacements.forEach(r -> {
                JsonObject replacement;
                MultiblockConfiguration variation;
                if (r.isJsonObject() && (variation = MultiblockConfiguration.parseVariation(replacement = r.getAsJsonObject(), this.blockStates)).getIsValid()) {
                    this.variations.add(variation);
                }
            });
        }
    }

    private boolean tryLoadStructure(Level world) {
        if (!this.hasLoaded) {
            if (world instanceof ServerLevel) {
                if (!this.isValid) {
                    return false;
                }
                this.structureBlocks = MultiblockConfiguration.loadStructure(((ServerLevel)world).getStructureManager(), this.structure, this.blockStates);
                this.hasLoaded = this.structureBlocks.getIsValid();
            } else if (!this.hasRequested) {
                this.structureBlocks = MultiblockConfiguration.createDummyStructure();
                ClientMessageDispatcher.sendMultiblockSyncRequestMessage((LivingEntity)ManaAndArtifice.instance.proxy.getClientPlayer(), this.getId());
                this.hasRequested = true;
            }
        }
        return this.hasLoaded;
    }

    @Override
    public boolean spawn(ServerLevel world, BlockPos origin) {
        return this.spawn(world, origin, Rotation.NONE, true);
    }

    @Override
    public boolean spawn(ServerLevel world, BlockPos origin, Rotation rotation, boolean centerOffset) {
        if (!this.isValid) {
            return false;
        }
        if (!this.tryLoadStructure((Level)world)) {
            return false;
        }
        if (centerOffset) {
            origin = origin.subtract(new Vec3i(this.getSize().getX() / 2, 0, this.getSize().getZ() / 2));
        }
        this.structureBlocks.resetMatchData();
        this.structureBlocks.rotate(rotation);
        this.variations.forEach(v -> {
            v.resetMatchData();
            v.rotate(rotation);
        });
        BlockPos fOrigin = origin;
        this.structureBlocks.getOffsets().forEach(packedOffset -> {
            BlockPos offset = BlockPos.of((long)packedOffset);
            BlockPos worldPos = fOrigin.offset((Vec3i)offset);
            int blockStateIndex = this.structureBlocks.getBlockAt(offset);
            if (blockStateIndex < 0 || blockStateIndex >= this.blockStates.size()) {
                this.isValid = false;
                return;
            }
            BlockState desiredState = this.blockStates.get(blockStateIndex);
            world.setBlockAndUpdate(worldPos, desiredState);
        });
        return true;
    }

    @Override
    public boolean match(Level world, BlockPos origin) {
        return this.match(world, origin, Rotation.NONE, true);
    }

    @Override
    public boolean match(Level world, BlockPos origin, Rotation rotation, boolean centerOffset) {
        Pair<Integer, Integer> matchCount = this.matchWithCount(world, origin, rotation, centerOffset);
        return ((Integer)matchCount.getFirst()).equals(matchCount.getSecond());
    }

    @Override
    public Pair<Integer, Integer> matchWithCount(Level world, BlockPos origin, Rotation rotation, boolean centerOffset) {
        if (!this.isValid) {
            return new Pair(0, 1);
        }
        if (!this.tryLoadStructure(world)) {
            return new Pair(0, 1);
        }
        if (world.isClientSide && !this.hasLoaded) {
            return new Pair(0, 1);
        }
        if (centerOffset) {
            origin = origin.subtract(new Vec3i(this.getSize().getX() / 2, 0, this.getSize().getZ() / 2));
        }
        this.structureBlocks.resetMatchData();
        this.structureBlocks.rotate(rotation);
        this.variations.forEach(v -> {
            v.resetMatchData();
            v.rotate(rotation);
        });
        BlockPos fOrigin = origin;
        this.structureBlocks.getOffsets().forEach(packedOffset -> {
            BlockPos offset = BlockPos.of((long)packedOffset);
            BlockPos worldPos = fOrigin.offset((Vec3i)offset);
            BlockState worldState = world.getBlockState(worldPos);
            int blockStateIndex = this.structureBlocks.getBlockAt(offset);
            if (blockStateIndex < 0 || blockStateIndex >= this.blockStates.size()) {
                this.isValid = false;
                return;
            }
            BlockState desiredState = this.blockStates.get(blockStateIndex);
            if (this.matchBlockState(world, offset, worldPos, worldState, desiredState)) {
                this.structureBlocks.markMatch(offset);
            }
            this.variations.forEach(variation -> {
                int variationStateIndex = variation.getBlockAt(offset);
                if (variationStateIndex < 0 || blockStateIndex >= this.blockStates.size()) {
                    return;
                }
                BlockState variationState = this.blockStates.get(variationStateIndex);
                if (this.matchBlockState(world, offset, worldPos, worldState, variationState)) {
                    variation.markMatch(offset);
                }
            });
        });
        this.variations.forEach(v -> v.computeMatch());
        List<MultiblockConfiguration> matchedVariations = this.variations.stream().filter(v -> v.matched()).collect(Collectors.toList());
        return this.structureBlocks.matchCount(matchedVariations);
    }

    @Override
    public int getBlockCount() {
        if (!this.isValid || !this.hasLoaded) {
            return 1;
        }
        return this.structureBlocks.countBlocks();
    }

    @Override
    @Nullable
    public HashMap<BlockPos, BlockState> getMissingBlocks(Level world, BlockPos origin, Rotation rotation, boolean centerOffset) {
        if (!this.tryLoadStructure(world)) {
            return null;
        }
        if (world.isClientSide && !this.hasLoaded) {
            return null;
        }
        if (!this.isValid) {
            return null;
        }
        if (centerOffset) {
            origin = origin.subtract(new Vec3i(this.getSize().getX() / 2, 0, this.getSize().getZ() / 2));
        }
        HashMap<BlockPos, BlockState> missingBlocks = new HashMap<BlockPos, BlockState>();
        BlockPos fOrigin = origin;
        this.structureBlocks.getOffsets().forEach(packedOffset -> {
            BlockPos offset = BlockPos.of((long)packedOffset);
            BlockPos worldPos = fOrigin.offset((Vec3i)offset);
            BlockState worldState = world.getBlockState(worldPos);
            int blockStateIndex = this.structureBlocks.getBlockAt(offset);
            if (blockStateIndex < 0 || blockStateIndex >= this.blockStates.size()) {
                this.isValid = false;
                return;
            }
            BlockState desiredState = this.blockStates.get(blockStateIndex);
            if (!this.matchBlockState(world, offset, worldPos, worldState, desiredState)) {
                missingBlocks.put(worldPos, desiredState);
            }
        });
        this.variations.stream().forEach(v -> v.getOffsets().forEach(packedOffset -> {
            BlockPos offset = BlockPos.of((long)packedOffset);
            BlockPos worldPos = fOrigin.offset((Vec3i)offset);
            BlockState worldState = world.getBlockState(worldPos);
            int blockStateIndex = v.getBlockAt(offset);
            if (blockStateIndex < 0 || blockStateIndex >= this.blockStates.size()) {
                this.isValid = false;
                return;
            }
            BlockState desiredState = this.blockStates.get(blockStateIndex);
            if (this.matchBlockState(world, offset, worldPos, worldState, desiredState)) {
                missingBlocks.remove(worldPos);
            }
        }));
        return missingBlocks;
    }

    @Override
    public List<String> getMatchedVariations() {
        return this.variations.stream().filter(v -> v.matched()).map(v -> v.identifier).collect(Collectors.toList());
    }

    @Override
    public boolean isSymmetrical() {
        return this.isSymmetrical;
    }

    private boolean matchBlockState(Level world, BlockPos offset, BlockPos worldPos, BlockState existing, BlockState desired) {
        int matcherIndex = -1;
        if (this.specialBlockMatchersByOffset.containsKey(offset.asLong())) {
            matcherIndex = this.specialBlockMatchersByOffset.get(offset.asLong());
        } else if (this.specialBlockMatchersByBlock.containsKey(BuiltInRegistries.BLOCK.getKey(desired.getBlock()))) {
            matcherIndex = this.specialBlockMatchersByBlock.get(BuiltInRegistries.BLOCK.getKey(desired.getBlock()));
        }
        if (matcherIndex > -1) {
            return blockMatchers.get(matcherIndex).match(world, offset, worldPos, desired, existing, true);
        }
        if (desired.getBlock() instanceof StairBlock) {
            return stairsMatcher.match(world, offset, worldPos, desired, existing, true);
        }
        return defaultMatcher.match(world, offset, worldPos, desired, existing, true);
    }

    public boolean matches(CraftingContainer inv, Level worldIn) {
        return false;
    }

    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.MULTIBLOCK_RECIPE_SERIALIZER.get();
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return this.getResultItem();
    }

    @Override
    public ItemStack getResultItem() {
        if (this.guiStack.isEmpty()) {
            this.guiStack = new ItemStack((ItemLike)Items.KNOWLEDGE_BOOK);
            this.guiStack.set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)"mna:multiblock_recipe").append((Component)Component.translatable((String)this.getId().toString())));
        }
        return this.guiStack;
    }

    @Override
    public ResourceLocation getStructurePath() {
        return this.structure;
    }

    @Override
    public Vec3i getSize() {
        if (!this.hasLoaded) {
            return Vec3i.ZERO;
        }
        return this.structureBlocks.getSize();
    }

    @Override
    public void setStructurePath(ResourceLocation path) {
        this.structure = path;
    }

    @Override
    public HashMap<ResourceLocation, Integer> getSpecialBlockMatchersByBlock() {
        return this.specialBlockMatchersByBlock;
    }

    @Override
    public void setSpecialBlockMatchersByBlock(HashMap<ResourceLocation, Integer> matchers) {
        this.specialBlockMatchersByBlock = matchers;
    }

    @Override
    public HashMap<Long, Integer> getSpecialBlockMatchersByOffset() {
        return this.specialBlockMatchersByOffset;
    }

    @Override
    public void setSpecialBlockMatchersByOffset(HashMap<Long, Integer> matchers) {
        this.specialBlockMatchersByOffset = matchers;
    }

    public CompoundTag serializeVariations() {
        CompoundTag data = new CompoundTag();
        ListTag list = new ListTag();
        for (MultiblockConfiguration conf : this.variations) {
            list.add(conf.serialize());
        }
        data.put("list", (Tag)list);
        return data;
    }

    public void deserializeVariations(CompoundTag data) {
        if (data.contains("list")) {
            ListTag list = data.getList("list", 10);
            this.variations.clear();
            list.forEach(e -> {
                CompoundTag entry = (CompoundTag)e;
                MultiblockConfiguration conf = MultiblockConfiguration.deserialize(entry);
                if (conf.getIsValid()) {
                    this.variations.add(conf);
                }
            });
        }
    }

    public CompoundTag serializeCoreBlocks(ServerLevel world) {
        if (!this.isValid) {
            return new CompoundTag();
        }
        if (!this.tryLoadStructure((Level)world)) {
            return new CompoundTag();
        }
        CompoundTag data = new CompoundTag();
        data.put("offsets", (Tag)this.structureBlocks.serialize());
        ListTag states = new ListTag();
        this.blockStates.forEach(state -> states.add(NbtUtils.writeBlockState((BlockState)state)));
        data.put("states", (Tag)states);
        return data;
    }

    public void deserializeCoreBlocks(CompoundTag data) {
        this.blockStates = new ArrayList<BlockState>();
        if (data.contains("offsets")) {
            this.structureBlocks = MultiblockConfiguration.deserialize(data.getCompound("offsets"));
        }
        if (data.contains("states", 9)) {
            ListTag states = (ListTag)data.get("states");
            states.forEach(a -> this.blockStates.add(BlockUtils.readBlockState((CompoundTag)a)));
        }
        this.isValid = this.structureBlocks.getIsValid() && this.blockStates.size() > 0;
        this.hasLoaded = true;
    }

    public void setBlockStates(List<BlockState> states) {
        this.blockStates = states;
    }

    public void setStructure(MultiblockConfiguration mainStructure) {
        this.structureBlocks = mainStructure;
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.MULTIBLOCK_TYPE.get();
    }

    @Nullable
    public List<List<ItemStack>> getItemsList(Level world) {
        if (!this.tryLoadStructure(world)) {
            return null;
        }
        if (world.isClientSide && !this.hasLoaded) {
            return null;
        }
        HashMap<Block, Integer> blockCounts = new HashMap<>();
        this.structureBlocks.structureBlocks.entrySet().forEach(e -> {
            BlockState state = this.blockStates.get((Integer)((Pair)e.getValue()).getFirst());
            if (!state.isAir()) {
                blockCounts.put(state.getBlock(), blockCounts.getOrDefault(state.getBlock(), 0) + 1);
            }
        });
        ArrayList<List<ItemStack>> blocksList = new ArrayList<List<ItemStack>>();
        blockCounts.entrySet().forEach(e -> {
            List<Object> valid = this.getValidItemsFor((Block)e.getKey());
            valid.forEach(s -> s.setCount(((Integer)e.getValue()).intValue()));
            valid = valid.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
            if (valid.size() > 0) {
                blocksList.add(valid);
            }
        });
        return blocksList;
    }

    private ArrayList<ItemStack> getValidItemsFor(Block block) {
        int matcherIndex = this.specialBlockMatchersByBlock.getOrDefault(BuiltInRegistries.BLOCK.getKey(block), -1);
        if (matcherIndex > -1) {
            return blockMatchers.get(matcherIndex).getValidItems(block);
        }
        ArrayList<ItemStack> out = new ArrayList<ItemStack>();
        out.add(new ItemStack((ItemLike)block));
        return out;
    }

    @Override
    public List<BlockPos> getPositions(ResourceLocation blockType, @Nullable Predicate<BlockState> predicate) {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        this.structureBlocks.structureBlocks.forEach((l, p) -> {
            BlockState state = this.blockStates.get((Integer)p.getFirst());
            if (state != null) {
                if (predicate != null && !predicate.test(state)) {
                    return;
                }
                if (!BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(blockType)) {
                    return;
                }
                positions.add(BlockPos.of((long)l));
            }
        });
        return positions;
    }

    @Override
    public List<BlockPos> getPositions(List<ResourceLocation> blockTypes, @Nullable Predicate<BlockState> predicate) {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        this.structureBlocks.structureBlocks.forEach((l, p) -> {
            BlockState state = this.blockStates.get((Integer)p.getFirst());
            if (state != null) {
                if (predicate != null && !predicate.test(state)) {
                    return;
                }
                if (blockTypes.stream().noneMatch(s -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(s))) {
                    return;
                }
                positions.add(BlockPos.of((long)l));
            }
        });
        return positions;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }

    static {
        defaultMatcher = new ExactBlockMatcher();
        stairsMatcher = new StairsBlockMatcher();
        statelessMatcher = new StatelessBlockMatcher();
        chalkMatcher = new ChalkBlockMatcher();
        refractionLensMatcher = new RefractionLensBlockMatcher();
        pedestalMatcher = new PedestalBlockMatcher();
        blockMatchers = new ArrayList();
        blockMatchers.add(stairsMatcher);
        blockMatchers.add(statelessMatcher);
        blockMatchers.add(refractionLensMatcher);
        blockMatchers.add(pedestalMatcher);
        blockMatchers.add(chalkMatcher);
    }
}


