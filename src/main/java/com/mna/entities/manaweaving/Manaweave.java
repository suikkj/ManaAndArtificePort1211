 
package com.mna.entities.manaweaving;

import com.mna.Registries;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.blocks.IManaweaveNotifiable;
import com.mna.api.cantrips.ICantrip;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.events.EventDispatcher;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ItemManaweaveBottle;
import com.mna.network.ServerMessageDispatcher;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import com.mna.tools.EntityUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

public class Manaweave
extends Entity {
    private static final int MAX_AGE = 200;
    private static final EntityDataAccessor<CompoundTag> PATTERNS = SynchedEntityData.defineId(Manaweave.class, (EntityDataSerializer)EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(Manaweave.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> CASTER_PLAYER_ID = SynchedEntityData.defineId(Manaweave.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> CASTER_TIER = SynchedEntityData.defineId(Manaweave.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> CASTER_FACTION_ID = SynchedEntityData.defineId(Manaweave.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private static final String NBT_ENTITY_AGE = "entity_age";
    private static final String NBT_PATTERN = "patterns";
    private static final String NBT_CASTER_ID = "caster_id";
    private static final String NBT_CASTER_UUID = "caster_uuid";
    private static final String NBT_CASTER_TIER = "caster_tier";
    private static final String NBT_CASTER_FACTION = "caster_faction";
    private static final String MANUALLY_DRAWN = "manually_drawn";
    private static final String MANA_REFUNDED = "mana_refunded";
    private static final String NBT_HAND = "hand";
    int age = 0;
    boolean isMerging = false;
    private ArrayList<IManaweavePattern> cachedPatterns;
    private InteractionHand hand;
    private boolean manuallyDrawn = false;
    private float manaRefunded = 0.0f;

    public Manaweave(EntityType<? extends Entity> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public Manaweave(Level world) {
        this((EntityType<? extends Entity>)((EntityType)EntityInit.MANAWEAVE_ENTITY.get()), world);
    }

    public void tick() {
        if (!(this.getPatterns() != null && this.getPatterns().size() != 0 || this.level().isClientSide())) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
        if (this.age == 0 && !this.level().isClientSide() && !this.notifyNearbyBlocksAltars()) {
            this.mergeWithNearby(this.hand);
        }
        ++this.age;
        if (this.age >= 200 && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void onRemovedFromLevel() {
        if (this.level().isClientSide()) {
            for (int i = 0; i < 50; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())), this.getX(), this.getY(), this.getZ(), -0.25 + Math.random() * 0.5, -0.25 + Math.random() * 0.5, -0.25 + Math.random() * 0.5);
            }
        }
        super.onRemovedFromLevel();
    }

    public void onAddedToLevel() {
        LivingEntity caster;
        super.onAddedToLevel();
        if (!this.level().isClientSide() && (caster = this.getCaster()) != null && caster instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)caster;
            if (this.getPatterns().size() == 1) {
                CustomAdvancementTriggers.DRAW_MANAWEAVE.trigger(player, this.getPatterns().get(0).getRegistryId());
            }
        }
    }

    public boolean isMerging() {
        return this.isMerging;
    }

    public void setMerging(boolean merging) {
        this.isMerging = merging;
    }

    public float getManaReturn(Player player) {
        if (this.manuallyDrawn) {
            return this.manaRefunded;
        }
        return 0.0f;
    }

    public boolean isAttackable() {
        return true;
    }

    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    public boolean isPickable() {
        return true;
    }

    public boolean isPushable() {
        return false;
    }

    public void setPattern(ResourceLocation pattern) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("count", 0);
        nbt.putString("index_0", pattern.toString());
        this.entityData.set(PATTERNS, nbt);
        this.cachedPatterns = null;
    }

    public void addPattern(ResourceLocation pattern) {
        CompoundTag nbt = ((CompoundTag)this.entityData.get(PATTERNS)).copy();
        int count = nbt.getInt("count") + 1;
        nbt.putInt("count", count);
        nbt.putString("index_" + count, pattern.toString());
        this.entityData.set(PATTERNS, nbt);
        this.cachedPatterns = null;
    }

    @Nullable
    public LivingEntity getCaster() {
        Entity e;
        Level level;
        Optional casterUUID = (Optional)this.entityData.get(CASTER_PLAYER_ID);
        if (casterUUID.isPresent() && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Player serverPlayer = serverLevel.getPlayerByUUID((UUID)casterUUID.get());
            if (serverPlayer != null) {
                return serverPlayer;
            }
            FakePlayer fakePlayer = EntityUtil.getFakePlayer(serverLevel, (UUID)casterUUID.get());
            IPlayerProgression p = (IPlayerProgression)fakePlayer.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (p != null) {
                p.setTier((Integer)this.entityData.get(CASTER_TIER), null);
                IFaction faction = (IFaction)Registries.Factions.getValue(ResourceLocation.parse((String)this.entityData.get(CASTER_FACTION_ID)));
                p.setAlliedFaction(faction, null);
            }
            return fakePlayer;
        }
        int id = (Integer)this.entityData.get(CASTER_ID);
        if (id > -1 && (e = this.level().getEntity(id)) instanceof LivingEntity) {
            return (LivingEntity)e;
        }
        return null;
    }

    public void setCaster(LivingEntity caster, InteractionHand hand) {
        if (caster != null) {
            this.entityData.set(CASTER_ID, caster.getId());
            this.hand = hand;
            if (caster instanceof Player) {
                Player player = (Player)caster;
                this.entityData.set(CASTER_PLAYER_ID, Optional.of(player.getGameProfile().getId()));
                IPlayerProgression p = (IPlayerProgression)caster.getData(PlayerProgressionProvider.PROGRESSION.get());
                if (p != null) {
                    this.entityData.set(CASTER_TIER, p.getTier());
                    if (p.hasAlliedFaction()) {
                        this.entityData.set(CASTER_FACTION_ID, Registries.Factions.getKey(p.getAlliedFaction()).toString());
                    }
                }
            } else {
                this.entityData.set(CASTER_PLAYER_ID, Optional.empty());
            }
        }
    }

    public void setManuallyDrawn() {
        this.manuallyDrawn = true;
    }

    public void setManaRefunded(int drawTicks) {
        this.manaRefunded = (float)drawTicks * 1.0f;
    }

    public void mergeWith(Manaweave other) {
        ArrayList<IManaweavePattern> otherPatterns = other.getPatterns();
        for (int i = otherPatterns.size() - 1; i >= 0; --i) {
            this.addPattern(otherPatterns.get(i).getRegistryId());
        }
        this.manuallyDrawn |= other.manuallyDrawn;
        this.manaRefunded += other.manaRefunded;
        other.remove(Entity.RemovalReason.DISCARDED);
    }

    private void mergeWithNearby(InteractionHand hand) {
        if (this.getCaster() == null) {
            return;
        }
        List mws = this.level().getEntities((Entity)this, this.getBoundingBox().inflate(2.0), e -> e instanceof Manaweave && ((Manaweave)(e)).getCaster() == this.getCaster());
        for (Entity e2 : mws) {
            this.mergeWith((Manaweave)e2);
        }
        if (this.getPatterns().size() > 1) {
            this.checkMergeRecipe(hand);
        }
    }

    private void checkMergeRecipe(InteractionHand hand) {
        LivingEntity caster = this.getCaster();
        if (caster == null || !(caster instanceof Player)) {
            return;
        }
        Player player = (Player)caster;
        IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
        if (magic == null) {
            return;
        }
        if (caster.distanceToSqr((Entity)this) > 25.0) {
            return;
        }
        ICantrip cantrip = magic.getCantripData().matchAndCastCantrip(player, hand, this.getPatterns().stream().map(r -> (Recipe)r).collect(Collectors.toList()));
        if (cantrip != null) {
            EventDispatcher.DispatchCantripCast(cantrip, player);
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                ServerMessageDispatcher.sendCantripTimerMessage(cantrip.getId().toString(), cantrip.getDelay(), serverPlayer);
                CustomAdvancementTriggers.CANTRIP_CAST.trigger(serverPlayer, cantrip.getId(), cantrip.getTier());
            }
            if (cantrip.getSound() != null) {
                this.level().playSound(null, this.blockPosition(), cantrip.getSound(), SoundSource.PLAYERS, 0.5f, 1.0f);
            }
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Nullable
    public ArrayList<IManaweavePattern> getPatterns() {
        CompoundTag nbt;
        if (this.cachedPatterns == null && (nbt = (CompoundTag)this.entityData.get(PATTERNS)).contains("count")) {
            this.cachedPatterns = new ArrayList();
            int count = nbt.getInt("count");
            for (int i = 0; i <= count; ++i) {
                String key = "index_" + i;
                if (!nbt.contains(key)) continue;
                String rLoc = nbt.getString(key);
                ManaweavingPattern mwp = ManaweavingPatternHelper.GetManaweavingRecipe(this.level(), ResourceLocation.parse(rLoc));
                if (mwp == null) continue;
                this.cachedPatterns.add(0, mwp);
            }
        }
        return this.cachedPatterns;
    }

    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() == Items.GLASS_BOTTLE && this.getPatterns().size() == 1) {
            if (!this.level().isClientSide()) {
                ItemStack captureStack = new ItemStack((ItemLike)ItemInit.MANAWEAVE_BOTTLE.get());
                ItemManaweaveBottle.setPattern(this.getPatterns().get(0), captureStack);
                player.getItemInHand(hand).shrink(1);
                if (!player.addItem(captureStack)) {
                    player.spawnAtLocation(captureStack);
                }
                this.remove(Entity.RemovalReason.DISCARDED);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(PATTERNS, new CompoundTag());
        builder.define(CASTER_ID, -1);
        builder.define(CASTER_PLAYER_ID, Optional.empty());
        builder.define(CASTER_TIER, 0);
        builder.define(CASTER_FACTION_ID, "mna:none");
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains(NBT_ENTITY_AGE)) {
            this.age = compound.getInt(NBT_ENTITY_AGE);
        }
        if (compound.contains(NBT_PATTERN)) {
            this.entityData.set(PATTERNS, ((CompoundTag)compound.get(NBT_PATTERN)));
        }
        if (compound.contains(NBT_CASTER_ID)) {
            this.entityData.set(CASTER_ID, compound.getInt(NBT_CASTER_ID));
        }
        if (compound.contains(NBT_HAND)) {
            this.hand = InteractionHand.values()[compound.getInt(NBT_HAND)];
        }
        if (compound.contains(MANUALLY_DRAWN)) {
            this.manuallyDrawn = compound.getBoolean(MANUALLY_DRAWN);
        }
        if (compound.contains(MANA_REFUNDED)) {
            this.manaRefunded = compound.getFloat(MANA_REFUNDED);
        }
        if (compound.contains(NBT_CASTER_UUID)) {
            this.entityData.set(CASTER_PLAYER_ID, Optional.of(compound.getUUID(NBT_CASTER_UUID)));
        }
        if (compound.contains(NBT_CASTER_TIER)) {
            this.entityData.set(CASTER_TIER, compound.getInt(NBT_CASTER_TIER));
        }
        if (compound.contains(NBT_CASTER_FACTION)) {
            this.entityData.set(CASTER_FACTION_ID, compound.getString(NBT_CASTER_FACTION));
        }
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt(NBT_ENTITY_AGE, this.age);
        compound.put(NBT_PATTERN, (Tag)this.entityData.get(PATTERNS));
        compound.putInt(NBT_CASTER_ID, ((Integer)this.entityData.get(CASTER_ID)).intValue());
        compound.putInt(NBT_HAND, this.hand != null ? this.hand.ordinal() : 0);
        compound.putBoolean(MANUALLY_DRAWN, this.manuallyDrawn);
        compound.putFloat(MANA_REFUNDED, this.manaRefunded);
        compound.putInt(NBT_CASTER_TIER, ((Integer)this.entityData.get(CASTER_TIER)).intValue());
        compound.putString(NBT_CASTER_FACTION, (String)this.entityData.get(CASTER_FACTION_ID));
        Optional casterUUID = (Optional)this.entityData.get(CASTER_PLAYER_ID);
        if (casterUUID.isPresent()) {
            compound.putUUID(NBT_CASTER_UUID, (UUID)casterUUID.get());
        }
    }

    public int getAge() {
        return this.age;
    }

    private boolean notifyNearbyBlocksAltars() {
        ArrayList<IManaweavePattern> p = this.getPatterns();
        if (p == null || p.size() != 1) {
            return false;
        }
        LivingEntity caster = this.getCaster();
        BlockPos me = this.blockPosition();
        int radius = 3;
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                for (int k = -radius; k <= radius; ++k) {
                    IManaweaveNotifiable notifiable;
                    IManaweaveNotifiable notifiable2;
                    BlockPos test = me.offset(i, j, k);
                    BlockState state = this.level().getBlockState(test);
                    Block block = state.getBlock();
                    if (block instanceof IManaweaveNotifiable && (notifiable2 = (IManaweaveNotifiable)block).notify(this.level(), test, state, p, caster)) {
                        this.remove(Entity.RemovalReason.DISCARDED);
                        return true;
                    }
                    BlockEntity be = this.level().getBlockEntity(test);
                    if (be == null || !(be instanceof IManaweaveNotifiable) || !(notifiable = (IManaweaveNotifiable)be).notify(this.level(), test, state, p, caster)) continue;
                    this.remove(Entity.RemovalReason.DISCARDED);
                    return true;
                }
            }
        }
        return false;
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (key == PATTERNS) {
            this.cachedPatterns = null;
        }
        super.onSyncedDataUpdated(key);
    }

    public Iterable<ItemStack> getArmorSlots() {
        return new ArrayList<ItemStack>();
    }

    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
    }
}


