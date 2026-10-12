 
package com.mna.items.artifice;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.items.TieredItem;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.entities.constructs.BubbleBoat;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BubbleBoatItem
extends TieredItem {
    public static final int UPGRADE_NONE = 0;
    public static final int UPGRADE_LAVA = 1;
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private int upgrade;

    public BubbleBoatItem(int upgrade) {
        super(new Item.Properties().stacksTo(1));
        this.upgrade = upgrade;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public boolean isFireResistant() {
        return this.upgrade == 1;
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return stack.getItem() == this && armorType == EquipmentSlot.HEAD;
    }

    public void onArmorTick(ItemStack stack, Level world, Player player) {
        if (player.isInWater() && player.getEffect(MobEffects.WATER_BREATHING) == null) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0, false, false));
        }
        if (this.upgrade == 1 && (player.isInLava() || player.isOnFire()) && player.getEffect(MobEffects.FIRE_RESISTANCE) == null) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, false, false));
        }
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        IPlayerMagic magic = (IPlayerMagic)playerIn.getData(PlayerMagicProvider.MAGIC.get());
        if (!magic.isMagicUnlocked()) {
            return InteractionResultHolder.fail(itemstack);
        }
        BlockHitResult raytraceresult = BubbleBoatItem.getPlayerPOVHitResult((Level)worldIn, (Player)playerIn, (ClipContext.Fluid)ClipContext.Fluid.ANY);
        if (raytraceresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        }
        Vec3 vec3d = playerIn.getViewVector(1.0f);
        List list = worldIn.getEntities((Entity)playerIn, playerIn.getBoundingBox().expandTowards(vec3d.scale(5.0)).inflate(1.0), ENTITY_PREDICATE);
        if (!list.isEmpty()) {
            Vec3 vec3d1 = playerIn.getEyePosition(1.0f);
            for (Entity entity : list) {
                AABB axisalignedbb = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                if (!axisalignedbb.contains(vec3d1)) continue;
                return InteractionResultHolder.pass(itemstack);
            }
        }
        if (raytraceresult.getType() == HitResult.Type.BLOCK) {
            BubbleBoat boatentity = new BubbleBoat(worldIn, raytraceresult.getLocation().x, raytraceresult.getLocation().y + 0.5, raytraceresult.getLocation().z);
            if (this.upgrade == 1) {
                boatentity = boatentity.setBrimstone();
            }
            boatentity.setYRot(playerIn.getYRot());
            if (!worldIn.noCollision((Entity)boatentity, boatentity.getBoundingBox().inflate(-0.1))) {
                return InteractionResultHolder.fail(itemstack);
            }
            if (!worldIn.isClientSide) {
                worldIn.addFreshEntity((Entity)boatentity);
                if (!playerIn.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
            }
            playerIn.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResultHolder.success(itemstack);
        }
        return InteractionResultHolder.pass(itemstack);
    }
}

