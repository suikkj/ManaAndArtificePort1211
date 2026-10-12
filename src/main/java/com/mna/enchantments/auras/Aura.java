 
package com.mna.enchantments.auras;

import com.mna.api.events.AuraEvent;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.enchantments.base.MAEnchantmentBase;
import com.mna.items.ItemInit;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.registries.DeferredHolder;
import top.theillusivec4.curios.api.CuriosApi;
import com.mna.api.tools.MNACap;
import com.mna.api.events.AuraEvent.Compatibility.Result;
import net.minecraft.world.item.enchantment.Enchantment;
import com.mna.api.enchantments.MAEnchantmentBridge;
import com.mna.enchantments.framework.EnchantmentBehaviors;

public class Aura
extends MAEnchantmentBase {
    private int _minLevel = 1;
    private int _maxLevel = 1;
    private int _duration = 100;
    private int _magnitude = 0;
    private int _radius = 25;
    private float _manaCost;
    private net.minecraft.core.Holder<MobEffect> _effectHolder;
    private Predicate<Player> _applicationPredicate;

    public Aura(MAEnchantmentBridge.Rarity rarityIn) {
    }

    public boolean canEnchant(ItemStack stack) {
        boolean result = false;
        Item item = stack.getItem();
        if (item instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem)item;
            EquipmentSlot slot = armor.getEquipmentSlot();
            result = slot == EquipmentSlot.CHEST;
        }
        return result;
    }

    public boolean canEnchant(ItemStack stack, Player enchanter) {
        boolean canEnchant = this.canEnchant(stack);
        AuraEvent.Compatibility event = new AuraEvent.Compatibility(this, null, stack, (LivingEntity)enchanter);
        event.setResult(canEnchant ? Result.ALLOW : Result.DENY);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult() == Result.ALLOW;
    }

    protected boolean checkCompatibility(Enchantment ench) {
        return !(EnchantmentBehaviors.get(ench) instanceof Aura);
    }

    public boolean isCompatibleWith(Enchantment other) {
        return this.checkCompatibility(other);
    }

    public boolean isCompatibleWith(Enchantment other, Player enchanter) {
        boolean isCompatible = this.isCompatibleWith(other);
        AuraEvent.Compatibility event = new AuraEvent.Compatibility(this, other, ItemStack.EMPTY, (LivingEntity)enchanter);
        event.setResult(isCompatible ? Result.ALLOW : Result.DENY);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult() == Result.ALLOW;
    }

    public int getMinLevel() {
        return this._minLevel;
    }

    public int getMaxLevel() {
        return this._maxLevel;
    }

    public Aura withDuration(int duration) {
        this._duration = duration;
        return this;
    }

    public Aura withMagnitude(int magnitude) {
        this._magnitude = magnitude;
        return this;
    }

    public Aura withRadius(int radius) {
        this._radius = radius;
        return this;
    }

    public Aura withManaCost(float manaCost) {
        this._manaCost = manaCost;
        return this;
    }

    public Aura withEffect(net.minecraft.core.Holder<MobEffect> effect) {
        this._effectHolder = effect;
        return this;
    }

    public Aura withPredicate(Predicate<Player> predicate) {
        this._applicationPredicate = predicate;
        return this;
    }

    public Aura withMinLevel(int minLevel) {
        this._minLevel = minLevel;
        return this;
    }

    public Aura withMaxLevel(int maxLevel) {
        this._maxLevel = maxLevel;
        return this;
    }

    private net.minecraft.core.Holder<MobEffect> getEffect() {
        return this._effectHolder;
    }

    public void apply(Player source, int enchantmentLevel, boolean manaOnly) {
        if (this._applicationPredicate != null && !this._applicationPredicate.test(source)) {
            return;
        }
        if (NeoForge.EVENT_BUS.post(new AuraEvent.Tick(this, (LivingEntity)source))) {
            return;
        }
        MNACap.get(source, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            boolean hasBelt = CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)source, (Item)ItemInit.BELT_SELFISHNESS.get()).isPresent();
            List<Player> players = null;
            double radius = this._radius;
            if (hasBelt) {
                players = Arrays.asList(source);
            } else {
                AuraEvent.Radius radiusEvent = new AuraEvent.Radius(this, source, this._radius);
                NeoForge.EVENT_BUS.post(radiusEvent);
                radius = radiusEvent.getRadius();
                players = source.level().getEntitiesOfClass(Player.class, source.getBoundingBox().inflate(radius));
            }
            int magnitude = enchantmentLevel - 1 + this._magnitude;
            for (Player player : players) {
                float adjustedManaCost = this._manaCost * (float)(hasBelt ? 2 : 5);
                AuraEvent.Numerics numericsEvent = new AuraEvent.Numerics(this, source, (LivingEntity)player, radius, magnitude, adjustedManaCost, hasBelt);
                NeoForge.EVENT_BUS.post(numericsEvent);
                if (!m.getCastingResource().hasEnoughAbsolute((LivingEntity)player, numericsEvent.getManaCost())) break;
                if (source.getTeam() != null && !player.isAlliedTo((Entity)source)) continue;
                if (!manaOnly) {
                    MobEffectInstance newInst = new MobEffectInstance(this.getEffect(), this._duration, numericsEvent.getMagnitude(), true, false);
                    player.addEffect(newInst);
                }
                m.getCastingResource().consume((LivingEntity)source, numericsEvent.getManaCost());
            }
        });
    }
}


