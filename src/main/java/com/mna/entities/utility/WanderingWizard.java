 
package com.mna.entities.utility;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import com.mna.api.entities.ai.CastSpellAtTargetGoal;
import com.mna.api.entities.ai.CastSpellOnSelfGoal;
import com.mna.api.events.WanderingWizardSelectingTradesEvent;
import com.mna.api.sound.SFX;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.collections.Components;
import com.mna.api.spells.collections.Shapes;
import com.mna.api.tools.MATags;
import com.mna.entities.EntityInit;
import com.mna.entities.IAnimPacketSync;
import com.mna.entities.ai.AIPredicateWrapperGoal;
import com.mna.entities.ai.PlayIdleAnimationGoal;
import com.mna.gui.containers.entity.ContainerWanderingWizard;
import com.mna.items.ItemInit;
import com.mna.items.manaweaving.ManaweavingPatternPaper;
import com.mna.items.ritual.ThaumaturgicLink;
import com.mna.network.ServerMessageDispatcher;
import com.mna.recipes.manaweaving.ManaweavingPattern;
import com.mna.recipes.manaweaving.ManaweavingPatternSerializer;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.BiomeUtils;
import com.mna.tools.StructureUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import org.apache.commons.lang3.mutable.MutableInt;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.mna.api.tools.MNAItemData;

