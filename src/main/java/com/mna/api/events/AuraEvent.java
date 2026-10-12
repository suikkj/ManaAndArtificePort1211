/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantment
 *  net.neoforged.bus.api.Cancelable
 *  net.neoforged.bus.api.Event
 *  net.neoforged.bus.api.Event$HasResult
 */
package com.mna.api.events;

import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.Event;

public class AuraEvent
extends Event {
    private final com.mna.enchantments.base.MAEnchantmentBase AuraEnchantment;

    public AuraEvent(com.mna.enchantments.base.MAEnchantmentBase auraEnchantment) {
        this.AuraEnchantment = auraEnchantment;
    }

    public com.mna.enchantments.base.MAEnchantmentBase getAura() {
        return this.AuraEnchantment;
    }
    public static class Tick
    extends AuraEvent {
        private final LivingEntity wearer;

        public Tick(com.mna.enchantments.base.MAEnchantmentBase aurEnchantment, LivingEntity wearer) {
            super(aurEnchantment);
            this.wearer = wearer;
        }

        public LivingEntity getWearer() {
            return this.wearer;
        }
    }

    /**
     * [PORT 1.21.1] {@code Event.HasResult} foi removido no NeoForge: o resultado
     * passa a ser proprio do evento (mapeado do enum antigo do Forge).
     */
    public static class Compatibility
    extends AuraEvent {
        public enum Result { ALLOW, DENY, DEFAULT }

        private final ItemStack placedOn;
        private final LivingEntity enchanter;
        private final Enchantment otherEnchantment;
        private Result result = Result.DEFAULT;

        public Compatibility(com.mna.enchantments.base.MAEnchantmentBase auraEnchantment, @Nullable Enchantment otherEnchantment, ItemStack placedOn, LivingEntity enchanter) {
            super(auraEnchantment);
            this.placedOn = placedOn.copy();
            this.enchanter = enchanter;
            this.otherEnchantment = otherEnchantment;
        }

        public Result getResult() {
            return this.result;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public ItemStack getPlacedOn() {
            return this.placedOn;
        }

        public LivingEntity getEnchanter() {
            return this.enchanter;
        }

        @Nullable
        public Enchantment getOtherEnchantment() {
            return this.otherEnchantment;
        }
    }

    public static class Numerics
    extends AuraEvent {
        private final boolean hasSelfishBelt;
        private final Player source;
        private final LivingEntity affecting;
        private final double radius;
        private float manaCost;
        private int magnitude;

        public Numerics(com.mna.enchantments.base.MAEnchantmentBase auraEnchantment, Player source, LivingEntity affecting, double radius, int defaultMagnitude, float defaultManaCost, boolean hasSelfishBelt) {
            super(auraEnchantment);
            this.source = source;
            this.affecting = affecting;
            this.hasSelfishBelt = hasSelfishBelt;
            this.radius = radius;
            this.magnitude = defaultMagnitude;
            this.manaCost = defaultManaCost;
        }

        public Player getSource() {
            return this.source;
        }

        public LivingEntity getAffecting() {
            return this.affecting;
        }

        public boolean hasSelfishBelt() {
            return this.hasSelfishBelt;
        }

        public double getRadius() {
            return this.radius;
        }

        public int getMagnitude() {
            return this.magnitude;
        }

        public float getManaCost() {
            return this.manaCost;
        }

        public void setManaCost(float value) {
            this.manaCost = value;
        }

        public void setMagnitude(int value) {
            this.magnitude = value;
        }
    }

    public static class Radius
    extends AuraEvent {
        private final Player source;
        private double radius;

        public Radius(com.mna.enchantments.base.MAEnchantmentBase auraEnchantment, Player source, double defaultRadius) {
            super(auraEnchantment);
            this.source = source;
            this.radius = defaultRadius;
        }

        public Player getSource() {
            return this.source;
        }

        public double getRadius() {
            return this.radius;
        }

        public void setRadius(double value) {
            this.radius = value;
        }
    }
}


