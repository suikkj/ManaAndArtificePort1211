package com.mna.enchantments.framework;

import com.mna.api.enchantments.MAEnchantmentBridge;
import com.mna.api.tools.MATags;
import com.mna.effects.EffectInit;
import com.mna.enchantments.Beheading;
import com.mna.enchantments.Bouncing;
import com.mna.enchantments.Cloudstep;
import com.mna.enchantments.ExperienceToSouls;
import com.mna.enchantments.Fireproof;
import com.mna.enchantments.Gilded;
import com.mna.enchantments.ManaRepair;
import com.mna.enchantments.Returning;
import com.mna.enchantments.Soulbound;
import com.mna.enchantments.TransitoryStep;
import com.mna.enchantments.auras.Aura;
import com.mna.enchantments.base.MAEnchantmentBase;
import com.mna.enchantments.staves.Bludgeoning;
import com.mna.enchantments.staves.DurationModifier;
import com.mna.enchantments.staves.RangeModifier;
import com.mna.enchantments.staves.SpeedModifier;
import com.mna.network.ClientMessageDispatcher;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * [PORT 1.21.1] Enchantment virou record: cada ench registra um Enchantment vanilla
 * construido por {@link MAEnchantmentBridge#build}, e a LOGICA custom (Aura, Bouncing...)
 * fica registrada em {@link EnchantmentBehaviors} por ResourceKey.
 */
@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class EnchantmentInit {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Registries.ENCHANTMENT, "mna");

    private static MAEnchantmentBridge.EnchantmentDefinition auraDef(MAEnchantmentBridge.Rarity rarity, int maxLevel) {
        return MAEnchantmentBridge.definition(MAEnchantmentBridge.itemTag(net.minecraft.tags.ItemTags.CHEST_ARMOR_ENCHANTABLE), rarity, maxLevel, List.of(EquipmentSlotGroup.CHEST));
    }

    private static MAEnchantmentBridge.EnchantmentDefinition staffDef(MAEnchantmentBridge.Rarity rarity, int maxLevel) {
        return MAEnchantmentBridge.definition(MAEnchantmentBridge.itemTag(MATags.Items.STAVES), rarity, maxLevel, List.of(EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND));
    }

    private static MAEnchantmentBridge.EnchantmentDefinition weaponDef(MAEnchantmentBridge.Rarity rarity, int maxLevel) {
        return MAEnchantmentBridge.definition(MAEnchantmentBridge.itemTag(net.minecraft.tags.ItemTags.WEAPON_ENCHANTABLE), rarity, maxLevel, List.of(EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND));
    }

    private static MAEnchantmentBridge.EnchantmentDefinition bootsDef(MAEnchantmentBridge.Rarity rarity, int maxLevel) {
        return MAEnchantmentBridge.definition(MAEnchantmentBridge.itemTag(net.minecraft.tags.ItemTags.FOOT_ARMOR_ENCHANTABLE), rarity, maxLevel, List.of(EquipmentSlotGroup.FEET));
    }

    private static MAEnchantmentBridge.EnchantmentDefinition armorDef(MAEnchantmentBridge.Rarity rarity, int maxLevel) {
        return MAEnchantmentBridge.definition(MAEnchantmentBridge.itemTag(net.minecraft.tags.ItemTags.ARMOR_ENCHANTABLE), rarity, maxLevel, List.of(EquipmentSlotGroup.ARMOR));
    }

    private static MAEnchantmentBridge.EnchantmentDefinition bowDef(MAEnchantmentBridge.Rarity rarity, int maxLevel) {
        return MAEnchantmentBridge.definition(MAEnchantmentBridge.itemTag(net.minecraft.tags.ItemTags.BOW_ENCHANTABLE), rarity, maxLevel, List.of(EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND));
    }

    private static MAEnchantmentBridge.EnchantmentDefinition anyDef(MAEnchantmentBridge.Rarity rarity, int maxLevel) {
        return MAEnchantmentBridge.definition(MAEnchantmentBridge.itemTag(net.minecraft.tags.ItemTags.DURABILITY_ENCHANTABLE), rarity, maxLevel, List.of(EquipmentSlotGroup.ANY));
    }

    public static final DeferredHolder<Enchantment, Enchantment> HEALING = ENCHANTMENTS.register("aura-healing", () -> MAEnchantmentBridge.build("aura-healing", auraDef(MAEnchantmentBridge.Rarity.VERY_RARE, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> RUNNING = ENCHANTMENTS.register("aura-running", () -> MAEnchantmentBridge.build("aura-running", auraDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> PILGRIM = ENCHANTMENTS.register("aura-pilgrim", () -> MAEnchantmentBridge.build("aura-pilgrim", auraDef(MAEnchantmentBridge.Rarity.VERY_RARE, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> JUMPING = ENCHANTMENTS.register("aura-jumping", () -> MAEnchantmentBridge.build("aura-jumping", auraDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> WATER_BREATHING = ENCHANTMENTS.register("aura-depths", () -> MAEnchantmentBridge.build("aura-depths", auraDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> MINING_BOOST = ENCHANTMENTS.register("aura-mining", () -> MAEnchantmentBridge.build("aura-mining", auraDef(MAEnchantmentBridge.Rarity.UNCOMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> BULWARK = ENCHANTMENTS.register("aura-bulwark", () -> MAEnchantmentBridge.build("aura-bulwark", auraDef(MAEnchantmentBridge.Rarity.UNCOMMON, 3)));
    public static final DeferredHolder<Enchantment, Enchantment> REPAIR = ENCHANTMENTS.register("aura-repair", () -> MAEnchantmentBridge.build("aura-repair", auraDef(MAEnchantmentBridge.Rarity.VERY_RARE, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> BOUNCING = ENCHANTMENTS.register("bouncing", () -> MAEnchantmentBridge.build("bouncing", bootsDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> CLOUDSTEP = ENCHANTMENTS.register("cloudstep", () -> MAEnchantmentBridge.build("cloudstep", bootsDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> BEHEADING = ENCHANTMENTS.register("beheading", () -> MAEnchantmentBridge.build("beheading", weaponDef(MAEnchantmentBridge.Rarity.COMMON, 3)));
    public static final DeferredHolder<Enchantment, Enchantment> TRANSITORY_STEP = ENCHANTMENTS.register("transitorystep", () -> MAEnchantmentBridge.build("transitorystep", bootsDef(MAEnchantmentBridge.Rarity.RARE, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> GILDED = ENCHANTMENTS.register("gilded", () -> MAEnchantmentBridge.build("gilded", weaponDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> FIREPROOF = ENCHANTMENTS.register("fireproof", () -> MAEnchantmentBridge.build("fireproof", armorDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> MANA_REPAIR = ENCHANTMENTS.register("mana-repair", () -> MAEnchantmentBridge.build("mana-repair", anyDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> RETURNING = ENCHANTMENTS.register("returning", () -> MAEnchantmentBridge.build("returning", bowDef(MAEnchantmentBridge.Rarity.COMMON, 1)));
    public static final DeferredHolder<Enchantment, Enchantment> BLUDGEONING = ENCHANTMENTS.register("bludgeoning", () -> MAEnchantmentBridge.build("bludgeoning", staffDef(MAEnchantmentBridge.Rarity.COMMON, 5)));
    public static final DeferredHolder<Enchantment, Enchantment> DURATION_MOD = ENCHANTMENTS.register("duration_modifier", () -> MAEnchantmentBridge.build("duration_modifier", staffDef(MAEnchantmentBridge.Rarity.UNCOMMON, 5)));
    public static final DeferredHolder<Enchantment, Enchantment> RANGE_MOD = ENCHANTMENTS.register("range_modifier", () -> MAEnchantmentBridge.build("range_modifier", staffDef(MAEnchantmentBridge.Rarity.UNCOMMON, 5)));
    public static final DeferredHolder<Enchantment, Enchantment> SPEED_MOD = ENCHANTMENTS.register("speed_modifier", () -> MAEnchantmentBridge.build("speed_modifier", staffDef(MAEnchantmentBridge.Rarity.UNCOMMON, 5)));
    public static final DeferredHolder<Enchantment, Enchantment> XP_TO_SOULS = ENCHANTMENTS.register("xp_to_souls", () -> MAEnchantmentBridge.build("xp_to_souls", weaponDef(MAEnchantmentBridge.Rarity.COMMON, 3)));
    public static final DeferredHolder<Enchantment, Enchantment> SOULBOUND = ENCHANTMENTS.register("soulbound", () -> MAEnchantmentBridge.build("soulbound", anyDef(MAEnchantmentBridge.Rarity.COMMON, 1)));

    static {
        EnchantmentBehaviors.put(HEALING, new Aura(MAEnchantmentBridge.Rarity.VERY_RARE).withEffect(MobEffects.REGENERATION).withManaCost(10.0f).withPredicate(p -> p.getHealth() <= p.getMaxHealth() * 0.75f && p.getEffect(MobEffects.REGENERATION) == null));
        EnchantmentBehaviors.put(RUNNING, new Aura(MAEnchantmentBridge.Rarity.COMMON).withEffect(MobEffects.MOVEMENT_SPEED).withMagnitude(3).withManaCost(0.075f));
        EnchantmentBehaviors.put(PILGRIM, new Aura(MAEnchantmentBridge.Rarity.VERY_RARE).withEffect(EffectInit.PILGRIM).withMagnitude(0).withManaCost(0.0f));
        EnchantmentBehaviors.put(JUMPING, new Aura(MAEnchantmentBridge.Rarity.COMMON).withEffect(MobEffects.JUMP).withMagnitude(5).withManaCost(0.075f));
        EnchantmentBehaviors.put(WATER_BREATHING, new Aura(MAEnchantmentBridge.Rarity.COMMON).withEffect(MobEffects.CONDUIT_POWER).withManaCost(0.075f).withPredicate(p -> p.isInWaterOrBubble()));
        EnchantmentBehaviors.put(MINING_BOOST, new Aura(MAEnchantmentBridge.Rarity.UNCOMMON).withEffect(MobEffects.DIG_SPEED).withMagnitude(3).withManaCost(0.05f));
        EnchantmentBehaviors.put(BULWARK, new Aura(MAEnchantmentBridge.Rarity.UNCOMMON).withEffect(EffectInit.BULWARK).withMaxLevel(3).withManaCost(0.05f));
        EnchantmentBehaviors.put(REPAIR, new Aura(MAEnchantmentBridge.Rarity.VERY_RARE).withEffect(EffectInit.REPAIR).withManaCost(0.25f).withPredicate(p -> {
            for (int i = 0; i < p.getInventory().getContainerSize(); ++i) {
                ItemStack stack = p.getInventory().getItem(i);
                if (stack.isEmpty() || !stack.isRepairable() || !stack.isDamaged()) continue;
                return true;
            }
            return false;
        }));
        EnchantmentBehaviors.put(BOUNCING, new Bouncing(MAEnchantmentBridge.Rarity.COMMON));
        EnchantmentBehaviors.put(CLOUDSTEP, new Cloudstep(MAEnchantmentBridge.Rarity.COMMON));
        EnchantmentBehaviors.put(BEHEADING, new Beheading());
        EnchantmentBehaviors.put(TRANSITORY_STEP, new TransitoryStep(MAEnchantmentBridge.Rarity.RARE));
        EnchantmentBehaviors.put(GILDED, new Gilded());
        EnchantmentBehaviors.put(FIREPROOF, new Fireproof());
        EnchantmentBehaviors.put(MANA_REPAIR, new ManaRepair());
        EnchantmentBehaviors.put(RETURNING, new Returning());
        EnchantmentBehaviors.put(BLUDGEONING, new Bludgeoning());
        EnchantmentBehaviors.put(DURATION_MOD, new DurationModifier());
        EnchantmentBehaviors.put(RANGE_MOD, new RangeModifier());
        EnchantmentBehaviors.put(SPEED_MOD, new SpeedModifier());
        EnchantmentBehaviors.put(XP_TO_SOULS, new ExperienceToSouls());
        EnchantmentBehaviors.put(SOULBOUND, new Soulbound());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        ItemStack boots;
        if (event.getEntity().level().getGameTime() % 5L == 0L) {
            EnchantmentInit.tickAuras(event.getEntity(), event.getEntity().level().isClientSide() || event.getEntity().level().getGameTime() % 60L != 0L);
        }
        if (EnchantmentHelper.getEnchantmentLevel(TRANSITORY_STEP, event.getEntity()) > 0) {
            ((TransitoryStep)EnchantmentBehaviors.get(TRANSITORY_STEP)).apply(event.getEntity(), 1);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onPlayerFall(LivingFallEvent event) {
        ItemStack boots;
        Player player;
        if (event.isCanceled()) {
            return;
        }
        if (event.getEntity() instanceof Player && event.getDistance() > 2.0f && !(player = (Player)event.getEntity()).isShiftKeyDown() && player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get())) == null && !(boots = player.getItemBySlot(EquipmentSlot.FEET)).isEmpty() && EnchantmentHelper.getEnchantmentLevel(BOUNCING, event.getEntity()) > 0) {
            event.setCanceled(true);
            if (player.level().isClientSide()) {
                float maxVelocity = 4.0f;
                float bounceVelocity = (float)(player.getDeltaMovement().y * -0.5);
                Vec3 velocity = new Vec3(player.getDeltaMovement().x, (double)Math.min(maxVelocity, bounceVelocity), player.getDeltaMovement().z);
                player.setDeltaMovement(velocity.x, velocity.y, velocity.z);
                ClientMessageDispatcher.sendPlayerBounce(velocity);
            }
        }
    }

    private static void tickAuras(Player player, boolean manaOnly) {
        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.isEmpty()) continue;
            for (DeferredHolder<Enchantment, ? extends Enchantment> enchantment : ENCHANTMENTS.getEntries()) {
                MAEnchantmentBase behavior = EnchantmentBehaviors.get(enchantment);
                int level;
                if (!(behavior instanceof Aura) || (level = EnchantmentHelper.getEnchantmentLevel(enchantment, player)) <= 0) continue;
                ((Aura)behavior).apply(player, level, manaOnly);
            }
        }
    }
}
