 
package com.mna.events;

import com.mna.tools.EnchantmentUtils;
import com.mna.ManaAndArtifice;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.entities.DamageHelper;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.tools.MATags;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.capabilities.particles.ParticleAuraProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.effects.EffectInit;
import com.mna.effects.beneficial.EffectCamouflage;
import com.mna.effects.interfaces.INoCreeperLingering;
import com.mna.enchantments.framework.EnchantmentInit;
import com.mna.entities.boss.BossMonster;
import com.mna.entities.boss.PigDragon;
import com.mna.entities.constructs.animated.Construct;
import com.mna.factions.Factions;
import com.mna.gui.containers.item.ContainerPractitionersPouch;
import com.mna.gui.containers.providers.NamedGuideBook;
import com.mna.interop.CuriosInterop;
import com.mna.items.ItemInit;
import com.mna.items.armor.CouncilArmorItem;
import com.mna.items.armor.DemonArmorItem;
import com.mna.items.artifice.EnderguardAmulet;
import com.mna.items.artifice.charms.ContingencyCharm;
import com.mna.items.artifice.curio.TrickeryBracelet;
import com.mna.items.relic.CrystallizedWind;
import com.mna.items.ritual.PractitionersPouch;
import com.mna.items.ritual.PractitionersPouchPatches;
import com.mna.items.runes.ItemRuneMarking;
import com.mna.items.sorcery.MagicStaff;
import com.mna.network.ServerMessageDispatcher;
import com.mna.spells.components.ComponentTrueInvisibility;
import com.mna.tools.EntityUtil;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.StructureUtils;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypePreset;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.mna.api.tools.MNACap;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.mna.api.events.RunicAnvilShouldActivateEvent.Result;
import com.mna.api.tools.MNADyeableItem;
import com.mna.api.tools.MNAItemData;

