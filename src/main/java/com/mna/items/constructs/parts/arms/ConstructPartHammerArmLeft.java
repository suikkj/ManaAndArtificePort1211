 
package com.mna.items.constructs.parts.arms;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbilities;
import net.minecraft.world.item.Item;
import com.mna.api.tools.MNAtierSorting;

public class ConstructPartHammerArmLeft
extends ItemConstructPart {
    private final TagKey<Block> blocks = BlockTags.MINEABLE_WITH_PICKAXE;
    protected final float speed;
    private final float attackDamageBaseline;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ConstructPartHammerArmLeft(ConstructMaterial material, float damage, float speed) {
        super(material, ConstructSlot.LEFT_ARM, 2);
        this.speed = material.getEquivalentTier().getSpeed();
        this.attackDamageBaseline = damage + material.getEquivalentTier().getAttackDamageBonus();
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)this.attackDamageBaseline, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)speed, AttributeModifier.Operation.ADD_VALUE));
        this.defaultModifiers = builder.build();
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[]{ConstructCapability.MELEE_ATTACK, ConstructCapability.SMITH, ConstructCapability.MINE};
    }

    @Override
    public float getAttackDamage() {
        return this.getMaterial().getDamageBonus();
    }

    @Override
    public int getAttackSpeedModifier() {
        return 15;
    }

    @Override
    public float getKnockbackBonus() {
        return 2.0f;
    }

    public boolean canPerformAction(ItemStack stack, ItemAbilities toolAction) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(toolAction);
    }

    public float getDestroySpeed(ItemStack p_41004_, BlockState p_41005_) {
        return p_41005_.is(this.blocks) ? this.speed : 1.0f;
    }

    public boolean hurtEnemy(ItemStack p_40994_, LivingEntity p_40995_, LivingEntity p_40996_) {
        p_40994_.hurtAndBreak(2, p_40996_, EquipmentSlot.MAINHAND);
        return true;
    }

    public boolean mineBlock(ItemStack p_40998_, Level p_40999_, BlockState p_41000_, BlockPos p_41001_, LivingEntity p_41002_) {
        if (!p_40999_.isClientSide && p_41000_.getDestroySpeed((BlockGetter)p_40999_, p_41001_) != 0.0f) {
            p_40998_.hurtAndBreak(1, p_41002_, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_40990_) {
        return p_40990_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_40990_);
    }

    @Deprecated
    public boolean isCorrectToolForDrops(BlockState p_150816_) {
        if (MNAtierSorting.isTierSorted((Tier)this.getMaterial().getEquivalentTier())) {
            return MNAtierSorting.isCorrectTierForDrops((Tier)this.getMaterial().getEquivalentTier(), (BlockState)p_150816_) && p_150816_.is(this.blocks);
        }
        int i = com.mna.tools.MATools.getTierLevel(this.getMaterial().getEquivalentTier());
        if (i < 3 && p_150816_.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        }
        if (i < 2 && p_150816_.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        }
        return i < 1 && p_150816_.is(BlockTags.NEEDS_STONE_TOOL) ? false : p_150816_.is(this.blocks);
    }

    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(this.blocks) && MNAtierSorting.isCorrectTierForDrops((Tier)this.getMaterial().getEquivalentTier(), (BlockState)state);
    }
}


