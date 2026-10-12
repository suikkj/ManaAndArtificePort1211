/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.neoforge.common.ModConfigSpec$Builder
 *  net.neoforged.neoforge.common.ModConfigSpec$ConfigValue
 *  net.neoforged.neoforge.common.ModConfigSpec$DoubleValue
 */
package com.mna.config;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.config.ISpellConfigHelper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SpellConfigProvider {
    private static HashSet<ResourceLocation> _initializedParts = new HashSet();
    private static HashMap<CompoundConfigKey, ModConfigSpec.DoubleValue> _configs = new HashMap();
    public static HashMap<ResourceLocation, ModConfigSpec.ConfigValue<List<? extends String>>> _dimensionBlacklists = new HashMap();
    public static HashMap<ResourceLocation, ModConfigSpec.ConfigValue<List<? extends String>>> _biomeBlacklists = new HashMap();
    public static ModConfigSpec.ConfigValue<List<? extends String>> SPELL_DIMENSION_BLACKLIST;
    public static ModConfigSpec.ConfigValue<List<? extends String>> SPELL_BIOME_BLACKLIST;

    public static void initGeneralSpellConfigs(ModConfigSpec.Builder serverBuilder) {
        SPELL_DIMENSION_BLACKLIST = serverBuilder.comment("Comma separated list of dimension ids that spells can't be cast in.").defineListAllowEmpty("spellDimensionBlacklist", Arrays.asList(new String[0]), e -> true);
        SPELL_BIOME_BLACKLIST = serverBuilder.comment("Comma separated list of biome ids that spells can't be cast in.").defineListAllowEmpty("spellBiomeBlacklist", Arrays.asList(new String[0]), e -> true);
    }

    public static void initForPart(ModConfigSpec.Builder serverBuilder, ISpellComponent shape, AttributeValuePair ... pairs) {
        String key = shape.getRegistryName().toString();
        serverBuilder.push(key);
        _biomeBlacklists.put(shape.getRegistryName(), (ModConfigSpec.ConfigValue<List<? extends String>>)serverBuilder.comment("Biome Blacklists - Biomes that this spell part cannot be cast in.").defineListAllowEmpty("biomeBlacklists", Arrays.asList(new String[0]), e -> true));
        _dimensionBlacklists.put(shape.getRegistryName(), (ModConfigSpec.ConfigValue<List<? extends String>>)serverBuilder.comment("Dimension Blacklists - Dimensions that this spell part cannot be cast in.").defineListAllowEmpty("dimensionBlacklists", Arrays.asList(new String[0]), e -> true));
        for (AttributeValuePair pair : pairs) {
            serverBuilder.push(pair.getAttribute().name());
            _configs.put(new CompoundConfigKey(shape, pair.getAttribute(), ISpellConfigHelper.Value.MINIMUM), serverBuilder.comment("Minimum Value").defineInRange("minimum", (double)pair.getMinimum(), 0.0, 9999.0));
            _configs.put(new CompoundConfigKey(shape, pair.getAttribute(), ISpellConfigHelper.Value.MAXIMUM), serverBuilder.comment("Maximum Value").defineInRange("maximum", (double)pair.getMaximum(), 0.0, 9999.0));
            _configs.put(new CompoundConfigKey(shape, pair.getAttribute(), ISpellConfigHelper.Value.DEFAULT), serverBuilder.comment("Default Value").defineInRange("default", (double)pair.getDefaultValue(), 0.0, 9999.0));
            _configs.put(new CompoundConfigKey(shape, pair.getAttribute(), ISpellConfigHelper.Value.STEP), serverBuilder.comment("Step Value (how much does one click in the inscription table change the value)").defineInRange("step", (double)pair.getStep(), 0.0, 9999.0));
            _configs.put(new CompoundConfigKey(shape, pair.getAttribute(), ISpellConfigHelper.Value.COMPLEXITY), serverBuilder.comment("Complexity Value (how much does changing by one tick change the complexity)").defineInRange("step_complexity", (double)pair.getStepComplexity(), 0.0, 9999.0));
            serverBuilder.pop();
        }
        serverBuilder.pop();
        _initializedParts.add(shape.getRegistryName());
    }

    public static double GetForKey(CompoundConfigKey key, float defaultValue) {
        Double d;
        double configured = defaultValue;
        if (_configs.containsKey(key) && (d = (Double)_configs.get(key).get()) != null) {
            configured = d;
        }
        return configured;
    }

    public static boolean IsDimensionBlacklisted(ResourceLocation dimensionID, ResourceLocation spellPartID) {
        if (_dimensionBlacklists.containsKey(dimensionID)) {
            return ((List)_dimensionBlacklists.get(dimensionID).get()).contains(spellPartID.toString());
        }
        return false;
    }

    public static boolean IsPartInitialized(ISpellComponent part) {
        return _initializedParts.contains(part.getRegistryName());
    }

    public static class CompoundConfigKey {
        private ISpellComponent object;
        private Attribute attr;
        private ISpellConfigHelper.Value value;

        public CompoundConfigKey(ISpellComponent object, Attribute attr, ISpellConfigHelper.Value value) {
            this.object = object;
            this.attr = attr;
            this.value = value;
        }

        public boolean equals(Object obj) {
            if (obj instanceof CompoundConfigKey) {
                CompoundConfigKey other = (CompoundConfigKey)obj;
                return this.attr == other.attr && this.value == other.value && this.object.equals(other.object);
            }
            return false;
        }

        public int hashCode() {
            return this.object.hashCode() + this.attr.hashCode() + this.value.hashCode();
        }
    }
}


