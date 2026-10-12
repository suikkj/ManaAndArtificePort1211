/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.Mth
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.storage.loot.LootContext
 *  net.minecraft.world.level.storage.loot.LootContext$Builder
 *  net.minecraft.world.level.storage.loot.LootParams$Builder
 *  net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
 *  net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction
 *  net.minecraft.world.level.storage.loot.functions.LootItemFunction
 *  net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction
 *  net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
 *  net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction
 *  net.minecraft.world.level.storage.loot.functions.SmeltItemFunction
 *  net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
 *  net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator
 *  net.minecraft.world.level.storage.loot.providers.number.ConstantValue
 *  net.minecraft.world.level.storage.loot.providers.number.NumberProvider
 *  net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
 */
package com.mna.tools.loot;

import com.mna.tools.loot.Conditional;
import com.mna.tools.loot.ICustomLootFunction;
import com.mna.tools.loot.LootDrop;
import java.util.stream.IntStream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class LootFunctionHelper {
    private static final int STATISTICAL_TEST = 100;

    public static void applyFunction(ServerLevel serverLevel, LootItemFunction lootFunction, LootDrop lootDrop) {
        LootContext randContext = new LootContext.Builder(new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY)).create(null);
        if (lootFunction instanceof SetItemCountFunction) {
            lootDrop.minDrop = LootFunctionHelper.getMin(serverLevel, valueOf((SetItemCountFunction)lootFunction));
            if (lootDrop.minDrop < 0) {
                lootDrop.minDrop = 0;
            }
            lootDrop.item.setCount(Math.max(lootDrop.minDrop, 1));
            lootDrop.maxDrop = LootFunctionHelper.getMax(serverLevel, valueOf((SetItemCountFunction)lootFunction));
        } else if (lootFunction instanceof SetItemDamageFunction) {
            ((SetItemDamageFunction)lootFunction).run(lootDrop.item, randContext);
        } else if (lootFunction instanceof EnchantRandomlyFunction || lootFunction instanceof EnchantWithLevelsFunction) {
            lootDrop.enchanted = true;
        } else if (!(lootFunction instanceof SmeltItemFunction)) {
            if (lootFunction instanceof EnchantedCountIncreaseFunction) {
                lootDrop.addConditional(Conditional.affectedByLooting);
            } else if (lootFunction instanceof ICustomLootFunction) {
                ((ICustomLootFunction)lootFunction).apply(lootDrop);
            } else {
                try {
                    lootDrop.item = (ItemStack)lootFunction.apply(lootDrop.item, null);
                }
                catch (NullPointerException nullPointerException) {
                    // empty catch block
                }
            }
        }
    }

    public static int getMin(ServerLevel serverLevel, NumberProvider randomRange) {
        LootContext randContext = new LootContext.Builder(new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY)).create(null);
        if (randomRange instanceof ConstantValue) {
            return randomRange.getInt(randContext);
        }
        if (randomRange instanceof UniformGenerator) {
            return Mth.floor((float)((UniformGenerator)randomRange).min().getInt(randContext));
        }
        if (randomRange instanceof BinomialDistributionGenerator) {
            return 0;
        }
        return IntStream.iterate(0, i -> randomRange.getInt(randContext)).limit(100L).min().orElse(0);
    }

    public static int getMax(ServerLevel serverLevel, NumberProvider randomRange) {
        LootContext randContext = new LootContext.Builder(new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY)).create(null);
        if (randomRange instanceof ConstantValue) {
            return randomRange.getInt(randContext);
        }
        if (randomRange instanceof UniformGenerator) {
            return Mth.floor((float)((UniformGenerator)randomRange).max().getInt(randContext));
        }
        if (randomRange instanceof BinomialDistributionGenerator) {
            return com.mna.tools.MNAReflection.binomialN((BinomialDistributionGenerator)randomRange);
        }
        return IntStream.iterate(0, i -> randomRange.getInt(randContext)).limit(100L).max().orElse(0);
    }

    /** [PORT 1.21.1] SetItemCountFunction.value virou private. */
    private static NumberProvider valueOf(SetItemCountFunction f) {
        try {
            java.lang.reflect.Field fld = SetItemCountFunction.class.getDeclaredField("value");
            fld.setAccessible(true);
            return (NumberProvider)fld.get(f);
        } catch (Throwable t) {
            return null;
        }
    }
}

