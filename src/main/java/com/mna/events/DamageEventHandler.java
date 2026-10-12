 
package com.mna.events;

import com.mna.ManaAndArtifice;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.resource.CastingResourceIDs;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.entities.DamageHelper;
import com.mna.api.entities.IFactionEnemy;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.events.BonefeatherCharmUsedEvent;
import com.mna.api.events.CoalfeatherCharmUsedEvent;
import com.mna.api.events.ReedfeatherCharmUsedEvent;
import com.mna.api.items.ChargeableItem;
import com.mna.api.sound.SFX;
import com.mna.blocks.tileentities.pylon.RechargePylonTile;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.config.GeneralConfig;
import com.mna.effects.EffectInit;
import com.mna.effects.beneficial.EffectManaShield;
import com.mna.entities.boss.WitherLich;
import com.mna.entities.constructs.animated.Construct;
import com.mna.entities.summon.BloodGolem;
import com.mna.entities.utility.DisplayDamage;
import com.mna.factions.Factions;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.items.armor.CouncilArmorItem;
import com.mna.items.armor.DemonArmorItem;
import com.mna.items.armor.FeyArmorItem;
import com.mna.items.artifice.WardingAmulet;
import com.mna.items.artifice.WitherguardAmulet;
import com.mna.items.artifice.charms.ContingencyCharm;
import com.mna.items.artifice.curio.EmberglowBracelet;
import com.mna.items.artifice.curio.FortifyingAmulet;
import com.mna.items.relic.CrystallizedWind;
import com.mna.items.sorcery.CrystalPhylactery;
import com.mna.items.sorcery.ItemSpell;
import com.mna.items.sorcery.PhylacteryStaff;
import com.mna.items.sorcery.bound.BoundShield;
import com.mna.spells.components.ComponentShacklesOfPain;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.BlockUtils;
import com.mna.tools.EntityUtil;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.ProjectileHelper;
import com.mna.tools.SummonUtils;
import com.mna.tools.TeleportHelper;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypePreset;
import com.mna.api.tools.MNACap;
import com.mna.api.events.RunicAnvilShouldActivateEvent.Result;
import com.mna.api.enchantments.MNAEnchantmentHelper;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class DamageEventHandler {
    @SubscribeEvent(receiveCanceled=true)
    public static void onLivingFallAlways(LivingFallEvent event) {
        event.getEntity().getPersistentData().remove("mna:flung");
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity living = event.getEntity();
        if (living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get())) || CrystallizedWind.FindOn(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        if (living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.PILGRIM.get())) || living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get())) || living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.REDUCE.get()))) {
            event.setDistance(event.getDistance() - 2.0f);
        }
        if (living instanceof Player) {
            Player player = (Player)living;
            Entity entity = player.getVehicle();
            if (entity instanceof Construct) {
                Construct ridingConstruct = (Construct)entity;
                event.setCanceled(true);
                return;
            }
            if (((DemonArmorItem)ItemInit.DEMON_ARMOR_BOOTS.get()).handlePlayerMeteorJumpImpact(player, event.getDistance())) {
                event.setCanceled(true);
                return;
            }
            if (player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get())) != null) {
                event.setDistance(event.getDistance() * 2.0f);
            }
            if (player.getInventory().getItem(EquipmentSlot.CHEST.getIndex()).getItem() == ItemInit.SPECTRAL_ELYTRA.get()) {
                event.setDistance(event.getDistance() - 5.0f);
            }
            if (!event.isCanceled()) {
                DamageEventHandler.checkAndConsumeBonefeather(event, player);
            }
        }
    }

    @SubscribeEvent
