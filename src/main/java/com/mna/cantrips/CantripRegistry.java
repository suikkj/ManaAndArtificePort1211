 
package com.mna.cantrips;

import net.minecraft.core.component.DataComponents;
import com.google.common.collect.Lists;
import com.mna.ManaAndArtifice;
import com.mna.api.cantrips.ICantrip;
import com.mna.api.cantrips.ICantripRegistry;
import com.mna.api.capabilities.IChunkMagic;
import com.mna.api.capabilities.IPlayerCantrip;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IAreaItem;
import com.mna.api.items.IPositionalItem;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.timing.DelayedEventQueue;
import com.mna.api.timing.TimedDelayedEvent;
import com.mna.api.tools.RLoc;
import com.mna.cantrips.Cantrip;
import com.mna.capabilities.chunkdata.ChunkMagicProvider;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.entities.utility.PresentItem;
import com.mna.entities.utility.SpellFX;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ItemManaweaverWand;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.to_server.RequestWardDataMessage;
import com.mna.spells.SpellCaster;
import com.mna.spells.components.ComponentGust;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.BlockUtils;
import com.mna.tools.TeleportHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;
import com.mna.api.tools.MNACap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.CustomData;
import com.mna.api.tools.MNAItemData;

public class CantripRegistry
implements ICantripRegistry {
    private static ItemStack IGNITE_CANTRIP_SPELL = new ItemStack((ItemLike)ItemInit.SPELL.get());
    private static ItemStack TRANSMUTE_CANTRIP_SPELL = new ItemStack((ItemLike)ItemInit.SPELL.get());
    private List<ICantrip> cantrips = new ArrayList<ICantrip>();
    public static final CantripRegistry INSTANCE = new CantripRegistry();

    public CantripRegistry() {
        SpellRecipe recipe = new SpellRecipe();
        recipe.setShape(Shapes.TOUCH);
        recipe.addComponent(Components.FIRE_DAMAGE);
        recipe.changeComponentAttributeValue(0, Attribute.DAMAGE, 0.0f);
        recipe.writeToNBT(IGNITE_CANTRIP_SPELL.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        IGNITE_CANTRIP_SPELL.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("default_cantrip", true);
        recipe = new SpellRecipe();
        recipe.setShape(Shapes.TOUCH);
        recipe.addComponent(Components.TRANSMUTE);
        recipe.changeShapeAttributeValue(Attribute.RADIUS, 1.0f);
        recipe.writeToNBT(TRANSMUTE_CANTRIP_SPELL.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        TRANSMUTE_CANTRIP_SPELL.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().putBoolean("default_cantrip", true);
        this.registerCantrip(RLoc.create("ignite"), RLoc.create("textures/gui/cantrips/ignite.png"), 1, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::applySpellAtTargetOnDelay, IGNITE_CANTRIP_SPELL, RLoc.create("manaweave_patterns/square"), RLoc.create("manaweave_patterns/triangle")).setRequiredAdvancement(RLoc.create("tier_1/advance_tier_2"));
        this.registerCantrip(RLoc.create("transmute"), RLoc.create("textures/gui/cantrips/transmute.png"), 1, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::applySpellAtTargetOnDelay, TRANSMUTE_CANTRIP_SPELL, RLoc.create("manaweave_patterns/square"), RLoc.create("manaweave_patterns/square"));
        this.registerCantrip(RLoc.create("halp"), RLoc.create("textures/gui/cantrips/halp.png"), 1, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::firework, ItemStack.EMPTY, RLoc.create("manaweave_patterns/square"), RLoc.create("manaweave_patterns/slash")).dynamicItem(Items.FIREWORK_ROCKET);
        this.registerCantrip(RLoc.create("gust"), RLoc.create("textures/gui/cantrips/gust.png"), 1, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::gust, ItemStack.EMPTY, RLoc.create("manaweave_patterns/slash"), RLoc.create("manaweave_patterns/backslash"));
        this.registerCantrip(RLoc.create("ascend"), RLoc.create("textures/gui/cantrips/ascend.png"), 1, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::ascend, ItemStack.EMPTY, RLoc.create("manaweave_patterns/triangle"), RLoc.create("manaweave_patterns/backslash"));
        this.registerCantrip(RLoc.create("dispel"), RLoc.create("textures/gui/cantrips/dispel.png"), 2, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::dispel, ItemStack.EMPTY, RLoc.create("manaweave_patterns/circle"), RLoc.create("manaweave_patterns/triangle"));
        this.registerCantrip(RLoc.create("drought"), RLoc.create("textures/gui/cantrips/drought.png"), 2, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::drought, ItemStack.EMPTY, RLoc.create("manaweave_patterns/circle"), RLoc.create("manaweave_patterns/circle"), RLoc.create("manaweave_patterns/circle"));
        this.registerCantrip(RLoc.create("ward"), RLoc.create("textures/gui/cantrips/ward.png"), 2, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::ward, ItemStack.EMPTY, RLoc.create("manaweave_patterns/circle"), RLoc.create("manaweave_patterns/slash")).dynamicItem(null);
        this.registerCantrip(RLoc.create("reveal_ward"), RLoc.create("textures/gui/cantrips/ward_reveal.png"), 2, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::reveal_ward, ItemStack.EMPTY, RLoc.create("manaweave_patterns/circle"), RLoc.create("manaweave_patterns/diamond"));
        this.registerCantrip(RLoc.create("grimoire"), RLoc.create("textures/gui/cantrips/grimoire.png"), 3, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::summonGrimoire, ItemStack.EMPTY, RLoc.create("manaweave_patterns/triangle"), RLoc.create("manaweave_patterns/inverted_triangle"));
        this.registerCantrip(RLoc.create("faction_grimoire"), RLoc.create("textures/gui/cantrips/grimoire2.png"), 4, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::summonFactionGrimoire, ItemStack.EMPTY, RLoc.create("manaweave_patterns/hourglass"), RLoc.create("manaweave_patterns/triangle"), RLoc.create("manaweave_patterns/inverted_triangle"));
        this.registerCantrip(RLoc.create("freeform_spell_a"), RLoc.create("textures/gui/cantrips/chevron_1.png"), 3, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::applySpellAtTargetOnDelay, ItemStack.EMPTY, RLoc.create("manaweave_patterns/triangle"), RLoc.create("manaweave_patterns/circle")).dynamicItem((Item)ItemInit.SPELL.get());
        this.registerCantrip(RLoc.create("freeform_spell_b"), RLoc.create("textures/gui/cantrips/chevron_2.png"), 4, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::applySpellAtTargetOnDelay, ItemStack.EMPTY, RLoc.create("manaweave_patterns/diamond"), RLoc.create("manaweave_patterns/square")).dynamicItem((Item)ItemInit.SPELL.get());
        this.registerCantrip(RLoc.create("freeform_spell_c"), RLoc.create("textures/gui/cantrips/chevron_3.png"), 5, (TriConsumer<Player, ICantrip, InteractionHand>)CantripRegistry::applySpellAtTargetOnDelay, ItemStack.EMPTY, RLoc.create("manaweave_patterns/knot2"), RLoc.create("manaweave_patterns/split_triangle")).dynamicItem((Item)ItemInit.SPELL.get());
    }

    @Override
    public ICantrip registerCantrip(ResourceLocation id, ResourceLocation icon, int tier, TriConsumer<Player, ICantrip, InteractionHand> effector, ItemStack spell, ResourceLocation ... defaultCombination) {
        if (this.cantrips.stream().anyMatch(c -> c.getId().equals(id))) {
            ManaAndArtifice.LOGGER.error("Attempted to register duplicate cantrips with id " + id);
            return null;
        }
        ICantrip c2 = new Cantrip(id, effector, spell, defaultCombination).setTier(tier).setDelay(60).setSound(SFX.Event.Player.MANAWEAVE_PATTERN_MATCH).setIcon(icon);
        this.cantrips.add(c2);
        return c2;
    }

    public static void applySpellAtTargetOnDelay(Player player, ICantrip cantrip, InteractionHand hand) {
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCantripData().getCantrip(cantrip.getId()).ifPresent(c -> {
            SpellRecipe spell = SpellRecipe.fromNBT(c.getStack().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag());
            if (spell.isValid()) {
                DelayedEventQueue.pushEvent(player.level(), new TimedDelayedEvent<Triple>(player.getUUID().toString() + "cantrip", 60, Triple.of(player, cantrip, hand), CantripRegistry::applySpellAtTarget));
            }
        }));
    }

    public static void applySpellAtTarget(String id, Triple<Player, ICantrip, InteractionHand> data) {
        if (data.getLeft() == null || data.getMiddle() == null) {
            return;
        }
        InteractionHand checkHand = ((Player)data.getLeft()).getUsedItemHand();
        if (!(((Player)data.getLeft()).getItemInHand(checkHand).getItem() instanceof ItemManaweaverWand)) {
            InteractionHand interactionHand = checkHand = checkHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            if (!(((Player)data.getLeft()).getItemInHand(checkHand).getItem() instanceof ItemManaweaverWand)) {
                ((Player)data.getLeft()).sendSystemMessage((Component)Component.translatable((String)"item.mna.manaweaver_wand.cantrip_wand_removed"));
                return;
            }
        }
        InteractionHand useHand = checkHand;
        MNACap.get((Player)data.getLeft(), PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCantripData().getCantrip(((ICantrip)data.getMiddle()).getId()).ifPresent(c -> {
            CompoundTag tag = c.getStack().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            boolean isDefaultCantrip = tag.getBoolean("default_cantrip");
            SpellRecipe recipe = SpellRecipe.fromNBT(tag);
            if (recipe.isValid()) {
                if (!isDefaultCantrip) {
                    ((ModifiedSpellPart)recipe.getShape()).getContainedAttributes().forEach(a -> ((ModifiedSpellPart)recipe.getShape()).resetValueToDefault((Attribute)(((a)))));
                    recipe.iterateComponents(comp -> comp.getContainedAttributes().forEach(a -> comp.resetValueToDefault((Attribute)((((a)))))));
                }
                if (!((Shape)((ModifiedSpellPart)recipe.getShape()).getPart()).isChanneled() && recipe.getReagents((Player)data.getLeft(), null, null).size() == 0) {
                    SpellCaster.Affect(c.getStack(), recipe, ((Player)data.getLeft()).level(), new SpellSource((LivingEntity)data.getLeft(), useHand));
                }
            }
        }));
    }

    public static void summonGrimoire(Player player, ICantrip cantrip, InteractionHand hand) {
        Vec3 vec = player.getEyePosition(0.0f).add(player.getLookAngle().scale(2.0));
        PresentItem entity = new PresentItem(player.level(), vec.x, vec.y - 0.5, vec.z, new ItemStack((ItemLike)ItemInit.GRIMOIRE.get()));
        entity.setDefaultPickUpDelay();
        player.level().addFreshEntity((Entity)entity);
    }

    public static void summonFactionGrimoire(Player player, ICantrip cantrip, InteractionHand hand) {
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            IFaction f = p.getAlliedFaction();
            ItemStack stack = f.getFactionGrimoire();
            if (stack.isEmpty()) {
                player.sendSystemMessage((Component)Component.translatable((String)"cantrip.mna.faction_grimoire_nofaction"));
                return;
            }
            Vec3 vec = player.getEyePosition(0.0f).add(player.getLookAngle().scale(2.0));
            PresentItem entity = new PresentItem(player.level(), vec.x, vec.y - 0.5, vec.z, stack);
            entity.setDefaultPickUpDelay();
            player.level().addFreshEntity((Entity)entity);
        });
    }

    public static void dispel(Player player, ICantrip cantrip, InteractionHand hand) {
        player.removeEffectsCuredBy(net.neoforged.neoforge.common.EffectCure.MILK);
        SpellRecipe recipe = new SpellRecipe();
        recipe.setShape(Shapes.SELF);
        recipe.addComponent(Components.TRUE_INVISIBILITY);
        SpellFX fx = new SpellFX((EntityType)EntityInit.SPELL_FX.get(), player.level());
        fx.setCaster((LivingEntity)player);
        fx.setRecipe(recipe);
        fx.setPos(player.getX(), player.getY(), player.getZ());
        player.level().addFreshEntity((Entity)fx);
    }

    public static void drought(Player player, ICantrip cantrip, InteractionHand hand) {
        Level world = player.level();
        BlockPos pos = player.blockPosition();
        LinkedList queue = Lists.newLinkedList();
        queue.add(new Tuple(pos, 0));
        int i = 0;
        while (!queue.isEmpty()) {
            Tuple tuple = (Tuple)queue.poll();
            BlockPos blockpos = (BlockPos)tuple.getA();
            int j = (Integer)tuple.getB();
            for (Direction direction : Direction.values()) {
                BlockPos blockpos1 = blockpos.relative(direction);
                BlockState blockstate = world.getBlockState(blockpos1);
                FluidState fluidstate = world.getFluidState(blockpos1);
                if (!fluidstate.is(FluidTags.WATER)) continue;
                if (blockstate.getBlock() instanceof BucketPickup && ((BucketPickup)blockstate.getBlock()).pickupBlock(player, (LevelAccessor)world, blockpos1, blockstate) != ItemStack.EMPTY) {
                    ++i;
                    if (j >= 6) continue;
                    queue.add(new Tuple(blockpos1, (j + 1)));
                    continue;
                }
                if (blockstate.getBlock() instanceof LiquidBlock) {
                    world.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 3);
                    ++i;
                    if (j >= 6) continue;
                    queue.add(new Tuple(blockpos1, (j + 1)));
                    continue;
                }
                if (!ComponentGust.BlockStateIsGustable(blockstate)) continue;
                BlockEntity tileentity = blockstate.hasBlockEntity() ? world.getBlockEntity(blockpos1) : null;
                Block.dropResources((BlockState)blockstate, (LevelAccessor)world, (BlockPos)blockpos1, (BlockEntity)tileentity);
                world.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 3);
                ++i;
                if (j >= 6) continue;
                queue.add(new Tuple(blockpos1, (j + 1)));
            }
            if (i <= 64) continue;
            break;
        }
    }

    public static void gust(Player player, ICantrip cantrip, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            int radius = 4;
            int height = 2;
            for (int i = -radius; i <= radius; ++i) {
                for (int k = -radius; k <= radius; ++k) {
                    for (int j = 0; j < height; ++j) {
                        BlockPos pos = player.blockPosition().offset(i, j, k);
                        BlockState state = player.level().getBlockState(pos);
                        if (!ComponentGust.BlockStateIsGustable(state) || !BlockUtils.destroyBlock((LivingEntity)player, player.level(), pos, true, (Tier)Tiers.WOOD)) continue;
                        BlockUtils.updateBlockState(player.level(), pos);
                    }
                }
            }
        }
    }

    public static void ascend(Player player, ICantrip cantrip, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            BlockState above;
            int maxSpaceAbove = 6;
            BlockPos checkPos = player.blockPosition().above(2);
            BlockState state = null;
            int count = 0;
            do {
                state = player.level().getBlockState(checkPos);
                checkPos = checkPos.above();
            } while (!state.isSolid() && ++count < maxSpaceAbove);
            if (!state.isSolid()) {
                player.sendSystemMessage((Component)Component.translatable((String)"cantrip.mna.ascend.no_ceiling"));
                return;
            }
            checkPos = checkPos.above();
            while (!(checkPos.getY() >= player.level().getMaxBuildHeight() || (state = player.level().getBlockState(checkPos)).isAir() && (above = player.level().getBlockState(checkPos.above())).isAir())) {
                checkPos = checkPos.above();
            }
            if (checkPos.getY() == player.level().getMaxBuildHeight()) {
                player.sendSystemMessage((Component)Component.translatable((String)"cantrip.mna.ascend.no_valid_point"));
                return;
            }
            player.level().playSound(null, player.position().x, player.position().y, player.position().z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            TeleportHelper.teleportEntity((Entity)player, (ResourceKey<Level>)player.level().dimension(), Vec3.atBottomCenterOf((Vec3i)checkPos));
        }
    }

    public static void ward(Player player, ICantrip cantrip, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            DelayedEventQueue.pushEvent(player.level(), new TimedDelayedEvent<Triple>(player.getUUID().toString() + "cantrip", 60, Triple.of(player, cantrip, hand), CantripRegistry::applyWard));
        }
    }

    public static void reveal_ward(Player player, ICantrip cantrip, InteractionHand hand) {
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            AABB bounds = new AABB(serverPlayer.blockPosition()).inflate(2.0);
            new RequestWardDataMessage(bounds).Handle(serverPlayer.serverLevel(), serverPlayer);
        }
    }

    public static void applyWard(String id, Triple<Player, ICantrip, InteractionHand> data) {
        Player player = (Player)data.getLeft();
        Level level = player.level();
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> {
            IChunkMagic magic;
            ItemStack heldOffhand = data.getRight() == InteractionHand.MAIN_HAND ? player.getOffhandItem() : player.getMainHandItem();
            ArrayList<Long> ward_positions = new ArrayList<Long>();
            Item patt17803$temp = heldOffhand.getItem();
            if (patt17803$temp instanceof IAreaItem) {
                IAreaItem areaItem = (IAreaItem)patt17803$temp;
                AABB bounds = areaItem.getBounds(heldOffhand);
                if (bounds != null) {
                    for (int x = (int)bounds.minX; x <= (int)bounds.maxX; ++x) {
                        for (int y = (int)bounds.minY; y <= (int)bounds.maxY; ++y) {
                            for (int z = (int)bounds.minZ; z <= (int)bounds.maxZ; ++z) {
                                BlockPos pos = new BlockPos(x, y, z);
                                BlockState state = level.getBlockState(pos);
                                if (state.isAir()) continue;
                                ward_positions.add(pos.asLong());
                            }
                        }
                    }
                }
            } else {
                Item patt18379$temp = heldOffhand.getItem();
                if (patt18379$temp instanceof IPositionalItem) {
                    BlockState state;
                    IPositionalItem positional = (IPositionalItem)patt18379$temp;
                    BlockPos point = positional.getLocation(heldOffhand);
                    if (point != null && !(state = level.getBlockState(point)).isAir()) {
                        ward_positions.add(point.asLong());
                    }
                } else {
                    BlockPos target;
                    BlockState state;
                    double range = 4.5;
                    try {
                        range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
                    }
                    catch (Throwable t) {
                        range = 4.5;
                    }
                    HitResult raytraceresult = player.pick(range, 0.0f, false);
                    if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.BLOCK && !(state = level.getBlockState(target = ((BlockHitResult)raytraceresult).getBlockPos())).isAir()) {
                        ward_positions.add(target.asLong());
                    }
                }
            }
            if (ward_positions.size() == 0) {
                player.sendSystemMessage((Component)Component.translatable((String)"cantrip.mna.ward.no_target"));
                return;
            }
            Optional<IPlayerCantrip> cantrip = m.getCantripData().getCantrip(((ICantrip)data.getMiddle()).getId());
            ItemStack bypassItem = cantrip.isPresent() ? cantrip.get().getStack() : ItemStack.EMPTY;
            HashMap allAffectedChunks = new HashMap();
            ward_positions.stream().forEach(l -> allAffectedChunks.computeIfAbsent(new ChunkPos(l.longValue()), chunkPos -> {
                BlockPos pos = BlockPos.of((long)l);
                if (!player.level().isLoaded(pos)) {
                    return null;
                }
                LevelChunk chunkAccess = player.level().getChunkAt(pos);
                return (IChunkMagic)chunkAccess.getData(ChunkMagicProvider.MAGIC.get());
            }));
            boolean all_warded = true;
            for (Long l2 : ward_positions) {
                magic = (IChunkMagic)allAffectedChunks.get(new ChunkPos(l2.longValue()));
                if (magic.isWarded(l2)) continue;
                all_warded = false;
                break;
            }
            for (Long l3 : ward_positions) {
                magic = (IChunkMagic)allAffectedChunks.get(new ChunkPos(l3.longValue()));
                BlockPos pos = BlockPos.of((long)l3);
                if (all_warded) {
                    magic.removeBlockWard(pos, player);
                    continue;
                }
                magic.wardBlock(pos, player, bypassItem);
                ServerMessageDispatcher.sendRenderWard((ServerLevel)level, pos.asLong());
            }
        });
    }

    public static void firework(Player player, ICantrip cantrip, InteractionHand hand) {
        Vec3 vec = player.position().add(player.getForward().normalize().scale(4.0)).add(0.0, 1.0, 0.0);
        MutableObject fireworkStack = new MutableObject(ItemStack.EMPTY);
        MNACap.get(player, PlayerMagicProvider.MAGIC).ifPresent(m -> m.getCantripData().getCantrip(cantrip.getId()).ifPresent(c -> fireworkStack.setValue(c.getStack())));
        if (((ItemStack)fireworkStack.getValue()).isEmpty() || ((ItemStack)fireworkStack.getValue()).getItem() != Items.FIREWORK_ROCKET) {
            ItemStack defaultStack = new ItemStack((ItemLike)Items.FIREWORK_ROCKET);
            CompoundTag explosion = new CompoundTag();
            CompoundTag fw = MNAItemData.element(defaultStack, "Fireworks");
            DyeColor explosionColor = DyeColor.values()[(int)(Math.random() * (double)DyeColor.values().length)];
            DyeColor explosionFade = DyeColor.values()[(int)(Math.random() * (double)DyeColor.values().length)];
            explosion.putByte("Type", (byte)0);
            explosion.putIntArray("Colors", new int[]{explosionColor.getFireworkColor()});
            explosion.putIntArray("FadeColors", new int[]{explosionFade.getFireworkColor()});
            fw.putByte("Flight", (byte)2);
            ListTag explosions = new ListTag();
            explosions.add(explosion);
            fw.put("Explosions", (Tag)explosions);
            fireworkStack.setValue(defaultStack);
        }
        FireworkRocketEntity firework = new FireworkRocketEntity(player.level(), null, vec.x, vec.y, vec.z, (ItemStack)fireworkStack.getValue());
        player.level().addFreshEntity((Entity)firework);
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 1, false, false));
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.hasAlliedFaction() && p.getAllyCooldown() <= 0) {
                p.summonRandomAlly(player);
            }
        });
    }

    @Override
    public int countRegisteredCantrips() {
        return this.cantrips.size();
    }

    @Override
    @Nullable
    public Optional<ICantrip> getCantrip(ResourceLocation cantripID) {
        return this.cantrips.stream().filter(c -> c.getId().equals(cantripID)).findFirst();
    }

    @Override
    public List<ICantrip> getCantrips() {
        return this.cantrips;
    }

    @Override
    public List<ICantrip> getCantrips(Player player) {
        int tier = 0;
        IPlayerProgression progression = (IPlayerProgression)player.getData(PlayerProgressionProvider.PROGRESSION.get());
        if (progression != null) {
            tier = progression.getTier();
        }
        int resolvedTier = tier;
        return this.getCantrips().stream().filter(c -> {
            if (c.getTier() > resolvedTier) {
                return false;
            }
            return c.getRequiredAdvancement() == null || ManaAndArtifice.instance.proxy.playerHasAdvancement(player, c.getRequiredAdvancement());
        }).collect(Collectors.toList());
    }

    @Override
    public List<ICantrip> getCantrips(int tier) {
        return this.getCantrips().stream().filter(c -> c.getTier() <= tier && c.getRequiredAdvancement() == null).collect(Collectors.toList());
    }
}


