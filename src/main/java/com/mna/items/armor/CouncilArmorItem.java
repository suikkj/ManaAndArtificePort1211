 
package com.mna.items.armor;

import com.mna.ManaAndArtifice;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.items.ITieredItem;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.tools.RLoc;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.EffectInit;
import com.mna.factions.Factions;
import com.mna.items.ItemInit;
import com.mna.items.armor.IBrokenArmorReplaceable;
import com.mna.items.armor.ISetItem;
import com.mna.items.base.IManaRepairable;
import com.mna.items.renderers.CouncilArmorRenderer;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
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
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.mna.api.tools.MNACap;

public class CouncilArmorItem
extends ArmorItem
implements GeoItem,
ISetItem,
ITieredItem<CouncilArmorItem>,
IFactionSpecific,
IBrokenArmorReplaceable<CouncilArmorItem>,
IManaRepairable {
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private static final ResourceLocation council_armor_set_bonus = RLoc.create("council_armor_set_bonus");
    public static final String council_armor_set_bonus_key = "council_armor_set_bonus";
    public static final String council_armor_reflect_counters_key = "council_armor_reflect_counters";
    private static final float MANA_COST_PER_TICK = 0.75f;
    private int _tier = -1;

    public CouncilArmorItem(net.minecraft.core.Holder<ArmorMaterial> materialIn, ArmorItem.Type slot, Item.Properties builder) {
        super(materialIn, slot, builder.rarity(Rarity.EPIC).durability(slot.getDurability(33)));
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions(){
            private GeoArmorRenderer<?> renderer;

            @NotNull
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (this.renderer == null) {
                    this.renderer = new CouncilArmorRenderer();
                }
                this.renderer.prepForRender((Entity)livingEntity, itemStack, equipmentSlot, original);
                return this.renderer;
            }
        });
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.ambient")))});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    @Override
    public ResourceLocation getSetIdentifier() {
        return council_armor_set_bonus;
    }

    public void onArmorTick(ItemStack stack, Level world, Player player) {
        this.usedByPlayer(player);
        if (this.isSetEquipped((LivingEntity)player) && this == ItemInit.COUNCIL_ARMOR__CHEST.get()) {
            MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                if (player.getAbilities().flying && !player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LEVITATION.get()))) {
                    if (world.isClientSide) {
                        Vec3 look = player.getForward().cross(new Vec3(0.0, 1.0, 0.0));
                        float offset = (float)(Math.random() * 0.2);
                        look = look.scale((double)offset);
                        world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), player.getX() + look.x, player.getY(), player.getZ() + look.z, 0.0, (double)-0.05f, 0.0);
                        world.addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE.get())), player.getX() - look.x, player.getY(), player.getZ() - look.z, 0.0, (double)-0.05f, 0.0);
                    } else {
                        m.getCastingResource().consume((LivingEntity)player, 0.75f);
                    }
                }
                if (!m.getCastingResource().hasEnoughAbsolute((LivingEntity)player, 0.75f)) {
                    ManaAndArtifice.instance.proxy.setFlightEnabled(player, false);
                } else {
                    ManaAndArtifice.instance.proxy.setFlightEnabled(player, true);
                    if (!player.isCreative() && !player.isSpectator()) {
                        ManaAndArtifice.instance.proxy.setFlySpeed(player, 0.02f);
                        if (player.getAbilities().flying) {
                            player.setSprinting(false);
                        }
                    } else {
                        ManaAndArtifice.instance.proxy.setFlySpeed(player, 0.05f);
                    }
                }
            });
        }
    }

    @Override
    public void applySetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
        if (living instanceof Player) {
            living.getPersistentData().putBoolean(council_armor_set_bonus_key, true);
            MNACap.get((Player)living, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                m.getCastingResource().addModifier(council_armor_set_bonus_key, 750.0f);
                m.getCastingResource().addRegenerationModifier(council_armor_set_bonus_key, -0.25f);
            });
        }
    }

    @Override
    public void removeSetBonus(LivingEntity living, EquipmentSlot ... setSlots) {
        if (living instanceof Player) {
            living.getPersistentData().putBoolean(council_armor_set_bonus_key, false);
            MNACap.get((Player)living, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                m.getCastingResource().removeModifier(council_armor_set_bonus_key);
                m.getCastingResource().removeRegenerationModifier(council_armor_set_bonus_key);
            });
            ManaAndArtifice.instance.proxy.setFlySpeed((Player)living, 0.05f);
            ManaAndArtifice.instance.proxy.setFlightEnabled((Player)living, false);
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
        return Factions.COUNCIL;
    }

    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        ISetItem.super.addSetTooltip(tooltip);
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    private static int[] getReflectCharges(Player player) {
        int[] reflections = !player.getPersistentData().contains(council_armor_reflect_counters_key) ? new int[GeneralConfigValues.SpellweaverReflectCharges] : player.getPersistentData().getIntArray(council_armor_reflect_counters_key);
        if (reflections.length != GeneralConfigValues.SpellweaverReflectCharges) {
            reflections = new int[GeneralConfigValues.SpellweaverReflectCharges];
        }
        return reflections;
    }

    private static void updateReflectCharges(Player player, int[] reflections) {
        player.getPersistentData().putIntArray(council_armor_reflect_counters_key, reflections);
    }

    public static boolean consumeReflectCharget(Player player) {
        int[] reflections = CouncilArmorItem.getReflectCharges(player);
        for (int i = 0; i < reflections.length; ++i) {
            if (reflections[i] > 0) continue;
            reflections[i] = GeneralConfigValues.SpellweaverReflectTime;
            CouncilArmorItem.updateReflectCharges(player, reflections);
            return true;
        }
        return false;
    }

    public static void tickReflectCharges(Player player) {
        int[] reflections = CouncilArmorItem.getReflectCharges(player);
        for (int i = 0; i < reflections.length; ++i) {
            if (reflections[i] <= 0) continue;
            int n = i;
            reflections[n] = reflections[n] - 1;
        }
        CouncilArmorItem.updateReflectCharges(player, reflections);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
        return IBrokenArmorReplaceable.super.damageItem(stack, amount * 3, entity, onBroken);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        IManaRepairable.super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }
}


