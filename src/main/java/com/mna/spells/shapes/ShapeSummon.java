/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.entity.SpawnGroupData
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.phys.Vec3
 *  top.theillusivec4.curios.api.CuriosApi
 *  top.theillusivec4.curios.api.SlotResult
 */
package com.mna.spells.shapes;

import com.mna.api.config.GeneralConfigValues;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedSpellEffect;
import com.mna.api.tools.MATags;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.PhylacteryStaff;
import com.mna.spells.shapes.ShapeRaytrace;
import com.mna.tools.SummonUtils;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public class ShapeSummon
extends ShapeRaytrace {
    public ShapeSummon(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 30.0f, 30.0f, 240.0f, 30.0f, 20.0f), new AttributeValuePair(Attribute.RANGE, 8.0f, 1.0f, 32.0f, 1.0f, 2.0f), new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 10.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        List<SpellTarget> supertargets = super.Target(source, world, modificationData, recipe);
        SpellTarget superTarget = supertargets.get(0);
        if (superTarget != SpellTarget.NONE) {
            if (!world.isClientSide) {
                ItemStack phylactery;
                ItemStack itemStack = phylactery = source.getHand() == InteractionHand.MAIN_HAND ? source.getCaster().getOffhandItem() : source.getCaster().getMainHandItem();
                if (phylactery.isEmpty() || !PhylacteryStaff.isFilled(phylactery)) {
                    List results = CuriosApi.getCuriosHelper().findCurios(source.getCaster(), is -> is.getItem() == ItemInit.STAFF_PHYLACTERY.get() || is.getItem() == ItemInit.CRYSTAL_PHYLACTERY.get());
                    for (SlotResult sr : results) {
                        if (sr.stack().isEmpty()) continue;
                        phylactery = sr.stack().copy();
                    }
                }
                if (!PhylacteryStaff.isFilled(phylactery)) {
                    if (source.isPlayerCaster()) {
                        source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:shapes/summon.nophylactery"));
                    }
                    return Arrays.asList(SpellTarget.NONE);
                }
                EntityType<? extends Mob> type = PhylacteryStaff.getEntityType(phylactery);
                if (type == null) {
                    if (source.isPlayerCaster()) {
                        source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:shapes/summon.nophylactery"));
                    }
                    return Arrays.asList(SpellTarget.NONE);
                }
                if (MATags.isEntityIn(type, MATags.EntityTypes.SUMMON_BLACKLIST)) {
                    if (source.isPlayerCaster()) {
                        source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:shapes/summon.blacklisted"));
                    }
                    return Arrays.asList(SpellTarget.NONE);
                }
                ItemStack castItem = source.getCaster().getItemInHand(source.getHand());
                SummonUtils.clampTrackedEntities(source.getCaster());
                int numSummons = 1 + (int)Math.ceil((float)MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.LOYALTY, castItem) / 2.0f);
                for (int i = 0; i < numSummons; ++i) {
                    if (this.summonCreature(source, world, modificationData, recipe, type, superTarget)) continue;
                    return Arrays.asList(SpellTarget.NONE);
                }
            }
            return Arrays.asList(new SpellTarget((Entity)source.getCaster()));
        }
        return Arrays.asList(SpellTarget.NONE);
    }

    private boolean summonCreature(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe, EntityType<? extends Mob> type, SpellTarget superTarget) {
        String name = source.getCaster().getDisplayName().getString();
        MutableComponent summonName = Component.literal((String)name);
        summonName.append("'s ");
        summonName.append((Component)Component.translatable((String)type.getDescriptionId()));
        Mob summon = (Mob)type.create(world);
        summon.setPos(Vec3.atBottomCenterOf((Vec3i)superTarget.getBlock().above()));
        if (!GeneralConfigValues.SummonBosses && !com.mna.api.tools.MATools.canChangeDimensions(summon)) {
            return false;
        }
        int magnitude = (int)modificationData.getValue(Attribute.MAGNITUDE);
        if (!this.magnitudeHealthCheck(source, new SpellTarget((Entity)summon), magnitude, 20)) {
            return false;
        }
        SummonUtils.setSummon(summon, source.getCaster(), (int)(modificationData.getValue(Attribute.DURATION) * 20.0f));
        SpellTarget tgt = new SpellTarget((Entity)summon);
        SpellContext context = new SpellContext(world, recipe, (Entity)summon);
        recipe.getComponents().forEach(c -> {
            int delay = (int)Math.max(10.0f, c.getValue(Attribute.DELAY) * 20.0f);
            DelayedEventQueue.pushEvent((Level)context.getServerLevel(), new TimedDelayedSpellEffect("summon_delay", delay, source, tgt, (IModifiedSpellPart<SpellEffect>)c, context));
        });
        if (!world.addFreshEntity((Entity)summon)) {
            return false;
        }
        summon.finalizeSpawn((ServerLevelAccessor)((ServerLevel)world), world.getCurrentDifficultyAt(summon.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
        SummonUtils.addTrackedEntity(source.getCaster(), (Entity)summon);
        return true;
    }

    @Override
    public boolean spawnsTargetEntity() {
        return true;
    }

    @Override
    public float initialComplexity() {
        return 40.0f;
    }

    @Override
    public int baselineCooldown() {
        return 20;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.FRIENDLY;
    }

    @Override
    public boolean canBeOnRandomStaff() {
        return false;
    }
}

