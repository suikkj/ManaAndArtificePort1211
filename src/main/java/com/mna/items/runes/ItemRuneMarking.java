 
package com.mna.items.runes;

import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.IPositionalItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.ItemInit;
import com.mna.items.renderers.MarkingRuneRenderer;
import com.mna.items.runes.Rune;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.mna.api.tools.MNADyeableItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;

public class ItemRuneMarking
extends Rune
implements IPositionalItem<ItemRuneMarking>,
MNADyeableItem {
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new MarkingRuneRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public int getMaxStackSize(ItemStack stack) {
        return this.getLocation(stack) == null ? 64 : 1;
    }

    public InteractionResult useOn(UseOnContext context) {
        BlockState state;
        ItemStack stack = context.getItemInHand();
        if (!context.getPlayer().isCrouching() && (state = context.getLevel().getBlockState(context.getClickedPos())).getBlock() == Blocks.WATER_CAULDRON) {
            return super.useOn(context);
        }
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
        boolean didSplit = false;
        if (stack.getCount() > 1) {
            stack.shrink(1);
            stack = new ItemStack((ItemLike)ItemInit.RUNE_MARKING.get());
            didSplit = true;
        }
        this.setLocation(stack, context.getClickedPos(), context.getClickedFace(), context.getLevel());
        if (didSplit && !context.getPlayer().addItem(stack)) {
            context.getPlayer().drop(stack, true);
        }
        return InteractionResult.SUCCESS;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        BlockPos pos = this.getLocation(stack);
        if (pos != null) {
            Direction face;
            tooltip.add((Component)Component.translatable((String)"item.mna.rune_marking.setPosition", (Object[])new Object[]{pos.getX(), pos.getY(), pos.getZ()}));
            MutableComponent block = (MutableComponent)this.getBlockName(stack);
            if (block != null) {
                tooltip.add((Component)Component.translatable((String)"item.mna.rune_marking.blockWhenSet", (Object[])new Object[]{block.getString()}).withStyle(ChatFormatting.GOLD));
            }
            if ((face = this.getFace(stack)) != null) {
                tooltip.add((Component)Component.translatable((String)"item.mna.rune_marking.face", (Object[])new Object[]{face.toString()}).withStyle(ChatFormatting.GOLD));
            }
        } else {
            tooltip.add((Component)Component.translatable((String)"item.mna.rune_marking.noPosition"));
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public boolean isFoil(ItemStack stack) {
        return this.getLocation(stack) != null;
    }

    @Override
    public void setLocation(ItemStack stack, BlockPos pos, Direction face, Level world) {
        CompoundTag nbt = stack.getOrCreateTagElement("mark");
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        nbt.putInt("face", face.get3DDataValue());
        if (world.isLoaded(pos)) {
            Block block = world.getBlockState(pos).getBlock();
            nbt.putString("block", new ItemStack((ItemLike)block).getDescriptionId());
            nbt.putString("block_id", BuiltInRegistries.BLOCK.getKey(block).toString());
        }
    }

    @Override
    public void setLocation(ItemStack stack, BlockPos pos, Direction face) {
        CompoundTag nbt = stack.getOrCreateTagElement("mark");
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        nbt.putInt("face", face.get3DDataValue());
    }

    @Override
    public void setLocation(ItemStack stack, DirectionalPoint point) {
        CompoundTag nbt = stack.getOrCreateTagElement("mark");
        nbt.putInt("x", point.getPosition().getX());
        nbt.putInt("y", point.getPosition().getY());
        nbt.putInt("z", point.getPosition().getZ());
        nbt.putInt("face", point.getDirection().get3DDataValue());
        if (point.getBlockName() != null) {
            nbt.putString("block", point.getBlockName());
        }
        if (point.getBlockID() != null) {
            nbt.putString("block_id", point.getBlockID());
        }
    }

    @Override
    @Nullable
    public DirectionalPoint getDirectionalPoint(ItemStack stack) {
        ResourceLocation block = this.getMarkedBlockId(stack);
        return new DirectionalPoint(this.getLocation(stack), this.getFace(stack), this.getBlockTranslateKey(stack), block != null ? block.toString() : null);
    }

    @Override
    @Nullable
    public BlockPos getLocation(ItemStack stack) {
        if (!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return null;
        }
        CompoundTag nbt = stack.getOrCreateTagElement("mark");
        if (nbt.contains("x") && nbt.contains("y") && nbt.contains("z")) {
            return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
        }
        return null;
    }

    @Override
    public Direction getFace(ItemStack stack) {
        if (!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return Direction.DOWN;
        }
        CompoundTag nbt = stack.getOrCreateTagElement("mark");
        if (nbt.contains("face")) {
            return Direction.from3DDataValue((int)nbt.getInt("face"));
        }
        return Direction.DOWN;
    }

    @Override
    @Nullable
    public String getBlockTranslateKey(ItemStack stack) {
        if (!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return null;
        }
        CompoundTag nbt = stack.getOrCreateTagElement("mark");
        return nbt.contains("block") ? nbt.getString("block") : null;
    }

    @Override
    public ResourceLocation getMarkedBlockId(ItemStack stack) {
        if (!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return null;
        }
        CompoundTag nbt = stack.getOrCreateTagElement("mark");
        return nbt.contains("block_id") ? ResourceLocation.parse(nbt.getString("block_id")) : null;
    }

    @Override
    @Nullable
    public Component getBlockName(ItemStack stack) {
        String blockID;
        if (!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return null;
        }
        CompoundTag nbt = stack.getOrCreateTagElement("mark");
        if (nbt.contains("block") && (blockID = nbt.getString("block")) != null) {
            return Component.translatable((String)blockID);
        }
        return null;
    }

    @Override
    public void copyPositionFrom(ItemStack source, ItemStack destination) {
        if (!(source.getItem() instanceof IPositionalItem) || !(destination.getItem() instanceof IPositionalItem)) {
            return;
        }
        CompoundTag srcNbt = source.getOrCreateTagElement("mark");
        CompoundTag destNbt = destination.getOrCreateTagElement("mark");
        destNbt.putInt("x", srcNbt.getInt("x"));
        destNbt.putInt("y", srcNbt.getInt("y"));
        destNbt.putInt("z", srcNbt.getInt("z"));
        destNbt.putInt("face", srcNbt.getInt("face"));
        destNbt.putString("block", srcNbt.getString("block"));
        if (srcNbt.contains("block_id")) {
            destNbt.putString("block_id", srcNbt.getString("block_id"));
        }
    }
}


