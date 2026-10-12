 
package com.mna.items.sorcery;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.items.ItemInit;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class EntityCrystal
extends Item {
    public EntityCrystal() {
        super(new Item.Properties().stacksTo(1));
    }

    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!world.isClientSide) {
            Entity e = EntityCrystal.restoreEntity(world, context.getItemInHand());
            if (e != null) {
                context.getItemInHand().shrink(1);
                context.getPlayer().addItem(new ItemStack((ItemLike)Items.DIAMOND));
                Vec3 pos = new Vec3(context.getClickLocation().x, context.getClickLocation().y, context.getClickLocation().z);
                if (context.getClickedFace().getAxis() != Direction.Axis.Y) {
                    Vec3i normal = context.getClickedFace().getNormal();
                    pos = pos.add((double)normal.getX(), (double)normal.getY(), (double)normal.getZ());
                }
                e.setPos(pos);
                world.addFreshEntity(e);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt.contains("entityType")) {
            String entityID = nbt.getString("entityType");
            EntityType type = (EntityType)BuiltInRegistries.ENTITY_TYPE.getValue(ResourceLocation.parse(entityID));
            if (type != null) {
                Entity e = type.create(ManaAndArtifice.instance.proxy.getClientWorld());
                tooltip.add((Component)Component.translatable((String)"item.mna.entrapment_crystal.trapped", (Object[])new Object[]{e.getDisplayName().getString()}));
            } else {
                tooltip.add((Component)Component.translatable((String)"item.mna.entrapment_crystal.missing"));
            }
        } else {
            tooltip.add((Component)Component.translatable((String)"item.mna.entrapment_crystal.empty"));
        }
    }

    @Nullable
    public static EntityType<?> getEntityType(ItemStack stack) {
        CompoundTag entityNBT = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (entityNBT.contains("entityType")) {
            return EntityType.byString((String)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("entityType")).orElse(null);
        }
        return null;
    }

    public static ItemStack storeEntity(Entity e) {
        ItemStack stack = new ItemStack((ItemLike)ItemInit.ENTITY_ENTRAPMENT_CRYSTAL.get());
        CompoundTag nbt = new CompoundTag();
        e.saveWithoutId(nbt);
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().put("entityData", (Tag)nbt);
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString("entityType", EntityType.getKey(e.getType()).toString());
        return stack;
    }

    public static Entity restoreEntity(Level world, ItemStack stack) {
        EntityType<?> type = EntityCrystal.getEntityType(stack);
        if (type == null) {
            return null;
        }
        CompoundTag entityNBT = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        Entity e = type.create(world);
        e.load(entityNBT.getCompound("entityData"));
        return e;
    }

    public static void setNoDiamondBack(ItemStack stack) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("noDiamondBack", true);
    }

    public static boolean hasNoDiamondBack(ItemStack stack) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!nbt.contains("noDiamondBack")) {
            return false;
        }
        return nbt.getBoolean("noDiamondBack");
    }
}


