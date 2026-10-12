 
package com.mna.items.armor;

import com.mna.ManaAndArtifice;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.items.ITieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.tools.RLoc;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.items.armor.IBrokenArmorReplaceable;
import com.mna.items.armor.ISetItem;
import com.mna.items.renderers.FeyArmorRenderer;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.mna.api.tools.MNACap;

public class FeyArmorItem
extends ArmorItem
implements GeoItem,
AnimationController.SoundKeyframeHandler<FeyArmorItem>,
ISetItem,
ITieredItem<FeyArmorItem>,
IFactionSpecific,
IBrokenArmorReplaceable<FeyArmorItem> {
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private static final ResourceLocation fey_armor_set_bonus = RLoc.create("fey_armor_set_bonus");
    public static final String fey_armor_set_bonus_key = "fey_armor_set_bonus";
    private static final float FLIGHT_MANA_COST_PER_TICK = 0.75f;
    private static final float HOVER_MANA_COST_PER_TICK = 0.25f;
    private int _tier = -1;
    public static LivingEntity renderEntity;

    public FeyArmorItem(net.minecraft.core.Holder<ArmorMaterial> materialIn, ArmorItem.Type slot, Item.Properties builder) {
        super(materialIn, slot, builder.rarity(Rarity.EPIC).durability(slot.getDurability(33)));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private GeoArmorRenderer<?> renderer;

            @NotNull
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new FeyArmorRenderer();
                }
                this.renderer.prepForRender((Entity)livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, state -> {
            LivingEntity patt4126$temp;
            state.getController().transitionLength(15);
            if (renderEntity != null && (patt4126$temp = renderEntity) instanceof Player) {
                Player player = (Player)patt4126$temp;
                if (player.isFallFlying()) {
                    player.getPersistentData().putBoolean("wingsout", true);
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.fey_armor.flying"));
                }
                if (player.getAbilities().flying || player.getPersistentData().contains("wingsout")) {
                    if (player.onGround()) {
                        player.getPersistentData().remove("wingsout");
                    } else {
                        player.getPersistentData().putBoolean("wingsout", true);
                    }
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.fey_armor.hovering"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.fey_armor.notflying"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.fey_armor.notflying"));
        });
        controller.setSoundKeyframeHandler((AnimationController.SoundKeyframeHandler)this);
        registrar.add(new AnimationController[]{controller});
    }

    public void handle(SoundKeyframeEvent<FeyArmorItem> event) {
        Player player;
        if (event.getKeyframeData().getSound().equals("flap") && (player = ManaAndArtifice.instance.proxy.getClientPlayer()) != null && renderEntity != null) {
            double dist = player.distanceTo((Entity)renderEntity);
            float volume = dist == 0.0 ? 1.0f : (float)Math.min(1.0, 1.0 / dist);
            renderEntity.playSound(SFX.Event.Player.WING_FLAP, volume, 1.0f);
        }
    }

    @Override
    public ResourceLocation getSetIdentifier() {
        return fey_armor_set_bonus;
    }

    public void onArmorTick(ItemStack stack, Level world, Player player) {
        this.usedByPlayer(player);
        if (stack.getItem() == ItemInit.FEY_ARMOR_CHEST.get() && this.isSetEquipped((LivingEntity)player)) {
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                ManaAndArtifice.instance.proxy.setFlightEnabled(player, m.getCastingResource().hasEnoughAbsolute((LivingEntity)player, 0.25f));
                if (player.isCreative() || player.isSpectator()) {
                    ManaAndArtifice.instance.proxy.setFlySpeed(player, 0.05f);
                } else {
                    ManaAndArtifice.instance.proxy.setFlySpeed(player, 0.02f);
                }
                if (player.getAbilities().flying) {
                    m.getCastingResource().consume((LivingEntity)player, 0.25f);
                    player.setSprinting(false);
                }
            });
            if (player.getHealth() < player.getMaxHealth() * 0.1f && !player.getCooldowns().isOnCooldown((Item)this)) {
                player.getCooldowns().addCooldown((Item)this, 6000);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3, 60));
            }
        }
    }

    @Override
    public void applySetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
        if (living instanceof Player) {
            living.getPersistentData().putBoolean(fey_armor_set_bonus_key, true);
        }
    }

    @Override
    public void removeSetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
        if (living instanceof Player) {
            living.getPersistentData().putBoolean(fey_armor_set_bonus_key, false);
            ManaAndArtifice.instance.proxy.setFlightEnabled((Player)living, false);
            ManaAndArtifice.instance.proxy.setFlySpeed((Player)living, 0.05f);
        }
    }

    @Override
    public int itemsForSetBonus() {
        return 4;
    }

    @Override
    public int getCachedTier() {
        return this._tier;
    }

    @Override
    public void setCachedTier(int tier) {
        this._tier = tier;
    }

    @Override
    public IFaction getFaction() {
        return Factions.FEY;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ISetItem.super.addSetTooltip(tooltip);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        IPlayerMagic magic;
        if (!(entity instanceof Player)) {
            return false;
        }
        if (flightTicks % 100 == 0) {
            this.usedByPlayer((Player)entity);
        }
        if ((magic = MNACap.get((IPlayerMagic)((Player)entity), PlayerMagicProvider.MAGIC).orElse(null)) == null || !magic.getCastingResource().hasEnoughAbsolute(entity, 0.75f)) {
            return false;
        }
        Vec3 look = entity.getLookAngle();
        Vec3 motion = null;
        if (!entity.isShiftKeyDown()) {
            magic.getCastingResource().consume(entity, 0.75f);
            motion = entity.getDeltaMovement();
            float maxLength = 1.75f;
            lookScale = 0.06;
            scaled_look = look.scale(lookScale);
            motion = motion.add(scaled_look);
            if (motion.length() > (double)maxLength) {
                motion = motion.scale((double)maxLength / motion.length());
            }
            entity.setDeltaMovement(motion);
        } else {
            magic.getCastingResource().consume(entity, 0.5625f);
            motion = entity.getDeltaMovement();
            float minLength = 0.1f;
            lookScale = -0.01;
            scaled_look = look.scale(lookScale);
            motion = motion.add(scaled_look);
            if (motion.length() < (double)minLength) {
                motion = motion.scale((double)minLength / motion.length());
            }
            entity.setDeltaMovement(motion);
        }
        if (entity.level().isClientSide()) {
            Vec3 pos = entity.position().add(look.scale(3.0));
            for (int i = 0; i < 5; ++i) {
                entity.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10), pos.x - 0.5 + Math.random(), pos.y - 0.5 + Math.random(), pos.z - 0.5 + Math.random(), -look.x, -look.y, -look.z);
            }
        }
        return true;
    }

    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return entity instanceof Player && !entity.isInWaterOrBubble() && !entity.isInLava() && this.isSetEquipped(entity) && MNACap.get((Player)entity, PlayerMagicProvider.MAGIC).isPresent() && (MNACap.get((IPlayerMagic)((Player)entity), PlayerMagicProvider.MAGIC).orElse(null)).getCastingResource().hasEnoughAbsolute(entity, 0.75f);
    }

    public static boolean randomReflectChance() {
        return Math.random() < GeneralConfigValues.DruidicReflectChance;
    }

    public static boolean randomTeleportChance() {
        return Math.random() < GeneralConfigValues.DruidicTeleportChance;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return IBrokenArmorReplaceable.super.damageItem(stack, amount * 3, entity, onBroken);
    }
}


