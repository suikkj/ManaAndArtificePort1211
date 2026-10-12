/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.Mod$EventBusSubscriber
 *  net.neoforged.fml.common.Mod$EventBusSubscriber$Bus
 *  net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
 */
package com.mna.capabilities.playerdata.magic.resources;

import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.capabilities.resource.ICastingResource;
import com.mna.api.capabilities.resource.ICastingResourceRegistry;
import com.mna.capabilities.playerdata.magic.resources.Brimstone;
import com.mna.capabilities.playerdata.magic.resources.CouncilMana;
import com.mna.capabilities.playerdata.magic.resources.Mana;
import com.mna.capabilities.playerdata.magic.resources.Souls;
import com.mna.capabilities.playerdata.magic.resources.SummerFire;
import com.mna.capabilities.playerdata.magic.resources.WinterIce;
import java.security.InvalidParameterException;
import java.util.HashMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class CastingResourceRegistry
implements ICastingResourceRegistry {
    private HashMap<ResourceLocation, Class<? extends ICastingResource>> _registry = new HashMap();
    public static final CastingResourceRegistry Instance = new CastingResourceRegistry();

    private void registerDefaults() {
        this.register(CastingResourceIDs.MANA, Mana.class);
        this.register(CastingResourceIDs.SOULS, Souls.class);
        this.register(CastingResourceIDs.COUNCIL_MANA, CouncilMana.class);
        this.register(CastingResourceIDs.BRIMSTONE, Brimstone.class);
        this.register(CastingResourceIDs.SUMMER_FIRE, SummerFire.class);
        this.register(CastingResourceIDs.WINTER_ICE, WinterIce.class);
    }

    @SubscribeEvent
    public static void RegisterCastingResources(FMLCommonSetupEvent event) {
        Instance.registerDefaults();
    }

    @Override
    public void register(ResourceLocation identifier, Class<? extends ICastingResource> clazz) {
        if (identifier == null) {
            return;
        }
        if (this._registry.containsKey(identifier)) {
            throw new InvalidParameterException("The Casting Resource Identifier " + identifier.toString() + "is already in use.");
        }
        this._registry.put(identifier, clazz);
    }

    public Class<? extends ICastingResource> getRegisteredClass(ResourceLocation rLoc) {
        return this._registry.getOrDefault(rLoc, this._registry.get(CastingResourceIDs.MANA));
    }
}


