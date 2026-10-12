/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package com.mna.rituals.effects;

import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.entities.utility.PresentItem;
import com.mna.items.ItemInit;
import com.mna.items.artifice.ThaumaturgicCompass;
import com.mna.items.ritual.ThaumaturgicLink;
import com.mna.rituals.effects.WorldUtils;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class RitualEffectLocate
extends RitualEffect {
    public RitualEffectLocate(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    protected boolean applyRitualEffect(IRitualContext context) {
        Optional thaumLinkOptional = context.getCollectedReagents(i -> i.getItem() == ItemInit.THAUMATURGIC_LINK.get()).stream().findFirst();
        if (!thaumLinkOptional.isPresent()) {
            return false;
        }
        ItemStack outputCompass = new ItemStack((ItemLike)ItemInit.THAUMATURGIC_COMPASS.get());
        ItemStack thaumLink = (ItemStack)thaumLinkOptional.get();
        boolean isBiome = false;
        BlockPos targetPos = WorldUtils.locateStructure((ServerLevel)context.getLevel(), context.getCenter(), thaumLink);
        if (targetPos == null) {
            isBiome = true;
            targetPos = WorldUtils.locateBiome((ServerLevel)context.getLevel(), context.getCenter(), thaumLink);
            if (targetPos == null) {
                context.getCollectedReagents().forEach(i -> {
                    ItemEntity item = new ItemEntity(context.getLevel(), (double)context.getCenter().getX(), (double)context.getCenter().above().getY(), (double)context.getCenter().getZ(), i);
                    context.getLevel().addFreshEntity((Entity)item);
                });
                if (context.getCaster() != null) {
                    context.getCaster().sendSystemMessage((Component)Component.translatable((String)"mna:rituals/locating.failed"));
                }
                return false;
            }
        }
        context.getLevel().playSound((Player)null, context.getCenter(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
        ThaumaturgicCompass.setTrackedPosition(outputCompass, (ResourceKey<Level>)context.getLevel().dimension(), targetPos, ((ThaumaturgicLink)(ItemInit.THAUMATURGIC_LINK.get())).getLocationKey(thaumLink), isBiome ? ThaumaturgicCompass.TrackType.Biome : ThaumaturgicCompass.TrackType.Structure);
        PresentItem epi = new PresentItem(context.getLevel(), context.getCenter().getX(), context.getCenter().above().getY(), context.getCenter().getZ(), outputCompass);
        context.getLevel().addFreshEntity((Entity)epi);
        return false;
    }

    @Override
    protected int getApplicationTicks(IRitualContext context) {
        return 0;
    }
}

