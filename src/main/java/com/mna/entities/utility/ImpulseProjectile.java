/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ItemParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.EnchantmentHelper
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.api.distmarker.OnlyIn
 *  net.minecraftforge.entity.IEntityAdditionalSpawnData
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.utility;

import com.mna.tools.EnchantmentUtils;
import com.mna.api.entities.DamageHelper;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.entities.EntityInit;
import com.mna.items.ItemInit;
import com.mna.tools.InventoryUtilities;
import java.util.Map;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.mna.api.tools.MNAItemData;

public class ImpulseProjectile
extends ThrowableItemProjectile
{
    ItemStack defaultStack;
    float damage;
    float chanceForItemBack = 0.0f;

    public ImpulseProjectile(EntityType<? extends ImpulseProjectile> p_i50159_1_, Level p_i50159_2_) {
        super(p_i50159_1_, p_i50159_2_);
        this.defaultStack = new ItemStack((ItemLike)ItemInit.RUNE_PATTERN.get());
    }

    public ImpulseProjectile(Level worldIn, LivingEntity throwerIn, ItemStack stack, float damage) {
        super((EntityType)EntityInit.IMPULSE_PROJECTILE.get(), throwerIn, worldIn);
        this.defaultStack = stack.copy();
        this.defaultStack.setCount(1);
        this.damage = damage;
    }

    public ImpulseProjectile(Level worldIn, double x, double y, double z, ItemStack stack, float damage) {
        super((EntityType)EntityInit.IMPULSE_PROJECTILE.get(), x, y, z, worldIn);
        this.defaultStack = stack.copy();
        this.defaultStack.setCount(1);
        this.damage = damage;
    }

    protected Item getDefaultItem() {
        return this.defaultStack.getItem();
    }

    public void setChanceForRecovery(float chance) {
        this.chanceForItemBack = chance;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @OnlyIn(value=Dist.CLIENT)
    private ParticleOptions makeParticle() {
        return new ItemParticleOption(ParticleTypes.ITEM, this.defaultStack);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions iparticledata = this.makeParticle();
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(iparticledata, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    protected void onHit(HitResult result) {
        Entity owner = this.getOwner();
        if (result instanceof EntityHitResult && owner instanceof LivingEntity) {
            Entity e = ((EntityHitResult)result).getEntity();
            if (e == owner) {
                return;
            }
            if (e instanceof LivingEntity) {
                ((LivingEntity)e).hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.FALLING_STALACTITE, e.level().registryAccess(), owner), this.damage);
            }
        }
        if (result.getType() != HitResult.Type.MISS) {
            if (this.defaultStack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem)this.defaultStack.getItem()).getBlock();
                SoundEvent sound = block.defaultBlockState().getSoundType((LevelReader)this.level(), this.blockPosition(), (Entity)this).getPlaceSound();
                this.playSound(sound, 1.0f, 1.0f);
            } else {
                this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
            }
        }
        ItemStack dropStack = this.defaultStack.copy();
        Map enchants = EnchantmentUtils.enchantmentMap((ItemStack)dropStack);
        if (owner != null && owner instanceof Player && !((Player)owner).isCreative() && enchants.containsKey(Enchantments.LOYALTY) || enchants.containsKey(EnchantmentInit.RETURNING.get())) {
            dropStack = InventoryUtilities.mergeToPlayerInvPrioritizeOffhand((Player)owner, dropStack);
        }
        if (dropStack.getCount() > 0 && ((double)this.chanceForItemBack > Math.random() || result instanceof BlockHitResult) && !this.level().isClientSide()) {
            ItemEntity returnedItem = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), dropStack);
            this.level().addFreshEntity((Entity)returnedItem);
        }
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        CompoundTag item = new CompoundTag();
        MNAItemData.save(this.defaultStack, item);
        pCompound.put("item", (Tag)item);
        pCompound.putFloat("recoveryChance", this.chanceForItemBack);
        pCompound.putFloat("damage", this.damage);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("item")) {
            this.defaultStack = ItemStack.parseOptional(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().registryAccess(), (CompoundTag)pCompound.getCompound("item"));
        }
        if (pCompound.contains("recoveryChance")) {
            this.chanceForItemBack = pCompound.getFloat("recoveryChance");
        }
        if (pCompound.contains("damage")) {
            this.damage = pCompound.getFloat("damage");
        }
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeItemStack(this.defaultStack, true);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.defaultStack = additionalData.readItem();
    }
}


