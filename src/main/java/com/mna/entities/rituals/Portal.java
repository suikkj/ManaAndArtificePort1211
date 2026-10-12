 
package com.mna.entities.rituals;

import com.mna.Registries;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.ItemInit;
import com.mna.tools.TeleportHelper;
import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;

public class Portal
extends Entity {
    private static final String KEY_AGE = "age";
    private static final String KEY_PERMANENT = "permanent";
    private static final String KEY_RTP = "rtp";
    private static final String KEY_COLOR = "color";
    private static final String KEY_FACTION_ID = "faction_id";
    private static final String KEY_FACTION_IDX = "faction_idx";
    private static final String KEY_TP_X = "teleport_pos_x";
    private static final String KEY_TP_Y = "teleport_pos_y";
    private static final String KEY_TP_Z = "teleport_pos_z";
    private static final String KEY_TP_DIM_KEYTYPE = "dimension_key_type";
    private static final String KEY_TP_DIM_KEY = "dimension_key";
    private static final EntityDataAccessor<BlockPos> TELEPORT_POS = SynchedEntityData.defineId(Portal.class, (EntityDataSerializer)EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> DYE_COLOR = SynchedEntityData.defineId(Portal.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RTP = SynchedEntityData.defineId(Portal.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> FACTION_ID = SynchedEntityData.defineId(Portal.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> FACTION_IDX = SynchedEntityData.defineId(Portal.class, (EntityDataSerializer)EntityDataSerializers.BYTE);
    private ResourceKey<Level> dimension;
    private int age = 0;
    private int maxAge = 200;
    private boolean permanent = false;
    private Pair<IFaction, Byte> _renderData = null;

    public Portal(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public void tick() {
        ++this.age;
        this.level().getEntities((Entity)this, this.getBoundingBox(), e -> e.isAlive() && e instanceof ItemEntity).stream().map(e -> (ItemEntity)e).forEach(e -> {
            ItemStack stack = e.getItem();
            if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
                if (this.level().isClientSide()) {
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_GRAVITY.get())), e.getX(), e.getY(), e.getZ(), 0.0, 0.0, 0.0);
                } else {
                    this.maxAge = this.age + 20;
                    this.permanent = false;
                    e.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        });
        if (!this.level().isClientSide() && !this.permanent && this.getAge() >= this.maxAge) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void baseTick() {
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(TELEPORT_POS, BlockPos.ZERO);
        builder.define(DYE_COLOR, -1);
        builder.define(RTP, false);
        builder.define(FACTION_ID, "");
        builder.define(FACTION_IDX, (byte)0);
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains(KEY_AGE)) {
            this.age = compound.getInt(KEY_AGE);
        }
        if (compound.contains(KEY_TP_X) && compound.contains(KEY_TP_Y) && compound.contains(KEY_TP_Z) && compound.contains(KEY_TP_DIM_KEY) && compound.contains(KEY_TP_DIM_KEYTYPE)) {
            this.setTeleportBlockPos(new BlockPos(compound.getInt(KEY_TP_X), compound.getInt(KEY_TP_Y), compound.getInt(KEY_TP_Z)), (ResourceKey<Level>)ResourceKey.create((ResourceKey)net.minecraft.core.registries.Registries.DIMENSION, (ResourceLocation)ResourceLocation.parse(compound.getString(KEY_TP_DIM_KEYTYPE))));
        }
        if (compound.contains(KEY_PERMANENT)) {
            this.permanent = compound.getBoolean(KEY_PERMANENT);
        }
        if (compound.contains(KEY_RTP)) {
            this.entityData.set(RTP, compound.getBoolean(KEY_RTP));
        }
        if (compound.contains(KEY_COLOR)) {
            this.entityData.set(DYE_COLOR, compound.getInt(KEY_COLOR));
        }
        if (compound.contains(KEY_FACTION_ID)) {
            this.entityData.set(FACTION_ID, compound.getString(KEY_FACTION_ID));
        }
        if (compound.contains(KEY_FACTION_IDX)) {
            this.entityData.set(FACTION_IDX, compound.getByte(KEY_FACTION_IDX));
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt(KEY_AGE, this.getAge());
        compound.putBoolean(KEY_PERMANENT, this.permanent);
        compound.putBoolean(KEY_RTP, ((Boolean)this.entityData.get(RTP)).booleanValue());
        compound.putInt(KEY_COLOR, ((Integer)this.entityData.get(DYE_COLOR)).intValue());
        compound.putString(KEY_FACTION_ID, (String)this.entityData.get(FACTION_ID));
        compound.putByte(KEY_FACTION_IDX, ((Byte)this.entityData.get(FACTION_IDX)).byteValue());
        compound.putInt(KEY_TP_X, this.getTeleportBlockPos().getX());
        compound.putInt(KEY_TP_Y, this.getTeleportBlockPos().getY());
        compound.putInt(KEY_TP_Z, this.getTeleportBlockPos().getZ());
        if (this.dimension != null) {
            compound.putString(KEY_TP_DIM_KEY, this.dimension.registry().toString());
            compound.putString(KEY_TP_DIM_KEYTYPE, this.dimension.location().toString());
        }
    }

    public boolean isAttackable() {
        return false;
    }

    public boolean isPickable() {
        return false;
    }

    public void setTeleportBlockPos(BlockPos pos, ResourceKey<Level> dimension) {
        this.entityData.set(TELEPORT_POS, pos);
        this.dimension = dimension;
    }

    public BlockPos getTeleportBlockPos() {
        return (BlockPos)this.entityData.get(TELEPORT_POS);
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    public void playerTouch(Player entityIn) {
        if (this.getAge() < 40) {
            return;
        }
        if (entityIn.isShiftKeyDown()) {
            return;
        }
        java.util.Optional<PlayerMagicProvider> magicContainer = MNACap.get(entityIn, PlayerMagicProvider.MAGIC);
        if (!magicContainer.isPresent()) {
            return;
        }
        if (((IPlayerMagic)magicContainer.get()).getPortalCooldown() > 0) {
            return;
        }
        if (((Boolean)this.entityData.get(RTP)).booleanValue()) {
            if (!this.level().isClientSide() && !TeleportHelper.randomTeleport((LivingEntity)entityIn, GeneralConfigValues.RTPDistance, 10)) {
                entityIn.sendSystemMessage((Component)Component.translatable((String)"gui.mna.portal.rtp_failed"));
            }
            ((IPlayerMagic)magicContainer.get()).setPortalCooldown(100);
            return;
        }
        BlockPos tpPos = this.getTeleportBlockPos();
        if (tpPos == BlockPos.ZERO) {
            return;
        }
        if (!entityIn.level().isClientSide()) {
            entityIn.getServer().getAllLevels().iterator().forEachRemaining(sw -> {
                if (this.dimension.toString().equals(sw.dimension().toString())) {
                    this.dimension = sw.dimension();
                }
            });
            TeleportHelper.teleportEntity((Entity)entityIn, this.dimension, new Vec3((double)((float)tpPos.getX() + 0.5f), (double)((float)tpPos.getY() + 0.5f), (double)((float)tpPos.getZ() + 0.5f)));
        } else {
            for (int i = 0; i < 10; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.SPARKLE_RANDOM.get())), entityIn.getX() - 1.0 + Math.random() * 2.0, entityIn.getY() + Math.random() * 2.0, entityIn.getZ() - 1.0 + Math.random() * 2.0, (double)0.1f, (double)0.1f, (double)0.1f);
            }
        }
        ((IPlayerMagic)magicContainer.get()).setPortalCooldown(100);
    }

    public int getAge() {
        return this.age;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    @Nullable
    public DyeColor getDyeColor() {
        int color = (Integer)this.entityData.get(DYE_COLOR);
        if (color == -1) {
            return null;
        }
        return DyeColor.byId((int)color);
    }

    public void setDyeColor(DyeColor color) {
        this.entityData.set(DYE_COLOR, color.getId());
    }

    public void setPermanent() {
        this.permanent = true;
    }

    public void setRTP() {
        this.entityData.set(RTP, true);
    }

    public boolean isRTP() {
        return (Boolean)this.entityData.get(RTP);
    }

    public Pair<IFaction, Byte> getRenderData() {
        if (this._renderData == null) {
            this._renderData = new Pair(((IFaction)Registries.Factions.getValue(ResourceLocation.parse((String)this.entityData.get(FACTION_ID)))), ((Byte)this.entityData.get(FACTION_IDX)));
        }
        return this._renderData;
    }

    public void setRenderData(IFaction faction, byte idx) {
        ResourceLocation key = faction == null ? null : Registries.Factions.getKey(faction);
        this.entityData.set(FACTION_ID, (key == null ? null : key.toString()));
        this.entityData.set(FACTION_IDX, idx);
    }
}

