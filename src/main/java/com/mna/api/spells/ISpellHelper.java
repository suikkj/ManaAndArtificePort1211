/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.registries.IForgeRegistry
 */
package com.mna.api.spells;

import com.mna.MnaRegistry;
import com.mna.api.spells.SpellCastingResult;
import com.mna.api.spells.adjusters.SpellAdjustingContext;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface ISpellHelper {
    public SpellCastingResult playerCast(ItemStack var1, Player var2, InteractionHand var3, boolean var4, boolean var5);

    @Deprecated
    default public SpellCastingResult playerCast(ItemStack stack, Player caster, InteractionHand hand, boolean consumeMana) {
        return this.playerCast(stack, caster, hand, consumeMana, false);
    }

    public SpellCastingResult affect(ItemStack var1, ISpellDefinition var2, Level var3, SpellSource var4, @Nullable SpellTarget var5);

    public SpellCastingResult affect(ItemStack var1, ISpellDefinition var2, Level var3, SpellSource var4, @Nullable SpellTarget var5, @Nullable SpellContext var6);

    public SpellCastingResult affect(ItemStack var1, ISpellDefinition var2, Level var3, SpellSource var4);

    public boolean containsSpell(ItemStack var1);

    public ISpellDefinition parseSpellDefinition(ItemStack var1);

    public ISpellDefinition parseSpellDefinition(CompoundTag var1);

    public ISpellDefinition parseSpellDefinition(ItemStack var1, @Nullable Player var2);

    public void writeSpellDefinition(ISpellDefinition var1, ItemStack var2);

    public ISpellDefinition createSpell(Shape var1, SpellEffect var2, Modifier ... var3);

    @Deprecated
    public void registerSpellAdjuster(Predicate<SpellAdjustingContext> var1, BiConsumer<ISpellDefinition, LivingEntity> var2);

    public void registerSpellAdjuster(Predicate<SpellAdjustingContext> var1, Consumer<SpellAdjustingContext> var2);

    public void registerSpellCastingItem(Item var1);

    public MnaRegistry<Shape> getShapeRegistry();

    public MnaRegistry<SpellEffect> getComponentRegistry();

    public MnaRegistry<Modifier> getModifierRegistry();

    public boolean reflectSpell(Level var1, @Nullable LivingEntity var2, ISpellDefinition var3, SpellSource var4, Vec3 var5, Vec3 var6, boolean var7);

    public void spawnSpellVFX(Level var1, Vec3 var2, Vec3 var3, SpellSource var4, SpellEffect var5);

    public void setCooldown(ItemStack var1, Player var2, ISpellDefinition var3);
}


