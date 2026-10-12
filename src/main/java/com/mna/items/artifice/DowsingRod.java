 
package com.mna.items.artifice;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.capabilities.WellspringNode;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.items.IPositionalItem;
import com.mna.api.items.TieredItem;
import com.mna.api.sound.SFX;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.items.ItemInit;
import com.mna.items.ritual.PlayerCharm;
import com.mna.sound.ItemInUseLoopingSound;
import com.mna.tools.math.MathUtils;
import java.util.HashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import com.mna.api.tools.MNACap;

public class DowsingRod
extends TieredItem {
    private static final float MANA_PER_TICK_WELLSPRING = 5.0f;
    private static final float MANA_PER_TICK_BED = 2.0f;
    private static final float MANA_PER_TICK_PLAYER = 20.0f;
    private static final String KEY_STORED_POINT = "stored_point";
    private static final String KEY_MODE = "mode";

    public DowsingRod() {
        super(new Item.Properties());
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public int getUseDuration(ItemStack stack) {
        return 999999;
    }

    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int ticks) {
        if (!(user instanceof Player)) {
            return;
        }
        Modes mode = this.getMode(stack);
        float manaCost = 2.0f;
        switch (mode) {
            case Wellspring: {
                manaCost = 5.0f;
                break;
            }
            case Player: {
                manaCost = 20.0f;
                break;
            }
            case Bed: 
            case Point: {
                manaCost = 2.0f;
            }
        }
        Player player = (Player)user;
        boolean manaConsumed = this.consumeMana(player, manaCost);
        if (!manaConsumed) {
            player.releaseUsingItem();
            return;
        }
        BlockPos trackPos = null;
        ItemStack offhand = user.getUsedItemHand() == InteractionHand.MAIN_HAND ? user.getItemInHand(InteractionHand.OFF_HAND) : user.getItemInHand(InteractionHand.MAIN_HAND);
        switch (mode) {
            case Wellspring: {
                trackPos = this.trackWellspring(player, world, stack);
                break;
            }
            case Player: {
                trackPos = this.trackPlayer(player, world, stack, offhand);
                break;
            }
            case Bed: 
            case Point: {
                trackPos = this.trackCachedPoint(player, world, stack);
            }
        }
        if (trackPos != null) {
            Vec3 closestPos = new Vec3((double)trackPos.getX() + 0.5, player.getY() + (double)player.getEyeHeight(), (double)trackPos.getZ() + 0.5);
            if (world.isClientSide) {
                MathUtils.rotateEntityLookTowards(EntityAnchorArgument.Anchor.EYES, (LivingEntity)player, closestPos, 5.0f);
            }
        } else if (!world.isClientSide) {
            player.sendSystemMessage((Component)Component.translatable((String)"item.mna.dowsing_rod.no_target"));
            player.releaseUsingItem();
        }
    }

    private boolean consumeMana(Player player, float amount) {
        MutableBoolean consumeSuccess = new MutableBoolean(true);
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (!m.isMagicUnlocked() || !m.getCastingResource().hasEnoughAbsolute((LivingEntity)player, amount)) {
                consumeSuccess.setFalse();
                return;
            }
            m.getCastingResource().consume((LivingEntity)player, amount);
        });
        return consumeSuccess.booleanValue();
    }

    @Nullable
    private BlockPos trackWellspring(Player player, Level level, ItemStack stack) {
        MutableObject closestPos = new MutableObject(null);
        MNACap.get(level, WorldMagicProvider.MAGIC).ifPresent(wm -> {
            if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_STORED_POINT)) {
                closestPos.setValue(BlockPos.of((long)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getLong(KEY_STORED_POINT)));
            } else {
                HashMap<BlockPos, WellspringNode> nodes = wm.getWellspringRegistry().getNearbyNodes(player.blockPosition(), 0, (int)((double)GeneralConfigValues.WellspringDistance * 1.5));
                MutableDouble closestDist = new MutableDouble();
                nodes.keySet().forEach(bp -> {
                    double dist = bp.distSqr((Vec3i)player.blockPosition());
                    if (closestPos.getValue() == null || dist < closestDist.getValue()) {
                        closestPos.setValue(bp);
                        closestDist.setValue(dist);
                    }
                });
                if (closestPos.getValue() != null) {
                    stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putLong(KEY_STORED_POINT, ((BlockPos)closestPos.getValue()).asLong());
                }
            }
        });
        return (BlockPos)closestPos.getValue();
    }

    @Nullable
    private BlockPos trackPlayer(Player player, Level level, ItemStack stack, ItemStack offhand) {
        if (level.getGameTime() % 20L == 0L || !stack.has(DataComponents.CUSTOM_DATA) || !stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_STORED_POINT)) {
            PlayerCharm charm;
            Player target;
            if (offhand.getItem() instanceof PlayerCharm && (target = (charm = (PlayerCharm)offhand.getItem()).GetPlayerTarget(offhand, level)) != null) {
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putLong(KEY_STORED_POINT, target.blockPosition().asLong());
                return target.blockPosition();
            }
        } else if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_STORED_POINT)) {
            return BlockPos.of((long)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getLong(KEY_STORED_POINT));
        }
        return null;
    }

    private BlockPos trackCachedPoint(Player player, Level level, ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains(KEY_STORED_POINT)) {
            return BlockPos.of((long)stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getLong(KEY_STORED_POINT));
        }
        return null;
    }

    private Modes getMode(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            int mode = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY_MODE);
            return Modes.values()[mode % Modes.values().length];
        }
        return Modes.Bed;
    }

    private void setMode(ItemStack stack, Modes mode) {
        stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putInt(KEY_MODE, mode.ordinal());
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int ticks) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove(KEY_STORED_POINT);
            stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().remove(KEY_MODE);
        }
        if (user instanceof Player) {
            ((Player)user).getCooldowns().addCooldown((Item)this, 20);
        }
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        MutableBoolean magicUnlocked = new MutableBoolean(true);
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (!m.isMagicUnlocked()) {
                if (world.isClientSide) {
                    player.sendSystemMessage((Component)Component.translatable((String)"item.mna.dowsing_rod.confusion"));
                }
                player.getCooldowns().addCooldown((Item)this, 100);
                magicUnlocked.setFalse();
            }
        });
        ItemStack itemstack = player.getItemInHand(hand);
        if (magicUnlocked.getValue().booleanValue()) {
            Modes mode = null;
            if (world.isClientSide) {
                mode = Modes.Bed;
                this.PlayLoopingSound(SFX.Loops.MANAWEAVING, player);
            } else {
                ItemStack offhandStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
                if (offhandStack.getItem() instanceof IPositionalItem) {
                    IPositionalItem markItem = (IPositionalItem)offhandStack.getItem();
                    DirectionalPoint point = markItem.getDirectionalPoint(offhandStack);
                    if (point != null && point.getPosition() != null) {
                        mode = Modes.Point;
                        itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putLong(KEY_STORED_POINT, point.getPosition().asLong());
                    } else {
                        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.rune_marking.noPosition"));
                    }
                } else if (offhandStack.getItem() instanceof PlayerCharm) {
                    PlayerCharm charm = (PlayerCharm)offhandStack.getItem();
                    if (charm.GetPlayerTarget(offhandStack, world) != null) {
                        mode = Modes.Player;
                    } else if (charm.getPlayerUUID(offhandStack) == null) {
                        player.sendSystemMessage((Component)Component.translatable((String)"mna:rituals/summon.not_found"));
                    } else {
                        player.sendSystemMessage((Component)Component.translatable((String)"mna:rituals/summon.wrong_dimension"));
                    }
                } else if (offhandStack.getItem() == ItemInit.CHIMERITE_GEM.get()) {
                    mode = Modes.Wellspring;
                } else if (!world.dimension().equals(((ServerPlayer)player).getRespawnDimension())) {
                    player.sendSystemMessage((Component)Component.translatable((String)"item.mna.bed_charm.dimension-wrong"));
                } else {
                    BlockPos bedPos = ((ServerPlayer)player).getRespawnPosition();
                    if (bedPos == null) {
                        player.sendSystemMessage((Component)Component.translatable((String)"item.mna.bed_charm.no-bed"));
                    } else {
                        mode = Modes.Bed;
                        itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putLong(KEY_STORED_POINT, bedPos.asLong());
                    }
                }
            }
            if (mode != null) {
                if (!world.isClientSide) {
                    this.setMode(itemstack, mode);
                    player.startUsingItem(hand);
                }
                return InteractionResultHolder.consume(itemstack);
            }
            if (!world.isClientSide) {
                player.sendSystemMessage((Component)Component.translatable((String)"item.mna.dowsing_rod.no_target"));
            }
        }
        player.getCooldowns().addCooldown((Item)this, 20);
        return InteractionResultHolder.fail(itemstack);
    }

    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return true;
    }

    @OnlyIn(value=Dist.CLIENT)
    private void PlayLoopingSound(SoundEvent soundID, Player player) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new ItemInUseLoopingSound(soundID, player));
    }

    private static enum Modes {
        Bed,
        Wellspring,
        Player,
        Point;

    }
}


