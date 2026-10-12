/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.entities.EntityInit;
import com.mna.entities.sorcery.Rift;
import com.mna.gui.containers.providers.NamedRift;
import com.mna.tools.GuiTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentRift
extends SpellEffect {
    public ComponentRift(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        boolean enderChest;
        if (context.countAffectedBlocks(this) + context.countAffectedEntities(this) > 0) {
            return ComponentApplicationResult.FAIL;
        }
        boolean bl = enderChest = modificationData.getValue(Attribute.PRECISION) == 1.0f;
        if (target.isBlock()) {
            BlockPos p = target.getBlock().offset(target.getBlockFace(this).getNormal());
            for (int count = 0; count < 5 && !context.getServerLevel().isEmptyBlock(p); ++count) {
                p = p.above();
            }
            this.spawnRift((Level)context.getServerLevel(), p, enderChest);
            return ComponentApplicationResult.SUCCESS;
        }
        if (!target.isLivingEntity()) return ComponentApplicationResult.FAIL;
        context.addAffectedEntity(this, target.getEntity());
        if (source.isPlayerCaster() && target.getLivingEntity() == source.getCaster()) {
            if (source.getPlayer().containerMenu != source.getPlayer().inventoryMenu) return ComponentApplicationResult.FAIL;
            if (enderChest) {
                GuiTools.openEnderChest(source.getPlayer());
                return ComponentApplicationResult.SUCCESS;
            } else {
                ((ServerPlayer)((ServerPlayer)source.getPlayer())).openMenu((MenuProvider)new NamedRift());
            }
            return ComponentApplicationResult.SUCCESS;
        } else {
            BlockPos p = target.getLivingEntity().blockPosition().above();
            this.spawnRift((Level)context.getServerLevel(), p, enderChest);
        }
        return ComponentApplicationResult.SUCCESS;
    }

    private void spawnRift(Level world, BlockPos pos, boolean enderChest) {
        Rift rift = new Rift((EntityType)EntityInit.RIFT.get(), world);
        rift.setEnderChest(enderChest);
        rift.setPos((float)pos.getX() + 0.5f, (float)pos.getY() + 0.5f, (float)pos.getZ() + 0.5f);
        world.addFreshEntity((Entity)rift);
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.ENDER;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        Vec3 rotationOffset = new Vec3(0.5, 0.0, 0.0);
        BlockPos bp = BlockPos.containing((Position)impact_position);
        for (int angle = 0; angle < 360; angle += 30) {
            Vec3 point = rotationOffset.yRot((float)((double)angle * Math.PI / 180.0));
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER.get())), (Entity)caster), (double)((float)bp.getX() + 0.5f) + point.x, (double)bp.getY(), (double)((float)bp.getZ() + 0.5f) + point.z, (double)((float)bp.getX() + 0.5f), (double)bp.getY(), (double)((float)bp.getZ() + 0.5f));
        }
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public float initialComplexity() {
        return 30.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}


