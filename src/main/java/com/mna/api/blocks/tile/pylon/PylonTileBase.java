 
package com.mna.api.blocks.tile.pylon;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.PlayerOwnershipRecord;
import com.mna.api.blocks.PylonBlock;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.blocks.tile.IPowerRequestStatus;
import com.mna.api.blocks.tile.PowerStatus;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.faction.IFaction;
import com.mna.api.gui.ContainerPylon;
import com.mna.api.items.IPositionalItem;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;
import com.mna.api.tools.MNAItemData;

public abstract class PylonTileBase
extends TileEntityWithInventory
implements IEldrinConsumerTile,
IPowerRequestStatus,
MenuProvider {
    public static final HashMap<Character, Integer> lookup = new HashMap();
    public static final int DEFAULT_MAX_BLOCKS_VISITED_PER_TICK = 50;
    public static final int DEFAULT_INVENTORY_SIZE = 2;
    public static final int MAX_RADIUS = 128;
    public static final int MIN_RADIUS = 1;
    protected final int powerUpTicksRequired = 60;
    private AABB currentAffectedArea;
    private PlayerOwnershipRecord owner;
    private BlockPos lastSearchPos;
    private HashMap<Affinity, Float> consumptionAccumulation;
    private HashMap<Affinity, PowerStatus> consumptionStatus;
    private int[] runeword = new int[0];
    private int clearHeightAbove = -1;
    private boolean factionOnly = false;
    protected int powerUpTicks = 0;
    protected float powerMultiplier = 1.0f;
    protected boolean active = false;
    protected boolean isRedstonePowered;
    protected int radius = 32;
    protected int[] color;
    protected boolean mapMakerMode = false;
    protected String errorMessage = null;

    public PylonTileBase(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState, 2);
        this.consumptionAccumulation = new HashMap();
        this.consumptionStatus = new HashMap();
        for (Affinity aff : Affinity.CoreSix()) {
            this.consumptionAccumulation.put(aff, Float.valueOf(0.0f));
            this.consumptionStatus.put(aff, PowerStatus.NOT_REQUESTING);
        }
        this.owner = PlayerOwnershipRecord.of((Player)null);
    }

    public AABB getBounds() {
        if (this.currentAffectedArea == null) {
            this.lastSearchPos = null;
            AABB runeBounds = this.getBoundsFromRunes();
            this.currentAffectedArea = runeBounds == null ? new AABB(this.getBlockPos()).inflate((double)this.radius) : runeBounds;
            double avgSize = this.currentAffectedArea.getSize() / 2.0;
            int factor = (int)Math.floor(avgSize / 16.0);
            this.powerMultiplier = Math.max(0.5f * (float)factor, 0.5f);
            this.onBoundsChanged();
            this.syncAndSave();
        }
        return this.currentAffectedArea;
    }

    public int getClearHeightAbove() {
        return this.clearHeightAbove;
    }

    @Nullable
    public int[] getColor() {
        return this.color;
    }

    @Nullable
    public String getErrorMessage() {
        return this.errorMessage;
    }

    public final int[] getRuneword() {
        return this.runeword;
    }

    protected final PlayerOwnershipRecord getOwnershipData() {
        return this.owner;
    }

    protected PylonBlock getBlock() {
        return (PylonBlock)this.getBlockState().getBlock();
    }

    protected HashMap<Affinity, Float> getPowerConsumption() {
        HashMap<Affinity, Float> basePowerConsumption = this.getBlock().getPowerConsumption();
        HashMap<Affinity, Float> multipliedPowerConsumption = new HashMap<Affinity, Float>();
        for (Map.Entry<Affinity, Float> entry : basePowerConsumption.entrySet()) {
            multipliedPowerConsumption.put(entry.getKey(), Float.valueOf(entry.getValue().floatValue() * this.powerMultiplier));
        }
        return multipliedPowerConsumption;
    }

    public float getPowerRequestRate(Affinity aff) {
        return this.getBlock().getPowerConsumption().getOrDefault(aff, Float.valueOf(0.0f)).floatValue() * this.powerMultiplier * 20.0f;
    }

    public float getPowerMultiplier() {
        return this.powerMultiplier;
    }

    public int getRadius() {
        return this.radius;
    }

    @Nullable
    private AABB getBoundsFromRunes() {
        this.errorMessage = null;
        Item item = this.getItem(0).getItem();
        if (item instanceof IPositionalItem) {
            IPositionalItem p1 = (IPositionalItem)item;
            item = this.getItem(1).getItem();
            if (item instanceof IPositionalItem) {
                IPositionalItem p2 = (IPositionalItem)item;
                BlockPos a = p1.getLocation(this.getItem(0));
                BlockPos b = p2.getLocation(this.getItem(1));
                if (a != null && b != null) {
                    AABB runeBounds = MathUtils.createInclusiveBB(a, b);
                    if (!runeBounds.contains(Vec3.atCenterOf((Vec3i)this.getBlockPos()))) {
                        this.errorMessage = "gui.mna.pylon.not_in_bounds";
                    } else if (runeBounds.getXsize() < 1.0 || runeBounds.getYsize() < 1.0 || runeBounds.getZsize() < 1.0) {
                        this.errorMessage = "gui.mna.pylon.bounds_too_small";
                    } else if (runeBounds.getXsize() > 128.0 || runeBounds.getYsize() > 128.0 || runeBounds.getZsize() > 128.0) {
                        this.errorMessage = "gui.mna.pylon.bounds_too_large";
                    } else {
                        return runeBounds;
                    }
                }
            }
        }
        return null;
    }

    public boolean isRadiusUsed() {
        return this.getBoundsFromRunes() == null;
    }

    public float getPowerupPct(float partialTick) {
        float f = this.powerUpTicks;
        Objects.requireNonNull(this);
        return f / 60.0f;
    }

    public final boolean isRedstonePowered() {
        return this.isRedstonePowered;
    }

    public final boolean isFactionOnly() {
        return this.factionOnly;
    }

    public abstract ResourceLocation getIcon();

    public String getFactionCheckboxTooltip() {
        return "gui.mna.pylon.faction_only";
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).expandTowards(0.0, (double)this.clearHeightAbove, 0.0);
    }

    public HashMap<Affinity, PowerStatus> powerRequirementStatus() {
        return this.consumptionStatus;
    }

    public void setOwner(Player owner) {
        this.owner = PlayerOwnershipRecord.of(owner);
    }

    public final void setRuneword(String runeword) {
        runeword = runeword.toLowerCase();
        ArrayList<Integer> runes = new ArrayList<Integer>();
        for (int i = 0; i < runeword.length(); ++i) {
            Integer val = lookup.get(Character.valueOf(runeword.charAt(i)));
            if (val != null) {
                runes.add(val);
                continue;
            }
            runes.add(-1);
        }
        int count = 0;
        this.runeword = new int[runes.size()];
        Iterator iterator = runes.iterator();
        while (iterator.hasNext()) {
            int i = (Integer)iterator.next();
            this.runeword[count++] = i;
        }
    }

    public void setColor(int[] color) {
        if (color != null && this.color != null && color[0] == this.color[0] && color[1] == this.color[1] && color[2] == this.color[2]) {
            this.color = null;
            this.syncAndSave();
            return;
        }
        this.color = color;
        this.syncAndSave();
    }

    public void setRadius(int radius) {
        if (this.radius != (radius = Math.min(Math.max(radius, 1), 128))) {
            this.radius = radius;
            if (this.isRadiusUsed()) {
                this.recalculateBounds();
            }
            if (!this.level.isClientSide) {
                this.syncAndSave();
            }
        }
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        this.recalculateBounds();
    }

    public void setMapMakerMode() {
        this.mapMakerMode = true;
        for (Affinity key : this.consumptionStatus.keySet()) {
            this.consumptionStatus.put(key, PowerStatus.NOT_REQUESTING);
        }
        this.owner = PlayerOwnershipRecord.of((Player)null);
    }

    protected void recalculateClearHeightAbove() {
        int count = 0;
        BlockPos bp = this.getBlockPos().above(3);
        while (count++ < 15 && this.level.getBlockState(bp).isAir()) {
            bp = bp.above();
        }
        this.clearHeightAbove = count;
    }

    protected boolean tick() {
        boolean wasRedstonePowered = this.isRedstonePowered;
        boolean bl = this.isRedstonePowered = this.level.getBestNeighborSignal(this.getBlockPos()) > 0;
        if (!wasRedstonePowered && this.isRedstonePowered) {
            for (Affinity key : this.consumptionStatus.keySet()) {
                this.consumptionStatus.put(key, PowerStatus.NOT_REQUESTING);
            }
        }
        if (this.level.isClientSide()) {
            if (this.clearHeightAbove == -1 || this.level.getGameTime() % 200L == 0L) {
                this.recalculateClearHeightAbove();
            }
            return false;
        }
        if (this.isRedstonePowered) {
            if (this.powerUpTicks > 0) {
                this.active = false;
                --this.powerUpTicks;
                this.syncAndSave();
            }
            return false;
        }
        if (this.mapMakerMode) {
            this.active = true;
            if (this.powerUpTicks < this.powerUpTicksRequired) {
                ++this.powerUpTicks;
                this.syncAndSave();
            }
            return true;
        }
        if (this.owner == null) {
            return false;
        }
        BlockPos bp = this.getBlockPos();
        Vec3 centerVec = Vec3.atCenterOf((Vec3i)bp);
        boolean fullConsume = true;
        boolean needsSync = false;
        HashMap<Affinity, Float> powerRequired = this.getPowerConsumption();
        for (Map.Entry<Affinity, Float> entry : powerRequired.entrySet()) {
            if (this.consumptionAccumulation.get(entry.getKey()).floatValue() >= entry.getValue().floatValue()) continue;
            float consumed = this.consume(this.owner, this.getLevel(), bp, centerVec, entry.getKey(), entry.getValue().floatValue());
            float currentConsumption = this.consumptionAccumulation.get(entry.getKey()).floatValue();
            float newConsumption = currentConsumption + consumed;
            if (consumed == 0.0f) {
                newConsumption = 0.0f;
            }
            this.consumptionAccumulation.put(entry.getKey(), Float.valueOf(newConsumption));
            PowerStatus consumptionStat = this.calculatePowerStatus(entry.getValue().floatValue(), newConsumption);
            if (this.consumptionStatus.get(entry.getKey()) != consumptionStat) {
                this.consumptionStatus.put(entry.getKey(), consumptionStat);
                needsSync = true;
            }
            if (!(newConsumption < entry.getValue().floatValue())) continue;
            fullConsume = false;
        }
        if (fullConsume) {
            for (Map.Entry<Affinity, Float> entry : powerRequired.entrySet()) {
                float current = this.consumptionAccumulation.get(entry.getKey()).floatValue();
                if ((current -= entry.getValue().floatValue()) < 0.0f) {
                    current = 0.0f;
                }
                this.consumptionAccumulation.put(entry.getKey(), Float.valueOf(current));
            }
            this.active = true;
            if (this.powerUpTicks < this.powerUpTicksRequired) {
                ++this.powerUpTicks;
                needsSync = true;
            }
        } else if (this.powerUpTicks > 0) {
            this.active = false;
            --this.powerUpTicks;
            needsSync = true;
        }
        if (needsSync) {
            this.syncAndSave();
        }
        return fullConsume;
    }

    protected void forAllPlayers(boolean requireLoS, Consumer<Player> consumer) {
        Vec3 origin = Vec3.atCenterOf((Vec3i)this.getBlockPos());
        AABB bounds = this.getBounds();
        this.level.players().stream().filter(p -> {
            Vec3 pPos;
            Vec3 originOffset;
            ClipContext cc;
            BlockHitResult bhr;
            if (!bounds.intersects(p.getBoundingBox())) {
                return false;
            }
            if (requireLoS && (bhr = this.level.clip(cc = new ClipContext(originOffset = origin.add((pPos = p.getEyePosition()).subtract(origin).normalize()), pPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, (Entity)p))).getType() != HitResult.Type.MISS) {
                return false;
            }
            if (this.isFactionOnly()) {
                return this.FactionMatchTest((Entity)p);
            }
            return true;
        }).forEach(consumer);
    }

    protected boolean forAllBlocks(int maxBlocksCheckedPerTick, BiConsumer<BlockPos, BlockState> consumer) {
        if (consumer == null) {
            return true;
        }
        AABB searchArea = this.getBounds();
        if (searchArea == null) {
            return true;
        }
        if (this.lastSearchPos == null) {
            this.lastSearchPos = new BlockPos((int)searchArea.minX, (int)searchArea.minY, (int)searchArea.minZ);
        }
        int x = this.lastSearchPos.getX();
        int y = this.lastSearchPos.getY();
        int z = this.lastSearchPos.getZ();
        int count = 0;
        while ((double)x <= searchArea.maxX) {
            while ((double)y <= searchArea.maxY) {
                while ((double)z <= searchArea.maxZ) {
                    BlockPos curSearch = new BlockPos(x, y, z);
                    if (!curSearch.equals(this.getBlockPos())) {
                        if (this.getLevel().isLoaded(curSearch)) {
                            consumer.accept(curSearch, this.level.getBlockState(curSearch));
                        }
                        if (++count >= maxBlocksCheckedPerTick) {
                            this.lastSearchPos = new BlockPos(x, y, z);
                            return false;
                        }
                    }
                    ++z;
                }
                z = (int)searchArea.minZ;
                ++y;
            }
            y = (int)searchArea.minY;
            ++x;
        }
        this.lastSearchPos = null;
        return true;
    }

    protected <T extends Entity> void forAllEntities(EntityTypeTest<Entity, T> test, boolean requireLoS, Consumer<T> consumer) {
        Vec3 origin = Vec3.atCenterOf((Vec3i)this.getBlockPos());
        AABB bounds = this.getBounds().inflate((double)this.getRadius());
        this.level.getEntities(test, bounds, e -> {
            Vec3 pPos;
            Vec3 originOffset;
            ClipContext cc;
            BlockHitResult bhr;
            if (requireLoS && (bhr = this.level.clip(cc = new ClipContext(originOffset = origin.add((pPos = e.getEyePosition()).subtract(origin).normalize()), pPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, e))).getType() != HitResult.Type.MISS) {
                return false;
            }
            if (this.isFactionOnly()) {
                return this.FactionMatchTest((Entity)e);
            }
            return true;
        }).forEach(consumer);
    }

    protected <T extends Entity> void forAllEntities(Class<T> clazz, boolean requireLoS, Consumer<T> consumer) {
        Vec3 origin = Vec3.atCenterOf((Vec3i)this.getBlockPos());
        AABB bounds = this.getBounds().inflate((double)this.getRadius());
        this.level.getEntitiesOfClass(clazz, bounds, e -> {
            Vec3 pPos;
            Vec3 originOffset;
            ClipContext cc;
            BlockHitResult bhr;
            if (requireLoS && (bhr = this.level.clip(cc = new ClipContext(originOffset = origin.add((pPos = e.getEyePosition()).subtract(origin).normalize()), pPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, e))).getType() != HitResult.Type.MISS) {
                return false;
            }
            if (this.isFactionOnly()) {
                return this.FactionMatchTest((Entity)e);
            }
            return true;
        }).forEach(consumer);
    }

    protected boolean FactionMatchTest(Entity e) {
        IFaction tileFaction = this.getOwnershipData().getFaction();
        if (tileFaction == null) {
            return false;
        }
        if (e instanceof Player) {
            IPlayerProgression progression = (IPlayerProgression)MNACap.get(e, ManaAndArtificeMod.getProgressionCapability()).orElse(null);
            if (progression == null) {
                return false;
            }
            IFaction playerFaction = progression.getAlliedFaction();
            if (playerFaction != null) {
                return playerFaction.isAlliedTo(tileFaction);
            }
        } else {
            if (e instanceof IConstruct) {
                IConstruct c = (IConstruct)e;
                return c.getFaction() != null && c.getFaction().isAlliedTo(tileFaction);
            }
            if (e instanceof IFactionEnemy) {
                IFactionEnemy f = (IFactionEnemy)e;
                return f.getFaction() != null && f.getFaction().isAlliedTo(tileFaction);
            }
        }
        return false;
    }

    public final void setFactionOnly(boolean factionOnly) {
        if (this.factionOnly != factionOnly) {
            this.factionOnly = factionOnly;
            if (!this.level.isClientSide) {
                this.syncAndSave();
            }
            this.onFactionOnlyChanged();
        }
    }

    private final void recalculateBounds() {
        this.currentAffectedArea = null;
        this.currentAffectedArea = this.getBounds();
    }

    protected void onBoundsChanged() {
    }

    protected void onFactionOnlyChanged() {
    }

    public final void syncAndSave() {
        if (!this.getLevel().isClientSide()) {
            this.setChanged();
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this, () -> {
            CompoundTag tag = new CompoundTag();
            tag.putInt("powerLevel", this.powerUpTicks);
            tag.putInt("radius", this.radius);
            tag.putFloat("powerMult", this.powerMultiplier);
            tag.putBoolean("active", this.active);
            tag.putBoolean("factionOnly", this.factionOnly);
            if (this.errorMessage != null) {
                tag.putString("errorMessage", this.errorMessage);
            }
            if (this.color != null) {
                tag.putIntArray("color", this.color);
            }
            this.writePowerConsumeStatus(tag, this.consumptionStatus);
            return tag;
        });
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        base.putInt("powerLevel", this.powerUpTicks);
        base.putInt("radius", this.radius);
        base.putFloat("powerMult", this.powerMultiplier);
        base.putBoolean("active", this.active);
        base.putBoolean("factionOnly", this.factionOnly);
        base.putIntArray("runeword", this.runeword);
        if (this.errorMessage != null) {
            base.putString("errorMessage", this.errorMessage);
        }
        if (this.color != null) {
            base.putIntArray("color", this.color);
        }
        this.writePowerConsumeStatus(base, this.consumptionStatus);
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        this.powerUpTicks = tag.getInt("powerLevel");
        this.active = tag.getBoolean("active");
        this.powerMultiplier = tag.getFloat("powerMult");
        this.factionOnly = tag.getBoolean("factionOnly");
        this.runeword = tag.getIntArray("runeword");
        this.radius = tag.getInt("radius");
        this.color = (int[])(tag.contains("color") ? tag.getIntArray("color") : null);
        this.errorMessage = tag.contains("errorMessage") ? tag.getString("errorMessage") : null;
        this.readPowerConsumeStatusList(tag, this.consumptionStatus);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        this.powerUpTicks = tag.getInt("powerLevel");
        this.powerMultiplier = tag.getFloat("powerMult");
        this.active = tag.getBoolean("active");
        this.radius = tag.getInt("radius");
        this.factionOnly = tag.getBoolean("factionOnly");
        this.color = (int[])(tag.contains("color") ? tag.getIntArray("color") : null);
        this.errorMessage = tag.contains("errorMessage") ? tag.getString("errorMessage") : null;
        this.readPowerConsumeStatusList(tag, this.consumptionStatus);
    }

    @Override
    public void saveAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.put("owner_data", (Tag)this.owner.save(this.getLevel()));
        pTag.putIntArray("runeword", this.runeword);
        pTag.putInt("radius", this.radius);
        pTag.putInt("powerLevel", this.powerUpTicks);
        pTag.putBoolean("active", this.active);
        pTag.putBoolean("factionOnly", this.factionOnly);
        if (this.color != null) {
            pTag.putIntArray("color", this.color);
        }
        pTag.putBoolean("mapMakerMode", this.mapMakerMode);
    }

    @Override
    public void loadAdditional(CompoundTag pTag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        this.owner = PlayerOwnershipRecord.of(pTag.getCompound("owner_data"));
        this.runeword = pTag.getIntArray("runeword");
        this.powerUpTicks = pTag.getInt("powerLevel");
        this.active = pTag.getBoolean("active");
        this.factionOnly = pTag.getBoolean("factionOnly");
        this.radius = pTag.getInt("radius");
        if (this.color != null) {
            this.color = pTag.getIntArray("color");
        }
        this.mapMakerMode = pTag.getBoolean("mapMakerMode");
        if (this.mapMakerMode) {
            for (Affinity key : this.consumptionStatus.keySet()) {
                this.consumptionStatus.put(key, PowerStatus.NOT_REQUESTING);
            }
        }
    }

    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ContainerPylon(pContainerId, pPlayerInventory, this);
    }

    public Component getDisplayName() {
        return Component.literal((String)"");
    }

    static {
        lookup.put(Character.valueOf(' '), -1);
        lookup.put(Character.valueOf('a'), 0);
        lookup.put(Character.valueOf('b'), 1);
        lookup.put(Character.valueOf('c'), 2);
        lookup.put(Character.valueOf('d'), 17);
        lookup.put(Character.valueOf('e'), 4);
        lookup.put(Character.valueOf('f'), 5);
        lookup.put(Character.valueOf('g'), 6);
        lookup.put(Character.valueOf('h'), 7);
        lookup.put(Character.valueOf('i'), 8);
        lookup.put(Character.valueOf('j'), 22);
        lookup.put(Character.valueOf('k'), 10);
        lookup.put(Character.valueOf('l'), 11);
        lookup.put(Character.valueOf('m'), 12);
        lookup.put(Character.valueOf('n'), 28);
        lookup.put(Character.valueOf('o'), 14);
        lookup.put(Character.valueOf('p'), 15);
        lookup.put(Character.valueOf('q'), 31);
        lookup.put(Character.valueOf('r'), 3);
        lookup.put(Character.valueOf('s'), 18);
        lookup.put(Character.valueOf('t'), 9);
        lookup.put(Character.valueOf('u'), 30);
        lookup.put(Character.valueOf('v'), 3);
        lookup.put(Character.valueOf('w'), 19);
        lookup.put(Character.valueOf('x'), 13);
        lookup.put(Character.valueOf('y'), 24);
        lookup.put(Character.valueOf('z'), 29);
    }
}

