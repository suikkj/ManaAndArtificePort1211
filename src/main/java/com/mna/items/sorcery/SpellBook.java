 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.ITieredItem;
import com.mna.api.items.inventory.ISpellBookInventory;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.providers.NamedSpellBook;
import com.mna.interop.CuriosInterop;
import com.mna.inventory.InventorySpellBook;
import com.mna.items.ItemInit;
import com.mna.items.base.IRadialMenuItem;
import com.mna.items.renderers.books.SpellBookRenderer;
import com.mna.items.sorcery.BookOfRote;
import com.mna.items.sorcery.ItemSpell;
import com.mna.network.ClientMessageDispatcher;
import com.mna.network.messages.to_server.SpellBookSlotChangeMessage;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import com.mna.api.tools.MNADyeableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.SlotContext;

public class SpellBook
extends ItemSpell
implements IRadialMenuItem,
ITieredItem<SpellBook>,
MNADyeableItem {
    public static final String KEY_ACTIVE_SLOT = "active_slot";
    protected static final String KEY_USE_DURATION = "cur_spell_use_duration";
    public static String NBT_ID = "mna:spell_book_data";
    public static final int MAX_ACTIVE_SLOT = 8;
    protected int tier = -1;
    public final boolean renderBookModel;
    public final boolean useMnABookModel;
    private final boolean renderInFirstPerson;
    protected final ResourceLocation open_model;
    protected final ResourceLocation closed_model;

    public SpellBook(boolean renderBookModel) {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
        this.renderBookModel = renderBookModel;
        this.useMnABookModel = true;
        this.open_model = null;
        this.closed_model = null;
        this.renderInFirstPerson = false;
    }

    public SpellBook(Item.Properties properties, @Nullable ResourceLocation openModel, @Nullable ResourceLocation closedModel, boolean renderInFirstPerson) {
        super(properties);
        this.useMnABookModel = openModel != null && closedModel != null;
        this.renderBookModel = this.useMnABookModel;
        this.open_model = openModel;
        this.closed_model = closedModel;
        this.renderInFirstPerson = renderInFirstPerson;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> {
                if (SpellBook.this.useMnABookModel) {
                    return new SpellBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
                }
                return new SpellBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels(), SpellBook.this.open_model, SpellBook.this.closed_model, SpellBook.this.renderInFirstPerson);
            };

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack book = player.getItemInHand(hand);
        player.swing(hand);
        if (!(world.isClientSide || hand != InteractionHand.OFF_HAND && this.openGuiIfModifierPressed(book, player, world) || !(book.getItem() instanceof SpellBook))) {
            return this.castCurrentSpell(book, player, world, hand);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer().isShiftKeyDown()) {
            return this.castCurrentSpell(stack, context.getPlayer(), context.getLevel(), context.getHand()).getResult();
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean castFromCurioSlot(ItemStack book, Player player) {
        ItemStack selectedStack = this.getSelectedStack(book, player);
        if (!selectedStack.isEmpty()) {
            return super.castFromCurioSlot(selectedStack, player);
        }
        return false;
    }

    private InteractionResultHolder<ItemStack> castCurrentSpell(ItemStack book, Player player, Level world, InteractionHand hand) {
        ItemStack selectedStack;
        if (player.getCooldowns().isOnCooldown((Item)this)) {
            return InteractionResultHolder.fail(book);
        }
        ISpellBookInventory bookInv = this.getInventory(book, (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get()));
        ItemStack itemStack = selectedStack = book.getItem() == ItemInit.GRIMOIRE.get() || book.getItem() instanceof BookOfRote ? bookInv.getActiveSpells()[SpellBook.getActiveSpellSlot(book)] : bookInv.getActiveSpells()[SpellBook.getActiveSpellSlot(book)];
        if (!selectedStack.isEmpty()) {
            InteractionResultHolder<ItemStack> result = ItemSpell.castSpellOnUse(selectedStack, world, player, hand, s -> true);
            switch (result.getResult()) {
                case CONSUME: 
                case CONSUME_PARTIAL: {
                    return InteractionResultHolder.consume(book);
                }
                case FAIL: {
                    return InteractionResultHolder.fail(book);
                }
                case PASS: {
                    return InteractionResultHolder.pass(book);
                }
                case SUCCESS: {
                    return InteractionResultHolder.success(book);
                }
            }
        }
        return InteractionResultHolder.fail(book);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        InventorySpellBook bookInv = new InventorySpellBook(stack);
        ItemStack selectedStack = bookInv.getStackInSlot(SpellBook.getActiveSpellSlot(stack) * 5);
        return super.getUseDuration(selectedStack);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity player, ItemStack stack, int count) {
        if (player instanceof Player) {
            ItemStack selectedStack = this.getSelectedStack(stack, (Player)player);
            super.onUseTick(pLevel, player, selectedStack, count);
        }
    }

    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains(NBT_ID)) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        }
        return null;
    }

    public static int getActiveSpellSlot(ItemStack item) {
        if (item.has(DataComponents.CUSTOM_DATA)) {
            return item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_ACTIVE_SLOT);
        }
        return 0;
    }

    @Nullable
    public ISpellBookInventory getInventory(ItemStack item, @Nullable IPlayerMagic magic) {
        return new InventorySpellBook(item);
    }

    public static void setSlot(Player player, ItemStack stack, int selectedSlot, InteractionHand hand, @Nullable SlotContext curioContext, boolean packet) {
        if (stack.getItem() instanceof SpellBook) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.putInt(KEY_ACTIVE_SLOT, selectedSlot);
            CompoundTag spellCompound = ((SpellBook)stack.getItem()).getSpellCompound(stack, player);
            tag.putInt(KEY_USE_DURATION, SpellRecipe.fromNBT(spellCompound).getMaxChannelTime());
            if (packet) {
                ClientMessageDispatcher.sendSpellBookSlotChange(selectedSlot, hand, curioContext);
            } else {
                ISpellBookInventory inv = ((SpellBook)stack.getItem()).getInventory(stack, (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get()));
                if (inv != null) {
                    ItemStack spell = inv.getActiveSpells()[selectedSlot % inv.getActiveSpells().length];
                    if (!spell.isEmpty()) {
                        stack.set(DataComponents.ITEM_NAME, spell.getHoverName());
                    } else {
                        stack.remove(DataComponents.CUSTOM_NAME);
                    }
                }
            }
        }
    }

    public static void handleSlotChangeMessage(SpellBookSlotChangeMessage msg, ServerPlayer player) {
        ItemStack stack;
        ItemStack itemStack = stack = msg.getContext() != null ? CuriosInterop.getCurioFromSlot((LivingEntity)player, msg.getContext()) : player.getItemInHand(msg.getHand());
        if (stack.getItem() instanceof SpellBook) {
            SpellBook.setSlot((Player)player, stack, msg.getSlot(), msg.getHand(), msg.getContext(), false);
        }
    }

    @Override
    public CompoundTag getSpellCompound(ItemStack stack, @Nullable Player player) {
        if (stack.getItem() instanceof SpellBook) {
            IItemHandler bookInv = (IItemHandler)this.getInventory(stack, null);
            ItemStack selectedStack = bookInv.getStackInSlot(SpellBook.getActiveSpellSlot(stack) * 5);
            return selectedStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        }
        return new CompoundTag();
    }

    protected ItemStack getSelectedStack(ItemStack bookStack, @Nullable Player player) {
        if (bookStack.getItem() instanceof SpellBook) {
            IItemHandler bookInv = (IItemHandler)this.getInventory(bookStack, null);
            ItemStack selectedStack = bookInv.getStackInSlot(SpellBook.getActiveSpellSlot(bookStack) * 5);
            return selectedStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedSpellBook(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        IRadialMenuItem.super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean canAcceptSpell(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canAcceptSpell(ItemStack stack, ISpellDefinition spell) {
        return false;
    }

    @Override
    public int getCachedTier() {
        return this.tier;
    }

    @Override
    public void setCachedTier(int tier) {
        this.tier = tier;
    }
}


