 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.events.GenericProgressionEvent;
import com.mna.api.events.ProgressionEventIDs;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.interop.lootr.ILootrBridge;
import com.mna.network.messages.to_client.ShowDidYouKnow;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.manaweaving.ManaweaveCacheEffect;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNAItemData;

public class ManaweaveCacheTile
extends RandomizableContainerBlockEntity
implements ILootrBridge {
    public static final int NUM_PATTERNS = 3;
    public static final int INVENTORY_SIZE = 27;
    private NonNullList<ItemStack> items = NonNullList.withSize((int)27, ItemStack.EMPTY);
    public Set<UUID> openers = new HashSet<UUID>();
    private long resetTime = -1L;
    private boolean isOpen = false;
    private float openPct = 0.0f;
    private boolean isBuff = false;
    private boolean generated = false;
    private int tier = 1;
    private UUID tileId;
    private ResourceLocation lootTableId;
    private long lootTableSeed;
    private ResourceLocation buff;
    private int amplifier = 0;
    private int duration = 1;
    private ResourceLocation[] requiredPatterns = new ResourceLocation[3];
    private ManaweavingPattern[] requiredPatternsCache = new ManaweavingPattern[3];
    private ArrayList<ResourceLocation> addedPatterns = new ArrayList();

    public ManaweaveCacheTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.MANAWEAVE_CACHE.get(), pos, state);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, ManaweaveCacheTile tile) {
        if (!level.isClientSide() && !tile.generated) {
            tile.generate();
        }
        if (!level.isClientSide() && tile.isBuff && tile.shouldReset(state, System.currentTimeMillis())) {
            tile.reset(state);
            return;
        }
        if (tile.isOpen && tile.openPct < 1.0f) {
            tile.openPct += 0.1f;
        } else if (!tile.isOpen && tile.openPct > 0.0f) {
            tile.openPct -= 0.1f;
        }
        tile.openPct = MathUtils.clamp01(tile.openPct);
        if (tile.isOpen && level.isClientSide() && tile.isBuff) {
            Vec3 cp = Vec3.atCenterOf((Vec3i)pos);
            level.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHTNING_BOLT.get())), cp.x, cp.y + 0.675, cp.z, cp.x, cp.y + 1.325, cp.z);
        }
    }

    public boolean notifyPattern(Player player, ResourceLocation pattern) {
        if (this.addedPatterns.size() >= 3) {
            return false;
        }
        if (this.requiredPatterns[this.addedPatterns.size()] == null) {
            return false;
        }
        if (!this.requiredPatterns[this.addedPatterns.size()].equals(pattern)) {
            return false;
        }
        this.addedPatterns.add(pattern);
        this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        if (this.addedPatterns.size() >= 3) {
            this.open();
            NeoForge.EVENT_BUS.post((Event)new GenericProgressionEvent(player, ProgressionEventIDs.OPEN_CACHE));
            if (player instanceof ServerPlayer) {
                CustomAdvancementTriggers.OPEN_CACHE.trigger((ServerPlayer)player, this.tier);
            }
            if (this.isBuff) {
                MobEffect effect = (MobEffect)BuiltInRegistries.MOB_EFFECT.get(this.buff);
                if (effect != null) {
                    player.addEffect(new MobEffectInstance(effect, this.duration, this.amplifier, false, false, true));
                } else {
                    ManaAndArtifice.LOGGER.warn("Effect " + this.buff.toString() + " was not able to be resolved; skipping.");
                }
                ManaAndArtifice.instance.proxy.showDidYouKnow(player, ShowDidYouKnow.Messages.BUFF_CACHE);
            }
        }
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        return true;
    }

    public float openPct() {
        return this.openPct;
    }

    private void open() {
        this.isOpen = true;
        long resetBase = 3600000L;
        this.resetTime = System.currentTimeMillis() + resetBase + (long)((int)(Math.random() * (double)resetBase));
        this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SFX.Event.Block.MANAWEAVE_CACHE_OPEN, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private void reset(BlockState state) {
        this.addedPatterns.clear();
        this.isOpen = false;
        this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SFX.Event.Block.MANAWEAVE_CACHE_OPEN, SoundSource.PLAYERS, 1.0f, 1.0f);
        this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    private boolean shouldReset(BlockState state, long time) {
        return this.isBuff && this.isOpen && System.currentTimeMillis() >= this.resetTime;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public boolean isBuff() {
        return this.isBuff;
    }

    public ManaweavingPattern[] getRequiredPatterns() {
        if (this.generated && this.requiredPatternsCache[0] == null) {
            for (int i = 0; i < 3; ++i) {
                this.requiredPatternsCache[i] = ManaweavingPatternHelper.GetManaweavingRecipe(this.getLevel(), this.requiredPatterns[i]);
            }
        }
        return this.requiredPatternsCache;
    }

    public boolean isPatternAdded(int index) {
        return this.addedPatterns.size() > index;
    }

    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(5.0);
    }

    private void generate() {
        this.tier = (int)(Math.random() * 5.0) + 1;
        List<ResourceLocation> patterns = this.getPatternsForTier();
        if (patterns.size() == 0) {
            this.tier = 0;
            patterns = this.getPatternsForTier();
        }
        for (int i = 0; i < 3; ++i) {
            this.requiredPatterns[i] = patterns.get((int)(Math.random() * (double)patterns.size()));
        }
        boolean bl = this.isBuff = Math.random() < 0.25;
        if (this.isBuff) {
            this.pickEffectForTier();
        }
        if (!this.isBuff) {
            this.setLootTable(RLoc.create("structure/tier_" + this.tier + "_cache"), this.level.random.nextLong());
        }
        this.tileId = UUID.randomUUID();
        this.generated = true;
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    private List<ResourceLocation> getPatternsForTier() {
        return this.getLevel().getRecipeManager().getAllRecipesFor(RecipeInit.MANAWEAVING_PATTERN_TYPE.get()).stream().filter(r -> r.value().getTier() <= this.tier).map(r -> r.id()).collect(Collectors.toList());
    }

    private void pickEffectForTier() {
        List effects = this.getLevel().getRecipeManager().getAllRecipesFor(RecipeInit.MANAWEAVE_CACHE_EFFECT_TYPE.get()).stream().filter(r -> r.value().getTier() <= this.tier).map(r -> r.value()).collect(Collectors.toList());
        if (effects.size() == 0) {
            this.isBuff = false;
            return;
        }
        ManaweaveCacheEffect myEffect = (ManaweaveCacheEffect)effects.get((int)(Math.random() * (double)effects.size()));
        this.buff = myEffect.getEffect();
        this.amplifier = myEffect.getMagnitude();
        this.duration = myEffect.getDurationMin() + (int)((double)(myEffect.getDurationMax() - myEffect.getDurationMin()) * Math.random());
    }

    private CompoundTag getCacheData() {
        int i;
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("generated", this.generated);
        if (!this.generated) {
            return tag;
        }
        tag.putFloat("openPct", this.openPct);
        tag.putBoolean("isOpen", this.isOpen);
        tag.putBoolean("isBuff", this.isBuff);
        tag.putInt("tier", this.tier);
        tag.putString("tileId", this.tileId.toString());
        if (this.lootTableId != null) {
            tag.putString("lootTableId", this.lootTableId.toString());
            tag.putLong("lootTableSeed", this.lootTableSeed);
        }
        if (this.isBuff) {
            tag.putString("buff", this.buff.toString());
            tag.putInt("amplifier", this.amplifier);
            tag.putInt("duration", this.duration);
        }
        for (i = 0; i < 3; ++i) {
            tag.putString("pattern" + i, this.requiredPatterns[i] == null ? "" : this.requiredPatterns[i].toString());
        }
        tag.putInt("numAdded", this.addedPatterns.size());
        for (i = 0; i < this.addedPatterns.size(); ++i) {
            tag.putString("added" + i, this.addedPatterns.get(i).toString());
        }
        ListTag list = new ListTag();
        for (UUID opener : this.openers) {
            list.add(NbtUtils.createUUID((UUID)opener));
        }
        tag.put("LootrOpeners", (Tag)list);
        return tag;
    }

    private void parseCachedData(CompoundTag tag) {
        int i;
        this.generated = tag.getBoolean("generated");
        if (!this.generated) {
            return;
        }
        this.openPct = tag.getFloat("openPct");
        this.isOpen = tag.getBoolean("isOpen");
        this.isBuff = tag.getBoolean("isBuff");
        this.tier = tag.getInt("tier");
        this.tileId = UUID.fromString(tag.getString("tileId"));
        if (tag.contains("lootTableId")) {
            this.lootTableId = ResourceLocation.parse(tag.getString("lootTableId"));
            this.lootTableSeed = tag.getLong("lootTableSeed");
        }
        if (this.isBuff) {
            this.buff = ResourceLocation.parse(tag.getString("buff"));
            this.amplifier = tag.getInt("amplifier");
            this.duration = tag.getInt("duration");
        }
        for (i = 0; i < 3; ++i) {
            this.requiredPatterns[i] = ResourceLocation.parse(tag.getString("pattern" + i));
        }
        this.addedPatterns.clear();
        for (i = 0; i < tag.getInt("numAdded"); ++i) {
            this.addedPatterns.add(ResourceLocation.parse(tag.getString("added" + i)));
        }
        if (tag.contains("LootrOpeners")) {
            ListTag openers = tag.getList("LootrOpeners", 11);
            this.openers.clear();
            for (Tag item : openers) {
                this.openers.add(NbtUtils.loadUUID((Tag)item));
            }
        }
    }

    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.put("cache_data", (Tag)this.getCacheData());
        if (!this.trySaveLootTable(compound)) {
            MNAItemData.saveItems((CompoundTag)compound, this.items);
        }
    }

    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        this.parseCachedData(compound.getCompound("cache_data"));
        if (!this.tryLoadLootTable(compound)) {
            MNAItemData.loadItems((CompoundTag)compound, this.items);
        }
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.getCacheData();
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void handleUpdateTag(CompoundTag tag) {
        this.parseCachedData(tag);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            this.parseCachedData(pkt.getTag());
        }
    }

    public int getContainerSize() {
        return 27;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    protected Component getDefaultName() {
        return Component.translatable((String)"mna:container.manaweave_cache");
    }

    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows((int)id, (Inventory)inventory, (Container)this);
    }

    @Override
    public UUID getTileId() {
        return this.tileId;
    }

    public void setLootTable(ResourceLocation lootTableIn, long seedIn) {
        this.lootTableId = lootTableIn;
        this.lootTableSeed = seedIn;
        super.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, lootTableIn), seedIn);
    }

    @Override
    public long getLootSeed() {
        return this.getLevel().getRandom().nextLong();
    }

    @Override
    public ResourceLocation getLootTable() {
        return this.lootTableId;
    }

    @Override
    public void setLootrOpened(boolean opened) {
    }

    @Override
    public Set<UUID> getOpeners() {
        return this.openers;
    }

    @Override
    public void updatePacketViaState() {
    }
}


