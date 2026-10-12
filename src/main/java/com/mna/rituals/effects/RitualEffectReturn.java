 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IRitualTeleportLocation;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.Portal;
import com.mna.items.ritual.WorldCharm;
import com.mna.tools.TeleportHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;

public class RitualEffectReturn
extends RitualEffect {
    public RitualEffectReturn(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        if (context.getCaster() == null) {
            return false;
        }
        List<ItemStack> reagents = context.getCollectedReagents();
        if (reagents.size() == 9 && !context.getLevel().isClientSide) {
            ServerLevel targetWorld;
            ResourceKey worldKey;
            ItemStack reagentEight = reagents.get(8);
            ResourceKey resourceKey = worldKey = !reagentEight.isEmpty() && reagentEight.getItem() instanceof WorldCharm ? ((WorldCharm)reagentEight.getItem()).GetWorldTarget(reagentEight) : context.getLevel().dimension();
            if (worldKey == null) {
                worldKey = context.getLevel().dimension();
            }
            if ((targetWorld = TeleportHelper.resolveRegistryKey((ServerLevel)context.getLevel(), worldKey.location())) == null) {
                context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/return.world_not_found"));
                return false;
            }
            java.util.Optional<WorldMagicProvider> worldMagicContainer = MNACap.get(targetWorld, WorldMagicProvider.MAGIC);
            if (worldMagicContainer.isPresent()) {
                IWorldMagic worldMagic = (IWorldMagic)worldMagicContainer.get();
                ArrayList<ResourceLocation> runes = new ArrayList<ResourceLocation>();
                for (int i = 0; i < 8; ++i) {
                    runes.add(BuiltInRegistries.ITEM.getKey(reagents.get(i).getItem()));
                }
                IRitualTeleportLocation teleportTarget = worldMagic.getRitualTeleportBlockLocation(runes, (ResourceKey<Level>)worldKey);
                if (teleportTarget == null) {
                    context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/return.not_found"));
                    return false;
                }
                Portal portal = new Portal((EntityType)EntityInit.PORTAL_ENTITY.get(), context.getLevel());
                portal.setPos(Vec3.atBottomCenterOf((Vec3i)context.getCenter()));
                portal.setTeleportBlockPos(teleportTarget.getPos().above(), teleportTarget.getWorldType());
                context.getLevel().addFreshEntity((Entity)portal);
                return true;
            }
        }
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 20;
    }
}


