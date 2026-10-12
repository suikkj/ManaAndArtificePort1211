/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.bus.api.Event
 */
package com.mna.items.artifice.charms;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.events.EnderfeatherCharmUsedEvent;
import com.mna.items.ItemInit;
import com.mna.items.artifice.charms.CharmBaseItem;
import com.mna.tools.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

public class EnderfeatherCharm
extends CharmBaseItem {
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        ServerPlayer spe = (ServerPlayer)player;
        ItemStack stack = player.getItemInHand(hand);
        if (!world.dimension().equals(spe.getRespawnDimension())) {
            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.bed_charm.dimension-wrong"));
            return InteractionResultHolder.fail(stack);
        }
        BlockPos bedPos = spe.getRespawnPosition();
        if (bedPos == null) {
            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.bed_charm.no-bed"));
            return InteractionResultHolder.fail(stack);
        }
        if (this.consume(spe, hand)) {
            EnderfeatherCharmUsedEvent event = new EnderfeatherCharmUsedEvent((Player)spe);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                spe.addItem(new ItemStack((ItemLike)this));
                return InteractionResultHolder.fail(stack);
            }
            double dist = spe.position().distanceTo(new Vec3((double)bedPos.getX(), (double)bedPos.getY(), (double)bedPos.getZ()));
            if (dist >= 9999.0) {
                CustomAdvancementTriggers.USE_CHARM.trigger((ServerPlayer)player, new ItemStack((ItemLike)this));
            }
            player.awardStat(Stats.ITEM_USED.get(((Item)ItemInit.BED_CHARM.get())));
            CustomAdvancementTriggers.SHAMAN.trigger((ServerPlayer)player);
            world.broadcastEntityEvent((Entity)spe, (byte)46);
            world.playSound(null, spe.getX(), spe.getY(), spe.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.9f + (float)Math.random() * 0.2f);
            TeleportHelper.teleportEntity((Entity)spe, (ResourceKey<Level>)world.dimension(), new Vec3((double)bedPos.getX() + 0.5, (double)bedPos.getY(), (double)bedPos.getZ() + 0.5));
            world.playSound(null, (double)bedPos.getX(), (double)bedPos.getY(), (double)bedPos.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.9f + (float)Math.random() * 0.2f);
            world.broadcastEntityEvent((Entity)spe, (byte)46);
            return InteractionResultHolder.success(stack);
        }
        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.bed_charm.consume-failed"));
        return InteractionResultHolder.fail(stack);
    }
}


