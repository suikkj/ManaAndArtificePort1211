/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.mna.api.spells.base;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.SpellCastingResult;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ISpellDefinition {
    public static final ISpellDefinition EMPTY = new ISpellDefinition(){

        @Override
        public void writeToNBT(CompoundTag nbt) {
        }

        @Override
        public void setParticleColorOverride(int color) {
        }

        @Override
        public void setOverrideAffinity(Affinity affinity) {
        }

        @Override
        public void setManaCost(float manaCost) {
        }

        @Override
        public void iterateComponents(Consumer<IModifiedSpellPart<SpellEffect>> consumer) {
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public boolean isMysterious() {
            return false;
        }

        @Override
        public boolean isHarmful() {
            return false;
        }

        @Override
        public boolean isChanneled() {
            return false;
        }

        @Override
        public int getTier(Level world) {
            return 0;
        }

        @Override
        public IModifiedSpellPart<Shape> getShape() {
            return null;
        }

        @Override
        public List<SpellReagent> getReagents(Player caster, InteractionHand hand, SpellCastingResult castResult) {
            return new ArrayList<SpellReagent>();
        }

        @Override
        public int getParticleColorOverride() {
            return 0;
        }

        @Override
        public List<Modifier> getModifiers() {
            return new ArrayList<Modifier>();
        }

        @Override
        public Modifier getModifier(int index) {
            return null;
        }

        @Override
        public float getManaCost() {
            return 0.0f;
        }

        @Override
        public Affinity getHighestAffinity() {
            return null;
        }

        @Override
        public List<IModifiedSpellPart<SpellEffect>> getComponents() {
            return new ArrayList<IModifiedSpellPart<SpellEffect>>();
        }

        @Override
        public IModifiedSpellPart<SpellEffect> getComponent(int index) {
            return null;
        }

        @Override
        public float getComplexity() {
            return 0.0f;
        }

        @Override
        public HashMap<Affinity, Float> getAffinity() {
            return new HashMap<Affinity, Float>();
        }

        @Override
        public int findComponent(SpellEffect component) {
            return 0;
        }

        @Override
        public int countModifiers() {
            return 0;
        }

        @Override
        public int countComponents() {
            return 0;
        }

        @Override
        public MAParticleType colorParticle(MAParticleType particle, Entity living) {
            return particle;
        }

        @Override
        public void clearComponents() {
        }

        @Override
        public int getCooldown(@Nullable LivingEntity caster) {
            return 0;
        }

        @Override
        public boolean canFactionCraft(IPlayerProgression progression) {
            return false;
        }

        @Override
        public void addItemTooltip(ItemStack stack, Level worldIn, List<Component> tooltip, Player player) {
        }

        @Override
        public boolean isFactionSpell(IFaction faction) {
            return false;
        }

        @Override
        public boolean containsPart(ResourceLocation partID) {
            return false;
        }

        @Override
        public boolean isChargedSpell(ItemStack stack) {
            return false;
        }

        @Override
        public void consumeCharges(ItemStack stack, Player caster) {
        }
    };

    @Nullable
    public IModifiedSpellPart<Shape> getShape();

    @Nullable
    public IModifiedSpellPart<SpellEffect> getComponent(int var1);

    public List<IModifiedSpellPart<SpellEffect>> getComponents();

    public void iterateComponents(Consumer<IModifiedSpellPart<SpellEffect>> var1);

    public int countComponents();

    public void clearComponents();

    public boolean isMysterious();

    public int findComponent(SpellEffect var1);

    @Nullable
    public Modifier getModifier(int var1);

    public List<Modifier> getModifiers();

    public int countModifiers();

    public HashMap<Affinity, Float> getAffinity();

    public Affinity getHighestAffinity();

    public void setOverrideAffinity(Affinity var1);

    public int getTier(@Nonnull Level var1);

    public boolean isValid();

    public float getComplexity();

    public float getManaCost();

    public boolean isChanneled();

    public boolean isHarmful();

    public int getCooldown(@Nullable LivingEntity var1);

    public int getParticleColorOverride();

    public void setParticleColorOverride(int var1);

    public MAParticleType colorParticle(MAParticleType var1, Entity var2);

    public boolean canFactionCraft(IPlayerProgression var1);

    public List<SpellReagent> getReagents(@Nullable Player var1, @Nullable InteractionHand var2, @Nullable SpellCastingResult var3);

    public void setManaCost(float var1);

    public void writeToNBT(CompoundTag var1);

    public void addItemTooltip(ItemStack var1, Level var2, List<Component> var3, Player var4);

    public boolean isFactionSpell(IFaction var1);

    public boolean containsPart(ResourceLocation var1);

    public boolean isChargedSpell(ItemStack var1);

    public void consumeCharges(ItemStack var1, Player var2);

    default public boolean isSame(ISpellDefinition other, boolean checkShape, boolean checkComponents, boolean checkModifiers) {
        int i;
        if (checkShape && !this.getShape().isSame(other.getShape())) {
            return false;
        }
        if (checkComponents) {
            if (this.countComponents() != other.countComponents()) {
                return false;
            }
            for (i = 0; i < this.countComponents(); ++i) {
                IModifiedSpellPart<SpellEffect> theirs;
                IModifiedSpellPart<SpellEffect> mine = this.getComponent(i);
                if (mine.isSame(theirs = other.getComponent(i))) continue;
                return false;
            }
        }
        if (checkModifiers) {
            if (this.countModifiers() != other.countModifiers()) {
                return false;
            }
            for (i = 0; i < this.countModifiers(); ++i) {
                if (this.getModifier(i) == other.getModifier(i)) continue;
                return false;
            }
        }
        return true;
    }
}

