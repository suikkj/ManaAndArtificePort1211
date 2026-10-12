 
package com.mna.items.manaweaving;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.KeybindInit;
import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IShowHud;
import com.mna.api.items.TieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.blocks.BlockInit;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.manaweaving.Manaweave;
import com.mna.gui.containers.providers.NamedCantrips;
import com.mna.items.base.IItemWithGui;
import com.mna.items.base.IRadialMenuItem;
import com.mna.network.ClientMessageDispatcher;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import com.mna.sound.ItemInUseLoopingSound;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.joml.Vector3f;
import com.mna.api.tools.MNACap;

public class ItemManaweaverWand
extends TieredItem
implements IShowHud,
IItemWithGui<ItemManaweaverWand>,
IRadialMenuItem {
    public static final float MANA_COST_PER_TICK = 1.0f;
    private static final int AUTOWEAVE_TICKS_REQUIRED = 30;
    private static final String KEY_STORED_ALTAR = "altar_location";
    private static final String KEY_MODEL_DATA = "CustomModelData";
    private static final String KEY_SELECTED_PATTERN = "SelectedPattern";

    public ItemManaweaverWand() {
        this(new Item.Properties().stacksTo(1));
    }

    public ItemManaweaverWand(Item.Properties props) {
        super(props);
    }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return armorType == EquipmentSlot.MAINHAND;
    }

    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player)) {
            return;
        }
        Player player = (Player)entityLiving;
        if (player.getCooldowns().isOnCooldown((Item)this)) {
            return;
        }
        if (worldIn.isClientSide) {
            IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
            IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
            if (magic != null && progression != null) {
                Vector3f[] points = magic.getRememberedPoints();
                Vector3f[] looks = magic.getRememberedLooks();
                int useTicks = this.getUseDuration(stack) - timeLeft;
                if (points.length > 0) {
                    ManaweavingPattern pattern = ItemManaweaverWand.getStoredPattern(worldIn.level(), stack);
                    if (pattern == null) {
                        pattern = ManaweavingPattern.match(worldIn, progression.getTier(), points, looks);
                    } else if (useTicks < ItemManaweaverWand.getAutoweaveTicks((LivingEntity)player)) {
                        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.channel_too_short"));
                        return;
                    }
                    if (pattern == null) {
                        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.not_recognized"));
                    } else {
                        if (pattern.getTier() > progression.getTier()) {
                            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.low_tier"));
                            return;
                        }
                        double avgX = 0.0;
                        double avgZ = 0.0;
                        double minY = Double.MAX_VALUE;
                        for (Vector3f vec : points) {
                            avgX += (double)vec.x();
                            avgZ += (double)vec.z();
                            if (!((double)vec.y() < minY)) continue;
                            minY = vec.y();
                        }
                        ClientMessageDispatcher.sendManaweavePatternDrawn(player, pattern.getId(), new Vec3(avgX /= (double)points.length, minY - 0.4, avgZ /= (double)points.length), player.getUsedItemHand(), useTicks);
                    }
                }
            }
        }
        player.getCooldowns().addCooldown(stack.getItem(), 5);
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        MutableBoolean success = new MutableBoolean(true);
        MNACap.get(playerIn, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (m.isMagicUnlocked()) {
                if (!this.openGuiIfModifierPressed(itemstack, playerIn, worldIn)) {
                    if (worldIn.isClientSide) {
                        m.clearRememberedPoints();
                        this.PlayLoopingSound(SFX.Loops.MANAWEAVING, playerIn);
                    }
                    ManaweavingPattern pattern = ItemManaweaverWand.getStoredPattern(worldIn, itemstack);
                    java.util.Optional<PlayerProgressionProvider> progression = MNACap.get(playerIn, PlayerProgressionProvider.PROGRESSION);
                    if (pattern != null && progression.isPresent() && ((IPlayerProgression)progression.get()).getTier() < pattern.getTier()) {
                        ItemManaweaverWand.setStoredPattern(itemstack, null);
                    }
                    playerIn.startUsingItem(handIn);
                } else {
                    success.setFalse();
                }
            } else {
                if (worldIn.isClientSide) {
                    playerIn.sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.confusion"));
                }
                playerIn.getCooldowns().addCooldown((Item)this, 100);
            }
        });
        if (success.getValue().booleanValue()) {
            return InteractionResultHolder.consume(itemstack);
        }
        return InteractionResultHolder.fail(itemstack);
    }

    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer().isCrouching()) {
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            if (state.getBlock() == BlockInit.MANAWEAVING_ALTAR.get()) {
                if (!context.getLevel().isClientSide()) {
                    stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().put(KEY_STORED_ALTAR, (Tag)NbtUtils.writeBlockPos((BlockPos)context.getClickedPos()));
                    stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_MODEL_DATA, 1);
                    context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.position_stored"));
                }
                return InteractionResult.FAIL;
            }
            if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_STORED_ALTAR)) {
                if (!context.getLevel().isClientSide()) {
                    stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove(KEY_STORED_ALTAR);
                    stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove(KEY_MODEL_DATA);
                    context.getPlayer().sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.position_cleared"));
                }
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    public static BlockPos getStoredBlockPos(ItemStack stack) {
        if (stack.getItem() instanceof ItemManaweaverWand && stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_STORED_ALTAR)) {
            return NbtUtils.readBlockPos(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), KEY_STORED_ALTAR).orElse(null);
        }
        return null;
    }

    @Nullable
    public static ManaweavingPattern getStoredPattern(Level world, ItemStack stack) {
        ResourceLocation rLoc = ItemManaweaverWand.getStoredPatternID(world, stack);
        if (rLoc != null) {
            return ManaweavingPatternHelper.GetManaweavingRecipe(world, rLoc);
        }
        return null;
    }

    @Nullable
    public static ResourceLocation getStoredPatternID(Level world, ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_SELECTED_PATTERN)) {
            return null;
        }
        ResourceLocation rLoc = ResourceLocation.parse(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(KEY_SELECTED_PATTERN));
        return rLoc;
    }

    public static void setStoredPattern(ItemStack stack, ResourceLocation rLoc) {
        if (rLoc == null) {
            stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove(KEY_SELECTED_PATTERN);
        } else {
            stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putString(KEY_SELECTED_PATTERN, rLoc.toString());
        }
    }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        Manaweave mw;
        if (entity instanceof Manaweave && !(mw = (Manaweave)entity).isMerging()) {
            IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
            if (magic != null) {
                magic.getCastingResource().restore(mw.getManaReturn(player));
                magic.getCastingResource().setNeedsSync();
            }
            player.level().playSound(player, player.blockPosition(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0f, 1.0f);
            mw.remove(Entity.RemovalReason.DISCARDED);
            return false;
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    public int getUseDuration(ItemStack stack) {
        return 999999;
    }

    public void onUseTick(Level pLevel, LivingEntity player, ItemStack stack, int count) {
        java.util.Optional<PlayerMagicProvider> magicContainer = MNACap.get(player, PlayerMagicProvider.MAGIC);
        java.util.Optional<PlayerProgressionProvider> progContainer = MNACap.get(player, PlayerProgressionProvider.PROGRESSION);
        if (magicContainer.isPresent()) {
            IPlayerMagic magic = (IPlayerMagic)magicContainer.get();
            if (!magic.isMagicUnlocked()) {
                player.releaseUsingItem();
                return;
            }
            if (!magic.getCastingResource().hasEnoughAbsolute(player, 1.0f)) {
                if (player instanceof Player) {
                    ((Player)player).getCooldowns().addCooldown((Item)this, 100);
                    if (player.level().isClientSide() && player == ManaAndArtifice.instance.proxy.getClientPlayer()) {
                        ((Player)player).sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.oom"));
                    }
                }
                player.releaseUsingItem();
                return;
            }
            if (player.level().isClientSide()) {
                int ticksUsed;
                int[] color;
                IFaction faction;
                Vec3 eyePos = player.getEyePosition(0.0f);
                Vec3 look = player.getLookAngle();
                Vec3 position = eyePos.add(look);
                Vec3 particlePosition = eyePos.add(look.scale(1.5));
                ((IPlayerMagic)magicContainer.get()).addRememberedPoint(position.toVector3f(), new Vector3f(Mth.wrapDegrees((float)player.getYRot()), Mth.wrapDegrees((float)player.getXRot()), 0.0f));
                MAParticleType particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_STATIONARY.get())).setMaxAge(40);
                if (progContainer.isPresent() && (faction = ((IPlayerProgression)progContainer.get()).getAlliedFaction()) != null && (color = faction.getManaweaveRGB()) != null) {
                    particle.setColor(color[0], color[1], color[2]);
                }
                player.level().addParticle((ParticleOptions)particle, particlePosition.x, particlePosition.y, particlePosition.z, 0.0, 0.0, 0.0);
                ManaweavingPattern pattern = ItemManaweaverWand.getStoredPattern(player.level(), stack);
                if (pattern != null && (ticksUsed = this.getUseDuration(stack) - count) == ItemManaweaverWand.getAutoweaveTicks(player)) {
                    player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }
            magic.getCastingResource().consume(player, 1.0f);
        }
    }

    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    @OnlyIn(value=Dist.CLIENT)
    private void PlayLoopingSound(SoundEvent soundID, Player player) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new ItemInUseLoopingSound(soundID, player));
    }

    @Override
    public MenuProvider getProvider(ItemStack stack) {
        return new NamedCantrips();
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ManaweavingPattern stored = ItemManaweaverWand.getStoredPattern(worldIn.level(), stack);
        if (stored != null) {
            tooltip.add((Component)Component.translatable((String)"item.mna.manaweaver_wand.auto", (Object[])new Object[]{Component.translatable((String)stored.getId().toString()).getString()}).withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add((Component)Component.translatable((String)"item.mna.manaweaver_wand.manual").withStyle(ChatFormatting.GOLD));
        }
        IRadialMenuItem.super.appendHoverText(stack, worldIn, tooltip, flagIn);
        String txt = I18n.get((String)((KeyMapping)KeybindInit.InventoryItemOpen.get()).getKey().getName(), (Object[])new Object[0]);
        tooltip.add((Component)Component.translatable((String)"item.mna.manaweaver_wand.open-with", (Object[])new Object[]{txt}).withStyle(ChatFormatting.AQUA));
    }

    public static int getAutoweaveTicks(LivingEntity living) {
        if (!(living instanceof Player)) {
            return 30;
        }
        Player player = (Player)living;
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        int ticks = 30;
        if (progression != null) {
            ticks -= 5 * progression.getTier();
        }
        return ticks;
    }
}


