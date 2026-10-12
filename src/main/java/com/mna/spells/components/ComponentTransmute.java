 
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.entities.construct.ConstructCapability;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.api.entities.construct.IConstructConstruction;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.api.tools.MATags;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.entities.constructs.animated.Construct;
import com.mna.recipes.manaweaving.TransmutationRecipe;
import com.mna.recipes.manaweaving.TransmutationRecipeSerializer;
import com.mna.tools.BlockUtils;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mna.api.tools.MNACap;

public class ComponentTransmute
extends SpellEffect {
    public ComponentTransmute(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (source.getCaster() == null) {
            return ComponentApplicationResult.FAIL;
        }
        if (target.isBlock() && context.getServerLevel().isLoaded(target.getBlock())) {
            BlockState state = context.getServerLevel().getBlockState(target.getBlock());
            Block block = state.getBlock();
            List recipes = TransmutationRecipeSerializer.ALL_RECIPES.values().stream().filter(r -> MATags.isBlockIn(block, r.getTargetBlock()) || r.getTargetBlock().equals(BuiltInRegistries.BLOCK.getKey(block))).collect(Collectors.toList());
            if (recipes.size() == 0) {
                return ComponentApplicationResult.SUCCESS;
            }
            TransmutationRecipe recipe = (TransmutationRecipe)recipes.get(0);
            if (!source.isPlayerCaster()) {
                Construct construct;
                IConstructConstruction data;
                ConstructMaterial bestMat;
                if (recipe.getFactionRequirement() != null) {
                    return ComponentApplicationResult.FAIL;
                }
                int armTier = 0;
                LivingEntity livingEntity = source.getCaster();
                if (livingEntity instanceof Construct && (bestMat = (data = (construct = (Construct)livingEntity).getConstructData()).getHighestMatieralForCapability(ConstructCapability.CAST_SPELL)) != ConstructMaterial.UNKNOWN) {
                    armTier = bestMat.getCastingTierEquivalent();
                }
                if (recipe.getTier() > armTier) {
                    return ComponentApplicationResult.FAIL;
                }
            }
            if (source.isPlayerCaster()) {
                IPlayerProgression progression = MNACap.get((IPlayerProgression)source.getPlayer(), PlayerProgressionProvider.PROGRESSION).orElse(null);
                if (progression != null) {
                    if (progression.getTier() < recipe.getTier()) {
                        source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:components/transmute.low_tier"));
                        return ComponentApplicationResult.FAIL;
                    }
                    if (recipe.getFactionRequirement() != null && progression.getAlliedFaction() != recipe.getFactionRequirement()) {
                        source.getPlayer().sendSystemMessage((Component)Component.translatable((String)"mna:components/transmute.wrong_faction"));
                        return ComponentApplicationResult.FAIL;
                    }
                } else if (recipe.getTier() > 0 || recipe.getFactionRequirement() != null) {
                    return ComponentApplicationResult.FAIL;
                }
            }
            if (recipe != null && BlockUtils.destroyBlock(source.getCaster(), (Level)context.getServerLevel(), target.getBlock(), false, (Tier)Tiers.IRON)) {
                LootTable lootTable;
                Block replace;
                if (recipe.hasReplaceBlock() && (replace = (Block)BuiltInRegistries.BLOCK.get(recipe.getReplaceBlock())) != null) {
                    context.getServerLevel().setBlock(target.getBlock(), replace.defaultBlockState(), 3);
                }
                if (recipe.hasLootTable() && (lootTable = context.getServerLevel().getServer().getLootData().getLootTable(recipe.getLootTable())) != null) {
                    LootParams lootparams = new LootParams.Builder(context.getServerLevel()).withParameter(LootContextParams.THIS_ENTITY, source.getCaster()).withParameter(LootContextParams.DAMAGE_SOURCE, source.getCaster().damageSources().magic()).withOptionalParameter(LootContextParams.TOOL, source.getCaster().getItemInHand(source.getHand())).withParameter(LootContextParams.ORIGIN, target.getPosition()).create(LootContextParamSets.ENTITY);
                    Vec3 isPos = Vec3.atCenterOf((Vec3i)target.getBlock());
                    lootTable.getRandomItems(lootparams).forEach(is -> {
                        ItemEntity item = new ItemEntity((Level)context.getServerLevel(), isPos.x, isPos.y, isPos.z, is);
                        context.getServerLevel().addFreshEntity((Entity)item);
                    });
                }
                return ComponentApplicationResult.SUCCESS;
            }
        }
        return ComponentApplicationResult.FAIL;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ARCANE;
    }

    @Override
    public SoundEvent SoundEffect() {
        return SFX.Spell.Impact.Single.ARCANE;
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age < 5) {
            Vec3 rotationOffset = new Vec3(1.5, 0.0, 0.0);
            BlockPos bp = BlockPos.containing((Position)impact_position).offset(0, -1, 0);
            for (int angle = 0; angle < 360; angle += 30) {
                Vec3 point = rotationOffset.yRot((float)((double)angle * Math.PI / 180.0));
                world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.ARCANE_MAGELIGHT.get())), (Entity)caster), (double)((float)bp.getX() + 0.5f) + point.x, (double)bp.getY(), (double)((float)bp.getZ() + 0.5f) + point.z, (double)((float)bp.getX() + 0.5f), (double)(bp.getY() + 2), (double)((float)bp.getZ() + 0.5f));
            }
        }
    }

    @Override
    public float initialComplexity() {
        return 20.0f;
    }

    @Override
    public boolean targetsBlocks() {
        return true;
    }

    @Override
    public boolean targetsEntities() {
        return false;
    }

    @Override
    public int requiredXPForRote() {
        return 100;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }
}

