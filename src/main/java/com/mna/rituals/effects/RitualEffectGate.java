 
package com.mna.rituals.effects;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IRitualTeleportLocation;
import com.mna.api.capabilities.IWorldMagic;
import com.mna.api.faction.IFaction;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.Portal;
import com.mna.items.ItemInit;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;

public class RitualEffectGate
extends RitualEffect {
    public RitualEffectGate(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        List<ItemStack> reagents = context.getCollectedReagents();
        if (reagents.size() == 11 && !context.getLevel().isClientSide) {
            ServerLevel targetWorld;
            ResourceKey worldKey;
            ItemStack reagentNine = reagents.get(9);
            ResourceKey resourceKey = worldKey = !reagentNine.isEmpty() && reagentNine.getItem() instanceof WorldCharm ? ((WorldCharm)reagentNine.getItem()).GetWorldTarget(reagentNine) : context.getLevel().dimension();
            if (worldKey == null) {
                worldKey = context.getLevel().dimension();
            }
            if ((targetWorld = TeleportHelper.resolveRegistryKey((ServerLevel)context.getLevel(), worldKey.location())) == null) {
                context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/return.world_not_found"));
                return false;
            }
            java.util.Optional<com.mna.api.capabilities.IWorldMagic> worldMagicContainer = MNACap.get(targetWorld, WorldMagicProvider.MAGIC);
            if (worldMagicContainer.isPresent()) {
                IWorldMagic worldMagic = (IWorldMagic)worldMagicContainer.get();
                ItemStack reagentEight = reagents.get(8);
                DyeColor portalColor = null;
                boolean rtp = false;
                if (!reagentEight.isEmpty()) {
                    DyeColor dyeColor = portalColor = reagentEight.getItem() instanceof DyeItem ? ((DyeItem)reagents.get(8).getItem()).getDyeColor() : null;
                    if (reagentEight.getItem() == ItemInit.GREATER_MOTE_ENDER.get()) {
                        rtp = true;
                    }
                }
                ArrayList<ResourceLocation> runes = new ArrayList<ResourceLocation>();
                for (int i = 0; i < 8; ++i) {
                    runes.add(BuiltInRegistries.ITEM.getKey(reagents.get(i).getItem()));
                }
                IRitualTeleportLocation teleportTarget = worldMagic.getRitualTeleportBlockLocation(runes, (ResourceKey<Level>)worldKey);
                if (teleportTarget == null && !rtp) {
                    context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/return.not_found"));
                    return false;
                }
                Portal portal = new Portal((EntityType)EntityInit.PORTAL_ENTITY.get(), context.getLevel());
                portal.setPos(Vec3.atBottomCenterOf((Vec3i)context.getCenter()));
                if (!rtp) {
                    portal.setTeleportBlockPos(teleportTarget.getPos().above(), teleportTarget.getWorldType());
                }
                portal.setPermanent();
                if (portalColor != null) {
                    portal.setDyeColor(portalColor);
                }
                if (rtp) {
                    portal.setRTP();
                }
                if (context.getCaster() != null) {
                    IPlayerProgression p = MNACap.<IPlayerProgression>get(context.getCaster(), PlayerProgressionProvider.PROGRESSION).orElse(null);
                    IPlayerMagic m = MNACap.<IPlayerMagic>get(context.getCaster(), PlayerMagicProvider.MAGIC).orElse(null);
                    if (p != null && m != null && p.hasAlliedFaction()) {
                        IFaction faction = p.getAlliedFaction();
                        int idx = 0;
                        ResourceLocation castingResource = m.getCastingResource().getRegistryName();
                        for (int i = 0; i < p.getAlliedFaction().getCastingResources().length; i = (int)((byte)(i + 1))) {
                            if (!faction.getCastingResources()[i].equals(castingResource)) continue;
                            idx = i;
                            break;
                        }
                        portal.setRenderData(p.getAlliedFaction(), (byte)idx);
                    }
                }
                context.getLevel().addFreshEntity((Entity)portal);
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 20;
    }
}


