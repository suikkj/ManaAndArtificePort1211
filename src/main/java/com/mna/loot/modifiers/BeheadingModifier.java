 
package com.mna.loot.modifiers;

import com.google.common.base.Suppliers;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class BeheadingModifier
extends LootModifier {
    public static final Supplier<com.mojang.serialization.MapCodec<BeheadingModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> BeheadingModifier.codecStart((RecordCodecBuilder.Instance)inst).and(Codec.FLOAT.fieldOf("chancePerLevel").forGetter(m -> Float.valueOf(m.chancePerLevel))).and(BuiltInRegistries.ENTITY_TYPE.getCodec().fieldOf("mobType").forGetter(m -> m.mobType)).and(BuiltInRegistries.ITEM.getCodec().fieldOf("head").forGetter(m -> m.head)).apply(inst, BeheadingModifier::new)));
    private final float chancePerLevel;
    private final Item head;
    private final EntityType<?> mobType;

    public BeheadingModifier(LootItemCondition[] conditions, float chancePerLevel, EntityType<?> mobType, Item head) {
        super(conditions);
        this.chancePerLevel = chancePerLevel;
        this.head = head;
        this.mobType = mobType;
    }

    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LivingEntity killerLiving;
        int level;
        Entity dead = (Entity)context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Entity killer = (Entity)context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (dead != null && killer != null && killer instanceof LivingEntity && dead.getType().equals(this.mobType) && (level = Math.max(com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.BEHEADING, (killerLiving = (LivingEntity)killer).getMainHandItem()), com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(EnchantmentInit.BEHEADING, killerLiving.getOffhandItem()))) > 0 && Math.random() < (double)((float)level * this.chancePerLevel) && generatedLoot.stream().noneMatch(i -> i.getItem() == this.head)) {
            generatedLoot.add(new ItemStack((ItemLike)this.head));
        }
        return generatedLoot;
    }

    public com.mojang.serialization.MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}


