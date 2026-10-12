 
package com.mna.blocks.tileentities;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.Registries;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.IRequirePlayerReference;
import com.mna.api.blocks.PlayerOwnershipRecord;
import com.mna.api.blocks.tile.BlockPosCache;
import com.mna.api.blocks.tile.IEldrinCapacitorTile;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.blocks.tile.IMultiblockDefinition;
import com.mna.api.blocks.tile.MultiblockTile;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.events.EldrinCraftingEvent;
import com.mna.api.faction.IFaction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.tools.MATags;
import com.mna.api.tools.RLoc;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.PedestalTile;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.entities.utility.PresentItem;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.to_client.SpawnParticleEffectMessage;
import com.mna.particles.types.movers.ParticleBezierMover;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.particles.types.movers.ParticleSphereOrbitMover;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.recipes.ItemAndPatternCraftingInventory;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.eldrin.EldrinAltarRecipe;
import com.mna.sound.PredicateLoopingSound;
import com.mna.tools.InventoryUtilities;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.MultiblockRenderer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import com.mna.api.tools.MNACap;
import com.mna.api.tools.MNAItemData;

public class EldrinAltarTile
extends MultiblockTile
implements IBlockEntityExtension,
IRequirePlayerReference<EldrinAltarTile>,
IEldrinConsumerTile {
    private static final int MAX_ITEMS = 1;
    public static final int INVENTORY_SLOT_INDEX = 0;
    public static final int CONSUME_RATE = 10;
    public static final Vec3[] crystal_offsets = new Vec3[]{new Vec3(1.0, 0.0, 0.0), new Vec3(-1.0, 0.0, 0.0), new Vec3(0.5, 0.0, 1.0), new Vec3(-0.5, 0.0, 1.0), new Vec3(0.5, 0.0, -1.0), new Vec3(-0.5, 0.0, -1.0)};
    private int stage = Stage.IDLE.ordinal();
    private int stageTicks = 0;
    private EldrinAltarRecipe __cachedRecipe;
    private ResourceLocation __cachedRecipeId;
    private BlockPosCache __powerSuppliers;
    private int lastCraftTier = 0;
    private IFaction lastCraftFaction = null;
    private HashMap<Affinity, Float> lastCraftEldrin;
    private ResourceLocation lastCraftRecipeID;
    private EldrinAltarRecipe _lastCraftRecipe;
    private NonNullList<ItemStack> lastCraftItems;
    private NonNullList<ItemStack> lastCraftItemsMerged;
    private ItemStack lastCraftOutput = ItemStack.EMPTY;
    private HashMap<Affinity, Float> consumedPower;
    private HashMap<Affinity, Boolean> powerRequirementStatus;
    private UUID __crafterID;
    private Player __crafter;
    private final ArrayList<ActiveCrystal> activeCrystals = new ArrayList();
    private final ArrayList<CollectedItem> collectedItems = new ArrayList();

    public EldrinAltarTile(BlockEntityType<?> p_i48289_1_, BlockPos pos, BlockState state) {
        super(p_i48289_1_, RLoc.create("multiblock/eldrin_altar"), pos, state, 1);
        this.__powerSuppliers = new BlockPosCache(this, 7, (bp, world) -> {
            IEldrinCapacitorTile cap;
            BlockEntity te = this.getLevel().getBlockEntity(bp);
            if (te != null && te instanceof IEldrinCapacitorTile && (cap = (IEldrinCapacitorTile)te).canSupply(Affinity.UNKNOWN, PlayerOwnershipRecord.of(this.__crafter))) {
                return this.losCheck(bp.above());
            }
            return false;
        });
        this.consumedPower = new HashMap();
        this.powerRequirementStatus = new HashMap();
        this.lastCraftEldrin = new HashMap();
        this.lastCraftItems = NonNullList.create();
        this.lastCraftItemsMerged = NonNullList.create();
    }

    protected boolean losCheck(BlockPos pos) {
        Vec3 vector3d = Vec3.atCenterOf((Vec3i)this.getBlockPos().above());
        Vec3 vector3d1 = Vec3.atCenterOf((Vec3i)pos);
        BlockHitResult result = this.getLevel().clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity)null));
        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getBlockPos().equals(pos);
        }
        return result.getType() == HitResult.Type.MISS;
    }

    public EldrinAltarTile(BlockPos pos, BlockState state) {
        this((BlockEntityType)TileEntityInit.ELDRIN_ALTAR_TILE.get(), pos, state);
    }

    public Stage getStage() {
        return Stage.values()[this.stage];
    }

    public int getStageTicks() {
        return this.stageTicks;
    }

    private void setStage(Stage stage) {
        this.stage = stage.ordinal();
        this.stageTicks = 0;
        if (!this.getLevel().isClientSide()) {
            this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
    }

    private EldrinAltarRecipe getCachedRecipe() {
        if (this.__cachedRecipe == null && this.__cachedRecipeId != null) {
            this.getLevel().getRecipeManager().byKey(this.__cachedRecipeId).ifPresent(r -> {
                if (r.value() instanceof EldrinAltarRecipe) {
                    this.__cachedRecipe = (EldrinAltarRecipe)r.value();
                }
            });
        }
        return this.__cachedRecipe;
    }

    private Player getCrafter() {
        if (this.__crafter == null && this.__crafterID != null) {
            this.__crafter = this.getLevel().getPlayerByUUID(this.__crafterID);
        }
        return this.__crafter;
    }

    @Override
    public void setPlayerReference(Player player) {
        this.__crafter = player;
        this.__crafterID = player.getUUID();
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(5.0);
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, EldrinAltarTile tile) {
        MultiblockTile.Tick(level, pos, state, tile);
        tile.__powerSuppliers.tick();
        ++tile.stageTicks;
        if (level.isClientSide()) {
            tile.spawnParticles();
            tile.playSounds();
        } else {
            switch (tile.getStage()) {
                case FINDING_POWER: {
                    tile.tickLogic_findPower();
                    break;
                }
                case CONSUMING_POWER: {
                    tile.tickLogic_consumePower();
                    break;
                }
                case CONSUMING_REAGENTS: {
                    tile.tickLogic_consumeReagents();
                    break;
                }
                case CRAFTING: {
                    tile.tickLogic_craft();
                    break;
                }
                case COMPLETING: {
                    tile.tickLogic_complete();
                    break;
                }
            }
        }
    }

    public boolean structureMatched() {
        return this.structureMatched;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void setGhostMultiblock() {
        if (!this.structureMatched) {
            this.match(true);
            if (!this.structureMatched) {
                this.getDefinition().ifPresent(r -> {
                    BlockPos anchorPos = this.getMatchOrigin();
                    MultiblockRenderer.setMultiblock(r, (Component)Component.translatable((String)r.getId().toString()), false);
                    MultiblockRenderer.anchorTo(anchorPos, Rotation.NONE);
                });
            }
        }
    }

    public HashMap<Affinity, Boolean> getPowerRequirementStatus() {
        return this.powerRequirementStatus;
    }

    @OnlyIn(value=Dist.CLIENT)
    private void spawnParticles() {
        if (this.getStage() != Stage.IDLE) {
            this.spawnParticles_active();
            this.spawnParticles_crystals();
            this.spawnParticles_items();
            this.spawnParticles_lights();
            this.spawnParticles_power();
        }
    }

    private void spawnParticles_power() {
        for (Affinity aff : Affinity.values()) {
            if (!this.consumedPower.containsKey(aff) || !(this.consumedPower.get(aff).floatValue() >= 0.0f)) continue;
            for (int i = 0; i < 10; ++i) {
                MAParticleType particle = null;
                switch (aff) {
                    case ARCANE: {
                        particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get()));
                        break;
                    }
                    case EARTH: {
                        particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get()));
                        break;
                    }
                    case ENDER: {
                        particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get()));
                        break;
                    }
                    case FIRE: 
                    case HELLFIRE: 
                    case LIGHTNING: {
                        particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get()));
                        break;
                    }
                    case WATER: 
                    case ICE: {
                        particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get()));
                        break;
                    }
                    case WIND: {
                        particle = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10);
                        break;
                    }
                }
                if (particle == null) continue;
                Vec3 particlePos = Vec3.atCenterOf((Vec3i)this.worldPosition);
                particle.setMover(new ParticleSphereOrbitMover(particlePos.x, particlePos.y + 1.25, particlePos.z, 0.01f, Math.random() * 360.0, 1.5));
                this.getLevel().addParticle((ParticleOptions)particle, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private void spawnParticles_active() {
        int i;
        int angleStep = 60;
        Vec3 center = Vec3.atCenterOf((Vec3i)this.getBlockPos()).subtract(0.0, 0.5, 0.0);
        for (i = 0; i < 360; i += angleStep) {
            double angle = (double)((i + this.stageTicks) % 360) * Math.PI / 180.0;
            Vec3 offset = center.add(new Vec3(Math.sin(angle), 0.0, Math.cos(angle)).normalize().scale(4.0)).add(0.0, 1.0, 0.0);
            this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.TRAIL_ORBIT.get())).setScale(0.25f).setMaxAge(50).setColor(2, 1, 5).setMover(new ParticleBezierMover(center, offset)), center.x, center.y, center.z, 0.0, 0.0, 0.0);
        }
        if (this.stageTicks == 1) {
            angleStep = 10;
            for (i = 0; i <= 360; i += angleStep) {
                int angle = (i + this.stageTicks * 2) % 360;
                Vec3 offset = new Vec3(Math.sin((double)angle * Math.PI / 180.0), 0.0, Math.cos((double)angle * Math.PI / 180.0)).normalize().scale(0.1);
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setScale(0.5f).setMaxAge(20), center.x, center.y, center.z, offset.x, offset.y, offset.z);
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private void spawnParticles_crystals() {
        this.activeCrystals.forEach(e -> {
            Vec3 offset = Vec3.atCenterOf((Vec3i)this.getBlockPos()).add(crystal_offsets[e.offsetIndex]);
            Vec3 crystalWidth = new Vec3(0.1, 0.0, 0.1);
            int[] color = e.affinity.getShiftAffinity().getColor();
            for (int i = 0; i < 5; ++i) {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType(((MAParticleType)((ParticleInit.LIGHT_VELOCITY.get()))).setColor(color[0], color[1], color[2])), offset.x - crystalWidth.x + Math.random() * crystalWidth.x * 2.0, offset.y + 0.55, offset.z - crystalWidth.z + Math.random() * crystalWidth.z * 2.0, 0.0, 0.01, 0.0);
            }
        });
    }

    @OnlyIn(value=Dist.CLIENT)
    private void spawnParticles_items() {
        if (this.stage < Stage.CONSUMING_REAGENTS.ordinal()) {
            return;
        }
        Vec3 center = Vec3.atCenterOf((Vec3i)this.getBlockPos()).add(0.0, 1.0, 0.0);
        int burstIndex = this.collectedItems.size() - this.stageTicks / 20;
        for (int i = 0; i < this.collectedItems.size(); ++i) {
            CollectedItem ci = this.collectedItems.get(i);
            Vec3 pedestalParticleCenter = new Vec3((double)ci.tilePos.getX() + 0.5, (double)ci.tilePos.getY() + 1.5, (double)ci.tilePos.getZ() + 0.5);
            if (this.stage == Stage.CONSUMING_REAGENTS.ordinal() && this.stageTicks > 0 && this.stageTicks % 20 == 0 && i == burstIndex) {
                for (int n = 0; n < 20; ++n) {
                    this.getLevel().addParticle((ParticleOptions)this.createRandomParticle(ci.stack).setMaxAge(5).setMover(new ParticleVelocityMover(Math.random() * 0.2 - 0.1, Math.random() * 0.2 - 0.1, Math.random() * 0.2 - 0.1, true)), pedestalParticleCenter.x, pedestalParticleCenter.y, pedestalParticleCenter.z, 0.0, 0.0, 0.0);
                }
            }
            if (this.stage == Stage.CONSUMING_REAGENTS.ordinal() && i >= burstIndex || i <= burstIndex && this.stage == Stage.CRAFTING.ordinal()) {
                this.getLevel().addParticle((ParticleOptions)this.createRandomParticle(ci.stack).setMover(new ParticleSphereOrbitMover(pedestalParticleCenter.x, pedestalParticleCenter.y, pedestalParticleCenter.z, 0.01f, Math.random() * 360.0, 0.5)), pedestalParticleCenter.x, pedestalParticleCenter.y, pedestalParticleCenter.z, 0.0, 0.0, 0.0);
            }
            if (this.stage != Stage.CRAFTING.ordinal() || i != burstIndex) continue;
            this.getLevel().addParticle((ParticleOptions)this.createRandomParticle(ci.stack).setMover(new ParticleLerpMover(pedestalParticleCenter.x, pedestalParticleCenter.y, pedestalParticleCenter.z, center.x, center.y, center.z)), pedestalParticleCenter.x, pedestalParticleCenter.y, pedestalParticleCenter.z, 0.0, 0.0, 0.0);
        }
        if (this.stage >= Stage.CRAFTING.ordinal()) {
            float radius = 0.75f;
            float scale = 0.1f;
            if (this.getStage() == Stage.COMPLETING) {
                if (this.stageTicks > 40) {
                    float pct = MathUtils.clamp01((float)(this.stageTicks - 40) / 40.0f);
                    radius -= 0.74f * pct;
                    scale = (float)((double)scale - 0.09 * (double)pct);
                } else if (this.stageTicks > 80) {
                    return;
                }
            }
            for (int i = 0; i < this.collectedItems.size(); ++i) {
                if (this.getStage() == Stage.CRAFTING && i <= burstIndex) continue;
                CollectedItem ci = this.collectedItems.get(i);
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ITEM.get())).setStack(ci.stack).setScale(scale).setMover(new ParticleSphereOrbitMover(center.x, center.y, center.z, 0.01f, Math.random() * 360.0, radius)), center.x, center.y, center.z, 0.0, 0.0, 0.0);
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private void spawnParticles_lights() {
        for (int i = 0; i < this.collectedItems.size(); ++i) {
            CollectedItem item = this.collectedItems.get(i);
            if (item.stack.isEmpty()) continue;
            Vec3 offset = Vec3.atCenterOf((Vec3i)item.tilePos);
            Vec3 crystalWidth = new Vec3(0.25, 0.0, 0.25);
            for (int n = 0; n < 10; ++n) {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHT_VELOCITY.get())), offset.x - crystalWidth.x + Math.random() * crystalWidth.x * 2.0, offset.y + 0.55, offset.z - crystalWidth.z + Math.random() * crystalWidth.z * 2.0, 0.0, 0.01, 0.0);
            }
        }
        if (this.getStage() == Stage.COMPLETING && this.stageTicks == 129) {
            int i;
            Random rnd = new Random();
            Vec3 basePoint = Vec3.atBottomCenterOf((Vec3i)this.getBlockPos());
            Vec3 srcPoint = basePoint.add(0.5, 1.5, 0.5);
            for (i = 0; i < 150; ++i) {
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), srcPoint.x(), srcPoint.y(), srcPoint.z(), -0.5 + Math.random(), 0.01, -0.5 + Math.random());
            }
            for (i = 0; i < 50; ++i) {
                Vec3 lightPoint = new Vec3(srcPoint.x() - (double)0.2f + (double)(rnd.nextFloat() * 0.4f), srcPoint.y() - (double)0.3f, srcPoint.z() - (double)0.2f + (double)(rnd.nextFloat() * 0.4f));
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.LIGHT_VELOCITY.get())), lightPoint.x(), lightPoint.y(), lightPoint.z(), 0.0, 0.01, 0.0);
            }
            int angleStep = 10;
            for (int i2 = 0; i2 <= 360; i2 += angleStep) {
                int angle = (i2 + this.stageTicks * 2) % 360;
                Vec3 offset = new Vec3(Math.sin((double)angle * Math.PI / 180.0), 0.0, Math.cos((double)angle * Math.PI / 180.0)).normalize().scale(0.3);
                this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setScale(0.5f).setMaxAge(20), basePoint.x, basePoint.y, basePoint.z, offset.x, offset.y, offset.z);
            }
        }
    }

    @OnlyIn(value=Dist.CLIENT)
    private MAParticleType createRandomParticle(ItemStack stack) {
        if (Math.random() < 0.5 && this.getCachedRecipe() != null) {
            ArrayList<Affinity> affs = new ArrayList<Affinity>();
            affs.addAll(this.getCachedRecipe().getPowerRequirements().keySet());
            if (affs.size() > 0) {
                Affinity rand = (Affinity)(affs.get((int)(Math.random() * (double)affs.size())));
                switch (rand) {
                    case ARCANE: {
                        return new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get()));
                    }
                    case EARTH: {
                        return new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.DUST.get()));
                    }
                    case ENDER: {
                        return new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ENDER_VELOCITY.get()));
                    }
                    case FIRE: 
                    case HELLFIRE: {
                        return new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get()));
                    }
                    case WATER: 
                    case ICE: {
                        return new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.WATER.get())).setScale(0.03f);
                    }
                    case LIGHTNING: 
                    case WIND: {
                        return new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.04f).setColor(30, 30, 30);
                    }
                }
            }
        }
        return new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ITEM.get())).setStack(stack).setScale(0.1f);
    }

    @OnlyIn(value=Dist.CLIENT)
    private void playSounds() {
        if (this.getStage() == Stage.FINDING_POWER && this.stageTicks == 1) {
            this.PlaySound(SFX.Spell.Cast.ENDER);
            this.PlayLoopingSound(SFX.Loops.ELDRIN_ALTAR);
        }
        int ticksPerBeam = 5;
        if (this.stage == Stage.CONSUMING_REAGENTS.ordinal() && this.stageTicks % ticksPerBeam == 0 && this.stageTicks < this.getActiveCrystals().size() * ticksPerBeam) {
            this.PlaySound(SFX.Spell.Cast.ARCANE);
        }
        int burstFreq = 20;
        if (this.stage == Stage.CONSUMING_REAGENTS.ordinal() && this.stageTicks > 0 && this.stageTicks % burstFreq == 0 && this.stageTicks <= burstFreq * this.getCollectedItems().size()) {
            this.PlaySound(SFX.Spell.Impact.Single.EARTH);
        }
        if (this.stage == Stage.CRAFTING.ordinal() && this.stageTicks > 0 && this.stageTicks % burstFreq == 0 && this.stageTicks < burstFreq * this.getCollectedItems().size()) {
            this.PlaySound(SFX.Event.Eldrin.DRAW_IN_ITEM);
        }
        if (this.getStage() == Stage.COMPLETING && this.stageTicks == 129) {
            this.PlaySound(SFX.Event.Eldrin.CRAFT_COMPLETE);
        }
    }

    private void PlaySound(SoundEvent soundID) {
        this.getLevel().playLocalSound((double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), soundID, SoundSource.BLOCKS, 1.0f, 1.0f, false);
    }

    @OnlyIn(value=Dist.CLIENT)
    private void PlayLoopingSound(SoundEvent soundID) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)new PredicateLoopingSound(soundID, "eldrin_altar", this::soundLoopPredicate).setVolume(0.2f).setPosition(this.worldPosition));
    }

    private boolean soundLoopPredicate(String id) {
        return this.stage > 0;
    }

    public boolean startCrafting(Player player) {
        if (!this.structureMatched) {
            return false;
        }
        if (this.getStage() != Stage.IDLE) {
            return false;
        }
        if (!this.cacheRecipe(player, false)) {
            return false;
        }
        java.util.Optional<com.mna.api.capabilities.IPlayerProgression> progression = MNACap.get(player, PlayerProgressionProvider.PROGRESSION);
        if (!progression.isPresent()) {
            return false;
        }
        if (this.__cachedRecipe == null || this.__cachedRecipe.getTier() > ((IPlayerProgression)progression.get()).getTier()) {
            player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.lowtier"));
            return false;
        }
        this.activeCrystals.clear();
        this.collectedItems.clear();
        MutableObject playerNWStr = new MutableObject();
        MutableBoolean canStart = new MutableBoolean(true);
        MNACap.get(this.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> playerNWStr.setValue(m.getWellspringRegistry().getNodeNetworkStrengthFor(player)));
        if (canStart.booleanValue()) {
            this.__crafterID = player.getGameProfile().getId();
            this.__crafter = player;
            this.consumedPower.clear();
            this.setStage(Stage.FINDING_POWER);
        }
        return canStart.booleanValue();
    }

    private boolean cacheRecipe(Player crafter, boolean consume) {
        ItemStack stack;
        if (this.getDisplayedItem().isEmpty() || crafter == null) {
            return false;
        }
        Optional<IMultiblockDefinition> def = this.getDefinition();
        if (!def.isPresent()) {
            return false;
        }
        List<BlockPos> offsets = def.get().getPositions(Arrays.asList(BlockInit.PEDESTAL.getId(), BlockInit.PEDESTAL_WITH_SIGN.getId()), null);
        if (offsets.size() != 8) {
            throw new RuntimeException("THIS IS NOT AN M&A ISSUE.  A packmaker has replaced the structure for the Runic Altar and didn't put exactly 8 pedestals.  The pedestals are required to match the recipe.  Go tell them to fix it.");
        }
        BlockPos origin = BlockPos.ZERO;
        if (this.matchOriginIsCenter()) {
            origin = origin.subtract(new Vec3i(def.get().getSize().getX() / 2, 0, def.get().getSize().getZ() / 2));
        }
        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
        stacks.add(this.getDisplayedItem().copy());
        if (consume && !(stack = this.removeItem(0, 1)).isEmpty()) {
            this.collectedItems.add(new CollectedItem(this.getBlockPos(), stack));
        }
        for (BlockPos pos : offsets) {
            ItemStack stack2;
            BlockPos adjustedpos = origin.offset((Vec3i)pos);
            if (adjustedpos.getX() == 0 && adjustedpos.getY() == 0 && adjustedpos.getZ() == 0) continue;
            BlockEntity te = this.getLevel().getBlockEntity(this.getBlockPos().offset((Vec3i)adjustedpos));
            if (te == null || !(te instanceof PedestalTile)) {
                return false;
            }
            stacks.add(((PedestalTile)te).getDisplayedItem().copy());
            if (!consume || (stack2 = ((PedestalTile)te).removeItem(0, 1)).isEmpty()) continue;
            this.collectedItems.add(new CollectedItem(te.getBlockPos(), stack2));
        }
        ItemAndPatternCraftingInventory craftinginventory = new ItemAndPatternCraftingInventory(9, new ArrayList<String>());
        for (int i = 0; i < 9; ++i) {
            craftinginventory.setItem(i, (ItemStack)stacks.get(i));
        }
        this.__cachedRecipe = this.getLevel().getRecipeManager().getRecipeFor((RecipeType)RecipeInit.ELDRIN_ALTAR_TYPE.get(), (Container)craftinginventory, this.level);
        if (this.__cachedRecipe != null) {
            MNACap.get(crafter, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                IFaction playerfaction = p.getAlliedFaction();
                int playerTier = p.getTier();
                if (this.__cachedRecipe.getFactionRequirement() != null && this.__cachedRecipe.getFactionRequirement() != playerfaction) {
                    this.__cachedRecipe = null;
                    if (!crafter.level().isClientSide()) {
                        crafter.sendSystemMessage((Component)Component.translatable((String)"block.mna.eldrin_altar.wrongFaction"));
                    }
                }
                if (this.__cachedRecipe.getTier() > playerTier) {
                    this.__cachedRecipe = null;
                    if (!crafter.level().isClientSide()) {
                        crafter.sendSystemMessage((Component)Component.translatable((String)"block.mna.runic_anvil.low_tier"));
                    }
                }
            });
        }
        return this.__cachedRecipe != null;
    }

    private void tickLogic_findPower() {
        if (this.stageTicks == 1) {
            this.__powerSuppliers.queueRecheck();
        } else if (!this.__powerSuppliers.isSearching()) {
            if (this.getCachedRecipe() == null) {
                this.setStage(Stage.IDLE);
                return;
            }
            HashMap requiredpower = new HashMap();
            this.getCachedRecipe().getPowerRequirements().forEach((k, v) -> requiredpower.put(k, v));
            this.__powerSuppliers.getCachedPositions().forEach(bp -> {
                BlockEntity te = this.getLevel().getBlockEntity(bp);
                if (te != null && te instanceof IEldrinCapacitorTile) {
                    ((IEldrinCapacitorTile)te).getAffinities().forEach(suppliedPower -> {
                        if (requiredpower.containsKey(suppliedPower)) {
                            requiredpower.remove(suppliedPower);
                            this.activeCrystals.add(new ActiveCrystal((Affinity)((suppliedPower)), this.getClosestOffset((BlockPos)bp), (BlockPos)bp));
                        }
                    });
                }
            });
            if (requiredpower.size() == 0) {
                this.setStage(Stage.CONSUMING_POWER);
            } else {
                if (this.getCrafter() != null) {
                    this.getCrafter().sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.missing_conduit"));
                }
                this.consumedPower.clear();
                this.setStage(Stage.IDLE);
            }
        }
    }

    private void tickLogic_consumePower() {
        MutableBoolean fullyConsumed = new MutableBoolean(true);
        MutableBoolean consumedAny = new MutableBoolean(false);
        EldrinAltarRecipe cachedRecipe = this.getCachedRecipe();
        if (cachedRecipe == null) {
            this.setStage(Stage.IDLE);
            return;
        }
        PlayerOwnershipRecord record = PlayerOwnershipRecord.of(this.getCrafter());
        cachedRecipe.getPowerRequirements().forEach((affinity, required) -> {
            float existing;
            if (!this.consumedPower.containsKey(affinity)) {
                this.consumedPower.put((Affinity)(affinity), Float.valueOf(0.0f));
            }
            if (!this.powerRequirementStatus.containsKey(affinity)) {
                this.powerRequirementStatus.put((Affinity)(affinity), true);
            }
            if ((existing = this.consumedPower.get(affinity).floatValue()) < required.floatValue() && required.floatValue() > 0.0f && existing >= 0.0f) {
                fullyConsumed.setFalse();
                float toConsume = Math.min(10.0f, required.floatValue() - existing);
                float consumed = this.consume(record, this.level, this.getBlockPos(), Vec3.atCenterOf((Vec3i)this.getBlockPos()), (Affinity)(affinity), toConsume);
                if (consumed > 0.0f) {
                    float newAmt = existing + consumed;
                    if (newAmt < required.floatValue()) {
                        this.consumedPower.put((Affinity)(affinity), Float.valueOf(newAmt));
                    } else {
                        this.consumedPower.put((Affinity)(affinity), Float.valueOf(-1.0f));
                        consumedAny.setTrue();
                    }
                    this.powerRequirementStatus.put((Affinity)(affinity), true);
                } else {
                    this.powerRequirementStatus.put((Affinity)(affinity), false);
                }
            }
        });
        if (!this.getLevel().isClientSide() && this.getLevel().getGameTime() % 20L == 0L) {
            this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
        if (fullyConsumed.booleanValue()) {
            this.setStage(Stage.CONSUMING_REAGENTS);
        }
    }

    private int getClosestOffset(BlockPos pos) {
        int output = -1;
        double dist = Double.MAX_VALUE;
        Vec3 center = Vec3.atCenterOf((Vec3i)this.getBlockPos());
        Vec3 target = Vec3.atCenterOf((Vec3i)pos);
        for (int i = 0; i < crystal_offsets.length; ++i) {
            double loopDist = center.add(crystal_offsets[i]).distanceToSqr(target);
            if (!(loopDist < dist)) continue;
            dist = loopDist;
            output = i;
        }
        return output;
    }

    private void tickLogic_consumeReagents() {
        if (this.stageTicks == 1) {
            EldrinAltarRecipe prev = this.getCachedRecipe();
            if (!this.cacheRecipe(this.getCrafter(), true) || prev != this.getCachedRecipe()) {
                this.setStage(Stage.IDLE);
                return;
            }
            this.getLevel().sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
        }
        if (this.stageTicks >= 30 * this.getCachedRecipe().getRequiredItems().length) {
            this.setStage(Stage.CRAFTING);
        }
    }

    private void tickLogic_craft() {
        if (this.stageTicks >= 30 * this.getCachedRecipe().getRequiredItems().length) {
            this.setStage(Stage.COMPLETING);
        }
    }

    private void tickLogic_complete() {
        if (this.stageTicks >= 130) {
            Vec3 center = Vec3.atCenterOf((Vec3i)this.getBlockPos());
            NeoForge.EVENT_BUS.post((Event)new EldrinCraftingEvent(this.getCachedRecipe(), this.getCachedRecipe().getResultItem(), this.getCrafter()));
            Player crafter = this.getCrafter();
            if (crafter != null && crafter instanceof ServerPlayer) {
                CustomAdvancementTriggers.ELDRIN_ALTAR_CRAFT.trigger((ServerPlayer)crafter, this.getCachedRecipe().getResultItem());
            }
            PresentItem epi = new PresentItem(this.getLevel(), center.x, center.y + 0.5, center.z, this.getCachedRecipe().getResultItem());
            this.getLevel().addFreshEntity((Entity)epi);
            ArrayList<ItemStack> byproducts = this.getCachedRecipe().rollByproducts(this.level.getRandom());
            for (ItemStack byproduct : byproducts) {
                InventoryUtilities.DropItemAt(byproduct, Vec3.atCenterOf((Vec3i)this.getBlockPos().above()), this.level, true);
            }
            this.copyCraftInputToLastCraft();
            this.activeCrystals.clear();
            this.collectedItems.clear();
            this.setStage(Stage.IDLE);
            this.setChanged();
        }
    }

    private boolean checkReCraftReagents(Player player) {
        if (player.isCreative()) {
            return true;
        }
        MutableBoolean progressionMet = new MutableBoolean(false);
        MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
            if (p.getTier() < this.lastCraftTier) {
                player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.lowtier"));
            } else if (this.lastCraftFaction != null && p.getAlliedFaction() != this.lastCraftFaction) {
                player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.wrongfaction"));
            } else {
                progressionMet.setTrue();
            }
        });
        if (!progressionMet.booleanValue()) {
            return false;
        }
        MutableBoolean reagentsPresent = new MutableBoolean(true);
        this.lastCraftItemsMerged.forEach(stack -> {
            if (!stack.isEmpty() && !InventoryUtilities.hasStackInInventory(stack, true, true, (IItemHandler)new InvWrapper((Container)player.getInventory()))) {
                reagentsPresent.setFalse();
            }
        });
        if (!reagentsPresent.getValue().booleanValue()) {
            player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.missing_items"));
            return false;
        }
        MutableBoolean eldrinMet = new MutableBoolean(false);
        MNACap.get(player.level(), WorldMagicProvider.MAGIC).ifPresent(m -> {
            HashMap<Affinity, Float> power = m.getWellspringRegistry().getNodeNetworkAmountFor(player);
            for (Affinity aff : this.lastCraftEldrin.keySet()) {
                if (!(power.getOrDefault(aff, Float.valueOf(0.0f)).floatValue() < this.lastCraftEldrin.getOrDefault(aff, Float.valueOf(0.0f)).floatValue())) continue;
                player.sendSystemMessage((Component)Component.translatable((String)"gui.mna.recipe.missing_wellspring", (Object[])new Object[]{aff.name()}));
                return;
            }
            eldrinMet.setTrue();
        });
        return eldrinMet.booleanValue();
    }

    private void consumeReCraftReagents(Player player) {
        if (player.isCreative()) {
            return;
        }
        MNACap.get(this.getLevel(), WorldMagicProvider.MAGIC).ifPresent(m -> this.lastCraftEldrin.entrySet().forEach(e -> m.getWellspringRegistry().consumePower(player.getGameProfile().getId(), this.level, (Affinity)(((e.getKey()))), ((Float)e.getValue()).floatValue())));
        this.lastCraftItemsMerged.forEach(stack -> InventoryUtilities.removeItemFromInventory(stack, true, true, (IItemHandler)new InvWrapper((Container)player.getInventory())));
    }

    public boolean reCraft(Player player) {
        if (!this.isEmpty() || this.getLevel().isClientSide()) {
            return false;
        }
        if (this.checkReCraftReagents(player)) {
            ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)this.getLevel().dimension(), 32.0f, (float)this.getBlockPos().getX() + 0.5f, (float)this.getBlockPos().getY() + 1.1f, (float)this.getBlockPos().getZ() + 0.5f, SpawnParticleEffectMessage.ParticleTypes.MANAWEAVE_CRAFT_COMPLETE);
            this.getLevel().playSound(null, (double)this.getBlockPos().getX(), (double)this.getBlockPos().getY(), (double)this.getBlockPos().getZ(), SFX.Event.Eldrin.CRAFT_COMPLETE, SoundSource.PLAYERS, 1.0f, (float)(0.9 + Math.random() * 0.2));
            this.setPlayerReference(player);
            this.consumeReCraftReagents(player);
            ItemStack output = this.lastCraftOutput.copy();
            if (player != null && player instanceof ServerPlayer) {
                CustomAdvancementTriggers.ELDRIN_ALTAR_CRAFT.trigger((ServerPlayer)player, output);
            }
            boolean sendUpdate = false;
            if (!player.getInventory().add(output)) {
                this.setItem(0, output);
                sendUpdate = true;
            }
            if (this._lastCraftRecipe == null && this.lastCraftRecipeID != null) {
                this.level.getRecipeManager().byKey(this.lastCraftRecipeID).ifPresent(recipe -> {
                    if (recipe instanceof EldrinAltarRecipe) {
                        EldrinAltarRecipe eaRecipe;
                        this._lastCraftRecipe = eaRecipe = (EldrinAltarRecipe)recipe;
                    }
                });
            }
            if (this._lastCraftRecipe != null) {
                int count = 1;
                ArrayList<ItemStack> byproducts = this._lastCraftRecipe.rollByproducts(this.level.getRandom());
                for (ItemStack byproduct : byproducts) {
                    if (player.getInventory().add(byproduct)) continue;
                    if (count++ < 9) {
                        this.setItem(count, byproduct);
                        sendUpdate = true;
                        continue;
                    }
                    InventoryUtilities.DropItemAt(byproduct, Vec3.atCenterOf((Vec3i)this.getBlockPos().above()), this.level, true);
                }
            }
            if (sendUpdate) {
                this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
            }
            return true;
        }
        return false;
    }

    private void copyCraftInputToLastCraft() {
        this.lastCraftItems.clear();
        this.lastCraftItemsMerged.clear();
        this.lastCraftEldrin.clear();
        for (CollectedItem ci : this.collectedItems) {
            ItemStack stack = ci.stack;
            this.lastCraftItems.add(stack.copy());
            Optional<ItemStack> existing = this.lastCraftItemsMerged.stream().filter(is -> ItemStack.isSameItemSameComponents((ItemStack)is, (ItemStack)stack)).findFirst();
            if (existing.isPresent()) {
                existing.get().setCount(existing.get().getCount() + stack.getCount());
                continue;
            }
            this.lastCraftItemsMerged.add(stack.copy());
        }
        this.__cachedRecipe.getPowerRequirements().forEach((a, f) -> this.lastCraftEldrin.put((Affinity)(a), (Float)f));
        this.lastCraftOutput = this.__cachedRecipe.getResultItem().copy();
        this._lastCraftRecipe = this.__cachedRecipe;
        this.lastCraftRecipeID = this.__cachedRecipe.getId();
    }

    public ItemStack getReCraftOutput() {
        return this.lastCraftOutput;
    }

    public HashMap<Affinity, Float> getReCraftEldrin() {
        return this.lastCraftEldrin;
    }

    public List<ItemStack> getReCraftInput() {
        return this.lastCraftItems.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
    }

    @Nullable
    public IFaction getLastCraftFaction() {
        return this.lastCraftFaction;
    }

    public int getLastCraftTier() {
        return this.lastCraftTier;
    }

    public void setReCraftRecipe(ItemStack output, List<ItemStack> inputs, IFaction faction, int tier, HashMap<Affinity, Float> powerRequirements) {
        this.lastCraftOutput = output;
        this.lastCraftItems.clear();
        this.lastCraftItemsMerged.clear();
        for (ItemStack stack : inputs) {
            this.lastCraftItems.add(stack.copy());
            Optional<ItemStack> existing = this.lastCraftItemsMerged.stream().filter(is -> ItemStack.matches((ItemStack)is, (ItemStack)stack)).findFirst();
            if (existing.isPresent()) {
                existing.get().setCount(existing.get().getCount() + stack.getCount());
                continue;
            }
            this.lastCraftItemsMerged.add(stack.copy());
        }
        this.lastCraftFaction = faction;
        this.lastCraftTier = tier;
        this.lastCraftEldrin = powerRequirements;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        super.setItem(index, stack);
        this.updateBlockState();
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack out = super.removeItem(index, count);
        this.updateBlockState();
        return out;
    }

    private void updateBlockState() {
        this.updateBlockState("invChange", 131);
    }

    private void updateBlockState(String id, Integer data) {
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), data.intValue());
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    public int getMaxStackSize() {
        return 1;
    }

    public ItemStack getDisplayedItem() {
        return this.getItem(0);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, Direction direction) {
        return this.getItem(0).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return !this.getItem(0).isEmpty();
    }

    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag base = super.getUpdateTag(registries);
        CompoundTag sub = this.writeAdditonal(new CompoundTag());
        base.put("invSync", (Tag)sub);
        CompoundTag powerStatus = new CompoundTag();
        this.powerRequirementStatus.entrySet().forEach(e -> powerStatus.putBoolean(((Affinity)((e.getKey()))).name(), ((Boolean)e.getValue()).booleanValue()));
        base.put("powerStatus", (Tag)powerStatus);
        return base;
    }

    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag, com.mna.api.tools.MNAItemData.provider());
        CompoundTag sub = tag.getCompound("invSync");
        this.readAdditional(sub);
        CompoundTag powerStatus = tag.getCompound("powerStatus");
        this.powerRequirementStatus.clear();
        for (int i = 0; i < Affinity.values().length; ++i) {
            Affinity aff = Affinity.values()[i];
            if (!powerStatus.contains(aff.name())) continue;
            this.powerRequirementStatus.put(aff, powerStatus.getBoolean(aff.name()));
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag sub = pkt.getTag().getCompound("invSync");
        this.readAdditional(sub);
        CompoundTag powerStatus = pkt.getTag().getCompound("powerStatus");
        this.powerRequirementStatus.clear();
        for (int i = 0; i < Affinity.values().length; ++i) {
            Affinity aff = Affinity.values()[i];
            if (!powerStatus.contains(aff.name())) continue;
            this.powerRequirementStatus.put(aff, powerStatus.getBoolean(aff.name()));
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        this.writeAdditonal(compound);
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        this.readAdditional(compound);
        super.loadAdditional(compound, registries);
    }

    private CompoundTag writeAdditonal(CompoundTag compound) {
        MNAItemData.saveItems((CompoundTag)compound, (NonNullList)this.inventoryItems);
        compound.putInt("stage", this.stage);
        compound.putInt("stageTicks", this.stageTicks);
        ListTag activeCrystals = new ListTag();
        this.activeCrystals.forEach(a -> activeCrystals.add(a.toNBT()));
        compound.put("activeCrystals", (Tag)activeCrystals);
        ListTag collectedItems = new ListTag();
        this.collectedItems.forEach(i -> collectedItems.add(i.toNBT()));
        compound.put("collectedItems", (Tag)collectedItems);
        CompoundTag consumed = new CompoundTag();
        this.consumedPower.entrySet().forEach(e -> consumed.putFloat(((Affinity)((e.getKey()))).name(), ((Float)e.getValue()).floatValue()));
        compound.put("consumedPower", (Tag)consumed);
        if (this.__crafterID != null) {
            compound.putString("crafter_uuid", this.__crafterID.toString());
        }
        if (this.__cachedRecipe != null) {
            compound.putString("cachedRecipeId", this.__cachedRecipe.getId().toString());
        }
        CompoundTag lastCraft = new CompoundTag();
        lastCraft.put("lastCraftReagents", (Tag)MNAItemData.saveItems((CompoundTag)new CompoundTag(), this.lastCraftItems));
        lastCraft.putInt("tier", this.lastCraftTier);
        if (this.lastCraftFaction != null) {
            lastCraft.putString("faction", Registries.Factions.getKey(this.lastCraftFaction).toString());
        }
        lastCraft.put("lastCraftOutput", (Tag)MNAItemData.save(this.lastCraftOutput, new CompoundTag()));
        CompoundTag eldrin = new CompoundTag();
        this.lastCraftEldrin.forEach((a, f) -> eldrin.putFloat(a.name(), f.floatValue()));
        lastCraft.put("eldrin", (Tag)eldrin);
        compound.put("lastCraft", (Tag)lastCraft);
        return compound;
    }

    private void readAdditional(CompoundTag compound) {
        this.clearContent();
        this.lastCraftItems.clear();
        for (int i = 0; i < 9; ++i) {
            this.lastCraftItems.add(ItemStack.EMPTY);
        }
        MNAItemData.loadItems((CompoundTag)compound, (NonNullList)this.inventoryItems);
        this.stage = compound.getInt("stage");
        this.stageTicks = compound.getInt("stageTicks");
        if (compound.contains("cachedRecipeId")) {
            this.__cachedRecipeId = ResourceLocation.parse(compound.getString("cachedRecipeId"));
        }
        ListTag list = compound.getList("activeCrystals", 10);
        this.activeCrystals.clear();
        list.forEach(inbt -> {
            CompoundTag activeCrystal = (CompoundTag)inbt;
            this.activeCrystals.add(ActiveCrystal.fromNBT(activeCrystal));
        });
        list = compound.getList("collectedItems", 10);
        this.collectedItems.clear();
        list.forEach(inbt -> {
            CompoundTag collectedItem = (CompoundTag)inbt;
            this.collectedItems.add(CollectedItem.fromNBT(collectedItem));
        });
        if (compound.contains("consumedPower")) {
            this.consumedPower.clear();
            CompoundTag consumed = compound.getCompound("consumedPower");
            for (Affinity aff : Affinity.values()) {
                if (!consumed.contains(aff.name())) continue;
                this.consumedPower.put(aff, Float.valueOf(consumed.getFloat(aff.name())));
            }
        }
        if (compound.contains("crafter_uuid")) {
            this.__crafterID = UUID.fromString(compound.getString("crafter_uuid"));
        }
        if (compound.contains("lastCraft")) {
            CompoundTag lastCraft = compound.getCompound("lastCraft");
            this.lastCraftOutput = ItemStack.parseOptional(this.getLevel().registryAccess(), lastCraft.getCompound("lastCraftOutput"));
            MNAItemData.loadItems((CompoundTag)lastCraft.getCompound("lastCraftReagents"), this.lastCraftItems);
            this.lastCraftTier = lastCraft.getInt("tier");
            if (lastCraft.contains("faction")) {
                ResourceLocation factionID = ResourceLocation.parse(lastCraft.getString("faction"));
                this.lastCraftFaction = (IFaction)Registries.Factions.getValue(factionID);
            }
            this.lastCraftEldrin.clear();
            if (lastCraft.contains("eldrin")) {
                CompoundTag eldrin = lastCraft.getCompound("eldrin");
                for (Affinity aff : Affinity.values()) {
                    if (!eldrin.contains(aff.name())) continue;
                    this.lastCraftEldrin.put(aff, Float.valueOf(eldrin.getFloat(aff.name())));
                }
            }
            this.lastCraftRecipeID = ResourceLocation.parse(lastCraft.getString("lastCraftRecipeID"));
            for (ItemStack stack : this.lastCraftItems) {
                Optional<ItemStack> existing = this.lastCraftItemsMerged.stream().filter(is -> ItemStack.matches((ItemStack)is, (ItemStack)stack)).findFirst();
                if (existing.isPresent()) {
                    existing.get().setCount(existing.get().getCount() + stack.getCount());
                    continue;
                }
                this.lastCraftItemsMerged.add(stack.copy());
            }
        }
    }

    public boolean MatchesReagent(ResourceLocation rLoc) {
        if (this.getItem(0).isEmpty()) {
            return false;
        }
        return MATags.isItemEqual(this.getItem(0), rLoc);
    }

    public ArrayList<ActiveCrystal> getActiveCrystals() {
        return this.activeCrystals;
    }

    public ArrayList<CollectedItem> getCollectedItems() {
        return this.collectedItems;
    }

    public static enum Stage {
        IDLE,
        FINDING_POWER,
        CONSUMING_POWER,
        CONSUMING_REAGENTS,
        CRAFTING,
        COMPLETING;

    }

    public static class CollectedItem {
        public final BlockPos tilePos;
        public final ItemStack stack;

        public CollectedItem(BlockPos pos, ItemStack stack) {
            this.tilePos = pos;
            this.stack = stack;
        }

        public CompoundTag toNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("pos", this.tilePos.asLong());
            MNAItemData.save(this.stack, nbt);
            return nbt;
        }

        public static CollectedItem fromNBT(CompoundTag nbt) {
            return new CollectedItem(BlockPos.of((long)nbt.getLong("pos")), ItemStack.parseOptional(this.getLevel().registryAccess(), nbt));
        }
    }

    public static class ActiveCrystal {
        public final Affinity affinity;
        public final int offsetIndex;
        public final BlockPos tilePos;

        public ActiveCrystal(Affinity affinity, int offsetIndex, BlockPos tilePos) {
            this.affinity = affinity;
            this.offsetIndex = offsetIndex;
            this.tilePos = tilePos;
        }

        public CompoundTag toNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("affinity", this.affinity.name());
            nbt.putInt("offsetIndex", this.offsetIndex);
            nbt.putLong("position", this.tilePos.asLong());
            return nbt;
        }

        public static ActiveCrystal fromNBT(CompoundTag nbt) {
            return new ActiveCrystal(Affinity.valueOf(nbt.getString("affinity")), nbt.getInt("offsetIndex"), BlockPos.of((long)nbt.getLong("position")));
        }
    }
}


