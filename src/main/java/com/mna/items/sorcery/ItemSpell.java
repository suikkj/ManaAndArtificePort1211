 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.items.IShowHud;
import com.mna.api.spells.SpellCastingResult;
import com.mna.api.spells.SpellCastingResultCode;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.effects.EffectInit;
import com.mna.gui.containers.providers.NamedSpellAdjust;
import com.mna.gui.containers.providers.NamedSpellCustomization;
import com.mna.items.ItemInit;
import com.mna.items.renderers.ItemSpellRenderer;
import com.mna.items.sorcery.EnchantedVellum;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import java.util.ArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;


public class ItemSpell
extends EnchantedVellum
implements IShowHud {
    public ItemSpell() {
        this(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    public ItemSpell(Item.Properties properties) {
        super(properties);
    }

    public static final void castSpellOnChannelTick(ItemStack stack, Player player, int itemInUseCount, BiPredicate<Player, ItemStack> consumeChanneledMana) {
        if (itemInUseCount == 0 || !consumeChanneledMana.test(player, stack) || player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SILENCE.get()))) {
            player.releaseUsingItem();
        }
    }

    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        if (entityLiving instanceof Player) {
            ((Player)entityLiving).releaseUsingItem();
        }
        return stack;
    }

    public static final InteractionResultHolder<ItemStack> castSpellOnUse(ItemStack spellStack, Level world, Player player, InteractionHand hand, Function<ItemStack, Boolean> consumeMana) {
        if (!SpellRecipe.stackContainsSpell(spellStack)) {
            return InteractionResultHolder.pass(spellStack);
        }
        player.swing(hand);
        SpellCastingResult result = SpellCaster.PlayerCast(spellStack, player, hand, player.position().add(0.0, (double)player.getEyeHeight(), 0.0), player.getLookAngle(), world, consumeMana.apply(spellStack), false);
        if (result.getCode() == SpellCastingResultCode.CHANNEL) {
            player.startUsingItem(hand);
            if (consumeMana.apply(spellStack).booleanValue()) {
                player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_STUNT.get()), spellStack.getUseDuration(), 5));
            }
            return InteractionResultHolder.success(spellStack);
        }
        if (result.getCode() == SpellCastingResultCode.SUCCESS) {
            ItemSpell.setCooldown(player, spellStack);
            return InteractionResultHolder.success(spellStack);
        }
        ItemSpell.setCooldown(player, spellStack);
        return InteractionResultHolder.success(spellStack);
    }

    public static final InteractionResult castSpellOnRightClickEntity(ItemStack spellStack, Level world, Player player, InteractionHand hand, Function<ItemStack, Boolean> consumeMana) {
        if (SpellRecipe.fromNBT(spellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()).isChanneled()) {
            return InteractionResult.PASS;
        }
        if (player.getCooldowns().isOnCooldown(spellStack.getItem())) {
            return InteractionResult.PASS;
        }
        return ItemSpell.castSpellOnUse(spellStack, world, player, hand, consumeMana).getResult();
    }

    protected boolean shouldConsumeMana(ItemStack stack) {
        return true;
    }

    protected boolean consumeChanneledMana(Player player, ItemStack stack) {
        return SpellCaster.consumeChanneledMana(player, stack);
    }

    protected static final void setCooldown(Player player, ItemStack stack) {
        if (stack.getItem() instanceof EnchantedVellum) {
            EnchantedVellum item = (EnchantedVellum)stack.getItem();
            SpellRecipe recipe = SpellRecipe.fromNBT(item.getSpellCompound(stack, player));
            SpellCaster.setCooldown(stack, player, recipe.getCooldown((LivingEntity)player));
        }
    }

    public static ItemStack createWTFBoomStack() {
        ItemStack spellStack = new ItemStack((ItemLike)ItemInit.SPELL.get());
        spellStack.set(DataComponents.ITEM_NAME, (Component)Component.literal((String)"L").withStyle(ChatFormatting.RED).append((Component)Component.literal((String)"M").withStyle(ChatFormatting.GOLD)).append((Component)Component.literal((String)"N").withStyle(ChatFormatting.GREEN)).append((Component)Component.literal((String)"O").withStyle(ChatFormatting.BLUE)).append((Component)Component.literal((String)"P").withStyle(ChatFormatting.LIGHT_PURPLE)));
        SpellRecipe recipe = new SpellRecipe();
        recipe.setShape(Shapes.SELF);
        recipe.addComponent(Components.LMNOP);
        recipe.writeToNBT(spellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        ItemSpell.setCustomIcon(spellStack, 328);
        return spellStack;
    }

    @Override
    protected ArrayList<Component> getFlavorText(ItemStack stack) {
        SpellRecipe recipe;
        if (SpellRecipe.stackContainsSpell(stack) && (recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())).isValid() && recipe.getComponents().stream().anyMatch(c -> c.getPart() == Components.LMNOP) && !recipe.isMysterious()) {
            ArrayList<Component> output = new ArrayList<Component>();
            output.add((Component)Component.translatable((String)"mna:components/wtfboom.warning"));
            return output;
        }
        return super.getFlavorText(stack);
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new ItemSpellRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() == ItemInit.SPELL.get() && ItemSpell.getCustomIcon(itemstack) == -1) {
            if (!world.isClientSide) {
                ((ServerPlayer)((ServerPlayer)player)).openMenu((MenuProvider)new NamedSpellCustomization());
            }
            return InteractionResultHolder.success(itemstack);
        }
        if (!SpellRecipe.isMysterious(itemstack) && this.openGuiIfModifierPressed(player.getItemInHand(hand), player, world)) {
            return InteractionResultHolder.success(itemstack);
        }
        if (player.getCooldowns().isOnCooldown((Item)this)) {
            return InteractionResultHolder.fail(itemstack);
        }
        return ItemSpell.castSpellOnUse(itemstack, world, player, hand, this::shouldConsumeMana);
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedSpellAdjust();
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
        return ItemSpell.castSpellOnRightClickEntity(stack, playerIn.level(), playerIn, hand, this::shouldConsumeMana);
    }

    public void onUseTick(Level pLevel, LivingEntity player, ItemStack stack, int count) {
        if (player instanceof Player) {
            ItemSpell.castSpellOnChannelTick(stack, (Player)player, count, this::consumeChanneledMana);
        }
    }

    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player) {
            Player player = (Player)entityLiving;
            SpellRecipe recipe = SpellRecipe.fromNBT(this.getSpellCompound(stack, player));
            ItemSpell.setCooldown(player, stack);
            if (this.shouldConsumeMana(stack)) {
                if (entityLiving.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_STUNT.get())) && entityLiving.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_STUNT.get())).getAmplifier() == 5) {
                    entityLiving.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_STUNT.get()));
                }
                SpellCaster.AddAffinityAndMagicXP(recipe, player, recipe.getMaxChannelTime() - timeLeft);
            }
        }
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return armorType == EquipmentSlot.MAINHAND || armorType == EquipmentSlot.OFFHAND;
    }

    public int getUseDuration(ItemStack stack) {
        SpellRecipe recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        int base = recipe.getMaxChannelTime();
        if (EnchantmentHelper.hasChanneling((ItemStack)stack)) {
            base *= 2;
        }
        return base;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    @Override
    protected Component getTranslatedDisplayName(SpellRecipe recipe) {
        if (recipe.isMysterious()) {
            return Component.translatable((String)"item.mna.spell.mysterious");
        }
        MutableComponent shape = Component.translatable((String)((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).getRegistryName().toString());
        StringBuilder sb = new StringBuilder();
        recipe.iterateComponents(c -> {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(Component.translatable((String)((SpellEffect)c.getPart()).getRegistryName().toString()).getString());
        });
        MutableComponent base = Component.translatable((String)"item.mna.spell.display_name", (Object[])new Object[]{shape.getString(), sb.toString()});
        return base;
    }

    public static void setCustomIcon(ItemStack stack, int index) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("icon", index);
    }

    public static int getCustomIcon(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("icon")) {
            return tag.getInt("icon");
        }
        return -1;
    }

    @Override
    public boolean requiresModifierKey() {
        return true;
    }
}


