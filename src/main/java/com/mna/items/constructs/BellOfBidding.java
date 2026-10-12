 
package com.mna.items.constructs;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.api.blocks.FillerBlock;
import com.mna.api.entities.construct.IConstruct;
import com.mna.api.events.GenericProgressionEvent;
import com.mna.api.events.ProgressionEventIDs;
import com.mna.api.items.TieredItem;
import com.mna.api.tools.MATags;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.artifice.LodestarBlock;
import com.mna.blocks.tileentities.OffsetBlockTile;
import com.mna.entities.constructs.ai.ConstructChop;
import com.mna.entities.constructs.ai.ConstructCollectFluid;
import com.mna.entities.constructs.ai.ConstructCommandFollowAndGuard;
import com.mna.entities.constructs.ai.ConstructCommandFollowLodestar;
import com.mna.entities.constructs.ai.ConstructCommandReturnToTable;
import com.mna.entities.constructs.ai.ConstructHarvest;
import com.mna.entities.constructs.ai.ConstructWearItem;
import com.mna.entities.constructs.ai.base.ConstructTasks;
import com.mna.entities.constructs.animated.Construct;
import com.mna.items.base.IRadialInventorySelect;
import java.util.Arrays;
import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BellOfBidding
extends TieredItem
implements IRadialInventorySelect {
    private static final String NBT_COMMANDING_ENTITY = "commanding_entity";
    private static final String NBT_COMMAND_TYPE = "command";

    public BellOfBidding() {
        super(new Item.Properties().stacksTo(1));
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        AABB box;
        ItemStack stack = player.getItemInHand(hand);
        Vec3 playerPos = player.getEyePosition();
        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        range *= range;
        Vec3 viewVec = player.getViewVector(1.0f);
        Vec3 clipEnd = playerPos.add(viewVec.x * range, viewVec.y * range, viewVec.z * range);
        EntityHitResult res = ProjectileUtil.getEntityHitResult((Entity)player, (Vec3)playerPos, (Vec3)clipEnd, (AABB)(box = player.getBoundingBox().expandTowards(viewVec.scale(range)).inflate(1.0, 1.0, 1.0)), e -> e.isAlive(), (double)range);
        if (res != null && res.getType() == HitResult.Type.ENTITY) {
            Entity e2 = res.getEntity();
            Commands cmd = this.getCommand(stack);
            if (e2 instanceof Construct) {
                Construct construct = (Construct)e2;
                if (player.isCrouching()) {
                    this.setCommandingEntity(construct, stack);
                    if (!player.level().isClientSide()) {
                        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.bell_of_bidding.bound"));
                    }
                    return InteractionResultHolder.success(stack);
                }
                if (cmd == Commands.DIAGNOSTICS) {
                    return this.issueDiagnosticsCommand(stack, player, (LivingEntity)construct, hand);
                }
                if (cmd == Commands.STAY) {
                    return this.issueStayCommand(stack, player, (LivingEntity)construct, hand);
                }
                if (cmd == Commands.FOLLOW) {
                    return this.issueFollowCommand(stack, player, (LivingEntity)construct, hand, false);
                }
                if (cmd == Commands.GUARD) {
                    return this.issueFollowCommand(stack, player, (LivingEntity)construct, hand, true);
                }
                if (cmd == Commands.EAT) {
                    return this.issueEatCommand(stack, player, construct, hand);
                }
            } else if (e2 instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)e2;
                if (cmd == Commands.ATTACK) {
                    return this.issueAttackCommand(stack, player, living, hand);
                }
                if (cmd == Commands.GUARD) {
                    return this.issueFollowCommand(stack, player, living, hand, true);
                }
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        if (context.getPlayer().isCrouching()) {
            if (world.isClientSide) {
                return InteractionResult.PASS;
            }
            this.clearCommandingEntity(stack);
            context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.bell_of_bidding.cleared"));
            return InteractionResult.SUCCESS;
        }
        Commands cmd = this.getCommand(stack);
        if (cmd == Commands.MOVE) {
            return this.issueMoveCommand(stack, context);
        }
        if (cmd == Commands.INTERACT) {
            return this.issueInteractCommand(stack, context);
        }
        if (cmd == Commands.WEAR) {
            return this.issueWearCommand(stack, context);
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, net.minecraft.world.item.Item.TooltipContext p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
        IRadialInventorySelect.super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
    }

    private Commands getCommand(ItemStack stack) {
        return Commands.values()[this.getIndex(stack)];
    }

    public static void setCommand(ItemStack stack, Commands option) {
        if (!(stack.getItem() instanceof BellOfBidding)) {
            return;
        }
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(NBT_COMMAND_TYPE, option.ordinal());
    }

    private void setCommandingEntity(Construct construct, ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(NBT_COMMANDING_ENTITY, construct != null ? construct.getId() : -1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private void clearCommandingEntity(ItemStack stack) {
        this.setCommandingEntity(null, stack);
    }

    private Construct getCommandedEntity(Level world, ItemStack stack) {
        Entity e = world.getEntity(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(NBT_COMMANDING_ENTITY));
        if (e instanceof Construct) {
            return (Construct)e;
        }
        return null;
    }

    private List<Construct> getCommandingEntities(Level world, Player player, ItemStack stack) {
        Construct commandedEntity = this.getCommandedEntity(world, stack);
        if (commandedEntity != null) {
            return Arrays.asList(commandedEntity);
        }
        return world.getEntitiesOfClass(Construct.class, player.getBoundingBox().inflate(32.0)).stream().filter(c -> c.isFollowing(player)).toList();
    }

    private void ding(Player player) {
        if (!player.level().isClientSide()) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    private InteractionResultHolder<ItemStack> issueWearCommand(ItemStack stack, Player player, ItemEntity target, InteractionHand hand) {
        this.getCommandingEntities(player.level(), player, stack).forEach(construct -> {
            ConstructWearItem task = (ConstructWearItem)ConstructTasks.WEAR_ITEM.instantiateTask((IConstruct<?>)construct);
            task.setTarget(target);
            task.setOneOff(player);
            if (construct.setCurrentCommand(player, task)) {
                this.ding(player);
            }
        });
        this.ding(player);
        return InteractionResultHolder.sidedSuccess(stack, (!player.level().isClientSide() ? 1 : 0) != 0);
    }

    private InteractionResultHolder<ItemStack> issueAttackCommand(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        this.getCommandingEntities(player.level(), player, stack).forEach(construct -> {
            ConstructCommandFollowAndGuard task = (ConstructCommandFollowAndGuard)ConstructTasks.FOLLOW_DEFEND.instantiateTask((IConstruct<?>)construct);
            task.setFollowTarget((LivingEntity)player);
            task.setShouldGuard(true);
            construct.setTarget(target);
        });
        this.ding(player);
        return InteractionResultHolder.sidedSuccess(stack, (!player.level().isClientSide() ? 1 : 0) != 0);
    }

    private InteractionResultHolder<ItemStack> issueEatCommand(ItemStack stack, Player player, Construct target, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            target.eatItem(player);
        }
        this.ding(player);
        return InteractionResultHolder.sidedSuccess(stack, (!player.level().isClientSide() ? 1 : 0) != 0);
    }

    private InteractionResultHolder<ItemStack> issuePickupCommand(ItemStack stack, Player player, BlockPos target, InteractionHand hand) {
        this.getCommandingEntities(player.level(), player, stack).forEach(construct -> construct.pickupItem(target, player));
        return InteractionResultHolder.sidedSuccess(stack, (!player.level().isClientSide() ? 1 : 0) != 0);
    }

    private InteractionResultHolder<ItemStack> issueDiagnosticsCommand(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide() && target instanceof Construct) {
            ManaAndArtifice.instance.proxy.openConstructDiagnostics((Construct)target);
        }
        this.ding(player);
        return InteractionResultHolder.success(stack);
    }

    private InteractionResultHolder<ItemStack> issueFollowCommand(ItemStack stack, Player player, LivingEntity target, InteractionHand hand, boolean guard) {
        if (player.level().isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }
        if (target instanceof Construct) {
            Construct construct = (Construct)target;
            ConstructCommandFollowAndGuard task = (ConstructCommandFollowAndGuard)ConstructTasks.FOLLOW_DEFEND.instantiateTask(construct);
            task.setFollowTarget((LivingEntity)player);
            task.setShouldGuard(guard);
            if (!guard) {
                construct.setTarget(null);
            }
            if (construct.setCurrentCommand(player, task)) {
                this.ding(player);
            }
            return InteractionResultHolder.success(stack);
        }
        this.getCommandingEntities(player.level(), player, stack).forEach(commandedEntity -> {
            ConstructCommandFollowAndGuard task = (ConstructCommandFollowAndGuard)ConstructTasks.FOLLOW_DEFEND.instantiateTask((IConstruct<?>)commandedEntity);
            task.setFollowTarget(target);
            task.setShouldGuard(guard);
            commandedEntity.setCurrentCommand(player, task);
        });
        this.ding(player);
        return InteractionResultHolder.success(stack);
    }

    private InteractionResultHolder<ItemStack> issueChopCommand(ItemStack stack, Player player, LivingEntity target, BlockPos pos, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }
        if (target instanceof Construct) {
            Construct construct = (Construct)target;
            ConstructChop task = (ConstructChop)ConstructTasks.CHOP.instantiateTask(construct);
            task.setAreaManually(pos, pos);
            task.setOneOff(player);
            if (construct.setCurrentCommand(player, task)) {
                this.ding(player);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private InteractionResultHolder<ItemStack> issueHarvestCommand(ItemStack stack, Player player, LivingEntity target, BlockPos pos, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }
        if (target instanceof Construct) {
            Construct construct = (Construct)target;
            ConstructHarvest task = (ConstructHarvest)ConstructTasks.HARVEST.instantiateTask(construct);
            task.setAreaManually(pos, pos);
            task.setOneOff(player);
            if (construct.setCurrentCommand(player, task)) {
                this.ding(player);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private InteractionResultHolder<ItemStack> issueCollectFluidCommand(ItemStack stack, Player player, LivingEntity target, BlockPos pos, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }
        if (target instanceof Construct) {
            Construct construct = (Construct)target;
            ConstructCollectFluid task = (ConstructCollectFluid)ConstructTasks.GATHER_FLUID.instantiateTask(construct);
            task.setAreaManually(pos, pos);
            task.setOneOff(player);
            if (construct.setCurrentCommand(player, task)) {
                this.ding(player);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private InteractionResultHolder<ItemStack> issueStayCommand(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }
        if (target instanceof Construct) {
            Construct construct = (Construct)target;
            if (construct.setCurrentCommand(player, ConstructTasks.STAY.instantiateTask(construct))) {
                this.ding(player);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    private InteractionResult issueInteractCommand(ItemStack stack, UseOnContext context) {
        BlockEntity be;
        BlockPos clickPos = context.getClickedPos();
        BlockPos offsetPos = clickPos.offset(context.getClickedFace().getNormal());
        BlockState block = context.getLevel().getBlockState(context.getClickedPos());
        BlockState offsetBlock = context.getLevel().getBlockState(offsetPos);
        if (block.getBlock() instanceof FillerBlock && (be = context.getLevel().getBlockEntity(context.getClickedPos())) != null && be instanceof OffsetBlockTile) {
            clickPos = context.getClickedPos().offset((Vec3i)((OffsetBlockTile)be).getOffset());
            offsetPos = clickPos.offset(context.getClickedFace().getNormal());
            block = context.getLevel().getBlockState(clickPos);
            offsetBlock = context.getLevel().getBlockState(offsetPos);
        }
        BlockState adjBlock = block;
        BlockState adjOffsetBlock = offsetBlock;
        BlockPos adjOffsetPos = offsetPos;
        BlockPos adjClickPos = clickPos;
        this.getCommandingEntities(context.getLevel(), context.getPlayer(), stack).forEach(commandedEntity -> {
            if (adjBlock.getBlock() == BlockInit.CONSTRUCT_WORKBENCH.get()) {
                ConstructCommandReturnToTable modify = (ConstructCommandReturnToTable)ConstructTasks.MODIFY.instantiateTask((IConstruct<?>)commandedEntity);
                modify.setTablePos(adjClickPos);
                commandedEntity.setCurrentCommand(context.getPlayer(), modify);
            } else if (adjBlock.getBlock() instanceof LodestarBlock) {
                ConstructCommandFollowLodestar lodestar = (ConstructCommandFollowLodestar)ConstructTasks.LODESTAR.instantiateTask((IConstruct<?>)commandedEntity);
                lodestar.setTileEntity(adjClickPos, Direction.DOWN);
                if (commandedEntity.setCurrentCommand(context.getPlayer(), lodestar)) {
                    NeoForge.EVENT_BUS.post((Event)new GenericProgressionEvent(context.getPlayer(), ProgressionEventIDs.CONSTRUCT_LODESTAR_ASSIGNED));
                    Player patt16154$temp = context.getPlayer();
                    if (patt16154$temp instanceof ServerPlayer) {
                        ServerPlayer serverPlayer = (ServerPlayer)patt16154$temp;
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, adjClickPos, stack);
                    }
                }
            } else if (context.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(adjClickPos.above())).size() > 0) {
                this.issuePickupCommand(stack, context.getPlayer(), adjClickPos.above(), context.getHand());
            } else if (!adjOffsetBlock.getFluidState().isEmpty()) {
                this.issueCollectFluidCommand(stack, context.getPlayer(), (LivingEntity)commandedEntity, adjOffsetPos, context.getHand());
            } else if (MATags.isBlockIn(adjBlock.getBlock(), ResourceLocation.parse("logs"))) {
                this.issueChopCommand(stack, context.getPlayer(), (LivingEntity)commandedEntity, adjClickPos, context.getHand());
            } else if (ConstructHarvest.validBlock(adjBlock)) {
                this.issueHarvestCommand(stack, context.getPlayer(), (LivingEntity)commandedEntity, adjClickPos, context.getHand());
            }
        });
        this.ding(context.getPlayer());
        return InteractionResult.SUCCESS;
    }

    private InteractionResult issueWearCommand(ItemStack stack, UseOnContext context) {
        BlockEntity be;
        BlockPos clickPos = context.getClickedPos();
        BlockState block = context.getLevel().getBlockState(context.getClickedPos());
        if (block.getBlock() instanceof FillerBlock && (be = context.getLevel().getBlockEntity(context.getClickedPos())) != null && be instanceof OffsetBlockTile) {
            clickPos = context.getClickedPos().offset((Vec3i)((OffsetBlockTile)be).getOffset());
            block = context.getLevel().getBlockState(clickPos);
        }
        BlockPos adjClickPos = clickPos;
        this.getCommandingEntities(context.getLevel(), context.getPlayer(), stack).forEach(commandedEntity -> {
            List<ItemEntity> entities = context.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(adjClickPos.above()));
            if (entities.size() > 0) {
                this.issueWearCommand(stack, context.getPlayer(), (ItemEntity)entities.get(0), context.getHand());
            }
        });
        this.ding(context.getPlayer());
        return InteractionResult.SUCCESS;
    }

    private InteractionResult issueMoveCommand(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        if (world.isClientSide) {
            return InteractionResult.PASS;
        }
        this.getCommandingEntities(world, context.getPlayer(), stack).forEach(construct -> construct.moveTo(context.getClickLocation(), context.getPlayer()));
        this.ding(context.getPlayer());
        return InteractionResult.SUCCESS;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public int getIndex(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !(stack.getItem() instanceof BellOfBidding)) {
            return 0;
        }
        int type = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("index");
        Commands[] values = Commands.values();
        if (type < 0 || type >= values.length) {
            return 0;
        }
        return values[type].ordinal();
    }

    @Override
    public void setIndex(ItemStack stack, int index) {
        if (!(stack.getItem() instanceof BellOfBidding)) {
            return;
        }
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt("index", index);
    }

    public boolean isFoil(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(NBT_COMMANDING_ENTITY) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(NBT_COMMANDING_ENTITY) != -1;
    }

    public static enum Commands {
        FOLLOW,
        GUARD,
        INTERACT,
        MOVE,
        STAY,
        DIAGNOSTICS,
        EAT,
        ATTACK,
        WEAR;

        private final ResourceLocation iconTexture = RLoc.create("textures/gui/construct/command_icons/" + this.toString().toLowerCase() + ".png");

        public ResourceLocation getIcon() {
            return this.iconTexture;
        }
    }
}


