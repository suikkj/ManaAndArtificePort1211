 
package com.mna.blocks.artifice;

import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.blocks.BlockInit;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;

public class WardingCandleBlock
extends WaterloggableBlock
implements ISpellInteractibleBlock<WardingCandleBlock> {
    public static final BooleanProperty ACTIVE = BooleanProperty.create((String)"active");
    protected static final VoxelShape SHAPE = Block.box((double)6.0, (double)0.0, (double)6.0, (double)10.0, (double)16.0, (double)10.0);

    public WardingCandleBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).noOcclusion().strength(1.0f).isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue((Property)ACTIVE, (Comparable)Boolean.valueOf(false)));
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        if (!worldIn.isClientSide) {
            BlockState newstate = state;
            newstate = (BlockState)state.setValue((Property)ACTIVE, (Comparable)Boolean.valueOf((Boolean)state.getValue((Property)ACTIVE) == false));
            if (((Boolean)newstate.getValue((Property)ACTIVE)).booleanValue()) {
                if (player.getItemInHand(handIn).getItem() != Items.FLINT_AND_STEEL) {
                    return InteractionResult.FAIL;
                }
                MNACap.get(worldIn, WorldMagicProvider.MAGIC).ifPresent(w -> w.addWardingCandleLocation(pos));
                worldIn.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                MNACap.get(worldIn, WorldMagicProvider.MAGIC).ifPresent(w -> w.removeWardingCandleLocation(pos));
                worldIn.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            worldIn.setBlock(pos, newstate, 1);
            worldIn.sendBlockUpdated(pos, state, newstate, 2);
        }
        return InteractionResult.SUCCESS;
    }

    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (((Boolean)stateIn.getValue((Property)ACTIVE)).booleanValue()) {
            for (int i = 0; i < 2; ++i) {
                worldIn.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setScale(0.02f).setMaxAge((int)(20.0 + Math.random() * 20.0)).setColor(30, 172, 255), (double)((float)pos.getX() + 0.5f), (double)pos.getY() + 0.93 + Math.random() * 0.05, (double)((float)pos.getZ() + 0.5f), 0.0, 0.005, 0.0);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{ACTIVE});
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return (Boolean)state.getValue((Property)ACTIVE) != false ? 15 : 5;
    }

    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!(pLevel.isClientSide || pNewState.is((Block)BlockInit.WARDING_CANDLE.get()) && ((Boolean)pNewState.getValue((Property)ACTIVE)).booleanValue())) {
            MNACap.get(pLevel, WorldMagicProvider.MAGIC).ifPresent(w -> w.removeWardingCandleLocation(pPos));
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, BlockEntity te, ItemStack stack) {
        if (!worldIn.isClientSide) {
            MNACap.get(worldIn, WorldMagicProvider.MAGIC).ifPresent(w -> w.removeWardingCandleLocation(pos));
        }
        super.playerDestroy(worldIn, player, pos, state, te, stack);
    }

    public void wasExploded(Level worldIn, BlockPos pos, Explosion explosionIn) {
        if (!worldIn.isClientSide) {
            MNACap.get(worldIn, WorldMagicProvider.MAGIC).ifPresent(w -> w.removeWardingCandleLocation(pos));
        }
    }

    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (!worldIn.isClientSide) {
            MNACap.get(worldIn, WorldMagicProvider.MAGIC).ifPresent(w -> w.removeWardingCandleLocation(pos));
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public boolean onHitBySpell(Level world, BlockPos pos, ISpellDefinition spell) {
        BlockState existing;
        if (!world.isClientSide && spell.getAffinity().containsKey(Affinity.FIRE) && (existing = world.getBlockState(pos)).hasProperty((Property)ACTIVE) && !((Boolean)existing.getValue((Property)ACTIVE)).booleanValue()) {
            BlockState newState = (BlockState)existing.setValue((Property)ACTIVE, (Comparable)Boolean.valueOf(true));
            MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(w -> w.addWardingCandleLocation(pos));
            world.setBlock(pos, newState, 1);
            world.sendBlockUpdated(pos, existing, newState, 2);
            return true;
        }
        return false;
    }

    public static boolean shouldEntityBeBlocked(EntityType<?> entityType) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        String regName = key.toString();
        String modName = key.getNamespace() + ":*";
        if (GeneralConfigValues.WardingCandleBlacklist.contains("*.*")) {
            return true;
        }
        if (GeneralConfigValues.WardingCandleBlacklist.contains(regName) || GeneralConfigValues.WardingCandleBlacklist.contains(modName)) {
            return true;
        }
        if (GeneralConfigValues.WardingCandleWhitelist.contains(regName) || GeneralConfigValues.WardingCandleWhitelist.contains(modName)) {
            return false;
        }
        return false;
    }

    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return true;
    }
}


