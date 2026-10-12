 
package com.mna.items.constructs.parts.arms;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import com.mna.api.spells.ICanContainSpell;
import com.mna.effects.EffectInit;
import com.mna.gui.containers.providers.NamedSpellAdjust;
import com.mna.items.base.IItemWithGui;
import com.mna.items.sorcery.ItemSpell;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ConstructPartCasterArmRight
extends ItemConstructPart
implements ICanContainSpell,
IItemWithGui<ConstructPartCasterArmRight> {
    public ConstructPartCasterArmRight(ConstructMaterial material) {
        super(material, ConstructSlot.RIGHT_ARM, 128);
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[]{ConstructCapability.CAST_SPELL};
    }

    @Override
    public float getAttackDamage() {
        return 0.0f;
    }

    @Override
    public int getAttackSpeedModifier() {
        return 10;
    }

    @Override
    public float getRangedAttackDamage() {
        return 0.0f;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> lines, TooltipFlag flags) {
        super.appendHoverText(stack, world, lines, flags);
        ICanContainSpell.super.getSpell(stack).addItemTooltip(stack, world, lines, ManaAndArtifice.instance.proxy.getClientPlayer());
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (this.openGuiIfModifierPressed(held, player, world)) {
            return InteractionResultHolder.success(held);
        }
        return ItemSpell.castSpellOnUse(held, world, player, hand, this::shouldConsumeMana);
    }

    public int getUseDuration(ItemStack p_41454_) {
        return 99999;
    }

    public void onUseTick(Level pLevel, LivingEntity player, ItemStack stack, int count) {
        if (player instanceof Player) {
            ItemSpell.castSpellOnChannelTick(stack, (Player)player, count, this::shouldConsumeChanneledMana);
        }
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        if (entityLiving instanceof Player) {
            ((Player)entityLiving).releaseUsingItem();
        }
        return stack;
    }

    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player) {
            Player player = (Player)entityLiving;
            SpellRecipe recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            SpellCaster.setCooldown(stack, player, recipe.getCooldown((LivingEntity)player));
            if (this.shouldConsumeMana(stack)) {
                if (entityLiving.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_STUNT.get())) && entityLiving.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_STUNT.get())).getAmplifier() == 5) {
                    entityLiving.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_STUNT.get()));
                }
                SpellCaster.AddAffinityAndMagicXP(recipe, player, recipe.getMaxChannelTime() - timeLeft);
            }
        }
    }

    public boolean isFoil(ItemStack p_41453_) {
        return SpellRecipe.stackContainsSpell(p_41453_);
    }

    protected boolean shouldConsumeMana(ItemStack stack) {
        return true;
    }

    protected boolean shouldConsumeChanneledMana(Player player, ItemStack stack) {
        return true;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedSpellAdjust();
    }
}

