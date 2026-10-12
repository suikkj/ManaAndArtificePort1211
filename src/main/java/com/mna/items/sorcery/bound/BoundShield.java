 
package com.mna.items.sorcery.bound;

import java.util.function.Supplier;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.items.base.INoCreativeTab;
import com.mna.items.renderers.bound.BoundShieldItemRenderer;
import com.mna.items.sorcery.bound.IBoundItem;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbilities;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotTypePreset;

public class BoundShield
extends ShieldItem
implements IBoundItem,
INoCreativeTab {
    public AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public BoundShield() {
        super(new Item.Properties());
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new BoundShieldItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public int getDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public float getPassiveManaDrain(Entity entity) {
        if (entity instanceof LivingEntity && ((LivingEntity)entity).isBlocking()) {
            return 2.0f;
        }
        if (entity instanceof Player) {
            Player p = (Player)entity;
            if (CuriosInterop.IsItemInCurioSlot((Item)ItemInit.WARDING_AMULET.get(), (LivingEntity)p, SlotTypePreset.NECKLACE)) {
                return 0.0f;
            }
        }
        return 0.2f;
    }

    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        this.handleInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public boolean canPerformAction(ItemStack stack, ItemAbilities toolAction) {
        return toolAction == ItemAbilities.SHIELD_BLOCK;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        SpellRecipe recipe = this.getRecipe(stack);
        recipe.addItemTooltip(stack, worldIn, tooltip, (Player)mc.player);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public ItemStack createFromSpell(LivingEntity caster, ItemStack original, ISpellDefinition recipe) {
        return IBoundItem.super.createFromSpell(caster, (Item)this, original, recipe);
    }

    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        ItemStack restored = this.restoreItem(item);
        player.getInventory().setItem(player.getInventory().selected, restored);
        return false;
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.bound_shield.idle")))});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}


