 
package com.mna.progression;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.recipes.progression.ProgressionCondition;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSleepInBedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import com.mna.api.tools.MNACap;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class ProgressionEventHandler {
    @SubscribeEvent
    public static void onPlayerSleep(PlayerSleepInBedEvent event) {
        IPlayerProgression progression = MNACap.get((IPlayerProgression)event.getEntity(), PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression != null && progression.getTier() == 1 && progression.getTierProgress(event.getEntity().level()) >= 1.0f) {
            progression.setTier(progression.getTier() + 1, event.getEntity());
            event.getEntity().sendSystemMessage((Component)Component.translatable((String)"mna:progresscondition.advanced", (Object[])new Object[]{progression.getTier()}));
        }
    }

    @SubscribeEvent
    public static void onPlayerAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }
        if (player.level().isClientSide()) {
            ProgressionEventHandler.confirmExistingAdvancements(event.getEntity());
        } else {
            IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (progression == null || progression.getTier() >= 5) {
                return;
            }
            List<ProgressionCondition> conditions = ProgressionCondition.get(player.level(), progression.getTier(), progression.getCompletedProgressionSteps());
            boolean advanced = false;
            for (ProgressionCondition condition : conditions) {
                if (!condition.getAdvancementID().equals(event.getAdvancement().id())) continue;
                advanced = true;
                progression.addTierProgressionComplete(condition.getId());
            }
            if (advanced && progression.getTierProgress(player.level()) >= 1.0f) {
                player.sendSystemMessage((Component)Component.translatable((String)"mna:progresscondition.ready"));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            ProgressionEventHandler.confirmExistingAdvancements(event.getEntity());
        }
    }

    public static void confirmExistingAdvancements(Player player) {
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression == null || progression.getTier() >= 5) {
            return;
        }
        List<ProgressionCondition> conditions = ProgressionCondition.get(player.level(), progression.getTier(), progression.getCompletedProgressionSteps());
        PlayerAdvancements advancements = ((ServerLevel)player.level()).getServer().getPlayerList().getPlayerAdvancements((ServerPlayer)player);
        ServerAdvancementManager advancementMgr = ((ServerLevel)player.level()).getServer().getAdvancements();
        for (ProgressionCondition condition : conditions) {
            for (Advancement adv : advancementMgr.getAllAdvancements()) {
                if (!advancements.getOrStartProgress(adv).isDone() || !condition.getAdvancementID().equals(adv.id())) continue;
                progression.addTierProgressionComplete(condition.getId());
            }
        }
    }
}


