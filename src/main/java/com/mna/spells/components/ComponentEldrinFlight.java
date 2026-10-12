 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.WellspringNode;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.utility.EldrinFlight;
import com.mna.factions.Factions;
import com.mna.network.ServerMessageDispatcher;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.bus.api.Event;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import com.mna.api.tools.MNACap;

public class ComponentEldrinFlight
extends SpellEffect {
    public ComponentEldrinFlight(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (source.isPlayerCaster() && target.isEntity() && target.getEntity() instanceof Player) {
            if (source.getPlayer() != target.getEntity() && !source.getPlayer().isAlliedTo(target.getEntity())) {
                return ComponentApplicationResult.FAIL;
            }
            int dist = GeneralConfigValues.WellspringDistance;
            if (!source.getPlayer().hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHOOSING_WELLSPRING.get()))) {
                source.getPlayer().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.CHOOSING_WELLSPRING.get()), 200));
                ServerMessageDispatcher.sendWellspringSyncMessage(context.getServerLevel(), (ServerPlayer)source.getPlayer(), (int)((double)dist * 1.5));
                return ComponentApplicationResult.FAIL;
            }
            source.getPlayer().removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHOOSING_WELLSPRING.get()));
            Vec3 start = new Vec3(source.getOrigin().x(), 0.0, source.getOrigin().z());
            MutableBoolean success = new MutableBoolean(false);
            MNACap.get(context.getServerLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> {
                int halfDist = dist / 2;
                HashMap<BlockPos, WellspringNode> nodes = m.getWellspringRegistry().getNearbyNodes(source.getPlayer().blockPosition(), 50, dist + halfDist);
                if (nodes.size() == 0) {
                    source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:components/eldrin_flight.none-nearby"));
                } else {
                    MutableObject selectedWellspring = new MutableObject();
                    MutableFloat closestAngle = new MutableFloat((Number)Math.PI);
                    Vec3 playerLook = Vec3.directionFromRotation((Vec2)new Vec2(0.0f, source.getPlayer().yHeadRot));
                    double startLength = playerLook.length();
                    nodes.forEach((pos, node) -> {
                        double aLenTimesBLen;
                        Vec3 nodePos = new Vec3((double)pos.getX() + 0.5, node.hasForcedYLevel() ? (double)(node.getYLevel() - 3) : -1.0, (double)pos.getZ() + 0.5);
                        Vec3 nodeDir = nodePos.subtract(start);
                        double dot = playerLook.dot(nodeDir);
                        double theta = Math.acos(dot / (aLenTimesBLen = startLength * nodeDir.length()));
                        if (theta < (double)closestAngle.getValue().floatValue()) {
                            closestAngle.setValue((Number)theta);
                            selectedWellspring.setValue(nodePos);
                        }
                    });
                    Vec3 end = (Vec3)selectedWellspring.getValue();
                    EntityTeleportEvent tpEvent = new EntityTeleportEvent(target.getEntity(), end.x, end.y, end.z);
                    if (!NeoForge.EVENT_BUS.post((Event)tpEvent)) {
                        target.getLivingEntity().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_FLIGHT.get()), 600));
                        EldrinFlight travel = new EldrinFlight((Level)context.getServerLevel(), (Player)target.getEntity(), target.getEntity().position(), end);
                        context.getServerLevel().addFreshEntity((Entity)travel);
                        target.getLivingEntity().getPersistentData().putInt("eldrin_flight_entity_id", travel.getId());
                        success.setTrue();
                    }
                }
            });
            if (success.getValue().booleanValue()) {
                return ComponentApplicationResult.SUCCESS;
            }
        }
        return ComponentApplicationResult.FAIL;
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
    public float ire() {
        return 0.25f;
    }

    @Override
    public float initialComplexity() {
        return 55.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.SELF;
    }
}


