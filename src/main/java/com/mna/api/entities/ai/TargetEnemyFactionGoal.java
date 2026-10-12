 
package com.mna.api.entities.ai;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.faction.IFaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class TargetEnemyFactionGoal
extends NearestAttackableTargetGoal<LivingEntity> {
    private IFaction alliedTo;

    public TargetEnemyFactionGoal(Mob pMob, Class<LivingEntity> pTargetType, boolean pMustSee, boolean pMustReach, IFaction alliedFaction) {
        super(pMob, pTargetType, pMustSee, pMustReach);
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this::targetPredicate);
        this.alliedTo = alliedFaction;
    }

    protected boolean targetPredicate(LivingEntity potentialTarget) {
        MutableBoolean isFaction = new MutableBoolean(false);
        if (potentialTarget instanceof Player) {
            Player player = (Player)potentialTarget;
            MNACap.get(player, ManaAndArtificeMod.getProgressionCapability()).ifPresent(progression -> isFaction.setValue(player.isAlive() && progression.getAlliedFaction() != null && progression.getAlliedFaction().getEnemyFactions().contains(this.alliedTo)));
            return isFaction.booleanValue();
        }
        if (potentialTarget instanceof Mob) {
            LivingEntity livingEntity;
            Mob mob = (Mob)potentialTarget;
            if (mob instanceof IFactionEnemy && ((IFactionEnemy)mob).getFaction().getAlliedFactions().contains(this.alliedTo)) {
                return false;
            }
            if (mob.isAlive() && (livingEntity = mob.getTarget()) instanceof Player) {
                Player player = (Player)livingEntity;
                MNACap.get(player, ManaAndArtificeMod.getProgressionCapability()).ifPresent(p -> {
                    IFaction playerFaction = p.getAlliedFaction();
                    if (this.alliedTo != null && playerFaction != null && this.alliedTo.isAlliedTo(playerFaction)) {
                        isFaction.setTrue();
                    }
                });
                return isFaction.booleanValue();
            }
        }
        return false;
    }
}

