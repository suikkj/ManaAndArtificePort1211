 
package com.mna.items.constructs.parts.arms;

import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.ItemConstructPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbilities;

public class ConstructPartShieldArmRight
extends ItemConstructPart {
    public ConstructPartShieldArmRight(ConstructMaterial material) {
        super(material, ConstructSlot.RIGHT_ARM, 32);
    }

    @Override
    public ConstructCapability[] getEnabledCapabilities() {
        return new ConstructCapability[]{ConstructCapability.MELEE_ATTACK, ConstructCapability.BLOCK};
    }

    @Override
    public float getAttackDamage() {
        return this.getMaterial().getDamageBonus();
    }

    @Override
    public int getAttackSpeedModifier() {
        return 20;
    }

    @Override
    public int getArmor() {
        return this.getMaterial().getArmorBonus(this.getSlot());
    }

    @Override
    public int getToughness() {
        return this.getMaterial().getToughnessBonus(this.getSlot());
    }

    @Override
    public float getKnockbackBonus() {
        return 0.5f;
    }

    public UseAnim getUseAnimation(ItemStack p_43105_) {
        return UseAnim.BLOCK;
    }

    public int getUseDuration(ItemStack p_43107_) {
        return 72000;
    }

    public InteractionResultHolder<ItemStack> use(Level p_43099_, Player p_43100_, InteractionHand p_43101_) {
        ItemStack itemstack = p_43100_.getItemInHand(p_43101_);
        p_43100_.startUsingItem(p_43101_);
        return InteractionResultHolder.consume(itemstack);
    }

    public boolean canPerformAction(ItemStack stack, ItemAbilities toolAction) {
        return ItemAbilities.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
    }
}


