 
package com.mna.api.timing;

import com.mna.api.timing.IDelayedEvent;
import java.util.ArrayList;
import java.util.HashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class DelayedEventQueue {
    private static HashMap<ResourceKey<Level>, ArrayList<IDelayedEvent>> _events = new HashMap();

    @SubscribeEvent
    public static void worldTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!_events.containsKey(event.getLevel().dimension())) {
            return;
        }
        ArrayList<IDelayedEvent> worldDelayedEvents = _events.get(event.getLevel().dimension());
        for (int i = 0; i < worldDelayedEvents.size(); ++i) {
            if (!worldDelayedEvents.get(i).tick()) continue;
            worldDelayedEvents.remove(i);
            --i;
        }
    }

    public static void pushEvent(Level world, IDelayedEvent event) {
        if (event.getID() == null) {
            return;
        }
        if (!_events.containsKey(world.dimension())) {
            _events.put((ResourceKey<Level>)world.dimension(), new ArrayList());
        }
        _events.get(world.dimension()).add(event);
    }

    public static boolean hasEvent(Level world, String identifier) {
        if (!_events.containsKey(world.dimension())) {
            return false;
        }
        return _events.get(world.dimension()).stream().anyMatch(e -> e.getID() != null && e.getID().equals(identifier));
    }
}