public static void onLivingAttack(LivingIncomingDamageEvent event) {
        DamageSource source = event.getSource();
        LivingEntity living = event.getEntity();
        if (living == null || source == null) {
            return;
        }
        if (source.is(DamageTypes.IN_WALL) && (living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get())) || living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ELDRIN_FLIGHT.get())) || living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get())) && living.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get())).getAmplifier() == 2)) {
            event.setCanceled(true);
            return;
        }
        if (living instanceof Player) {
            if (DamageEventHandler.checkAndConsumeReedfeather(event, (Player)living)) {
                return;
            }
            if (DamageEventHandler.checkAndConsumeCoalfeather(event, (Player)living)) {
                return;
            }
        }
        if (!(!living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.FORTIFICATION.get())) || source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypeTags.BYPASSES_RESISTANCE))) {
            event.setCanceled(true);
            living.hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.FELL_OUT_OF_WORLD, living.level().registryAccess(), source.getEntity()), (float)GeneralConfigValues.FossilizeDamage);
            return;
        }
        if (source.getDirectEntity() instanceof Projectile) {
            boolean shouldReflect = false;
            boolean shouldReflectAtShooter = false;
            Entity sourceEntity = source.getEntity();
            if (living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.WIND_WALL.get())) && (sourceEntity == null || com.mna.tools.MATools.canChangeDimensions(sourceEntity))) {
                shouldReflect = true;
                MobEffectInstance windWall = living.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.WIND_WALL.get()));
                if (windWall != null) {
                    shouldReflectAtShooter = windWall.getAmplifier() > 0;
                }
            } else if (event.getEntity() instanceof Player) {
                Player player = (Player)event.getEntity();
                if (((CouncilArmorItem)ItemInit.COUNCIL_ARMOR__CHEST.get()).isSetEquipped(living)) {
                    shouldReflect = CouncilArmorItem.consumeReflectCharget(player);
                    shouldReflectAtShooter = true;
                } else if (((FeyArmorItem)ItemInit.FEY_ARMOR_CHEST.get()).isSetEquipped(living)) {
                    shouldReflect = FeyArmorItem.randomReflectChance();
                    shouldReflectAtShooter = true;
                }
            }
            if (shouldReflect) {
                event.setCanceled(true);
                ProjectileHelper.ReflectProjectile(living, (Projectile)source.getDirectEntity(), shouldReflectAtShooter, 10.0f);
                return;
            }
        }
        if (!source.is(DamageTypeTags.IS_PROJECTILE) && source.getEntity() != null && source.getEntity() instanceof LivingEntity && source.getEntity().distanceToSqr((Entity)event.getEntity()) < GeneralConfigValues.MeleeDistance && ((FeyArmorItem)ItemInit.FEY_ARMOR_CHEST.get()).isSetEquipped(living) && FeyArmorItem.randomTeleportChance() && TeleportHelper.randomTeleport((LivingEntity)source.getEntity(), 16.0f, 10)) {
            event.setCanceled(true);
            return;
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            if (((DemonArmorItem)ItemInit.DEMON_ARMOR_CHEST.get()).isSetEquipped(living)) {
                event.setCanceled(true);
                return;
            }
            if (((EmberglowBracelet)ItemInit.EMBERGLOW_BRACELET.get()).isEquippedAndHasMana(living, 1.0f, true)) {
                event.setCanceled(true);
                return;
            }
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION) && ((DemonArmorItem)ItemInit.DEMON_ARMOR_CHEST.get()).isSetEquipped(living)) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
public static void onLivingHurt(LivingDamageEvent.Pre event) {
        Entity source = event.getSource().getEntity();
        LivingEntity hurtEntity = event.getEntity();
        if ((event.getSource().is(DamageTypeTags.IS_FALL) || event.getSource().is(DamageTypes.FLY_INTO_WALL)) && CrystallizedWind.FindOn(hurtEntity)) {
            event.setNewDamage(0.0f);
            return;
        }
        if (source != null && source instanceof LivingEntity) {
            double dist;
            boolean retaliate;
            int amplifier;
            LivingEntity sourceLiving = (LivingEntity)source;
            if (event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO)) {
                if (hurtEntity.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.AMPLIFY_MAGIC.get()))) {
                    amplifier = hurtEntity.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.AMPLIFY_MAGIC.get())).getAmplifier() + 1;
                    event.setNewDamage(event.getNewDamage() * (1.0f + (float)amplifier * 0.1f));
                }
                if (hurtEntity.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.DAMPEN_MAGIC.get()))) {
                    amplifier = hurtEntity.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.DAMPEN_MAGIC.get())).getAmplifier() + 1;
                    event.setNewDamage(event.getNewDamage() * (1.0f - (float)amplifier * 0.1f));
                }
            }
            if (hurtEntity.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.FRAILTY.get()))) {
                amplifier = hurtEntity.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.FRAILTY.get())).getAmplifier() + 1;
                event.setNewDamage(event.getNewDamage() * (1.0f + (float)amplifier * 0.2f));
            }
            boolean bl = retaliate = (dist = hurtEntity.distanceToSqr((Entity)sourceLiving)) <= 25.0;
            if (retaliate && hurtEntity.getUseItem().getItem() == ItemInit.BOUND_SHIELD.get() && DamageEventHandler.canEntityBlock(event.getSource(), hurtEntity)) {
                SpellRecipe recipe = ((BoundShield)ItemInit.BOUND_SHIELD.get()).getRecipe(hurtEntity.getUseItem());
                if (hurtEntity instanceof Player) {
                    Player player = (Player)hurtEntity;
                    if (!player.getCooldowns().isOnCooldown((Item)ItemInit.BOUND_SHIELD.get())) {
                        ((BoundShield)ItemInit.BOUND_SHIELD.get()).affectTarget(recipe, hurtEntity, sourceLiving, hurtEntity.getUsedItemHand());
                        player.getCooldowns().addCooldown((Item)ItemInit.BOUND_SHIELD.get(), recipe.getCooldown((LivingEntity)player));
                    }
                } else {
                    ((BoundShield)ItemInit.BOUND_SHIELD.get()).affectTarget(recipe, hurtEntity, sourceLiving, hurtEntity.getUsedItemHand());
                }
            }
        }
        boolean reduce = !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !event.getSource().is(DamageTypeTags.IS_FALL) && !event.getSource().is(DamageTypes.IN_WALL) && !event.getSource().is(DamageTypes.STARVE) && !event.getSource().is(DamageTypeTags.IS_DROWNING) && !event.getSource().is(DamageTypes.CRAMMING) && !event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE);
        MobEffectInstance mana_shield = hurtEntity.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_SHIELD.get()));
        if (mana_shield != null && reduce && event.getSource().getEntity() != null) {
            Player mana_shield_source = DamageEventHandler.resolveManaShieldSource((Entity)hurtEntity);
            if (mana_shield_source != null) {
                float reduction = mana_shield.getAmplifier() + 1;
                float reductionCost = ((EffectManaShield)EffectInit.MANA_SHIELD.get()).getReductionCost(mana_shield.getAmplifier() + 1);
                MNACap.get(mana_shield_source, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                    float amountReduced = reduction;
                    if (m.getCastingResource().getAmount() <= amountReduced * reductionCost) {
                        amountReduced = m.getCastingResource().getAmount() / reductionCost;
                    }
                    m.getCastingResource().consume((LivingEntity)mana_shield_source, amountReduced * reductionCost);
                    m.forceSync(1);
                    if (m.getCastingResource().getAmount() <= 0.0f) {
                        hurtEntity.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_SHIELD.get()));
                    }
                    event.setNewDamage(event.getNewDamage() - amountReduced);
                });
            } else {
                hurtEntity.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MANA_SHIELD.get()));
            }
            if (event.getNewDamage() == 0.0f) {
                event.setNewDamage(0.0f);
                return;
            }
        }
        if (source instanceof Player) {
            Player sourcePlayer = (Player)source;
            if (!(event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO) || event.getSource().is(DamageTypeTags.IS_PROJECTILE) || event.getSource().is(DamageTypeTags.IS_FIRE) || event.getSource().is(DamageTypeTags.IS_EXPLOSION) || event.getSource().is(DamageTypeTags.IS_FALL) || !((double)source.distanceTo((Entity)hurtEntity) <= GeneralConfigValues.MeleeDistance))) {
                IPlayerMagic magic;
                boolean offhandIsSword;
                ItemStack mainHand = sourcePlayer.getMainHandItem();
                ItemStack offHand = sourcePlayer.getOffhandItem();
                boolean mainHandIsSword = mainHand.getItem() instanceof SwordItem || mainHand.getItem() == ItemInit.ASTRO_BLADE.get() || mainHand.getItem() == ItemInit.BONE_SCYTHE.get();
                boolean bl = offhandIsSword = offHand.getItem() instanceof SwordItem || offHand.getItem() == ItemInit.ASTRO_BLADE.get() || offHand.getItem() == ItemInit.BONE_SCYTHE.get();
                if ((mainHandIsSword && offHand.getItem() instanceof ItemSpell || offhandIsSword && mainHand.getItem() instanceof ItemSpell) && CuriosInterop.IsItemInCurioSlot((Item)ItemInit.BATTLEMAGE_AMULET.get(), (LivingEntity)sourcePlayer, SlotTypePreset.NECKLACE) && (magic = (IPlayerMagic)sourcePlayer.getData(PlayerMagicProvider.MAGIC.get())) != null) {
                    float pct = 1.0f - magic.getCastingResource().getAmount() / magic.getCastingResource().getMaxAmount();
                    event.setNewDamage(event.getNewDamage() + 5.0f * pct);
                }
            }
        }
        if (event.getNewDamage() > 0.0f) {
            MobEffectInstance lev;
            Player player;
            if (hurtEntity instanceof Player && !CrystallizedWind.FindOn((LivingEntity)(player = (Player)hurtEntity)) && (lev = hurtEntity.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LEVITATION.get()))) != null) {
                Player target = (Player)event.getEntity();
                boolean wasFlying = target.getAbilities().flying;
                int amplifier = lev.getAmplifier();
                int duration = lev.isInfiniteDuration() ? 1200 : lev.getDuration();
                target.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LEVITATION.get()));
                if ((duration /= 2) > 100) {
                    target.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.LEVITATION.get()), duration, amplifier));
                    ManaAndArtifice.instance.proxy.setFlightEnabled(target, true);
                    target.getAbilities().flying = wasFlying;
                }
            }
            if (hurtEntity.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.BIND_WOUNDS.get()))) {
                hurtEntity.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.BIND_WOUNDS.get()));
            }
        }
    }

    @SubscribeEvent
