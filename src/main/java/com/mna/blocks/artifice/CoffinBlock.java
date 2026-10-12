 
package com.mna.blocks.artifice;

import com.mna.api.sound.SFX;
import com.mna.blocks.tileentities.CoffinTile;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.boss.WitherLich;
import com.mna.factions.Factions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import com.mna.api.tools.MNACap;

public class CoffinBlock
extends BedBlock {
    public CoffinBlock() {
        super(DyeColor.BLACK, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).noOcclusion().strength(3.0f));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.CONSUME;
        }
        if (state.getValue((Property)PART) != BedPart.HEAD && (state = worldIn.getBlockState(pos = pos.relative((Direction)state.getValue((Property)FACING)))).getBlock() != this) {
            return InteractionResult.CONSUME;
        }
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te != null && te instanceof CoffinTile) {
            CoffinTile coffin = (CoffinTile)te;
            if (coffin.getRitualPlayer() == player) {
                if (!((Boolean)state.getValue((Property)OCCUPIED)).booleanValue()) {
                    this.applyColdDark(player, worldIn, state, pos);
                    coffin.setRitualPlayer(null);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
            MutableBoolean canUse = new MutableBoolean(false);
            MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> canUse.setValue(p.getAlliedFaction() == Factions.UNDEAD));
            if (!canUse.booleanValue()) {
                player.sendSystemMessage((Component)Component.translatable((String)"mna.faction.cant_use_generic"));
                return InteractionResult.FAIL;
            }
            return super.use(state, worldIn, pos, player, handIn, hit);
        }
        return InteractionResult.FAIL;
    }

    private void applyColdDark(Player player, Level world, BlockState coffin, BlockPos pos) {
        player.getPersistentData().putLong("coldDarkPos", pos.asLong());
        player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.COLD_DARK.get()), 200));
        Direction dir = coffin.getBedDirection((LevelReader)world, pos).getOpposite();
        BlockPos destBP = pos.relative(dir.getCounterClockWise()).relative(dir, 2);
        BlockPos startBP = destBP.relative(dir, 10);
        Vec3 adjustment = Vec3.ZERO;
        switch (dir) {
            case WEST: {
                adjustment = new Vec3(0.0, 0.0, -1.0);
                break;
            }
            case EAST: {
                adjustment = new Vec3(0.0, 0.0, 1.0);
                break;
            }
            case NORTH: {
                adjustment = new Vec3(1.0, 0.0, 0.0);
                break;
            }
            case SOUTH: {
                adjustment = new Vec3(-1.0, 0.0, 0.0);
                break;
            }
        }
        Vec3 dest = new Vec3((double)((float)destBP.getX() + 0.5f), (double)destBP.getY() + 0.5, (double)((float)destBP.getZ() + 0.5f)).add(adjustment);
        Vec3 start = new Vec3((double)startBP.getX(), (double)startBP.getY(), (double)startBP.getZ()).add(adjustment);
        WitherLich lich = new WitherLich(world);
        lich.setFactionJoin(player, start, dest, 100);
        lich.setPos(start.x, start.y, start.z);
        world.addFreshEntity((Entity)lich);
        world.playSound(null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SFX.Event.Ritual.COLD_DARK, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
    }

    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float dist) {
        super.fallOn(level, state, pos, entity, dist * 2.0f);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoffinTile(pos, state);
    }
}

