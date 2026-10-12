 
package com.mna.items.artifice;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.items.IShowHud;
import com.mna.api.items.TieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.items.ItemInit;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

public class CrystalOfMemories
extends TieredItem
implements IShowHud {
    private static final String KEY_MODE = "memory_crystal_fragment_mode";
    private static final int MODE_INFUSE = 0;
    private static final int MODE_SUPPLEMENT = 1;
    private static final String KEY_STORED_XP = "stored_xp";
    private static final int MAX_XP = 20000;

    public CrystalOfMemories() {
        super(new Item.Properties().durability(100).setNoRepair());
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 0;
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    private int getContainedXP(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || stack.getItem() != ItemInit.CRYSTAL_OF_MEMORIES.get()) {
            return 0;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_STORED_XP);
    }

    private void storeContainedXP(ItemStack stack, int amount) {
        if (stack.getItem() != ItemInit.CRYSTAL_OF_MEMORIES.get()) {
            return;
        }
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_STORED_XP, amount);
    }

    public void onUseTick(Level pLevel, LivingEntity living, ItemStack stack, int count) {
        if (living instanceof Player) {
            Player p = (Player)living;
            int contained_xp = this.getContainedXP(stack);
            if (!living.level().isClientSide()) {
                switch (this.getMode(stack)) {
                    case 1: {
                        if (contained_xp == 0) {
                            return;
                        }
                        int xp_to_give = Math.max(p.experienceLevel, 1);
                        if (contained_xp < xp_to_give) {
                            xp_to_give = contained_xp;
                        }
                        p.giveExperiencePoints(xp_to_give);
                        this.storeContainedXP(stack, contained_xp - xp_to_give);
                        break;
                    }
                    default: {
                        int xp_to_store;
                        if (contained_xp == 20000 || p.totalExperience == 0) {
                            return;
                        }
                        int n = xp_to_store = p.experienceLevel == 0 ? 1 : p.experienceLevel;
                        if (contained_xp + xp_to_store > 20000) {
                            xp_to_store = 20000 - contained_xp;
                        }
                        p.giveExperiencePoints(-xp_to_store);
                        this.storeContainedXP(stack, Math.min(contained_xp + xp_to_store, 20000));
                    }
                }
            }
        }
        if (living.level().isClientSide()) {
            for (int i = 0; i < 5; ++i) {
                living.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), living.getX() - 0.5 + Math.random(), living.getY() + Math.random(), living.getZ() - 0.5 + Math.random(), 0.0, (double)0.1f, 0.0);
            }
        }
    }

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
        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(stack);
    }

    public int getMode(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_MODE)) {
            return 0;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_MODE);
    }

    private void setMode(ItemStack stack, int mode) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_MODE, mode);
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        int contained_xp = this.getContainedXP(stack);
        tooltip.add((Component)Component.translatable((String)"item.mna.crystal_of_memories.contains", (Object[])new Object[]{contained_xp, 20000}).withStyle(ChatFormatting.ITALIC));
        switch (this.getMode(stack)) {
            case 1: {
                tooltip.add((Component)Component.translatable((String)"item.mna.crystal_of_memories.supplement"));
                break;
            }
            default: {
                tooltip.add((Component)Component.translatable((String)"item.mna.crystal_of_memories.infusion"));
            }
        }
    }

    public int getBarColor(ItemStack stack) {
        return FastColor.ARGB32.color((int)255, (int)128, (int)64, (int)255);
    }

    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    public int getBarWidth(ItemStack stack) {
        float xp = this.getContainedXP(stack);
        return (int)(13.0f * (xp / 20000.0f));
    }
}

