 
package com.mna.spells.shapes;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.sorcery.targeting.Smite;
import com.mna.factions.Factions;
import com.mna.items.sorcery.MagicStaff;
import com.mna.spells.shapes.ShapeRaytrace;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.mna.api.tools.MNACap;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public class ShapeSmite
extends ShapeRaytrace {
    public ShapeSmite(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RADIUS, 1.0f, 1.0f, 5.0f, 0.5f, 3.0f), new AttributeValuePair(Attribute.RANGE, 8.0f, 8.0f, 32.0f, 1.0f, 2.0f), new AttributeValuePair(Attribute.HEIGHT, 1.0f, 1.0f, 3.0f, 0.5f, 3.0f));
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        List<SpellTarget> tgts = super.Target(source, world, modificationData, recipe);
        SpellTarget tgt = tgts.get(0);
        if (tgt == SpellTarget.NONE) {
            return tgts;
        }
        return this.targetAfterRaytrace(source, world, modificationData, recipe, tgt);
    }

    @Override
    public List<SpellTarget> TargetNPCCast(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe, SpellTarget targetHint) {
        return this.targetAfterRaytrace(source, world, modificationData, recipe, targetHint);
    }

    private List<SpellTarget> targetAfterRaytrace(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe, SpellTarget hint) {
        if (!world.isClientSide) {
            ItemStack heldItem;
            java.util.Optional progression;
            Vec3 position = null;
            if (hint.isBlock()) {
                BlockPos blockPos = hint.getBlock().relative(hint.getBlockFace(null));
                position = new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
            } else if (hint.isEntity()) {
                position = hint.getEntity().position();
            }
            int count = 0;
            BlockPos targetPos = BlockPos.containing((Position)position);
            while (count < 15) {
                if (!world.isEmptyBlock(targetPos)) {
                    --count;
                    break;
                }
                ++count;
                targetPos = targetPos.above();
            }
            position = position.add(0.0, (double)count, 0.0);
            float bonusDamage = 0.0f;
            if (source.isPlayerCaster() && (progression = MNACap.get(source.getPlayer(), PlayerProgressionProvider.PROGRESSION)).isPresent() && ((IPlayerProgression)progression.get()).getAlliedFaction() == Factions.COUNCIL && (heldItem = source.getCaster().getItemInHand(source.getHand())).getItem() instanceof MagicStaff) {
                bonusDamage = (float)((byte)MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER, heldItem)) * 0.1f;
            }
            CompoundTag recipeData = new CompoundTag();
            recipe.writeToNBT(recipeData);
            Smite projectile = new Smite(world, position, recipeData, source.getCaster());
            projectile.setBonusDamagePctFromHeight(bonusDamage);
            world.addFreshEntity((Entity)projectile);
            return Arrays.asList(new SpellTarget((Entity)projectile));
        }
        return Arrays.asList(SpellTarget.NONE);
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
        return 500;
    }
}

