/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.entities.EntityInit;
import com.mna.entities.rituals.Portal;
import com.mna.items.ItemInit;
import com.mna.items.ritual.PlayerCharm;
import com.mna.items.ritual.WorldCharm;
import com.mna.tools.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RitualEffectSummon
extends RitualEffect {
    public RitualEffectSummon(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        Player player;
        BlockPos teleportTarget = context.getCenter().above();
        Player portalTarget = null;
        ResourceKey<Level> worldKey = null;
        ItemStack playerCharm = ItemStack.EMPTY;
        for (ItemStack stack : context.getCollectedReagents()) {
            if (stack.getItem() == ItemInit.PLAYER_CHARM.get()) {
                playerCharm = stack;
            }
            if (stack.getItem() != ItemInit.WORLD_CHARM.get()) continue;
            worldKey = ((WorldCharm)ItemInit.WORLD_CHARM.get()).GetWorldTarget(stack);
        }
        if (worldKey == null) {
            worldKey = context.getLevel().dimension();
        }
        ServerLevel targetWorld = TeleportHelper.resolveRegistryKey((ServerLevel)context.getLevel(), worldKey.location());
        if (!playerCharm.isEmpty() && (player = ((PlayerCharm)ItemInit.PLAYER_CHARM.get()).GetPlayerTarget(playerCharm, (Level)targetWorld)) != null) {
            portalTarget = player;
        }
        if (portalTarget == null) {
            context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/visit.not_found"));
            return false;
        }
        Portal portal = new Portal((EntityType)EntityInit.PORTAL_ENTITY.get(), context.getLevel());
        portal.setPos(Vec3.atBottomCenterOf((Vec3i)portalTarget.blockPosition()));
        portal.setTeleportBlockPos(teleportTarget.above(), (ResourceKey<Level>)context.getLevel().dimension());
        context.getLevel().addFreshEntity((Entity)portal);
        return false;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 20;
    }
}

