 
package com.mna.items.relic;

import com.mna.tools.EnchantmentUtils;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.api.items.IRelic;
import com.mna.api.items.IShowHud;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedSpellEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.gui.containers.providers.NamedAstroBlade;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.base.ItemBagBase;
import com.mna.items.filters.ItemFilterGroup;
import com.mna.items.renderers.AstroBladeRenderer;
import com.mna.items.sorcery.ItemSpell;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbilities;

import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;
import com.mna.api.enchantments.MNAEnchantmentRegistry;

public class AstroBlade
extends ItemBagBase
implements IShowHud,
IRelic {
    public static final String NBT_AS_WARDEN = "as_warden";
    private final ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> allowedEnchantments = new ArrayList();
    public static final int INVENTORY_SIZE = 2;
    public static final int SLOT_HANDLE = 0;
    public static final int SLOT_BLADE = 1;
    private final Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> defaultModifiers;

    public AstroBlade() {
        super(new Item.Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(1));
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 10.0, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)-2.4f, AttributeModifier.Operation.ADD_VALUE));
        this.defaultModifiers = builder.build();
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new AstroBladeRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
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

    @Override
    public ItemFilterGroup filterGroup() {
        return ItemFilterGroup.ANY_SPELL;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedAstroBlade(stack);
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        SpellRecipe recipe;
        ItemInventoryBase inv = new ItemInventoryBase(stack);
        ItemStack bladeSlot = inv.getStackInSlot(1);
        if (bladeSlot.getItem() == ItemInit.ENCHANTED_VELLUM.get()) {
            return super.onLeftClickEntity(stack, player, entity);
        }
        if (!bladeSlot.isEmpty() && SpellRecipe.stackContainsSpell(bladeSlot) && !player.level().isClientSide() && (recipe = SpellRecipe.fromNBT(bladeSlot.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag())).isValid()) {
            MutableBoolean consumed = new MutableBoolean(false);
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(c -> {
                if (c.getCastingResource().hasEnoughAbsolute((LivingEntity)player, recipe.getManaCost())) {
                    c.getCastingResource().consume((LivingEntity)player, recipe.getManaCost());
                    consumed.setTrue();
                }
            });
            if (consumed.getValue().booleanValue()) {
                SpellSource source = new SpellSource((LivingEntity)player, InteractionHand.MAIN_HAND);
                SpellContext context = new SpellContext(player.level(), recipe);
                recipe.iterateComponents(c -> {
                    int delay = (int)(c.getValue(Attribute.DELAY) * 20.0f);
                    boolean appliedComponent = false;
                    if (delay > 0) {
                        DelayedEventQueue.pushEvent(player.level(), new TimedDelayedSpellEffect(((SpellEffect)c.getPart()).getRegistryName().toString(), delay, source, new SpellTarget(entity), (IModifiedSpellPart<SpellEffect>)c, context));
                        appliedComponent = true;
                    } else if (((SpellEffect)c.getPart()).ApplyEffect(source, new SpellTarget(entity), (IModifiedSpellPart<SpellEffect>)c, context) == ComponentApplicationResult.SUCCESS) {
                        appliedComponent = true;
                    }
                    if (appliedComponent) {
                        SpellCaster.addComponentRoteProgress(player, (SpellEffect)c.getPart());
                    }
                });
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            if (this.openGuiIfModifierPressed(held, player, world)) {
                return new InteractionResultHolder(InteractionResult.SUCCESS, held);
            }
            ItemInventoryBase inv = new ItemInventoryBase(held);
            ItemStack hiltSlot = inv.getStackInSlot(0);
            if (hiltSlot.getItem() == ItemInit.ENCHANTED_VELLUM.get()) {
                return new InteractionResultHolder(InteractionResult.PASS, player.getItemInHand(hand));
            }
            if (!hiltSlot.isEmpty()) {
                ItemSpell.castSpellOnUse(hiltSlot, world, player, hand, this::shouldConsumeMana);
                return new InteractionResultHolder(InteractionResult.SUCCESS, held);
            }
        }
        return new InteractionResultHolder(InteractionResult.PASS, player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add((Component)Component.translatable((String)"item.mna.astro_blade.lore_1").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        tooltip.add((Component)Component.translatable((String)"item.mna.astro_blade.lore_2").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        tooltip.add((Component)Component.literal((String)"  "));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public net.minecraft.world.item.component.ItemAttributeModifiers getDefaultAttributeModifiers() {
        net.minecraft.world.item.component.ItemAttributeModifiers.Builder builder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
        this.defaultModifiers.forEach((attribute, modifier) -> builder.add(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifier, net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND));
        return builder.build();
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

    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        if (pState.is(Blocks.COBWEB)) {
            return 15.0f;
        }
        return pState.is(BlockTags.SWORD_EFFICIENT) ? 1.5f : 1.0f;
    }

    public boolean isCorrectToolForDrops(BlockState pBlock) {
        return pBlock.is(Blocks.COBWEB);
    }

    public boolean canPerformAction(ItemStack stack, ItemAbilities toolAction) {
        return ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }

    protected ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> getAllowedEnchantments() {
        this.allowedEnchantments.clear();
        if (this.allowedEnchantments.size() == 0) {
            this.allowedEnchantments.add(Enchantments.BANE_OF_ARTHROPODS);
            this.allowedEnchantments.add(Enchantments.FIRE_ASPECT);
            this.allowedEnchantments.add(Enchantments.KNOCKBACK);
            this.allowedEnchantments.add(Enchantments.LOOTING);
            this.allowedEnchantments.add(Enchantments.SMITE);
            this.allowedEnchantments.add(EnchantmentInit.BEHEADING.getKey());
            this.allowedEnchantments.add(EnchantmentInit.DURATION_MOD.getKey());
            this.allowedEnchantments.add(EnchantmentInit.RANGE_MOD.getKey());
            this.allowedEnchantments.add(EnchantmentInit.SPEED_MOD.getKey());
            this.allowedEnchantments.add(Enchantments.AQUA_AFFINITY);
            this.allowedEnchantments.add(Enchantments.CHANNELING);
            this.allowedEnchantments.add(Enchantments.EFFICIENCY);
            this.allowedEnchantments.add(Enchantments.FLAME);
            this.allowedEnchantments.add(Enchantments.LOYALTY);
            this.allowedEnchantments.add(Enchantments.LURE);
            this.allowedEnchantments.add(Enchantments.POWER);
            this.allowedEnchantments.add(Enchantments.QUICK_CHARGE);
            this.allowedEnchantments.add(Enchantments.THORNS);
            this.allowedEnchantments.add(Enchantments.FROST_WALKER);
            this.allowedEnchantments.add(Enchantments.SHARPNESS);
            this.allowedEnchantments.add(Enchantments.SWEEPING_EDGE);
        }
        return this.allowedEnchantments;
    }

    protected boolean shouldConsumeMana(ItemStack stack) {
        return true;
    }

    protected boolean shouldConsumeChanneledMana(Player player, ItemStack stack) {
        return true;
    }
}


