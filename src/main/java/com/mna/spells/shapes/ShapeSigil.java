 
package com.mna.spells.shapes;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.spells.targeting.SpellTargetHelper;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.sorcery.targeting.SpellSigil;
import com.mna.factions.Factions;
import com.mna.items.sorcery.MagicStaff;
import com.mna.spells.shapes.ShapeRaytrace;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public class ShapeSigil
extends ShapeRaytrace {
    public ShapeSigil(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RADIUS, 1.0f, 1.0f, 3.0f, 1.0f, 5.0f), new AttributeValuePair(Attribute.WIDTH, 1.0f, 1.0f, 5.0f, 1.0f, 15.0f), new AttributeValuePair(Attribute.MAGNITUDE, 1.0f, 1.0f, 3.0f, 1.0f, 5.0f), new AttributeValuePair(Attribute.RANGE, 3.0f, 3.0f, 16.0f, 1.0f, 3.0f));
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        Vec3 sigilPos;
        BlockPos pos;
        ItemStack heldItem;
        java.util.Optional progression;
        SpellSigil sigil;
        EntityHitResult ehr;
        if (!source.isPlayerCaster()) {
            return Arrays.asList(SpellTarget.NONE);
        }
        float range = modificationData.getValue(Attribute.RANGE);
        HitResult targetResult = SpellTargetHelper.rayTrace((Entity)source.getCaster(), world, source.getOrigin(), source.getForward(), true, false, ClipContext.Block.OUTLINE, entity -> entity.isPickable() && entity.isAlive() && entity != source.getCaster(), source.getBoundingBox().inflate((double)range, (double)range, (double)range), range);
        HitResult.Type targetType = targetResult.getType();
        if (targetType == HitResult.Type.MISS) {
            return Arrays.asList(SpellTarget.NONE);
        }
        if (targetType == HitResult.Type.ENTITY && (ehr = (EntityHitResult)targetResult).getEntity() instanceof SpellSigil && (sigil = (SpellSigil)ehr.getEntity()).isPermanent()) {
            sigil.addCharge();
            return Arrays.asList(new SpellTarget((Entity)source.getPlayer()));
        }
        boolean permanent = false;
        byte bonus = 0;
        if (source.isPlayerCaster() && (progression = MNACap.get(source.getPlayer(), PlayerProgressionProvider.PROGRESSION)).isPresent() && ((IPlayerProgression)progression.get()).getAlliedFaction() == Factions.COUNCIL && (heldItem = source.getCaster().getItemInHand(source.getHand())).getItem() instanceof MagicStaff) {
            bonus = (byte)MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER, heldItem);
        }
        if (world.getEntitiesOfClass(Entity.class, new AABB(pos = BlockPos.containing((Position)(sigilPos = targetResult.getLocation())))).size() != 0) {
            return Arrays.asList(SpellTarget.NONE);
        }
        if (targetType == HitResult.Type.BLOCK) {
            BlockHitResult brtr = (BlockHitResult)targetResult;
            BlockState state = world.getBlockState(brtr.getBlockPos());
            if (state.getBlock() == BlockInit.CHALK_RUNE.get() && ((Boolean)state.getValue((Property)ChalkRuneBlock.METAL)).booleanValue()) {
                permanent = true;
                world.setBlock(brtr.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
            } else if (brtr.getDirection() != Direction.UP) {
                sigilPos.add((double)brtr.getDirection().getStepX(), (double)brtr.getDirection().getStepY(), (double)brtr.getDirection().getStepZ());
            }
        }
        if (!world.isClientSide) {
            SpellSigil sigil2 = new SpellSigil(source.getPlayer(), recipe);
            sigil2.setPos((float)pos.getX() + 0.5f, sigilPos.y() + 0.01, (float)pos.getZ() + 0.5f);
            if (permanent) {
                sigil2.setPermanent();
            }
            sigil2.setSize((byte)modificationData.getValue(Attribute.WIDTH));
            sigil2.setCastWithBonus(bonus);
            world.addFreshEntity((Entity)sigil2);
        }
        return Arrays.asList(new SpellTarget((Entity)source.getPlayer()));
    }

    @Override
    public boolean spawnsTargetEntity() {
        return true;
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 250;
    }
}

