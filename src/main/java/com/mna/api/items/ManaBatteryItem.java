 
package com.mna.api.items;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.ChargeableItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.mna.api.tools.MNACap;

public class ManaBatteryItem
extends ChargeableItem {
    static final String KEY_MODE = "mana_crystal_fragment_mode";
    static final String KEY_MANA = "mana_crystal_fragment_mana";
    static final int MODE_INFUSE = 0;
    static final int MODE_SUPPLEMENT = 1;

    public ManaBatteryItem(Item.Properties properties, float maxMana) {
        super(properties, maxMana);
    }

    public int getMode(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_MODE)) {
            return 0;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_MODE);
    }

    protected void setMode(ItemStack stack, int mode) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_MODE, mode);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity living, ItemStack stack, int pRemainingUseDuration) {
        IPlayerMagic magic = (IPlayerMagic)MNACap.get(living, ManaAndArtificeMod.getMagicCapability()).orElse(null);
        if (magic == null || !(living instanceof Player)) {
            return;
        }
        int mana_shift_amount = 10;
        switch (this.getMode(stack)) {
            case 1: {
                if (living.level().isClientSide()) {
                    living.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), living.getX() - 0.5 + Math.random(), living.getY() + Math.random(), living.getZ() - 0.5 + Math.random(), 0.0, (double)0.1f, 0.0);
                    break;
                }
                if (!(magic.getCastingResource().getAmount() < magic.getCastingResource().getMaxAmount())) break;
                if (this.consumeMana(stack, mana_shift_amount, null)) {
                    magic.getCastingResource().restore(mana_shift_amount);
                    break;
                }
                living.stopUsingItem();
                break;
            }
            default: {
                if (living.level().isClientSide()) {
                    living.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), living.getX() - 0.5 + Math.random(), living.getY() + Math.random(), living.getZ() - 0.5 + Math.random(), 0.0, (double)0.1f, 0.0);
                    break;
                }
                if (magic.getCastingResource().getAmount() > (float)mana_shift_amount) {
                    if (this.refundMana(stack, mana_shift_amount, (Player)living) > 0.0f) {
                        magic.getCastingResource().setAmount(magic.getCastingResource().getAmount() - (float)mana_shift_amount);
                        break;
                    }
                    living.stopUsingItem();
                    return;
                }
                living.stopUsingItem();
                return;
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn.isShiftKeyDown()) {
            if (!worldIn.isClientSide) {
                if (this.getMode(stack) == 0) {
                    this.setMode(stack, 1);
                } else {
                    this.setMode(stack, 0);
                }
            }
            return InteractionResultHolder.success(stack);
        }
        if (this.getMode(stack) == 1) {
            if (this.getMana(stack) > 0.0f) {
                MNACap.get(playerIn, ManaAndArtificeMod.getMagicCapability()).ifPresent(m -> {
                    if (m.getCastingResource().canRechargeFrom(stack) && m.getCastingResource().getAmount() < m.getCastingResource().getMaxAmount()) {
                        playerIn.startUsingItem(handIn);
                    }
                });
            }
            return InteractionResultHolder.sidedSuccess(stack, (boolean)worldIn.isClientSide);
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        switch (this.getMode(stack)) {
            case 1: {
                tooltip.add((Component)Component.translatable((String)"item.mna.mana_battery.supplement").withStyle(ChatFormatting.AQUA));
                break;
            }
            default: {
                tooltip.add((Component)Component.translatable((String)"item.mna.mana_battery.infusion").withStyle(ChatFormatting.AQUA));
            }
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            MNACap.get(mc.player, ManaAndArtificeMod.getMagicCapability()).ifPresent(m -> {
                if (!m.getCastingResource().canRechargeFrom(stack)) {
                    tooltip.add((Component)Component.translatable((String)"item.mna.mana_battery.no_supplement").withStyle(ChatFormatting.RED));
                }
            });
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return this.getMode(newStack) == 0 ? this.getMana(newStack) < this.getMaxMana() : this.getMode(newStack) == 1 && this.getMana(newStack) >= 0.0f;
    }

    @Override
    protected boolean tickCurio() {
        return false;
    }

    @Override
    protected boolean tickInventory() {
        return false;
    }

    @Override
    protected boolean tickEffect(ItemStack stack, Player player, Level world, int slot, float mana, boolean selected) {
        return false;
    }
}

