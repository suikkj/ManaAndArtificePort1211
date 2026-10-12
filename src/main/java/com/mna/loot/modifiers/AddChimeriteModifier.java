 
package com.mna.loot.modifiers;

import com.google.common.base.Suppliers;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.items.ItemInit;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public class AddChimeriteModifier
extends LootModifier {
    public static final Supplier<Codec<AddChimeriteModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> AddChimeriteModifier.codecStart((RecordCodecBuilder.Instance)inst).and((App)Codec.INT.fieldOf("numChimerite").forGetter(m -> m.numChimerite)).and((App)Codec.INT.fieldOf("magicLevel").forGetter(m -> m.magicLevel)).and((App)Codec.FLOAT.fieldOf("chance").forGetter(m -> Float.valueOf(m.chance))).and((App)Codec.FLOAT.fieldOf("fortuneBonusChance").forGetter(m -> Float.valueOf(m.fortuneBonusChance))).and((App)BuiltInRegistries.ITEM.getCodec().fieldOf("dropsWith").forGetter(m -> m.dropsWith)).apply((Applicative)inst, AddChimeriteModifier::new)));
    private final Item dropsWith;
    private final int numChimerite;
    private final int magicLevel;
    private final float chance;
    private final float fortuneBonusChance;

    public AddChimeriteModifier(LootItemCondition[] conditions, int numChimerite, int magicLevel, float chance, float fortuneBonusChance, Item dropsWith) {
        super(conditions);
        this.numChimerite = numChimerite;
        this.magicLevel = magicLevel;
        this.chance = chance;
        this.fortuneBonusChance = fortuneBonusChance;
        this.dropsWith = dropsWith;
    }

    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!generatedLoot.stream().filter(i -> i.getItem() == this.dropsWith).findFirst().isPresent()) {
            return generatedLoot;
        }
        Entity e = (Entity)context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (e instanceof Player) {
            ItemStack toolStack = (ItemStack)context.getParamOrNull(LootContextParams.TOOL);
            int fortuneLevel = 0;
            if (toolStack != null && !toolStack.isEmpty()) {
                fortuneLevel = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, toolStack);
            }
            float bonus = (float)fortuneLevel * this.fortuneBonusChance;
            MNACap.get((Player)e, PlayerMagicProvider.MAGIC).ifPresent(magic -> {
                if (magic.getMagicLevel() >= this.magicLevel && Math.random() <= (double)(this.chance + bonus)) {
                    generatedLoot.add(new ItemStack((ItemLike)ItemInit.CHIMERITE_GEM.get(), (int)Math.ceil(Math.random() * (double)this.numChimerite * (double)(bonus + 1.0f))));
                }
            });
        }
        return generatedLoot;
    }

    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}


