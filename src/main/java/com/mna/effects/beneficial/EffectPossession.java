 
package com.mna.effects.beneficial;

import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.effects.interfaces.IDoubleTapEndEarly;
import com.mna.effects.interfaces.IInputDisable;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.factions.Factions;
import com.mna.tools.EntityUtil;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class EffectPossession
extends MobEffect
implements IInputDisable,
IDoubleTapEndEarly,
INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    public static final int AMP_TARGET = 0;
    public static final int AMP_CASTER_STANDARD = 1;
    public static final int AMP_CASTER_GREATER = 2;

    public EffectPossession() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    public void onEffectAdded(LivingEntity entityLivingBaseIn, int amplifier) {
            if (entityLivingBaseIn instanceof Mob) {
                Mob mob = (Mob)entityLivingBaseIn;
                mob.setNoAi(true);
            }
    
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            if (entityLivingBaseIn instanceof Mob) {
                ((Mob)entityLivingBaseIn).setNoAi(false);
            }
            if (entityLivingBaseIn instanceof Player) {
                Player player = (Player)entityLivingBaseIn;
                if (amplifier == 2) {
                    EntityUtil.removeInvisibility((LivingEntity)player);
                    Mob mob = this.getPossessedMob(player);
                    if (mob == null || !mob.isAlive() || mob.isDeadOrDying()) {
                        MutableInt mistFormDuration = new MutableInt(200);
                        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                            if (p.getAlliedFaction() != null && p.getAlliedFaction().isAlliedTo(Factions.UNDEAD)) {
                                mistFormDuration.setValue(-1);
                            }
                        });
                        player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get()), mistFormDuration.getValue().intValue()));
                    }
                }
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }

    @Override
    public EnumSet<IInputDisable.InputMask> getDisabledFlags() {
        return EnumSet.of(IInputDisable.InputMask.LEFT_CLICK, IInputDisable.InputMask.RIGHT_CLICK, IInputDisable.InputMask.MOVEMENT);
    }

    @Override
    public boolean canEndEarly(LivingEntity living, MobEffectInstance effect) {
        return effect.getAmplifier() > 0;
    }

    @Override
    public void onRemoved(LivingEntity living, MobEffectInstance effect) {
        Player player;
        Mob mob;
        if (living instanceof Player && (mob = this.getPossessedMob(player = (Player)living)) != null) {
            mob.removeEffect(com.mna.api.tools.MAEffects.holder(this));
        }
    }

    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    public boolean applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if (pLivingEntity instanceof Player) {
            Mob mob;
            Player player = (Player)pLivingEntity;
            if (pAmplifier == 2 && (mob = this.getPossessedMob(player)) != null) {
                Vec3 forward = mob.getForward();
                player.teleportTo(mob.getX() - forward.x, mob.getY(), mob.getZ() - forward.z);
                player.resetFallDistance();
                player.setInvisible(true);
            }
        }
        return true;
    }

    @Nullable
    private Mob getPossessedMob(Player player) {
        if (player.getPersistentData().contains("posessed_entity_id")) {
            int id = player.getPersistentData().getInt("posessed_entity_id");
            Entity e = player.level().getEntity(id);
            if (e instanceof Mob) {
                Mob mob = (Mob)e;
                return mob;
            }
        }
        return null;
    }
}

