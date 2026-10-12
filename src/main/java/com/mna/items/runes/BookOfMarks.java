 
package com.mna.items.runes;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.IPositionalItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.providers.NamedMarkBook;
import com.mna.inventory.ItemInventoryBase;
import com.mna.items.ItemInit;
import com.mna.items.base.IRadialInventorySelect;
import com.mna.items.base.ItemBagBase;
import com.mna.items.filters.ItemFilterGroup;
import com.mna.items.renderers.books.MarkBookRenderer;
import com.mna.items.runes.ItemRuneMarking;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import com.mna.api.tools.MNACap;


public class BookOfMarks
extends ItemBagBase
implements IRadialInventorySelect,
IPositionalItem<BookOfMarks> {
    public static final int INVENTORY_SIZE = 16;

    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!context.getLevel().isClientSide()) {
            IPlayerMagic magic = MNACap.get((IPlayerMagic)context.getPlayer(), PlayerMagicProvider.MAGIC).orElse(null);
            if (!magic.isMagicUnlocked()) {
                if (context.getLevel().isClientSide()) {
                    context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.rune_marking.no_magic").withStyle(ChatFormatting.GOLD));
                }
                return InteractionResult.FAIL;
            }
            if (context.getHand() == InteractionHand.OFF_HAND) {
                return InteractionResult.FAIL;
            }
            this.setLocation(stack, context.getClickedPos(), context.getClickedFace(), context.getLevel());
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new MarkBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    @Override
    public ItemFilterGroup filterGroup() {
        return ItemFilterGroup.ANY_MARKING_RUNE;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedMarkBook(stack);
    }

    @Override
    public int getIndex(ItemStack stack) {
        if (stack.getItem() == ItemInit.BOOK_MARKS.get() && stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains("index")) {
            return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("index");
        }
        return 0;
    }

    @Override
    public void setIndex(ItemStack stack, int index) {
        if (stack.getItem() == ItemInit.BOOK_MARKS.get()) {
            stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("index", index);
        }
    }

    @Override
    public void setLocation(ItemStack stack, DirectionalPoint point) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(stack, point);
        } else if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(invStack, point);
        }
    }

    @Override
    public void setLocation(ItemStack stack, BlockPos pos, Direction face) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(stack, pos, face);
        } else if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(invStack, pos, face);
        }
    }

    @Override
    public void setLocation(ItemStack stack, BlockPos pos, Direction face, Level world) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(stack, pos, face, world);
        } else if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setLocation(invStack, pos, face, world);
            inv.writeItemStack();
        }
    }

    @Override
    public ResourceLocation getMarkedBlockId(ItemStack stack) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getMarkedBlockId(stack);
        }
        if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getMarkedBlockId(stack);
        }
        return null;
    }

    @Override
    public void copyPositionFrom(ItemStack source, ItemStack destination) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (source.getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).copyPositionFrom(source, destination);
        } else if (source.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(source)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(source))).getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).copyPositionFrom(invStack, destination);
        }
    }

    @Override
    public BlockPos getLocation(ItemStack stack) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getLocation(stack);
        }
        if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getLocation(invStack);
        }
        return null;
    }

    @Override
    public Direction getFace(ItemStack stack) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getFace(stack);
        }
        if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getFace(invStack);
        }
        return Direction.DOWN;
    }

    @Override
    public Component getBlockName(ItemStack stack) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getBlockName(stack);
        }
        if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getBlockName(invStack);
        }
        return null;
    }

    @Override
    public DirectionalPoint getDirectionalPoint(ItemStack stack) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getDirectionalPoint(stack);
        }
        if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getDirectionalPoint(invStack);
        }
        return null;
    }

    @Override
    public String getBlockTranslateKey(ItemStack stack) {
        int index;
        ItemInventoryBase inv;
        ItemStack invStack;
        if (stack.getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getBlockTranslateKey(stack);
        }
        if (stack.getItem() == ItemInit.BOOK_MARKS.get() && (invStack = (inv = new ItemInventoryBase(stack)).getStackInSlot(index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack))).getItem() == ItemInit.RUNE_MARKING.get()) {
            return ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getBlockTranslateKey(invStack);
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ItemInventoryBase inv = new ItemInventoryBase(stack);
        int index = ((BookOfMarks)ItemInit.BOOK_MARKS.get()).getIndex(stack);
        ItemStack invStack = inv.getStackInSlot(index);
        if (invStack.getItem() == ItemInit.RUNE_MARKING.get()) {
            ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).appendHoverText(invStack, worldIn, tooltip, flagIn);
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        IRadialInventorySelect.super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public int capacity() {
        return 16;
    }
}


