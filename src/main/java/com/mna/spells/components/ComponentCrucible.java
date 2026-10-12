/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.Container
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.item.crafting.SmeltingRecipe
 *  net.minecraft.world.level.Level
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.factions.Factions;
import com.mna.tools.BlockUtils;
import com.mna.tools.ContainerTools;
import com.mna.tools.EnchantmentUtils;
import com.mna.tools.InventoryUtilities;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

public class ComponentCrucible
extends SpellEffect {
    public ComponentCrucible(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 10.0f));
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (source.isPlayerCaster()) {
            ItemStack offhand;
            if (target.isBlock()) {
                int harvestLevel = (int)modificationData.getValue(Attribute.MAGNITUDE);
                Tier harvestTier = BlockUtils.tierFromHarvestLevel(harvestLevel - 1);
                if (BlockUtils.canDestroyBlock(source.getCaster(), context.getLevel(), target.getBlock(), harvestTier)) {
                    int fortune_level = EnchantmentUtils.getFortuneLevel(source.getPlayer());
                    Pair<Boolean, Boolean> captureRedirect = InventoryUtilities.getCaptureAndRedirect(source.getPlayer());
                    List<ItemStack> stacks = BlockUtils.destroyBlockCaptureDrops((LivingEntity)source.getPlayer(), context.getLevel(), target.getBlock(), false, fortune_level, harvestTier);
                    ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
                    for (ItemStack stack : stacks) {
                        drops.add(this.getFurnace(context.getLevel(), stack));
                    }
                    if (drops.size() > 0) {
                        InventoryUtilities.redirectCaptureOrDrop(source.getPlayer(), (Level)context.getServerLevel(), drops, (Boolean)captureRedirect.getSecond());
                        return ComponentApplicationResult.SUCCESS;
                    }
                }
            } else if (target.getLivingEntity() == source.getCaster() && !(offhand = source.getPlayer().getItemInHand(source.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)).isEmpty()) {
                ItemStack output = this.getFurnace(context.getLevel(), offhand);
                offhand.shrink(1);
                if (!source.getPlayer().addItem(output)) {
                    source.getPlayer().drop(output, true);
                }
                return ComponentApplicationResult.SUCCESS;
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    private ItemStack getFurnace(Level level, ItemStack input) {
        ItemStack output;
        CraftingContainer inv = ContainerTools.createTemporaryContainer(input);
        Optional furnace_recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.item.crafting.SingleRecipeInput(inv.getItem(0)), level);
        if (furnace_recipe.isPresent() && !(output = ((SmeltingRecipe)((net.minecraft.world.item.crafting.RecipeHolder)furnace_recipe.get()).value()).assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(inv.getItem(0)), level.registryAccess())).isEmpty()) {
            return output;
        }
        return input;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.FIRE;
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public IFaction getFactionRequirement() {
        return Factions.DEMONS;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}

