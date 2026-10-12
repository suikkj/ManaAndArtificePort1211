/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Explosion
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.mna.blocks.ritual;

import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.blocks.FacingBlock;
import com.mna.entities.sorcery.AffinityIcon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChimeriteCrystalBlock
extends FacingBlock
implements ISpellInteractibleBlock<ChimeriteCrystalBlock>,
ITranslucentBlock {
    private static final VoxelShape SHAPE = Block.box((double)3.0, (double)0.0, (double)3.0, (double)13.0, (double)16.0, (double)13.0);

    public ChimeriteCrystalBlock() {
        super(BlockBehaviour.Properties.of().noOcclusion().strength(2.0f));
    }

    @Override
    public boolean onHitBySpell(Level world, BlockPos pos, ISpellDefinition spell) {
        for (IModifiedSpellPart<SpellEffect> c : spell.getComponents()) {
            if (c.getPart() != Components.BREAK) continue;
            return false;
        }
        if (!world.isClientSide) {
            Vec3 entityPos = this.getPositionBasedOnState(world, pos);
            BlockPos search = BlockPos.containing((Position)entityPos);
            world.getEntitiesOfClass(AffinityIcon.class, new AABB(search)).stream().forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
            AffinityIcon efi = new AffinityIcon(world);
            efi.setAffinity(spell.getHighestAffinity());
            efi.setPos(entityPos.x, entityPos.y, entityPos.z);
            world.addFreshEntity((Entity)efi);
        }
        return true;
    }

    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(worldIn, pos, state, player);
        this.removeEntities(worldIn, pos);
    }

    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        super.onBlockExploded(state, world, pos, explosion);
        this.removeEntities(world, pos);
    }

    private void removeEntities(Level world, BlockPos pos) {
        if (!world.isClientSide) {
            Vec3 entityPos = this.getPositionBasedOnState(world, pos);
            BlockPos search = BlockPos.containing((Position)entityPos);
            world.getEntitiesOfClass(AffinityIcon.class, new AABB(search)).stream().forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
        }
    }

    private Vec3 getPositionBasedOnState(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Vec3 basePos = new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        if (state.hasProperty((Property)SURFACE_TYPE) && state.hasProperty((Property)FACING)) {
            int surfType = (Integer)state.getValue((Property)SURFACE_TYPE);
            if (surfType == 1) {
                return basePos.add(0.0, 1.0, 0.0);
            }
            if (surfType == 2) {
                return basePos.add(0.0, -1.0, 0.0);
            }
            Direction facing = (Direction)state.getValue((Property)FACING);
            return basePos.add((double)facing.getStepX(), (double)facing.getStepY(), (double)facing.getStepZ());
        }
        return basePos;
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return 15;
    }

    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }
}

