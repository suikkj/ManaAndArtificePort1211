 
package com.mna.items.runes;

import net.minecraft.core.component.DataComponents;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.IAreaItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.gui.containers.providers.NamedAreaAdjust;
import com.mna.items.ItemInit;
import com.mna.items.base.IItemWithGui;
import com.mna.items.runes.Rune;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import com.mna.api.tools.MNADyeableItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import com.mna.api.tools.MNACap;
import com.mna.api.tools.MNAItemData;

public class RuneMarkingPair
extends Rune
implements IItemWithGui<RuneMarkingPair>,
IAreaItem<RuneMarkingPair>,
MNADyeableItem {
    public int getMaxStackSize(ItemStack stack) {
        return this.getBounds(stack) == null ? 64 : 1;
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack held = pPlayer.getItemInHand(pUsedHand);
        if (pLevel.isClientSide) {
            return InteractionResultHolder.pass(held);
        }
        if (pUsedHand == InteractionHand.MAIN_HAND) {
            AABB bounds = this.getBounds(held);
            if (bounds == null) {
                pPlayer.sendSystemMessage((Component)Component.translatable((String)"item.mna_rune_marking_pair.no_area_no_gui"));
                return InteractionResultHolder.consume(held);
            }
            if (IItemWithGui.super.openGuiIfModifierPressed(this.getDefaultInstance(), pPlayer, pLevel)) {
                return InteractionResultHolder.consume(held);
            }
        }
        return InteractionResultHolder.pass(held);
    }

    public InteractionResult useOn(UseOnContext context) {
        AABB bounds;
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
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
        if (context.getHand() == InteractionHand.MAIN_HAND && (bounds = this.getBounds(context.getItemInHand())) != null && IItemWithGui.super.openGuiIfModifierPressed(this.getDefaultInstance(), context.getPlayer(), context.getLevel())) {
            return InteractionResult.CONSUME;
        }
        ItemStack stack = context.getItemInHand();
        boolean didSplit = false;
        if (stack.getCount() > 1) {
            stack.shrink(1);
            stack = new ItemStack((ItemLike)ItemInit.RUNE_MARKING_PAIR.get());
            didSplit = true;
        }
        this.scanAndSetArea(stack, (LevelReader)context.getLevel(), context.getClickedPos(), 4);
        if (didSplit && !context.getPlayer().addItem(stack)) {
            context.getPlayer().drop(stack, true);
        }
        return InteractionResult.SUCCESS;
    }

    private void scanAndSetArea(ItemStack stack, LevelReader reader, BlockPos center, int maxRadius) {
        BlockState centerState = reader.getBlockState(center);
        int minX = 0;
        int minY = 0;
        int minZ = 0;
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        for (int i = -maxRadius; i <= maxRadius; ++i) {
            for (int j = -maxRadius; j <= maxRadius; ++j) {
                for (int k = -maxRadius; k <= maxRadius; ++k) {
                    BlockPos offset;
                    BlockState offsetState;
                    if (i == 0 && j == 0 && k == 0 || !(offsetState = reader.getBlockState(offset = center.offset(i, j, k))).is(centerState.getBlock())) continue;
                    if (minX > i) {
                        minX = i;
                    }
                    if (maxX < i) {
                        maxX = i;
                    }
                    if (minY > j) {
                        minY = j;
                    }
                    if (maxY < j) {
                        maxY = j;
                    }
                    if (minZ > k) {
                        minZ = k;
                    }
                    if (maxZ >= k) continue;
                    maxZ = k;
                }
            }
        }
        this.setArea(stack, center.offset(minX, minY, minZ), center.offset(maxX, maxY, maxZ));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        AABB area = this.getBounds(stack);
        if (area != null) {
            tooltip.add((Component)Component.translatable((String)"item.mna.rune_marking_pair.setArea", (Object[])new Object[]{area.minX, area.minY, area.minZ, area.maxX, area.maxY, area.maxZ}));
        } else {
            tooltip.add((Component)Component.translatable((String)"item.mna.rune_marking_pair.noPosition"));
        }
        IItemWithGui.super.appendHoverText(stack, worldIn, tooltip, flagIn);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public boolean isFoil(ItemStack stack) {
        return this.getBounds(stack) != null;
    }

    @Override
    public void setArea(ItemStack stack, BlockPos center, int radius) {
        CompoundTag nbt = MNAItemData.element(stack, "mark");
        this.writeAABB(nbt, new AABB(center).inflate((double)radius));
    }

    @Override
    public void setArea(ItemStack stack, BlockPos corner_a, BlockPos corner_b) {
        CompoundTag nbt = MNAItemData.element(stack, "mark");
        this.writeAABB(nbt, new AABB(corner_a, corner_b));
    }

    @Override
    public void setArea(ItemStack stack, AABB aabb) {
        CompoundTag nbt = MNAItemData.element(stack, "mark");
        this.writeAABB(nbt, aabb);
    }

    @Override
    public AABB getBounds(ItemStack stack) {
        if (!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return null;
        }
        CompoundTag nbt = MNAItemData.element(stack, "mark");
        return this.readAABB(nbt);
    }

    @Override
    public void copyAreaFrom(ItemStack source, ItemStack destination) {
        if (!(source.getItem() instanceof IAreaItem) || !(destination.getItem() instanceof IAreaItem)) {
            return;
        }
        CompoundTag srcNbt = MNAItemData.element(source, "mark");
        CompoundTag destNbt = MNAItemData.element(destination, "mark");
        destNbt.putInt("minx", srcNbt.getInt("minx"));
        destNbt.putInt("miny", srcNbt.getInt("miny"));
        destNbt.putInt("minz", srcNbt.getInt("minz"));
        destNbt.putInt("maxx", srcNbt.getInt("maxx"));
        destNbt.putInt("maxy", srcNbt.getInt("maxy"));
        destNbt.putInt("maxz", srcNbt.getInt("maxz"));
    }

    private void writeAABB(CompoundTag tag, AABB aabb) {
        tag.putDouble("minx", aabb.minX);
        tag.putDouble("miny", aabb.minY);
        tag.putDouble("minz", aabb.minZ);
        tag.putDouble("maxx", aabb.maxX);
        tag.putDouble("maxy", aabb.maxY);
        tag.putDouble("maxz", aabb.maxZ);
    }

    private AABB readAABB(CompoundTag tag) {
        if (tag.contains("minx") && tag.contains("miny") && tag.contains("minz") && tag.contains("maxx") && tag.contains("maxy") && tag.contains("maxz")) {
            return new AABB(tag.getDouble("minx"), tag.getDouble("miny"), tag.getDouble("minz"), tag.getDouble("maxx"), tag.getDouble("maxy"), tag.getDouble("maxz"));
        }
        return null;
    }

    @Override
    public void adjust(ItemStack stack, Vector3f amount, boolean grow) {
        AABB aabb = this.getBounds(stack);
        aabb = grow ? aabb.expandTowards((double)amount.x, (double)amount.y, (double)amount.z) : aabb.contract((double)amount.x, (double)amount.y, (double)amount.z);
        this.setArea(stack, aabb);
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedAreaAdjust();
    }
}

