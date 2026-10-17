/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.neoforge.common.ModConfigSpec
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.ModLoadingContext
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.config.IConfigSpec
 *  net.neoforged.fml.config.ModConfig$Type
 *  net.neoforged.fml.event.config.ModConfigEvent
 */
package com.mna.config;

import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.config.ISpellConfigHelper;
import com.mna.config.SpellConfigProvider;
import java.util.HashMap;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class SpellConfig {
    public static final ModConfigSpec.Builder SPELLS_CONFIG_BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec SPELLS_CONFIG_SPEC;
    private static boolean finalized;
    private static HashMap<SpellConfigProvider.CompoundConfigKey, Double> _resolvedConfigs;
    private static HashMap<ResourceLocation, List<? extends String>> _resolvedDimensionBlacklists;
    private static HashMap<ResourceLocation, List<? extends String>> _resolvedBiomeBlacklists;
    private static List<? extends String> GlobalSpellDimensionBlacklist;
    private static List<? extends String> GlobalSpellBiomeBlacklist;

    public static void finalizeServerConfig(net.neoforged.fml.ModContainer container) {
        SPELLS_CONFIG_SPEC = SPELLS_CONFIG_BUILDER.build();
        container.registerConfig(ModConfig.Type.SERVER, (IConfigSpec)SPELLS_CONFIG_SPEC, "mna-spells.toml");
        finalized = true;
    }

    @SubscribeEvent
    public static void onModConfigEvent(ModConfigEvent configEvent) {
        // [PORT 1.21.1] NeoForge dispara ModConfigEvent.Unloading no stop do servidor (handleServerStopped);
        // ler valores de config nesse evento lanca "Cannot get config value before config is loaded"
        // (a spec ja foi descarregada). No Forge 1.20.1 esse evento nao existia — ignorar no port.
        if (configEvent instanceof ModConfigEvent.Unloading) {
            return;
        }
        if (configEvent.getConfig().getSpec() == SPELLS_CONFIG_SPEC) {
            SpellConfig.bakeConfig();
        }
    }

    private static void bakeConfig() {
        ManaAndArtifice.LOGGER.info("M&A >> Baking Spell Config");
        GlobalSpellDimensionBlacklist = (List)SpellConfigProvider.SPELL_DIMENSION_BLACKLIST.get();
        GlobalSpellBiomeBlacklist = (List)SpellConfigProvider.SPELL_BIOME_BLACKLIST.get();
        Registries.Shape.forEach(s -> s.getModifiableAttributes().forEach(attr -> {
            for (ISpellConfigHelper.Value value : ISpellConfigHelper.Value.values()) {
                SpellConfigProvider.CompoundConfigKey key = new SpellConfigProvider.CompoundConfigKey((ISpellComponent)s, attr.getAttribute(), value);
                _resolvedConfigs.put(key, SpellConfigProvider.GetForKey(key, attr.getDefaultValue()));
            }
        }));
        Registries.SpellEffect.forEach(s -> s.getModifiableAttributes().forEach(attr -> {
            for (ISpellConfigHelper.Value value : ISpellConfigHelper.Value.values()) {
                SpellConfigProvider.CompoundConfigKey key = new SpellConfigProvider.CompoundConfigKey((ISpellComponent)s, attr.getAttribute(), value);
                _resolvedConfigs.put(key, SpellConfigProvider.GetForKey(key, attr.getDefaultValue()));
            }
        }));
        SpellConfigProvider._dimensionBlacklists.forEach((dim, keys) -> _resolvedDimensionBlacklists.put((ResourceLocation)dim, (List)keys.get()));
        SpellConfigProvider._biomeBlacklists.forEach((dim, keys) -> _resolvedBiomeBlacklists.put((ResourceLocation)dim, (List)keys.get()));
    }

    public static boolean finalized() {
        return finalized;
    }

    public static float getConfiguredValue(ISpellComponent part, Attribute attribute, ISpellConfigHelper.Value value, float defaultValue) {
        if (!SpellConfig.finalized()) {
            return defaultValue;
        }
        SpellConfigProvider.CompoundConfigKey key = new SpellConfigProvider.CompoundConfigKey(part, attribute, value);
        _resolvedConfigs.computeIfAbsent(key, k -> SpellConfigProvider.GetForKey(key, defaultValue));
        return _resolvedConfigs.get(key).floatValue();
    }

    public static boolean isDimensionBlacklisted(ISpellComponent part, ResourceLocation dimensionID) {
        String flattenedDimensionID = dimensionID.toString();
        if (GlobalSpellDimensionBlacklist.contains(flattenedDimensionID) || GlobalSpellDimensionBlacklist.contains("*")) {
            return true;
        }
        if (_resolvedDimensionBlacklists.containsKey(part.getRegistryName())) {
            return _resolvedDimensionBlacklists.get(part.getRegistryName()).contains(dimensionID.toString()) || _resolvedDimensionBlacklists.get(part.getRegistryName()).contains("*");
        }
        return false;
    }

    public static boolean isBiomeBlacklisted(ISpellComponent part, ResourceLocation biomeID) {
        if (GlobalSpellBiomeBlacklist.contains(biomeID.toString()) || GlobalSpellBiomeBlacklist.contains("*")) {
            return false;
        }
        if (_resolvedBiomeBlacklists.containsKey(part.getRegistryName())) {
            return _resolvedBiomeBlacklists.get(part.getRegistryName()).contains(biomeID.toString()) || _resolvedBiomeBlacklists.get(part.getRegistryName()).contains("*");
        }
        return false;
    }

    public static boolean isPartInitialized(ISpellComponent part) {
        return SpellConfigProvider.IsPartInitialized(part);
    }

    static {
        finalized = false;
        _resolvedConfigs = new HashMap();
        _resolvedDimensionBlacklists = new HashMap();
        _resolvedBiomeBlacklists = new HashMap();
    }
}


