/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.event.config.ModConfigEvent
 *  org.apache.commons.lang3.tuple.Pair
 */
package com.mna.config;

import com.mna.api.config.ClientConfigValues;
import com.mna.config.ClientConfigProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    public static final ClientConfigProvider CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;
    public static HashSet<String> DidYouKnowTipsShown;

    @SubscribeEvent
    public static void onModConfigEvent(ModConfigEvent configEvent) {
        // [PORT 1.21.1] NeoForge dispara ModConfigEvent.Unloading no stop do servidor (handleServerStopped);
        // ler valores de config nesse evento lanca "Cannot get config value before config is loaded"
        // (a spec ja foi descarregada). No Forge 1.20.1 esse evento nao existia — ignorar no port.
        if (configEvent instanceof ModConfigEvent.Unloading) {
            return;
        }
        if (configEvent.getConfig().getSpec() == CLIENT_SPEC) {
            ClientConfig.bakeConfig();
        }
    }

    public static void bakeConfig() {
        ClientConfigValues.FancyMagelights = (Boolean)ClientConfigProvider.FANCY_MAGELIGHTS.get();
        ClientConfigValues.ParticleBlur = (Boolean)ClientConfigProvider.PARTICLE_BLUR.get();
        ClientConfigValues.HudAffinity = (Boolean)ClientConfigProvider.HUD_AFFINITY.get();
        ClientConfigValues.ShowRunescribingRecipeMutex = (Boolean)ClientConfigProvider.SHOW_RUNESCRIBING_MUTEX.get();
        DidYouKnowTipsShown = new HashSet((Collection)ClientConfigProvider.DID_YOU_KNOW_TIPS.get());
        ClientConfigValues.ShowHudMode = ClientConfigValues.HudMode.values()[Mth.clamp((int)((Integer)ClientConfigProvider.SHOW_HUD_MODE.get()), (int)0, (int)(ClientConfigValues.HudMode.values().length - 1))];
        ClientConfigValues.CodexBackMode = ClientConfigValues.CodexMode.values()[Mth.clamp((int)((Integer)ClientConfigProvider.CODEX_BACK_STYLE.get()), (int)0, (int)(ClientConfigValues.CodexMode.values().length - 1))];
        ClientConfigValues.HudPosition = ClientConfigValues.HudPos.values()[Mth.clamp((int)((Integer)ClientConfigProvider.HUD_POSITION.get()), (int)0, (int)(ClientConfigValues.HudPos.values().length - 1))];
        ClientConfigValues.PinnedRecipeScale = ClientConfigValues.PinnedRecipeSize.values()[Mth.clamp((int)((Integer)ClientConfigProvider.PINNED_RECIPE_SCALE.get()), (int)0, (int)(ClientConfigValues.PinnedRecipeSize.values().length - 1))];
    }

    public static void SetDidYouKnowTipShown(String id) {
        DidYouKnowTipsShown.add(id);
        ClientConfigProvider.DID_YOU_KNOW_TIPS.set(Arrays.asList(DidYouKnowTipsShown.toArray(new String[0])));
    }

    public static void setCodexBackStyle(int style) {
        ClientConfigProvider.CODEX_BACK_STYLE.set(style);
    }

    public static void setHudMode(int mode) {
        ClientConfigProvider.SHOW_HUD_MODE.set(mode);
    }

    public static void setHudPosition(int position) {
        ClientConfigProvider.HUD_POSITION.set(position);
    }

    public static void setPinnedRecipeScale(int style) {
        ClientConfigProvider.PINNED_RECIPE_SCALE.set(style);
    }

    static {
        Pair specPair = new ModConfigSpec.Builder().configure(ClientConfigProvider::new);
        CLIENT = (ClientConfigProvider)specPair.getLeft();
        CLIENT_SPEC = (ModConfigSpec)specPair.getRight();
    }
}


