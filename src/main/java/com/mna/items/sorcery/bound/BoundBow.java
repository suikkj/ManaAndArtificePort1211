 
package com.mna.items.sorcery.bound;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import java.util.function.Supplier;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.sorcery.targeting.BoundBowProjectile;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.items.base.INoCreativeTab;
import com.mna.items.renderers.bound.BoundBowItemRenderer;
import com.mna.items.sorcery.bound.IBoundItem;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.SpellRecipe;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import org.apache.commons.lang3.mutable.MutableObject;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotTypePreset;
import com.mna.api.tools.MNACap;

public class BoundBow
extends BowItem
implements IBoundItem,
INoCreativeTab {
    private static final RawAnimation DRAW = RawAnimation.begin().thenPlay("animation.bound_bow.draw").thenLoop("animation.bound_bow.draw_idle");
    private static final RawAnimation FIRE = RawAnimation.begin().thenPlay("animation.bound_bow.fire").thenLoop("animation.bound_bow.idle");
    private static final String controller_id = "bound_bow_fire";
    AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public BoundBow() {
        super(new Item.Properties());
        // [PORT] GeckoLibNetwork removido no GeckoLib 4.x
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private final Supplier<BlockEntityWithoutLevelRenderer> ister = () -> new BoundBowItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return (BlockEntityWithoutLevelRenderer)this.ister.get();
            }
        });
    }

    public int getDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public float getPassiveManaDrain(Entity entity) {
        if (entity instanceof Player) {
            Player p = (Player)entity;
            if (CuriosInterop.IsItemInCurioSlot((Item)ItemInit.BATTLEMAGE_AMULET.get(), (LivingEntity)p, SlotTypePreset.NECKLACE)) {
                return 0.0f;
            }
        }
        return 0.2f;
    }

    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        this.handleInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        SpellRecipe recipe = this.getRecipe(stack);
        recipe.addItemTooltip(stack, worldIn.level(), tooltip, (Player)mc.player);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public ItemStack createFromSpell(LivingEntity caster, ItemStack original, ISpellDefinition recipe) {
        return IBoundItem.super.createFromSpell(caster, (Item)this, original, recipe);
    }

    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        ItemStack restored = this.restoreItem(item);
        player.getInventory().setItem(player.getInventory().selected, restored);
        return false;
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public int manaPerShot(Player player, InteractionHand hand, ItemStack stack) {
        SpellRecipe recipe = this.getRecipe(stack);
        SpellCaster.applyAdjusters(stack, (LivingEntity)player, hand, false, recipe, SpellCastStage.CALCULATING_MANA_COST);
        return (int)recipe.getManaCost();
    }

    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);
        MutableObject returnValue = new MutableObject(InteractionResultHolder.fail(itemstack));
        SpellRecipe recipe = SpellRecipe.fromNBT(itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (!this.canCastSpell(recipe, (LivingEntity)playerIn, handIn)) {
            return InteractionResultHolder.fail(itemstack);
        }
        MNACap.get(playerIn, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            if (playerIn.getAbilities().instabuild || m.getCastingResource().hasEnough((LivingEntity)playerIn, this.manaPerShot(playerIn, handIn, itemstack))) {
                playerIn.startUsingItem(handIn);
                if (worldIn instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)worldIn;
                    this.triggerAnim((Entity)playerIn, GeoItem.getOrAssignId((ItemStack)itemstack, (ServerLevel)serverLevel), controller_id, "draw");
                }
                returnValue.setValue(InteractionResultHolder.consume(itemstack));
            }
        });
        return (InteractionResultHolder)returnValue.getValue();
    }

    public static float getArrowVelocity(int charge) {
        float f = (float)charge / 20.0f;
        if ((f = (f * f + f * 2.0f) / 3.0f) > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player) {
            int i;
            Player playerentity = (Player)entityLiving;
            if (worldIn instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)worldIn;
                this.triggerAnim((Entity)playerentity, GeoItem.getOrAssignId((ItemStack)stack, (ServerLevel)serverLevel), controller_id, "fire");
            }
            if ((i = this.getUseDuration(stack) - timeLeft) < 0) {
                return;
            }
            float f = BoundBow.getArrowVelocity(i);
            if (!((double)f < 0.1)) {
                MNACap.get(playerentity, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                    int mana = this.manaPerShot(playerentity, InteractionHand.MAIN_HAND, stack);
                    if (m.getCastingResource().hasEnough(entityLiving, mana)) {
                        SpellRecipe recipe = this.getRecipe(stack);
                        if (!worldIn.isClientSide) {
                            BoundBowProjectile shot = new BoundBowProjectile((EntityType<? extends AbstractArrow>)((EntityType)EntityInit.BOUND_BOW_PROJECTILE.get()), worldIn);
                            shot.setPos(playerentity.getX(), playerentity.getY() + (double)playerentity.getEyeHeight(), playerentity.getZ());
                            shot.setOwner((Entity)playerentity);
                            shot.shootFromRotation((Entity)playerentity, playerentity.getXRot(), playerentity.getYRot(), 0.0f, f * 3.0f, 1.0f);
                            shot.setSpell(recipe);
                            worldIn.addFreshEntity((Entity)shot);
                        }
                        m.getCastingResource().consume((LivingEntity)playerentity, mana);
                        IPlayerProgression progression = (IPlayerProgression)playerentity.getData(PlayerProgressionProvider.PROGRESSION.get());
                        if (progression != null && !recipe.canFactionCraft(progression)) {
                            this.getRecipe(stack).usedByPlayer(playerentity);
                        }
                        worldIn.playSound((Player)null, playerentity.getX(), playerentity.getY(), playerentity.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f / (worldIn.random.nextFloat() * 0.4f + 1.2f) + f * 0.5f);
                        playerentity.getCooldowns().addCooldown((Item)this, (int)((float)recipe.getCooldown((LivingEntity)playerentity) * 0.75f));
                    }
                });
            }
        }
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, controller_id, 0, state -> PlayState.STOP).triggerableAnim("draw", DRAW).triggerableAnim("fire", FIRE)});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}


