 
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
import com.mna.effects.EffectInit;
import com.mna.entities.boss.attacks.IllusionCreeper;
import com.mna.factions.Factions;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentIllusoryCreeper
extends SpellEffect {
    public ComponentIllusoryCreeper(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.DURATION, 10.0f, 10.0f, 60.0f, 5.0f, 5.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        Level world = context.getLevel();
        if (!world.isClientSide) {
            if (source.getCaster() == null || context.hasEntityBeenAffected(this, (Entity)source.getCaster())) {
                return ComponentApplicationResult.FAIL;
            }
            context.addAffectedEntity(this, (Entity)source.getCaster());
            IllusionCreeper ic = new IllusionCreeper(world);
            Vec3i normal = target.getBlockFace(this).getNormal();
            BlockPos spawnPos = BlockPos.containing((Position)target.getPosition().add((double)normal.getX(), (double)normal.getY(), (double)normal.getZ()));
            int count = 0;
            while (count < 10 && !world.getBlockState(spawnPos).getCollisionShape((BlockGetter)world, spawnPos).isEmpty()) {
                ++count;
                spawnPos = spawnPos.above();
            }
            if (count >= 10) {
                return ComponentApplicationResult.FAIL;
            }
            ic.setPos(Vec3.atBottomCenterOf((Vec3i)spawnPos));
            ic.getPersistentData().putBoolean("mna:deathflag", true);
            ic.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.TIMED_DEATH.get()), (int)modificationData.getValue(Attribute.DURATION) * 20));
            world.addFreshEntity((Entity)ic);
        }
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.FEY;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ARCANE, Affinity.ENDER, Affinity.WATER, Affinity.WIND, Affinity.ICE, Affinity.LIGHTNING, Affinity.EARTH);
    }

    @Override
    public boolean isSilverSpell() {
        return true;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.SELF;
    }
}

