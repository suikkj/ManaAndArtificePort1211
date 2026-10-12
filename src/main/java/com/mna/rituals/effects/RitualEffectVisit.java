 
package com.mna.rituals.effects;

import com.mna.api.items.IPositionalItem;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RitualEffectVisit
extends RitualEffect {
    public RitualEffectVisit(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        Player teleportTarget = null;
        int boneCount = 0;
        ResourceKey worldKey = null;
        ItemStack playerCharm = ItemStack.EMPTY;
        ItemStack positionCharm = ItemStack.EMPTY;
        for (ItemStack stack : context.getCollectedReagents()) {
            if (stack.getItem() == ItemInit.PLAYER_CHARM.get()) {
                playerCharm = stack;
            } else if (stack.getItem() instanceof IPositionalItem) {
                positionCharm = stack;
            }
            if (stack.getItem() == ItemInit.WORLD_CHARM.get()) {
                worldKey = ((WorldCharm)ItemInit.WORLD_CHARM.get()).GetWorldTarget(stack);
            }
            if (stack.getItem() != Items.BONE) continue;
            ++boneCount;
        }
        if (worldKey == null) {
            worldKey = context.getLevel().dimension();
        }
        BlockPos teleportTargetPos = null;
        ResourceKey teleportTargetDimension = worldKey;
        if (positionCharm.isEmpty()) {
            if (!playerCharm.isEmpty()) {
                ServerLevel targetWorld = TeleportHelper.resolveRegistryKey((ServerLevel)context.getLevel(), worldKey.location());
                Player player = ((PlayerCharm)ItemInit.PLAYER_CHARM.get()).GetPlayerTarget(playerCharm, (Level)targetWorld);
                if (player != null) {
                    teleportTarget = player;
                }
            } else {
                teleportTarget = context.getCaster();
            }
            if (teleportTarget == null) {
                if (context.getCaster() != null) {
                    context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/visit.not_found"));
                }
                return false;
            }
            teleportTargetPos = teleportTarget.blockPosition().above();
            teleportTargetDimension = teleportTarget.level().dimension();
        } else {
            teleportTargetPos = ((IPositionalItem)positionCharm.getItem()).getLocation(positionCharm);
            int count = 0;
            while (!context.getLevel().getBlockState(teleportTargetPos).isAir() && count++ < 10) {
                teleportTargetPos = teleportTargetPos.above();
            }
            if (count == 10) {
                teleportTargetPos = null;
            }
        }
        if (teleportTargetPos == null) {
            if (context.getCaster() != null) {
                context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/visit.teleport_impossible"));
            }
            return false;
        }
        if (boneCount >= 2 && positionCharm.isEmpty()) {
            CompoundTag casterPD = teleportTarget.getPersistentData();
            if (!casterPD.contains("mna_last_death_data")) {
                context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/visit.no_death_data"));
                return false;
            }
            CompoundTag deathData = casterPD.getCompound("mna_last_death_data");
            teleportTargetPos = BlockPos.of((long)deathData.getLong("position"));
            teleportTargetDimension = ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)ResourceLocation.parse(deathData.getString("dimension")));
        }
        Portal portal = new Portal((EntityType)EntityInit.PORTAL_ENTITY.get(), context.getLevel());
        portal.setPos(Vec3.atBottomCenterOf((Vec3i)context.getCenter()));
        portal.setTeleportBlockPos(teleportTargetPos, (ResourceKey<Level>)teleportTargetDimension);
        context.getLevel().addFreshEntity((Entity)portal);
        return false;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 20;
    }
}

