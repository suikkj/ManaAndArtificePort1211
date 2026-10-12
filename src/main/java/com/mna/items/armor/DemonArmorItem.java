 
package com.mna.items.armor;

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
import com.mna.effects.EffectInit;
import com.mna.entities.utility.MAExplosion;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.items.armor.IBrokenArmorReplaceable;
import com.mna.items.armor.ISetItem;
import com.mna.items.renderers.DemonArmorRenderer;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DemonArmorItem
extends ArmorItem
implements GeoItem,
ISetItem,
ITieredItem<DemonArmorItem>,
IFactionSpecific,
IBrokenArmorReplaceable<DemonArmorItem> {
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private static final ResourceLocation demon_armor_set_bonus = RLoc.create("demon_armor_set_bonus");
    public static final String demon_armor_set_bonus_key = "demon_armor_set_bonus";
    public static final String demon_armor_meteor_jumping_key = "demon_armor_meteor_jumping";
    public static final String demon_armor_sprint_time_key = "demon_armor_sprint_time";
    public static final String demon_armor_run_speed_1 = "demon_armor_set_bonus_1";
    public static final String demon_armor_run_speed_2 = "demon_armor_set_bonus_2";
    public static final String demon_armor_run_speed_3 = "demon_armor_set_bonus_3";
    public static final AttributeModifier runSpeed_1 = new AttributeModifier(ResourceLocation.parse("demon_armor_set_bonus_1"), (double)0.05f, AttributeModifier.Operation.ADD_VALUE);
    public static final AttributeModifier runSpeed_2 = new AttributeModifier(ResourceLocation.parse("demon_armor_set_bonus_2"), (double)0.1f, AttributeModifier.Operation.ADD_VALUE);
    public static final AttributeModifier runSpeed_3 = new AttributeModifier(ResourceLocation.parse("demon_armor_set_bonus_3"), (double)0.1f, AttributeModifier.Operation.ADD_VALUE);
    private static final float MANA_COST_PER_TICK = 0.25f;
    private static final float METEOR_JUMP_MANA_COST = 40.0f;
    private int _tier = -1;

    public DemonArmorItem(net.minecraft.core.Holder<ArmorMaterial> materialIn, ArmorItem.Type slot, Item.Properties builder) {
        super(materialIn, slot, builder.rarity(Rarity.EPIC).durability(slot.getDurability(33)));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private GeoArmorRenderer<?> renderer;

            @NotNull
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new DemonArmorRenderer();
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
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> PlayState.STOP)});
    }

    @Override
    public ResourceLocation getSetIdentifier() {
        return demon_armor_set_bonus;
    }

    public void onArmorTick(ItemStack stack, Level world, Player player) {
        this.usedByPlayer(player);
        if (stack.isDamaged() && player.isOnFire() && player.level().getGameTime() % 10L == 0L) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
        if (this.isSetEquipped((LivingEntity)player)) {
            MobEffectInstance eff;
            if (stack.getItem() != ItemInit.DEMON_ARMOR_CHEST.get()) {
                return;
            }
            IPlayerMagic magic = (IPlayerMagic)player.getData(PlayerMagicProvider.MAGIC.get());
            if (magic == null) {
                return;
            }
            boolean showParticles = false;
            boolean removeModifier = true;
            if (player.isSprinting() && magic.getCastingResource().hasEnoughAbsolute((LivingEntity)player, 0.25f)) {
                int sprint_time = player.getPersistentData().getInt(demon_armor_sprint_time_key);
                AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (sprint_time == 0) {
                    if (!attr.hasModifier(runSpeed_1)) {
                        attr.addTransientModifier(runSpeed_1);
                        player.playSound(SFX.Event.Artifact.DEMON_ARMOR_SPRINT_START, 1.0f, 0.8f);
                    }
                } else if (sprint_time == 30) {
                    if (!attr.hasModifier(runSpeed_2)) {
                        attr.addTransientModifier(runSpeed_2);
                        player.playSound(SFX.Event.Artifact.DEMON_ARMOR_SPRINT_START, 1.0f, 1.0f);
                    }
                } else if (sprint_time == 60 && !attr.hasModifier(runSpeed_3)) {
                    attr.addTransientModifier(runSpeed_3);
                    player.playSound(SFX.Event.Artifact.DEMON_ARMOR_SPRINT_START, 1.0f, 1.2f);
                }
                player.getPersistentData().putInt(demon_armor_sprint_time_key, ++sprint_time);
                magic.getCastingResource().consume((LivingEntity)player, 0.25f);
                removeModifier = false;
                showParticles = true;
            }
            if (removeModifier) {
                player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(runSpeed_1);
                player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(runSpeed_2);
                player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(runSpeed_3);
                player.getPersistentData().remove(demon_armor_sprint_time_key);
            }
            if (!player.onGround() && player.getDeltaMovement().y < 0.0 && player.isCrouching() && magic.getCastingResource().hasEnough((LivingEntity)player, 40.0f) && !player.getPersistentData().contains(demon_armor_meteor_jumping_key)) {
                int heightAboveGround;
                int reqHeight = 5;
                BlockPos pos = player.blockPosition();
                for (heightAboveGround = 0; player.level().isEmptyBlock(pos) && heightAboveGround < reqHeight; ++heightAboveGround) {
                    pos = pos.below();
                }
                if (heightAboveGround >= reqHeight) {
                    player.getPersistentData().putBoolean(demon_armor_meteor_jumping_key, true);
                    player.playSound(SFX.Event.Artifact.METEOR_JUMP, 0.25f, 0.8f);
                    magic.getCastingResource().consume((LivingEntity)player, 40.0f);
                    showParticles = true;
                }
            }
            if (player.getPersistentData().contains(demon_armor_meteor_jumping_key)) {
                this.handlePlayerMeteorJump(player);
                showParticles = true;
            }
            if (player.isOnFire()) {
                MobEffectInstance strength;
                if (!(player.level().isClientSide() || (strength = player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.BURNING_RAGE.get()))) != null && strength.getDuration() >= 105)) {
                    player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.BURNING_RAGE.get()), 600, 1, true, true));
                }
            } else if (!player.level().isClientSide() && (eff = player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.BURNING_RAGE.get()))) != null && eff.isAmbient()) {
                player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.BURNING_RAGE.get()));
            }
            if (world.isClientSide && showParticles) {
                Vec3 motion = player.getDeltaMovement();
                Vec3 look = player.getForward().cross(new Vec3(0.0, 1.0, 0.0));
                float offset = (float)(Math.random() * 0.2);
                float yOffset = 0.2f;
                look = look.scale((double)offset);
                for (int i = 0; i < 5; ++i) {
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), player.getX() + look.x + Math.random() * motion.x * 2.0, player.getY() + (double)yOffset + Math.random() * motion.y * 2.0, player.getZ() + look.z + Math.random() * motion.z * 2.0, 0.0, 0.0, 0.0);
                    world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), player.getX() - look.x + Math.random() * motion.x * 2.0, player.getY() + (double)yOffset + Math.random() * motion.y * 2.0, player.getZ() - look.z + Math.random() * motion.z * 2.0, 0.0, 0.0, 0.0);
                }
            }
        } else {
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(runSpeed_1);
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(runSpeed_2);
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(runSpeed_3);
        }
    }

    public boolean handlePlayerJump(Player player) {
        if (this.isSetEquipped((LivingEntity)player) && player.isSprinting() && player.onGround()) {
            float multiplier = 3.0f;
            player.push((double)((float)(player.getDeltaMovement().x * (double)multiplier)), 0.75, (double)((float)(player.getDeltaMovement().z * (double)multiplier)));
            return true;
        }
        return false;
    }

    private void handlePlayerMeteorJump(Player player) {
        if (player.onGround()) {
            this.handlePlayerMeteorJumpImpact(player, 0.0f);
        }
        if (this.isSetEquipped((LivingEntity)player)) {
            player.push(0.0, -0.05, 0.0);
            if (player.level().isClientSide()) {
                for (int i = 0; i < 25; ++i) {
                    player.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())), player.getX() - 0.5 + Math.random() * 0.5, player.getY() + Math.random(), player.getZ() - 0.5 + Math.random() * 0.5, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    public boolean handlePlayerMeteorJumpImpact(Player player, float fallDistance) {
        if (!this.isSetEquipped((LivingEntity)player)) {
            return false;
        }
        if (player.getPersistentData().contains(demon_armor_meteor_jumping_key)) {
            player.getPersistentData().remove(demon_armor_meteor_jumping_key);
            if (!player.level().isClientSide() && fallDistance > 5.0f) {
                MAExplosion.make((LivingEntity)player, (ServerLevel)player.level(), player.getX(), player.getY(), player.getZ(), Math.min(5.0f, fallDistance / 6.0f), Math.min(20.0f, fallDistance / 1.5f), true, GeneralConfigValues.MeteorJumpEnabled && ((ServerLevel)player.level()).getServer().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP, player.damageSources().explosion((Entity)player, null));
            }
        }
        return true;
    }

    @Override
    public void applySetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
        if (living instanceof Player) {
            living.getPersistentData().putBoolean(demon_armor_set_bonus_key, true);
        }
    }

    @Override
    public void removeSetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
        if (living instanceof Player) {
            living.getPersistentData().putBoolean(demon_armor_set_bonus_key, false);
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
        return Factions.DEMONS;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ISetItem.super.addSetTooltip(tooltip);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return IBrokenArmorReplaceable.super.damageItem(stack, amount * 3, entity, onBroken);
    }
}


