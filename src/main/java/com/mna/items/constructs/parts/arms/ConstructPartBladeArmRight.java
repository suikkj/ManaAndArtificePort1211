 
package com.mna.items.constructs.parts.arms;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbilities;
import net.minecraft.world.item.Item;

public class ConstructPartBladeArmRight
extends ItemConstructPart {
    private final float attackDamage;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ConstructPartBladeArmRight(ConstructMaterial material, int attackDamage, float attackSpeed) {
        super(material, ConstructSlot.RIGHT_ARM, 4);
        this.attackDamage = (float)attackDamage + material.getEquivalentTier().getAttackDamageBonus();
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)this.attackDamage, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)attackSpeed, AttributeModifier.Operation.ADD_VALUE));
        this.defaultModifiers = builder.build();
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[]{ConstructCapability.MELEE_ATTACK, ConstructCapability.HARVEST, ConstructCapability.SHEAR};
    }

    @Override
    public float getAttackDamage() {
        return this.getMaterial().getDamageBonus() * 1.5f;
    }

    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(Blocks.COBWEB)) {
            return 15.0f;
        }
        return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5f : 1.0f;
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity living, LivingEntity target) {
        stack.hurtAndBreak(1, target, breaker -> breaker.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity living) {
        if (state.getDestroySpeed((BlockGetter)world, pos) != 0.0f) {
            stack.hurtAndBreak(2, (net.minecraft.server.level.ServerLevel)living.level(), living, p_43276_ -> p_43276_.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(Blocks.COBWEB);
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    public boolean canPerformAction(ItemStack stack, ItemAbilities toolAction) {
        return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }
}


