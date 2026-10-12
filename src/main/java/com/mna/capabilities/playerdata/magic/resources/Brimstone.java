/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.world.Difficulty
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.biome.Biome
 */
package com.mna.capabilities.playerdata.magic.resources;

import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.capabilities.resource.SimpleCastingResource;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.entities.DamageHelper;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public class Brimstone
extends SimpleCastingResource {
    public Brimstone() {
        super(GeneralConfigValues.TotalManaRegenTicks);
    }

    @Override
    public float getRegenerationModifier(LivingEntity caster) {
        float baseline = super.getRegenerationModifier(caster) / 2.0f;
        Holder biome = caster.level().getBiome(caster.blockPosition());
        if (((Biome)biome.value()).coldEnoughToSnow(caster.blockPosition())) {
            baseline *= 2.0f;
        } else if (biome.is(BiomeTags.SNOW_GOLEM_MELTS)) {
            baseline *= 0.5f;
        }
        return baseline;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return CastingResourceIDs.BRIMSTONE;
    }

    @Override
    public void setMaxAmountByLevel(int level) {
        this.setMaxAmount(50 + 10 * level);
    }

    @Override
    public boolean hungerAffectsRegenRate() {
        return false;
    }

    @Override
    public void consume(LivingEntity caster, float amount) {
        if (this.getAmount() < amount) {
            ItemStack mainHand = caster.getMainHandItem();
            ItemStack offHand = caster.getOffhandItem();
            float brimstonePerHeart = GeneralConfigValues.BrimstonePerHeart;
            if (MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAME, mainHand) > 0 || MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAME, offHand) > 0) {
                brimstonePerHeart *= 2.0f;
            }
            int heartsRequired = (int)Math.ceil((amount - this.getAmount()) / brimstonePerHeart);
            caster.hurt(DamageHelper.forType(DamageHelper.CONFLAGRATE, caster.level().registryAccess()), (float)heartsRequired);
        }
        super.consume(caster, amount);
    }

    @Override
    public boolean hasEnough(LivingEntity caster, float amount) {
        float hearts_add;
        if (caster.level().getDifficulty() == Difficulty.HARD) {
            return true;
        }
        float base = this.getAmount();
        return amount <= base + (hearts_add = (caster.getHealth() - 1.0f) * (float)GeneralConfigValues.BrimstonePerHeart);
    }
}

