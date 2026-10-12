/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.mna.spells.components;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.blocks.BlockInit;
import com.mna.blocks.decoration.ClayMugBlock;
import com.mna.entities.summon.AnimusBlock;
import com.mna.entities.summon.GreaterAnimus;
import com.mna.entities.summon.Simulacrum;
import com.mna.factions.Factions;
import com.mna.tools.BlockUtils;
import com.mna.tools.SummonUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class ComponentAnimus
extends SpellEffect {
    private final boolean greater;

    public ComponentAnimus(ResourceLocation guiIcon, boolean greater) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 10.0f, 5.0f, 60.0f, 5.0f, 2.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 0.0f, 0.0f, 500.0f, 50.0f, 0.0f), new AttributeValuePair(Attribute.DURATION, 10.0f, 5.0f, 60.0f, 5.0f, 2.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        ItemStack offhand;
        if (context.countAffectedBlocks(this) > 0) {
            return ComponentApplicationResult.FAIL;
        }
        if (this.greater && source.getCaster() instanceof Simulacrum) {
            return ComponentApplicationResult.FAIL;
        }
        if (target.isBlock()) {
            BlockState state = context.getServerLevel().getBlockState(target.getBlock());
            if (!context.getServerLevel().isEmptyBlock(target.getBlock()) && context.getServerLevel().getFluidState(target.getBlock()).isEmpty() && !(state.getBlock() instanceof EntityBlock) && BlockUtils.canDestroyBlock((LivingEntity)source.getPlayer(), (Level)context.getServerLevel(), target.getBlock(), (Tier)Tiers.IRON)) {
                if (state.getBlock() == BlockInit.CLAY_MUG.get() && ((Boolean)state.getValue((Property)ClayMugBlock.HAS_LIQUID)).booleanValue()) {
                    CustomAdvancementTriggers.BE_OUR_GUEST.trigger((ServerPlayer)source.getPlayer());
                }
                AnimusBlock animatedBlockEntity = new AnimusBlock((Level)context.getServerLevel(), state, target.getBlock(), (int)modificationData.getValue(Attribute.DURATION) * 20);
                context.getServerLevel().addFreshEntity((Entity)animatedBlockEntity);
                target.overrideSpellTarget((Entity)animatedBlockEntity);
                return ComponentApplicationResult.SUCCESS;
            }
        } else if (target.isEntity() && target.getEntity() == source.getCaster() && !(offhand = source.getCaster().getItemInHand(source.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)).isEmpty()) {
            ItemStack summonStack = offhand.copy();
            summonStack.setCount(1);
            if (summonStack.getItem() instanceof BlockItem) {
                BlockState state = ((BlockItem)summonStack.getItem()).getBlock().defaultBlockState();
                if (source.isPlayerCaster() && state.getBlock() == BlockInit.CLAY_MUG.get() && ((Boolean)state.getValue((Property)ClayMugBlock.HAS_LIQUID)).booleanValue()) {
                    CustomAdvancementTriggers.BE_OUR_GUEST.trigger((ServerPlayer)source.getPlayer());
                }
                AnimusBlock animatedBlockEntity = new AnimusBlock((Level)context.getServerLevel(), state, BlockPos.containing((Position)source.getOrigin()), (int)(modificationData.getValue(Attribute.DURATION) * 20.0f));
                animatedBlockEntity.doNotClearOrigin();
                context.getServerLevel().addFreshEntity((Entity)animatedBlockEntity);
                target.overrideSpellTarget((Entity)animatedBlockEntity);
                offhand.shrink(1);
                return ComponentApplicationResult.SUCCESS;
            }
            if (this.greater) {
                GreaterAnimus eag = new GreaterAnimus(context.getLevel(), summonStack, modificationData.getValue(Attribute.LESSER_MAGNITUDE));
                eag.setPos(source.getOrigin());
                SummonUtils.addTrackedEntity(source.getCaster(), (Entity)eag);
                SummonUtils.setSummon((Mob)eag, source.getCaster(), true, (int)modificationData.getValue(Attribute.DURATION) * 20);
                context.getLevel().addFreshEntity((Entity)eag);
                target.overrideSpellTarget((Entity)eag);
                offhand.shrink(1);
                return ComponentApplicationResult.SUCCESS;
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.WATER;
    }

    @Override
    public float initialComplexity() {
        return this.greater ? 60.0f : 10.0f;
    }

    @Override
    public boolean targetsEntities() {
        return true;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.COUNCIL;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    @Override
    public boolean isSilverSpell() {
        return this.greater;
    }

    public static final void adjustSpell(ISpellDefinition recipe, @Nullable LivingEntity caster) {
        if (!recipe.isValid()) {
            return;
        }
        if (recipe.getShape().getPart().affectsCaster()) {
            return;
        }
        recipe.getComponents().stream().filter(c -> c.getPart() == Components.ANIMUS).findFirst().ifPresent(c -> recipe.setManaCost(recipe.getManaCost() + c.getValue(Attribute.LESSER_MAGNITUDE)));
    }
}

