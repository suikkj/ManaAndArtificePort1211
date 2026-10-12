 
package com.mna.items.sorcery.bound;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.items.base.INoCreativeTab;
import com.mna.items.renderers.bound.BoundSwordItemRenderer;
import com.mna.items.sorcery.bound.IBoundItem;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotTypePreset;
import com.mna.api.tools.MNACap;

public class BoundSword
extends SwordItem
implements IBoundItem,
INoCreativeTab {
    public AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public BoundSword(float attackDamageIn, float attackSpeedIn) {
        super((Tier)Tiers.DIAMOND, (int)attackDamageIn, attackSpeedIn, new Item.Properties());
    }

    public int getDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public float getPassiveManaDrain(Entity entity) {
        if (entity instanceof Player) {
            Player p = (Player)entity;
            if (CuriosInterop.IsItemInCurioSlot((Item)ItemInit.BATTLEMAGE_AMULET.get(), (LivingEntity)p, SlotTypePreset.NECKLACE)) {
                return 0.0f;
            }
        }
        return 0.2f;
    }

    public Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap baseAttrs = super.getAttributeModifiers(slot, stack);
        if (slot != EquipmentSlot.MAINHAND) {
            return baseAttrs;
        }
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return baseAttrs;
        }
        SpellRecipe recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (!recipe.isValid()) {
            return baseAttrs;
        }
        int damage = (int)((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.DAMAGE);
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)damage, AttributeModifier.Operation.ADD_VALUE));
        int range = (int)((ModifiedSpellPart)recipe.getShape()).getValue(Attribute.RANGE);
        builder.put((Attributes.ENTITY_INTERACTION_RANGE), new AttributeModifier(BASE_RANGE_UUID, (double)range, AttributeModifier.Operation.ADD_VALUE));
        baseAttrs.forEach((a, m) -> {
            if (a == Attributes.ENTITY_INTERACTION_RANGE || a == Attributes.ATTACK_DAMAGE) {
                return;
            }
            builder.put(a, m);
        });
        return builder.build();
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Player player;
        SpellRecipe recipe = this.getRecipe(stack);
        if (attacker instanceof Player && recipe.isValid() && !(player = (Player)attacker).getCooldowns().isOnCooldown((Item)this)) {
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(c -> {
                if (c.getCastingResource().hasEnoughAbsolute((LivingEntity)player, recipe.getManaCost()) && this.affectTarget(recipe, attacker, target, InteractionHand.MAIN_HAND)) {
                    c.getCastingResource().consume((LivingEntity)player, recipe.getManaCost());
                }
            });
            player.getCooldowns().addCooldown((Item)this, (int)((float)recipe.getCooldown((LivingEntity)player) * 0.75f));
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        this.handleInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        SpellRecipe recipe = this.getRecipe(stack);
        recipe.addItemTooltip(stack, worldIn.level(), tooltip, (Player)mc.player);
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

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new BoundSwordItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.bound_swordaxe.idle")))});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}