public static void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity living;
        Entity source;
        boolean reduce;
        MobEffectInstance fire_shield = event.getEntity().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.FIRE_SHIELD.get()));
        MobEffectInstance briar_shield = event.getEntity().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.BRIARTHORN_BARRIER.get()));
        MobEffectInstance life_tap = event.getEntity().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFE_TAP.get()));
        boolean bl = reduce = !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !event.getSource().is(DamageTypeTags.IS_FALL) && !event.getSource().is(DamageTypes.IN_WALL) && !event.getSource().is(DamageTypes.STARVE) && !event.getSource().is(DamageTypeTags.IS_DROWNING) && !event.getSource().is(DamageTypes.CRAMMING) && !event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE);
        if (event.getSource().is(DamageTypes.WITHER) && ((WitherguardAmulet)ItemInit.WITHERGUARD_AMULET.get()).isEquippedAndHasMana(event.getEntity(), 1.0f, true)) {
            event.setNewDamage(0.0f);
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.getPersistentData().getBoolean("hellfiretrident")) {
                if (player.autoSpinAttackTicks > 0 && event.getSource().getMsgId() == "player") {
                    event.setNewDamage(event.getNewDamage() * 2.0f);
                }
                player.getPersistentData().putBoolean("hellfiretrident", false);
            }
            EntityUtil.suppressInvisibilityEffects((LivingEntity)player);
        }
        if (reduce && event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO) && ((WardingAmulet)ItemInit.WARDING_AMULET.get()).isEquippedAndHasMana(event.getEntity(), 1.0f, true)) {
            event.setNewDamage(event.getNewDamage() - event.getNewDamage() * 0.2f);
        }
        if (event.getSource().is(DamageTypeTags.IS_FIRE) && fire_shield != null && reduce) {
            float reduction = event.getNewDamage() * 0.1f * (float)(fire_shield.getAmplifier() + 1);
            event.setNewDamage(event.getNewDamage() - reduction);
        }
        if ((source = event.getSource().getEntity()) != null && source instanceof LivingEntity) {
            LivingEntity sourceLiving = (LivingEntity)source;
            if (source != event.getEntity()) {
                boolean retaliate;
                living = event.getEntity();
                double dist = living.distanceToSqr((Entity)sourceLiving);
                boolean bl2 = retaliate = dist <= 25.0;
                if (retaliate && event.getEntity().getUseItem().getItem() == ItemInit.BOUND_SHIELD.get() && DamageEventHandler.canEntityBlock(event.getSource(), living)) {
                    SpellRecipe recipe = ((BoundShield)ItemInit.BOUND_SHIELD.get()).getRecipe(living.getUseItem());
                    if (living instanceof Player) {
                        Player player = (Player)living;
                        if (!player.getCooldowns().isOnCooldown((Item)ItemInit.BOUND_SHIELD.get())) {
                            ((BoundShield)ItemInit.BOUND_SHIELD.get()).affectTarget(recipe, living, sourceLiving, living.getUsedItemHand());
                            player.getCooldowns().addCooldown((Item)ItemInit.BOUND_SHIELD.get(), recipe.getCooldown((LivingEntity)player));
                        }
                    } else {
                        ((BoundShield)ItemInit.BOUND_SHIELD.get()).affectTarget(recipe, living, sourceLiving, living.getUsedItemHand());
                    }
                }
                if (briar_shield != null) {
                    if (event.getSource().getMsgId().equals("thorns") || event.getSource().getMsgId().equals("poison") || event.getSource().getMsgId().equals("sting") || event.getSource().getMsgId().equals("cactus") || event.getSource().getMsgId().equals("sweetBerryBush")) {
                        float reduction = event.getNewDamage() * 0.1f * (float)(briar_shield.getAmplifier() + 1);
                        event.setNewDamage(event.getNewDamage() - reduction);
                    }
                    if (retaliate) {
                        float damage = (briar_shield.getAmplifier() + 1) * 2;
                        sourceLiving.hurt(DamageHelper.forType(DamageHelper.BRIARS, sourceLiving.level().registryAccess()), damage * GeneralConfig.getDamageMultiplier());
                    }
                }
                if (fire_shield != null && retaliate) {
                    float damage = (fire_shield.getAmplifier() + 1) * 2;
                    sourceLiving.hurt(sourceLiving.damageSources().inFire(), damage * GeneralConfig.getDamageMultiplier());
                }
                if (life_tap != null) {
                    float pct = 0.05f;
                    if (sourceLiving.getPersistentData().contains("life_tap_pct")) {
                        pct = sourceLiving.getPersistentData().getFloat("life_tap_pct");
                    }
                    float healing = event.getNewDamage() * pct * (float)(life_tap.getAmplifier() + 1);
                    sourceLiving.heal(healing);
                }
                if (source instanceof Player) {
                    Player sourcePlayer = (Player)source;
                    ItemStack mhStack = sourcePlayer.getMainHandItem();
                    ItemStack ohStack = sourcePlayer.getOffhandItem();
                    if (!event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                        CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)sourcePlayer, (Item)ItemInit.EMBERGLOW_BRACELET.get()).ifPresent(t -> {
                            if (living.distanceToSqr(source) < 4.0) {
                                living.igniteForSeconds((float)(4));
                            }
                            t.stack().hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)living.level(), living, damager -> CuriosApi.getCuriosHelper().onBrokenCurio(t.slotContext()));
                        });
                    }
                    if (sourcePlayer.isShiftKeyDown() && sourcePlayer.getMainHandItem().isEmpty()) {
                        CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)sourcePlayer, (Item)ItemInit.TRICKERY_BRACELET.get()).ifPresent(t -> {
                            living.knockback(5.0, sourcePlayer.getX() - living.getX(), sourcePlayer.getZ() - living.getZ());
                            t.stack().hurtAndBreak(1, (net.minecraft.server.level.ServerLevel)living.level(), living, damager -> CuriosApi.getCuriosHelper().onBrokenCurio(t.slotContext()));
                        });
                    }
                    MNACap.get(sourcePlayer, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                        int ohThorns;
                        int mhThorns;
                        int appliedThorns;
                        if (p.getAlliedFaction() == Factions.UNDEAD) {
                            float souls;
                            if (sourcePlayer.getPersistentData().getBoolean("bone_armor_set_bonus") && (souls = EntityUtil.getSoulsRestored(sourcePlayer, (Entity)living)) > 0.0f) {
                                MNACap.get(sourcePlayer, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                                    m.getCastingResource().restore(souls * 0.1f);
                                    m.getCastingResource().setNeedsSync();
                                });
                            }
                            if (CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)sourcePlayer, (Item)ItemInit.COWL_OF_CONSUMPTION.get()) != null && event.getNewDamage() >= 10.0f) {
                                sourcePlayer.getFoodData().eat(1, 0.1f);
                            }
                        }
                        if (p.getAlliedFaction() == Factions.FEY && (appliedThorns = Math.max(mhThorns = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.THORNS, mhStack), ohThorns = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.THORNS, ohStack))) > 0) {
                            living.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.WRITHING_BRAMBLES.get()), 100, appliedThorns));
                        }
                    });
                    if (living instanceof Mob) {
                        int mhLure = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.LURE, mhStack);
                        int ohLure = MNAEnchantmentHelper.getItemEnchantmentLevel(Enchantments.LURE, ohStack);
                        int appliedLure = Math.max(mhLure, ohLure);
                        if (Math.random() < 0.15 * (double)appliedLure) {
                            ((Mob)living).setTarget((LivingEntity)sourcePlayer);
                        }
                    }
                }
            }
        }
        if (event.getNewDamage() <= 0.0f) {
            event.setNewDamage(0.0f);
        } else if (event.getEntity().getPersistentData().getBoolean("bone_armor_set_bonus") && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && event.getEntity().getHealth() > 1.0f && event.getNewDamage() > event.getEntity().getHealth()) {
            event.getEntity().addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get()), 200, 0, true, true));
            event.getEntity().setHealth(1.0f);
            event.setNewDamage(0.0f);
        }
        living = event.getEntity();
        if (living instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)living;
            if (event.getNewDamage() > 0) {
                MobEffectInstance effect;
                if (player.getHealth() <= player.getMaxHealth() * 0.25f) {
                    ContingencyCharm.CheckAndConsumeCharmCharge(player, ContingencyCharm.ContingencyEvent.LOW_HEALTH);
                } else if (event.getSource().is(DamageTypes.IN_WALL) || event.getSource().is(DamageTypeTags.IS_DROWNING)) {
                    ContingencyCharm.CheckAndConsumeCharmCharge(player, ContingencyCharm.ContingencyEvent.SUFFOCATION);
                } else {
                    ContingencyCharm.CheckAndConsumeCharmCharge(player, ContingencyCharm.ContingencyEvent.DAMAGE);
                }
                LivingEntity shackledTo = ComponentShacklesOfPain.getShackleTarget((Player)player);
                if (shackledTo != null && shackledTo != player && (effect = shackledTo.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SHACKLES_OF_PAIN.get()))) != null) {
                    int amplifier = effect.getAmplifier() + 1;
                    shackledTo.hurt(shackledTo.damageSources().magic(), event.getNewDamage() * (float)amplifier);
                }
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        Player playerCause;
        Entity causeOfDeath = event.getDamageSource().getEntity();
        if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            Optional curio = CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)player, (Item)ItemInit.CRIT_IMMUNITY_AMULET.get());
            if (curio.isPresent() && !player.getCooldowns().isOnCooldown((Item)ItemInit.CRIT_IMMUNITY_AMULET.get()) && ((FortifyingAmulet)ItemInit.CRIT_IMMUNITY_AMULET.get()).consumeMana(((SlotResult)curio.get()).stack(), 250.0f, player)) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SFX.Spell.Buff.ARCANE, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.getCooldowns().addCooldown((Item)ItemInit.CRIT_IMMUNITY_AMULET.get(), 12000);
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4));
                if (player.getHealth() < 1.0f) {
                    player.setHealth(1.0f);
                }
                event.setCanceled(true);
                return;
            }
            if (player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHRONO_ANCHOR.get())) != null) {
                MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                    if (m.getChronoAnchorData().canRevert(player)) {
                        event.setCanceled(true);
                        player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHRONO_ANCHOR.get()));
                    }
                });
            } else {
                SummonUtils.getSummons((LivingEntity)player).forEach(s -> s.discard());
            }
            if (player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()))) {
                player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.LIFT.get()));
            }
            if (player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIND_VISION.get()))) {
                player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIND_VISION.get()));
            }
            if (player.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()))) {
                player.removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()));
            }
            if (player instanceof ServerPlayer) {
                ContingencyCharm.CheckAndConsumeCharmCharge((ServerPlayer)player, ContingencyCharm.ContingencyEvent.DEATH);
            }
            CompoundTag deathData = new CompoundTag();
            deathData.putLong("position", player.blockPosition().above().asLong());
            deathData.putString("dimension", player.level().dimension().location().toString());
            player.getPersistentData().put("mna_last_death_data", (Tag)deathData);
        } else {
            LivingEntity player = event.getEntity();
            if (player instanceof Mob) {
                Mob mob = (Mob)player;
                boolean isSummon = SummonUtils.isSummon((Entity)mob);
                if (isSummon && !(mob instanceof BloodGolem)) {
                    event.setCanceled(true);
                    event.getEntity().setHealth(1.0f);
                    event.getEntity().discard();
                    return;
                }
                if (causeOfDeath != null && causeOfDeath instanceof Player) {
                    LivingEntity summoner;
                    Player playerCause2 = (Player)causeOfDeath;
                    if (!isSummon && !(event.getEntity() instanceof Construct)) {
                        ItemStack mainHand = playerCause2.getMainHandItem();
                        ItemStack offHand = playerCause2.getOffhandItem();
                        boolean added = false;
                        EntityType type = event.getEntity().getType();
                        int amount = playerCause2.isCreative() ? 100 : 1;
                        Level level = causeOfDeath.level();
                        IPlayerProgression progression = (IPlayerProgression)playerCause2.getData(PlayerProgressionProvider.PROGRESSION.get());
                        if (progression != null && progression.getAlliedFaction() == Factions.UNDEAD) {
                            amount *= 5;
                        }
                        if (mainHand.getItem() == ItemInit.STAFF_PHYLACTERY.get()) {
                            added = PhylacteryStaff.addToPhylactery(mainHand, (EntityType<? extends Mob>)type, amount, level);
                        }
                        if (!added && offHand.getItem() == ItemInit.STAFF_PHYLACTERY.get()) {
                            added = PhylacteryStaff.addToPhylactery(offHand, (EntityType<? extends Mob>)type, amount, level);
                        }
                        if (!added) {
                            ItemStack backPhylactery = ItemStack.EMPTY;
                            List results = CuriosApi.getCuriosHelper().findCurios((LivingEntity)((Player)causeOfDeath), is -> is.getItem() == ItemInit.STAFF_PHYLACTERY.get() || is.getItem() == ItemInit.CRYSTAL_PHYLACTERY.get());
                            for (SlotResult sr : results) {
                                if (sr.stack().isEmpty()) continue;
                                backPhylactery = sr.stack();
                                break;
                            }
                            if (!backPhylactery.isEmpty()) {
                                added = PhylacteryStaff.addToPhylactery(backPhylactery, (EntityType<? extends Mob>)type, amount, level);
                            }
                            if (!added) {
                                CrystalPhylactery.addToPhylactery((Container)playerCause2.getInventory(), (EntityType<? extends Mob>)type, (float)amount, level, true);
                            }
                        }
                    }
                    if (GeneralConfigValues.FactionMobKillIre && event.getEntity() instanceof IFactionEnemy) {
                        MNACap.get(playerCause2, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> p.incrementFactionAggro(((IFactionEnemy)event.getEntity()).getFaction(), 0.025f, 0.05f));
                    }
                    SummonUtils.getSummons(event.getEntity()).forEach(s -> s.discard());
                    if (SummonUtils.isSummon((Entity)event.getEntity()) && (summoner = SummonUtils.getSummoner(event.getEntity())) != null && summoner instanceof WitherLich) {
                        ((WitherLich)summoner).decrementSummons();
                    }
                }
            }
        }
        Player soulRecipient = causeOfDeath instanceof Player ? (playerCause = (Player)causeOfDeath) : null;
        float soulsMultiplier = 1.0f;
        if (causeOfDeath instanceof LivingEntity) {
            LivingEntity livingCause = (LivingEntity)causeOfDeath;
            if (SummonUtils.isSummon((Entity)livingCause)) {
                soulRecipient = SummonUtils.getSummonerAsPlayer(livingCause);
            } else if (livingCause instanceof Construct) {
                Construct construct = (Construct)livingCause;
                soulRecipient = construct.getOwner();
                soulsMultiplier = (float)construct.getConstructData().getPartsForMaterial(ConstructMaterial.BONE).size() * 0.2f;
            }
        }
        if (soulRecipient != null && soulsMultiplier > 0.0f) {
            float soulsRestored = EntityUtil.getSoulsRestored(soulRecipient, (Entity)event.getEntity()) * soulsMultiplier;
            MNACap.get(soulRecipient, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                if (m.getCastingResource().getRegistryName().equals(CastingResourceIDs.SOULS)) {
                    m.getCastingResource().restore(soulsRestored);
                    m.getCastingResource().setNeedsSync();
                }
            });
            if (event.getEntity().hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOUL_TRAP.get()))) {
                int restored = Math.max((int)Math.ceil(soulsRestored / 500.0f), 50);
                soulRecipient.getInventory().armor.forEach(is -> RechargePylonTile.rechargeItem(is, restored));
                soulRecipient.getInventory().items.forEach(is -> RechargePylonTile.rechargeItem(is, restored));
                soulRecipient.getInventory().offhand.forEach(is -> RechargePylonTile.rechargeItem(is, restored));
                CuriosApi.getCuriosInventory((LivingEntity)soulRecipient).ifPresent(handler -> handler.findCurios(is -> is.getItem() instanceof ChargeableItem).forEach(slotResult -> RechargePylonTile.rechargeItem(slotResult.stack(), restored)));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttackTarget(AttackEntityEvent event) {
        if (event.getEntity().hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get()))) {
            event.setCanceled(true);
            event.setResult(Result.DENY);
        } else {
            EntityUtil.suppressInvisibilityEffects((LivingEntity)event.getEntity());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
public static void onLivingDamage_Insight(LivingDamageEvent.Pre event) {
        float insightRange = 16.0f;
        if (!event.getEntity().level().isClientSide()) {
            boolean shouldSpawn;
            boolean bl = shouldSpawn = event.getEntity().level().getNearbyPlayers(TargetingConditions.forNonCombat().ignoreLineOfSight().ignoreInvisibilityTesting().range((double)insightRange).selector(le -> le.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.INSIGHT.get()))), event.getEntity(), event.getEntity().getBoundingBox().inflate((double)insightRange)).stream().map(p -> (ServerPlayer)p).toList().size() > 0;
            if (shouldSpawn) {
                Vec3 pos = event.getEntity().getEyePosition().add(new Vec3(-0.5 + Math.random(), 0.0, -0.5 + Math.random()));
                float amount = Math.max(event.getNewDamage(), 0.0f);
                DisplayDamage edd = new DisplayDamage(event.getEntity().level(), event.getSource(), amount);
                edd.setPos(pos);
                event.getEntity().level().addFreshEntity((Entity)edd);
            }
        }
    }

    public static void onCriticalHit(CriticalHitEvent event) {
        if (event.getTarget() instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)event.getTarget();
            Optional curio = CuriosApi.getCuriosHelper().findFirstCurio(living, (Item)ItemInit.CRIT_IMMUNITY_AMULET.get());
            if (curio.isPresent()) {
                event.setResult(Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getEntity() instanceof Construct) {
            ((Construct)event.getEntity()).onShieldBlock();
        }
    }

    @SubscribeEvent
    public static void onLivingHealed(LivingHealEvent event) {
        if (event.getEntity().hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CURSE_OF_AGONY.get())) && event.getAmount() > 1.0f) {
            event.getEntity().removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CURSE_OF_AGONY.get()));
        }
    }

    private static boolean canEntityBlock(DamageSource damageSource, LivingEntity living) {
        Vec3 vector3d2;
        AbstractArrow abstractarrowentity;
        Entity entity = damageSource.getDirectEntity();
        boolean flag = false;
        if (entity instanceof AbstractArrow && (abstractarrowentity = (AbstractArrow)entity).getPierceLevel() > 0) {
            flag = true;
        }
        if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR) && living.isBlocking() && !flag && (vector3d2 = damageSource.getSourcePosition()) != null) {
            Vec3 vector3d = living.getViewVector(1.0f);
            Vec3 vector3d1 = vector3d2.vectorTo(living.position()).normalize();
            vector3d1 = new Vec3(vector3d1.x, 0.0, vector3d1.z);
            if (vector3d1.dot(vector3d) < 0.0) {
                return true;
            }
        }
        return false;
    }

    private static int calculateFallDamage(LivingEntity entity, float distance, float damageMult) {
        MobEffectInstance effectinstance = entity.getEffect(MobEffects.JUMP);
        float f = effectinstance == null ? 0.0f : (float)(effectinstance.getAmplifier() + 1);
        return Mth.ceil((float)((distance - 3.0f - f) * damageMult));
    }

    private static void checkAndConsumeBonefeather(LivingFallEvent event, Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        int damage = DamageEventHandler.calculateFallDamage((LivingEntity)player, event.getDistance(), event.getDamageMultiplier());
        if ((float)damage > player.getHealth()) {
            ItemStack charmStack = new ItemStack((ItemLike)ItemInit.FALL_CHARM.get());
            boolean consumed_fall_charm = false;
            if (CuriosInterop.IsItemInCurioSlot((Item)ItemInit.FALL_CHARM.get(), (LivingEntity)player, SlotTypePreset.CHARM)) {
                consumed_fall_charm = true;
                CuriosInterop.DamageCurioInSlot((Item)ItemInit.FALL_CHARM.get(), (LivingEntity)player, SlotTypePreset.CHARM, 999);
            } else if (InventoryUtilities.removeItemFromInventory(charmStack, true, true, (IItemHandler)new InvWrapper((Container)player.getInventory()))) {
                consumed_fall_charm = true;
            }
            if (consumed_fall_charm) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                event.setCanceled(true);
                BonefeatherCharmUsedEvent bfe = new BonefeatherCharmUsedEvent(player);
                NeoForge.EVENT_BUS.post((Event)bfe);
                if (player instanceof ServerPlayer) {
                    CustomAdvancementTriggers.USE_CHARM.trigger((ServerPlayer)player, charmStack);
                    player.awardStat(Stats.ITEM_USED.get(((Item)ItemInit.FALL_CHARM.get())));
                    CustomAdvancementTriggers.SHAMAN.trigger((ServerPlayer)player);
                }
            }
        }
    }

    private static boolean checkAndConsumeReedfeather(LivingIncomingDamageEvent event, Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (event.getDamageSource().is(DamageTypeTags.IS_DROWNING)) {
            ItemStack charmStack = new ItemStack((ItemLike)ItemInit.DROWN_CHARM.get());
            boolean consumed_charm = false;
            if (CuriosInterop.IsItemInCurioSlot((Item)ItemInit.DROWN_CHARM.get(), (LivingEntity)player, SlotTypePreset.CHARM)) {
                consumed_charm = true;
                CuriosInterop.DamageCurioInSlot((Item)ItemInit.DROWN_CHARM.get(), (LivingEntity)player, SlotTypePreset.CHARM, 999);
            } else if (InventoryUtilities.removeItemFromInventory(charmStack, true, true, (IItemHandler)new InvWrapper((Container)player.getInventory()))) {
                consumed_charm = true;
            }
            if (consumed_charm) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                event.setCanceled(true);
                ReedfeatherCharmUsedEvent bfe = new ReedfeatherCharmUsedEvent(player);
                NeoForge.EVENT_BUS.post((Event)bfe);
                if (player instanceof ServerPlayer) {
                    CustomAdvancementTriggers.USE_CHARM.trigger((ServerPlayer)player, charmStack);
                    player.awardStat(Stats.ITEM_USED.get(((Item)ItemInit.DROWN_CHARM.get())));
                    CustomAdvancementTriggers.SHAMAN.trigger((ServerPlayer)player);
                }
                player.setAirSupply(player.getMaxAirSupply());
                if (!player.level().isClientSide()) {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1, 40));
                }
                return true;
            }
        }
        return false;
    }

    private static boolean checkAndConsumeCoalfeather(LivingIncomingDamageEvent event, Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (event.getDamageSource().is(DamageTypeTags.IS_FIRE) || event.getDamageSource().is(DamageTypes.LAVA)) {
            boolean consumed_charm = false;
            ItemStack charmStack = new ItemStack((ItemLike)ItemInit.BURN_CHARM.get());
            if (CuriosInterop.IsItemInCurioSlot((Item)ItemInit.BURN_CHARM.get(), (LivingEntity)player, SlotTypePreset.CHARM)) {
                consumed_charm = true;
                CuriosInterop.DamageCurioInSlot((Item)ItemInit.BURN_CHARM.get(), (LivingEntity)player, SlotTypePreset.CHARM, 999);
            } else if (InventoryUtilities.removeItemFromInventory(charmStack, true, true, (IItemHandler)new InvWrapper((Container)player.getInventory()))) {
                consumed_charm = true;
            }
            if (consumed_charm) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                event.setCanceled(true);
                CoalfeatherCharmUsedEvent bfe = new CoalfeatherCharmUsedEvent(player);
                NeoForge.EVENT_BUS.post((Event)bfe);
                if (player instanceof ServerPlayer) {
                    CustomAdvancementTriggers.USE_CHARM.trigger((ServerPlayer)player, charmStack);
                    player.awardStat(Stats.ITEM_USED.get(((Item)ItemInit.BURN_CHARM.get())));
                    CustomAdvancementTriggers.SHAMAN.trigger((ServerPlayer)player);
                }
                player.clearFire();
                int radius = 3;
                BlockUtils.IterateBlocksInCube(radius, player.blockPosition(), pos -> {
                    BlockState state = player.level().getBlockState(pos);
                    if (state.getBlock() instanceof BaseFireBlock) {
                        player.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    } else if (state.getFluidState().is((Fluid)Fluids.LAVA)) {
                        player.level().setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                    } else if (state.getFluidState().is((Fluid)Fluids.FLOWING_LAVA)) {
                        player.level().setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                    }
                });
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 1.0f);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Player resolveManaShieldSource(Entity target) {
        int id = target.getPersistentData().getInt("mana_shield_source_id");
        Entity res = target.level().getEntity(id);
        if (res != null && res instanceof Player) {
            float maxDist = target.getPersistentData().getFloat("mana_shield_range");
            if (res.distanceTo(target) <= maxDist) {
                return (Player)res;
            }
        }
        return null;
    }
}


