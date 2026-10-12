 
package com.mna.items.artifice;

import com.mna.tools.EnchantmentUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.sound.SFX;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;
import com.mna.api.enchantments.MNAEnchantmentRegistry;

public class HellfireTrident
extends Item
implements IFactionSpecific {
    public static final int THROW_THRESHOLD_TIME = 10;
    public static final float BASE_DAMAGE = 10.0f;
    public static final float SHOOT_POWER = 2.5f;
    private final int enchantability = 25;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;
    private final ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> allowedEnchantments = new ArrayList();

    public HellfireTrident() {
        super(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC).durability(500));
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 10.0, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)-1.9f, AttributeModifier.Operation.ADD_VALUE));
        this.defaultModifiers = builder.build();
    }

    protected ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> getAllowedEnchantments() {
        this.allowedEnchantments.clear();
        if (this.allowedEnchantments.size() == 0) {
            this.allowedEnchantments.add(Enchantments.SHARPNESS);
            this.allowedEnchantments.add(Enchantments.KNOCKBACK);
            this.allowedEnchantments.add(Enchantments.LOOTING);
            this.allowedEnchantments.add(Enchantments.SMITE);
            this.allowedEnchantments.add(Enchantments.IMPALING);
            this.allowedEnchantments.add(Enchantments.FIRE_ASPECT);
            this.allowedEnchantments.add(Enchantments.MENDING);
            this.allowedEnchantments.add(Enchantments.UNBREAKING);
            this.allowedEnchantments.add(EnchantmentInit.MANA_REPAIR.getKey());
            this.allowedEnchantments.add(EnchantmentInit.BEHEADING.getKey());
        }
        return this.allowedEnchantments;
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 25;
    }

    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        Map<net.minecraft.core.Holder<Enchantment>, Integer> enchantments = EnchantmentUtils.enchantmentMap((ItemStack)book);
        for (net.minecraft.core.Holder<Enchantment> ench : enchantments.keySet()) {
            if (this.getAllowedEnchantments().contains(ench.unwrapKey().orElse(null))) continue;
            return false;
        }
        return true;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return this.getAllowedEnchantments().contains(com.mna.api.enchantments.MNAEnchantmentRegistry.keyOf(enchantment));
    }

    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return !pPlayer.isCreative();
    }

    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (pLivingEntity instanceof Player) {
            Player player = (Player)pLivingEntity;
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                int manaCost;
                int n = manaCost = player.onGround() ? 100 : 200;
                if (magic.getCastingResource().hasEnough((LivingEntity)player, manaCost)) {
                    int j = 3;
                    magic.getCastingResource().consume((LivingEntity)player, manaCost);
                    this.usedByPlayer(player);
                    float f7 = player.getYRot();
                    float f = player.getXRot();
                    float f1 = -Mth.sin((float)(f7 * ((float)Math.PI / 180))) * Mth.cos((float)(f * ((float)Math.PI / 180)));
                    float f2 = -Mth.sin((float)(f * ((float)Math.PI / 180)));
                    float f3 = Mth.cos((float)(f7 * ((float)Math.PI / 180))) * Mth.cos((float)(f * ((float)Math.PI / 180)));
                    float f4 = Mth.sqrt((float)(f1 * f1 + f2 * f2 + f3 * f3));
                    float f5 = 3.0f * ((1.0f + (float)j) / 4.0f);
                    player.push((double)(f1 *= f5 / f4), (double)(f2 *= f5 / f4), (double)(f3 *= f5 / f4));
                    player.startAutoSpinAttack(20, 0.0f, player.getMainHandItem());
                    if (player.onGround()) {
                        player.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
                    }
                    pLevel.playSound((Player)null, (Entity)player, SFX.Spell.Impact.Single.FIRE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    pLevel.playSound((Player)null, (Entity)player, SoundEvents.TRIDENT_RIPTIDE_3.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.getCooldowns().addCooldown((Item)this, 20);
                    player.getPersistentData().putBoolean("hellfiretrident", true);
                }
            });
        }
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(itemstack);
        }
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public IFaction getFaction() {
        return Factions.DEMONS;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.SPEAR;
    }

    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        pStack.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)pAttacker.level(), pAttacker, brokenItem -> pAttacker.onEquippedItemBroken(brokenItem, EquipmentSlot.MAINHAND));
        return true;
    }

    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
        if ((double)pState.getDestroySpeed((BlockGetter)pLevel, pPos) != 0.0) {
            pStack.hurtAndBreak(2, (net.minecraft.server.level.ServerLevel)pEntityLiving.level(), pEntityLiving, brokenItem -> pEntityLiving.onEquippedItemBroken(brokenItem, EquipmentSlot.MAINHAND));
        }
        return true;
    }

    public net.minecraft.world.item.component.ItemAttributeModifiers getDefaultAttributeModifiers() {
        net.minecraft.world.item.component.ItemAttributeModifiers.Builder builder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
        this.defaultModifiers.forEach((attribute, modifier) -> builder.add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifier, net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND));
        return builder.build();
    }

    public int getEnchantmentValue() {
        return 1;
    }

    public boolean isValidRepairItem(ItemStack pStack, ItemStack pRepairCandidate) {
        return pRepairCandidate.getItem() == ItemInit.LIVING_FLAME.get();
    }
}

