 
package com.mna.spells.shapes;

import com.mna.api.items.ChargeableItem;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.boss.Odin;
import com.mna.entities.sorcery.targeting.SpellProjectile;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import com.mna.api.tools.MNACap;
import com.mna.api.enchantments.MNAEnchantmentHelper;

public class ShapeProjectile
extends Shape {
    public ShapeProjectile(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair(Attribute.RADIUS, 0.0f, 0.0f, 3.0f, 1.0f), new AttributeValuePair(Attribute.SPEED, 1.0f, 1.0f, 3.0f, 0.2f), new AttributeValuePair(Attribute.PRECISION, 0.0f, 0.0f, 1.0f, 1.0f));
    }

    @Override
    public List<SpellTarget> Target(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe) {
        if (source == null) {
            return Arrays.asList(SpellTarget.NONE);
        }
        Optional curio = CuriosApi.getCuriosHelper().findFirstCurio(source.getCaster(), (Item)ItemInit.PROJECTILE_AMULET.get());
        if (source.isPlayerCaster() && curio.isPresent() && ((ChargeableItem)((SlotResult)curio.get()).stack().getItem()).consumeMana(((SlotResult)curio.get()).stack(), 1.0f, source.getPlayer())) {
            for (int i = -1; i <= 1; ++i) {
                this.SpawnProjectile(source, world, modificationData, recipe, i, null);
            }
        } else {
            this.SpawnProjectile(source, world, modificationData, recipe, 0.0f, null);
        }
        return Arrays.asList(new SpellTarget((Entity)source.getCaster()));
    }

    @Override
    public List<SpellTarget> TargetNPCCast(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe, SpellTarget targetHint) {
        if (source == null) {
            return Arrays.asList(SpellTarget.NONE);
        }
        this.SpawnProjectile(source, world, modificationData, recipe, 0.0f, targetHint);
        return Arrays.asList(new SpellTarget((Entity)source.getCaster()));
    }

    private void SpawnProjectile(SpellSource source, Level world, IModifiedSpellPart<Shape> modificationData, ISpellDefinition recipe, float offset, @Nullable SpellTarget targetHint) {
        ItemStack stack;
        SpellProjectile projectile = new SpellProjectile(source.getCaster(), world);
        if (offset != 0.0f) {
            Vec3 eyePos = source.getCaster().getEyePosition();
            Vec3 forward = source.getCaster().getLookAngle();
            Vec3 cross = forward.cross(new Vec3(0.0, 1.0, 0.0)).normalize().scale((double)offset);
            projectile.setPos(eyePos.add(cross));
        }
        if ((stack = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.AQUA_AFFINITY, source.getCaster().getItemInHand(source.getHand()))) > 0) {
            projectile.setNoWaterFizzle();
        }
        if (source.isPlayerCaster()) {
            MNACap.get(source.getPlayer(), PlayerProgressionProvider.PROGRESSION).ifPresent(m -> {
                if (m.getAlliedFaction() == Factions.COUNCIL) {
                    float homing = (float)MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER, stack) * 0.2f;
                    projectile.setHomingStrength(homing);
                }
            });
        }
        if (source.getCaster() instanceof Odin) {
            projectile.setHomingStrength(0.75f);
        }
        Vec3 direction = source.getForward();
        if (targetHint != null) {
            direction = targetHint.getPosition().subtract(source.getCaster().getEyePosition());
        }
        CompoundTag nbt = new CompoundTag();
        recipe.writeToNBT(nbt);
        projectile.setSpellRecipe(nbt);
        projectile.shoot((Entity)source.getCaster(), direction, modificationData.getValue(Attribute.SPEED), 0.0f);
        if (source.isPlayerCaster() && source.getPlayer().getItemInHand(source.getHand()).getItem() == ItemInit.PUNKIN_STAFF.get()) {
            projectile.setSpecialRender(SpellProjectile.SpecialRenderType.HALLOWEEN);
        }
        world.addFreshEntity((Entity)projectile);
    }

    @Override
    public boolean spawnsTargetEntity() {
        return true;
    }

    @Override
    public float initialComplexity() {
        return 10.0f;
    }

    @Override
    public int requiredXPForRote() {
        return 500;
    }
}

