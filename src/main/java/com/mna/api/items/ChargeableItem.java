 
package com.mna.api.items;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.CurioItemCapability;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.items.IShowHud;
import com.mna.api.items.ManaBatteryItem;
import com.mna.api.items.TieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.apache.commons.lang3.mutable.MutableBoolean;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import com.mna.api.tools.MNACap;

public abstract class ChargeableItem
extends TieredItem
implements IShowHud,
IItemExtension,
IFactionSpecific {
    private static final String KEY_NBT_MANA = "mana";
    private float max_mana;

    public ChargeableItem(Item.Properties properties, float maxMana) {
        super(properties.durability(100));
        this.max_mana = maxMana;
    }

    public boolean consumeMana(ItemStack stack, float amount, @Nullable Player player) {
        CompoundTag tag;
        if (player != null) {
            this.usedByPlayer(player);
        }
        if (player != null && !(this instanceof ManaBatteryItem)) {
            List invItems = player.getInventory().items.stream().filter(i -> i.getItem() instanceof ManaBatteryItem).collect(Collectors.toList());
            for (ItemStack invStack : invItems) {
                ManaBatteryItem item = (ManaBatteryItem)invStack.getItem();
                float contained = item.getMana(invStack);
                float toConsume = amount;
                if (contained < amount) {
                    toConsume = contained;
                }
                if (item.consumeMana(invStack, toConsume, player)) {
                    amount -= toConsume;
                }
                if (!(amount <= 0.0f)) continue;
                return true;
            }
        }
        if ((tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()) == null) {
            return false;
        }
        float curMana = tag.getFloat(KEY_NBT_MANA);
        if (curMana < amount) {
            return false;
        }
        tag.putFloat(KEY_NBT_MANA, curMana -= amount);
        return true;
    }

    public float refundMana(ItemStack stack, float amount, @Nullable Player player) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag == null) {
            return 0.0f;
        }
        float curMana = tag.getFloat(KEY_NBT_MANA);
        if (curMana + amount > this.max_mana) {
            amount = this.max_mana - curMana;
        }
        tag.putFloat(KEY_NBT_MANA, curMana += amount);
        return amount;
    }

    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return 0;
    }

    public ICapabilityProvider initCapabilities(final ItemStack stack, CompoundTag nbt) {
        if (this.tickCurio()) {
            return CurioItemCapability.createProvider(new ICurioItem(){

                public void curioTick(top.theillusivec4.curios.api.SlotContext slotContext, ItemStack slotStack) {
                    LivingEntity livingEntity = slotContext.entity();
                    if (ChargeableItem.this.beforeCurioTick(livingEntity, slotContext.index(), stack)) {
                        ChargeableItem.this.internalInventoryTick((Entity)livingEntity, slotContext.index(), stack, false);
                    }
                }

                public ItemStack getStack() {
                    return stack;
                }
            });
        }
        return super.initCapabilities(stack, nbt);
    }

    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (this.tickInventory()) {
            this.internalInventoryTick(entityIn, itemSlot, stack, isSelected);
        }
    }

    protected boolean beforeCurioTick(LivingEntity entity, int index, ItemStack stack) {
        return true;
    }

    private final void internalInventoryTick(Entity entity, int slot, ItemStack stack, boolean selected) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            float mana = this.getMana(stack);
            if (mana >= this.manaPerOperation() && this.tickEffect(stack, player, player.level(), slot, mana, selected)) {
                this.consumeMana(stack, this.manaPerOperation(), player);
            }
        }
    }

    public final boolean applyEffect(ItemStack stack, Player player, Level world, int slot, float mana, boolean selected) {
        float curMana = this.getMana(stack);
        if (curMana < mana) {
            return false;
        }
        if (this.tickEffect(stack, player, world, slot, curMana, selected)) {
            this.consumeMana(stack, this.manaPerOperation(), player);
            return true;
        }
        return false;
    }

    protected abstract boolean tickEffect(ItemStack var1, Player var2, Level var3, int var4, float var5, boolean var6);

    protected float manaPerRechargeTick() {
        return 10.0f;
    }

    public final float getMana(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getFloat(KEY_NBT_MANA);
        }
        return 0.0f;
    }

    public final float getMaxMana() {
        return this.max_mana;
    }

    protected boolean tickCurio() {
        return true;
    }

    protected boolean tickInventory() {
        return false;
    }

    protected float manaPerOperation() {
        return 1.0f;
    }

    protected float minimumManaToStartChanneling() {
        return this.manaPerRechargeTick() * 5.0f;
    }

    protected int repairPerRechargeTick() {
        return 10;
    }

    protected boolean canChannelRepair() {
        return true;
    }

    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return this.canChannelRepair() && this.getMana(newStack) < this.getMaxMana();
    }

    public int getUseDuration(ItemStack stack) {
        return 9999;
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (stack.getItem() != this) {
            return InteractionResultHolder.fail(stack);
        }
        if (this.canChannelRepair() && this.getMana(stack) < this.max_mana) {
            MutableBoolean hasMana = new MutableBoolean(false);
            MNACap.get(playerIn, ManaAndArtificeMod.getMagicCapability()).ifPresent(m -> {
                if (m.getCastingResource().hasEnoughAbsolute((LivingEntity)playerIn, this.minimumManaToStartChanneling())) {
                    playerIn.startUsingItem(handIn);
                }
            });
            if (hasMana.getValue().booleanValue()) {
                return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
            }
        }
        return InteractionResultHolder.fail(playerIn.getItemInHand(handIn));
    }

    public void onUseTick(Level pLevel, LivingEntity living, ItemStack stack, int pRemainingUseDuration) {
        if (living instanceof Player) {
            if (!living.level().isClientSide()) {
                ((Player)living).getCapability(ManaAndArtificeMod.getMagicCapability()).ifPresent(m -> {
                    if (m.getCastingResource().hasEnoughAbsolute(living, this.manaPerRechargeTick()) && this.getMana(stack) < this.getMaxMana()) {
                        m.getCastingResource().consume(living, this.manaPerRechargeTick());
                        this.refundMana(stack, ((Player)living).isCreative() ? (float)(this.repairPerRechargeTick() * 100) : (float)this.repairPerRechargeTick(), (Player)living);
                    } else {
                        ((Player)living).stopUsingItem();
                    }
                });
            } else {
                living.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), living.getX() - 0.5 + Math.random(), living.getY() + Math.random(), living.getZ() - 0.5 + Math.random(), 0.0, (double)0.1f, 0.0);
            }
        }
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add((Component)Component.translatable((String)"item.mna.chargeable.mana", (Object[])new Object[]{Float.valueOf(this.getMana(stack)), Float.valueOf(this.max_mana)}).withStyle(ChatFormatting.AQUA));
        tooltip.add((Component)Component.translatable((String)"item.mna.chargeable.pedestalCharge").withStyle(ChatFormatting.AQUA));
        if (this.canChannelRepair()) {
            tooltip.add((Component)Component.translatable((String)"item.mna.chargeable.rightClickCharge").withStyle(ChatFormatting.AQUA));
        }
    }

    public boolean isEquippedAndHasMana(LivingEntity living, float mana, boolean autoConsume) {
        MutableBoolean valid = new MutableBoolean(false);
        CuriosApi.getCuriosHelper().findFirstCurio(living, (Item)this).ifPresent(t -> {
            if (this.getMana(t.stack()) >= mana) {
                if (autoConsume && living instanceof Player) {
                    this.consumeMana(t.stack(), mana, (Player)living);
                }
                valid.setTrue();
            }
        });
        return valid.booleanValue();
    }

    public int getBarColor(ItemStack stack) {
        return FastColor.ARGB32.color((int)255, (int)128, (int)64, (int)255);
    }

    public int getBarWidth(ItemStack stack) {
        float mana = this.getMana(stack);
        float pct = mana / this.max_mana;
        return Math.round(13.0f * pct);
    }

    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }
}


