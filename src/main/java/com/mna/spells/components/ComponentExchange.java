 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.tools.BlockUtils;
import com.mna.tools.EnchantmentUtils;
import com.mna.tools.InventoryUtilities;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class ComponentExchange
extends SpellEffect {
    public ComponentExchange(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 5.0f));
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (!source.isPlayerCaster() || target.isEntity()) {
            return ComponentApplicationResult.FAIL;
        }
        if (context.getServerLevel().isEmptyBlock(target.getBlock())) {
            return ComponentApplicationResult.FAIL;
        }
        int harvestLevel = (int)modificationData.getValue(Attribute.MAGNITUDE);
        Tier harvestTier = BlockUtils.tierFromHarvestLevel(harvestLevel - 1);
        if (!BlockUtils.canDestroyBlock(source.getCaster(), (Level)context.getServerLevel(), target.getBlock(), harvestTier)) {
            return ComponentApplicationResult.FAIL;
        }
        CompoundTag nbt = context.getMeta();
        Block searchBlock = null;
        Item searchItem = null;
        if (!nbt.contains("blockType")) {
            ItemStack offhandStack = source.getPlayer().getItemInHand(source.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!(offhandStack.getItem() instanceof BlockItem)) {
                return ComponentApplicationResult.FAIL;
            }
            if (!(offhandStack.getItem() instanceof BlockItem)) {
                return ComponentApplicationResult.FAIL;
            }
            searchBlock = ((BlockItem)offhandStack.getItem()).getBlock();
            searchItem = offhandStack.getItem();
            nbt.putString("blockType", BuiltInRegistries.ITEM.getKey(offhandStack.getItem()).toString());
        } else {
            ResourceLocation rLoc = ResourceLocation.parse(nbt.getString("blockType"));
            searchItem = (Item)BuiltInRegistries.ITEM.get(rLoc);
            if (searchItem != null && searchItem instanceof BlockItem) {
                searchBlock = ((BlockItem)searchItem).getBlock();
            }
        }
        if (searchBlock == null || searchItem == null) {
            return ComponentApplicationResult.FAIL;
        }
        if (!searchBlock.defaultBlockState().canSurvive((LevelReader)context.getServerLevel(), target.getBlock())) {
            return ComponentApplicationResult.FAIL;
        }
        if (!source.getPlayer().isCreative() && InventoryUtilities.removeSingleItemFromInventory(BuiltInRegistries.ITEM.getKey(searchItem), (Container)source.getPlayer().getInventory()).isEmpty()) {
            return ComponentApplicationResult.FAIL;
        }
        boolean silkTouch = EnchantmentUtils.getSilkTouch(source.getPlayer());
        int fortune_level = 0;
        if (!silkTouch) {
            fortune_level = EnchantmentUtils.getFortuneLevel(source.getPlayer());
        }
        BlockState sourceState = context.getServerLevel().getBlockState(target.getBlock());
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        Pair<Boolean, Boolean> captureRedirect = InventoryUtilities.getCaptureAndRedirect(source.getPlayer());
        drops.addAll(BlockUtils.destroyBlockCaptureDrops(source.getCaster(), (Level)context.getServerLevel(), target.getBlock(), silkTouch, fortune_level, harvestTier));
        if (source.isPlayerCaster()) {
            InventoryUtilities.redirectCaptureOrDrop(source.getPlayer(), (Level)context.getServerLevel(), drops, (Boolean)captureRedirect.getSecond());
        }
        BlockState destState = searchBlock.withPropertiesOf(sourceState);
        context.getServerLevel().setBlock(target.getBlock(), destState, 3);
        return ComponentApplicationResult.SUCCESS;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.EARTH;
    }

    @Override
    public float initialComplexity() {
        return 2.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 500;
    }

    @Override
    public boolean targetsEntities() {
        return false;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    public class PlaceBlockInfo {
        public final Block block;
        public final BlockPos position;
        public final Level world;

        public PlaceBlockInfo(Block block, BlockPos position, Level world) {
            this.block = block;
            this.position = position;
            this.world = world;
        }
    }
}


