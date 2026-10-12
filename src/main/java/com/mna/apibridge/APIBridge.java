/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.DistExecutor
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent
 */
package com.mna.apibridge;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.ManaAndArtificeMod;
import com.mna.apibridge.AdvancementHelper;
import com.mna.apibridge.ConfigHelper;
import com.mna.apibridge.ContainerHelper;
import com.mna.apibridge.EntityHelper;
import com.mna.apibridge.GuiRenderHelper;
import com.mna.apibridge.InventoryHelper;
import com.mna.apibridge.ItemHelper;
import com.mna.apibridge.ParticleSerializerHelper;
import com.mna.apibridge.TileHelper;
import com.mna.cantrips.CantripRegistry;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceGuiRegistry;
import com.mna.capabilities.playerdata.magic.resources.CastingResourceRegistry;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.config.SpellConfig;
import com.mna.config.SpellConfigProvider;
import com.mna.factions.Factions;
import com.mna.spells.SpellCaster;
import com.mna.tools.PortalHelper;
import com.mna.tools.SummonUtils;
import com.mna.tools.render.WorldRenderUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class APIBridge {
    public static void earlyInit(net.neoforged.fml.ModContainer container) {
        ManaAndArtificeMod.setAPIHelper(new ConfigHelper());
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        ManaAndArtificeMod.setAPIHelper(new PortalHelper());
        ManaAndArtificeMod.setAPIHelper(new SpellCaster());
        ManaAndArtificeMod.setAPIHelper(new EntityHelper());
        ManaAndArtificeMod.setMagicCapability(PlayerMagicProvider.MAGIC);
        ManaAndArtificeMod.setProgressionCapability(PlayerProgressionProvider.PROGRESSION);
        ManaAndArtificeMod.setWorldMagicCapability(WorldMagicProvider.MAGIC);
        ManaAndArtificeMod.setChunkMagicCapability(ChunkMagicProvider.MAGIC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ManaAndArtificeMod.setAPIHelper(WorldRenderUtils.INSTANCE);
            ManaAndArtificeMod.setAPIHelper(new GuiRenderHelper());
            ManaAndArtificeMod.setAPIHelper(CastingResourceGuiRegistry.Instance);
        }
        ManaAndArtificeMod.setAPIHelper(new SummonUtils());
        ManaAndArtificeMod.setAPIHelper(Factions.INSTANCE);
        ManaAndArtificeMod.setAPIHelper(new InventoryHelper());
        ManaAndArtificeMod.setAPIHelper(new ParticleSerializerHelper());
        ManaAndArtificeMod.setAPIHelper(new AdvancementHelper());
        ManaAndArtificeMod.setAPIHelper(new ItemHelper());
        ManaAndArtificeMod.setAPIHelper(new ContainerHelper());
        ManaAndArtificeMod.setAPIHelper(new TileHelper());
        ManaAndArtificeMod.setAPIHelper(CastingResourceRegistry.Instance);
        ManaAndArtificeMod.setCantripRegistry(CantripRegistry.INSTANCE);
        SpellConfigProvider.initGeneralSpellConfigs(SpellConfig.SPELLS_CONFIG_BUILDER);
        Registries.Shape.forEach(s -> s.onRegistered());
        Registries.SpellEffect.forEach(s -> s.onRegistered());
        Registries.Modifier.forEach(s -> s.onRegistered());
        SpellConfig.finalizeServerConfig(container);
        Registries.ConstructTasks.forEach(t -> t.instantiateTask(null));
        ManaAndArtifice.LOGGER.info("M&A API initialized, it is now useable");
    }
}


