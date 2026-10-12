/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Position
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 */
package com.mna.spells.components;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.blocks.BlockInit;
import com.mna.blocks.ritual.ChalkRuneBlock;
import com.mna.blocks.sorcery.SpectralCraftingTableBlock;
import com.mna.gui.containers.block.ContainerSpectralCraftingTable;
import java.lang.invoke.LambdaMetafactory;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class ComponentSpectralCraftingTable
extends SpellEffect {
    public ComponentSpectralCraftingTable(ResourceLocation guiIcon) {
        super(guiIcon, new AttributeValuePair[0]);
    }

    @Override
    public int requiredXPForRote() {
        return 200;
    }

    @Override
    public ComponentApplicationResult ApplyEffect(SpellSource source, SpellTarget target, IModifiedSpellPart<SpellEffect> modificationData, SpellContext context) {
        if (context.hasEntityBeenAffected(this, (Entity)source.getCaster())) {
            return ComponentApplicationResult.FAIL;
        }
        context.addAffectedEntity(this, (Entity)source.getCaster());
        if (target.isBlock()) {
            ServerLevel world = context.getServerLevel();
            BlockPos targetBlock = target.getBlock();
            Direction targetBlockFace = target.getBlockFace(this);
            return ComponentApplicationResult.fromBoolean(this.tryPlaceBlock((Player)source.getPlayer(), (ServerLevel)world, (Block)((Block)BlockInit.SPECTRAL_CRAFTING_TABLE.get()), (BlockPos)targetBlock, (Direction)targetBlockFace, (boolean)true, (pos, state) -> ComponentSpectralCraftingTable.lambda$ApplyEffect$0(world, source, pos, state)).success);
        }
        if (target.isEntity() && source.isPlayerCaster() && target.getEntity() instanceof Player) {
            ((Player)target.getEntity()).openMenu(this.getContainer(context.getServerLevel().getBlockState(source.getPlayer().blockPosition()), (Level)context.getServerLevel(), source.getPlayer().blockPosition()));
            return ComponentApplicationResult.SUCCESS;
        }
        return ComponentApplicationResult.FAIL;
    }

    public MenuProvider getContainer(BlockState state, Level worldIn, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> new ContainerSpectralCraftingTable(id, inventory, ContainerLevelAccess.create((Level)worldIn, (BlockPos)pos)), SpectralCraftingTableBlock.CONTAINER_NAME);
    }

    @Override
    public void SpawnParticles(Level world, Vec3 impact_position, Vec3 normal, int age, LivingEntity caster, ISpellDefinition recipe) {
        if (age > 1) {
            return;
        }
        BlockPos imp = BlockPos.containing((Position)impact_position);
        for (int i = 0; i < 50; ++i) {
            world.addParticle((ParticleOptions)recipe.colorParticle(new MAParticleType((ParticleType<MAParticleType>)((ParticleType)ParticleInit.BLUE_SPARKLE_GRAVITY.get())), (Entity)caster), (double)imp.getX() + 0.5, (double)imp.getY() + 0.5, (double)imp.getZ() + 0.5, -0.25 + Math.random() * 0.5, Math.random() * 0.25, -0.25 + Math.random() * 0.5);
        }
    }

    @Override
    public boolean targetsBlocks() {
        return true;
    }

    @Override
    public boolean canBeChanneled() {
        return false;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.ARCANE;
    }

    @Override
    public float initialComplexity() {
        return 5.0f;
    }

    @Override
    public SpellPartTags getUseTag() {
        return SpellPartTags.UTILITY;
    }

    private static /* synthetic */ BlockState lambda$ApplyEffect$0(ServerLevel world, SpellSource source, BlockPos pos, BlockState state) {
        BlockState against = world.getBlockState(pos);
        if (against.getBlock() == BlockInit.CHALK_RUNE.get() && ((Boolean)against.getValue((Property)ChalkRuneBlock.METAL)).booleanValue() && source.isPlayerCaster()) {
            state = (BlockState)state.setValue(SpectralCraftingTableBlock.PERMANENT, Boolean.valueOf(true));
        }
        return state;
    }
}

