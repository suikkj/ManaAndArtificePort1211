 
package com.mna.api.spells.parts;

import com.google.common.collect.ImmutableList;
import com.mna.ManaAndArtifice;
import com.mna.api.ManaAndArtificeMod;
import com.mna.api.affinity.Affinity;
import com.mna.api.faction.IFaction;
import com.mna.api.recipes.IMARecipe;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellCraftingContext;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiable;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.base.SpellBlacklistResult;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.common.util.TriPredicate;

public abstract class SpellEffect
implements IModifiable<SpellEffect>,
ISpellComponent {
    private final ResourceLocation guiIcon;
    private ResourceLocation _registryNameCached;
    private final ImmutableList<AttributeValuePair> modifiableAttributes;
    private Integer tier;
    private ArrayList<SpellReagent> _reagents = null;

    public SpellEffect(ResourceLocation guiIcon, AttributeValuePair ... attributeValuePairs) {
        ArrayList<AttributeValuePair> list = new ArrayList<AttributeValuePair>(Arrays.asList(attributeValuePairs));
        if (!list.stream().anyMatch(m -> m.getAttribute() == Attribute.DELAY)) {
            list.add(new AttributeValuePair(Attribute.DELAY, 0.0f, 0.0f, 3.0f, 0.1f, 0.5f));
        }
        this.modifiableAttributes = ImmutableList.copyOf(list);
        this.guiIcon = guiIcon;
    }

    @Override
    public void onRegistered() {
        this.initializeConfigs((AttributeValuePair[])this.modifiableAttributes.toArray((Object[])new AttributeValuePair[0]));
    }

    @Override
    public final ImmutableList<AttributeValuePair> getModifiableAttributes() {
        this.lookupAttributeConfig();
        return AttributeValuePair.deepCopy(this.modifiableAttributes);
    }

    public final void lookupAttributeConfig() {
        for (AttributeValuePair m : this.modifiableAttributes) {
            m.lookupConfig(this);
        }
    }

    @Override
    public SpellBlacklistResult canBeCastAt(Level world, Vec3 position) {
        if (world.isClientSide) {
            return SpellBlacklistResult.ALLOWED;
        }
        try {
            Registry biomeRegistry = ((ServerLevel)world).registryAccess().registryOrThrow(Registries.BIOME);
            BlockPos bp = BlockPos.containing((Position)position);
            Biome biome = (Biome)world.getBiome(bp).value();
            if (ManaAndArtificeMod.getConfigHelper().isDimensionBlacklisted(this, world.dimension().location())) {
                return SpellBlacklistResult.DIMENSION_BLOCKED;
            }
            if (ManaAndArtificeMod.getConfigHelper().isBiomeBlacklisted(this, biomeRegistry.getKey(biome))) {
                return SpellBlacklistResult.BIOME_BLOCKED;
            }
        }
        catch (Throwable t) {
            ManaAndArtifice.LOGGER.error("Failed to resolve biome at " + position.toString());
        }
        return SpellBlacklistResult.ALLOWED;
    }

    public abstract ComponentApplicationResult ApplyEffect(SpellSource var1, SpellTarget var2, IModifiedSpellPart<SpellEffect> var3, SpellContext var4);

    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.ARCANE;
    }

    @OnlyIn(value=Dist.CLIENT)
    public void SpawnParticles(Level world, Vec3 impact_position, int age, @Nullable Player caster, @Nullable ISpellDefinition recipe) {
        this.SpawnParticles(world, impact_position, Vec3.ZERO, age, (LivingEntity)caster, recipe);
    }

    @OnlyIn(value=Dist.CLIENT)
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, @Nullable LivingEntity caster, @Nullable ISpellDefinition recipe) {
    }

    public abstract Affinity getAffinity();

    @Override
    public final ResourceLocation getGuiIcon() {
        return this.guiIcon;
    }

    @Override
    public ResourceLocation getRegistryName() {
        if (this._registryNameCached == null) {
            this._registryNameCached = ManaAndArtificeMod.getComponentRegistry().getKey(this);
        }
        return this._registryNameCached;
    }

    @Override
    public boolean isCraftable(SpellCraftingContext context) {
        return true;
    }

    public abstract float initialComplexity();

    public int baselineCooldown() {
        return 0;
    }

    public boolean canBeChanneled() {
        return true;
    }

    public boolean targetsEntities() {
        return true;
    }

    public boolean targetsBlocks() {
        return true;
    }

    public boolean isHellfireBoosted(Attribute attr) {
        return true;
    }

    public Direction defaultBlockFace() {
        return Direction.UP;
    }

    @Override
    public boolean isUseableByPlayers() {
        return true;
    }

    public boolean applyAtChanneledEntityPos(boolean clientSide) {
        return false;
    }

    @Nullable
    public List<SpellReagent> getRequiredReagents(@Nullable Player caster, @Nullable InteractionHand hand) {
        if (this._reagents == null) {
            return null;
        }
        return this._reagents.stream().filter(r -> !r.isIgnoredBy(caster)).toList();
    }

    public void addReagent(ItemStack reagentStack, boolean compareNBT, boolean ignoreDurability, boolean consume, IFaction ... ignoredBy) {
        if (this._reagents == null) {
            this._reagents = new ArrayList();
        }
        this._reagents.add(new SpellReagent(this, reagentStack, compareNBT, ignoreDurability, consume, false, ignoredBy));
    }

    public void addReagent(ItemStack reagentStack, IFaction ... ignoredBy) {
        this.addReagent(reagentStack, false, false, true, ignoredBy);
    }

    public void addOptionalReagent(ItemStack reagentStack, boolean compareNBT, boolean ignoreDurability, boolean consume, IFaction ... ignoredBy) {
        if (this._reagents == null) {
            this._reagents = new ArrayList();
        }
        this._reagents.add(new SpellReagent(this, reagentStack, compareNBT, ignoreDurability, consume, true, ignoredBy));
    }

    public void addOptionalReagent(ItemStack reagentStack, IFaction ... ignoredBy) {
        this.addOptionalReagent(reagentStack, false, false, true, ignoredBy);
    }

    public boolean autoConsumeReagents() {
        return true;
    }

    public void addReagentTooltip(Player player, @Nullable InteractionHand hand, List<Component> tooltip, SpellReagent reagent) {
        MutableComponent c = Component.literal((String)String.format("%d x ", reagent.getReagentStack().getCount())).append(reagent.getReagentStack().getHoverName());
        if (reagent.getOptional()) {
            c.append((Component)Component.literal((String)" (")).append((Component)Component.translatable((String)"item.mna.spell.tooltip.optional")).append((Component)Component.literal((String)")"));
        }
        tooltip.add((Component)c);
    }

    public BlockPlaceResult tryPlaceBlock(@Nullable Player player, ServerLevel world, Block block, BlockPos pos, Direction face, boolean checkOtherDirections, @Nullable TriPredicate<Level, BlockPos, BlockPos> additionalPredicate, @Nullable BiFunction<BlockPos, BlockState, BlockState> stateAdjuster) {
        ArrayList<Object> dirs = new ArrayList<Object>();
        dirs.add(null);
        dirs.add(face);
        if (checkOtherDirections) {
            for (Direction d : Direction.values()) {
                if (d == face) continue;
                dirs.add(d);
            }
        }
        for (Direction direction : (java.util.List<Direction>)dirs) {
            BlockState placementState;
            BlockPlaceContext bpc;
            Direction operatingDirection;
            BlockPos targetPos = direction == null ? new BlockPos((Vec3i)pos) : pos.offset(direction.getNormal());
            Direction direction2 = operatingDirection = direction == null ? Direction.UP : direction;
            if (additionalPredicate != null && !additionalPredicate.test(world, pos, targetPos)) continue;
            BlockState existing = world.getBlockState(targetPos);
            Player bpcPlayer = player;
            if (bpcPlayer == null) {
                bpcPlayer = FakePlayerFactory.getMinecraft((ServerLevel)world);
                bpcPlayer.setPos(Vec3.atCenterOf((Vec3i)targetPos));
                bpcPlayer.lookAt(EntityAnchorArgument.Anchor.FEET, Vec3.atCenterOf((Vec3i)targetPos));
            }
            if (!existing.canBeReplaced(bpc = new BlockPlaceContext(bpcPlayer, InteractionHand.MAIN_HAND, new ItemStack((ItemLike)block), new BlockHitResult(Vec3.atCenterOf((Vec3i)targetPos), operatingDirection, targetPos, false))) || (placementState = block.getStateForPlacement(bpc)) == null || !placementState.canSurvive((LevelReader)world, targetPos)) continue;
            if (true) {
                if (stateAdjuster != null) {
                    placementState = stateAdjuster.apply(targetPos, placementState);
                }
                world.setBlockAndUpdate(targetPos, placementState);
            }
            return new BlockPlaceResult(true, targetPos, operatingDirection);
        }
        return new BlockPlaceResult(false, null, null);
    }

    public BlockPlaceResult tryPlaceBlock(@Nullable Player player, ServerLevel world, Block block, BlockPos pos, Direction face, boolean checkOtherDirections, @Nullable TriPredicate<Level, BlockPos, BlockPos> additionalPredicate) {
        return this.tryPlaceBlock(player, world, block, pos, face, checkOtherDirections, additionalPredicate, null);
    }

    public BlockPlaceResult tryPlaceBlock(@Nullable Player player, ServerLevel world, Block block, BlockPos pos, Direction face, boolean checkOtherDirections) {
        return this.tryPlaceBlock(player, world, block, pos, face, checkOtherDirections, null, null);
    }

    public BlockPlaceResult tryPlaceBlock(@Nullable Player player, ServerLevel world, Block block, BlockPos pos, Direction face, boolean checkOtherDirections, @Nullable BiFunction<BlockPos, BlockState, BlockState> stateAdjuster) {
        return this.tryPlaceBlock(player, world, block, pos, face, checkOtherDirections, null, stateAdjuster);
    }

    @Override
    public int getTier(Level world) {
        if (this.tier == null) {
            if (this.isSilverSpell()) {
                this.tier = 5;
            } else {
                Optional recipe = world.getRecipeManager().byKey(this.getRegistryName());
                this.tier = recipe.isPresent() && recipe.get() instanceof IMARecipe ? Integer.valueOf(((IMARecipe)recipe.get()).getTier()) : Integer.valueOf(0);
                if (this.tier < 0) {
                    this.tier = 0;
                }
            }
        }
        return this.tier;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.NEUTRAL;
    }

    public List<Affinity> getValidTinkerAffinities() {
        return Arrays.asList(Affinity.ARCANE, Affinity.EARTH, Affinity.ENDER, Affinity.FIRE, Affinity.WATER, Affinity.WIND, Affinity.ICE, Affinity.LIGHTNING);
    }

    public float getSoundVolume() {
        return 0.15f;
    }

    protected boolean casterTeamCheck(SpellSource source, SpellTarget target) {
        if (source.getCaster() == target.getEntity()) {
            return true;
        }
        if (!target.isEntity()) {
            return false;
        }
        if (source.isPlayerCaster() && target.getLivingEntity() instanceof Player) {
            Player caster = source.getPlayer();
            Player tgt = (Player)target.getLivingEntity();
            return ManaAndArtificeMod.getSummonHelper().isEntityFriendly((Entity)tgt, (LivingEntity)caster);
        }
        return true;
    }

    public class BlockPlaceResult {
        public final boolean success;
        @Nullable
        public final BlockPos position;
        @Nullable
        public final Direction placedAgainst;

        public BlockPlaceResult(boolean success, BlockPos position, Direction placedAgainst) {
            this.success = success;
            this.position = position;
            this.placedAgainst = placedAgainst;
        }
    }

    public static class PhantomComponent
    extends SpellEffect {
        public static PhantomComponent instance = new PhantomComponent();
        private static final ResourceLocation _default = ResourceLocation.parse("mna:component_default");

        public PhantomComponent() {
            super(_default, new AttributeValuePair[0]);
        }

        @Override
        public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
            return ComponentApplicationResult.SUCCESS;
        }

        @Override
        public Affinity getAffinity() {
            return Affinity.UNKNOWN;
        }

        @Override
        public float initialComplexity() {
            return 0.0f;
        }

        @Override
        public int requiredXPForRote() {
            return -1;
        }

        @Override
        public SpellPartTags getUseTag() {
            return SpellPartTags.NEUTRAL;
        }
    }
}


