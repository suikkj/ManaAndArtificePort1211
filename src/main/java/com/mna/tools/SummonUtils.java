 
package com.mna.tools;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.ISummonHelper;
import com.mna.api.entities.ai.CastSpellAtTargetGoal;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.ai.FollowOwnerGoal;
import com.mna.entities.ai.NeverTargetOwnerGoal;
import com.mna.entities.ai.SummonMeleeFallbackGoal;
import com.mna.entities.ai.TargetDefendOwnerGoal;
import com.mna.entities.summon.Simulacrum;
import com.mna.factions.Factions;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableInt;
import com.mna.api.tools.MNACap;

public class SummonUtils
implements ISummonHelper {
    public static final String TAG_SUMMON_IDS = "mna:summon_ids";
    public static final String TAG_BONUS_SUMMONS = "mna:bonus_summon_count";
    public static final String TAG_EMBER_IDS = "mna:ember_ids";
    public static final String TAG_SIMULACRUM_IDS = "mna:simulacrum_ids";

    public static boolean isSummon(Entity candidate) {
        return candidate != null && candidate.getPersistentData().contains("mna:summon_tag");
    }

    private static int getSummonerID(Entity candidate) {
        if (!SummonUtils.isSummon(candidate)) {
            return -1;
        }
        return candidate.getPersistentData().getInt("mna:summon_tag");
    }

    @Nullable
    public static LivingEntity getSummoner(LivingEntity candidate) {
        if (candidate instanceof OwnableEntity) {
            OwnableEntity ownable = (OwnableEntity)candidate;
            return ownable.getOwner();
        }
        if (!SummonUtils.isSummon((Entity)candidate)) {
            return null;
        }
        int summonerID = candidate.getPersistentData().getInt("mna:summon_tag");
        Entity e = candidate.level().getEntity(summonerID);
        if (e instanceof LivingEntity) {
            return (LivingEntity)e;
        }
        return null;
    }

    @Nullable
    public static Player getSummonerAsPlayer(LivingEntity candidate) {
        if (!SummonUtils.isSummon((Entity)candidate)) {
            return null;
        }
        int summonerID = candidate.getPersistentData().getInt("mna:summon_tag");
        Entity e = candidate.level().getEntity(summonerID);
        if (e instanceof Player) {
            return (Player)e;
        }
        return null;
    }

    public static void tagAsSummon(Entity entity, LivingEntity owner) {
        entity.getPersistentData().putInt("mna:summon_tag", owner.getId());
    }

    public static boolean setSummon(Mob candidate, LivingEntity owner, int duration) {
        return SummonUtils.setSummon(candidate, owner, true, duration);
    }

    public static boolean setSummon(Mob candidate, LivingEntity owner, boolean overrideAI, int duration) {
        if (SummonUtils.isSummon((Entity)candidate)) {
            return false;
        }
        SummonUtils.tagAsSummon((Entity)candidate, owner);
        if (duration > 0) {
            candidate.getPersistentData().putBoolean("mna:deathflag", true);
            candidate.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.TIMED_DEATH.get()), duration));
        }
        if (overrideAI) {
            candidate.goalSelector.addGoal(0, (Goal)new FollowOwnerGoal(candidate, 1.35, 8.0f, 20.0f, 32.0f, true));
            com.mna.tools.MNAGoalSelectorAccess.availableGoals(candidate.targetSelector).clear();
            candidate.targetSelector.addGoal(0, (Goal)new NeverTargetOwnerGoal(candidate));
            candidate.targetSelector.addGoal(1, new TargetDefendOwnerGoal<LivingEntity>(candidate, LivingEntity.class, 10, true, false));
            if (!com.mna.tools.MNAGoalSelectorAccess.availableGoals(candidate.goalSelector).stream().anyMatch(g -> g.getGoal() instanceof MeleeAttackGoal || g.getGoal() instanceof CastSpellAtTargetGoal || g.getGoal() instanceof RangedAttackGoal || g.getGoal() instanceof RangedBowAttackGoal || g.getGoal() instanceof RangedCrossbowAttackGoal)) {
                candidate.goalSelector.addGoal(1, (Goal)new SummonMeleeFallbackGoal(candidate, 1.35f, false));
            }
            if (candidate instanceof Pillager) {
                com.mna.tools.MNAGoalSelectorAccess.availableGoals(candidate.goalSelector).removeIf(g -> g.getPriority() == 2);
            }
        }
        return true;
    }

    public static List<Mob> getSummons(LivingEntity summoner) {
        return SummonUtils.getSummons(summoner, summoner.level());
    }

    public static List<Mob> getSummons(LivingEntity summoner, Level world) {
        LinkedList<Mob> summons = new LinkedList<Mob>();
        if (summoner != null && world != null && summoner.getPersistentData().contains(TAG_SUMMON_IDS)) {
            int[] ids = summoner.getPersistentData().getIntArray(TAG_SUMMON_IDS);
            for (int i = 0; i < ids.length; ++i) {
                Entity e = world.getEntity(ids[i]);
                if (e == null || !(e instanceof Mob) || !SummonUtils.isSummon(e) || SummonUtils.getSummonerID(e) != summoner.getId() || !e.isAlive()) continue;
                summons.push((Mob)e);
            }
            ArrayList idList = new ArrayList();
            summons.forEach(s -> idList.add(s.getId()));
            summoner.getPersistentData().putIntArray(TAG_SUMMON_IDS, idList);
        }
        return summons;
    }

    public static boolean isTargetFriendly(Entity target, LivingEntity summoner) {
        if (target == null || summoner == null || !target.isAlive()) {
            return true;
        }
        int count = 0;
        while (SummonUtils.isSummon((Entity)summoner) && count++ < 10) {
            summoner = SummonUtils.getSummoner(summoner);
        }
        if (target instanceof OwnableEntity) {
            OwnableEntity ownable = (OwnableEntity)target;
            summoner = ownable.getOwner();
        }
        if (summoner == null) {
            return true;
        }
        if (target == summoner) {
            return true;
        }
        if (target instanceof LivingEntity) {
            NeutralMob neutral;
            LivingEntity living = (LivingEntity)target;
            if (living.isDeadOrDying()) {
                return true;
            }
            if (target == summoner.getLastHurtMob() || target == summoner.getLastHurtByMob()) {
                return false;
            }
            LivingEntity livingSummoner = SummonUtils.getSummoner(living);
            if (livingSummoner != null) {
                if (livingSummoner.getId() == summoner.getId()) {
                    return true;
                }
                return SummonUtils.isTargetFriendly((Entity)livingSummoner, summoner);
            }
            if (living instanceof NeutralMob && !(neutral = (NeutralMob)living).isAngryAt(summoner)) {
                return true;
            }
            if (summoner instanceof Player) {
                IPlayerProgression prog = (IPlayerProgression)summoner.getData(PlayerProgressionProvider.PROGRESSION.get());
                if (prog != null && living instanceof IFactionEnemy) {
                    IFaction livingFaction = ((IFactionEnemy)living).getFaction();
                    IFaction summonerFaction = prog.getAlliedFaction();
                    if (livingFaction != null && summonerFaction != null) {
                        if (summonerFaction.getEnemyFactions().contains(livingFaction)) {
                            return false;
                        }
                        if (summonerFaction.isAlliedTo(livingFaction)) {
                            return true;
                        }
                    }
                }
                if (living.getTeam() != null && living.getTeam().isAlliedTo(summoner.getTeam())) {
                    return true;
                }
                if (living instanceof Monster) {
                    return false;
                }
                if (living instanceof Mob && ((Mob)living).getTarget() == summoner) {
                    return false;
                }
            } else {
                Mob lMob;
                if (living instanceof Mob && (lMob = (Mob)living).getTarget() == summoner) {
                    return false;
                }
                if (summoner instanceof IFactionEnemy) {
                    Player targetPlayer;
                    IPlayerProgression prog;
                    IFactionEnemy summonerFactionEntity = (IFactionEnemy)summoner;
                    if (living instanceof IFactionEnemy) {
                        return ((IFactionEnemy)living).getFaction() == summonerFactionEntity.getFaction();
                    }
                    if (living instanceof Player && (prog = (IPlayerProgression)(targetPlayer = MNACap.get((Player)living, PlayerProgressionProvider.PROGRESSION)).orElse(null)) != null && prog.getAlliedFaction() != null && prog.getAlliedFaction().isAlliedTo(summonerFactionEntity.getFaction())) {
                        return true;
                    }
                }
                if (summoner instanceof Mob && (lMob = (Mob)summoner).getTarget() == living) {
                    return false;
                }
                return !(living instanceof Player);
            }
        }
        return true;
    }

    public static void clampTrackedEntities(LivingEntity summoner) {
        SummonUtils.clampTrackedEntities(summoner, TAG_SUMMON_IDS, SummonUtils::getMaxSummons, SummonUtils::isValidEntity);
    }

    public static void clampTrackedEntities(LivingEntity summoner, String key, Function<LivingEntity, Integer> maximumEvaluator, BiFunction<Entity, LivingEntity, Boolean> validator) {
        int maxSummons = maximumEvaluator.apply(summoner);
        SummonUtils.countAndCleanupTrackedEntities(summoner, key);
        LinkedList<Integer> entityIDs = new LinkedList<Integer>();
        if (summoner.getPersistentData().contains(key)) {
            int[] ids = summoner.getPersistentData().getIntArray(key);
            for (int i = 0; i < ids.length; ++i) {
                entityIDs.add(ids[i]);
            }
            while (entityIDs.size() >= maxSummons) {
                Entity e = summoner.level().getEntity(((Integer)entityIDs.pop()).intValue());
                if (e == null || !validator.apply(e, summoner).booleanValue()) continue;
                e.discard();
            }
        }
    }

    public static int countTrackedEntities(LivingEntity summoner, String key) {
        if (summoner.getPersistentData().contains(key)) {
            int[] ids = summoner.getPersistentData().getIntArray(key);
            return ids.length;
        }
        return 0;
    }

    public static void limitSummonsOfType(LivingEntity summoner, Class<? extends LivingEntity> type, int limit) {
        SummonUtils.limitSummonsOfType(summoner, TAG_SUMMON_IDS, type, limit);
    }

    public static void limitSummonsOfType(LivingEntity summoner, String key, Class<? extends LivingEntity> type, int limit) {
        if (summoner.getPersistentData().contains(key)) {
            int count = 0;
            int[] ids = summoner.getPersistentData().getIntArray(key);
            for (int idx = ids.length - 1; idx >= 0; --idx) {
                LivingEntity living;
                int entityID = ids[idx];
                Entity summon = summoner.level().getEntity(entityID);
                if (!(summon instanceof LivingEntity) || !SummonUtils.isSummon((Entity)(living = (LivingEntity)summon)) || SummonUtils.getSummoner(living) != summoner || !type.isInstance(living) || ++count <= limit) continue;
                summon.discard();
            }
        }
    }

    private static int countAndCleanupTrackedEntities(LivingEntity summoner, String key) {
        if (summoner.getPersistentData().contains(key)) {
            int[] ids = summoner.getPersistentData().getIntArray(key);
            ArrayList<Integer> idList = new ArrayList<Integer>();
            for (int i : ids) {
                Entity e = summoner.level().getEntity(i);
                if (e == null || !e.isAlive() || !SummonUtils.isSummon(e) || SummonUtils.getSummonerID(e) != summoner.getId()) continue;
                idList.add(i);
            }
            summoner.getPersistentData().putIntArray(key, idList);
            return idList.size();
        }
        return 0;
    }

    public static boolean isTracked(LivingEntity summoner, Entity e, String key) {
        if (summoner.getPersistentData().contains(key)) {
            int[] ids;
            for (int i : ids = summoner.getPersistentData().getIntArray(key)) {
                if (i != e.getId()) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean isFirstTrackedEntity(LivingEntity summoner, String key, Entity e) {
        int[] ids;
        SummonUtils.countAndCleanupTrackedEntities(summoner, key);
        if (summoner.getPersistentData().contains(key) && (ids = summoner.getPersistentData().getIntArray(key)).length > 0) {
            return ids[0] == e.getId();
        }
        return false;
    }

    public static void addTrackedEntity(LivingEntity summoner, Entity summon) {
        SummonUtils.addTrackedEntity(summoner, summon, TAG_SUMMON_IDS);
    }

    public static void addTrackedEntity(LivingEntity summoner, Entity entity, String key) {
        LinkedList<Integer> entityIDs = new LinkedList<Integer>();
        if (summoner.getPersistentData().contains(key)) {
            int[] ids = summoner.getPersistentData().getIntArray(key);
            for (int i = 0; i < ids.length; ++i) {
                entityIDs.add(ids[i]);
            }
        }
        entityIDs.add(entity.getId());
        summoner.getPersistentData().putIntArray(key, entityIDs);
    }

    public static int getMaxSummons(LivingEntity summoner) {
        if (summoner instanceof Simulacrum) {
            return 1;
        }
        MutableInt max_summons = new MutableInt(2);
        MNACap.get(summoner, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getAlliedFaction() == Factions.UNDEAD) {
                max_summons.add(p.getTier() - 2);
            }
        });
        int bonus = summoner.getPersistentData().getInt(TAG_BONUS_SUMMONS);
        max_summons.add(bonus);
        return max_summons.getValue();
    }

    public static boolean isValidEntity(Entity e, LivingEntity living) {
        return e instanceof LivingEntity && SummonUtils.isSummon(e) && SummonUtils.getSummoner((LivingEntity)e) == living;
    }

    public static int getMaxEmbers(LivingEntity living) {
        MutableInt max_embers = new MutableInt(3);
        MNACap.get(living, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getAlliedFaction() == Factions.COUNCIL) {
                max_embers.add(p.getTier() - 2);
            }
        });
        return max_embers.getValue();
    }

    public static void iterateTrackedEntities(LivingEntity living, String key, Consumer<Integer> iterator) {
        if (living.getPersistentData().contains(key)) {
            int[] ids;
            for (int i : ids = living.getPersistentData().getIntArray(key)) {
                iterator.accept(i);
            }
        }
    }

    public static void setBonusSummons(LivingEntity living, int bonus) {
        living.getPersistentData().putInt(TAG_BONUS_SUMMONS, bonus);
    }

    @Override
    public boolean checkIsSummon(Entity candidate) {
        return SummonUtils.isSummon(candidate);
    }

    @Override
    public LivingEntity resolveSummoner(LivingEntity candidate) {
        return SummonUtils.getSummoner(candidate);
    }

    @Override
    public boolean makeSummon(Mob candidate, LivingEntity owner, int duration) {
        return SummonUtils.setSummon(candidate, owner, duration);
    }

    @Override
    public boolean makeSummon(Mob candidate, LivingEntity owner, boolean overrideAI, int duration) {
        return SummonUtils.setSummon(candidate, owner, overrideAI, duration);
    }

    @Override
    public List<Mob> getAllSummons(LivingEntity summoner) {
        return SummonUtils.getSummons(summoner);
    }

    @Override
    public boolean isEntityFriendly(Entity target, LivingEntity summoner) {
        return SummonUtils.isTargetFriendly(target, summoner);
    }

    @Override
    public int getSummonCap(LivingEntity summoner) {
        return SummonUtils.getMaxSummons(summoner);
    }

    @Override
    public int getEmberCap(LivingEntity living) {
        return SummonUtils.getMaxEmbers(living);
    }

    @Override
    public void setSummonBonus(LivingEntity living, int bonus) {
        SummonUtils.setBonusSummons(living, bonus);
    }
}

