 
package com.mna.items.relic;

import java.util.function.Supplier;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.items.IRelic;
import com.mna.gui.containers.providers.NamedRunicMalus;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.base.ItemBagBase;
import com.mna.items.filters.ItemFilterGroup;
import com.mna.items.renderers.obj_gecko.RunicMalusRenderer;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RunicMalus
extends ItemBagBase
implements GeoItem,
IRelic {
    public static final int INVENTORY_SIZE = 6;
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private static final String CONTROLLER_NAME = "runicMalusController";
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public RunicMalus() {
        super(new Item.Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(1));
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 10.0, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("mna", "e6b1b07c-9824-4d3e-be88-24714cc3fe0c"), 2.0, AttributeModifier.Operation.ADD_VALUE));
        this.defaultModifiers = builder.build();
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new RunicMalusRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public ItemFilterGroup filterGroup() {
        return ItemFilterGroup.ANY_ENCHANTED_RUNE;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedRunicMalus(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide && this.openGuiIfModifierPressed(player.getItemInHand(hand), player, world)) {
            return new InteractionResultHolder(InteractionResult.SUCCESS, player.getItemInHand(hand));
        }
        return new InteractionResultHolder(InteractionResult.PASS, player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add((Component)Component.translatable((String)"item.mna.runic_malus.lore_1").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        tooltip.add((Component)Component.translatable((String)"item.mna.runic_malus.lore_2").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        tooltip.add((Component)Component.literal((String)"  "));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot p_43274_) {
        return p_43274_ == EquipmentSlot.MAINHAND ? this.defaultModifiers : (com.google.common.collect.Multimap)super.getDefaultAttributeModifiers();
    }

    public static NonNullList<ItemStack> getRunes(ItemStack itemStack) {
        return new ItemInventoryBase(itemStack, 6).getAllItems();
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.malus.idle")))});
    }
}


