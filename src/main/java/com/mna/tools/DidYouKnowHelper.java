/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.api.distmarker.OnlyIn
 */
package com.mna.tools;

import com.mna.config.ClientConfig;
import com.mna.network.ServerMessageDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DidYouKnowHelper {
    public static void CheckAndShowDidYouKnow(Player to, String id) {
        if (to == null) {
            return;
        }
        if (to instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)to;
            ServerMessageDispatcher.sendDidYouKnow(serverPlayer, id);
        } else if (to.level().isClientSide() && !DidYouKnowHelper.HasTipBeenShown(id)) {
            to.sendSystemMessage((Component)Component.translatable((String)id).withStyle(ChatFormatting.GOLD));
            ClientConfig.SetDidYouKnowTipShown(id);
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    public static boolean HasTipBeenShown(String id) {
        if (ClientConfig.DidYouKnowTipsShown == null) {
            return true;
        }
        return ClientConfig.DidYouKnowTipsShown.contains(id);
    }
}

