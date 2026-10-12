 
package com.mna.items.artifice;

import java.util.function.Supplier;
import com.mna.api.items.TieredBlockItem;
import com.mna.api.spells.targeting.SpellTargetHelper;
import com.mna.blocks.artifice.FluidJugBlock;
import com.mna.items.renderers.fluid_jugs.FluidJugItemRenderer;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FluidJug
extends TieredBlockItem {
    public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.core.component.DataComponentType<?>> COMPONENTS = net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, "mna");
    public static final java.util.function.Supplier<net.minecraft.core.component.DataComponentType<net.neoforged.neoforge.fluids.SimpleFluidContent>> SIMPLE_FLUID_COMPONENT = COMPONENTS.register("simple_fluid", () -> new net.minecraft.core.component.DataComponentType.Builder<net.neoforged.neoforge.fluids.SimpleFluidContent>().persistent(net.neoforged.neoforge.fluids.SimpleFluidContent.CODEC).networkSynchronized(net.neoforged.neoforge.fluids.SimpleFluidContent.STREAM_CODEC).build());
    public static final int CAPACITY = 16000;

    public FluidJug(FluidJugBlock forBlock) {
        super((Block)forBlock, new Item.Properties().stacksTo(1));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new FluidJugItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels(), FluidJug.this.isInfinite() ? FluidJugItemRenderer.jug_artifact : FluidJugItemRenderer.jug_base);

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        FluidStack fluidStack = this.getFluidTagData(stack);
        if (!fluidStack.isEmpty()) {
            int mb = fluidStack.getAmount();
            Component name = fluidStack.getDisplayName();
            tooltip.add((Component)Component.translatable((String)"block.mna.fluid_jug.contents", (Object[])new Object[]{mb, name}));
        }
    }

    public FluidStack getFluidTagData(ItemStack stack) {
        Optional contained = FluidUtil.getFluidContained((ItemStack)stack);
        if (contained.isPresent() && !((FluidStack)contained.get()).isEmpty()) {
            return (FluidStack)contained.get();
        }
        return FluidStack.EMPTY;
    }

    public boolean isInfinite() {
        return ((FluidJugBlock)this.getBlock()).is_infinite();
    }

    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer().isCrouching()) {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        FluidStack flStack = this.getFluidTagData(itemstack);
        BlockHitResult rayTraceResult = FluidJug.getPlayerPOVHitResult((Level)world, (Player)player, (ClipContext.Fluid)ClipContext.Fluid.SOURCE_ONLY);
        EntityHitResult cowHit = SpellTargetHelper.rayTraceEntities(world, (Entity)player, player.getEyePosition(), player.getEyePosition().add(player.getForward().scale(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE))), player.getBoundingBox().inflate(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)), e -> e instanceof Cow && e.isAlive());
        if (cowHit != null) {
            FluidStack insertStack = new FluidStack((Fluid)NeoForgeMod.MILK.get(), 1000);
            if (flStack.getFluid().isSame((Fluid)NeoForgeMod.MILK.get()) || flStack.isEmpty()) {
                java.util.Optional<net.neoforged.neoforge.fluids.capability.IFluidHandlerItem> handler = FluidUtil.getFluidHandler((ItemStack)itemstack);
                if (handler.isPresent()) {
                    IFluidHandlerItem handlerResolved = (IFluidHandlerItem)handler.get();
                    handlerResolved.fill(insertStack, IFluidHandler.FluidAction.EXECUTE);
                }
                return InteractionResultHolder.sidedSuccess(itemstack, (boolean)world.isClientSide);
            }
            return InteractionResultHolder.fail(itemstack);
        }
        if (rayTraceResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        }
        BlockPos blockpos = rayTraceResult.getBlockPos();
        Direction direction = rayTraceResult.getDirection();
        BlockPos offsetPos = blockpos.relative(direction);
        if (world.mayInteract(player, blockpos) && player.mayUseItemAt(offsetPos, direction, itemstack)) {
            FluidState worldFluid = world.getFluidState(blockpos);
            if (!worldFluid.isEmpty() && (flStack.isEmpty() || worldFluid.is(flStack.getFluid())) && (this.isInfinite() || flStack.getAmount() < 16000)) {
                return this.pickupFluid(world, blockpos, itemstack, player, direction);
            }
            if (worldFluid.isEmpty() && !flStack.isEmpty()) {
                return this.placeFluid(world, blockpos, offsetPos, player, rayTraceResult, itemstack, hand);
            }
        }
        return InteractionResultHolder.fail(itemstack);
    }

    private InteractionResultHolder<ItemStack> pickupFluid(Level world, BlockPos blockpos, ItemStack itemstack, Player player, Direction dir) {
        FluidActionResult res = FluidUtil.tryPickUpFluid((ItemStack)itemstack, (Player)player, (Level)world, (BlockPos)blockpos, (Direction)dir);
        if (res.success) {
            return InteractionResultHolder.sidedSuccess(res.result, (boolean)world.isClientSide);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    private InteractionResultHolder<ItemStack> placeFluid(Level world, BlockPos blockpos, BlockPos offsetPos, Player player, BlockHitResult rayTraceResult, ItemStack itemstack, InteractionHand hand) {
        FluidStack myFluid = this.getFluidTagData(itemstack);
        BlockState worldState = world.getBlockState(blockpos);
        BlockPos resolvedOffset = this.canBlockContainFluid(world, blockpos, worldState, myFluid) ? blockpos : offsetPos;
        FluidActionResult res = FluidUtil.tryPlaceFluid((Player)player, (Level)world, (InteractionHand)hand, (BlockPos)resolvedOffset, (ItemStack)itemstack, (FluidStack)myFluid);
        if (res.success) {
            return InteractionResultHolder.sidedSuccess(res.result, (boolean)world.isClientSide);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    private boolean canBlockContainFluid(Level worldIn, BlockPos posIn, BlockState blockstate, FluidStack myFluid) {
        return blockstate.getBlock() instanceof LiquidBlockContainer && ((LiquidBlockContainer)blockstate.getBlock()).canPlaceLiquid((Player)null, (BlockGetter)worldIn, posIn, blockstate, myFluid.getFluid());
    }
}


