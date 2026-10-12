 
package com.mna.spells.components;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.entities.DamageHelper;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedDamage;
import com.mna.effects.EffectInit;
import com.mna.tools.TeleportHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ComponentWtfboom
extends SpellEffect {
    public ComponentWtfboom(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!source.isPlayerCaster()) {
            return ComponentApplicationResult.FAIL;
        }
        Player caster = source.getPlayer();
        if (!caster.level().isClientSide()) {
            if (target.getEntity() != caster && caster instanceof ServerPlayer) {
                CustomAdvancementTriggers.NOT_SO_EASY.trigger((ServerPlayer)caster);
            }
            TeleportHelper.teleportEntity((Entity)caster, (ResourceKey<Level>)caster.level().dimension(), new Vec3(caster.getX(), 300.0, caster.getZ()));
            caster.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()), 500, 2, false, false));
            caster.level().playSound(null, caster.getX(), caster.getY(), caster.getZ(), SFX.Event.Player.WTF_BOOM, SoundSource.PLAYERS, 1.0f, 1.0f);
            DelayedEventQueue.pushEvent(caster.level(), new TimedDelayedDamage("wtfboom", 264, 2.1474836E9f, (Entity)caster, DamageHelper.forType(DamageHelper.WTF_BOOM, caster.level().registryAccess())));
        }
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public int requiredXPForRote() {
        return 2000;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ENDER;
    }

    @Override
    public float initialComplexity() {
        return 500.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.DONOTUSE;
    }

    @Override
    public boolean isCraftable(SpellCraftingContext context) {
        return false;
    }
}

