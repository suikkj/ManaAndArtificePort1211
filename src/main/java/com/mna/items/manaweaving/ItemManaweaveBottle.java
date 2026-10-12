/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.client.extensions.common.IClientItemExtensions
 *  net.neoforged.neoforge.common.util.NonNullLazy
 */
package com.mna.items.manaweaving;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.items.TieredItem;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.entities.manaweaving.ThrownManaweaveBottle;
import com.mna.items.renderers.ManaweaveBottleRenderer;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternSerializer;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;


public class ItemManaweaveBottle
extends TieredItem {
    private static final String KEY_PATTERN_ID = "pattern";

    public ItemManaweaveBottle() {
        super(new Item.Properties());
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new ManaweaveBottleRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public static boolean hasPattern(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_PATTERN_ID);
    }

    @Nullable
    public static ManaweavingPattern getPattern(ItemStack stack) {
        if (!ItemManaweaveBottle.hasPattern(stack)) {
            return null;
        }
        String patternID = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(KEY_PATTERN_ID);
        Optional<ManaweavingPattern> pattern = ManaweavingPatternSerializer.ALL_RECIPES.values().stream().filter(p -> p.getId().toString().equals(patternID)).findFirst();
        return pattern.orElse(null);
    }

    public static void setPattern(IManaweavePattern pattern, ItemStack stack) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(KEY_PATTERN_ID, pattern.getRegistryId().toString());
    }

    public Component getName(ItemStack stack) {
        MutableComponent baseline = Component.translatable((String)this.getDescriptionId(stack));
        if (ItemManaweaveBottle.hasPattern(stack)) {
            String patternName = Component.translatable((String)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(KEY_PATTERN_ID)).getString();
            baseline.append((Component)Component.translatable((String)"item.mna.manaweave_bottle.suffix", (Object[])new Object[]{patternName}));
        }
        return baseline;
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide) {
            ThrownManaweaveBottle potionentity = new ThrownManaweaveBottle(worldIn, (LivingEntity)playerIn);
            potionentity.setItem(itemstack);
            potionentity.shootFromRotation((Entity)playerIn, playerIn.getXRot(), playerIn.getYRot(), -20.0f, 0.5f, 1.0f);
            worldIn.addFreshEntity((Entity)potionentity);
        }
        if (!playerIn.getAbilities().instabuild) {
            itemstack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(itemstack, (boolean)worldIn.isClientSide());
    }

    public static void setRandomPattern(ItemStack is) {
        List patterns = ManaweavingPatternSerializer.ALL_RECIPES.values().stream().collect(Collectors.toList());
        if (patterns.size() == 0) {
            return;
        }
        ItemManaweaveBottle.setPattern((IManaweavePattern)patterns.get((int)((double)patterns.size() * Math.random())), is);
    }
}


