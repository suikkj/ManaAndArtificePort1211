 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.blocks.tile.OwnerInformation;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.blocks.artifice.ElementalSentryBlock;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.entities.projectile.SentryProjectile;
import com.mna.tools.SummonUtils;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;

public class ElementalSentryTile
extends BlockEntity
implements IBlockEntityExtension,
IEldrinConsumerTile {
    private static final int TARGET_TIME = 100;
    private static final float POWER_PER_SHOT = 1.0f;
    private LivingEntity target = null;
    private int findTargetCounter = 0;
    private int shootCounter = 0;
    private int warmupCounter = 0;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    public float nextPageAngle;
    public float pageAngle;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float yaw;
    public int ticks;
    public int ticksSinceTargetChange;
    private static final Random random = new Random();
    private static float radius = 0.5f;
    private static float yOffset = 1.3f;
    private OwnerInformation ownerInfo;
    private Affinity selectedAffinity;
    private float accruedPower = 0.0f;

    public ElementalSentryTile(BlockPos pos, BlockState state, Affinity shootAffinity) {
        super((BlockEntityType)TileEntityInit.ELEMENTAL_SENTRY.get(), pos, state);
        this.selectedAffinity = shootAffinity;
        this.ownerInfo = new OwnerInformation();
    }

    public ElementalSentryTile(BlockPos pos, BlockState state) {
        this(pos, state, Affinity.UNKNOWN);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, ElementalSentryTile blockEntity) {
        if (!blockEntity.hasTarget()) {
            blockEntity.getTarget();
        } else {
            blockEntity.shoot();
        }
        if (level.isClientSide()) {
            blockEntity.updatePageTurning();
            blockEntity.spawnParticles();
        }
    }

    private void updatePageTurning() {
        float f2;
        this.pageTurningSpeed = this.nextPageTurningSpeed;
        this.pageAngle = this.nextPageAngle;
        if (this.target != null) {
            double d0 = this.target.getX() - ((double)this.worldPosition.getX() + 0.5);
            double d1 = this.target.getZ() - ((double)this.worldPosition.getZ() + 0.5);
            this.yaw = (float)Mth.atan2((double)d1, (double)d0);
            this.nextPageTurningSpeed += 0.1f;
            if (this.nextPageTurningSpeed < 0.5f || random.nextInt(40) == 0) {
                float f1 = this.flipT;
                do {
                    this.flipT += (float)(random.nextInt(4) - random.nextInt(4));
                } while (f1 == this.flipT);
            }
        } else {
            this.yaw += 0.02f;
            this.nextPageTurningSpeed -= 0.1f;
        }
        while (this.nextPageAngle >= (float)Math.PI) {
            this.nextPageAngle -= (float)Math.PI * 2;
        }
        while (this.nextPageAngle < (float)(-Math.PI)) {
            this.nextPageAngle += (float)Math.PI * 2;
        }
        while (this.yaw >= (float)Math.PI) {
            this.yaw -= (float)Math.PI * 2;
        }
        while (this.yaw < (float)(-Math.PI)) {
            this.yaw += (float)Math.PI * 2;
        }
        for (f2 = this.yaw - this.nextPageAngle; f2 >= (float)Math.PI; f2 -= (float)Math.PI * 2) {
        }
        while (f2 < (float)(-Math.PI)) {
            f2 += (float)Math.PI * 2;
        }
        this.nextPageAngle += f2 * 0.4f;
        this.nextPageTurningSpeed = Mth.clamp((float)this.nextPageTurningSpeed, (float)0.0f, (float)1.0f);
        ++this.ticks;
        ++this.ticksSinceTargetChange;
        this.oFlip = this.flip;
        float f = (this.flipT - this.flip) * 0.4f;
        f = Mth.clamp((float)f, (float)-0.2f, (float)0.2f);
        this.flipA += (f - this.flipA) * 0.9f;
        this.flip += this.flipA;
    }

    private Vec3 getPointInFront() {
        if (this.target == null) {
            return Vec3.ZERO;
        }
        Vec3 pos = new Vec3((double)((float)this.getBlockPos().getX() + 0.5f), (double)((float)this.getBlockPos().getY() + yOffset), (double)((float)this.getBlockPos().getZ() + 0.5f));
        Vec3 targetPos = this.target.position();
        Vec3 delta = targetPos.subtract(pos).normalize().scale(0.5);
        return pos.add(delta);
    }

    private void spawnParticles() {
        if (((Boolean)this.getBlockState().getValue((Property)ElementalSentryBlock.SHOOTING)).booleanValue()) {
            int numParticles = 5;
            Vec3 pos = this.getPointInFront();
            MAParticleType particle = null;
            switch (this.selectedAffinity) {
                case EARTH: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST_LERP.get()));
                    break;
                }
                case ENDER: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get()));
                    break;
                }
                case FIRE: 
                case LIGHTNING: 
                case HELLFIRE: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME_LERP.get()));
                    break;
                }
                case WATER: 
                case ICE: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER_LERP.get()));
                    break;
                }
                case WIND: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_LERP.get()));
                    break;
                }
                default: {
                    particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_LERP.get()));
                }
            }
            for (int i = 0; i < numParticles; ++i) {
                Vec3 offset = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale((double)radius);
                this.getLevel().addParticle((ParticleOptions)particle, pos.x + offset.x, pos.y + offset.y, pos.z + offset.z, pos.x, pos.y, pos.z);
            }
        }
    }

    public Affinity getAffinity() {
        return this.selectedAffinity;
    }

    public void setOwner(Player owner) {
        this.ownerInfo.setOwner(owner);
    }

    public boolean hasTarget() {
        block2: {
            block4: {
                block3: {
                    if (this.target == null) break block2;
                    if (!this.target.isAlive()) break block3;
                    Vec3 vec3 = new Vec3((double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ());
                    if (!(this.target.distanceToSqr(vec3) > 2304.0)) break block4;
                }
                this.clearTarget();
                this.getTarget(true);
                return false;
            }
            return true;
        }
        return false;
    }

    private void getTarget() {
        this.getTarget(false);
    }

    private void getTarget(boolean force) {
        if (((Boolean)this.getBlockState().getValue((Property)ElementalSentryBlock.TARGET_OVERRIDE)).booleanValue()) {
            return;
        }
        ++this.findTargetCounter;
        if (this.findTargetCounter >= 100 || force) {
            this.findTargetCounter = 0;
            Vec3 myPos = new Vec3((double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ());
            List potentialTargets = this.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(this.getBlockPos()).inflate(32.0), e -> {
                LivingEntity summoner;
                boolean subPredicate;
                Vec3 theirpos = new Vec3(e.getX(), e.getEyeY(), e.getZ());
                boolean bl = subPredicate = e instanceof Enemy || e instanceof FlyingMob;
                if (SummonUtils.isSummon((Entity)e) && (summoner = SummonUtils.getSummoner(e)) != null && this.ownerInfo.isFriendlyTo(summoner)) {
                    subPredicate = false;
                }
                return e.isAlive() && subPredicate && this.getLevel().clip(new ClipContext(myPos, theirpos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getType() == HitResult.Type.MISS;
            });
            if (potentialTargets.size() > 0) {
                potentialTargets.sort((o1, o2) -> {
                    Double o1Dist = o1.distanceToSqr(myPos);
                    Double o2Dist = o2.distanceToSqr(myPos);
                    return o1Dist.compareTo(o2Dist);
                });
                this.target = (LivingEntity)potentialTargets.get(0);
                this.ticksSinceTargetChange = 0;
            }
        }
    }

    public void forceTarget(LivingEntity target) {
        if (!this.getLevel().isClientSide() && !((Boolean)this.getBlockState().getValue((Property)ElementalSentryBlock.TARGET_OVERRIDE)).booleanValue()) {
            this.getLevel().setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)ElementalSentryBlock.TARGET_OVERRIDE, (Comparable)Boolean.valueOf(true)), 3);
        }
        if (this.target != target) {
            this.target = target;
            this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)ElementalSentryBlock.SHOOTING, (Comparable)Boolean.valueOf(false)));
        }
    }

    private void clearTarget() {
        this.target = null;
        this.ticksSinceTargetChange = 0;
        this.warmupCounter = 0;
        this.shootCounter = 0;
        this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)ElementalSentryBlock.SHOOTING, (Comparable)Boolean.valueOf(false)));
    }

    private int getPowerupTime() {
        switch (this.selectedAffinity.getShiftAffinity()) {
            case EARTH: 
            case FIRE: {
                return 15;
            }
            case WIND: {
                return 20;
            }
        }
        return 60;
    }

    private int getShootInterval() {
        switch (this.selectedAffinity.getShiftAffinity()) {
            case EARTH: 
            case FIRE: {
                return 5;
            }
            case WIND: {
                return 40;
            }
        }
        return 60;
    }

    private SoundEvent getShootSound() {
        switch (this.selectedAffinity.getShiftAffinity()) {
            case EARTH: {
                return SFX.Spell.Cast.EARTH;
            }
            case ENDER: {
                return SFX.Spell.Cast.ENDER;
            }
            case FIRE: {
                return SFX.Spell.Cast.FIRE;
            }
            case WATER: {
                return SFX.Spell.Cast.WATER;
            }
            case WIND: {
                return SFX.Spell.Cast.WIND;
            }
        }
        return SFX.Spell.Cast.ARCANE;
    }

    private boolean consumeFuel() {
        if (this.accruedPower <= 1.0f) {
            this.accruedPower += this.consumeDirect(this.ownerInfo.getOwnerId(), this.level, this.selectedAffinity, 1.0f);
            if (this.accruedPower <= 1.0f) {
                return false;
            }
        }
        return true;
    }

    private void shoot() {
        if (this.target == null) {
            return;
        }
        if (this.getLevel().clip(new ClipContext(Vec3.atCenterOf((Vec3i)this.getBlockPos()), this.target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getType() != HitResult.Type.MISS) {
            return;
        }
        if (this.shootCounter < this.getShootInterval()) {
            ++this.shootCounter;
            return;
        }
        if (!this.getLevel().isClientSide() && this.consumeFuel()) {
            if (!((Boolean)this.getBlockState().getValue((Property)ElementalSentryBlock.SHOOTING)).booleanValue()) {
                this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)ElementalSentryBlock.SHOOTING, (Comparable)Boolean.valueOf(true)));
            }
            if (this.warmupCounter < this.getPowerupTime()) {
                ++this.warmupCounter;
                return;
            }
            this.warmupCounter = 0;
            this.shootCounter = 0;
            this.getLevel().setBlockAndUpdate(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)ElementalSentryBlock.SHOOTING, (Comparable)Boolean.valueOf(false)));
            this.getLevel().playSound(null, this.getBlockPos(), this.getShootSound(), SoundSource.BLOCKS, 1.0f, (float)((double)0.8f + Math.random() * (double)0.4f));
            Vec3 myPos = this.getPointInFront();
            SentryProjectile projectile = new SentryProjectile(this.getLevel(), myPos.x(), myPos.y(), myPos.z(), this.selectedAffinity);
            if (this.target != null) {
                Vec3 direction = this.target.getEyePosition(0.0f).subtract(myPos).normalize();
                projectile.shoot(direction.x, direction.y, direction.z, 2.0f, 0.0f);
                this.getLevel().addFreshEntity((Entity)projectile);
            }
        }
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("affinity", this.selectedAffinity.ordinal());
        this.ownerInfo.save(tag);
    }

    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownerInfo.load(tag);
        if (tag.contains("affinity")) {
            try {
                this.selectedAffinity = Affinity.values()[tag.getInt("affinity")];
            }
            catch (Throwable t) {
                this.selectedAffinity = Affinity.UNKNOWN;
            }
        }
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }
}


