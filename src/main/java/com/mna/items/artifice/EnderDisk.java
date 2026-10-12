 
package com.mna.items.artifice;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IRitualTeleportLocation;
import com.mna.api.items.TieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.blocks.BlockInit;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.gui.containers.providers.NamedEnderDisc;
import com.mna.items.base.IItemWithGui;
import com.mna.items.base.IRadialMenuItem;
import com.mna.items.renderers.obj_gecko.EnderDiscRenderer;
import com.mna.tools.TeleportHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.mna.api.tools.MNACap;

public class EnderDisk
extends TieredItem
implements IItemWithGui<EnderDisk>,
IRadialMenuItem,
GeoItem {
    private static final String KEY_INDEX = "ender_disk_index";
    private static final String KEY_NBT = "ender_disk_data";
    private static final String KEY_COUNT = "count";
    private static final String KEY_NAME = "name";
    private static final String KEY_DIMENSION = "dimension";
    private static final String KEY_ENTRY_PREFIX = "reagent_";
    private static final String CONTROLLER_NAME = "enderDiscController";
    public static final int MAX_PATTERNS = 8;
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public EnderDisk() {
        super(new Item.Properties());
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new EnderDiscRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedEnderDisc();
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        IPlayerMagic magic = (IPlayerMagic)playerIn.getData(PlayerMagicProvider.MAGIC.get());
        if (!magic.isMagicUnlocked()) {
            return InteractionResultHolder.fail(stack);
        }
        if (!this.openGuiIfModifierPressed(playerIn.getItemInHand(handIn), playerIn, worldIn)) {
            this.teleport(worldIn, playerIn, stack);
        }
        return InteractionResultHolder.success(stack);
    }

    public InteractionResult useOn(UseOnContext context) {
        IPlayerMagic magic = MNACap.get((IPlayerMagic)context.getPlayer(), PlayerMagicProvider.MAGIC).orElse(null);
        if (!magic.isMagicUnlocked()) {
            return InteractionResult.FAIL;
        }
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.getBlock() == BlockInit.RITUAL_TELEPORT_DESTINATION.get() && context.getPlayer().isCrouching()) {
            if (!context.getLevel().isClientSide()) {
                MNACap.get(context.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> {
                    List<ResourceLocation> pattern = m.getRitualTeleportBlockReagents(context.getClickedPos());
                    int curIndex = EnderDisk.getIndex(context.getItemInHand());
                    Component name = EnderDisk.getPatternName(context.getItemInHand(), curIndex);
                    ResourceLocation dimensionID = context.getLevel().dimension().location();
                    EnderDisk.setPattern(context.getItemInHand(), pattern, dimensionID, curIndex, name.getString());
                    context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.ender_disc.copied"));
                });
            }
            return InteractionResult.sidedSuccess((boolean)context.getLevel().isClientSide());
        }
        if (!this.openGuiIfModifierPressed(context.getItemInHand(), context.getPlayer(), context.getLevel())) {
            this.teleport(context.getLevel(), context.getPlayer(), context.getItemInHand());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add((Component)Component.translatable((String)"mna:ender_disc.set_to", (Object[])new Object[]{EnderDisk.getPatternName(stack, EnderDisk.getIndex(stack))}).withStyle(ChatFormatting.GOLD));
        IItemWithGui.super.appendHoverText(stack, worldIn, tooltip, flagIn);
        IRadialMenuItem.super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    private void teleport(Level world, Player player, ItemStack stack) {
        if (world.isClientSide) {
            float particle_spread = 1.0f;
            float v = 1.0f;
            int particleCount = 25;
            for (int i = 0; i < particleCount; ++i) {
                Vec3 velocity = new Vec3(0.0, Math.random() * (double)v, 0.0);
                world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get())), player.position().x() + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), player.position().y() + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), player.position().z() + ((double)(-particle_spread) + Math.random() * (double)particle_spread * 2.0), velocity.x, velocity.y, velocity.z);
            }
            return;
        }
        ArrayList<ResourceLocation> pattern = EnderDisk.getPattern(stack);
        if (pattern.size() == 0) {
            return;
        }
        ResourceLocation dimensionID = EnderDisk.getDimension(stack);
        ServerLevel resolvedWorld = TeleportHelper.resolveRegistryKey((ServerLevel)world, dimensionID);
        ServerLevel targetWorld = resolvedWorld != null ? resolvedWorld : (ServerLevel)world;
        MNACap.get(targetWorld, WorldMagicProvider.MAGIC).ifPresent(worldMagicContainer -> {
            IRitualTeleportLocation teleportPosition = worldMagicContainer.getRitualTeleportBlockLocation(pattern, (ResourceKey<Level>)world.dimension());
            if (teleportPosition == null) {
                return;
            }
            if (!teleportPosition.getWorldType().equals(targetWorld.dimension())) {
                player.sendSystemMessage((Component)Component.translatable((String)"mna:rituals/return.wrong_dimension"));
                return;
            }
            ((ServerLevel)world).playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, (float)((double)0.7f + Math.random() * (double)0.3f));
            targetWorld.playSound(null, teleportPosition.getPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, (float)((double)0.7f + Math.random() * (double)0.3f));
            TeleportHelper.teleportEntity((Entity)player, (ResourceKey<Level>)targetWorld.dimension(), Vec3.atBottomCenterOf((Vec3i)teleportPosition.getPos().above()));
        });
    }

    private static CompoundTag getCurrentCompound(ItemStack stack, int index) {
        ListTag list;
        if (stack == null || !stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_NBT)) {
            return null;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(KEY_NBT, 10)) {
            return tag.getCompound(KEY_NBT);
        }
        if (tag.contains(KEY_NBT, 9) && index < (list = (ListTag)tag.get(KEY_NBT)).size()) {
            return list.getCompound(index);
        }
        return null;
    }

    public static ArrayList<ResourceLocation> getPattern(ItemStack stack) {
        return EnderDisk.getPattern(stack, EnderDisk.getIndex(stack));
    }

    public static ArrayList<ResourceLocation> getPattern(ItemStack stack, int index) {
        ArrayList<ResourceLocation> output = new ArrayList<ResourceLocation>(8);
        CompoundTag nbt = EnderDisk.getCurrentCompound(stack, index);
        if (nbt != null) {
            int count = Math.min(nbt.getInt(KEY_COUNT), 8);
            for (int i = 0; i < count; ++i) {
                output.add(ResourceLocation.parse(nbt.getString(KEY_ENTRY_PREFIX + i)));
            }
        }
        while (output.size() < 8) {
            output.add(ResourceLocation.parse("minecraft:air"));
        }
        return output;
    }

    public static ResourceLocation getDimension(ItemStack stack) {
        return EnderDisk.getDimension(stack, EnderDisk.getIndex(stack));
    }

    public static ResourceLocation getDimension(ItemStack stack, int index) {
        CompoundTag nbt = EnderDisk.getCurrentCompound(stack, index);
        if (nbt == null || !nbt.contains(KEY_DIMENSION)) {
            return ResourceLocation.parse("mna:dimension_current");
        }
        return ResourceLocation.parse(nbt.getString(KEY_DIMENSION));
    }

    public static Component getPatternName(ItemStack stack, int index) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = null;
        if (!tag.contains(KEY_NBT, 9)) {
            return Component.translatable((String)"mna:ender_disc.unused");
        }
        list = tag.getList(KEY_NBT, 10);
        CompoundTag nbt = null;
        if (index >= list.size()) {
            return Component.translatable((String)"mna:ender_disc.unused");
        }
        nbt = list.getCompound(index);
        String str = nbt.getString(KEY_NAME);
        if (str.length() > 0) {
            return Component.literal((String)str);
        }
        return Component.translatable((String)"mna:ender_disc.unused");
    }

    public static void setPattern(ItemStack stack, List<ResourceLocation> locations, ResourceLocation dimensionID, int index, String name) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = null;
        list = tag.contains(KEY_NBT, 9) ? tag.getList(KEY_NBT, 10) : new ListTag();
        while (list.size() < 8) {
            list.add(new CompoundTag());
        }
        CompoundTag nbt = null;
        nbt = index < list.size() ? list.getCompound(index) : new CompoundTag();
        nbt.putInt(KEY_COUNT, locations.size());
        nbt.putString(KEY_NAME, name);
        nbt.putString(KEY_DIMENSION, dimensionID != null ? dimensionID.toString() : "");
        int count = 0;
        for (ResourceLocation loc : locations) {
            nbt.putString(KEY_ENTRY_PREFIX + count++, loc.toString());
        }
        list.set(index, (Tag)nbt);
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().put(KEY_NBT, (Tag)list);
    }

    public static void setIndex(ItemStack stack, int index) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_INDEX, index);
    }

    public static int getIndex(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_INDEX);
        }
        return 0;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.ender_disc_armature.idle")))});
    }
}


