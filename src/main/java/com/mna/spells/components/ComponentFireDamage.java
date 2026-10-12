/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.AbstractFurnaceBlock
 *  net.minecraft.world.level.block.BaseFireBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.fml.util.ObfuscationReflectionHelper
 *  top.theillusivec4.curios.api.SlotTypePreset
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.entities.DamageHelper;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IDamageComponent;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.blocks.BlockInit;
import com.mna.config.GeneralConfig;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.tools.BlockUtils;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import top.theillusivec4.curios.api.SlotTypePreset;

public class ComponentFireDamage
extends SpellEffect
implements IDamageComponent {
    public ComponentFireDamage(ResourceLocation icon) {
        super(icon, new AttributeValuePair(Attribute.DAMAGE, 5.0f, 1.0f, 20.0f, 0.5f, 3.0f), new AttributeValuePair(Attribute.DURATION, 3.0f, 0.0f, 10.0f, 1.0f, 2.0f), new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f, 5.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        block14: {
            if (target.isEntity()) {
                if (target.getEntity() instanceof LivingEntity) {
                    float damage = modificationData.getValue(Attribute.DAMAGE) * GeneralConfig.getDamageMultiplier();
                    if (target.getEntity().isOnFire()) {
                        damage *= 1.25f;
                    }
                    target.getEntity().hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.IN_FIRE, context.getLevel().registryAccess(), (Entity)source.getCaster()), damage);
                    target.getEntity().igniteForSeconds((float)((int)modificationData.getValue(Attribute.DURATION)));
                    return ComponentApplicationResult.SUCCESS;
                }
            } else if (target.isBlock() && !context.getServerLevel().isEmptyBlock(target.getBlock())) {
                BlockEntity be = context.getServerLevel().getBlockEntity(target.getBlock());
                if (be != null && be instanceof AbstractFurnaceBlockEntity) {
                    int burnTime = 105 * (int)modificationData.getValue(Attribute.DAMAGE);
                    burnTime = (int)((float)burnTime * Math.max(modificationData.getValue(Attribute.DURATION), 1.0f));
                    if (source.isPlayerCaster() && CuriosInterop.IsItemInCurioSlot((Item)ItemInit.EMBERGLOW_BRACELET.get(), source.getCaster(), SlotTypePreset.BRACELET)) {
                        burnTime *= 10;
                    }
                    AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity)be;
                    Field f = ObfuscationReflectionHelper.findField(AbstractFurnaceBlockEntity.class, (String)"litTime");
                    Field f2 = ObfuscationReflectionHelper.findField(AbstractFurnaceBlockEntity.class, (String)"litDuration");
                    try {
                        if (f == null || f2 == null || f.getInt(furnace) >= burnTime) break block14;
                        f.set(furnace, burnTime);
                        f2.set(furnace, burnTime);
                        BlockState state = context.getServerLevel().getBlockState(target.getBlock());
                        if (state.hasProperty((Property)AbstractFurnaceBlock.LIT)) {
                            context.getServerLevel().setBlock(target.getBlock(), (BlockState)state.setValue((Property)AbstractFurnaceBlock.LIT, (Comparable)Boolean.valueOf(true)), 3);
                        }
                    }
                    catch (Throwable throwable) {}
                } else if (modificationData.getValue(Attribute.PRECISION) == 0.0f && context.getServerLevel().isEmptyBlock(target.getBlock().above())) {
                    if (context.getSpell().getAffinity().containsKey(Affinity.HELLFIRE)) {
                        BlockUtils.placeBlock(context.getServerLevel(), target.getBlock().above(), target.getBlockFace(this), ((Block)BlockInit.HELLFIRE.get()).defaultBlockState(), source.getPlayer());
                    } else {
                        BlockState fireState = BaseFireBlock.getState((BlockGetter)context.getServerLevel(), (BlockPos)target.getBlock().above());
                        BlockUtils.placeBlock(context.getServerLevel(), target.getBlock().above(), target.getBlockFace(this), fireState, source.getPlayer());
                    }
                    return ComponentApplicationResult.SUCCESS;
                }
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        Random rndm = new Random(1234L);
        if (age < 5) {
            for (int i = 0; i < 25; ++i) {
                if (recipe.getAffinity().containsKey(Affinity.HELLFIRE)) {
                    world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), (Entity)caster), impact_position.x + (double)rndm.nextFloat() - 0.5, impact_position.y + (double)rndm.nextFloat() - 0.5, impact_position.z + (double)rndm.nextFloat() - 0.5, 0.0, 0.0, 0.0);
                    continue;
                }
                world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), (Entity)caster), impact_position.x + (double)rndm.nextFloat() - 0.5, impact_position.y + (double)rndm.nextFloat() - 0.5, impact_position.z + (double)rndm.nextFloat() - 0.5, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.FIRE;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.FIRE;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 500;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.HARMFUL;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.FIRE);
    }
}


