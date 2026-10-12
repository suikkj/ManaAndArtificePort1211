/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.KeyMapping
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 *  net.neoforged.neoforge.common.util.Lazy
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.mna;

import com.mna.ManaAndArtifice;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class KeybindInit {
    public static final Lazy<KeyMapping> RadialMenuOpen = Lazy.of(() -> new KeyMapping("key.spellbookopen", InputConstants.Type.KEYSYM, 90, "key.categories.mna"));
    public static final Lazy<KeyMapping> SpellBookChord = Lazy.of(() -> new KeyMapping("key.spellbookchord", InputConstants.Type.KEYSYM, 86, "key.categories.mna"));
    public static final Lazy<KeyMapping> InventoryItemOpen = Lazy.of(() -> new KeyMapping("key.gui-key-modifier", InputConstants.Type.KEYSYM, 341, "key.categories.mna"));
    public static final Lazy<KeyMapping> OpenBeltPouch = Lazy.of(() -> new KeyMapping("key.open-belt-pouch", InputConstants.Type.KEYSYM, 66, "key.categories.mna"));
    public static final Lazy<KeyMapping> CastHandSlot = Lazy.of(() -> new KeyMapping("key.cast-hand-slot", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories.mna"));
    public static final Lazy<KeyMapping> CastBeltSlot = Lazy.of(() -> new KeyMapping("key.cast-belt-slot", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories.mna"));
    public static final Lazy<KeyMapping> CastBackSlot = Lazy.of(() -> new KeyMapping("key.cast-back-slot", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), "key.categories.mna"));
    public static final Lazy<KeyMapping> CastAnySlot = Lazy.of(() -> new KeyMapping("key.cast-any-slot", InputConstants.Type.KEYSYM, 67, "key.categories.mna"));

    @SubscribeEvent
    public static void initKeybinds(RegisterKeyMappingsEvent event) {
        event.register((KeyMapping)RadialMenuOpen.get());
        event.register((KeyMapping)InventoryItemOpen.get());
        event.register((KeyMapping)SpellBookChord.get());
        event.register((KeyMapping)OpenBeltPouch.get());
        event.register((KeyMapping)CastHandSlot.get());
        event.register((KeyMapping)CastBeltSlot.get());
        event.register((KeyMapping)CastBackSlot.get());
        event.register((KeyMapping)CastAnySlot.get());
        ManaAndArtifice.LOGGER.info("M&A -> Keybindings Registered");
    }
}


