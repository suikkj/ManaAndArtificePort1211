 
package com.mna.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.spells.targeting.SpellTargetHelper;
import com.mna.entities.constructs.animated.Construct;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.NeoForgeMod;

public class MnADebugStick
extends Item
implements INoCreativeTab {
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public MnADebugStick() {
        super(new Item.Properties().stacksTo(1).setNoRepair());
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 1000000.0, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)-2.4f, AttributeModifier.Operation.ADD_VALUE));
        this.defaultModifiers = builder.build();
    }

    public int getUseDuration(ItemStack stack) {
        return 1000;
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof Construct) {
            Construct construct = (Construct)target;
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        Entity entity;
        ItemStack itemstack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return InteractionResultHolder.pass(itemstack);
        }
        EntityHitResult ehr = SpellTargetHelper.rayTraceEntities(world, (Entity)player, player.getEyePosition(), player.getEyePosition().add(player.getForward().scale(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE))), player.getBoundingBox().inflate(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)), e -> e instanceof Construct);
        if (ehr.getEntity() != null && (entity = ehr.getEntity()) instanceof Construct) {
            Construct construct = (Construct)entity;
            construct.setDefeated(600);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        return super.finishUsingItem(stack, worldIn, entityLiving);
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public void onUseTick(Level pLevel, LivingEntity player, ItemStack stack, int count) {
    }

    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_43274_) {
        return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(p_43274_);
    }
}


