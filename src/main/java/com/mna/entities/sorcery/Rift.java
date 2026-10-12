/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.entity.IEntityAdditionalSpawnData
 *  net.neoforged.neoforge.network.NetworkHooks
 */
package com.mna.entities.sorcery;

import com.mna.gui.containers.providers.NamedRift;
import com.mna.tools.GuiTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Rift
extends Entity
{
    private static final String KEY_AGE = "age";
    private static final String KEY_IS_ENDERCHEST = "isEnderchest";
    private int age = 0;
    private boolean isEnderChest = false;

    public Rift(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    public void tick() {
        this.setAge(this.getAge() + 1);
        if (this.getAge() >= 200 && !this.level().isClientSide()) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (this.isEnderChest) {
                GuiTools.openEnderChest(player);
            } else {
                ((ServerPlayer)((ServerPlayer)player)).openMenu((MenuProvider)new NamedRift());
            }
        }
        return InteractionResult.SUCCESS;
    }

    public void baseTick() {
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains(KEY_AGE)) {
            this.setAge(compound.getInt(KEY_AGE));
        }
        if (compound.contains(KEY_IS_ENDERCHEST)) {
            this.setEnderChest(compound.getBoolean(KEY_IS_ENDERCHEST));
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt(KEY_AGE, this.getAge());
        compound.putBoolean(KEY_IS_ENDERCHEST, this.isEnderChest);
    }

    public boolean isAttackable() {
        return false;
    }

    public boolean isPickable() {
        return true;
    }

    public int getAge() {
        return this.age;
    }

    public boolean getIsEnderChest() {
        return this.isEnderChest;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEnderChest(boolean enderChest) {
        this.isEnderChest = enderChest;
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.age);
        buffer.writeBoolean(this.isEnderChest);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.age = additionalData.readInt();
        this.isEnderChest = additionalData.readBoolean();
    }
}


