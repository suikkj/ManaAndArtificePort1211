 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.tile.TileEntityWithInventory;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.SpellBook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNADyeableItem;
import com.mna.api.tools.MNAItemData;

public class BookshelfTile
extends TileEntityWithInventory {
    private static List<Item> directDisplayItems = Arrays.asList((Item)ItemInit.GUIDE_BOOK.get(), (Item)ItemInit.SPELL_BOOK.get(), (Item)ItemInit.GRIMOIRE.get(), (Item)ItemInit.GRIMOIRE_COUNCIL.get(), (Item)ItemInit.GRIMOIRE_DEMON.get(), (Item)ItemInit.GRIMOIRE_UNDEAD.get(), (Item)ItemInit.GRIMOIRE_FEY.get(), (Item)ItemInit.BOOK_MARKS.get(), (Item)ItemInit.FLAT_LANDS_BOOK.get(), (Item)ItemInit.MODIFIER_BOOK.get(), (Item)ItemInit.ROTE_BOOK.get(), (Item)ItemInit.RECIPE_COPY_BOOK.get());
    private ItemStack[] displayItems = new ItemStack[15];

    public BookshelfTile(BlockPos pos, BlockState state) {
        super((BlockEntityType)TileEntityInit.BOOKSHELF.get(), pos, state, 15);
        for (int i = 0; i < 15; ++i) {
            this.displayItems[i] = ItemStack.EMPTY;
        }
    }

    private void refreshDisplayItems(ArrayList<ResourceLocation> items, ArrayList<Integer> colors) {
        for (int i = 0; i < 15; ++i) {
            Item item = (Item)BuiltInRegistries.ITEM.get(items.get(i));
            int color = colors.get(i);
            if (item == null || item == Items.AIR) {
                this.displayItems[i] = ItemStack.EMPTY;
                continue;
            }
            if (directDisplayItems.contains(item)) {
                this.displayItems[i] = new ItemStack((ItemLike)item);
                if (color <= -1 || !(item instanceof MNADyeableItem)) continue;
                MNADyeableItem.setColor(this.displayItems[i], color);
                continue;
            }
            this.displayItems[i] = new ItemStack((ItemLike)ItemInit.SPELL_BOOK.get());
            MNADyeableItem.setColor(this.displayItems[i], color > -1 ? color : this.resourceLocationToColor(items.get(i)));
        }
    }

    private int resourceLocationToColor(ResourceLocation rLoc) {
        return rLoc.toString().hashCode();
    }

    public ItemStack getDisplayItem(int index) {
        return this.displayItems[index % this.displayItems.length];
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        for (int i = 0; i < 15; ++i) {
            CompoundTag item = new CompoundTag();
            int color = -1;
            if (this.getItem(i).getItem() instanceof MNADyeableItem) {
                color = MNADyeableItem.getColor(this.getItem(i));
            }
            item.putString("rLoc", BuiltInRegistries.ITEM.getKey(this.getItem(i).getItem()).toString());
            if (color > -1) {
                item.putInt("color", color);
            }
            items.add(item);
        }
        tag.put("displayItems", (Tag)items);
        return tag;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        this.parseItemsList(tag);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag compoundtag = pkt.getTag();
        if (compoundtag != null) {
            this.parseItemsList(compoundtag);
        }
    }

    private void parseItemsList(CompoundTag tag) {
        if (tag.contains("displayItems")) {
            ListTag items = tag.getList("displayItems", 10);
            ArrayList<ResourceLocation> rLocs = new ArrayList<ResourceLocation>();
            ArrayList<Integer> colors = new ArrayList<Integer>();
            items.forEach(i -> {
                CompoundTag item = (CompoundTag)i;
                rLocs.add(ResourceLocation.parse(item.getString("rLoc")));
                if (item.contains("color")) {
                    colors.add(item.getInt("color"));
                } else {
                    colors.add(-1);
                }
            });
            this.refreshDisplayItems(rLocs, colors);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] items = new int[this.getContainerSize()];
        for (int i = 0; i < this.getContainerSize(); ++i) {
            items[i] = i;
        }
        return items;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).expandTowards(0.0, 1.0, 0.0);
    }
}