@EventBusSubscriber(modid="mna", bus=EventBusSubscriber.Bus.GAME)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && event.getItemStack().getItem() == Items.LEATHER) {
            CommonEventHandler.handleRightClick_Leather(player, event);
        }
    }

    private static void handleRightClick_Leather(Player player, PlayerInteractEvent.RightClickItem event) {
        BlockState state;
        ItemStack activestack = player.getItemInHand(event.getHand());
        float f = player.getXRot();
        float f1 = player.getYRot();
        Vec3 vec3d = player.getEyePosition(1.0f);
        float f2 = Mth.cos((float)(-f1 * ((float)Math.PI / 180) - (float)Math.PI));
        float f3 = Mth.sin((float)(-f1 * ((float)Math.PI / 180) - (float)Math.PI));
        float f4 = -Mth.cos((float)(-f * ((float)Math.PI / 180)));
        float f5 = Mth.sin((float)(-f * ((float)Math.PI / 180)));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue();
        Vec3 vec3d1 = vec3d.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);
        BlockHitResult raytraceresult = player.level().clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, (Entity)player));
        if (raytraceresult.getType() == HitResult.Type.BLOCK && (state = player.level().getBlockState(raytraceresult.getBlockPos())) == Blocks.WATER.defaultBlockState()) {
            ItemStack vellum = new ItemStack((ItemLike)ItemInit.VELLUM.get());
            if (!event.getEntity().addItem(vellum)) {
                event.getEntity().drop(vellum, true);
            }
            activestack.shrink(1);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!GeneralConfigValues.SummonInteractions && event.getEntity() instanceof LivingEntity && SummonUtils.isSummon((Entity)event.getEntity())) {
            event.setCanceled(true);
            event.setCanceled(true); event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            return;
        }
    }

    @SubscribeEvent
    public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
        BlockState state;
        ItemStack bookStack;
        BlockEntity te;
        if (!CommonEventHandler.handleWardBlockEvent(event.getLevel(), event.getPos(), event.getEntity())) {
            event.setCanceled(true);
            event.setCanceled(true); event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            return;
        }
        Level world = event.getLevel();
        if (!world.isClientSide && world.getBlockState(event.getPos()).getBlock() == Blocks.LECTERN && (te = world.getBlockEntity(event.getPos())) instanceof LecternBlockEntity && (bookStack = ((LecternBlockEntity)te).getBook()).getItem() == ItemInit.GUIDE_BOOK.get()) {
            event.setCanceled(true);
            if (event.getEntity().isShiftKeyDown()) {
                ((LecternBlockEntity)te).setBook(ItemStack.EMPTY);
                LecternBlock.resetBookState((Entity)event.getEntity(), (Level)world, (BlockPos)event.getPos(), (BlockState)world.getBlockState(event.getPos()), (boolean)false);
                if (!event.getEntity().addItem(bookStack)) {
                    event.getEntity().spawnAtLocation(bookStack);
                }
            } else if (!ManaAndArtificeMod.checkMagicUnlock((ServerLevel)world, event.getEntity())) {
                ((ServerPlayer)((ServerPlayer)event.getEntity())).openMenu((MenuProvider)new NamedGuideBook());
            }
            return;
        }
        Map map = EnchantmentUtils.enchantmentMap((ItemStack)event.getItemStack());
        if (map.containsKey(EnchantmentInit.RETURNING.get()) && (state = event.getLevel().getBlockState(event.getPos())).getBlock() != BlockInit.RUNIC_ANVIL.get()) {
            event.setCanceled(true);
        }
    }

    private static boolean handleWardBlockEvent(Level world, BlockPos pos, @Nullable Player player) {
        if (player != null && player.isCreative()) {
            return true;
        }
        LevelChunk chunkAccess = world.getChunkAt(pos);
        java.util.Optional<com.mna.api.capabilities.IChunkMagic> chunkmagic = MNACap.get(chunkAccess, ChunkMagicProvider.MAGIC);
        if (chunkmagic.isPresent() && ((IChunkMagic)chunkmagic.get()).blockInteractionFor(pos, player)) {
            if (!player.level().isClientSide()) {
                if (player.getPersistentData().getInt("warded_notification_cooldown") == 0) {
                    player.sendSystemMessage((Component)Component.translatable((String)"helptip.mna.block_warded").withStyle(ChatFormatting.AQUA));
                    player.getPersistentData().putInt("warded_notification_cooldown", 10);
                }
                ServerMessageDispatcher.sendRenderWard((ServerPlayer)player, pos.asLong());
            }
            return false;
        }
        return true;
    }

    private static void handleBossArenaBlockEvent(Level world, BlockEvent event, @Nullable Player player) {
        if (GeneralConfigValues.DisableBossArenaProtection || player != null && player.isCreative()) {
            return;
        }
        if (event.getState().getBlock() instanceof ChalkRuneBlock) {
            return;
        }
        if (world instanceof ServerLevel && StructureUtils.isPointInAnyStructure((ServerLevel)world, event.getPos(), MATags.Structures.BOSS_ARENAS)) {
            int bosses;
            if (player != null && (bosses = world.getEntities((Entity)player, new AABB(event.getPos()).inflate(64.0), e -> e.isAlive() && e instanceof BossMonster).size()) == 0 && player.getPersistentData().getInt("boss_arena_notification_cooldown") == 0) {
                player.sendSystemMessage((Component)Component.translatable((String)"helptip.mna.boss_break_denied").withStyle(ChatFormatting.AQUA));
                player.getPersistentData().putInt("boss_arena_notification_cooldown", 1200);
            }
            ((net.neoforged.bus.api.ICancellableEvent)event).setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        Level world = event.getLevel();
        for (int i = 0; i < 7; ++i) {
            BlockPos pos;
            BlockPos blockPos = pos = i == 0 ? BlockPos.containing((Position)event.getExplosion().center()) : CommonEventHandler.getExplosionExtent(event.getExplosion(), Direction.values()[i - 1]);
            if (!(world instanceof ServerLevel) || !StructureUtils.isPointInAnyStructure((ServerLevel)world, pos, MATags.Structures.BOSS_ARENAS)) continue;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Level world = (Level)event.getLevel();
        CommonEventHandler.handleBossArenaBlockEvent(world, (BlockEvent)event, event.getPlayer());
        if (!CommonEventHandler.handleWardBlockEvent(world, event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onVanillaGameEvent(VanillaGameEvent event) {
        if (event.getVanillaEvent() == GameEvent.BLOCK_DESTROY) {
            BlockPos pos = BlockPos.containing((Position)event.getEventPosition());
            LevelChunk chunkAccess = event.getLevel().getChunkAt(pos);
            MNACap.get(chunkAccess, ChunkMagicProvider.MAGIC).ifPresent(c -> c.removeBlockWard(pos));
        }
    }

    @SubscribeEvent
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        CommonEventHandler.handleBossArenaBlockEvent((Level)event.getLevel(), (BlockEvent)event, null);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        CommonEventHandler.handleBossArenaBlockEvent((Level)event.getLevel(), (BlockEvent)event, event.getEntity() instanceof Player ? (Player)event.getEntity() : null);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        Optional trickery;
        int wardCD;
        int breakCD;
        Player player = event.getEntity();
        if (player.getPersistentData().contains("mna:flung")) {
            boolean flingEffect;
            long flingtime = player.getPersistentData().getLong("mna:fling_time");
            boolean bl = flingEffect = player.level().getGameTime() - flingtime >= 3L;
            if (player.isInWater() || player.onGround()) {
                if (flingEffect) {
                    player.getPersistentData().remove("mna:flung");
                }
            } else if (player.horizontalCollision) {
                if (!CuriosInterop.IsItemInCurioSlot((Item)ItemInit.AIR_CAST_RING.get(), (LivingEntity)player, SlotTypePreset.RING) && !CrystallizedWind.FindOn((LivingEntity)player) && flingEffect) {
                    player.hurt(player.damageSources().flyIntoWall(), player.getPersistentData().getFloat("mna:flung") * 3.0f);
                    player.level().playSound(null, player.position().x, player.position().y, player.position().z, SFX.Entity.Spellbreaker.SHIELD_BASH, SoundSource.PLAYERS, 0.3f, 1.0f);
                }
                player.getPersistentData().remove("mna:flung");
            }
        }
        if ((breakCD = player.getPersistentData().getInt("boss_arena_notification_cooldown")) > 0) {
            player.getPersistentData().putInt("boss_arena_notification_cooldown", --breakCD);
        }
        if ((wardCD = player.getPersistentData().getInt("warded_notification_cooldown")) > 0) {
            player.getPersistentData().putInt("warded_notification_cooldown", --wardCD);
        }
        if (player.getPersistentData().contains("mna_remove_flight")) {
            int remaining = player.getPersistentData().getInt("mna_remove_flight") - 1;
            ManaAndArtifice.instance.proxy.setFlightEnabled(player, false);
            if (remaining == 0) {
                player.getPersistentData().remove("mna_remove_flight");
            } else {
                player.getPersistentData().putInt("mna_remove_flight", remaining);
            }
        }
        if (!player.onGround() && player.getDeltaMovement().y < 0.0 && player.fallDistance > 4.0f && player instanceof ServerPlayer && !player.hasEffect(MobEffects.SLOW_FALLING) && (double)CommonEventHandler.getDistanceToGround(player) <= Math.abs(player.getDeltaMovement().y * 10.0)) {
            ContingencyCharm.CheckAndConsumeCharmCharge((ServerPlayer)player, ContingencyCharm.ContingencyEvent.FALL);
        }
        if ((trickery = CuriosApi.getCuriosHelper().findCurios((LivingEntity)event.getEntity(), (Item)ItemInit.TRICKERY_BRACELET.get()).stream().findFirst()).isPresent()) {
            boolean crouching = event.getEntity().isShiftKeyDown();
            if (crouching) {
                TrickeryBracelet.ApplyMoveSpeed(player);
            } else {
                TrickeryBracelet.RemoveMoveSpeed(player);
            }
            player.setInvisible(crouching || EntityUtil.shouldBeInvisible((LivingEntity)player));
        } else {
            TrickeryBracelet.RemoveMoveSpeed(player);
        }
        if (!event.getEntity().level().isClientSide() && event.getEntity().isInWater() && event.getEntity().hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get())) && event.getEntity().level().getGameTime() % 20L == 0L) {
            event.getEntity().hurt(DamageHelper.forType(DamageHelper.DISPERSE, event.getEntity().level().registryAccess()), 5.0f);
        }
        if (event.getEntity().hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get())) && event.getEntity().getPersistentData().contains("posessed_entity_id")) {
            int id = event.getEntity().getPersistentData().getInt("posessed_entity_id");
            Entity e = event.getEntity().level().getEntity(id);
            if (e == null || !(e instanceof Mob) || !e.isAlive()) {
                event.getEntity().getPersistentData().remove("posessed_entity_id");
                event.getEntity().removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()));
            }
        }
        MNACap.get(event.getEntity(), PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getAlliedFaction() == Factions.UNDEAD) {
                MobEffectInstance existing;
                int frostWalkerLevelOH;
                int frostWalkerLevelMH;
                int frostWalkerLevel;
                if (CommonEventHandler.isPlayerInDaylight(event.getEntity()) && !event.getEntity().isInWaterRainOrBubble() && !event.getEntity().hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()))) {
                    if (event.getEntity().hasItemInSlot(EquipmentSlot.HEAD)) {
                        if (event.getEntity().level().getRandom().nextFloat() < 0.05f) {
                            ItemStack headslot = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
                            headslot.hurtAndBreak(1, (LivingEntity)event.getEntity(), EquipmentSlot.HEAD);
                        }
                    } else {
                        event.getEntity().igniteForSeconds((float)(8));
                    }
                } else if (event.getEntity().isInWater() && event.getEntity().getEffect(MobEffects.WATER_BREATHING) == null) {
                    event.getEntity().addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 1, false, false, true));
                }
                if (!((frostWalkerLevel = Math.max(frostWalkerLevelMH = player.getMainHandItem().getItem() instanceof MagicStaff ? EnchantmentHelper.getEnchantmentLevel(player.level().registryAccess().holderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FROST_WALKER), player) : 0, frostWalkerLevelOH = player.getOffhandItem().getItem() instanceof MagicStaff ? EnchantmentHelper.getEnchantmentLevel(player.level().registryAccess().holderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FROST_WALKER), player) : 0)) <= 0 || event.getEntity().level().isClientSide() || (existing = player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.AURA_OF_FROST.get()))) != null && existing.getDuration() >= 20)) {
                    player.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.AURA_OF_FROST.get()), 201, frostWalkerLevel));
                }
            }
        });
        if (((CouncilArmorItem)ItemInit.COUNCIL_ARMOR__CHEST.get()).isSetEquipped((LivingEntity)event.getEntity())) {
            CouncilArmorItem.tickReflectCharges(event.getEntity());
        }
        if (event.getEntity().level().isClientSide()) {
            MNACap.get(event.getEntity(), ParticleAuraProvider.AURA).ifPresent(a -> {
                a.requestIfNeeded(player);
                Minecraft m = Minecraft.getInstance();
                if (!m.options.getCameraType().isFirstPerson() || a.showInFirstPerson()) {
                    a.spawn(event.getEntity().level(), event.getEntity().position(), Vec3.directionFromRotation((Vec2)new Vec2(event.getEntity().getXRot(), event.getEntity().yBodyRot)));
                }
            });
            if (mnaSpinAttackTicks(event.getEntity()) > 0 && event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem() == ItemInit.HELLFIRE_TRIDENT.get()) {
                Vec3 vel = event.getEntity().getDeltaMovement();
                for (int i = 0; i < 5; ++i) {
                    event.getEntity().level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.HELLFIRE.get())), event.getEntity().getX() + Math.random() * vel.x, event.getEntity().getY() + Math.random() * vel.y, event.getEntity().getZ() + Math.random() * vel.z, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    private static boolean mnaHasNoCreeperLingering(AreaEffectCloud cloud) {
        try {
            java.lang.reflect.Field f = AreaEffectCloud.class.getDeclaredField("potionContents");
            f.setAccessible(true);
            net.minecraft.world.item.alchemy.PotionContents pc = (net.minecraft.world.item.alchemy.PotionContents)f.get(cloud);
            return pc != null && pc.getAllEffects().stream().anyMatch(e -> ((net.minecraft.world.effect.MobEffectInstance)e).getEffect().value() instanceof INoCreeperLingering);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static int mnaSpinAttackTicks(LivingEntity entity) {
        try {
            java.lang.reflect.Field f = LivingEntity.class.getDeclaredField("autoSpinAttackTicks");
            f.setAccessible(true);
            return (Integer)f.get(entity);
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    @SubscribeEvent
    public static void onPlayerPickupItem(ItemEntityPickupEvent.Pre event) {
        ItemStack invStack;
        Optional<SlotResult> curioSlot;
        ItemStack pickedUpItem = event.getItemEntity().getItem();
        int originalQty = pickedUpItem.getCount();
        Inventory inv = event.getPlayer().getInventory();
        boolean modifiedCollection = false;
        int openSlot = -1;
        AbstractContainerMenu abstractContainerMenu = event.getPlayer().containerMenu;
        if (abstractContainerMenu instanceof ContainerPractitionersPouch) {
            ContainerPractitionersPouch cpp = (ContainerPractitionersPouch)abstractContainerMenu;
            openSlot = event.getPlayer().getInventory().selected;
        }
        if ((curioSlot = CuriosInterop.GetSingleItem((LivingEntity)event.getPlayer(), (Item)ItemInit.PRACTITIONERS_POUCH.get(), (Item)ItemInit.MITHIONS_MAGNIFICENT_MBAG.get())).isPresent() && !(invStack = curioSlot.get().stack()).isEmpty() && invStack.getItem() instanceof PractitionersPouch) {
            PractitionersPouch pouchItem = (PractitionersPouch)invStack.getItem();
            if (pouchItem.getPatchLevel(invStack, PractitionersPouchPatches.COLLECTION) > 0 && pouchItem.hasRoomForItem(invStack, pickedUpItem)) {
                boolean bl = modifiedCollection = originalQty != (pickedUpItem = pouchItem.insertItem(invStack, pickedUpItem)).getCount();
            }
            if (pickedUpItem.isEmpty()) {
                event.getPlayer().take((Entity)event.getItemEntity(), originalQty);
                event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
                event.getItemEntity().remove(Entity.RemovalReason.DISCARDED);
                return;
            }
            if (pouchItem.getPatchLevel(invStack, PractitionersPouchPatches.VOID) > 0 && pouchItem.shouldVoidItem(invStack, pickedUpItem)) {
                event.getPlayer().take((Entity)event.getItemEntity(), originalQty);
                event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
                event.getItemEntity().remove(Entity.RemovalReason.DISCARDED);
                return;
            }
        }
        if (!pickedUpItem.isEmpty()) {
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack invStack2;
                if (openSlot == i || (invStack2 = inv.getItem(i)).isEmpty() || !(invStack2.getItem() instanceof PractitionersPouch)) continue;
                PractitionersPouch pouchItem = (PractitionersPouch)invStack2.getItem();
                if (pouchItem.getPatchLevel(invStack2, PractitionersPouchPatches.COLLECTION) > 0 && pouchItem.hasRoomForItem(invStack2, pickedUpItem)) {
                    boolean bl = modifiedCollection = originalQty != (pickedUpItem = pouchItem.insertItem(invStack2, pickedUpItem)).getCount();
                }
                if (pickedUpItem.isEmpty()) {
                    event.getPlayer().take((Entity)event.getItemEntity(), originalQty);
                    event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
                    event.getItemEntity().remove(Entity.RemovalReason.DISCARDED);
                    return;
                }
                if (pouchItem.getPatchLevel(invStack2, PractitionersPouchPatches.VOID) <= 0 || !pouchItem.shouldVoidItem(invStack2, pickedUpItem)) continue;
                event.getPlayer().take((Entity)event.getItemEntity(), originalQty);
                event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
                event.getItemEntity().remove(Entity.RemovalReason.DISCARDED);
                return;
            }
        }
        if (modifiedCollection && !pickedUpItem.isEmpty()) {
            event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
            event.getItemEntity().setItem(pickedUpItem);
            ;
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent event) {
        MobEffectInstance effect;
        LivingEntity living = (LivingEntity)event.getEntity();
        if (living.getPersistentData().contains("mna_remove_ginvis")) {
            int remaining = living.getPersistentData().getInt("mna_remove_ginvis") - 1;
            EntityUtil.removeInvisibility(living);
            if (remaining == 0) {
                living.getPersistentData().remove("mna_remove_ginvis");
            } else {
                living.getPersistentData().putInt("mna_remove_ginvis", remaining);
            }
        }
        if (living.getPersistentData().contains("mna:flung")) {
            if (living.isInWater()) {
                living.getPersistentData().remove("mna:flung");
            } else if (living.horizontalCollision) {
                if (!living.onGround()) {
                    long flingtime = living.getPersistentData().getLong("mna:fling_time");
                    if (living.level().getGameTime() - flingtime >= 3L) {
                        living.hurt(DamageHelper.createSourcedType((ResourceKey<DamageType>)DamageTypes.FLY_INTO_WALL, living.level().registryAccess(), (Entity)living), living.getPersistentData().getFloat("mna:flung") * 3.0f);
                        living.level().playSound(null, living.position().x, living.position().y, living.position().z, SFX.Entity.Spellbreaker.SHIELD_BASH, SoundSource.HOSTILE, 0.3f, 1.0f);
                    }
                }
                living.getPersistentData().remove("mna:flung");
            }
        }
        boolean isInWater = living.isInWater();
        boolean isWet = living.isInWaterRainOrBubble();
        if (living.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.WATERY_GRAVE.get())) != null && isInWater) {
            living.push(0.0, -0.2, 0.0);
        }
        if (!(living.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GRAVITY_WELL.get())) == null || living.onGround() || isInWater || living.isInLava())) {
            living.push(0.0, -0.2, 0.0);
        }
        if (!living.hasEffect(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()))) {
            Player player;
            if (!(!isWet || living instanceof Player && (player = (Player)living).isSpectator())) {
                living.addEffect(new MobEffectInstance(com.mna.api.tools.MAEffects.holder(EffectInit.SOAKED.get()), 300, 0));
            }
        } else if (living.isOnFire()) {
            living.setRemainingFireTicks(0);
        }
        if (living instanceof Mob) {
            Mob monster = (Mob)living;
            LivingEntity target = monster.getTarget();
            if (target != null) {
                Construct construct;
                MobEffectInstance effect2 = target.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GREATER_INVISIBILITY.get()));
                boolean clearTarget = false;
                if (effect2 != null && !ComponentTrueInvisibility.IsDisabledFor(target) && monster.distanceToSqr((Entity)target) > 3.0) {
                    clearTarget = true;
                }
                if ((effect2 = target.getEffect(MobEffects.INVISIBILITY)) != null && monster.distanceToSqr((Entity)target) > 16.0) {
                    clearTarget = true;
                }
                if ((effect2 = target.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CAMOUFLAGE.get()))) != null) {
                    float camoPercent = EffectCamouflage.getCamoflagePercent(target);
                    float disengageDistance = Mth.lerp((float)camoPercent, (float)8.0f, (float)50.0f);
                    if (monster.distanceToSqr((Entity)target) > (double)disengageDistance) {
                        clearTarget = true;
                    }
                }
                if ((effect2 = target.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.POSSESSION.get()))) != null && effect2.getAmplifier() == 2) {
                    clearTarget = true;
                }
                if (target instanceof Construct && (construct = (Construct)target).isDefeated()) {
                    clearTarget = true;
                }
                if (clearTarget) {
                    monster.setTarget(null);
                }
            }
            if (SummonUtils.isSummon((Entity)monster) && SummonUtils.getSummoner((LivingEntity)monster) == null) {
                monster.kill();
            }
        }
        if (living.level().isClientSide() && living.isFallFlying()) {
            int count;
            BlockPos pos = living.blockPosition();
            BlockState state = living.level().getBlockState(pos);
            for (count = 0; state.getBlock() != Blocks.WATER && count < 3; ++count) {
                pos = pos.below();
                state = living.level().getBlockState(pos);
            }
            if (count < 3) {
                Vec3 c = Vec3.atCenterOf((Vec3i)pos).add(-0.1, 0.5, -0.1);
                for (int i = 0; i < 5; ++i) {
                    Vec3 tempPos = c.add(Math.random() * 0.2, 0.0, Math.random() * 0.2);
                    living.level().addParticle((ParticleOptions)ParticleTypes.SPLASH, tempPos.x, tempPos.y, tempPos.z, 0.0, 0.25, 0.0);
                }
            }
        }
        float desiredScale = 1.0f;
        CompoundTag tag = living.getPersistentData();
        float actualScale = tag.getFloat("mna_entity_scale");
        float prevScale = tag.getFloat("mna_entity_scale_prev");
        if (actualScale <= 0.0f) {
            actualScale = 1.0f;
        }
        if ((effect = living.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.REDUCE.get()))) != null) {
            desiredScale -= 0.1f * (float)(effect.getAmplifier() + 1);
        }
        if ((effect = living.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.ENLARGE.get()))) != null) {
            Player player;
            desiredScale += 0.1f * (float)(effect.getAmplifier() + 1);
            if (living instanceof Player && (player = (Player)living).getGameProfile() != null && player.getGameProfile().getId() != null && player.getGameProfile().getId().toString() == "c10026c1-3b38-401a-8e4a-590cba8037b4") {
                desiredScale += 0.5f * (float)(effect.getAmplifier() + 1);
            }
        }
        if (desiredScale < 0.2f) {
            desiredScale = 0.2f;
        } else if (desiredScale > 5.0f) {
            desiredScale = 5.0f;
        }
        if (desiredScale != actualScale) {
            float delta;
            float f = delta = desiredScale < actualScale ? -0.1f : 0.1f;
            if (Math.abs(desiredScale - actualScale) < 0.1f) {
                actualScale = desiredScale;
                delta = 0.0f;
            }
            tag.putFloat("mna_entity_scale_prev", actualScale);
            tag.putFloat("mna_entity_scale", actualScale + delta);
            living.refreshDimensions();
        } else if (actualScale != prevScale) {
            tag.putFloat("mna_entity_scale_prev", actualScale);
            tag.putFloat("mna_entity_scale", actualScale);
            living.refreshDimensions();
        }
    }

    @SubscribeEvent
    public static void onEntityChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target != null && entity instanceof Mob) {
            Construct construct;
            Mob monster = (Mob)entity;
            boolean clearTarget = false;
            MobEffectInstance effect = target.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.GREATER_INVISIBILITY.get()));
            if (effect != null && !ComponentTrueInvisibility.IsDisabledFor(event.getEntity()) && monster.distanceToSqr((Entity)target) > 3.0) {
                clearTarget = true;
            }
            if ((effect = target.getEffect(MobEffects.INVISIBILITY)) != null && monster.distanceToSqr((Entity)target) > 16.0) {
                clearTarget = true;
            }
            if ((effect = target.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CAMOUFLAGE.get()))) != null) {
                float camoPercent = EffectCamouflage.getCamoflagePercent(target);
                float disengageDistance = Mth.lerp((float)camoPercent, (float)8.0f, (float)50.0f);
                if (monster.distanceToSqr((Entity)target) > (double)disengageDistance) {
                    clearTarget = true;
                }
            }
            if (target instanceof Construct && (construct = (Construct)target).isDefeated()) {
                clearTarget = true;
            }
            if (clearTarget) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntitySizeChange(EntityEvent.Size event) {
        LivingEntity living;
        float scale;
        if (event.getEntity().isAddedToLevel() && event.getEntity() instanceof LivingEntity && (scale = (living = (LivingEntity)event.getEntity()).getPersistentData().getFloat("mna_entity_scale")) != 1.0f) {
            event.setNewSize(event.getNewSize().scale(scale));
        }
    }

 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity().getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHRONO_ANCHOR.get())) != null) {
            event.getEntity().removeEffect(com.mna.api.tools.MAEffects.holder(EffectInit.CHRONO_ANCHOR.get()));
        }
        SummonUtils.getSummons((LivingEntity)event.getEntity(), (Level)event.getEntity().getServer().getLevel(event.getFrom())).forEach(s -> s.kill());
        MNACap.get(event.getEntity(), PlayerMagicProvider.MAGIC).ifPresent(m -> {
            m.setSyncGrimoire();
            m.setSyncRote();
            m.getCantripData().setNeedsSync();
            m.getCastingResource().setNeedsSync();
            m.forceSync();
        });
        ServerMessageDispatcher.sendWellspringPowerNetworkSyncMessage((ResourceKey<Level>)event.getTo(), (ServerPlayer)event.getEntity(), true);
    }

    @SubscribeEvent
    public static void onPlayerSleep(PlayerWakeUpEvent event) {
        BlockPos bp = event.getEntity().blockPosition();
        BlockState bedState = event.getEntity().level().getBlockState(bp);
        if (bedState.getBlock() == BlockInit.COFFIN.get()) {
            Player player = event.getEntity();
            MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                if (p.getAlliedFaction() == Factions.UNDEAD) {
                    long lastRestTime = player.getPersistentData().getLong("last_coffin_boost");
                    long curTime = player.level().getGameTime();
                    float pct = MathUtils.clamp01((float)(curTime - lastRestTime) / (float)GeneralConfigValues.UndeadCoffinSoulsDelay);
                    MNACap.get(event.getEntity(), PlayerMagicProvider.MAGIC).ifPresent(m -> {
                        m.getCastingResource().restore((float)((double)m.getCastingResource().getMaxAmount() * GeneralConfigValues.UndeadCoffinSoulsRestored * (double)pct));
                        player.getPersistentData().putLong("last_coffin_boost", curTime);
                    });
                }
            });
        }
    }

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent event) {
        Creeper creeper;
        Optional player;
        Entity entity = event.getExplosion().getDirectSourceEntity();
        if (entity instanceof Creeper && (player = (creeper = (Creeper)entity).level().getEntitiesOfClass(Player.class, creeper.getBoundingBox().inflate(5.0), p -> ((EnderguardAmulet)ItemInit.ENDERGUARD_AMULET.get()).isEquippedAndHasMana((LivingEntity)p, 50.0f, false)).stream().findFirst()).isPresent()) {
            CuriosApi.getCuriosHelper().findFirstCurio((LivingEntity)player.get(), (Item)ItemInit.ENDERGUARD_AMULET.get()).ifPresent(t -> {
                if (((EnderguardAmulet)ItemInit.ENDERGUARD_AMULET.get()).consumeMana(t.stack(), 50.0f, (Player)player.get())) {
                    int count = 0;
                    while (count++ < 50) {
                        double z;
                        double y;
                        double x = creeper.getX() + (creeper.getRandom().nextDouble() - 0.5) * 32.0;
                        Vec3 newPos = new Vec3(x, y = Mth.clamp((double)(creeper.getY() + (double)(creeper.getRandom().nextInt(16) - 8)), (double)0.0, (double)(creeper.level().getHeight() - 1)), z = creeper.getZ() + (creeper.getRandom().nextDouble() - 0.5) * 32.0);
                        if (!(newPos.distanceToSqr(((Player)player.get()).position()) >= 36.0) || !creeper.randomTeleport(x, y, z, true)) continue;
                        ((net.neoforged.bus.api.ICancellableEvent)event).setCanceled(true);
                        float f = creeper.isPowered() ? 2.0f : 1.0f;
                        creeper.level().explode(null, creeper.getX(), creeper.getY(), creeper.getZ(), 3.0f * f, Level.ExplosionInteraction.MOB);
                        creeper.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        break;
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Entity mount = event.getEntity().getVehicle();
        if (mount != null && mount instanceof Construct) {
            ((Construct)mount).soundHorn();
        }
    }

    @SubscribeEvent
    public static void onPlayerSize(EntityEvent.Size event) {
        MobEffectInstance mistForm;
        Player player;
        if (event.getEntity() instanceof Player && (player = (Player)event.getEntity()).getActiveEffectsMap() != null && (mistForm = player.getEffect(com.mna.api.tools.MAEffects.holder(EffectInit.MIST_FORM.get()))) != null && mistForm.getDuration() > 5) {
            event.setNewSize(EntityDimensions.fixed((float)0.5f, (float)0.5f));
        }
    }

    @SubscribeEvent
    public static void onEntityDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof PathfinderMob && SummonUtils.isSummon((Entity)event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityDropXP(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof PathfinderMob && SummonUtils.isSummon((Entity)event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerPickupXP(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            int enchantMagnitude;
            if (p.getAlliedFaction() == Factions.UNDEAD && (enchantMagnitude = EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.XP_TO_SOULS, (LivingEntity)player)) > 0) {
                event.setCanceled(true);
                ExperienceOrb orb = event.getOrb();
                player.takeXpDelay = 2;
                player.take((Entity)orb, 1);
                int souls = orb.value * 5 * enchantMagnitude;
                orb.value /= 2;
                if (orb.value > 0) {
                    player.giveExperiencePoints(orb.value);
                }
                orb.remove(Entity.RemovalReason.KILLED);
                MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCastingResource().restore(souls));
            }
        });
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AreaEffectCloud && mnaHasNoCreeperLingering((AreaEffectCloud)event.getEntity())) {
            event.setCanceled(true);
        }
        if (event.getEntity() instanceof LivingEntity) {
            event.getEntity().getPersistentData().putFloat("mna_entity_scale", 1.0f);
            event.getEntity().getPersistentData().putFloat("mna_entity_scale_prev", 1.0f);
        }
    }

 
 
 
 
 
 
 
 

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Entity owner;
        if (!event.getProjectile().level().isClientSide() && event.getProjectile() instanceof AbstractArrow && event.getProjectile().getPersistentData().contains("return_on_next_arrow_shot") && (owner = ((AbstractArrow)event.getProjectile()).getOwner()) != null && owner instanceof Player) {
            if (((AbstractArrow)event.getProjectile()).pickup == AbstractArrow.Pickup.CREATIVE_ONLY && !((Player)owner).isCreative()) {
                return;
            }
            if (((AbstractArrow)event.getProjectile()).pickup == AbstractArrow.Pickup.DISALLOWED) {
                return;
            }
            ItemStack returnStack = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)((CompoundTag)event.getProjectile().getPersistentData().get("return_stack")));
            if (!returnStack.isEmpty() && !((Player)owner).isCreative()) {
                boolean drop = true;
                if (event.getProjectile().getPersistentData().getBoolean("cannon_shot")) {
                    drop = !(returnStack = InventoryUtilities.mergeToPlayerInvPrioritizeOffhand((Player)owner, returnStack)).isEmpty();
                } else {
                    boolean bl = drop = !((Player)owner).addItem(returnStack);
                }
                if (drop) {
                    ((Player)owner).drop(returnStack, true);
                }
            }
            event.getProjectile().discard();
        }
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        ItemStack itemstack;
        if (event.hasAmmo() && event.getEntity() instanceof Player && !(itemstack = event.getEntity().getProjectile(event.getBow())).isEmpty() && EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.RETURNING, (LivingEntity)event.getEntity()) > 0) {
            event.getEntity().getPersistentData().putBoolean("return_on_next_arrow_shot", true);
            ItemStack toReturn = itemstack.copy();
            toReturn.setCount(1);
            event.getEntity().getPersistentData().put("return_stack", (Tag)com.mna.api.tools.MNAItemData.save(toReturn, new CompoundTag()));
            return;
        }
        event.getEntity().getPersistentData().remove("return_on_next_arrow_shot");
    }

    @SubscribeEvent
    public static void onEntityAddedToWorld(EntityJoinLevelEvent event) {
        EnderDragon dragon;
        EndDragonFight fight;
        AbstractArrow arrow;
        if (event.getEntity() instanceof AbstractArrow && (arrow = (AbstractArrow)event.getEntity()).getOwner() != null && arrow.getOwner().getPersistentData().contains("return_on_next_arrow_shot")) {
            arrow.getPersistentData().putBoolean("return_on_next_arrow_shot", true);
            if (arrow.getOwner().getPersistentData().contains("return_stack")) {
                arrow.getPersistentData().put("return_stack", arrow.getOwner().getPersistentData().get("return_stack"));
            }
            arrow.getOwner().getPersistentData().remove("return_on_next_arrow_shot");
            arrow.getOwner().getPersistentData().remove("return_stack");
        }
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof EnderDragon && (fight = (dragon = (EnderDragon)event.getEntity()).getDragonFight()) != null && fight.hasPreviouslyKilledDragon()) {
            AABB search = dragon.getBoundingBox().inflate(256.0);
            List<ItemEntity> items = dragon.level().getEntities((Entity)dragon, search, e -> e instanceof ItemEntity).stream().map(e -> (ItemEntity)e).collect(Collectors.toList());
            boolean foundChop = items.stream().anyMatch(e -> e.getItem().getItem() == Items.PORKCHOP || e.getItem().getItem() == Items.COOKED_PORKCHOP);
            boolean foundCookie = items.stream().anyMatch(e -> e.getItem().getItem() == Items.COOKIE);
            if (foundChop && foundCookie) {
                event.setCanceled(true);
                PigDragon pd = new PigDragon(dragon.level());
                pd.setPos(dragon.position());
                dragon.level().addFreshEntity((Entity)pd);
                dragon.remove(Entity.RemovalReason.DISCARDED);
                fight.setDragonKilled(dragon);
            } else {
                dragon.level().getNearbyPlayers(new TargetingConditions(false).ignoreInvisibilityTesting().ignoreLineOfSight().range(256.0), null, search).stream().forEach(p -> p.sendSystemMessage((Component)Component.translatable((String)"helptip.mna.pigdragon").withStyle(ChatFormatting.AQUA)));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingGearChange(LivingEquipmentChangeEvent event) {
        int newLoyalty = com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(event.getEntity().level().registryAccess().holderOrThrow(Enchantments.LOYALTY), event.getFrom());
        SummonUtils.setBonusSummons(event.getEntity(), newLoyalty);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        Player player = event.getEntity();
        Player original = event.getOriginal();
        CompoundTag soulbound_inventory = original.getPersistentData().getCompound("mna_soulbound_inventory");
        CompoundTag soulbound_curios = original.getPersistentData().getCompound("mna_soulbound_curios");
        CompoundTag last_death_data = original.getPersistentData().getCompound("mna_last_death_data");
        for (int i = 0; i < original.getInventory().getContainerSize(); ++i) {
            if (!soulbound_inventory.contains("slot_" + i)) continue;
            CompoundTag itemTag = soulbound_inventory.getCompound("slot_" + i);
            ItemStack soulboundStack = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)itemTag);
            player.getInventory().setItem(i, soulboundStack);
        }
        CuriosApi.getCuriosHelper().getCuriosHandler((LivingEntity)player).ifPresent(c -> c.getCurios().forEach((identifier, stackHandler) -> {
            if (soulbound_curios.contains(identifier)) {
                CompoundTag tagForIdentifier = soulbound_curios.getCompound(identifier);
                for (int i = 0; i < stackHandler.getSlots(); ++i) {
                    if (!tagForIdentifier.contains("slot_" + i)) continue;
                    CompoundTag itemTag = tagForIdentifier.getCompound("slot_" + i);
                    ItemStack soulboundStack = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)itemTag);
                    stackHandler.getStacks().setStackInSlot(i, soulboundStack);
                }
            }
        }));
        player.getPersistentData().put("mna_last_death_data", (Tag)last_death_data);
        player.getPersistentData().remove("mna_soulbound_inventory");
        player.getPersistentData().remove("mna_soulbound_curios");
    }

     
     
     
     

    @SubscribeEvent
    public static void onAdvancementComplete(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            ServerMessageDispatcher.sendAdvancementUnlocked(sp, event.getAdvancement().id());
        }
    }

    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            Level world = living.level();
            MNACap.get(world, WorldMagicProvider.MAGIC).ifPresent(m -> {
                if (m.isInterdicted(living, event.getPrev(), event.getTarget())) {
                    event.setCanceled(true);
                    if (living instanceof Player) {
                        Player player = (Player)living;
                        player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.interdicted"));
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack outputStack = event.getCrafting();
        Container inventory = event.getInventory();
        ItemStack inputRune = ItemStack.EMPTY;
        int itemCount = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack ingredient = inventory.getItem(i);
            if (!ingredient.isEmpty()) {
                ++itemCount;
            }
            if (!(ingredient.getItem() instanceof ItemRuneMarking)) continue;
            inputRune = ingredient;
        }
        if (itemCount == 1 && outputStack.getItem() instanceof ItemRuneMarking) {
            MNADyeableItem.setColor(outputStack, MNADyeableItem.getColor(inputRune));
        }
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent event) {
        if (true) {
            MNACap.get(event.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> m.getWellspringRegistry().resetDiminishing());
            int chunkRadius = 5;
            float decayRate = 0.01f;
            HashSet<Long> tickedChunks = new HashSet<Long>();
            for (Player player : event.getLevel().players()) {
                ChunkPos chunkPos = player.chunkPosition();
                for (int i = -chunkRadius; i <= chunkRadius; ++i) {
                    for (int j = -chunkRadius; j <= chunkRadius; ++j) {
                        long currentChunkCoord;
                        if (!event.getLevel().hasChunk(chunkPos.x + i, chunkPos.z + j) || tickedChunks.contains(currentChunkCoord = new ChunkPos(chunkPos.x + i, chunkPos.z + j).toLong())) continue;
                        tickedChunks.add(currentChunkCoord);
                        LevelChunk chunkData = event.getLevel().getChunk(chunkPos.x + i, chunkPos.z + j);
                        IChunkMagic chunkMagic = (IChunkMagic)chunkData.getData(ChunkMagicProvider.MAGIC.get());
                        if (chunkMagic == null || !(chunkMagic.getResidualMagic() > 0.0f)) continue;
                        chunkMagic.removeResidualMagic(decayRate);
                    }
                }
            }
        }
    }

    private static float getDistanceToGround(Player player) {
        float distance = 0.0f;
        BlockPos pos = player.blockPosition();
        while (pos.getY() > player.level().getMinBuildHeight() && !player.level().getBlockState(pos).entityCanStandOnFace((BlockGetter)player.level(), pos, (Entity)player, Direction.UP)) {
            distance += 1.0f;
            pos = pos.below();
        }
        return distance;
    }

    private static boolean isPlayerInDaylight(Player player) {
        if (player.level().isDay() && !player.level().isClientSide()) {
            BlockPos blockpos;
            float f = player.getLightLevelDependentMagicValue();
            BlockPos blockPos = blockpos = player.getVehicle() instanceof Boat ? BlockPos.containing((double)player.getX(), (double)Math.round(player.getY()), (double)player.getZ()).above() : BlockPos.containing((double)player.getX(), (double)Math.round(player.getY()), (double)player.getZ());
            if (f > 0.5f && player.level().random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && player.level().canSeeSky(blockpos)) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos getExplosionExtent(Explosion explosion, Direction dir) {
        BlockPos pos = BlockPos.containing(com.mna.tools.MNAReflection.explosionPos(explosion));
        Vec3i offset = dir.getNormal().multiply((int)Math.ceil(com.mna.tools.MNAReflection.explosionRadius(explosion)));
        return pos.offset(offset);
    }
}


