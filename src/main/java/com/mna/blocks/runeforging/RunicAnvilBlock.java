 
package com.mna.blocks.runeforging;

import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.blocks.WaterloggableBlock;
import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.events.GenericProgressionEvent;
import com.mna.api.events.ProgressionEventIDs;
import com.mna.api.events.RunicAnvilItemUsedEvent;
import com.mna.api.sound.SFX;
import com.mna.api.tools.MATags;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.RunicAnvilTile;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.items.ItemInit;
import com.mna.items.armor.BrokenMageArmor;
import com.mna.items.ritual.PractitionersPatch;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.ritual.PractitionersPouchPatches;
import com.mna.items.runes.Rune;
import com.mna.items.runes.RunescribingRecipePaper;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.runeforging.RuneforgingRecipe;
import com.mna.recipes.runeforging.RunescribingRecipe;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;

public class RunicAnvilBlock
extends WaterloggableBlock
implements ICutoutBlock,
EntityBlock {
    protected static final VoxelShape SHAPE_BASE_WE = Block.box((double)4.0, (double)0.0, (double)3.0, (double)12.0, (double)13.0, (double)13.0);
    protected static final VoxelShape SHAPE_HEAD_WE = Block.box((double)4.0, (double)13.0, (double)-3.0, (double)12.0, (double)16.0, (double)19.0);
    protected static final VoxelShape SHAPE_BASE_NS = Block.box((double)3.0, (double)0.0, (double)4.0, (double)13.0, (double)13.0, (double)12.0);
    protected static final VoxelShape SHAPE_HEAD_NS = Block.box((double)-3.0, (double)13.0, (double)4.0, (double)19.0, (double)16.0, (double)12.0);
    protected static final VoxelShape SHAPE_WE = Shapes.or((VoxelShape)SHAPE_BASE_WE, (VoxelShape)SHAPE_HEAD_WE);
    protected static final VoxelShape SHAPE_NS = Shapes.or((VoxelShape)SHAPE_BASE_NS, (VoxelShape)SHAPE_HEAD_NS);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create((String)"active");
    public static final ResourceLocation TAG_COALS = ResourceLocation.parse("coals");

    public RunicAnvilBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(2.0f, 3.0f).noOcclusion().sound(SoundType.ANVIL).isSuffocating(BlockInit.NEVER), false);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(ACTIVE, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(new Property[]{FACING, ACTIVE});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return (BlockState)super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        Direction facing = (Direction)state.getValue((Property)FACING);
        if (facing == Direction.EAST || facing == Direction.WEST) {
            return SHAPE_WE;
        }
        return SHAPE_NS;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunicAnvilTile(pos, state);
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult p_225533_6_) {
        ItemStack activeStack = player.getItemInHand(handIn);
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity == null && !(tileEntity instanceof RunicAnvilTile)) {
            return InteractionResult.SUCCESS;
        }
        RunicAnvilTile te = (RunicAnvilTile)tileEntity;
        if (activeStack.getItem() == ItemInit.RUNESMITH_HAMMER.get() || activeStack.getItem() == ItemInit.RUNIC_MALUS.get()) {
            return this.onHammerUse(activeStack, player, worldIn, pos, state, te);
        }
        if (MATags.isItemEqual(activeStack, TAG_COALS)) {
            return this.onCharcoalUse(activeStack, player, worldIn, pos, state, te);
        }
        if (activeStack.getItem() == ItemInit.SORCEROUS_SEWING_SET.get()) {
            return this.onSewingKitUse(activeStack, player, worldIn, pos, state, te);
        }
        return this.onGenericItemUse(activeStack, player, worldIn, pos, state, te, handIn);
    }

    private InteractionResult onGenericItemUse(ItemStack activeStack, Player player, Level worldIn, BlockPos pos, BlockState state, RunicAnvilTile te, InteractionHand hand) {
        if (!worldIn.isClientSide) {
            IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (progression == null) {
                return InteractionResult.FAIL;
            }
            RunicAnvilItemUsedEvent event = new RunicAnvilItemUsedEvent(te.getItem(0), te.getItem(1), activeStack, player);
            if (NeoForge.EVENT_BUS.post(event)) {
                te.setItem(0, event.pattern.isEmpty() ? ItemStack.EMPTY : event.pattern.copy());
                te.setItem(1, event.material.isEmpty() ? ItemStack.EMPTY : event.material.copy());
                player.setItemInHand(hand, event.catalyst.isEmpty() ? ItemStack.EMPTY : event.catalyst.copy());
                return InteractionResult.SUCCESS;
            }
            ItemStack existing = te.getItem(0);
            if (!(existing.getItem() != ItemInit.RUNESMITH_HAMMER.get() && existing.getItem() != ItemInit.RUNIC_MALUS.get() || activeStack.isEmpty())) {
                te.setItem(0, ItemStack.EMPTY);
                if (this.insertItems(te, player, activeStack, worldIn, pos, state, progression.getTier())) {
                    if (!player.addItem(existing)) {
                        player.drop(existing, true);
                    }
                } else {
                    te.setItem(0, existing);
                }
                return InteractionResult.SUCCESS;
            }
            if (!this.insertItems(te, player, activeStack, worldIn, pos, state, progression.getTier())) {
                this.removeItems(te, player, activeStack, worldIn, pos, state);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult onCharcoalUse(ItemStack activeStack, Player player, Level worldIn, BlockPos pos, BlockState state, RunicAnvilTile te) {
        ItemStack rune = te.getItem(0);
        if (rune.isEmpty()) {
            return InteractionResult.FAIL;
        }
        if (te.getItem(1).getItem() != Items.PAPER) {
            return InteractionResult.FAIL;
        }
        Optional<RuneforgingRecipe> forgeRecipe = worldIn.getRecipeManager().getAllRecipesFor(RecipeInit.RUNEFORGING_TYPE.get()).stream().filter(r -> r.value().getOutputResource().toString().equals(BuiltInRegistries.ITEM.getKey(rune.getItem()).toString())).map(r -> r.value()).findFirst();
        if (!forgeRecipe.isPresent()) {
            return InteractionResult.FAIL;
        }
        Optional<RunescribingRecipe> scribeRecipe = worldIn.getRecipeManager().getAllRecipesFor(RecipeInit.RUNESCRIBING_TYPE.get()).stream().filter(r -> r.value().getOutputResource().toString().equals(((RuneforgingRecipe)forgeRecipe.get()).getPatternResource().toString())).map(r -> r.value()).findFirst();
        if (!scribeRecipe.isPresent()) {
            return InteractionResult.FAIL;
        }
        ItemStack patternStack = new ItemStack((ItemLike)ItemInit.RECIPE_SCRAP_RUNESCRIBING.get());
        RunescribingRecipePaper.setRecipe(patternStack, scribeRecipe.get());
        if (!worldIn.isClientSide) {
            te.setItem(1, patternStack);
        }
        worldIn.playSound(player, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SFX.Gui.CHARCOAL_SCRIBBLE, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (!player.isCreative()) {
            activeStack.shrink(1);
        }
        if (!worldIn.isClientSide && player instanceof ServerPlayer) {
            CustomAdvancementTriggers.RUNIC_ANVIL_CRAFT.trigger((ServerPlayer)player, patternStack);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult onHammerUse(ItemStack activeStack, Player player, Level worldIn, BlockPos pos, BlockState state, RunicAnvilTile te) {
        if (player.getCooldowns().getCooldownPercent(activeStack.getItem(), 0.0f) > 0.0f) {
            return InteractionResult.SUCCESS;
        }
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression == null) {
            return InteractionResult.FAIL;
        }
        if (worldIn.isClientSide) {
            Item item = te.getItem(1).getItem();
            this.spawnParticles(item, worldIn, pos);
        }
        int advanceResponse = te.advanceCrafting(player, progression.getTier());
        if (!worldIn.isClientSide) {
            worldIn.playSound(null, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5f, (float)Math.random() * 0.1f + 0.9f);
            if (advanceResponse == 0 || advanceResponse == 4) {
                player.getCooldowns().addCooldown(activeStack.getItem(), 10);
            } else if (advanceResponse == 2) {
                player.sendSystemMessage((Component)Component.translatable((String)"block.mna.runic_anvil.low_tier"));
                player.getCooldowns().addCooldown(activeStack.getItem(), 40);
            } else if (te.getItem(0).isEmpty()) {
                this.insertItems(te, player, activeStack, worldIn, pos, state, progression.getTier());
            }
        }
        activeStack.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)player.level(), player, e -> {});
        return InteractionResult.SUCCESS;
    }

    private InteractionResult onSewingKitUse(ItemStack activeStack, Player player, Level worldIn, BlockPos pos, BlockState state, RunicAnvilTile te) {
        ItemStack sewable = te.getItem(0);
        if (sewable.getItem() instanceof BrokenMageArmor) {
            if (te.getItem(1).getItem() != ItemInit.INFUSED_SILK.get()) {
                return InteractionResult.FAIL;
            }
            BrokenMageArmor restoredItem = (BrokenMageArmor)sewable.getItem();
            ItemStack restoredStack = restoredItem.restore(sewable);
            restoredStack.setDamageValue(0);
            te.setItem(1, ItemStack.EMPTY);
            activeStack.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)player.level(), player, e -> {});
            if (player instanceof ServerPlayer) {
                CustomAdvancementTriggers.RUNIC_ANVIL_CRAFT.trigger((ServerPlayer)player, restoredStack);
            }
            worldIn.playSound(player, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0f, 1.0f);
            te.setItem(0, restoredStack);
            return InteractionResult.SUCCESS;
        }
        if (sewable.getItem() instanceof PractitionersPouch) {
            ItemStack patchStack = te.getItem(1);
            if (!(patchStack.getItem() instanceof PractitionersPatch)) {
                return InteractionResult.FAIL;
            }
            PractitionersPouchPatches patch = ((PractitionersPatch)patchStack.getItem()).getPatch();
            int patchLevel = ((PractitionersPatch)patchStack.getItem()).getLevel();
            if (((PractitionersPouch)ItemInit.PRACTITIONERS_POUCH.get()).getPatchLevel(sewable, patch) >= patchLevel) {
                return InteractionResult.FAIL;
            }
            if (((PractitionersPouch)ItemInit.PRACTITIONERS_POUCH.get()).countAppliedPatchesForLimit(sewable, patchStack) >= 4) {
                return InteractionResult.FAIL;
            }
            ((PractitionersPouch)ItemInit.PRACTITIONERS_POUCH.get()).addPatch(sewable, patch, patchLevel);
            te.setItem(1, ItemStack.EMPTY);
            activeStack.hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)player.level(), player, e -> {});
            worldIn.playSound(player, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!worldIn.isClientSide) {
                NeoForge.EVENT_BUS.post(new GenericProgressionEvent(player, ProgressionEventIDs.APPLY_POUCH_PATCH));
                if (player instanceof ServerPlayer) {
                    CustomAdvancementTriggers.RUNIC_ANVIL_CRAFT.trigger((ServerPlayer)player, sewable);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private void spawnParticles(Item item, Level worldIn, BlockPos pos) {
        block3: {
            block2: {
                if (item != ItemInit.VINTEUM_INGOT_SUPERHEATED.get()) break block2;
                for (int i = 0; i < 3; ++i) {
                    worldIn.addParticle((ParticleOptions)ParticleTypes.LAVA, (double)((float)pos.getX() + 0.5f), (double)((float)pos.getY() + 1.1f), (double)((float)pos.getZ() + 0.5f), -0.5 + Math.random(), Math.random(), -0.5 + Math.random());
                }
                break block3;
            }
            if (!(item instanceof Rune)) break block3;
            for (int i = 0; i < 3; ++i) {
                worldIn.addParticle((ParticleOptions)ParticleTypes.CRIT, (double)((float)pos.getX() + 0.5f), (double)((float)pos.getY() + 1.1f), (double)((float)pos.getZ() + 0.5f), -0.5 + Math.random(), Math.random(), -0.5 + Math.random());
            }
        }
    }

    private boolean insertItems(RunicAnvilTile te, Player player, ItemStack heldStack, Level world, BlockPos pos, BlockState state, int playerTier) {
        ItemStack single = heldStack.copy();
        single.setCount(1);
        if (te.pushItemStack(single, player, playerTier)) {
            if (!player.isCreative() || heldStack.getItem() == ItemInit.RUNESMITH_HAMMER.get()) {
                heldStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    private void removeItems(RunicAnvilTile te, Player player, ItemStack heldStack, Level world, BlockPos pos, BlockState state) {
        ItemStack stack = te.popItemStack();
        if (!stack.isEmpty() && !player.addItem(stack)) {
            player.drop(stack, true);
        }
    }

    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            this.dropInventory(worldIn, pos);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private void dropInventory(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null && tileEntity instanceof Container) {
            Containers.dropContents((Level)world, (BlockPos)pos, (Container)((Container)tileEntity));
        }
    }
}


