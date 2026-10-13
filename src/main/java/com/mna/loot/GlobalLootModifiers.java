/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.storage.loot.Serializer
 *  net.minecraft.world.level.storage.loot.functions.LootItemFunctionType
 *  net.neoforged.neoforge.common.loot.IGlobalLootModifier
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries$Keys
 *  net.neoforged.neoforge.registries.RegistryObject
 */
package com.mna.loot;

import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.loot.RandomArtifactSpell;
import com.mna.loot.RandomConstructPart;
import com.mna.loot.RandomHat;
import com.mna.loot.RandomJokeSpell;
import com.mna.loot.RandomJournalPage;
import com.mna.loot.RandomManaweaveRecipe;
import com.mna.loot.RandomRunescribeRecipe;
import com.mna.loot.RandomSilverSpell;
import com.mna.loot.RandomSpellStaff;
import com.mna.loot.RandomThaumaturgicLink;
import com.mna.loot.modifiers.AddChimeriteModifier;
import com.mna.loot.modifiers.BeheadingModifier;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class GlobalLootModifiers {
    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create((ResourceKey)NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, (String)"mna");
    public static final DeferredHolder<com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>, ? extends com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>> DROPS_CHIMERITE = LOOT_MODIFIERS.register("drops_chimerite", AddChimeriteModifier.CODEC);
    public static final DeferredHolder<com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>, ? extends com.mojang.serialization.MapCodec<? extends IGlobalLootModifier>> BEHEADING = LOOT_MODIFIERS.register("beheading", BeheadingModifier.CODEC);

    @SubscribeEvent
    public static final void registerLootFunctions(net.neoforged.neoforge.registries.RegisterEvent event) {
        // [PORT 1.21.1] Forge 1.20.1 permitia registrar em vanilla registries no common setup;
        // o NeoForge congela os registries antes disso -> registrar via RegisterEvent.
        event.register(BuiltInRegistries.LOOT_FUNCTION_TYPE.key(), helper -> {
            helper.register(RLoc.create("random_spell_staff"), RandomSpellStaff.TYPE);
            helper.register(RLoc.create("random_journal_page"), RandomJournalPage.TYPE);
            helper.register(RLoc.create("random_manaweave_recipe"), RandomManaweaveRecipe.TYPE);
            helper.register(RLoc.create("random_runescribe_recipe"), RandomRunescribeRecipe.TYPE);
            helper.register(RLoc.create("random_construct_part"), RandomConstructPart.TYPE);
            helper.register(RLoc.create("random_thaumaturgic_link"), RandomThaumaturgicLink.TYPE);
            helper.register(RLoc.create("random_joke_spell"), RandomJokeSpell.TYPE);
            helper.register(RLoc.create("random_artifact_spell"), RandomArtifactSpell.TYPE);
            helper.register(RLoc.create("random_silver_spell"), RandomSilverSpell.TYPE);
            helper.register(RLoc.create("random_hat"), RandomHat.TYPE);
        });
        ManaAndArtifice.LOGGER.info("Mana And Artifice >> Registered loot functions");
    }
}


