 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.spells.components.PotionEffectComponent;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class ComponentShacklesOfPain
extends PotionEffectComponent {
    public static final String KEY_SHACKLE_TARGET = "shackles_of_pain_target";

    public ComponentShacklesOfPain(ResourceLocation guiIcon) {
        super(guiIcon, EffectInit.SHACKLES_OF_PAIN, new AttributeValuePair(Attribute.DURATION, 60.0f, 30.0f, 600.0f, 30.0f, 5.0f), new AttributeValuePair(Attribute.LESSER_MAGNITUDE, 1.0f, 0.5f, 2.5f, 0.25f, 15.0f));
        this.addReagent(new ItemStack((ItemLike)Items.CHAIN), new IFaction[0]);
        this.addReagent(new ItemStack((ItemLike)Items.SWEET_BERRIES), new IFaction[0]);
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!source.isPlayerCaster() || !target.isEntity() || source.getCaster() == target.getEntity()) {
            return ComponentApplicationResult.FAIL;
        }
        LivingEntity shackleTarget = ComponentShacklesOfPain.getShackleTarget(source.getPlayer());
        if (shackleTarget != null) {
            shackleTarget.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SHACKLES_OF_PAIN.get()));
        }
        source.getPlayer().getPersistentData().putInt(KEY_SHACKLE_TARGET, target.getEntity().getId());
        return super.ApplyEffect(source, target, modificationData, context);
    }

    @Override
    public int requiredXPForRote() {
        return 135;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BLOOD;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.DEMONS;
    }

    @Override
    public float ire() {
        return 0.05f;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public float initialComplexity() {
        return 60.0f;
    }

    @Nullable
    public static LivingEntity getShackleTarget(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(KEY_SHACKLE_TARGET)) {
            LivingEntity living;
            Entity e = player.level().getEntity(persistent.getInt(KEY_SHACKLE_TARGET));
            if (e != null && e instanceof LivingEntity && (living = (LivingEntity)e).hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SHACKLES_OF_PAIN.get()))) {
                if (living.distanceTo((Entity)player) < 16.0f) {
                    return living;
                }
                living.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SHACKLES_OF_PAIN.get()));
            }
            persistent.remove(KEY_SHACKLE_TARGET);
        }
        return null;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SoundEvents.CHAIN_PLACE;
    }
}

