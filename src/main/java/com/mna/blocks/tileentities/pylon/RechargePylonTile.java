/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  top.theillusivec4.curios.api.CuriosApi
 */
package com.mna.blocks.tileentities.pylon;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.pylon.PylonTileBase;
import com.mna.api.items.ChargeableItem;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.init.TileEntityInit;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import top.theillusivec4.curios.api.CuriosApi;

public class RechargePylonTile
extends PylonTileBase {
    private static final ResourceLocation icon = RLoc.create("textures/gui/pylon/recharge.png");

    public RechargePylonTile(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public RechargePylonTile(BlockPos pPos, BlockState pBlockState) {
        super((BlockEntityType)TileEntityInit.RECHARGE_PYLON.get(), pPos, pBlockState);
    }

    @Override
    protected HashMap<Affinity, Float> getPowerConsumption() {
        return this.getBlock().getPowerConsumption();
    }

    @Override
    public ResourceLocation getIcon() {
        return icon;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, RechargePylonTile tile) {
        if (tile.tick()) {
            int n = tile.powerUpTicks;
            Objects.requireNonNull(tile);
            if (n == 60 && level.getGameTime() % 20L == 0L) {
                tile.forAllPlayers(false, p -> {
                    p.getInventory().armor.forEach(RechargePylonTile::rechargeItem);
                    p.getInventory().items.forEach(RechargePylonTile::rechargeItem);
                    p.getInventory().offhand.forEach(RechargePylonTile::rechargeItem);
                    CuriosApi.getCuriosInventory((LivingEntity)p).ifPresent(handler -> handler.findCurios(is -> is.getItem() instanceof ChargeableItem).forEach(slotResult -> RechargePylonTile.rechargeItem(slotResult.stack())));
                });
            }
        }
    }

    public static void rechargeItem(ItemStack item) {
        RechargePylonTile.rechargeItem(item, 50);
    }

    public static void rechargeItem(ItemStack item, int amount) {
        Item item2 = item.getItem();
        if (item2 instanceof ChargeableItem) {
            ChargeableItem chargeable = (ChargeableItem)item2;
            chargeable.refundMana(item, amount, null);
        }
    }
}

