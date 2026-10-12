 
package com.mna.entities.manaweaving;

import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.manaweaving.Manaweave;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ItemManaweaveBottle;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import com.mna.api.tools.MNACap;

public class ThrownManaweaveBottle
extends ThrowableItemProjectile {
    public static final Predicate<LivingEntity> WATER_SENSITIVE = LivingEntity::isSensitiveToWater;

    public ThrownManaweaveBottle(EntityType<? extends ThrownManaweaveBottle> typeIn, Level worldIn) {
        super(typeIn, worldIn);
    }

    public ThrownManaweaveBottle(Level worldIn, LivingEntity livingEntityIn) {
        super((EntityType)EntityInit.MANAWEAVE_BOTTLE_THROWN.get(), livingEntityIn, worldIn);
    }

    public ThrownManaweaveBottle(Level worldIn, double x, double y, double z) {
        super((EntityType)EntityInit.MANAWEAVE_BOTTLE_THROWN.get(), x, y, z, worldIn);
    }

    protected Item getDefaultItem() {
        return (Item)ItemInit.MANAWEAVE_BOTTLE.get();
    }

    protected double getGravity() {
        return 0.05;
    }

    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            ItemStack itemstack = this.getItem();
            ManaweavingPattern p = ItemManaweaveBottle.getPattern(itemstack);
            Entity shooter = this.getOwner();
            if (p != null && shooter != null && shooter instanceof Player) {
                Player caster = (Player)shooter;
                MNACap.get(caster, PlayerProgressionProvider.PROGRESSION).ifPresent(progression -> {
                    int tier = progression.getTier();
                    if (tier >= p.getTier()) {
                        Manaweave emw = new Manaweave(this.level());
                        emw.setPos(this.getX(), this.getY(), this.getZ());
                        emw.setPattern(p.getId());
                        emw.setCaster((LivingEntity)caster, InteractionHand.MAIN_HAND);
                        this.level().addFreshEntity((Entity)emw);
                    }
                });
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.level().isClientSide()) {
            for (int i = 0; i < 50; ++i) {
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), this.getX(), this.getY(), this.getZ(), -0.5 + Math.random(), -0.5 + Math.random(), -0.5 + Math.random());
            }
        }
    }
}


