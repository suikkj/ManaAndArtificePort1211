/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.animal.AbstractGolem
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.bus.api.Event
 *  net.minecraftforge.fluids.capability.IFluidHandler
 *  net.minecraftforge.items.IItemHandlerModifiable
 */
package com.mna.api.entities.construct;

import com.mna.api.entities.construct.Animations;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.ConstructSlot;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.api.entities.construct.IConstructDiagnostics;
import com.mna.api.entities.construct.ai.ConstructAITask;
import com.mna.api.events.construct.ConstructDiagnosticMessageLoggedEvent;
import com.mna.api.faction.IFaction;
import com.mna.api.items.DynamicItemFilter;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.targeting.SpellTarget;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public interface IConstruct<T extends AbstractGolem>
extends IFluidHandler,
IItemHandlerModifiable,
OwnableEntity {
    default public AbstractGolem asEntity() {
        return (AbstractGolem)this;
    }

    public IConstructConstruction getConstructData();

    public IConstructDiagnostics getDiagnostics();

    default public void pushDiagnosticMessage(String message, @Nullable ResourceLocation icon, boolean allowDuplicates) {
        ConstructDiagnosticMessageLoggedEvent event = new ConstructDiagnosticMessageLoggedEvent(this, message);
        NeoForge.EVENT_BUS.post(event);
        this.getDiagnostics().pushDiagnosticMessage(event.getMessage(), icon, allowDuplicates);
    }

    default public void pushDiagnosticMessage(Component message, @Nullable ResourceLocation icon, boolean allowDuplicates) {
        this.pushDiagnosticMessage(message.getContents().toString(), icon, allowDuplicates);
    }

    default public void pushDiagnosticMessage(Component message, @Nullable ResourceLocation icon) {
        this.pushDiagnosticMessage(message, icon, false);
    }

    default public void pushDiagnosticMessage(String message, @Nullable ResourceLocation icon) {
        this.pushDiagnosticMessage(message, icon, false);
    }

    public int getCarrySize();

    public InteractionHand[] getCarryingHands();

    public InteractionHand[] getCarryingHands(Predicate<ItemStack> var1);

    public InteractionHand[] getCarryingHands(DynamicItemFilter var1);

    public InteractionHand[] getEmptyHands();

    public ISpellDefinition[] getCastableSpells();

    @Nullable
    public ISpellDefinition getCastableSpell(ConstructSlot var1);

    public Optional<InteractionHand> getHandWithCapability(ConstructCapability var1);

    public Optional<LivingEntity> getDualCannonTarget();

    public Player getOwner();

    public UUID getOwnerId();

    public void lookTowards(Vec3 var1, float var2);

    public void lookTowards(Vec3 var1);

    public void forceAnimation(Animations var1, boolean var2);

    public void clearForcedAnimation();

    public boolean setupSpellCast(ConstructSlot var1);

    @Deprecated
    public boolean setupSpellCast(boolean var1);

    public boolean startSpellCast(@Nullable SpellTarget var1);

    public boolean tickSpellCast();

    public void resetSpellCast();

    public boolean isRangedAttacking();

    public boolean canManaCannonAttack();

    public boolean canFluidSpray();

    public boolean canDualSweep();

    public boolean expandFluidRange();

    public boolean canSpellCast();

    public boolean isDefeated();

    public boolean isDueling();

    public int getAttackDelay();

    public void setWatering();

    public void setMining(boolean var1);

    public void setAdventuring(boolean var1);

    public void setHunting(boolean var1);

    public boolean isFishing();

    public void setFishing(BlockPos var1);

    public void setEating(InteractionHand var1);

    public void setDefeated(int var1);

    public void stopFishing();

    public void resetActions();

    public int getStoredFluidAmount();

    public float getFluidPct();

    public float getMana();

    public float getMaxMana();

    public float getManaPct();

    default public List<LivingEntity> getValidAttackTargets() {
        return this.getValidAttackTargets(null);
    }

    public List<LivingEntity> getValidAttackTargets(@Nullable LivingEntity var1);

    public boolean validateFriendlyTarget(LivingEntity var1);

    public boolean performRangedAttack(LivingEntity var1);

    public void setHat(ItemStack var1);

    public void setBanner(ItemStack var1);

    public void setDualSweeping();

    public void setHappy(int var1);

    public void setAngry(int var1);

    public void setUnimpressed(int var1);

    public void setConfused(int var1);

    public void setConcerned(int var1);

    public boolean setCurrentCommand(Player var1, ConstructAITask<?> var2);

    public ConstructAITask<?> getCurrentCommand();

    public int getIntelligence();

    public boolean hasItem(ItemStack var1, boolean var2, boolean var3);

    public boolean hasItem(DynamicItemFilter var1);

    public boolean hasItem(ResourceLocation var1, int var2);

    public void dropAllItems();

    default public Tier getBlockHarvestLevel(ConstructCapability capability) {
        ConstructMaterial material = this.getConstructData().getLowestMaterialCooldownMultiplierForCapability(capability);
        return material != null ? material.getEquivalentTier() : Tiers.WOOD;
    }

    public int getTeleportCooldown();

    public void setTeleportCooldown(int var1);

    public int getEquivalentTier();

    @Nullable
    public IFaction getFaction();
}


