 
package com.mna.effects.neutral;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.factions.Factions;
import java.lang.reflect.Field;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import com.mna.api.tools.MNACap;

public class EffectColdDark
extends MobEffect
implements INoCreeperLingering, com.mna.api.effects.IMNAEffectHook {
    private static final Field SLEEP_TIMER = ObfuscationReflectionHelper.findField(Player.class, (String)"sleepCounter");
    public static final String PD_KEY = "coldDarkPos";

    public EffectColdDark() {
        super(MobEffectCategory.NEUTRAL, 0);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private void trySetSleepTimer(Player player) {
        try {
            SLEEP_TIMER.setInt(player, 0);
        }
        catch (Exception ex) {
            ManaAndArtifice.LOGGER.error("Error setting player sleep timer");
        }
    }

    public void onEffectAdded(LivingEntity entityLivingBaseIn, int amplifier) {
            if (entityLivingBaseIn instanceof Player && entityLivingBaseIn.getPersistentData().contains(PD_KEY)) {
                long lpos = entityLivingBaseIn.getPersistentData().getLong(PD_KEY);
                ((Player)entityLivingBaseIn).startSleeping(BlockPos.of((long)lpos));
                this.trySetSleepTimer((Player)entityLivingBaseIn);
            }
    
    }

    public void addAttributeModifiers(AttributeMap attributeMapIn, int amplifier) {
        super.addAttributeModifiers(attributeMapIn, amplifier);
    }

    public void onEffectRemoved(LivingEntity entityLivingBaseIn, int amplifier) {
            if (entityLivingBaseIn instanceof Player && entityLivingBaseIn.getPersistentData().contains(PD_KEY)) {
                MNACap.get((Player)entityLivingBaseIn, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                    if (p.getAlliedFaction() == null) {
                        p.setAlliedFaction(Factions.UNDEAD, (Player)entityLivingBaseIn);
                        ((Player)entityLivingBaseIn).sendSystemMessage((Component)Component.translatable((String)"event.mna.faction_ally_undead").withStyle(ChatFormatting.AQUA));
                    }
                    if (p.getAlliedFaction() == Factions.UNDEAD) {
                        p.setTier(p.getTier() + 1, (Player)entityLivingBaseIn);
                        ((Player)entityLivingBaseIn).sendSystemMessage((Component)Component.translatable((String)"mna:progresscondition.advanced", (Object[])new Object[]{p.getTier()}).withStyle(ChatFormatting.AQUA));
                    }
                });
                MNACap.get((Player)entityLivingBaseIn, PlayerMagicProvider.MAGIC).ifPresent(m -> m.setCastingResourceType(CastingResourceIDs.SOULS));
                entityLivingBaseIn.getPersistentData().remove(PD_KEY);
                entityLivingBaseIn.stopSleeping();
            }
    
    }

    public void removeAttributeModifiers(AttributeMap attributeMapIn) {
        super.removeAttributeModifiers(attributeMapIn);
    }

    public boolean applyEffectTick(LivingEntity entityLivingBaseIn, int amplifier) {
        if (entityLivingBaseIn instanceof Player && entityLivingBaseIn.getPersistentData().contains(PD_KEY)) {
            this.trySetSleepTimer((Player)entityLivingBaseIn);
        }
        return true;
    }
}


