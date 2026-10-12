/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.faction.Broker;
import com.mna.entities.utility.WanderingWizard;
import com.mna.items.sorcery.EntityCrystal;
import com.mna.spells.SpellCaster;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.SummonUtils;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public class ComponentCrystallize
extends SpellEffect {
    public ComponentCrystallize(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 1.0f, 1.0f, 4.0f, 1.0f, 10.0f));
        this.addReagent(new ItemStack((ItemLike)Items.DIAMOND), false, true, false, new IFaction[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!source.isPlayerCaster()) {
            return ComponentApplicationResult.FAIL;
        }
        LivingEntity living = target.getLivingEntity();
        if (living instanceof WanderingWizard) {
            WanderingWizard ww = (WanderingWizard)living;
            ww.setTarget(source.getCaster());
            if (source.isPlayerCaster()) {
                source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:generic.too_powerful"));
            }
            return ComponentApplicationResult.FAIL;
        }
        if (living instanceof Broker) {
            Broker broker = (Broker)living;
            broker.hurt(broker.damageSources().magic(), 1.0f);
            if (source.isPlayerCaster()) {
                source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:generic.too_powerful"));
            }
            return ComponentApplicationResult.FAIL;
        }
        if (living != null && !(living instanceof Player) && living.canChangeDimensions() && living.isAlive()) {
            boolean isConstructAndIsMine = living instanceof Construct && ((Construct)living).getOwner() == source.getPlayer();
            int magnitude = (int)modificationData.getValue(Attribute.LESSER_MAGNITUDE);
            if (!(isConstructAndIsMine || this.magnitudeHealthCheck(source, target, magnitude, 20) && !SummonUtils.isSummon((Entity)living))) {
                return ComponentApplicationResult.FAIL;
            }
            if (!SpellCaster.consumeReagents(source.getPlayer(), source.getHand(), this.getRequiredReagents(source.getPlayer(), source.getHand()))) {
                return ComponentApplicationResult.FAIL;
            }
            if (!context.getLevel().isClientSide()) {
                Pair<Boolean, Boolean> captureRedirect = InventoryUtilities.getCaptureAndRedirect(source.getPlayer());
                ItemStack stack = EntityCrystal.storeEntity(target.getEntity());
                target.getEntity().remove(Entity.RemovalReason.DISCARDED);
                if (((Boolean)captureRedirect.getFirst()).booleanValue()) {
                    ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
                    drops.add(stack);
                    InventoryUtilities.redirectCaptureOrDrop(source.getPlayer(), (Level)context.getServerLevel(), drops, (Boolean)captureRedirect.getSecond());
                } else {
                    ItemEntity item = new ItemEntity(context.getLevel(), target.getPosition().x, target.getPosition().y, target.getPosition().z, stack);
                    context.getLevel().addFreshEntity((Entity)item);
                }
            }
            return ComponentApplicationResult.SUCCESS;
        }
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
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public boolean targetsEntities() {
        return true;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}

