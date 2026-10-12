/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.ModList
 */
package com.mna.api.spells.base;

import com.mna.api.ManaAndArtificeMod;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.SpellBlacklistResult;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public interface ISpellComponent {
    public ResourceLocation getRegistryName();

    public ResourceLocation getGuiIcon();

    public boolean isCraftable(SpellCraftingContext var1);

    public boolean isUseableByPlayers();

    public int requiredXPForRote();

    public SpellPartTags getUseTag();

    default public String getDescriptionTooltip(Attribute forAttr) {
        return this.getRegistryName().toString() + ".desc." + forAttr.toString().toLowerCase();
    }

    default public IFaction getFactionRequirement() {
        return null;
    }

    default public boolean canBeOnRandomStaff() {
        return !this.isSilverSpell();
    }

    default public boolean replacesHeldItem() {
        return false;
    }

    public int getTier(Level var1);

    public SpellBlacklistResult canBeCastAt(Level var1, Vec3 var2);

    public void onRegistered();

    default public boolean magnitudeHealthCheck(SpellSource source, SpellTarget target, int magnitude, int healthPerMagnitude) {
        if (source.getCaster() == target.getEntity()) {
            return true;
        }
        if (!target.isLivingEntity()) {
            return false;
        }
        int maxHP = healthPerMagnitude * magnitude;
        if (target.getLivingEntity().getMaxHealth() > (float)maxHP) {
            if (source.isPlayerCaster()) {
                source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:generic.too_powerful"));
            }
            return false;
        }
        return true;
    }

    default public boolean isTargetFriendlyToCaster(SpellSource source, Entity target) {
        if (!target.isAlive() || !(target instanceof LivingEntity) || source.isPlayerCaster()) {
            return false;
        }
        boolean targetIsPlayer = target instanceof Player;
        if (targetIsPlayer && (((Player)target).isCreative() || ((Player)target).isSpectator())) {
            return true;
        }
        return ManaAndArtificeMod.getSummonHelper().isEntityFriendly(target, source.getCaster());
    }

    default public boolean isSilverSpell() {
        return false;
    }

    default public boolean isBaseMna() {
        return this.getRegistryName().getNamespace().equals("mna");
    }

    default public String getAddingModName() {
        Optional otherModContainer = ModList.get().getModContainerById(this.getRegistryName().getNamespace());
        if (otherModContainer.isPresent()) {
            return ((ModContainer)otherModContainer.get()).getModInfo().getDisplayName();
        }
        return "<Unknown Mod>";
    }

    default public void initializeConfigs(AttributeValuePair ... attributeValuePairs) {
        if (ManaAndArtificeMod.getConfigHelper() == null) {
            return;
        }
        if (!ManaAndArtificeMod.getConfigHelper().isPartInitialized(this)) {
            ManaAndArtificeMod.getConfigHelper().initForPart(this, attributeValuePairs);
        }
    }

    default public float ire() {
        return 0.01f;
    }
}


