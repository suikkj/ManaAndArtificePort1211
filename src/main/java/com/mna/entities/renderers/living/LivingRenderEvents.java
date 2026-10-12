 
package com.mna.entities.renderers.living;

import com.mna.capabilities.entity.MAPFX;
import com.mna.capabilities.entity.MAPFXProvider;
import com.mna.spells.components.ComponentTrueInvisibility;
import com.mna.tools.EntityUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import com.mna.api.tools.MNACap;
import com.mna.api.events.RunicAnvilShouldActivateEvent.Result;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class LivingRenderEvents {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    @OnlyIn(value=Dist.CLIENT)
    public static void onLivingRenderPre(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() == null) {
            return;
        }
        if (EntityUtil.shouldBeInvisible(event.getEntity())) {
            event.setResult(Result.DENY);
            event.setCanceled(true);
            return;
        }
        MNACap.get(event.getEntity(), MAPFXProvider.MAPFX).ifPresent(pfx -> {
            if (pfx.getFlag(event.getEntity(), MAPFX.Flag.TRUE_INVISIBILITY) && !ComponentTrueInvisibility.IsDisabledFor(event.getEntity())) {
                event.setResult(Result.DENY);
                event.setCanceled(true);
            } else if (pfx.getFlag(event.getEntity(), MAPFX.Flag.CANCEL_RENDER)) {
                event.setResult(Result.DENY);
                event.setCanceled(true);
            }
        });
        try {
            float curScale = event.getEntity().getPersistentData().getFloat("mna_entity_scale");
            float prevScale = event.getEntity().getPersistentData().getFloat("mna_entity_scale_prev");
            if (curScale == 0.0f || prevScale == 0.0f) {
                curScale = 1.0f;
                prevScale = 1.0f;
            }
            float partial = event.getPartialTick();
            float scale = curScale + (curScale - prevScale) * partial;
            event.getPoseStack().scale(scale, scale, scale);
            if (event.getEntity().isDiscrete()) {
                event.getPoseStack().translate(0.0, 0.125, 0.0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    @OnlyIn(value=Dist.CLIENT)
    public static void onLivingRenderPost(RenderLivingEvent.Post<?, ?> event) {
    }
}


