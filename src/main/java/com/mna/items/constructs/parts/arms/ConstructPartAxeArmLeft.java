 
package com.mna.items.constructs.parts.arms;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbilities;
import net.minecraft.world.item.Item;
import com.mna.api.tools.MNAtierSorting;

public class ConstructPartAxeArmLeft
extends ItemConstructPart {
    protected static final Map<Block, Block> STRIPPABLES = new ImmutableMap.Builder().put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD).put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG).put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD).put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG).put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD).put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG).put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD).put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG).put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD).put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG).put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD).put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG).put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM).put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE).put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM).put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE).build();
    private final TagKey<Block> blocks = BlockTags.MINEABLE_WITH_AXE;
    protected final float speed;
    private final float attackDamageBaseline;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ConstructPartAxeArmLeft(ConstructMaterial material, float damage, float speed) {
        super(material, ConstructSlot.LEFT_ARM, 8);
        this.speed = material.getEquivalentTier().getSpeed();
        this.attackDamageBaseline = damage + material.getEquivalentTier().getAttackDamageBonus();
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)this.attackDamageBaseline, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)speed, AttributeModifier.Operation.ADD_VALUE));
        this.defaultModifiers = builder.build();
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[]{ConstructCapability.MELEE_ATTACK, ConstructCapability.CHOP_WOOD};
    }

    @Override
    public float getAttackDamage() {
        return this.getMaterial().getDamageBonus() * 1.25f;
    }

    @Override
    public int getAttackSpeedModifier() {
        return 10;
    }

    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos blockpos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        BlockState blockstate = level.getBlockState(blockpos);
        Optional<BlockState> optional = Optional.ofNullable(blockstate.getToolModifiedState(ctx, ItemAbilities.AXE_STRIP, false));
        Optional<BlockState> optional1 = Optional.ofNullable(blockstate.getToolModifiedState(ctx, ItemAbilities.AXE_SCRAPE, false));
        Optional<BlockState> optional2 = Optional.ofNullable(blockstate.getToolModifiedState(ctx, ItemAbilities.AXE_WAX_OFF, false));
        ItemStack itemstack = ctx.getItemInHand();
        Optional<BlockState> optional3 = Optional.empty();
        if (optional.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
            optional3 = optional;
        } else if (optional1.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(player, 3005, blockpos, 0);
            optional3 = optional1;
        } else if (optional2.isPresent()) {
            level.playSound(player, blockpos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.levelEvent(player, 3004, blockpos, 0);
            optional3 = optional2;
        }
        if (optional3.isPresent()) {
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
            }
            level.setBlock(blockpos, (BlockState)optional3.get(), 11);
            if (player != null) {
                itemstack.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)player.level(), player, brokenItem -> player.onEquippedItemBroken(brokenItem, ctx.getHand()));
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Nullable
    public static BlockState getAxeStrippingState(BlockState originalState) {
        Block block = STRIPPABLES.get(originalState.getBlock());
        return block != null ? (BlockState)block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, ((Direction.Axis)originalState.getValue(RotatedPillarBlock.AXIS))) : null;
    }

    public boolean canPerformAction(ItemStack stack, ItemAbilities toolAction) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(toolAction);
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


