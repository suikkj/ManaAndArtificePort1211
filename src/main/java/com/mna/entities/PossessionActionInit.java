 
package com.mna.entities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.entities.possession.PossessionActions;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.items.ItemInit;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraft.world.item.alchemy.PotionContents;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.MOD)
public class PossessionActionInit {
    @SubscribeEvent
    public static void loadCompleteEventHandler(FMLLoadCompleteEvent event) {
        PossessionActions.RegisterLeftClickAction(EntityType.CREEPER, c -> c.ignite());
        PossessionActions.RegisterLeftClickAction(EntityType.ENDERMAN, e -> {
            SpellRecipe recipe = new SpellRecipe();
            recipe.setShape(Shapes.SELF);
            recipe.addComponent(Components.BLINK);
            ((ModifiedSpellPart)recipe.getComponent(0)).setValue(Attribute.RANGE, 16.0f);
            ItemStack spell = new ItemStack((ItemLike)ItemInit.SPELL.get());
            recipe.writeToNBT(spell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            SpellCaster.Affect(spell, recipe, e.level(), new SpellSource((LivingEntity)e, InteractionHand.MAIN_HAND));
        });
        PossessionActions.RegisterLeftClickAction(EntityType.GHAST, e -> {
            Vec3 vector3d = e.getViewVector(1.0f);
            e.level().levelEvent((Player)null, 1016, e.blockPosition(), 0);
            LargeFireball fireballentity = new LargeFireball(e.level(), (LivingEntity)e, vector3d.x, vector3d.y, vector3d.z, e.getExplosionPower());
            fireballentity.setPos(e.getX() + vector3d.x * 4.0, e.getY(0.5) + 0.5, fireballentity.getZ() + vector3d.z * 4.0);
            e.level().addFreshEntity((Entity)fireballentity);
        });
        PossessionActions.RegisterLeftClickAction(EntityType.WITCH, e -> {
            Vec3 vector3d = e.getViewVector(1.0f);
            ThrownPotion potionentity = new ThrownPotion(e.level(), (LivingEntity)e);
            potionentity.setItem(com.mna.api.tools.MNAItemData.setPotionContents(new ItemStack((ItemLike)Items.SPLASH_POTION), new PotionContents(Potions.POISON)));
            potionentity.setXRot(potionentity.getXRot() - 20.0f);
            potionentity.shoot(vector3d.x, vector3d.y, vector3d.z, 0.75f, 8.0f);
            e.level().playSound((Player)null, e.getX(), e.getY(), e.getZ(), SoundEvents.WITCH_THROW, e.getSoundSource(), 1.0f, 0.8f + e.level().getRandom().nextFloat() * 0.4f);
            e.level().addFreshEntity((Entity)potionentity);
        });
        PossessionActions.RegisterLeftClickAction(EntityType.SKELETON, e -> {
            ItemStack itemstack = e.getProjectile(e.getItemInHand(ProjectileUtil.getWeaponHoldingHand((LivingEntity)e, item -> item == Items.BOW)));
            AbstractArrow abstractarrowentity = ProjectileUtil.getMobArrow((LivingEntity)e, (ItemStack)itemstack, (float)BowItem.getPowerForTime((int)40));
            if (e.getMainHandItem().getItem() instanceof BowItem) {
                abstractarrowentity = ((BowItem)e.getMainHandItem().getItem()).customArrow(abstractarrowentity);
            }
            Vec3 fwd = Vec3.directionFromRotation((Vec2)e.getRotationVector()).normalize();
            abstractarrowentity.shoot(fwd.x, fwd.y, fwd.z, 1.6f, (float)(14 - e.level().getDifficulty().getId() * 4));
            e.playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (e.getRandom().nextFloat() * 0.4f + 0.8f));
            e.level().addFreshEntity((Entity)abstractarrowentity);
        });
        PossessionActions.RegisterLeftClickAction(EntityType.BEE, e -> {
            ClipContext ctx = new ClipContext(e.position(), e.position().add(e.getForward().scale(4.0)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)e);
            BlockHitResult bhr = e.level().clip(ctx);
            if (bhr.getType() == HitResult.Type.MISS) {
                PossessionActions.InvokeDefault(e);
            } else {
                BlockState clickState = e.level().getBlockState(bhr.getBlockPos());
                if (clickState.is(BlockTags.FLOWERS)) {
                    e.setHasNectar(true);
                }
            }
        });
        PossessionActions.RegisterLeftClickAction(EntityType.ILLUSIONER, e -> {
            SpellRecipe recipe = new SpellRecipe();
            recipe.setShape(Shapes.BOLT);
            recipe.addComponent(Components.BLIND);
            recipe.addComponent(Components.MAGIC_DAMAGE);
            ((ModifiedSpellPart)recipe.getShape()).setValue(Attribute.RANGE, 16.0f);
            ItemStack spell = new ItemStack((ItemLike)ItemInit.SPELL.get());
            recipe.writeToNBT(spell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
            SpellCaster.Affect(spell, recipe, e.level(), new SpellSource((LivingEntity)e, InteractionHand.MAIN_HAND));
        });
        PossessionActions.RegisterLeftClickAction(EntityType.EVOKER, e -> {
            BlockPos blockpos = e.blockPosition();
            boolean flag = false;
            double d0 = 0.0;
            Vec3 forward = e.getForward().normalize();
            Vec3 summonPos = e.position().add(forward.x, 0.0, forward.z);
            do {
                BlockState blockstate1;
                VoxelShape voxelshape;
                BlockPos blockpos1 = blockpos.below();
                BlockState blockstate = e.level().getBlockState(blockpos1);
                if (!blockstate.isFaceSturdy((BlockGetter)e.level(), blockpos1, Direction.UP)) continue;
                if (!e.level().isEmptyBlock(blockpos) && !(voxelshape = (blockstate1 = e.level().getBlockState(blockpos)).getCollisionShape((BlockGetter)e.level(), blockpos)).isEmpty()) {
                    d0 = voxelshape.max(Direction.Axis.Y);
                }
                flag = true;
                break;
            } while ((blockpos = blockpos.below()).getY() >= Mth.floor((double)summonPos.z) - 1);
            if (flag) {
                for (int i = 0; i < 8; ++i) {
                    Vec3 scaledFwd = forward.scale((double)((float)i * 1.25f));
                    Vec3 pos = summonPos.add(scaledFwd.x, 0.0, scaledFwd.z);
                    e.level().addFreshEntity((Entity)new EvokerFangs(e.level(), pos.x, (double)blockpos.getY() + d0, pos.z, e.getYRot(), i * 2, (LivingEntity)e));
                }
            }
        });
    }
}


