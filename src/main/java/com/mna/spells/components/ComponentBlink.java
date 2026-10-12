 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.ItemInit;
import com.mna.items.artifice.SpellModifierRing;
import com.mna.tools.TeleportHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.bus.api.Event;
import top.theillusivec4.curios.api.CuriosApi;
import com.mna.api.tools.MNACap;

public class ComponentBlink
extends SpellEffect {
    public ComponentBlink(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RANGE, 5.0f, 5.0f, 16.0f, 1.0f, 2.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (target == SpellTarget.NONE) {
            return ComponentApplicationResult.FAIL;
        }
        Vec3 targetPosition = null;
        LivingEntity tgt = null;
        if (target.isLivingEntity()) {
            if (source.isPlayerCaster() && ((SpellModifierRing)ItemInit.BLINK_PRECISION_RING.get()).isEquippedAndHasMana(source.getCaster(), 1.0f, false) && source.getCaster() != target.getEntity()) {
                float step = 1.5f;
                Vec3 delta = target.getLivingEntity().getForward().normalize().scale((double)step);
                tgt = source.getCaster();
                int tries = 0;
                do {
                    ++tries;
                    targetPosition = target.getLivingEntity().position().subtract(delta);
                    if (!(targetPosition.distanceTo(source.getCaster().position()) < (double)step)) continue;
                    ((Player)source.getCaster()).sendSystemMessage((Component)Component.translatable((String)"mna:components/blink.failed"));
                    return ComponentApplicationResult.FAIL;
                } while (tries <= 10 && !TeleportHelper.coordsValidForBlink((Level)context.getServerLevel(), (int)targetPosition.x(), (int)targetPosition.y(), (int)targetPosition.z()));
                Vec3 targetEye = target.getLivingEntity().getEyePosition();
                Player looker = source.getPlayer();
                DelayedEventQueue.pushEvent(context.getLevel(), new TimedDelayedEvent<Vec3>("look", 1, targetEye, (k, v) -> looker.lookAt(EntityAnchorArgument.Anchor.EYES, targetEye)));
                CuriosApi.getCuriosHelper().findFirstCurio(source.getCaster(), (Item)ItemInit.BLINK_PRECISION_RING.get()).ifPresent(t -> ((SpellModifierRing)ItemInit.BLINK_PRECISION_RING.get()).consumeMana(t.stack(), 1.0f, source.getPlayer()));
            } else {
                tgt = target.getLivingEntity();
                targetPosition = TeleportHelper.calculateBlinkPosition(modificationData.getValue(Attribute.RANGE), tgt, tgt.getForward(), (Level)context.getServerLevel());
            }
        } else if (target.isBlock()) {
            if (source.hasCasterReference() && source.isPlayerCaster() && ((SpellModifierRing)ItemInit.BLINK_PRECISION_RING.get()).isEquippedAndHasMana(source.getCaster(), 1.0f, false)) {
                tgt = source.getCaster();
                BlockPos check = target.getBlock().offset(target.getBlockFace(this).getNormal());
                if (TeleportHelper.coordsValidForBlink((Level)context.getServerLevel(), check.getX(), check.getY(), check.getZ()) && (targetPosition = Vec3.atBottomCenterOf((Vec3i)check)).distanceTo(tgt.position()) > (double)(modificationData.getValue(Attribute.RANGE) * 2.0f)) {
                    if (tgt instanceof Player) {
                        ((Player)tgt).sendSystemMessage((Component)Component.translatable((String)"mna:components/blink.toofar"));
                    }
                    return ComponentApplicationResult.FAIL;
                }
                if (source.isPlayerCaster()) {
                    CuriosApi.getCuriosHelper().findFirstCurio(source.getCaster(), (Item)ItemInit.BLINK_PRECISION_RING.get()).ifPresent(t -> ((SpellModifierRing)ItemInit.BLINK_PRECISION_RING.get()).consumeMana(t.stack(), 1.0f, source.getPlayer()));
                }
            }
        } else {
            return ComponentApplicationResult.FAIL;
        }
        if (tgt == source.getCaster() && source.isPlayerCaster()) {
            IPlayerMagic magic = MNACap.get((IPlayerMagic)source.getPlayer(), PlayerMagicProvider.MAGIC).orElse(null);
            if (magic == null || magic.getAirCasts() >= magic.getAirCastLimit(source.getPlayer(), context.getSpell())) {
                return ComponentApplicationResult.FAIL;
            }
            if (!source.getPlayer().onGround()) {
                magic.incrementAirCasts(source.getPlayer(), context.getSpell());
            }
        }
        if (!context.getServerLevel().isClientSide() && tgt != null) {
            if (targetPosition == null) {
                if (tgt instanceof Player) {
                    ((Player)tgt).sendSystemMessage((Component)Component.translatable((String)"mna:components/blink.failed"));
                }
                return ComponentApplicationResult.FAIL;
            }
            EntityTeleportEvent tpEvent = new EntityTeleportEvent((Entity)tgt, targetPosition.x, targetPosition.y, targetPosition.z);
            if (!NeoForge.EVENT_BUS.post(tpEvent)) {
                tgt.teleportTo(targetPosition.x, targetPosition.y, targetPosition.z);
            }
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age == 0) {
            float particle_spread = 1.0f;
            float v = 1.0f;
            int particleCount = 25;
            for (int i = 0; i < particleCount; ++i) {
                Vec3 velocity = new Vec3(0.0, Math.random() * (double)v, 0.0);
                world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get())), (Entity)caster), impact_position.x + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.y + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), impact_position.z + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
            }
        }
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 300;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.SELF;
    }
}


