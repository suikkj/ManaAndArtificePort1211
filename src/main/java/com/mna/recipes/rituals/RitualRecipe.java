 
package com.mna.recipes.rituals;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mna.ManaAndArtifice;
import com.mna.api.recipes.IRitualRecipe;
import com.mna.api.rituals.IRitualReagent;
import com.mna.api.rituals.RitualBlockPos;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.items.ItemInit;
import com.mna.recipes.AMRecipeBase;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.multiblock.MultiblockConfiguration;
import com.mna.recipes.multiblock.MultiblockDefinition;
import com.mna.recipes.rituals.RitualRecipeHelper;
import com.mna.rituals.MatchedRitual;
import com.mna.rituals.RitualReagent;
import com.mojang.datafixers.util.Pair;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class RitualRecipe
extends AMRecipeBase
implements IRitualRecipe {
    private long innerColor = 0xFFFFFFL;
    private long outerColor = 65280L;
    private long beamColor = 0xFFFFFFL;
    private boolean connectBeam = true;
    private boolean displayIndexes = true;
    private boolean kittable = true;
    private boolean isValid;
    private int[][] pattern;
    private int[][] displayPattern;
    private RitualReagent[][] reagents;
    private String[] manaweave_patterns;
    private int RITUAL_SIZE;
    private ItemStack createdItem = ItemStack.EMPTY;
    private ItemStack guiItem = ItemStack.EMPTY;
    private String command;
    private MultiblockDefinition _cachedMultiblock;

    public RitualRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public ItemStack getResultItem() {
        if (this.createdItem.isEmpty()) {
            ItemStack output = new ItemStack((ItemLike)ItemInit.RUNE_RITUAL_METAL.get());
            output.set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)this.getId().toString()));
            this.createdItem = output;
        }
        return this.createdItem;
    }

    @Override
    public void parseExtraJson(JsonObject json) {
        if (!(json.has("keys") && json.has("pattern") && json.has("reagents") && json.get("pattern").isJsonArray() && json.get("reagents").isJsonArray() && json.get("keys").isJsonObject())) {
            ManaAndArtifice.LOGGER.error("Malformed JSON for ritual recipe (" + this.getId().toString() + ").  Missing critical elements.  It was NOT loaded!");
            this.isValid = false;
            return;
        }
        JsonArray patternData = json.get("pattern").getAsJsonArray();
        try {
            this.pattern = this.parsePatternArray(patternData);
            if (this.pattern.length == 0) {
                this.isValid = false;
                return;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            this.isValid = false;
            return;
        }
        if (json.has("displayPattern")) {
            JsonArray displayPatternData = json.get("displayPattern").getAsJsonArray();
            try {
                this.displayPattern = this.parsePatternArray(displayPatternData);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                this.displayPattern = this.pattern;
                ManaAndArtifice.LOGGER.error("Malformed display pattern JSON for ritual recipe (" + this.getId().toString() + ").  Falling back to main pattern for display.");
            }
        } else {
            this.displayPattern = this.pattern;
        }
        JsonObject reagentKeys = json.get("keys").getAsJsonObject();
        JsonArray reagentData = json.get("reagents").getAsJsonArray();
        try {
            if (!this.parseReagentArray(reagentData, reagentKeys)) {
                this.isValid = false;
                return;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            this.isValid = false;
            return;
        }
        if (json.has("manaweave")) {
            try {
                JsonElement manaweave = json.get("manaweave");
                this.parseManaweave(manaweave);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                this.isValid = false;
                return;
            }
        } else {
            this.manaweave_patterns = new String[0];
        }
        if (json.has("parameters")) {
            try {
                JsonObject params = json.get("parameters").getAsJsonObject();
                this.parseParameters(params);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                this.isValid = false;
                return;
            }
        }
        if (json.has("createsItem")) {
            try {
                Item item;
                ResourceLocation output = ResourceLocation.parse(json.get("createsItem").getAsString());
                Item item2 = item = BuiltInRegistries.ITEM.containsKey(output) ? (Item)BuiltInRegistries.ITEM.get(output) : null;
                if (item != null) {
                    this.createdItem = new ItemStack((ItemLike)item);
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                this.isValid = false;
                return;
            }
        }
        if (json.has("command")) {
            this.command = json.get("command").getAsString();
        }
        if (!this.validatePatternArray(this.pattern)) {
            ManaAndArtifice.LOGGER.error("Error parsing pattern " + this.getId().toString() + ". Ritual pattern must be square.  Pattern size: " + this.RITUAL_SIZE + "x" + this.RITUAL_SIZE + ".");
            this.isValid = false;
            return;
        }
        if (!this.validateDisplayPatternArray(this.displayPattern)) {
            ManaAndArtifice.LOGGER.error("Error parsing display pattern " + this.getId().toString() + ". Ritual display pattern must be the same size as the main pattern array.  Pattern size: " + this.RITUAL_SIZE + "x" + this.RITUAL_SIZE + ".");
            this.isValid = false;
            return;
        }
        if (!this.validateReagentArray(this.reagents, this.pattern)) {
            ManaAndArtifice.LOGGER.error("Error parsing pattern " + this.getId().toString() + ". Reagent array must be the same size as the pattern array, and reagent locations must correspond to the same non-zero elements in the pattern array.");
            this.isValid = false;
            return;
        }
        this.isValid = true;
    }

    private void parseManaweave(JsonElement manaweave) {
        if (manaweave.isJsonArray()) {
            JsonArray manaweave_array = manaweave.getAsJsonArray();
            this.manaweave_patterns = new String[manaweave_array.size()];
            for (int i = 0; i < manaweave_array.size(); ++i) {
                this.manaweave_patterns[i] = manaweave_array.get(i).getAsString();
            }
        } else {
            this.manaweave_patterns = new String[0];
        }
    }

    private void parseParameters(JsonObject parameters) {
        if (parameters.has("innerColor")) {
            this.innerColor = Long.decode(parameters.get("innerColor").getAsString());
        }
        if (parameters.has("outerColor")) {
            this.outerColor = Long.decode(parameters.get("outerColor").getAsString());
        }
        if (parameters.has("beamColor")) {
            this.beamColor = Long.decode(parameters.get("beamColor").getAsString());
        }
        if (parameters.has("connectBeam")) {
            this.connectBeam = parameters.get("connectBeam").getAsBoolean();
        }
        if (parameters.has("displayIndexes")) {
            this.displayIndexes = parameters.get("displayIndexes").getAsBoolean();
        }
        if (parameters.has("kittable")) {
            this.kittable = parameters.get("kittable").getAsBoolean();
        }
        this.tier = parameters.has("tier") ? parameters.get("tier").getAsInt() : 0;
    }

    private int[][] parsePatternArray(JsonArray patternData) {
        int[][] output = new int[patternData.size()][];
        for (int i = 0; i < patternData.size(); ++i) {
            JsonElement subElem = patternData.get(i);
            if (subElem.isJsonArray()) {
                JsonArray subArray = subElem.getAsJsonArray();
                output[i] = new int[subArray.size()];
                for (int j = 0; j < subArray.size(); ++j) {
                    JsonElement elem = subArray.get(j);
                    output[i][j] = elem.getAsInt();
                }
                continue;
            }
            ManaAndArtifice.LOGGER.error("Malformed pattern JSON for ritual recipe (" + this.getId().toString() + ").  Sub element is not an array.");
            return new int[0][];
        }
        return output;
    }

     
    private boolean parseReagentArray(JsonArray reagentPatterns, JsonObject keyData) {
        HashMap<Character, RitualReagent> keys = new HashMap<Character, RitualReagent>();
        if (reagentPatterns.size() != this.pattern.length) {
            return false;
        }
        this.reagents = new RitualReagent[reagentPatterns.size()][];
        int i = 0;
        while (i < reagentPatterns.size()) {
            JsonElement elem = reagentPatterns.get(i);
            if (!elem.isJsonPrimitive()) {
                return false;
            }
            char[] cArr = elem.getAsString().toCharArray();
            this.reagents[i] = new RitualReagent[cArr.length];
            for (int j = 0; j < cArr.length; ++j) {
                RitualReagent rr = null;
                if (cArr[j] != ' ') {
                    if (!keys.containsKey(Character.valueOf(cArr[j]))) {
                        JsonObject subObj;
                        String key = String.valueOf(cArr[j]);
                        if (!keyData.has(key)) {
                            ManaAndArtifice.LOGGER.error("Missing pattern map in ritual (" + this.getId().toString() + ") - key '" + key + "' is undefined.");
                            return false;
                        }
                        JsonElement subElem = keyData.get(key);
                        if (subElem.isJsonObject() && (subObj = subElem.getAsJsonObject()).has("item")) {
                            rr = new RitualReagent(subObj.get("item").getAsString());
                            if (subObj.has("optional") && subObj.get("optional").getAsBoolean()) {
                                rr.asOptional();
                            }
                            if (subObj.has("consume") && !subObj.get("consume").getAsBoolean()) {
                                rr.noConsume();
                            }
                            if (subObj.has("manual_return") && subObj.get("manual_return").getAsBoolean()) {
                                rr.manualReturn();
                            }
                            if (subObj.has("is_dynamic") && subObj.get("is_dynamic").getAsBoolean()) {
                                rr.asDynamic();
                            }
                            if (subObj.has("dynamic_source") && subObj.get("dynamic_source").getAsBoolean()) {
                                rr.asDynamicSource();
                            }
                            keys.put(Character.valueOf(cArr[j]), rr);
                        }
                    } else {
                        rr = (RitualReagent)keys.get(Character.valueOf(cArr[j]));
                    }
                }
                this.reagents[i][j] = rr;
            }
            ++i;
        }
        return true;
    }

    @Nullable
    private RitualReagent parseReagentKey(JsonObject key) {
        JsonElement itemObj = key.get("item");
        JsonElement optionalObj = key.get("optional");
        JsonElement consumeObj = key.get("consume");
        if (itemObj == null) {
            return null;
        }
        RitualReagent rr = new RitualReagent(itemObj.getAsString());
        if (optionalObj != null && optionalObj.getAsBoolean()) {
            rr.asOptional();
        }
        if (consumeObj != null && !consumeObj.getAsBoolean()) {
            rr.noConsume();
        }
        return rr;
    }

    CompoundTag reagentsToNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("length", this.reagents.length);
        for (int i = 0; i < this.reagents.length; ++i) {
            for (int j = 0; j < this.reagents[i].length; ++j) {
                if (this.reagents[i][j] == null) continue;
                CompoundTag reagentData = new CompoundTag();
                this.reagents[i][j].writeToNBT(reagentData);
                nbt.put("r" + i + "_" + j, (Tag)reagentData);
            }
        }
        return nbt;
    }

    void reagentsFromNBT(CompoundTag nbt) {
        if (!nbt.contains("length")) {
            return;
        }
        int length = nbt.getInt("length");
        this.reagents = new RitualReagent[length][];
        for (int i = 0; i < length; ++i) {
            this.reagents[i] = new RitualReagent[length];
            for (int j = 0; j < length; ++j) {
                if (nbt.contains("r" + i + "_" + j)) {
                    CompoundTag reagentData = nbt.getCompound("r" + i + "_" + j);
                    this.reagents[i][j] = RitualReagent.fromNBT(reagentData);
                    continue;
                }
                this.reagents[i][j] = null;
            }
        }
        if (!this.validateReagentArray(this.reagents, this.pattern)) {
            throw new InvalidParameterException("Error parsing pattern " + this.getId().toString() + ". Reagent array must be the same size as the pattern array, and reagent locations must correspond to the same non-zero elements in the pattern array.");
        }
    }

    @Override
    public int[][] getPattern() {
        return this.pattern;
    }

    void setPattern(int[][] pattern) {
        this.pattern = pattern;
        if (!this.validatePatternArray(pattern)) {
            throw new InvalidParameterException("Error parsing pattern " + this.getId().toString() + ". Ritual pattern must be square.  Pattern size: " + this.RITUAL_SIZE + "x" + this.RITUAL_SIZE + ".");
        }
        assert (this.RITUAL_SIZE % 2 != 0);
        this.isValid = true;
    }

    public int[][] getDisplayPattern() {
        return this.displayPattern;
    }

    void setDisplayPattern(int[][] displayPattern) {
        this.displayPattern = displayPattern;
    }

    @Override
    public IRitualReagent[][] getReagents() {
        return this.reagents;
    }

    void setReagents(RitualReagent[][] reagents) {
        this.reagents = reagents;
        if (!this.validateReagentArray(reagents, this.pattern)) {
            throw new InvalidParameterException("Error parsing pattern " + this.getId().toString() + ". Reagent array must be the same size as the pattern array, and reagent locations must correspond to the same non-zero elements in the pattern array.");
        }
        this.isValid = true;
    }

    @Override
    public int getLowerBound() {
        return (int)Math.floor((float)this.RITUAL_SIZE / 2.0f);
    }

    public RitualReagent[] AllReagents() {
        ArrayList<RitualReagent> list = new ArrayList<RitualReagent>();
        for (int i = 0; i < this.reagents.length; ++i) {
            for (int j = 0; j < this.reagents[i].length; ++j) {
                if (this.reagents[i][j] == null) continue;
                list.add(this.reagents[i][j]);
            }
        }
        RitualReagent[] output = new RitualReagent[list.size()];
        return list.toArray(output);
    }

    @Override
    public int countRunes() {
        int count = 0;
        for (int i = 0; i < this.pattern.length; ++i) {
            for (int j = 0; j < this.pattern[i].length; ++j) {
                if (this.pattern[i][j] == 0) continue;
                ++count;
            }
        }
        return count;
    }

    public long getInnerColor() {
        return this.innerColor;
    }

    public long getBeamColor() {
        return this.beamColor;
    }

    void setBeamColor(long beamColor) {
        this.beamColor = beamColor;
    }

    void setInnerColor(long innerColor) {
        this.innerColor = innerColor;
    }

    public long getOuterColor() {
        return this.outerColor;
    }

    void setOuterColor(long outerColor) {
        this.outerColor = outerColor;
    }

    public boolean getConnectBeam() {
        return this.connectBeam;
    }

    void setConnectBeam(boolean connectBeam) {
        this.connectBeam = connectBeam;
    }

    public boolean getIsKittable() {
        return this.kittable;
    }

    void setKittable(boolean kittable) {
        this.kittable = kittable;
    }

    public boolean getDisplayIndexes() {
        return this.displayIndexes;
    }

    public void setDisplayIndexes(boolean displayIndexes) {
        this.displayIndexes = displayIndexes;
    }

    @Override
    public String[] getManaweavePatterns() {
        return this.manaweave_patterns;
    }

    void setManaweavePatterns(String[] patterns) {
        this.manaweave_patterns = patterns;
    }

    @Override
    public boolean isValid() {
        return this.isValid;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        if (this.guiItem.isEmpty()) {
            ItemStack output = new ItemStack((ItemLike)ItemInit.RUNE_RITUAL_METAL.get());
            output.set(DataComponents.ITEM_NAME, (Component)Component.translatable((String)this.getId().toString()));
            output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("hideTier", true);
            this.guiItem = output;
        }
        return this.guiItem;
    }

    public MultiblockDefinition getAsMultiblock() {
        if (this._cachedMultiblock == null) {
            this._cachedMultiblock = new MultiblockDefinition(this.getId());
            this._cachedMultiblock.setBlockStates(Arrays.asList(((Block)BlockInit.CHALK_RUNE.get()).defaultBlockState()));
            HashMap<ResourceLocation, Integer> matchers = new HashMap<ResourceLocation, Integer>();
            matchers.put(MultiblockDefinition.statelessMatcher.getId(), 0);
            this._cachedMultiblock.setSpecialBlockMatchersByBlock(matchers);
            MultiblockConfiguration conf = MultiblockConfiguration.createDummyStructure();
            this.getBlockPositions(BlockPos.ZERO, this.pattern, this.displayPattern, this.reagents).forEach(rbp -> conf.setBlockAt(rbp.getBlockPos(), 0));
            conf.validate();
            this._cachedMultiblock.setStructure(conf);
        }
        return this._cachedMultiblock;
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public boolean hasCommand() {
        return !StringUtil.isNullOrEmpty((String)this.command);
    }

    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider access) {
        return this.getResultItem();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)RecipeInit.RITUAL_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return (RecipeType)RecipeInit.RITUAL_TYPE.get();
    }

    public boolean matches(CraftingContainer inv, Level worldIn) {
        return true;
    }

    public static RitualRecipe find(Level world, ResourceLocation name) {
        return RitualRecipeHelper.GetRitualRecipe(world, name);
    }

    @Nullable
    public static MatchedRitual matchAnyInWorld(BlockPos center, Level world) {
        Collection<RitualRecipe> rituals = RitualRecipeHelper.getAllRituals(world);
        for (RitualRecipe ritual : rituals) {
            Pair<Direction, NonNullList<RitualBlockPos>> matchedPositions = ritual.matchInWorld(center, world);
            if (matchedPositions == null) continue;
            return new MatchedRitual((NonNullList<RitualBlockPos>)((NonNullList)matchedPositions.getSecond()), ritual, center, (Direction)matchedPositions.getFirst());
        }
        return null;
    }

    @Override
    @Nullable
    public Direction getMatchedDirection(Level world, BlockPos center) {
        if (!this.isValid) {
            return null;
        }
        if (this.matchInWorld(center, world, this.pattern)) {
            return Direction.NORTH;
        }
        int[][] rot90 = this.rotateNumberArrayCW(this.pattern);
        if (this.matchInWorld(center, world, rot90)) {
            return Direction.EAST;
        }
        int[][] rot180 = this.rotateNumberArrayCW(rot90);
        if (this.matchInWorld(center, world, rot180)) {
            return Direction.SOUTH;
        }
        int[][] rot270 = this.rotateNumberArrayCW(rot180);
        if (this.matchInWorld(center, world, rot270)) {
            return Direction.WEST;
        }
        return null;
    }

    @Override
    public Direction getMatchedDirection(List<BlockPos> offsets, BlockPos center) {
        return this.getMatchedDirection(offsets.stream().map(bp -> bp.subtract((Vec3i)center)).collect(Collectors.toList()));
    }

    @Override
    public Direction getMatchedDirection(List<BlockPos> offsets) {
        Optional<Integer> minX = offsets.stream().map(bp -> bp.getX()).min(Comparator.comparingInt(i -> i));
        Optional<Integer> maxX = offsets.stream().map(bp -> bp.getX()).max(Comparator.comparingInt(i -> i));
        Optional<Integer> minZ = offsets.stream().map(bp -> bp.getZ()).min(Comparator.comparingInt(i -> i));
        Optional<Integer> maxZ = offsets.stream().map(bp -> bp.getZ()).max(Comparator.comparingInt(i -> i));
        if (minX.isEmpty() || maxX.isEmpty() || minZ.isEmpty() || maxZ.isEmpty()) {
            return null;
        }
        int xOffset = minX.get() < 0 ? -minX.get().intValue() : 0;
        int zOffset = minZ.get() < 0 ? -minZ.get().intValue() : 0;
        int centerIdx = Math.max(xOffset, zOffset);
        int rangeX = maxX.get() - minX.get();
        int rangeZ = maxZ.get() - minZ.get();
        if (rangeX > 13 || rangeZ > 13) {
            return null;
        }
        int patternSize = Math.max(rangeX, rangeZ) + 1;
        int[][] offsetsPattern = new int[patternSize][];
        for (int i2 = 0; i2 < patternSize; ++i2) {
            offsetsPattern[i2] = new int[patternSize];
        }
        offsets.forEach(bp -> {
            offsetsPattern[centerIdx + bp.getX()][centerIdx + bp.getZ()] = -1;
        });
        if (this.compare2DArrays(this.pattern, offsetsPattern)) {
            return Direction.NORTH;
        }
        int[][] rot90 = this.rotateNumberArrayCW(this.pattern);
        if (this.compare2DArrays(rot90, offsetsPattern)) {
            return Direction.EAST;
        }
        int[][] rot180 = this.rotateNumberArrayCW(rot90);
        if (this.compare2DArrays(rot180, offsetsPattern)) {
            return Direction.SOUTH;
        }
        int[][] rot270 = this.rotateNumberArrayCW(rot180);
        if (this.compare2DArrays(rot270, offsetsPattern)) {
            return Direction.WEST;
        }
        return null;
    }

    public boolean isRuneAtOffset(int x, int y) {
        if (x < 0 || x >= this.RITUAL_SIZE || y < 0 || y >= this.RITUAL_SIZE) {
            return false;
        }
        return this.pattern[x][y] != 0;
    }

    @Nullable
    public RitualReagent getReagentAtOffset(int x, int y) {
        if (x < 0 || x >= this.RITUAL_SIZE || y < 0 || y >= this.RITUAL_SIZE) {
            return null;
        }
        return this.reagents[x][y];
    }

    public Pair<Direction, NonNullList<RitualBlockPos>> matchInWorld(BlockPos center, Level world) {
        if (!this.isValid) {
            return null;
        }
        if (this.matchInWorld(center, world, this.pattern)) {
            return new Pair(Direction.NORTH, this.getBlockPositions(center, this.pattern, this.displayPattern, this.reagents));
        }
        int[][] rot90 = this.rotateNumberArrayCW(this.pattern);
        int[][] dRot90 = this.rotateNumberArrayCW(this.displayPattern);
        RitualReagent[][] rot90rLoc = this.rotateReagentsCW(this.reagents);
        if (this.matchInWorld(center, world, rot90)) {
            return new Pair(Direction.EAST, this.getBlockPositions(center, rot90, dRot90, rot90rLoc));
        }
        int[][] rot180 = this.rotateNumberArrayCW(rot90);
        int[][] dRot180 = this.rotateNumberArrayCW(dRot90);
        RitualReagent[][] rot180rLoc = this.rotateReagentsCW(rot90rLoc);
        if (this.matchInWorld(center, world, rot180)) {
            return new Pair(Direction.SOUTH, this.getBlockPositions(center, rot180, dRot180, rot180rLoc));
        }
        int[][] rot270 = this.rotateNumberArrayCW(rot180);
        int[][] dRot270 = this.rotateNumberArrayCW(dRot180);
        RitualReagent[][] rot270rLoc = this.rotateReagentsCW(rot180rLoc);
        if (this.matchInWorld(center, world, rot270)) {
            return new Pair(Direction.WEST, this.getBlockPositions(center, rot270, dRot270, rot270rLoc));
        }
        return null;
    }

    private int[][] rotateNumberArrayCW(int[][] mat) {
        int M = mat.length;
        int N = mat[0].length;
        int[][] ret = new int[N][M];
        for (int r = 0; r < M; ++r) {
            for (int c = 0; c < N; ++c) {
                ret[c][M - 1 - r] = mat[r][c];
            }
        }
        return ret;
    }

    private RitualReagent[][] rotateReagentsCW(RitualReagent[][] mat) {
        int M = mat.length;
        int N = mat[0].length;
        RitualReagent[][] ret = new RitualReagent[N][M];
        for (int r = 0; r < M; ++r) {
            for (int c = 0; c < N; ++c) {
                ret[c][M - 1 - r] = mat[r][c];
            }
        }
        return ret;
    }

    private boolean compare2DArrays(int[][] a, int[][] b) {
        if (a.length != b.length || !this.validatePatternArray(a) || !this.validateDisplayPatternArray(b)) {
            return false;
        }
        for (int i = 0; i < a.length; ++i) {
            int[] ai = a[i];
            int[] bi = b[i];
            for (int j = 0; j < ai.length; ++j) {
                if (ai[j] != 0 == (bi[j] != 0)) continue;
                return false;
            }
        }
        return true;
    }

    private boolean validatePatternArray(int[][] array) {
        this.RITUAL_SIZE = array.length;
        for (int i = 0; i < array.length; ++i) {
            if (array[i].length == this.RITUAL_SIZE) continue;
            return false;
        }
        return true;
    }

    private boolean validateDisplayPatternArray(int[][] array) {
        if (array.length != this.pattern.length) {
            return false;
        }
        for (int i = 0; i < array.length; ++i) {
            if (array[i].length == this.pattern[i].length) continue;
            return false;
        }
        return true;
    }

    private boolean validateReagentArray(RitualReagent[][] array, int[][] pattern) {
        if (array.length != this.RITUAL_SIZE) {
            return false;
        }
        for (int i = 0; i < array.length; ++i) {
            if (array[i].length == this.RITUAL_SIZE) continue;
            return false;
        }
        boolean dynamicSourceFound = false;
        for (int i = 0; i < array.length; ++i) {
            for (int j = 0; j < array[i].length; ++j) {
                if (array[i][j] == null) continue;
                if (pattern[i][j] == 0) {
                    return false;
                }
                if (!array[i][j].isDynamicSource()) continue;
                if (dynamicSourceFound) {
                    ManaAndArtifice.LOGGER.error("Ritual recipe " + this.getId().toString() + " has more than one dynamic source defined - there can only be one.");
                    this.isValid = false;
                    return false;
                }
                dynamicSourceFound = true;
            }
        }
        return true;
    }

    private boolean matchInWorld(BlockPos center, Level world, int[][] pattern) {
        int bound = this.getLowerBound();
        for (int i = -bound; i <= bound; ++i) {
            for (int j = -bound; j <= bound; ++j) {
                boolean checkValue = world.getBlockState(center.offset(i, 0, j)).getBlock() instanceof ChalkRuneBlock;
                if (!(pattern[i + bound][j + bound] == 0 ? checkValue : !checkValue)) continue;
                return false;
            }
        }
        return true;
    }

    private NonNullList<RitualBlockPos> getBlockPositions(BlockPos center, int[][] pattern, int[][] displayPattern, RitualReagent[][] reagents) {
        NonNullList positions = NonNullList.create();
        int bound = this.getLowerBound();
        for (int i = -bound; i <= bound; ++i) {
            for (int j = -bound; j <= bound; ++j) {
                if (pattern[i + bound][j + bound] == 0) continue;
                positions.add(new RitualBlockPos(pattern[i + bound][j + bound], displayPattern[i + bound][j + bound], new BlockPos(center.getX() + i, center.getY(), center.getZ() + j), reagents[i + bound][j + bound]));
            }
        }
        return positions;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.getId();
    }
}


