 
package com.mna.items.food;

import com.mna.api.items.TieredItem;
import com.mna.blocks.BlockInit;
import com.mna.blocks.decoration.ClayMugBlock;
import com.mna.effects.EffectInit;
import com.mna.items.ItemInit;
import com.mna.items.food.Foods;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ManaTea
extends TieredItem {
    public ManaTea() {
        super(new Item.Properties().food(Foods.MANA_COFFEE));
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public boolean isEnchantable(ItemStack p_77616_1_) {
        return false;
    }

    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult raytraceresult = ManaTea.getPlayerPOVHitResult((Level)world, (Player)player, (ClipContext.Fluid)ClipContext.Fluid.NONE);
        if (raytraceresult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockraytraceresult = raytraceresult;
            BlockPos blockpos = blockraytraceresult.getBlockPos();
            Direction direction = blockraytraceresult.getDirection();
            if (!world.mayInteract(player, blockpos) || !player.mayUseItemAt(blockpos.relative(direction), direction, itemstack)) {
                return InteractionResultHolder.fail(itemstack);
            }
            BlockState state = world.getBlockState(blockpos);
            if (state.isFaceSturdy((BlockGetter)world, blockpos, Direction.UP) && world.isEmptyBlock(blockpos.above())) {
                world.playSound(player, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                world.setBlock(blockpos.above(), (BlockState)((BlockState)((Block)BlockInit.CLAY_MUG.get()).defaultBlockState().setValue((Property)ClayMugBlock.HAS_LIQUID, (Comparable)Boolean.valueOf(true))).setValue((Property)HorizontalDirectionalBlock.FACING, (Comparable)Direction.fromYRot((double)player.getYRot()).getOpposite()), 3);
                itemstack.shrink(1);
                return InteractionResultHolder.success(itemstack);
            }
        }
        return ItemUtils.startUsingInstantly((Level)world, (Player)player, (InteractionHand)hand);
    }

    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        Player playerentity;
        Player player = playerentity = entity instanceof Player ? (Player)entity : null;
        if (playerentity instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)playerentity, stack);
        }
        if (!world.isClientSide) {
            entity.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_REGEN.get()), 4800, 4));
        }
        if (playerentity != null) {
            playerentity.awardStat(Stats.ITEM_USED.get(this));
            if (!playerentity.isCreative()) {
                stack.shrink(1);
                playerentity.addItem(new ItemStack((ItemLike)ItemInit.CLAY_MUG.get()));
            }
        }
        return stack;
    }

    public int getUseDuration(ItemStack p_77626_1_) {
        return 32;
    }

    public UseAnim getUseAnimation(ItemStack p_77661_1_) {
        return UseAnim.DRINK;
    }
}

