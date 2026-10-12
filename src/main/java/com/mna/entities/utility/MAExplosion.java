/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 *  net.minecraft.Util
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundExplodePacket
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.item.PrimedTnt
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.ProtectionEnchantment
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.EntityBasedExplosionDamageCalculator
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Explosion$BlockInteraction
 *  net.minecraft.world.level.ExplosionDamageCalculator
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BaseFireBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.storage.loot.LootParams$Builder
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParams
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.event.ForgeEventFactory
 */
package com.mna.entities.utility;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MAExplosion
extends Explosion {
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private final boolean fire;
    private Explosion.BlockInteraction blockInteraction;
    private final Random random = new Random();
    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    private final float damage;
    @Nullable
    private final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();
    private final Vec3 position;

    public static MAExplosion make(@Nullable LivingEntity caster, ServerLevel world, double x, double y, double z, float radius, float damage, boolean doFire, Explosion.BlockInteraction mode, DamageSource source) {
        MAExplosion explosion = new MAExplosion((Level)world, (Entity)caster, source, new ExplosionDamageCalculator(), x, y, z, radius, doFire, mode, damage);
        if (false) {
            explosion.blockInteraction = Explosion.BlockInteraction.KEEP;
        }
        explosion.explode();
        explosion.finalizeExplosion(false);
        if (mode == Explosion.BlockInteraction.KEEP) {
            explosion.clearToBlow();
        }
        for (ServerPlayer serverplayerentity : world.players()) {
            if (!(serverplayerentity.distanceToSqr(x, y, x) < 4096.0)) continue;
            serverplayerentity.connection.send((Packet)new ClientboundExplodePacket(x, y, z, radius, explosion.getToBlow(), explosion.getHitPlayers().get(serverplayerentity), mode, null, null, SoundEvents.GENERIC_EXPLODE));
        }
        world.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, (float)(0.9 + Math.random() * 0.2), (float)(0.9 + Math.random() * 0.2));
        return explosion;
    }

    public MAExplosion(Level level, @Nullable Entity source, DamageSource damagesource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean doFire, Explosion.BlockInteraction interactionMode, float damage) {
        super(level, source, x, y, z, radius, doFire, interactionMode);
        this.damage = damage;
        this.level = level;
        this.source = source;
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.z = z;
        this.fire = doFire;
        this.blockInteraction = interactionMode;
        this.damageSource = damagesource;
        this.damageCalculator = damageCalculator == null ? this.makeDamageCalculator(source) : damageCalculator;
        this.position = new Vec3(this.x, this.y, this.z);
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity entity) {
        return entity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(entity);
    }

    public static float getSeenPercent(Vec3 vector, Entity entity) {
        AABB axisalignedbb = entity.getBoundingBox();
        double d0 = 1.0 / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0 + 1.0);
        double d1 = 1.0 / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0 + 1.0);
        double d2 = 1.0 / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0 + 1.0);
        double d3 = (1.0 - Math.floor(1.0 / d0) * d0) / 2.0;
        double d4 = (1.0 - Math.floor(1.0 / d2) * d2) / 2.0;
        if (!(d0 < 0.0 || d1 < 0.0 || d2 < 0.0)) {
            int i = 0;
            int j = 0;
            float f = 0.0f;
            while (f <= 1.0f) {
                float f1 = 0.0f;
                while (f1 <= 1.0f) {
                    float f2 = 0.0f;
                    while (f2 <= 1.0f) {
                        double d5 = Mth.lerp((double)f, (double)axisalignedbb.minX, (double)axisalignedbb.maxX);
                        double d6 = Mth.lerp((double)f1, (double)axisalignedbb.minY, (double)axisalignedbb.maxY);
                        double d7 = Mth.lerp((double)f2, (double)axisalignedbb.minZ, (double)axisalignedbb.maxZ);
                        Vec3 vector3d = new Vec3(d5 + d3, d6, d7 + d4);
                        if (entity.level().clip(new ClipContext(vector3d, vector, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS) {
                            ++i;
                        }
                        ++j;
                        f2 = (float)((double)f2 + d2);
                    }
                    f1 = (float)((double)f1 + d1);
                }
                f = (float)((double)f + d0);
            }
            return (float)i / (float)j;
        }
        return 0.0f;
    }

    public void explode() {
        HashSet set = Sets.newHashSet();
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) continue;
                    double d0 = (float)j / 15.0f * 2.0f - 1.0f;
                    double d1 = (float)k / 15.0f * 2.0f - 1.0f;
                    double d2 = (float)l / 15.0f * 2.0f - 1.0f;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    d0 /= d3;
                    d1 /= d3;
                    d2 /= d3;
                    double d4 = this.x;
                    double d6 = this.y;
                    double d8 = this.z;
                    for (float f = this.radius * (0.7f + this.level.random.nextFloat() * 0.6f); f > 0.0f; f -= 0.22500001f) {
                        FluidState fluidstate;
                        BlockState blockstate;
                        BlockPos blockpos = BlockPos.containing((double)d4, (double)d6, (double)d8);
                        Optional optional = this.damageCalculator.getBlockExplosionResistance((Explosion)this, (BlockGetter)this.level, blockpos, blockstate = this.level.getBlockState(blockpos), fluidstate = this.level.getFluidState(blockpos));
                        if (optional.isPresent()) {
                            f -= (((Float)optional.get()).floatValue() + 0.3f) * 0.3f;
                        }
                        if (f > 0.0f && this.damageCalculator.shouldBlockExplode((Explosion)this, (BlockGetter)this.level, blockpos, blockstate, f)) {
                            set.add(blockpos);
                        }
                        d4 += d0 * (double)0.3f;
                        d6 += d1 * (double)0.3f;
                        d8 += d2 * (double)0.3f;
                    }
                }
            }
        }
        this.toBlow.addAll((Collection)set);
        float f2 = this.radius * 2.0f;
        int k1 = Mth.floor((double)(this.x - (double)f2 - 1.0));
        int l1 = Mth.floor((double)(this.x + (double)f2 + 1.0));
        int i2 = Mth.floor((double)(this.y - (double)f2 - 1.0));
        int i1 = Mth.floor((double)(this.y + (double)f2 + 1.0));
        int j2 = Mth.floor((double)(this.z - (double)f2 - 1.0));
        int j1 = Mth.floor((double)(this.z + (double)f2 + 1.0));
        List list = this.level.getEntities(this.source, new AABB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
        ;
        Vec3 vector3d = new Vec3(this.x, this.y, this.z);
        for (int k2 = 0; k2 < list.size(); ++k2) {
            Player playerentity;
            double d9;
            double d7;
            double d5;
            double d13;
            double d12;
            Entity entity = (Entity)list.get(k2);
            if (entity.ignoreExplosion(this) || !((d12 = (double)(Mth.sqrt((float)((float)entity.distanceToSqr(vector3d))) / f2)) <= 1.0) || (d13 = (double)Mth.sqrt((float)((float)((d5 = entity.getX() - this.x) * d5 + (d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y) * d7 + (d9 = entity.getZ() - this.z) * d9)))) == 0.0) continue;
            d5 /= d13;
            d7 /= d13;
            d9 /= d13;
            double d14 = MAExplosion.getSeenPercent(vector3d, entity);
            double d10 = (1.0 - d12) * d14;
            entity.hurt(this.getDamageSource(), this.damage);
            double d11 = d10;
            entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11, d7 * d11, d9 * d11));
            if (!(entity instanceof Player) || (playerentity = (Player)entity).isSpectator() || playerentity.isCreative() && playerentity.getAbilities().flying) continue;
            this.hitPlayers.put(playerentity, new Vec3(d5 * d10, d7 * d10, d9 * d10));
        }
    }

    public void finalizeExplosion(boolean p_77279_1_) {
        boolean flag;
        if (this.level.isClientSide()) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0f, (1.0f + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2f) * 0.7f, false);
        }
        boolean bl = flag = this.blockInteraction != Explosion.BlockInteraction.KEEP;
        if (p_77279_1_) {
            if (!(this.radius < 2.0f) && flag) {
                this.level.addParticle((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            } else {
                this.level.addParticle((ParticleOptions)ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            }
        }
        if (flag) {
            ObjectArrayList objectarraylist = new ObjectArrayList();
            boolean flag1 = this.getIndirectSourceEntity() instanceof Player;
            Util.shuffle(this.toBlow, (RandomSource)this.level.random);
            for (BlockPos blockpos : this.toBlow) {
                Level $$9;
                BlockState blockstate = this.level.getBlockState(blockpos);
                if (blockstate.isAir()) continue;
                BlockPos blockpos1 = blockpos.immutable();
                this.level.getProfiler().push("explosion_blocks");
                if (blockstate.canDropFromExplosion((BlockGetter)this.level, blockpos, (Explosion)this) && ($$9 = this.level) instanceof ServerLevel) {
                    ServerLevel serverlevel = (ServerLevel)$$9;
                    BlockEntity blockentity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                    LootParams.Builder lootparams$builder = new LootParams.Builder(serverlevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf((Vec3i)blockpos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity).withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                    if (this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                        lootparams$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, Float.valueOf(this.radius));
                    }
                    blockstate.spawnAfterBreak(serverlevel, blockpos, ItemStack.EMPTY, flag1);
                    blockstate.getDrops(lootparams$builder).forEach(p_46074_ -> MAExplosion.addBlockDrops((ObjectArrayList<Pair<ItemStack, BlockPos>>)objectarraylist, p_46074_, blockpos1));
                }
                blockstate.onBlockExploded(this.level, blockpos, (Explosion)this);
                this.level.getProfiler().pop();
            }
            for (Pair pair : (java.util.List<Pair>)objectarraylist) {
                Block.popResource((Level)this.level, (BlockPos)((BlockPos)pair.getSecond()), (ItemStack)((ItemStack)pair.getFirst()));
            }
        }
        if (this.fire) {
            for (BlockPos blockpos2 : this.toBlow) {
                if (this.random.nextInt(3) != 0 || !this.level.getBlockState(blockpos2).isAir() || !this.level.getBlockState(blockpos2.below()).isSolidRender((BlockGetter)this.level, blockpos2.below())) continue;
                this.level.setBlockAndUpdate(blockpos2, BaseFireBlock.getState((BlockGetter)this.level, (BlockPos)blockpos2));
            }
        }
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> p_229976_0_, ItemStack p_229976_1_, BlockPos p_229976_2_) {
        int i = p_229976_0_.size();
        for (int j = 0; j < i; ++j) {
            Pair pair = (Pair)p_229976_0_.get(j);
            ItemStack itemstack = (ItemStack)pair.getFirst();
            if (!ItemEntity.areMergable((ItemStack)itemstack, (ItemStack)p_229976_1_)) continue;
            ItemStack itemstack1 = ItemEntity.merge((ItemStack)itemstack, (ItemStack)p_229976_1_, (int)16);
            p_229976_0_.set(j, Pair.of(itemstack1, ((BlockPos)pair.getSecond())));
            if (!p_229976_1_.isEmpty()) continue;
            return;
        }
        p_229976_0_.add(Pair.of(p_229976_1_, p_229976_2_));
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    public Entity getDirectSourceEntity() {
        Entity entity;
        if (this.source == null) {
            return null;
        }
        if (this.source instanceof PrimedTnt) {
            return ((PrimedTnt)this.source).getOwner();
        }
        if (this.source instanceof LivingEntity) {
            return this.source;
        }
        if (this.source instanceof Projectile && (entity = ((Projectile)this.source).getOwner()) instanceof LivingEntity) {
            return entity;
        }
        return null;
    }

    public void clearToBlow() {
        this.toBlow.clear();
    }

    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    @Nullable
    public Entity getExploder() {
        return this.source;
    }

    public static enum Mode {
        NONE,
        BREAK,
        DESTROY;

    }
}


