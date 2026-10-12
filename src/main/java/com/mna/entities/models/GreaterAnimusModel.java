/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.AnimationUtils
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.monster.AbstractIllager$IllagerArmPose
 */
package com.mna.entities.models;

import com.mna.entities.summon.GreaterAnimus;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;

public class GreaterAnimusModel
extends HumanoidModel<GreaterAnimus> {
    public GreaterAnimusModel(ModelPart pRoot) {
        super(pRoot);
    }

    public void setupAnim(GreaterAnimus pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180);
        this.head.xRot = pHeadPitch * ((float)Math.PI / 180);
        if (this.riding) {
            this.rightArm.xRot = -0.62831855f;
            this.rightArm.yRot = 0.0f;
            this.rightArm.zRot = 0.0f;
            this.leftArm.xRot = -0.62831855f;
            this.leftArm.yRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.rightLeg.xRot = -1.4137167f;
            this.rightLeg.yRot = 0.31415927f;
            this.rightLeg.zRot = 0.07853982f;
            this.leftLeg.xRot = -1.4137167f;
            this.leftLeg.yRot = -0.31415927f;
            this.leftLeg.zRot = -0.07853982f;
        } else {
            this.rightArm.xRot = Mth.cos((float)(pLimbSwing * 0.6662f + (float)Math.PI)) * 2.0f * pLimbSwingAmount * 0.5f;
            this.rightArm.yRot = 0.0f;
            this.rightArm.zRot = 0.0f;
            this.leftArm.xRot = Mth.cos((float)(pLimbSwing * 0.6662f)) * 2.0f * pLimbSwingAmount * 0.5f;
            this.leftArm.yRot = 0.0f;
            this.leftArm.zRot = 0.0f;
            this.rightLeg.xRot = Mth.cos((float)(pLimbSwing * 0.6662f)) * 1.4f * pLimbSwingAmount * 0.5f;
            this.rightLeg.yRot = 0.0f;
            this.rightLeg.zRot = 0.0f;
            this.leftLeg.xRot = Mth.cos((float)(pLimbSwing * 0.6662f + (float)Math.PI)) * 1.4f * pLimbSwingAmount * 0.5f;
            this.leftLeg.yRot = 0.0f;
            this.leftLeg.zRot = 0.0f;
        }
        AbstractIllager.IllagerArmPose abstractillager$illagerarmpose = pEntity.getArmPose();
        if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.ATTACKING) {
            if (pEntity.getMainHandItem().isEmpty()) {
                AnimationUtils.animateZombieArms((ModelPart)this.leftArm, (ModelPart)this.rightArm, (boolean)true, (float)this.attackTime, (float)pAgeInTicks);
            } else {
                AnimationUtils.swingWeaponDown((ModelPart)this.rightArm, (ModelPart)this.leftArm, (Mob)pEntity, (float)this.attackTime, (float)pAgeInTicks);
            }
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.SPELLCASTING) {
            this.rightArm.z = 0.0f;
            this.rightArm.x = -5.0f;
            this.leftArm.z = 0.0f;
            this.leftArm.x = 5.0f;
            this.rightArm.xRot = Mth.cos((float)(pAgeInTicks * 0.6662f)) * 0.25f;
            this.leftArm.xRot = Mth.cos((float)(pAgeInTicks * 0.6662f)) * 0.25f;
            this.rightArm.zRot = 2.3561945f;
            this.leftArm.zRot = -2.3561945f;
            this.rightArm.yRot = 0.0f;
            this.leftArm.yRot = 0.0f;
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1f + this.head.yRot;
            this.rightArm.xRot = -1.5707964f + this.head.xRot;
            this.leftArm.xRot = -0.9424779f + this.head.xRot;
            this.leftArm.yRot = this.head.yRot - 0.4f;
            this.leftArm.zRot = 1.5707964f;
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold((ModelPart)this.rightArm, (ModelPart)this.leftArm, (ModelPart)this.head, (boolean)true);
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge((ModelPart)this.rightArm, (ModelPart)this.leftArm, (LivingEntity)pEntity, (boolean)true);
        } else if (abstractillager$illagerarmpose == AbstractIllager.IllagerArmPose.CELEBRATING) {
            this.rightArm.z = 0.0f;
            this.rightArm.x = -5.0f;
            this.rightArm.xRot = Mth.cos((float)(pAgeInTicks * 0.6662f)) * 0.05f;
            this.rightArm.zRot = 2.670354f;
            this.rightArm.yRot = 0.0f;
            this.leftArm.z = 0.0f;
            this.leftArm.x = 5.0f;
            this.leftArm.xRot = Mth.cos((float)(pAgeInTicks * 0.6662f)) * 0.05f;
            this.leftArm.zRot = -2.3561945f;
            this.leftArm.yRot = 0.0f;
        }
    }
}

