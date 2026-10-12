/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.Container
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.monster.piglin.PiglinAi
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.inventory.PlayerEnderChestContainer
 */
package com.mna.tools;

import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;

public class GuiTools {
    private static final Component CONTAINER_TITLE = Component.translatable((String)"container.enderchest");

    public static void openEnderChest(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        PlayerEnderChestContainer playerenderchestcontainer = player.getEnderChestInventory();
        player.openMenu((MenuProvider)new SimpleMenuProvider((p_53124_, p_53125_, p_53126_) -> ChestMenu.threeRows((int)p_53124_, (Inventory)p_53125_, (Container)playerenderchestcontainer), CONTAINER_TITLE));
        player.awardStat(Stats.OPEN_ENDERCHEST);
        PiglinAi.angerNearbyPiglins((Player)player, (boolean)true);
    }
}

