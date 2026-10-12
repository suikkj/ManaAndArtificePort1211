 
package com.mna.entities.renderers.faction;

import com.mna.capabilities.entity.MAPFX;
import com.mna.capabilities.entity.MAPFXProvider;
import com.mna.entities.boss.WitherLich;
import com.mna.entities.models.boss.LichModel;
import com.mna.entities.renderers.MAGeckoRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import com.mna.api.tools.MNACap;

public class LichRenderer
extends MAGeckoRenderer<WitherLich> {
    public LichRenderer(EntityRendererProvider.Context context) {
        super(context, new LichModel());
    }

    public boolean shouldRender(WitherLich pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        java.util.Optional<MAPFXProvider> pfxCap = MNACap.get(pLivingEntity, MAPFXProvider.MAPFX);
        if (pfxCap.isPresent() && ((MAPFX)pfxCap.get()).getFlag((LivingEntity)pLivingEntity, MAPFX.Flag.MIST_FORM)) {
            return false;
        }
        return super.shouldRender((Entity)pLivingEntity, pCamera, pCamX, pCamY, pCamZ);
    }
}


