 
package com.mna.factions;

import com.mna.Registries;
import com.mna.api.faction.BaseFaction;
import com.mna.api.faction.FactionIDs;
import com.mna.api.faction.IFaction;
import com.mna.api.faction.IFactionHelper;
import com.mna.factions.Council;
import com.mna.factions.Demons;
import com.mna.factions.FeyCourt;
import com.mna.factions.Undead;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class Factions
implements IFactionHelper {
    public static final Factions INSTANCE = new Factions();
    public static final BaseFaction COUNCIL = new Council();
    public static final BaseFaction DEMONS = new Demons();
    public static final BaseFaction FEY = new FeyCourt();
    public static final BaseFaction UNDEAD = new Undead();

    @SubscribeEvent
    public static void registerFactions(RegisterEvent event) {
        event.register(Registries.Factions.getRegistryKey(), helper -> {
            helper.register(FactionIDs.COUNCIL, COUNCIL);
            helper.register(FactionIDs.DEMONS, DEMONS);
            helper.register(FactionIDs.FEY, FEY);
            helper.register(FactionIDs.UNDEAD, UNDEAD);
        });
    }

    @Override
    public List<IFaction> getAllFactions() {
        return Registries.Factions.getValues().stream().toList();
    }

    @Override
    public List<IFaction> getFactionsExcept(IFaction ... exclusions) {
        List<IFaction> excludeIDs = Arrays.asList(exclusions);
        return Registries.Factions.getValues().stream().filter(f -> !excludeIDs.contains(f)).toList();
    }

    @Override
    public IFaction getFaction(ResourceLocation id) {
        return (IFaction)Registries.Factions.getValue(id);
    }
}


