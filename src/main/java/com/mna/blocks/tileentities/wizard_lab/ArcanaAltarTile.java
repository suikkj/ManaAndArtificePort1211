 
package com.mna.blocks.tileentities.wizard_lab;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.advancements.CustomAdvancementTriggers;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.IManaweaveNotifiable;
import com.mna.api.blocks.tile.BlockPosCache;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.config.GeneralConfigValues;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.recipes.IManaweavePattern;
import com.mna.api.sound.SFX;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.tools.MATags;
import com.mna.blocks.tileentities.init.TileEntityInit;
import com.mna.blocks.tileentities.wizard_lab.WizardLabTile;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.gui.containers.block.ContainerArcaneAltar;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ItemManaweaveBottle;
import com.mna.network.ServerMessageDispatcher;
import com.mna.network.messages.to_client.SpawnParticleEffectMessage;
import com.mna.particles.types.movers.ParticleSphereOrbitMover;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternHelper;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.InventoryUtilities;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;
import com.mna.api.tools.MNACaps;

public class ArcanaAltarTile
extends WizardLabTile
implements GeoAnimatable,
IManaweaveNotifiable,
MenuProvider {
    private static final int ITEM_CONSUME_WAIT = 20;
    private static final int ITEM_RECHECK_WAIT = 100;
    private static final int INVENTORY_RESCAN_DELAY = 600;
    public static final int COMPLETION_TICKS = 60;
    public static final int SLOT_RECIPE = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final float growthPerTick = 0.005f;
    private SpellRecipe recipe;
    private HashMap<ResourceLocation, Integer> remainingReagents;
    private HashMap<ResourceLocation, Integer> remainingManaweaves;
    private int totalItems;
    private int totalWeaves;
    private ArrayList<ItemStack> remainingManaweaveItems;
    private IManaweavePattern requestingPattern;
    private List<Item> requestingStack = new ArrayList<Item>();
    private ArrayList<Pair<IItemHandler, Direction>> inventories;
    private States state = States.IDLE;
    private int waitCounter = 0;
    private int inventoryRescanWaitTime = 0;
    private CompoundTag pullParticleMeta;
    private BlockPosCache cache;
    private float targetItemCollectPct = 0.0f;
    private float itemCollectPct = 0.0f;
    private float manaweaveCollectPct = 0.0f;
    private int finalizeTicks = 0;

    public ArcanaAltarTile(BlockPos pWorldPosition, BlockState pBlockState) {
        this((BlockEntityType)TileEntityInit.ALTAR_OF_ARCANA.get(), pWorldPosition, pBlockState);
    }

    public ArcanaAltarTile(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState, 3);
        this.remainingManaweaves = new HashMap();
        this.remainingReagents = new HashMap();
        this.remainingManaweaveItems = new ArrayList();
        this.inventories = new ArrayList();
        this.cache = new BlockPosCache(this, 8, (p, w) -> {
            BlockEntity be = w.getBlockEntity(p);
            if (be == null) {
                return false;
            }
            java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(be, null);
            return handler.isPresent();
        });
        this.pullParticleMeta = new CompoundTag();
        this.pullParticleMeta.putLong("destination", this.getBlockPos().asLong());
    }

    public void reCacheRequirements() {
        this.remainingReagents.clear();
        this.remainingManaweaves.clear();
        this.remainingManaweaveItems.clear();
        ItemStack stack = this.getItem(0);
        if (stack.isEmpty()) {
            return;
        }
        SpellRecipe recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        if (recipe.isValid()) {
            this.addAllReagents((Collection<ResourceLocation>)SpellRecipe.getShapeReagents(stack));
            this.addAllReagents((Collection<ResourceLocation>)SpellRecipe.getComponentReagents(stack));
            this.addAllReagents((Collection<ResourceLocation>)SpellRecipe.getModifierReagents(stack, 0));
            this.addAllReagents((Collection<ResourceLocation>)SpellRecipe.getModifierReagents(stack, 1));
            this.addAllReagents((Collection<ResourceLocation>)SpellRecipe.getModifierReagents(stack, 2));
            SpellRecipe.getPatterns(stack).forEach(rLoc -> {
                if (this.remainingManaweaves.containsKey(rLoc)) {
                    this.remainingManaweaves.put((ResourceLocation)rLoc, this.remainingManaweaves.get(rLoc) + 1);
                } else {
                    this.remainingManaweaves.put((ResourceLocation)rLoc, 1);
                }
            });
            this.remainingManaweaves.entrySet().forEach(e -> {
                ItemStack bottle = new ItemStack((ItemLike)ItemInit.MANAWEAVE_BOTTLE.get());
                ManaweavingPattern pattern = ManaweavingPatternHelper.GetManaweavingRecipe(this.getLevel(), (ResourceLocation)e.getKey());
                if (pattern != null) {
                    ItemManaweaveBottle.setPattern(pattern, bottle);
                    bottle.setCount(((Integer)e.getValue()).intValue());
                    this.remainingManaweaveItems.add(bottle);
                }
            });
            this.totalItems = this.remainingReagents.values().stream().mapToInt(i -> i).sum();
            this.totalWeaves = this.remainingManaweaves.values().stream().mapToInt(i -> i).sum();
        }
    }

    public HashMap<ResourceLocation, Integer> getRemainingReagents() {
        return this.remainingReagents;
    }

    public List<ItemStack> getRemainingManaweaves() {
        return this.remainingManaweaveItems;
    }

    @OnlyIn(value=Dist.CLIENT)
    public Vector3f getCandlePos(int index, float partialTick) {
        float tickOffset = (float)Math.sin(((float)ManaAndArtifice.instance.proxy.getGameTicks() + partialTick + (float)(index * 5 * index)) / 40.0f) * 0.0125f;
        Vector3f offsets = new Vector3f();
        switch (index) {
            case 1: {
                offsets = new Vector3f(0.171875f, 1.125f + tickOffset, 0.25f);
                break;
            }
            case 2: {
                offsets = new Vector3f(0.140625f, 1.15625f + tickOffset, 0.5f);
                break;
            }
            case 3: {
                offsets = new Vector3f(0.296875f, 1.25f + tickOffset, 0.75f);
                break;
            }
            case 4: {
                offsets = new Vector3f(0.703725f, 1.21875f + tickOffset, 0.75f);
                break;
            }
            case 5: {
                offsets = new Vector3f(0.859375f, 1.1875f + tickOffset, 0.5f);
                break;
            }
            default: {
                offsets = new Vector3f(0.828125f, 1.09375f + tickOffset, 0.25f);
            }
        }
        offsets.add(-0.5f, 0.0f, -0.5f);
        return offsets;
    }

    public States getState() {
        return this.state;
    }

    @Nullable
    public IManaweavePattern getRequestingPattern() {
        return this.requestingPattern;
    }

    public List<Item> getRequestingStack() {
        return this.requestingStack;
    }

    public static void Tick(Level level, BlockPos pos, BlockState state, ArcanaAltarTile tile) {
        if (tile.waitCounter > 0) {
            --tile.waitCounter;
        }
        if (tile.inventoryRescanWaitTime > 0) {
            --tile.inventoryRescanWaitTime;
        }
        if (tile.cache.isSearching()) {
            tile.cache.tick();
        }
        WizardLabTile.Tick(tile.getLevel(), pos, state, tile);
        if (tile.state == States.FINALIZING) {
            ++tile.finalizeTicks;
        }
        if (tile.getLevel().isClientSide()) {
            tile.ClientTick();
        }
    }

    public void ClientTick() {
        if (this.isActive()) {
            this.spawnParticleForAffinity(Affinity.EARTH, 6);
            this.spawnParticleForAffinity(Affinity.ENDER, 5);
            this.spawnParticleForAffinity(Affinity.WIND, 4);
            this.spawnParticleForAffinity(Affinity.FIRE, 3);
            this.spawnParticleForAffinity(Affinity.ARCANE, 2);
            this.spawnParticleForAffinity(Affinity.WATER, 1);
            Vec3 pos = Vec3.upFromBottomCenterOf((Vec3i)this.getBlockPos(), (double)2.5);
            if (this.state.ordinal() >= States.GATHERING_REAGENTS.ordinal() && this.getActiveTicks() > 60) {
                float amount = 20.0f;
                int i = 0;
                while ((float)i < amount) {
                    float pct = (float)i / amount;
                    double radius = 0.5 * (double)pct;
                    double angle = 360.0f * pct;
                    int[] color = Math.random() < 0.5 ? Affinity.ARCANE.getColor() : Affinity.ARCANE.getSecondaryColor();
                    this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_VELOCITY.get())).setColor(color[0], color[1], color[2]).setMover(new ParticleSphereOrbitMover(pos.x(), pos.y(), pos.z(), 0.1, angle, radius)), pos.x(), pos.y(), pos.z(), 0.0, 0.0, 0.0);
                    ++i;
                }
            }
            if (this.state == States.FINALIZING && this.finalizeTicks > 10) {
                for (int i = 0; i < 150; ++i) {
                    this.getLevel().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), pos.x(), pos.y() - 1.4, pos.z(), -0.5 + Math.random(), 0.01, -0.5 + Math.random());
                }
            }
            if (this.itemCollectPct < this.targetItemCollectPct) {
                this.itemCollectPct += 0.005f;
            }
            if (this.itemCollectPct > this.targetItemCollectPct) {
                this.itemCollectPct = this.targetItemCollectPct;
            }
        }
    }

    private void spawnParticleForAffinity(Affinity aff, int index) {
        boolean bigBurst = false;
        int age = bigBurst ? 20 : 5;
        int amount = bigBurst ? 20 : 1;
        int[] color = aff.getColor();
        MAParticleType type = new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.FLAME.get())).setColor(color[0], color[1], color[2]).setScale(0.02f).setMaxAge(age + (int)(Math.random() * (double)age));
        BlockPos pos = this.getBlockPos();
        Vector3f offset = new Vector3f((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
        Vector3f candle = this.getCandlePos(index, 0.0f);
        BlockState state = this.getBlockState();
        switch ((Direction)state.getValue((Property)HorizontalDirectionalBlock.FACING)) {
            case EAST: {
                candle.rotate((Quaternionfc)Axis.YP.rotationDegrees(270.0f));
                break;
            }
            case SOUTH: {
                candle.rotate((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
                break;
            }
            case WEST: {
                candle.rotate((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
                break;
            }
        }
        if (index % 2 == 0) {
            candle.add(0.5f, 0.34375f, 0.5f);
        } else {
            candle.add(0.5f, 0.40625f, 0.5f);
        }
        offset.add((Vector3fc)candle);
        float puffSize = -0.005f;
        float puffSize2 = 0.01f;
        for (int i = 0; i < amount; ++i) {
            Vector3f velocity = bigBurst ? new Vector3f(puffSize + (float)Math.random() * puffSize2, 0.01f + (float)Math.random() * 0.005f, puffSize + (float)Math.random() * puffSize2) : new Vector3f(0.0f, 0.025f, 0.0f);
            this.getLevel().addParticle((ParticleOptions)type, (double)offset.x(), (double)offset.y(), (double)offset.z(), (double)velocity.x(), (double)velocity.y(), (double)velocity.z());
        }
    }

    private void setState(States newState) {
        this.state = newState;
        this.syncAndSave();
    }

    private void addAllReagents(Collection<ResourceLocation> reagents) {
        if (reagents == null) {
            return;
        }
        reagents.forEach(rLoc -> {
            if (this.remainingReagents.containsKey(rLoc)) {
                this.remainingReagents.put((ResourceLocation)rLoc, this.remainingReagents.get(rLoc) + 1);
            } else {
                this.remainingReagents.put((ResourceLocation)rLoc, 1);
            }
        });
    }

    private void cacheNearbyInventories() {
        if (!this.getLevel().isClientSide() && this.cache.finishedSearchingThisTick()) {
            this.inventories.clear();
            this.cache.getCachedPositions().forEach(bp -> {
                BlockEntity be = this.getLevel().getBlockEntity(bp);
                if (be == null) {
                    return;
                }
                java.util.Optional<net.neoforged.neoforge.items.IItemHandler> handler = MNACaps.itemHandler(be, null);
                if (!handler.isPresent()) {
                    return;
                }
                this.inventories.add((Pair<IItemHandler, Direction>)new Pair((handler.get()), null));
            });
        }
    }

    private void tickLogic_gatherReagents() {
        if (this.getActiveTicks() < 100) {
            return;
        }
        if (this.remainingReagents.size() == 0) {
            this.setState(States.GATHERING_MANAWEAVES);
            return;
        }
        if (this.waitCounter > 0) {
            return;
        }
        this.cacheNearbyInventories();
        ResourceLocation search = this.remainingReagents.keySet().iterator().next();
        List<Item> items = MATags.smartLookupItem(search);
        while (items == null || items.size() == 0) {
            this.remainingReagents.remove(search);
            if (this.remainingReagents.size() == 0) {
                return;
            }
            search = this.remainingReagents.keySet().iterator().next();
            items = MATags.smartLookupItem(search);
        }
        int count = this.remainingReagents.get(search);
        if (InventoryUtilities.consumeAcrossInventories(items, count, true, false, true, this.inventories)) {
            InventoryUtilities.consumeAcrossInventories(items, count, true, false, false, this.inventories);
            BlockPos randomInventoryPos = (BlockPos)this.cache.getCachedPositions().get((int)((double)this.cache.getCachedPositions().size() * Math.random()));
            Vec3 origin = Vec3.atCenterOf((Vec3i)randomInventoryPos);
            ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)this.getLevel().dimension(), 32.0f, origin.x, origin.y, origin.z, SpawnParticleEffectMessage.ParticleTypes.ENSORCELLATION_ITEM_PULL, this.pullParticleMeta);
            this.syncAndSave();
            this.remainingReagents.remove(search);
            this.waitCounter = 20;
            this.requestingStack.clear();
            this.syncAndSave();
        } else {
            this.requestingStack.clear();
            this.requestingStack.addAll(items);
            this.syncAndSave();
            if (this.inventoryRescanWaitTime <= 0) {
                this.inventoryRescanWaitTime = 600;
                this.cache.queueRecheck();
            }
            this.waitCounter = 100;
        }
    }

    private void tickLogic_gatherManaweaves() {
        if (this.remainingManaweaves.size() == 0) {
            this.getLevel().playSound(null, (double)this.worldPosition.getX(), (double)this.worldPosition.getY(), (double)this.worldPosition.getZ(), SFX.Event.AltarOfArcana.COMPLETE, SoundSource.BLOCKS, 1.0f, 1.0f);
            this.setState(States.FINALIZING);
            return;
        }
        if (this.waitCounter > 0) {
            return;
        }
        this.cacheNearbyInventories();
        ItemStack search = new ItemStack((ItemLike)ItemInit.MANAWEAVE_BOTTLE.get());
        ResourceLocation rLoc = this.remainingManaweaves.keySet().iterator().next();
        ManaweavingPattern pattern = ManaweavingPatternHelper.GetManaweavingRecipe(this.getLevel(), rLoc);
        if (pattern == null) {
            this.remainingManaweaves.remove(rLoc);
            return;
        }
        int count = this.remainingManaweaves.get(rLoc);
        ItemManaweaveBottle.setPattern(pattern, search);
        search.setCount(count);
        if (InventoryUtilities.consumeAcrossInventories(search, true, true, true, this.inventories)) {
            InventoryUtilities.consumeAcrossInventories(search, true, true, false, this.inventories);
            BlockPos randomInventoryPos = (BlockPos)this.cache.getCachedPositions().get((int)((double)this.cache.getCachedPositions().size() * Math.random()));
            Vec3 origin = Vec3.atCenterOf((Vec3i)randomInventoryPos);
            ServerMessageDispatcher.sendParticleEffect((ResourceKey<Level>)this.getLevel().dimension(), 32.0f, origin.x, origin.y, origin.z, SpawnParticleEffectMessage.ParticleTypes.ENSORCELLATION_ITEM_PULL, this.pullParticleMeta);
            this.syncAndSave();
            this.remainingManaweaves.remove(rLoc);
            this.waitCounter = 20;
            this.syncAndSave();
        } else {
            if (this.requestingPattern != pattern) {
                this.requestingPattern = pattern;
                this.syncAndSave();
            }
            if (this.inventoryRescanWaitTime <= 0) {
                this.inventoryRescanWaitTime = 600;
                this.cache.queueRecheck();
            }
            this.waitCounter = 100;
        }
    }

    private void tickLogic_finalize() {
        Player craftingPlayer;
        if (this.finalizeTicks < 60) {
            return;
        }
        ItemStack output = this.getItem(1).copy();
        if (output.getItem() == ItemInit.VELLUM.get()) {
            output = new ItemStack((ItemLike)ItemInit.SPELL.get());
        }
        if ((craftingPlayer = this.getCrafter()) != null && craftingPlayer instanceof ServerPlayer) {
            CustomAdvancementTriggers.CRAFT_SPELL.trigger((ServerPlayer)craftingPlayer, this.recipe);
            if (GeneralConfigValues.LiteMode) {
                MNACap.get(craftingPlayer, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
                    if (this.recipe.getShape() != null) {
                        r.addRoteXP(null, (ISpellComponent)((ModifiedSpellPart)this.recipe.getShape()).getPart(), ((Shape)((ModifiedSpellPart)this.recipe.getShape()).getPart()).requiredXPForRote());
                    }
                    this.recipe.iterateComponents(comp -> {
                        if (comp != null) {
                            r.addRoteXP(null, (ISpellComponent)comp.getPart(), ((SpellEffect)comp.getPart()).requiredXPForRote());
                        }
                    });
                    this.recipe.getModifiers().forEach(m -> r.addRoteXP(null, (ISpellComponent)m, m.requiredXPForRote()));
                });
            }
        }
        this.recipe.writeToNBT(output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        this.clearContent();
        this.setItem(2, output);
        this.setState(States.IDLE);
    }

    @Override
    public boolean notify(Level world, BlockPos pos, BlockState state, List<IManaweavePattern> patterns, @Nullable LivingEntity activator) {
        if (this.requestingPattern != null && patterns.contains(this.requestingPattern)) {
            Integer remainingCount = this.remainingManaweaves.get(this.requestingPattern.getRegistryId());
            if (remainingCount != null) {
                int remaining = remainingCount - 1;
                this.remainingManaweaves.put(this.requestingPattern.getRegistryId(), remaining);
                if (this.remainingManaweaves.get(this.requestingPattern.getRegistryId()) <= 0) {
                    this.remainingManaweaves.remove(this.requestingPattern.getRegistryId());
                }
            }
            this.requestingPattern = null;
            this.syncAndSave();
            return true;
        }
        return false;
    }

    @Override
    protected PlayState handleAnimState(AnimationState<? extends WizardLabTile> state) {
        if (this.isActive()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.laboratory_ensorcellation_armature.in").thenLoop("animation.laboratory_ensorcellation_armature.working"));
        }
        return state.setAndContinue(RawAnimation.begin().thenPlay("animation.laboratory_ensorcellation_armature.out").thenLoop("animation.laboratory_ensorcellation_armature.idle"));
    }

    @Override
    protected void tickActiveLogic() {
        switch (this.state) {
            case FINALIZING: {
                this.tickLogic_finalize();
                break;
            }
            case GATHERING_MANAWEAVES: {
                this.tickLogic_gatherManaweaves();
                break;
            }
            case GATHERING_REAGENTS: {
                this.tickLogic_gatherReagents();
                break;
            }
        }
    }

    @Override
    public void onCraftStart(Player crafter) {
        if (this.getLevel().isClientSide()) {
            return;
        }
        this.reCacheRequirements();
        this.itemCollectPct = 0.0f;
        this.manaweaveCollectPct = 0.0f;
        this.finalizeTicks = 0;
        this.requestingPattern = null;
        this.requestingStack.clear();
        if (this.cache.getCachedPositions().size() == 0) {
            this.inventoryRescanWaitTime = 600;
            this.cache.queueRecheck();
        }
        this.getLevel().playSound(null, (double)this.worldPosition.getX(), (double)this.worldPosition.getY(), (double)this.worldPosition.getZ(), SFX.Event.AltarOfArcana.START, SoundSource.BLOCKS, 1.0f, 1.0f);
        this.setState(States.GATHERING_REAGENTS);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SFX.Loops.ALTAR_OF_ARCANA;
    }

    @Override
    protected boolean canActivate(Player crafter) {
        ItemStack recipeStack = this.getItem(0);
        if (SpellRecipe.stackContainsSpell(recipeStack)) {
            this.recipe = SpellRecipe.fromNBT(recipeStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        }
        if (this.recipe == null || !this.recipe.isValid()) {
            return false;
        }
        if (this.getItem(1).isEmpty() || this.getItem(0).isEmpty()) {
            return false;
        }
        if (!this.getItem(2).isEmpty()) {
            return false;
        }
        java.util.Optional<PlayerProgressionProvider> lazyProgression = MNACap.get(crafter, PlayerProgressionProvider.PROGRESSION);
        if (!lazyProgression.isPresent()) {
            return false;
        }
        IPlayerProgression progression = (IPlayerProgression)lazyProgression.get();
        if (this.recipe.getTier(this.level) > progression.getTier() || this.recipe.getComplexity() > (float)progression.getTierMaxComplexity()) {
            return false;
        }
        return this.recipe.getFaction() == null || this.recipe.getFaction().isAlliedTo(progression.getAlliedFaction());
    }

    @Override
    protected boolean canContinue() {
        if (this.recipe == null || !this.recipe.isValid()) {
            return false;
        }
        if (this.getItem(1).isEmpty() || this.getItem(0).isEmpty()) {
            return false;
        }
        return this.getItem(2).isEmpty();
    }

    @Override
    protected List<Integer> getSyncedInventorySlots() {
        ArrayList<Integer> output = new ArrayList<Integer>();
        output.add(1);
        output.add(0);
        output.add(2);
        return output;
    }

    @Override
    public float getPctComplete() {
        if (this.state == States.IDLE) {
            return 0.0f;
        }
        return 0.375f * this.itemCollectPct + 0.375f * this.manaweaveCollectPct + 0.25f * ((float)this.finalizeTicks / 60.0f);
    }

    public float getItemCollectPct(float partialTick) {
        if (this.itemCollectPct == this.targetItemCollectPct) {
            return this.itemCollectPct;
        }
        return this.itemCollectPct + 0.005f * partialTick;
    }

    public float getManaweaveCollectPct() {
        return this.manaweaveCollectPct;
    }

    public int getCompletionTicks() {
        return this.finalizeTicks;
    }

    public float getBeamPct(float partialTick) {
        return Math.min(((float)this.finalizeTicks + partialTick) / 10.0f, 1.0f);
    }

    @Override
    protected CompoundTag getMeta() {
        CompoundTag meta = new CompoundTag();
        meta.putInt("state", this.state.ordinal());
        if (this.requestingPattern != null) {
            meta.putString("requestingPattern", this.requestingPattern.getRegistryId().toString());
        }
        if (!this.requestingStack.isEmpty()) {
            ListTag itemList = new ListTag();
            this.requestingStack.forEach(i -> itemList.add(StringTag.valueOf((String)BuiltInRegistries.ITEM.getKey(i).toString())));
            meta.put("requestingStack", (Tag)itemList);
        }
        int remainingItems = this.remainingReagents.values().stream().mapToInt(i -> i).sum();
        int remainingWeaves = this.remainingManaweaves.values().stream().mapToInt(i -> i).sum();
        this.itemCollectPct = 1.0f - (float)remainingItems / (float)this.totalItems;
        this.manaweaveCollectPct = 1.0f - (float)remainingWeaves / (float)this.totalWeaves;
        meta.putFloat("pctItems", this.itemCollectPct);
        meta.putFloat("pctWeaves", this.manaweaveCollectPct);
        meta.putInt("finalizeTicks", this.finalizeTicks);
        return meta;
    }

    @Override
    protected void loadMeta(CompoundTag tag) {
        if (tag.contains("state")) {
            this.state = States.values()[tag.getInt("state")];
        }
        if (tag.contains("pctItems")) {
            this.targetItemCollectPct = tag.getFloat("pctItems");
            if (this.itemCollectPct > this.targetItemCollectPct) {
                this.itemCollectPct = this.targetItemCollectPct;
            }
        } else {
            this.targetItemCollectPct = 0.0f;
            this.itemCollectPct = 0.0f;
        }
        this.finalizeTicks = tag.contains("finalizeTicks") ? tag.getInt("finalizeTicks") : 0;
        this.manaweaveCollectPct = tag.contains("pctWeaves") ? tag.getFloat("pctWeaves") : 0.0f;
        this.requestingPattern = tag.contains("requestingPattern") ? ManaweavingPatternHelper.GetManaweavingRecipe(this.getLevel(), ResourceLocation.parse(tag.getString("requestingPattern"))) : null;
        if (tag.contains("requestingStack")) {
            this.requestingStack.clear();
            ListTag itemList = tag.getList("requestingStack", 8);
            itemList.forEach(t -> {
                ResourceLocation rLoc = new ResourceLocation(((StringTag)t).getAsString());
                Item item = (Item)BuiltInRegistries.ITEM.get(rLoc);
                if (item != null) {
                    this.requestingStack.add(item);
                }
            });
        } else {
            this.requestingStack.clear();
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        CompoundTag recipeData = new CompoundTag();
        if (this.recipe != null) {
            this.recipe.writeToNBT(recipeData);
            compound.put("recipe", (Tag)recipeData);
        }
        compound.putInt("state", this.state.ordinal());
        if (this.remainingReagents.size() > 0) {
            ListTag reagentsList = new ListTag();
            this.remainingReagents.forEach((rLoc, count) -> {
                CompoundTag reagentTag = new CompoundTag();
                reagentTag.putString("rLoc", rLoc.toString());
                reagentTag.putInt("count", count.intValue());
                reagentsList.add(reagentTag);
            });
            compound.put("remainingReagents", (Tag)reagentsList);
        }
        if (this.remainingManaweaves.size() > 0) {
            ListTag manaweavesList = new ListTag();
            this.remainingManaweaves.forEach((rLoc, count) -> {
                CompoundTag reagentTag = new CompoundTag();
                reagentTag.putString("rLoc", rLoc.toString());
                reagentTag.putInt("count", count.intValue());
                manaweavesList.add(reagentTag);
            });
            compound.put("remainingManaweaves", (Tag)manaweavesList);
        }
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        this.recipe = SpellRecipe.fromNBT(compound.getCompound("recipe"));
        if (compound.contains("state")) {
            this.state = States.values()[compound.getInt("state")];
        }
        if (compound.contains("remainingReagents")) {
            this.remainingReagents.clear();
            ListTag reagentsList = compound.getList("remainingReagents", 10);
            reagentsList.forEach(t -> {
                CompoundTag tag = (CompoundTag)t;
                ResourceLocation rLoc = ResourceLocation.parse(tag.getString("rLoc"));
                int count = tag.getInt("count");
                this.remainingReagents.put(rLoc, count);
            });
        }
        if (compound.contains("remainingManaweaves")) {
            this.remainingManaweaves.clear();
            ListTag manaweavesList = compound.getList("remainingManaweaves", 10);
            manaweavesList.forEach(t -> {
                CompoundTag tag = (CompoundTag)t;
                ResourceLocation rLoc = ResourceLocation.parse(tag.getString("rLoc"));
                int count = tag.getInt("count");
                this.remainingManaweaves.put(rLoc, count);
            });
        }
    }

    @Override
    protected boolean shouldLoopingSoundPlay(String ID) {
        return super.shouldLoopingSoundPlay(ID) && this.state != States.FINALIZING && this.state != States.IDLE;
    }

    public AbstractContainerMenu createMenu(int id, Inventory pInventory, Player pPlayer) {
        return new ContainerArcaneAltar(id, pInventory, this);
    }

    public Component getDisplayName() {
        return Component.translatable((String)"mna:container.ensorcellation_station");
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index == 0) {
            if (SpellRecipe.stackContainsSpell(stack)) {
                this.recipe = SpellRecipe.fromNBT(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                this.reCacheRequirements();
            } else {
                this.recipe = null;
            }
        }
        super.setItem(index, stack);
    }

    public static enum States {
        IDLE,
        GATHERING_REAGENTS,
        GATHERING_MANAWEAVES,
        FINALIZING;

    }
}


