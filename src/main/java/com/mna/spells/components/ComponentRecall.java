/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.entity.EntityTeleportEvent
 *  net.neoforged.bus.api.Event
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IPositionalItem;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.tools.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.bus.api.Event;

public class ComponentRecall
extends SpellEffect {
    public ComponentRecall(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RANGE, 1.0f, 1.0f, 5.0f, 1.0f, 25.0f), new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 10.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    /*
     * Enabled aggressive block sorting
     */
    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        double maxDist;
        if (!source.hasCasterReference()) return ComponentApplicationResult.FAIL;
        if (!target.isEntity()) return ComponentApplicationResult.FAIL;
        if (!target.getEntity().isAlive()) return ComponentApplicationResult.FAIL;
        if (!com.mna.tools.MATools.canChangeDimensions(target.getEntity())) return ComponentApplicationResult.FAIL;
        ItemStack markingRune = source.getCaster().getMainHandItem().getItem() == ItemInit.RUNE_MARKING.get() || source.getCaster().getMainHandItem().getItem() == ItemInit.BOOK_MARKS.get() ? source.getCaster().getMainHandItem() : source.getCaster().getOffhandItem();
        Item item = markingRune.getItem();
        if (!(item instanceof IPositionalItem)) {
            if (!source.isPlayerCaster()) return ComponentApplicationResult.FAIL;
            source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:components/recall.no_marker"));
            return ComponentApplicationResult.FAIL;
        }
        IPositionalItem posItem = (IPositionalItem)item;
        BlockPos pos = posItem.getLocation(markingRune);
        Direction face = posItem.getFace(markingRune);
        if (pos == null) return ComponentApplicationResult.FAIL;
        double dist = target.getEntity().blockPosition().distSqr((Vec3i)pos);
        if (dist > (maxDist = (double)(modificationData.getValue(Attribute.RANGE) * 500.0f)) * maxDist) {
            if (!source.isPlayerCaster()) return ComponentApplicationResult.FAIL;
            source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:components/recall.too_far"));
            return ComponentApplicationResult.FAIL;
        }
        int magnitude = (int)modificationData.getValue(Attribute.MAGNITUDE);
        if (!this.casterTeamCheck(source, target) || !this.magnitudeHealthCheck(source, target, magnitude, 20)) {
            if (!source.isPlayerCaster()) return ComponentApplicationResult.FAIL;
            source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:generic.too_powerful"));
            return ComponentApplicationResult.FAIL;
        }
        if (face != null) {
            pos = pos.offset(face.getNormal());
        }
        Vec3 targetPos = Vec3.atCenterOf((Vec3i)pos);
        EntityTeleportEvent tpEvent = new EntityTeleportEvent(target.getEntity(), targetPos.x, targetPos.y, targetPos.z);
        if (!NeoForge.EVENT_BUS.post((Event)tpEvent)) {
            TeleportHelper.teleportEntity(target.getEntity(), (ResourceKey<Level>)context.getServerLevel().dimension(), targetPos);
            return ComponentApplicationResult.SUCCESS;
        }
        if (!source.isPlayerCaster()) return ComponentApplicationResult.FAIL;
        source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:components/recall.event_canceled"));
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ARCANE;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.COUNCIL;
    }

    @Override
    public float initialComplexity() {
        return 50.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}