public class WanderingWizard
extends WanderingTrader
implements GeoEntity,
AnimationController.CustomKeyframeHandler<WanderingWizard>,
IAnimPacketSync<WanderingWizard> {
    private AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);
    private boolean plantedStaff = false;
    private boolean renderScroll = false;
    private int castTimer = 0;
    private int castCooldown = 0;
    private int idleCooldown = this.getIdleCooldown();
    private State state = State.IDLE;
    private String idleAnim = "";
    private ItemStack staffStack;

    public WanderingWizard(EntityType<? extends WanderingTrader> type, Level worldIn) {
        super(type, worldIn);
        this.setNoAi(true);
        this.staffStack = MATags.getRandomItemFrom(MATags.Items.STAVES);
    }

    public WanderingWizard(Level worldIn) {
        this((EntityType<? extends WanderingTrader>)((EntityType)EntityInit.WANDERING_WIZARD.get()), worldIn);
    }

    private ItemStack createInvisibilitySpell() {
        ItemStack invisSpell = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe invis = new SpellRecipe();
        invis.setShape(Shapes.SELF);
        invis.addComponent(Components.INVISIBILITY);
        invis.changeComponentAttributeValue(0, Attribute.DURATION, 120.0f);
        invis.writeToNBT(invisSpell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        return invisSpell;
    }

    private ItemStack createRandomAttackSpell() {
        ItemStack attackSpell = new ItemStack((ItemLike)ItemInit.SPELL.get());
        SpellRecipe boom = new SpellRecipe();
        boom.setShape(Shapes.BOLT);
        if (Math.random() < 0.5) {
            boom.addComponent(Components.FROST_DAMAGE);
            boom.changeComponentAttributeValue(0, Attribute.DAMAGE, 10.0f);
            boom.addComponent(Components.SLOW);
            boom.changeComponentAttributeValue(1, Attribute.MAGNITUDE, 2.0f);
            boom.changeComponentAttributeValue(1, Attribute.DURATION, 5.0f);
        } else {
            boom.addComponent(Components.MAGIC_DAMAGE);
            boom.changeComponentAttributeValue(0, Attribute.DAMAGE, 10.0f);
            boom.addComponent(Components.FLING);
            boom.changeComponentAttributeValue(1, Attribute.SPEED, 2.0f);
        }
        boom.writeToNBT(attackSpell.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        return attackSpell;
    }

    private static boolean shouldCastInvisibility(WanderingWizard entity) {
        return entity.level().isNight() && !entity.isInvisible();
    }

    private static void onSelfCastStart(WanderingWizard entity) {
        entity.setState(State.CAST_SELF);
    }

    private static void onCastEnd(WanderingWizard entity) {
        entity.setState(State.IDLE);
        entity.castTimer = 0;
        entity.castCooldown = 30;
    }

    private static void onTargetCastStart(WanderingWizard entity) {
    }

    private static boolean onTargetCasting(WanderingWizard entity) {
        entity.setState(State.CAST_TARGET);
        if (entity.castTimer++ < 5) {
            return false;
        }
        entity.setState(State.IDLE);
        return true;
    }

    private static boolean isSpellOnCooldown(WanderingWizard entity) {
        return entity.castCooldown == 0;
    }

    private static boolean canPlayIdleAnim(WanderingWizard entity) {
        return entity.state() == State.IDLE && entity.idleCooldown == 0;
    }

    private static void onIdleAnimStart(WanderingWizard entity, String anim) {
        entity.idleAnim = anim;
        entity.setState(State.IDLE_ANIM);
    }

    private static void onIdleAnimStop(WanderingWizard entity) {
        entity.idleAnim = "";
        entity.idleCooldown = entity.getIdleCooldown();
        entity.plantedStaff = false;
        entity.renderScroll = false;
        entity.setState(State.IDLE);
    }

    private int getIdleCooldown() {
        return 400 + (int)(800.0 * Math.random());
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SFX.Entity.WanderingWizard.HIT;
    }

    protected SoundEvent getDeathSound() {
        return SFX.Entity.WanderingWizard.DEATH;
    }

    protected SoundEvent getAmbientSound() {
        return SFX.Entity.WanderingWizard.IDLE;
    }

    public SoundEvent getNotifyTradeSound() {
        return SFX.Entity.WanderingWizard.YES;
    }

    protected SoundEvent getTradeUpdatedSound(boolean pGetYesSound) {
        return pGetYesSound ? SFX.Entity.WanderingWizard.YES : SFX.Entity.WanderingWizard.NO;
    }

    public void tick() {
        super.tick();
        if (this.tickCount > 30) {
            this.setNoAi(false);
        }
        if (this.idleCooldown > 0) {
            --this.idleCooldown;
        }
        if (this.castCooldown > 0) {
            --this.castCooldown;
        }
        if (this.level().isDay() && this.hasEffect(MobEffects.INVISIBILITY)) {
            this.removeEffect(MobEffects.INVISIBILITY);
        }
    }

    public boolean hurt(DamageSource source, float amount) {
        if (this.tickCount < 30) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        return super.interactAt(player, vec, hand);
    }

    public void openTradingScreen(Player player, Component displayName, int level) {
        MerchantOffers merchantoffers;
        int bufferCount = 10;
        player.openMenu((MenuProvider)new SimpleMenuProvider((id, playerInventory, player2) -> new ContainerWanderingWizard(id, playerInventory, (Merchant)this), displayName));
        if (player.containerMenu != null && !(merchantoffers = this.getOffers()).isEmpty()) {
            MutableInt count = new MutableInt(0);
            MutableInt totalCount = new MutableInt(0);
            MerchantOffers packetOffers = new MerchantOffers();
            merchantoffers.forEach(o -> {
                boolean isFinal;
                packetOffers.add(o);
                count.increment();
                totalCount.increment();
                boolean bl = isFinal = totalCount.getValue() >= merchantoffers.size();
                if (count.getValue() >= 10 || isFinal) {
                    ServerMessageDispatcher.sendWanderingWizardContainerMessage(((ServerPlayer)player).containerCounter, packetOffers, level, this.getVillagerXp(), this.showProgressBar(), this.canRestock(), isFinal, (ServerPlayer)player);
                    count.setValue(0);
                    packetOffers.clear();
                }
            });
        }
    }

    public static AttributeSupplier.Builder getGlobalAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    protected void addOffersFromItemListings(MerchantOffers givenMerchantOffers, VillagerTrades.ItemListing[] newTrades, int maxNumbers) {
        if (this.level().isClientSide()) {
            return;
        }
        givenMerchantOffers.clear();
        ServerLevel serverLevel = (ServerLevel)this.level();
        ItemStack costItem = new ItemStack((ItemLike)ItemInit.VINTEUM_INGOT.get());
        for (ManaweavingPattern pattern : ManaweavingPatternSerializer.ALL_RECIPES.values()) {
            ItemStack stack = new ItemStack((ItemLike)ItemInit.RECIPE_SCRAP_MANAWEAVING_PATTERN.get());
            ManaweavingPatternPaper.setRecipe(stack, pattern);
            MerchantOffer offer = new MerchantOffer(costItem.copy(), stack, 5, 0, 1.0f);
            givenMerchantOffers.add(offer);
        }
        StructureUtils.getAllStructures(serverLevel).forEach(structureFeature -> {
            if (structureFeature.is(MATags.Structures.BROKER_STRUCTURES)) {
                return;
            }
            ItemStack stack = new ItemStack((ItemLike)ItemInit.THAUMATURGIC_LINK.get());
            ThaumaturgicLink.setStructureKey(serverLevel, stack, (Holder<Structure>)structureFeature);
            MerchantOffer offer = new MerchantOffer(costItem.copy(), stack, 1, 0, 1.0f);
            givenMerchantOffers.add(offer);
        });
        BiomeUtils.getAllBiomes(serverLevel).forEach(biome -> {
            if (biome.is(MATags.Biomes.BROKER_BIOMES)) {
                return;
            }
            ItemStack stack = new ItemStack((ItemLike)ItemInit.THAUMATURGIC_LINK.get());
            ThaumaturgicLink.setBiomeKey(serverLevel, stack, (Holder<Biome>)biome);
            MerchantOffer offer = new MerchantOffer(costItem.copy(), stack, 1, 0, 1.0f);
            givenMerchantOffers.add(offer);
        });
        givenMerchantOffers.add(new MerchantOffer(costItem.copy(), new ItemStack((ItemLike)Items.MAP), 1, 0, 1.0f));
        givenMerchantOffers.add(new MerchantOffer(costItem.copy(), new ItemStack((ItemLike)Items.COMPASS), 1, 0, 1.0f));
        NeoForge.EVENT_BUS.post((Event)new WanderingWizardSelectingTradesEvent((Merchant)this, givenMerchantOffers, newTrades, maxNumbers));
    }

    @Override
    public CompoundTag getPacketData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("state", this.state().ordinal());
        tag.putString("idleAnim", this.idleAnim);
        tag.put("staff", (Tag)MNAItemData.save(this.staffStack, new CompoundTag()));
        return tag;
    }

    @Override
    public void handlePacketData(CompoundTag nbt) {
        this.setState(State.values()[nbt.getInt("state")]);
        this.idleAnim = nbt.getString("idleAnim");
        this.staffStack = ItemStack.parseOptional(com.mna.api.tools.MNAItemData.provider(), (CompoundTag)nbt.getCompound("staff")).orElse(ItemStack.EMPTY);
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        AnimationController controller = new AnimationController((GeoAnimatable)this, state -> {
            switch (this.state) {
                case CAST_SELF: {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.WanderingWizard.cast_self"));
                }
                case CAST_TARGET: {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.WanderingWizard.cast_target"));
                }
                case IDLE_ANIM: {
                    return state.setAndContinue(RawAnimation.begin().thenPlayAndHold(this.idleAnim));
                }
                case IDLE: {
                    if (this.getDeltaMovement().add(0.0, -this.getDeltaMovement().y, 0.0).length() > (double)0.02f) {
                        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.WanderingWizard.walk"));
                    }
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.WanderingWizard.idle"));
                }
            }
            return PlayState.CONTINUE;
        });
        controller.setCustomInstructionKeyframeHandler((AnimationController.CustomKeyframeHandler)this);
        registrar.add(new AnimationController[]{controller});
    }

    public boolean renderPlantedStaff() {
        return this.plantedStaff;
    }

    public boolean renderScroll() {
        return this.renderScroll;
    }

    public ItemStack staff() {
        return this.staffStack;
    }

    public void handle(CustomInstructionKeyframeEvent<WanderingWizard> event) {
        switch (event.getKeyframeData().getInstructions()) {
            case "plant_staff;": {
                this.plantedStaff = true;
                break;
            }
            case "grab_scroll;": {
                this.renderScroll = true;
                break;
            }
            case "unplant_staff;": {
                this.plantedStaff = false;
                break;
            }
            case "ungrab_scroll;": {
                this.renderScroll = false;
            }
        }
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(0, new CastSpellOnSelfGoal<WanderingWizard>(this, this.createInvisibilitySpell(), WanderingWizard::shouldCastInvisibility, WanderingWizard::onSelfCastStart, WanderingWizard::onCastEnd, 34));
        this.goalSelector.addGoal(1, (Goal)new TradeWithPlayerGoal((AbstractVillager)this));
        this.goalSelector.addGoal(1, (Goal)new LookAtTradingPlayerGoal((AbstractVillager)this));
        this.goalSelector.addGoal(4, (Goal)new MoveTowardsRestrictionGoal((PathfinderMob)this, 0.35));
        this.goalSelector.addGoal(6, new CastSpellAtTargetGoal<WanderingWizard>(this, this.createRandomAttackSpell(), 0.35, 40, 10.0f).setAttackCooldown(60).setStopCallback(WanderingWizard::onCastEnd).setStartCallback(WanderingWizard::onTargetCastStart).setResetCallback(WanderingWizard::onCastEnd).setPrecastCallback(WanderingWizard::onTargetCasting).setCanUseCallback(WanderingWizard::isSpellOnCooldown));
        this.goalSelector.addGoal(8, new AIPredicateWrapperGoal<WanderingWizard>(this, (Goal)new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 0.35)).executionPredicate(ww -> ww.state == State.IDLE));
        this.goalSelector.addGoal(9, (Goal)new InteractGoal((Mob)this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, (Goal)new LookAtPlayerGoal((Mob)this, Mob.class, 8.0f));
        this.goalSelector.addGoal(11, new PlayIdleAnimationGoal<WanderingWizard>(this, WanderingWizard::canPlayIdleAnim, WanderingWizard::onIdleAnimStart, WanderingWizard::onIdleAnimStop, new Pair("animation.WanderingWizard.read_scroll", 187)));
        this.targetSelector.addGoal(2, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Mob.class, 5, false, false, p_234199_0_ -> p_234199_0_ instanceof Enemy && !(p_234199_0_ instanceof Creeper)));
    }

    public State state() {
        return this.state;
    }

    public void setState(State state) {
        this.state = state;
        if (!this.level().isClientSide()) {
            ServerMessageDispatcher.sendEntityStateMessage(this);
        }
    }

    static enum State {
        IDLE,
        IDLE_ANIM,
        CAST_SELF,
        CAST_TARGET;

    }
}


