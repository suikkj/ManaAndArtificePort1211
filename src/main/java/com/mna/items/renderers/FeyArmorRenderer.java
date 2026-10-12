/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  software.bernie.geckolib.model.GeoModel
 *  software.bernie.geckolib.renderer.GeoArmorRenderer
 */
package com.mna.items.renderers;

import com.mna.items.armor.FeyArmorItem;
import com.mna.items.armor.models.FeyArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class FeyArmorRenderer
extends GeoArmorRenderer<FeyArmorItem> {
    public FeyArmorRenderer() {
        super((GeoModel)new FeyArmorModel());
    }

    public void prepForRender(Entity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel) {
        if (entity instanceof LivingEntity) {
            FeyArmorItem.renderEntity = (LivingEntity)entity;
        }
        super.prepForRender(entity, stack, slot, baseModel);
    }
}

