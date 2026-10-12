 
package com.mna.entities.renderers.player;

import com.mna.items.ItemInit;
import com.mna.tools.EntityUtil;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.math.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import top.theillusivec4.curios.api.CuriosApi;
import com.mna.api.events.RunicAnvilShouldActivateEvent.Result;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class PlayerRenderEvents {
    @OnlyIn(value=Dist.CLIENT)
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getPersistentData().getBoolean("eldrin_flight") || EntityUtil.shouldBeInvisible((LivingEntity)event.getEntity())) {
            event.setResult(Result.DENY);
            event.setCanceled(true);
            return;
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        if (event.getEntity().getPersistentData().getBoolean("eldrin_flight") || EntityUtil.shouldBeInvisible((LivingEntity)event.getEntity())) {
            return;
        }
        CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)event.getEntity(), (Item)ItemInit.ELDRITCH_ORB.get()).ifPresent(t -> {
            Vector3f translation = new Vector3f(1.0f, 2.0f, 0.0f);
            Quaternionf rotation = Axis.YN.rotation((float)((double)(MathUtils.lerpf(event.getEntity().yHeadRotO, event.getEntity().yHeadRot, event.getPartialTick()) / 180.0f) * Math.PI));
            translation.rotate((Quaternionfc)rotation);
            event.getPoseStack().pushPose();
            event.getPoseStack().translate(translation.x(), translation.y(), translation.z());
            WorldRenderUtils.renderRadiant((Entity)event.getEntity(), event.getPoseStack(), event.getMultiBufferSource(), new int[]{0, 51, 102}, new int[]{179, 217, 255}, 255, 0.01f);
            event.getPoseStack().popPose();
        });
    }
}


