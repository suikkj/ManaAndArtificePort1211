 
package com.mna.entities.faction;

import com.google.common.collect.ImmutableList;
import com.mna.Registries;
import com.mna.api.events.BrokerSelectingTradesEvent;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.items.IRelic;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.EntityInit;
import com.mna.items.ItemInit;
import com.mna.items.ritual.ThaumaturgicLink;
import com.mna.items.sorcery.ItemTornJournalPage;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.eldrin.EldrinAltarRecipe;
import com.mna.recipes.manaweaving.ManaweavingRecipe;
import com.mna.spells.components.ComponentFling;
import com.mna.tools.BiomeUtils;
import com.mna.tools.StructureUtils;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;

public class Broker
extends WanderingTrader
implements GeoEntity {
    private static List<Item> allFactionItems;
    private MerchantOffers customerLimitedOffers;
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    public static final int NUM_FRAGMENTS = 5;
    private static final EntityDataAccessor<Boolean> DESPAWNING;
    private static final EntityDataAccessor<Boolean> DESPAWNING_HOSTILE;

    public Broker(EntityType<? extends WanderingTrader> type, Level worldIn) {
        super(type, worldIn);
    }

    public Broker(Level world) {
        this((EntityType<? extends WanderingTrader>)((EntityType)EntityInit.BROKER.get()), world);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new TradeWithPlayerGoal((AbstractVillager)this));
        this.goalSelector.addGoal(1, (Goal)new LookAtTradingPlayerGoal((AbstractVillager)this));
        this.goalSelector.addGoal(10, (Goal)new LookAtPlayerGoal((Mob)this, Mob.class, 8.0f));
    }

    protected SoundEvent getAmbientSound() {
        return null;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return null;
    }

    protected SoundEvent getDeathSound() {
        return null;
    }

    public SoundEvent getNotifyTradeSound() {
        return SFX.Entity.Broker.DEAL;
    }

    protected SoundEvent getTradeUpdatedSound(boolean getYesSound) {
        if (getYesSound) {
            return SFX.Entity.Broker.DEAL;
        }
        return null;
    }

    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DESPAWNING, false);
        builder.define(DESPAWNING_HOSTILE, false);
    }

    public void tick() {
        super.tick();
        if (!((Boolean)this.entityData.get(DESPAWNING)).booleanValue() && this.getDespawnDelay() == 1) {
            this.setDespawnDelay(10);
            this.entityData.set(DESPAWNING, true);
        }
    }

    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        if (this.level().isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SFX.Spell.Impact.AoE.ARCANE, SoundSource.NEUTRAL, 1.0f, 1.0f, true);
            for (int i = 0; i < 200; ++i) {
                int type = (int)Math.floor(Math.random() * 4.0);
                if (type != 2) {
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)(type == 0 ? (ParticleType)ParticleInit.ARCANE.get() : (type == 2 ? (ParticleType)ParticleInit.DUST.get() : (ParticleType)ParticleInit.FLAME.get()))), this.getX() + Math.random() * 2.0 - 1.0, this.getY() + Math.random() * 2.5, this.getZ() + Math.random() * 2.0 - 1.0, Math.random() * 0.2 - 0.1, Math.random() * 0.2 - 0.1, Math.random() * 0.2 - 0.1);
                    continue;
                }
                this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.EARTH.get())), this.getX() + Math.random() * 2.0 - 1.0, this.getY() + Math.random() * 2.5, this.getZ() + Math.random() * 2.0 - 1.0, 0.18, 0.3, 0.03);
            }
            if (((Boolean)this.entityData.get(DESPAWNING_HOSTILE)).booleanValue()) {
                int count = 180;
                double angleRads = 0.0;
                double step = Math.PI * 2 / (double)count;
                double radius = 6.0;
                for (int i = 0; i < count; ++i) {
                    Vec3 dir = new Vec3(Math.cos(angleRads += step), Math.random() * 0.1, Math.sin(angleRads)).normalize();
                    Vec3 vel = dir.scale((double)0.3f * radius);
                    dir = dir.scale(0.2);
                    this.level().addParticle((ParticleOptions)new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.AIR_VELOCITY.get())).setScale(0.2f).setColor(10, 10, 10), this.getX() + dir.x, this.getY() + Math.random(), this.getZ() + dir.z, vel.x, vel.y, vel.z);
                }
            }
        } else if (((Boolean)this.entityData.get(DESPAWNING_HOSTILE)).booleanValue()) {
            Vec3 bEyepos = this.position();
            this.level().getEntities((Entity)this, this.getBoundingBox().inflate(6.0), e -> e instanceof LivingEntity && e.isAlive() && e.isAddedToLevel()).stream().map(e -> (LivingEntity)e).forEach(e -> {
                Vec3 direction = e.getEyePosition().subtract(bEyepos);
                ComponentFling.flingTarget(e, direction, 6.0f, 3.0f, 0.0f);
            });
        }
    }

    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return super.hurt(source, amount);
        }
        if (!((Boolean)this.entityData.get(DESPAWNING)).booleanValue()) {
            this.setDespawnDelay(10);
            this.entityData.set(DESPAWNING, true);
            this.entityData.set(DESPAWNING_HOSTILE, true);
        }
        return false;
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 120.0).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    public void notifyTradeUpdated(ItemStack stack) {
    }

    public void notifyTrade(MerchantOffer offer) {
        this.offers.stream().filter(o -> o.satisfiedBy(offer.getCostA(), offer.getCostB())).findFirst().ifPresent(o -> o.increaseUses());
        this.playSound(this.getTradeUpdatedSound(true), this.getSoundVolume(), this.getVoicePitch());
        super.notifyTrade(offer);
    }

    protected void addOffersFromItemListings(MerchantOffers givenMerchantOffers, VillagerTrades.ItemListing[] newTrades, int maxNumbers) {
        ItemStack output;
        if (this.level().isClientSide()) {
            return;
        }
        givenMerchantOffers.clear();
        if (allFactionItems == null) {
            allFactionItems = BuiltInRegistries.ITEM.getValues().stream().filter(i -> i instanceof IFactionSpecific).collect(Collectors.toList());
        }
        Random rand = new Random();
        ServerLevel serverLevel = (ServerLevel)this.level();
        for (Item factionItem : allFactionItems) {
            IFaction thisFaction = ((IFactionSpecific)factionItem).getFaction();
            if (thisFaction == null) continue;
            output = new ItemStack((ItemLike)factionItem);
            List opposingFactionItems = allFactionItems.stream().filter(i -> ((IFactionSpecific)i).getFaction() != null && ((IFactionSpecific)i).getFaction() != thisFaction).collect(Collectors.toList());
            ItemStack opposingItemPrice = new ItemStack((ItemLike)opposingFactionItems.get(rand.nextInt(opposingFactionItems.size())));
            ItemStack tokenPrice = this.getRandomOpposingToken(thisFaction);
            if (tokenPrice.isEmpty() || opposingItemPrice.isEmpty()) continue;
            tokenPrice.setCount(32 + rand.nextInt(32));
            MerchantOffer offer = new MerchantOffer(opposingItemPrice, tokenPrice, output, 1, 0, 1.0f);
            givenMerchantOffers.add(offer);
        }
        List all_factions = Registries.Factions.getValues().stream().toList();
        for (int i2 = 0; i2 < 5; ++i2) {
            IFaction selected = (IFaction)all_factions.get(rand.nextInt(all_factions.size()));
            output = ItemTornJournalPage.getRandomPage(rand, selected);
            if (output.isEmpty()) continue;
            ItemStack fragmentPrice = new ItemStack((ItemLike)ItemInit.ENTITY_ENTRAPMENT_CRYSTAL.get());
            MerchantOffer offer = new MerchantOffer(fragmentPrice, ItemStack.EMPTY, output, 1, 1, 1.0f);
            givenMerchantOffers.add(offer);
        }
        ItemStack costItem = new ItemStack((ItemLike)ItemInit.SPELL_PART_THESIS.get());
        ItemStack costItemB = new ItemStack((ItemLike)ItemInit.SPELL_PART_THESIS.get());
        StructureUtils.getAllStructures(serverLevel).forEach(structureFeature -> {
            if (!structureFeature.is(MATags.Structures.BROKER_STRUCTURES)) {
                return;
            }
            ItemStack stack = new ItemStack((ItemLike)ItemInit.THAUMATURGIC_LINK.get());
            ThaumaturgicLink.setStructureKey(serverLevel, stack, (Holder<Structure>)structureFeature);
            MerchantOffer offer = new MerchantOffer(costItem.copy(), costItemB.copy(), stack, 1, 0, 1.0f);
            givenMerchantOffers.add(offer);
        });
        BiomeUtils.getAllBiomes(serverLevel).forEach(biome -> {
            if (!biome.is(MATags.Biomes.BROKER_BIOMES)) {
                return;
            }
            ItemStack stack = new ItemStack((ItemLike)ItemInit.THAUMATURGIC_LINK.get());
            ThaumaturgicLink.setBiomeKey(serverLevel, stack, (Holder<Biome>)biome);
            MerchantOffer offer = new MerchantOffer(costItem.copy(), costItemB.copy(), stack, 1, 0, 1.0f);
            givenMerchantOffers.add(offer);
        });
        NeoForge.EVENT_BUS.post(new BrokerSelectingTradesEvent((Merchant)this, givenMerchantOffers, newTrades, maxNumbers, (ImmutableList<Item>)ImmutableList.copyOf(allFactionItems)));
    }

    private ItemStack getRandomOpposingToken(IFaction faction) {
        List<IFaction> enemies = faction.getEnemyFactions();
        if (enemies.size() == 0) {
            return ItemStack.EMPTY;
        }
        IFaction selected = enemies.get((int)(Math.random() * (double)enemies.size()));
        if (selected.getTokenItem() == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack((ItemLike)selected.getTokenItem());
    }

    protected void updateTrades() {
        MerchantOffers merchantoffers = this.getOffers();
        this.addOffersFromItemListings(merchantoffers, null, 5);
    }

    public MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.updateTrades();
        }
        return this.customerLimitedOffers != null ? this.customerLimitedOffers : this.offers;
    }

    public void setTradingPlayer(Player player) {
        super.setTradingPlayer(player);
        if (player != null) {
            this.getOffers();
            this.customerLimitedOffers = new MerchantOffers(this.offers.createTag());
            MNACap.get(player, PlayerProgressionProvider.PROGRESSION).ifPresent(p -> {
                for (int i = 0; i < this.customerLimitedOffers.size(); ++i) {
                    MerchantOffer offer = (MerchantOffer)this.customerLimitedOffers.get(i);
                    Item output = offer.assemble().getItem();
                    if (output == ItemInit.TORN_JOURNAL_PAGE.get() || output instanceof IRelic) continue;
                    int requiredTier = 0;
                    Optional<ManaweavingRecipe> recipe = this.level().getRecipeManager().getAllRecipesFor(RecipeInit.MANAWEAVING_RECIPE_TYPE.get()).stream().filter(r -> r.value().getResultItem().getItem() == output).map(r -> r.value()).findFirst();
                    if (recipe.isPresent()) {
                        requiredTier = recipe.get().getTier();
                    } else {
                        Optional<EldrinAltarRecipe> altar_recipe = this.level().getRecipeManager().getAllRecipesFor(RecipeInit.ELDRIN_ALTAR_TYPE.get()).stream().filter(r -> r.value().getResultItem().getItem() == output).map(r -> r.value()).findFirst();
                        if (altar_recipe.isPresent()) {
                            requiredTier = altar_recipe.get().getTier();
                        }
                    }
                    if (requiredTier <= p.getTier()) continue;
                    offer.setToOutOfStock();
                }
            });
        }
    }

    public boolean isAffectedByPotions() {
        return false;
    }

    public boolean isPushable() {
        return false;
    }

    public boolean canChangeDimensions() {
        return false;
    }

    public boolean canBeLeashed(Player player) {
        return false;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> {
            if (!((Boolean)this.entityData.get(DESPAWNING)).booleanValue()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.idle"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.model.slam"));
        })});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    static {
        DESPAWNING = SynchedEntityData.defineId(Broker.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
        DESPAWNING_HOSTILE = SynchedEntityData.defineId(Broker.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    }
}


