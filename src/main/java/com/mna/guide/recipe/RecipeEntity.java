/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 *  org.joml.Quaternionf
 */
package com.mna.guide.recipe;

import com.mna.ManaAndArtifice;
import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Quaternionf;
import net.minecraft.core.registries.BuiltInRegistries;

public class RecipeEntity
extends RecipeRendererBase {
    private EntityType<?> _entity;
    private LivingEntity dummyEntity;

    public RecipeEntity(int xIn, int yIn) {
        super(xIn, yIn);
    }

    @Override
    protected void drawForeground(GuiGraphics pGuiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.dummyEntity == null) {
            return;
        }
        int scale = (int)(this.dummyEntity.getBoundingBox().getXsize() * 100.0);
        float bbHeight = Math.max((float)((int)this.dummyEntity.getBoundingBox().getYsize()), 0.15f);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float)Math.PI).rotateY((float)Math.toRadians(135.0));
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(0.0f, 0.0f, 100.0f);
        InventoryScreen.renderEntityInInventory((GuiGraphics)pGuiGraphics, (int)((int)((float)this.getX() / this.scale + (float)(this.width / 2) - 25.0f)), (int)((int)((float)this.getY() / this.scale + (float)(this.height / 2) + (float)this.height * bbHeight / 4.0f)), (int)scale, (Quaternionf)quaternionf, null, (LivingEntity)this.dummyEntity);
        pGuiGraphics.pose().popPose();
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return null;
    }

    @Override
    protected void init_internal(ResourceLocation recipeLocation) {
        this._entity = (EntityType)BuiltInRegistries.ENTITY_TYPE.get(recipeLocation);
        if (this._entity != null) {
            Entity e = this._entity.create(ManaAndArtifice.instance.proxy.getClientWorld());
            if (e instanceof LivingEntity) {
                this.dummyEntity = (LivingEntity)e;
            } else {
                ManaAndArtifice.LOGGER.warn("Passed in entity registration must be an instance of LivingEntity. " + recipeLocation.toString() + " is not!  This will not render in the codex.");
            }
        }
    }

    @Override
    public int getTier() {
        return 0;
    }
}


