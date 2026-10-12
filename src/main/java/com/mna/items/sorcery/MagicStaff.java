 
package com.mna.items.sorcery;

import com.mna.tools.EnchantmentUtils;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mna.Registries;
import com.mna.api.items.ITieredItem;
import com.mna.api.items.ItemUtils;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.tools.CollectionUtils;
import com.mna.api.tools.MATags;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.ItemSpell;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import com.mna.api.enchantments.MNAEnchantmentRegistry;

public class MagicStaff
extends ItemSpell
implements ITieredItem<MagicStaff> {
    private static final String KEY_CHARGES = "charges";
    protected float attackDamage = 4.5f;
    private final int enchantability = 25;
    private static int charge_modifier = 40;
    private int tier = -1;
    private final ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> allowedEnchantments = new ArrayList();
    private final Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> attributeModifiers;

    public MagicStaff(float attackDamage) {
        this(attackDamage, -3.0f);
    }

    public MagicStaff(Item.Properties properties, float attackDamage, float attackSpeed) {
        super(properties);
        this.attackDamage = attackDamage;
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)this.attackDamage, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)attackSpeed, AttributeModifier.Operation.ADD_VALUE));
        this.attributeModifiers = builder.build();
    }

    public MagicStaff(float attackDamage, float attackSpeed) {
        super(new Item.Properties().defaultDurability(100).durability(100).setNoRepair());
        this.attackDamage = attackDamage;
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)this.attackDamage, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)attackSpeed, AttributeModifier.Operation.ADD_VALUE));
        this.attributeModifiers = builder.build();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean addItemWithGuiTooltip(ItemStack stack) {
        return this.shouldConsumeMana(stack);
    }

    @Override
    public boolean openGuiIfModifierPressed(ItemStack stack, Player player, Level world) {
        if (!this.shouldConsumeMana(stack)) {
            return false;
        }
        return super.openGuiIfModifierPressed(stack, player, world);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack activeStack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide && !this.shouldConsumeMana(activeStack)) {
            this.consumeCharge(activeStack, playerIn, handIn);
        }
        return super.use(worldIn, playerIn, handIn);
    }

    public int getMaxDamage(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return super.getMaxDamage(stack);
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(KEY_CHARGES)) {
            return super.getMaxDamage(stack);
        }
        return tag.getInt(KEY_CHARGES);
    }

    public int getBarColor(ItemStack stack) {
        return FastColor.ARGB32.color((int)255, (int)128, (int)64, (int)255);
    }

    public int getBarWidth(ItemStack stack) {
        SpellRecipe recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        int chargeConsumed = (int)Math.ceil(recipe.getManaCost());
        if (!recipe.isChanneled()) {
            chargeConsumed *= charge_modifier;
        }
        int charges = ItemUtils.getCharges(stack) - chargeConsumed;
        int maxCharges = Math.max(ItemUtils.getMaxCharges(stack), 1);
        float pct = (float)charges / (float)maxCharges;
        return Math.round(13.0f * pct);
    }

    public boolean isBarVisible(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(KEY_CHARGES);
    }

    @Override
    protected boolean shouldConsumeMana(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return true;
        }
        return !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_CHARGES);
    }

    @Override
    protected boolean consumeChanneledMana(Player player, ItemStack stack) {
        if (this.shouldConsumeMana(stack)) {
            return super.consumeChanneledMana(player, stack);
        }
        this.consumeCharge(stack, player, player.getUsedItemHand());
        return true;
    }

    public void consumeCharge(ItemStack spellStack, Player player, @Nullable InteractionHand handIn) {
        if (player.isCreative()) {
            return;
        }
        SpellRecipe recipe = SpellRecipe.fromNBT(spellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (!recipe.isValid()) {
            return;
        }
        int chargeConsumed = (int)Math.ceil(recipe.getManaCost());
        if (!recipe.isChanneled()) {
            chargeConsumed *= charge_modifier;
        }
        int charges = ItemUtils.getCharges(spellStack) - chargeConsumed;
        int maxCharges = Math.max(ItemUtils.getMaxCharges(spellStack), 1);
        ItemUtils.writeCharges(spellStack, charges);
        float damagePct = 1.0f - (float)charges / (float)maxCharges;
        int newDamage = (int)((float)spellStack.getMaxDamage() * damagePct);
        if (charges <= 0) {
            if (handIn != null) {
                player.broadcastBreakEvent(handIn == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
            Item item = spellStack.getItem();
            spellStack.shrink(1);
            player.awardStat(Stats.ITEM_BROKEN.get(item));
            spellStack.setDamageValue(0);
        } else {
            spellStack.setDamageValue(newDamage);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    public Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot, stack);
    }

    public int getEnchantmentValue(ItemStack stack) {
        return 25;
    }

    @Override
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
    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        SpellRecipe recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (recipe != null && !recipe.isMysterious()) {
            int charges;
            int chargeConsumed = (int)Math.ceil(recipe.getManaCost());
            if (!recipe.isChanneled()) {
                chargeConsumed *= charge_modifier;
            }
            if ((charges = (int)Math.ceil((float)ItemUtils.getCharges(stack) / Math.max((float)chargeConsumed, 1.0f))) > 0) {
                MutableComponent comp = Component.translatable((String)"item.mna.spell.charges_display", (Object[])new Object[]{charges}).withStyle(ChatFormatting.AQUA);
                tooltip.add((Component)comp);
            }
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public static ItemStack buildRandomSpellStaff(float chanceForForceSelf, float chanceForNoHelpfulLogic, boolean allowMessWithMori, ServerLevel level) {
        boolean skipTags;
        boolean forceSelf;
        Random random = new Random();
        ItemStack stack = MATags.getRandomItemFrom(MATags.Items.GENERATED_SPELL_ITEMS);
        if (stack.isEmpty()) {
            stack = new ItemStack((ItemLike)ItemInit.STAFF_AUM.get());
        }
        SpellRecipe randomRecipe = new SpellRecipe();
        int numCharges = 20 + (int)(Math.random() * 180.0);
        boolean bl = forceSelf = Math.random() <= (double)chanceForForceSelf;
        boolean bl2 = forceSelf ? false : (skipTags = Math.random() <= (double)chanceForNoHelpfulLogic);
        if (!forceSelf) {
            List<Shape> shapeList = Registries.Shape.getValues().stream().filter(s -> s.canBeOnRandomStaff()).collect(Collectors.toList());
            MutableInt tierTotal = new MutableInt(0);
            shapeList.forEach(s -> tierTotal.add(s.getTier((Level)level)));
            shapeList.sort((a, b) -> {
                int bT;
                int aT = a.getTier((Level)level);
                if (aT < (bT = b.getTier((Level)level))) {
                    return -1;
                }
                if (aT > bT) {
                    return 1;
                }
                return 0;
            });
            double r = Math.random() * (double)tierTotal.getValue().intValue();
            Shape selectedShape = null;
            for (Shape shape : shapeList) {
                if (!((r -= (double)(6 - shape.getTier((Level)level))) <= 0.0)) continue;
                selectedShape = shape;
                break;
            }
            randomRecipe.setShape(selectedShape);
        } else {
            randomRecipe.setShape(Shapes.SELF);
        }
        MutableObject targetTag = new MutableObject(((Shape)((ModifiedSpellPart)randomRecipe.getShape()).getPart()).getUseTag());
        if (forceSelf) {
            double rnd = random.nextDouble();
            if (rnd < 0.05) {
                targetTag.setValue(SpellPartTags.DONOTUSE);
            } else if (rnd < 0.75) {
                targetTag.setValue(SpellPartTags.HARMFUL);
            } else {
                targetTag.setValue(SpellPartTags.SELF);
            }
        }
        int count = MathUtils.clamp(MathUtils.weightedRandomNumber(new int[]{89, 5, 3, 2, 1}) + 1, 1, 5);
        for (int i = 0; i < count; ++i) {
            SpellEffect c2 = CollectionUtils.getRandom(Registries.SpellEffect.getValues().stream().filter(comp -> {
                if (comp.getUseTag() == SpellPartTags.DONOTUSE && !skipTags || !comp.canBeOnRandomStaff()) {
                    return false;
                }
                List<SpellReagent> reagents = comp.getRequiredReagents(null, null);
                if (reagents != null && reagents.size() > 0) {
                    return false;
                }
                return skipTags ? true : CollectionUtils.componentMatchesShapeAndTag((Shape)((ModifiedSpellPart)randomRecipe.getShape()).getPart(), comp, (SpellPartTags)((targetTag.getValue())));
            }).collect(Collectors.toList())).orElse(null);
            if (c2 == null) continue;
            if (targetTag.getValue() == SpellPartTags.NEUTRAL) {
                targetTag.setValue(c2.getUseTag());
            }
            randomRecipe.addComponent(c2);
        }
        if (randomRecipe.getComponents().size() == 0) {
            return stack;
        }
        for (Attribute attr : ((ModifiedSpellPart)randomRecipe.getShape()).getContainedAttributes()) {
            if (attr == Attribute.DELAY) continue;
            ((ModifiedSpellPart)randomRecipe.getShape()).setValue(attr, ((ModifiedSpellPart)randomRecipe.getShape()).getMinimumValue(attr) + (((ModifiedSpellPart)randomRecipe.getShape()).getMaximumValue(attr) - ((ModifiedSpellPart)randomRecipe.getShape()).getMinimumValue(attr)) * (float)Math.random());
        }
        randomRecipe.iterateComponents(c -> {
            for (Attribute attr : c.getContainedAttributes()) {
                if (attr == Attribute.DELAY) continue;
                c.setValue(attr, c.getMinimumValue(attr) + (c.getMaximumValue(attr) - c.getMinimumValue(attr)) * 0.5f + ((float)Math.random() - 0.5f));
            }
        });
        if (!randomRecipe.isValid()) {
            return ItemStack.EMPTY;
        }
        randomRecipe.setMysterious(true);
        randomRecipe.writeToNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        int charges = (int)((float)numCharges * randomRecipe.getManaCost());
        ItemUtils.writeCharges(stack, charges *= charge_modifier);
        ItemUtils.writeMaxCharges(stack, charges);
        return stack;
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
            this.allowedEnchantments.add(EnchantmentInit.BLUDGEONING.getKey());
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
            this.buildAllowedEnchantments(this.allowedEnchantments);
        }
        return this.allowedEnchantments;
    }

    protected void buildAllowedEnchantments(ArrayList<net.minecraft.resources.ResourceKey<Enchantment>> defaultList) {
    }

    @Override
    public void setCachedTier(int tier) {
        this.tier = tier;
    }

    @Override
    public int getCachedTier() {
        return this.tier;
    }

    public boolean isChargeSpell(ItemStack stack) {
        return !this.shouldConsumeMana(stack);
    }
}


