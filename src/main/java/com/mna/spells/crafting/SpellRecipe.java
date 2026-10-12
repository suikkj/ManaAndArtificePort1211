 
package com.mna.spells.crafting;

import net.minecraft.core.component.DataComponents;
import com.mna.ManaAndArtifice;
import com.mna.Registries;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.capabilities.IPlayerRoteSpells;
import com.mna.api.events.SpellReagentsEvent;
import com.mna.api.faction.IFaction;
import com.mna.api.items.IFactionSpecific;
import com.mna.api.particles.MAParticleType;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellCastingResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.SpellReagent;
import com.mna.api.spells.SpellValidityResult;
import com.mna.api.spells.adjusters.SpellCastStage;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellComponent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Modifier;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.rote.PlayerRoteSpellsProvider;
import com.mna.items.ItemInit;
import com.mna.items.sorcery.MagicStaff;
import com.mna.recipes.ItemAndPatternRecipeHelper;
import com.mna.recipes.RecipeInit;
import com.mna.recipes.spells.ComponentRecipe;
import com.mna.recipes.spells.ModifierRecipe;
import com.mna.recipes.spells.ShapeRecipe;
import com.mna.spells.NameProcessors;
import com.mna.spells.SpellCaster;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.mna.tools.math.MathUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import com.mna.api.tools.MNACap;
import net.minecraft.world.item.component.CustomData;
import com.mna.api.tools.MNAItemData;

public class SpellRecipe
implements ISpellDefinition,
IFactionSpecific {
    public static final int MAX_COMPONENTS = 5;
    public static final int MAX_MODIFIERS = 3;
    public static final String SPELL_COMPOUND_TAG = "spell";
    public static final String SPELL_COLOR_TAG = "spellColor";
    private ModifiedSpellPart<Shape> shape;
    private ArrayList<ModifiedSpellPart<SpellEffect>> components;
    private Modifier[] modifiers;
    private Affinity overrideAffinity = Affinity.UNKNOWN;
    private float complexity;
    private int particleColorOverride = -1;
    private float calculatedManaCost;
    private float overrideManaCost = -1.0f;
    private boolean isMysterious = true;

    public SpellRecipe() {
        this.modifiers = new Modifier[3];
        this.components = new ArrayList(5);
    }

    public SpellRecipe(Shape shape, SpellEffect component) {
        this.modifiers = new Modifier[3];
        this.components = new ArrayList(5);
        this.setShape(shape);
        this.addComponent(component);
    }

    public SpellRecipe setShape(Shape shape) {
        this.shape = shape != null ? new ModifiedSpellPart<Shape>(shape) : null;
        this.recalculateSpellNumbers();
        return this;
    }

    public SpellRecipe addComponent(SpellEffect component) {
        if (this.components.size() < 5 && !this.components.stream().anyMatch(c -> c.getPart() == component)) {
            this.components.add(new ModifiedSpellPart<SpellEffect>(component));
            this.recalculateSpellNumbers();
        }
        return this;
    }

    public SpellRecipe setShapeModifier(Attribute modifier, float value) {
        IModifiedSpellPart shape = this.getShape();
        if (shape != null) {
            ((ModifiedSpellPart)shape).setValue(modifier, value);
        }
        return this;
    }

    public SpellRecipe setComponentModifier(int componentIndex, Attribute modifier, float value) {
        IModifiedSpellPart component = this.getComponent(componentIndex);
        if (component != null) {
            ((ModifiedSpellPart)component).setValue(modifier, value);
        }
        return this;
    }

    public void removeComponent(int index) {
        if (index < 0 || index >= this.components.size()) {
            return;
        }
        this.components.remove(index);
        this.recalculateSpellNumbers();
    }

    public void setModifier(Modifier modifier, int index) {
        if (index < 0 || index >= 3) {
            return;
        }
        if (modifier == null && this.modifiers[index] != null) {
            for (Attribute attribute : this.modifiers[index].getModifiedAttributes()) {
                if (this.shape != null) {
                    this.shape.resetValueToDefault(attribute);
                }
                for (ModifiedSpellPart<SpellEffect> component : this.components) {
                    if (component == null) continue;
                    component.resetValueToDefault(attribute);
                }
            }
        }
        this.modifiers[index] = modifier;
        this.recalculateSpellNumbers();
    }

    public boolean addModifier(Modifier modifier) {
        for (int i = 0; i < 3; ++i) {
            if (this.modifiers[i] != null) continue;
            this.modifiers[i] = modifier;
            return true;
        }
        return false;
    }

    private void recalculateSpellNumbers() {
        this.calculateComplexity();
        this.calculateManaCost();
    }

    public ModifiedSpellPart<Shape> getShape() {
        return this.shape;
    }

    @Nullable
    public ModifiedSpellPart<SpellEffect> getComponent(int index) {
        if (index < 0 || index >= this.components.size()) {
            return null;
        }
        return this.components.get(index);
    }

    @Override
    public int countComponents() {
        return this.components.size();
    }

    @Override
    public void clearComponents() {
        this.components.clear();
        this.recalculateSpellNumbers();
    }

    @Override
    public int findComponent(SpellEffect component) {
        OptionalInt index = IntStream.range(0, this.components.size()).filter(idx -> this.components.get(idx).getPart() == component).findFirst();
        return index.isPresent() ? index.getAsInt() : -1;
    }

    @Override
    public List<IModifiedSpellPart<SpellEffect>> getComponents() {
        return this.components.stream().map(c -> c).collect(Collectors.toList());
    }

    @Override
    public void iterateComponents(Consumer<IModifiedSpellPart<SpellEffect>> consumer) {
        for (ModifiedSpellPart<SpellEffect> component : this.components) {
            consumer.accept(component);
        }
    }

    @Override
    public Modifier getModifier(int index) {
        if (index < 0 || index >= 3) {
            return null;
        }
        return this.modifiers[index];
    }

    @Override
    public List<Modifier> getModifiers() {
        return Arrays.asList(this.modifiers).stream().filter(m -> m != null).collect(Collectors.toList());
    }

    public boolean isAttributeModifiable(Attribute attr) {
        for (Modifier m : this.modifiers) {
            if (m == null || !m.modifiesType(attr)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int countModifiers() {
        int c = 0;
        for (Modifier m : this.modifiers) {
            if (m == null) continue;
            ++c;
        }
        return c;
    }

    @Override
    public int getCooldown(@Nullable LivingEntity caster) {
        if (!this.isValid()) {
            return 0;
        }
        IPlayerRoteSpells roteCap = caster != null && caster instanceof Player ? (IPlayerRoteSpells)caster.getData(PlayerRoteSpellsProvider.ROTE.get()) : null;
        float complexityCooldown = this.getComplexity() / 4.0f;
        MutableFloat baseline = new MutableFloat((float)this.shape.getPart().baselineCooldown());
        MutableFloat componentMastery = new MutableFloat(0.0f);
        this.iterateComponents(c -> {
            float componentCD = ((SpellEffect)c.getPart()).baselineCooldown();
            baseline.add(componentCD);
            if (roteCap != null) {
                float mastery = roteCap.getMastery((ISpellComponent)c.getPart());
                componentMastery.add(mastery);
            }
        });
        float totalBaseline = baseline.getValue().floatValue() + complexityCooldown;
        float componentMasteryFactor = componentMastery.getValue().floatValue() * 0.625f / (float)this.getComponents().size();
        float shapeMasteryFactor = (roteCap != null ? roteCap.getMastery((ISpellComponent)((ModifiedSpellPart)this.getShape()).getPart()) : 0.0f) * 0.375f;
        float contribution = MathUtils.lerpf(1.0f, 0.5f, componentMasteryFactor + shapeMasteryFactor);
        return (int)Math.ceil(totalBaseline * contribution);
    }

    @Override
    public float getComplexity() {
        return this.complexity;
    }

    @Override
    public float getManaCost() {
        return this.overrideManaCost == -1.0f ? this.calculatedManaCost : this.overrideManaCost;
    }

    @Override
    public void setManaCost(float manaCost) {
        this.overrideManaCost = manaCost;
    }

    @Override
    public int getParticleColorOverride() {
        return this.particleColorOverride;
    }

    @Override
    public void setParticleColorOverride(int color) {
        this.particleColorOverride = color;
    }

    @Override
    public MAParticleType colorParticle(MAParticleType particle, Entity living) {
        float minAlpha = 0.1f;
        if (living != null && living instanceof Player) {
            MNACap.get((Player)living, PlayerMagicProvider.MAGIC).ifPresent(m -> {
                if (m.getParticleColorOverride() != -1) {
                    float pct = MathUtils.clamp((float)FastColor.ARGB32.alpha((int)m.getParticleColorOverride()) / 255.0f, minAlpha, 1.0f);
                    particle.setColor((float)FastColor.ARGB32.red((int)m.getParticleColorOverride()) * pct, (float)FastColor.ARGB32.green((int)m.getParticleColorOverride()) * pct, (float)FastColor.ARGB32.blue((int)m.getParticleColorOverride()) * pct);
                }
            });
        }
        if (this.particleColorOverride != -1) {
            float pct = MathUtils.clamp((float)FastColor.ARGB32.alpha((int)this.particleColorOverride) / 255.0f, minAlpha, 1.0f);
            particle.setColor((float)FastColor.ARGB32.red((int)this.particleColorOverride) * pct, (float)FastColor.ARGB32.green((int)this.particleColorOverride) * pct, (float)FastColor.ARGB32.blue((int)this.particleColorOverride) * pct);
        }
        return particle;
    }

    @Override
    public boolean isValid() {
        if (this.shape == null || this.shape.getPart() == null || this.components.stream().allMatch(c -> c == null)) {
            return false;
        }
        return !this.shape.getPart().isChanneled() && this.shape.getPart().allowChanneledComponents() || !this.components.stream().anyMatch(c -> !((SpellEffect)c.getPart()).canBeChanneled());
    }

    @Override
    public boolean isHarmful() {
        return this.components.stream().anyMatch(c -> c != null && c.getPart() != null && ((SpellEffect)c.getPart()).getUseTag() == SpellPartTags.HARMFUL);
    }

    public boolean isRote(Player player) {
        MutableBoolean valid = new MutableBoolean(true);
        MNACap.get(player, PlayerRoteSpellsProvider.ROTE).ifPresent(r -> {
            ArrayList<Attribute> modified_attrs = new ArrayList<Attribute>();
            if (!r.isRote(this.shape.getPart())) {
                valid.setFalse();
                return;
            }
            modified_attrs.addAll(this.shape.getContainedAttributes().stream().filter(a -> this.shape.getValueWithoutMultipliers((Attribute)((a))) != this.shape.getDefaultValue((Attribute)((a)))).collect(Collectors.toList()));
            this.components.forEach((Consumer<ModifiedSpellPart<SpellEffect>>)((Consumer<ModifiedSpellPart>)c -> {
                if (!r.isRote((ISpellComponent)c.getPart())) {
                    valid.setFalse();
                    return;
                }
                modified_attrs.addAll(c.getContainedAttributes().stream().filter(a -> this.shape.getValueWithoutMultipliers((Attribute)(((a)))) != this.shape.getDefaultValue((Attribute)(((a))))).collect(Collectors.toList()));
            }));
            modified_attrs.forEach(a -> {
                boolean modifier_attr_rote = false;
                for (int i = 0; i < this.modifiers.length; ++i) {
                    Modifier m = this.modifiers[i];
                    if (m == null) {
                        return;
                    }
                    if (!m.modifiesType((Attribute)((a)))) continue;
                    modifier_attr_rote = true;
                    break;
                }
                if (!modifier_attr_rote) {
                    valid.setFalse();
                }
            });
        });
        return valid.getValue();
    }

    public int getMaxChannelTime() {
        if (!this.isValid()) {
            return 0;
        }
        Shape s = (Shape)((ModifiedSpellPart)this.getShape()).getPart();
        return s.isChanneled() ? s.maxChannelTime(this.getShape()) : 0;
    }

    @Override
    public boolean isChanneled() {
        if (!this.isValid()) {
            return false;
        }
        return ((Shape)((ModifiedSpellPart)this.getShape()).getPart()).isChanneled();
    }

    @Override
    public boolean isMysterious() {
        return this.isMysterious;
    }

    public static boolean isMysterious(ItemStack stack) {
        CompoundTag spellTag;
        if (!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return false;
        }
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbt.contains(SPELL_COMPOUND_TAG) && (spellTag = nbt.getCompound(SPELL_COMPOUND_TAG)).contains("mysterious")) {
            return spellTag.getBoolean("mysterious");
        }
        return false;
    }

    public void setMysterious(boolean mysterious) {
        this.isMysterious = mysterious;
    }

    @Override
    public void setOverrideAffinity(Affinity affinity) {
        this.overrideAffinity = affinity;
    }

    public boolean hasOverrideAffinity() {
        return this.overrideAffinity != Affinity.UNKNOWN;
    }

    public boolean hasOverrideManaCost() {
        return this.overrideManaCost >= 0.0f;
    }

    @Override
    public boolean canFactionCraft(IPlayerProgression progression) {
        int i;
        if (((Shape)((ModifiedSpellPart)this.getShape()).getPart()).getFactionRequirement() != null && ((Shape)((ModifiedSpellPart)this.getShape()).getPart()).getFactionRequirement() != progression.getAlliedFaction()) {
            return false;
        }
        for (i = 0; i < 5; ++i) {
            IModifiedSpellPart c = this.getComponent(i);
            if (c == null || ((SpellEffect)((ModifiedSpellPart)c).getPart()).getFactionRequirement() == null || ((SpellEffect)((ModifiedSpellPart)c).getPart()).getFactionRequirement() == progression.getAlliedFaction()) continue;
            return false;
        }
        for (i = 0; i < 3; ++i) {
            Modifier m = this.getModifier(i);
            if (m == null || m.getFactionRequirement() == null || m.getFactionRequirement() == progression.getAlliedFaction()) continue;
            return false;
        }
        return true;
    }

    public boolean changeShapeAttributeValue(Attribute attribute, float newValue) {
        if (this.shape == null) {
            return false;
        }
        this.shape.setValue(attribute, newValue);
        this.recalculateSpellNumbers();
        return true;
    }

    public boolean changeComponentAttributeValue(int index, Attribute attribute, float newValue) {
        if (index < 0 || index >= this.components.size() || this.components.get(index) == null) {
            return false;
        }
        this.components.get(index).setValue(attribute, newValue);
        this.recalculateSpellNumbers();
        return true;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        CompoundTag spell = new CompoundTag();
        if (this.getShape() != null) {
            spell.put("shape", (Tag)((ModifiedSpellPart)this.getShape()).toNBT());
        }
        ListTag componentData = new ListTag();
        for (ModifiedSpellPart<SpellEffect> component : this.components) {
            if (component == null) continue;
            componentData.add(component.toNBT());
        }
        spell.put("components", (Tag)componentData);
        CompoundTag modifierNBT = new CompoundTag();
        for (int i = 0; i < 3; ++i) {
            if (this.getModifier(i) == null) {
                modifierNBT.putString("modifier_" + i, ManaAndArtifice.EMPTY.toString());
                continue;
            }
            modifierNBT.putString("modifier_" + i, this.getModifier(i).getRegistryName().toString());
        }
        spell.put("modifiers", (Tag)modifierNBT);
        spell.putBoolean("mysterious", this.isMysterious());
        spell.putString("overrideAffinity", this.overrideAffinity.toString());
        if (this.hasOverrideManaCost()) {
            spell.putFloat("overrideManaCost", this.overrideManaCost);
        }
        nbt.put(SPELL_COMPOUND_TAG, (Tag)spell);
        nbt.putInt(SPELL_COLOR_TAG, this.particleColorOverride);
    }

    public static void removeSpellFromTag(CompoundTag nbt) {
        nbt.remove(SPELL_COMPOUND_TAG);
    }

    public static SpellRecipe fromNBT(CompoundTag nbt) {
        SpellRecipe recipe = new SpellRecipe();
        if (nbt == null) {
            return recipe;
        }
        CompoundTag spellNBT = new CompoundTag();
        if (nbt.contains(SPELL_COMPOUND_TAG)) {
            spellNBT = nbt.getCompound(SPELL_COMPOUND_TAG);
        }
        if (spellNBT.contains("shape")) {
            recipe.shape = ModifiedSpellPart.fromNBT(spellNBT.getCompound("shape"), Registries.Shape);
        }
        if (spellNBT.contains("component")) {
            recipe.components.clear();
            recipe.components.add(ModifiedSpellPart.fromNBT(spellNBT.getCompound("component"), Registries.SpellEffect));
        } else if (spellNBT.contains("components")) {
            ListTag ComponentData = spellNBT.getList("components", 10);
            for (Tag cINBT : ComponentData) {
                ModifiedSpellPart<SpellEffect> comp;
                if (!(cINBT instanceof CompoundTag) || (comp = ModifiedSpellPart.fromNBT((CompoundTag)cINBT, Registries.SpellEffect)) == null) continue;
                recipe.components.add(comp);
            }
        }
        if (spellNBT.contains("modifiers")) {
            CompoundTag modifierNBT = spellNBT.getCompound("modifiers");
            for (int i = 0; i < 3; ++i) {
                ResourceLocation rLoc = ResourceLocation.parse(modifierNBT.getString("modifier_" + i));
                recipe.modifiers[i] = (Modifier)Registries.Modifier.getValue(rLoc);
            }
        }
        if (spellNBT.contains("mysterious")) {
            recipe.isMysterious = spellNBT.getBoolean("mysterious");
        }
        if (spellNBT.contains("overrideAffinity")) {
            recipe.overrideAffinity = Affinity.valueOf(spellNBT.getString("overrideAffinity"));
        }
        if (spellNBT.contains("overrideManaCost")) {
            recipe.overrideManaCost = spellNBT.getFloat("overrideManaCost");
        }
        recipe.particleColorOverride = SpellRecipe.getParticleColorOverride(nbt);
        recipe.recalculateSpellNumbers();
        return recipe;
    }

    public static boolean stackContainsSpell(ItemStack stack) {
        if (!stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            return false;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(SPELL_COMPOUND_TAG)) {
            CompoundTag subTag = tag.getCompound(SPELL_COMPOUND_TAG);
            return subTag.contains("shape") && (subTag.contains("component") || subTag.contains("components"));
        }
        return false;
    }

    public void calculateComplexity() {
        this.complexity = 0.0f;
        if (this.shape != null) {
            this.complexity += this.shape.getPart().initialComplexity();
            for (Attribute attr : this.shape.getContainedAttributes()) {
                float modifiedValue = this.shape.getValueWithoutMultipliers(attr);
                float modificationTotal = Math.abs(modifiedValue - this.shape.getDefaultValue(attr));
                if (modificationTotal == 0.0f) continue;
                this.complexity += (float)((int)Math.ceil(modificationTotal / this.shape.getStep(attr))) * this.shape.getStepComplexity(attr);
            }
        }
        for (ModifiedSpellPart<SpellEffect> component : this.components) {
            if (component == null) continue;
            this.complexity += component.getPart().initialComplexity();
            for (Attribute attr : component.getContainedAttributes()) {
                float modifiedValue = component.getValueWithoutMultipliers(attr);
                float modificationTotal = Math.abs(modifiedValue - component.getDefaultValue(attr));
                if (modificationTotal == 0.0f) continue;
                this.complexity += (float)((int)Math.ceil(modificationTotal / component.getStep(attr))) * component.getStepComplexity(attr);
            }
        }
    }

    public void calculateManaCost() {
        this.overrideManaCost = -1.0f;
        this.calculatedManaCost = this.complexity * 1.5f;
    }

    @Override
    public int getTier(@Nonnull Level world) {
        int tier = 1;
        if (this.shape != null) {
            tier = Math.max(tier, this.shape.getPart().getTier(world));
        }
        for (ModifiedSpellPart<SpellEffect> component : this.components) {
            if (component == null) continue;
            tier = Math.max(tier, component.getPart().getTier(world));
        }
        for (Modifier modifier : this.modifiers) {
            if (modifier == null) continue;
            tier = Math.max(tier, modifier.getTier(world));
        }
        return tier;
    }

    @Override
    public HashMap<Affinity, Float> getAffinity() {
        HashMap<Affinity, Float> affinityMap = new HashMap<Affinity, Float>();
        if (this.hasOverrideAffinity()) {
            affinityMap.put(this.overrideAffinity, Float.valueOf(1.0f));
            return affinityMap;
        }
        float total = 0.0f;
        HashMap<Affinity, Integer> affinityCount = new HashMap<Affinity, Integer>();
        for (ModifiedSpellPart<SpellEffect> component : this.components) {
            if (component == null) continue;
            total += 1.0f;
            Affinity aff = component.getPart().getAffinity();
            if (!affinityCount.containsKey(aff)) {
                affinityCount.put(aff, 1);
                continue;
            }
            affinityCount.put(aff, (Integer)affinityCount.get(aff) + 1);
        }
        for (Affinity aff : affinityCount.keySet()) {
            affinityMap.put(aff, Float.valueOf((float)((Integer)affinityCount.get(aff)).intValue() / total));
        }
        if (affinityMap.size() == 0) {
            affinityMap.put(Affinity.UNKNOWN, Float.valueOf(1.0f));
        }
        return affinityMap;
    }

    @Override
    public Affinity getHighestAffinity() {
        return (Affinity)(Collections.max(this.getAffinity().entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey());
    }

    public void maximize() {
        if (this.shape != null) {
            this.shape.getContainedAttributes().forEach(a -> this.shape.setValue((Attribute)(a), this.shape.getMaximumValue((Attribute)(a))));
        }
        this.components.forEach((Consumer<ModifiedSpellPart<SpellEffect>>)((Consumer<ModifiedSpellPart>)c -> c.getContainedAttributes().forEach(a -> c.setValue((Attribute)((a)), this.shape.getMaximumValue((Attribute)((a)))))));
        this.recalculateSpellNumbers();
    }

    @Override
    public void addItemTooltip(ItemStack stack, Level worldIn, List<Component> tooltip, Player player) {
        String displayName = stack.getHoverName().getString();
        if (this.isValid() && !this.isMysterious()) {
            SpellCaster.applyAdjusters(stack, (LivingEntity)player, InteractionHand.MAIN_HAND, false, this, SpellCastStage.SPELL_TOOLTIP);
            NameProcessors.checkAndAddDisplay(this, displayName, tooltip);
            if (Screen.hasShiftDown()) {
                IModifiedSpellPart shape = this.getShape();
                if (shape != null) {
                    tooltip.add((Component)Component.translatable((String)"item.mna.spell.shape_attributes").withStyle(ChatFormatting.GREEN));
                    for (AttributeValuePair attr : ((Shape)((ModifiedSpellPart)shape).getPart()).getModifiableAttributes()) {
                        tooltip.add((Component)Component.translatable((String)"item.mna.spell.attribute_display", (Object[])new Object[]{attr.getAttribute().name(), Float.valueOf(((ModifiedSpellPart)shape).getValue(attr.getAttribute()))}).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
                    }
                }
                tooltip.add((Component)Component.literal((String)" "));
                this.iterateComponents(component -> {
                    String componentName = Component.translatable((String)((SpellEffect)component.getPart()).getRegistryName().toString()).getString();
                    tooltip.add((Component)Component.translatable((String)"item.mna.spell.component_attributes", (Object[])new Object[]{componentName}).withStyle(ChatFormatting.GREEN));
                    for (AttributeValuePair attr : ((SpellEffect)component.getPart()).getModifiableAttributes()) {
                        tooltip.add((Component)Component.translatable((String)"item.mna.spell.attribute_display", (Object[])new Object[]{attr.getAttribute().name(), Float.valueOf(component.getValue(attr.getAttribute()))}).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
                    }
                });
                tooltip.add((Component)Component.literal((String)" "));
                if (this.isChanneled()) {
                    tooltip.add((Component)Component.translatable((String)"item.mna.spell.channeled_mana_cost_display", (Object[])new Object[]{String.format("%.2f", Float.valueOf(this.getManaCost() * 20.0f))}).withStyle(ChatFormatting.GOLD));
                } else {
                    tooltip.add((Component)Component.translatable((String)"item.mna.spell.mana_cost_display", (Object[])new Object[]{String.format("%.2f", Float.valueOf(this.getManaCost()))}).withStyle(ChatFormatting.GOLD));
                }
                tooltip.add((Component)Component.translatable((String)"item.mna.spell.complexity_display", (Object[])new Object[]{Float.valueOf(this.getComplexity())}).withStyle(ChatFormatting.GOLD));
                tooltip.add((Component)Component.literal((String)" "));
            } else {
                tooltip.add((Component)Component.translatable((String)"item.mna.spell.shift_prompt").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
            }
            if (this.getReagents(ManaAndArtifice.instance.proxy.getClientPlayer(), null, null).size() > 0) {
                if (Screen.hasAltDown()) {
                    tooltip.add((Component)Component.translatable((String)"item.mna.spell.required_reagents").withStyle(ChatFormatting.GOLD));
                    for (SpellReagent reagent : this.getReagents(ManaAndArtifice.instance.proxy.getClientPlayer(), null, null)) {
                        reagent.getAddedBy().addReagentTooltip(ManaAndArtifice.instance.proxy.getClientPlayer(), null, tooltip, reagent);
                    }
                    tooltip.add((Component)Component.literal((String)" "));
                } else {
                    tooltip.add((Component)Component.translatable((String)"item.mna.spell.alt_prompt").withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
                }
            }
        }
    }

    @Override
    public List<SpellReagent> getReagents(@Nullable Player caster, @Nullable InteractionHand hand, @Nullable SpellCastingResult spellResult) {
        ArrayList<SpellReagent> reagents = new ArrayList<SpellReagent>();
        List<SpellReagent> shape_reagents = ((Shape)((ModifiedSpellPart)this.getShape()).getPart()).getRequiredReagents(caster);
        if (shape_reagents != null) {
            reagents.addAll(shape_reagents);
        }
        for (int i = 0; i < this.components.size(); ++i) {
            List<SpellReagent> component_reagents;
            SpellEffect c = (SpellEffect)((ModifiedSpellPart)this.getComponent(i)).getPart();
            if (spellResult != null && spellResult.getResultFor(c) != ComponentApplicationResult.SUCCESS && spellResult.getResultFor(c) != ComponentApplicationResult.DELAYED && spellResult.getResultFor(c) != ComponentApplicationResult.TARGET_ENTITY_SPAWNED || (component_reagents = c.getRequiredReagents(caster, hand)) == null) continue;
            reagents.addAll(component_reagents);
        }
        SpellReagentsEvent event = new SpellReagentsEvent(this, caster, reagents);
        NeoForge.EVENT_BUS.post(event);
        return event.getRequiredReagents();
    }

    @Override
    public boolean containsPart(ResourceLocation partID) {
        if (this.getShape() != null && ((Shape)((ModifiedSpellPart)this.getShape()).getPart()).getRegistryName().equals(partID)) {
            return true;
        }
        for (IModifiedSpellPart<SpellEffect> effect : this.getComponents()) {
            if (!effect.getPart().getRegistryName().equals(partID)) continue;
            return true;
        }
        for (int i = 0; i < 3; ++i) {
            Modifier m = this.getModifier(i);
            if (m == null || !m.getRegistryName().equals(partID)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isFactionSpell(IFaction faction) {
        IFaction partReq;
        Object partReq2;
        if (this.getShape() != null && (partReq2 = ((Shape)((ModifiedSpellPart)this.getShape()).getPart()).getFactionRequirement()) != null && partReq2 == faction) {
            return true;
        }
        for (IModifiedSpellPart iModifiedSpellPart : this.getComponents()) {
            partReq = ((SpellEffect)iModifiedSpellPart.getPart()).getFactionRequirement();
            if (partReq == null || partReq != faction) continue;
            return true;
        }
        for (int i = 0; i < 3; ++i) {
            Modifier modifier = this.getModifier(i);
            if (modifier == null || (partReq = modifier.getFactionRequirement()) == null || partReq != faction) continue;
            return true;
        }
        return false;
    }

    public HashMap<IFaction, Float> getCastFactionIre() {
        IFaction partReq;
        Object partReq2;
        HashMap<IFaction, Float> factionIre = new HashMap<IFaction, Float>();
        if (this.getShape() != null && (partReq2 = ((Shape)((ModifiedSpellPart)this.getShape()).getPart()).getFactionRequirement()) != null) {
            factionIre.put((IFaction)partReq2, Float.valueOf(factionIre.getOrDefault(partReq2, Float.valueOf(0.0f)).floatValue() + ((Shape)((ModifiedSpellPart)this.getShape()).getPart()).ire()));
        }
        for (IModifiedSpellPart iModifiedSpellPart : this.getComponents()) {
            partReq = ((SpellEffect)iModifiedSpellPart.getPart()).getFactionRequirement();
            if (partReq == null) continue;
            factionIre.put(partReq, Float.valueOf(factionIre.getOrDefault(partReq, Float.valueOf(0.0f)).floatValue() + ((SpellEffect)iModifiedSpellPart.getPart()).ire()));
        }
        for (int i = 0; i < 3; ++i) {
            Modifier modifier = this.getModifier(i);
            if (modifier == null || (partReq = modifier.getFactionRequirement()) == null) continue;
            factionIre.put(partReq, Float.valueOf(factionIre.getOrDefault(partReq, Float.valueOf(0.0f)).floatValue() + modifier.ire()));
        }
        return factionIre;
    }

    public void writeRecipeForRitual(Level world, CompoundTag nbt) {
        ComponentRecipe componentRecipe;
        ModifiedSpellPart<SpellEffect> component;
        ShapeRecipe shapeRecipe;
        if (this.components.size() != 1) {
            return;
        }
        CompoundTag ritualReagentData = new CompoundTag();
        NonNullList patterns = NonNullList.create();
        if (this.shape != null && (shapeRecipe = (ShapeRecipe)ItemAndPatternRecipeHelper.GetRecipe(world, this.shape.getPart().getRegistryName(), (RecipeType)RecipeInit.SHAPE_TYPE.get())) != null) {
            this.WriteRLocList(ritualReagentData, Arrays.asList(shapeRecipe.getRequiredItems()), "shape_items");
            for (ResourceLocation rLoc : shapeRecipe.getRequiredPatterns()) {
                patterns.add(rLoc);
            }
        }
        if ((component = this.components.get(0)) != null && (componentRecipe = (ComponentRecipe)ItemAndPatternRecipeHelper.GetRecipe(world, component.getPart().getRegistryName(), (RecipeType)RecipeInit.COMPONENT_TYPE.get())) != null) {
            this.WriteRLocList(ritualReagentData, Arrays.asList(componentRecipe.getRequiredItems()), "component_items");
            for (ResourceLocation rLoc : componentRecipe.getRequiredPatterns()) {
                patterns.add(rLoc);
            }
        }
        for (int i = 0; i < this.modifiers.length; ++i) {
            ModifierRecipe modifierRecipe;
            if (this.modifiers[i] == null || (modifierRecipe = (ModifierRecipe)ItemAndPatternRecipeHelper.GetRecipe(world, this.modifiers[i].getRegistryName(), (RecipeType)RecipeInit.MODIFIER_TYPE.get())) == null) continue;
            this.WriteRLocList(ritualReagentData, Arrays.asList(modifierRecipe.getRequiredItems()), "modifier_" + i + "_items");
            for (ResourceLocation rLoc : modifierRecipe.getRequiredPatterns()) {
                patterns.add(rLoc);
            }
        }
        this.WriteRLocList(ritualReagentData, (Collection<ResourceLocation>)patterns, "pattern");
        nbt.put("ritual_reagent_data", (Tag)ritualReagentData);
    }

    private void WriteRLocList(CompoundTag nbt, Collection<ResourceLocation> list, String prefix) {
        if (list.size() == 0) {
            return;
        }
        int count = 0;
        nbt.putInt(prefix + "_count", list.size());
        for (ResourceLocation rLoc : list) {
            nbt.putString(prefix + "_" + count++, rLoc.toString());
        }
    }

    private static NonNullList<ResourceLocation> ReadRLocList(CompoundTag nbt, String prefix) {
        NonNullList rLocs = NonNullList.create();
        if (!nbt.contains(prefix + "_count")) {
            return rLocs;
        }
        int count = nbt.getInt(prefix + "_count");
        for (int i = 0; i < count; ++i) {
            String key = prefix + "_" + i;
            if (nbt.contains(key)) {
                rLocs.add(ResourceLocation.parse(nbt.getString(key)));
                continue;
            }
            ManaAndArtifice.LOGGER.error("Failed to read key '" + key + "' from dynamic ritual spell recipe NBT (not found).");
        }
        return rLocs;
    }

    public static boolean isReagentContainer(ItemStack recipeStack) {
        CompoundTag nbt = recipeStack.getTag();
        return nbt != null && nbt.contains("ritual_reagent_data");
    }

    public static NonNullList<ResourceLocation> getShapeReagents(ItemStack recipeStack) {
        CompoundTag ritualReagentData = MNAItemData.element(recipeStack, "ritual_reagent_data");
        return SpellRecipe.ReadRLocList(ritualReagentData, "shape_items");
    }

    public static NonNullList<ResourceLocation> getComponentReagents(ItemStack recipeStack) {
        CompoundTag ritualReagentData = MNAItemData.element(recipeStack, "ritual_reagent_data");
        return SpellRecipe.ReadRLocList(ritualReagentData, "component_items");
    }

    public static NonNullList<ResourceLocation> getModifierReagents(ItemStack recipeStack, int modifierIndex) {
        CompoundTag ritualReagentData = MNAItemData.element(recipeStack, "ritual_reagent_data");
        return SpellRecipe.ReadRLocList(ritualReagentData, "modifier_" + modifierIndex + "_items");
    }

    public static NonNullList<ResourceLocation> getPatterns(ItemStack recipeStack) {
        CompoundTag ritualReagentData = MNAItemData.element(recipeStack, "ritual_reagent_data");
        return SpellRecipe.ReadRLocList(ritualReagentData, "pattern");
    }

    public static int getParticleColorOverride(CompoundTag nbt) {
        if (nbt.contains(SPELL_COLOR_TAG)) {
            return nbt.getInt(SPELL_COLOR_TAG);
        }
        return -1;
    }

    public ItemStack createAsSpell() {
        ItemStack newSpell = new ItemStack((ItemLike)ItemInit.SPELL.get());
        this.writeToNBT(newSpell.getTag());
        return newSpell;
    }

    @Override
    public boolean isChargedSpell(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof MagicStaff) {
            MagicStaff staff = (MagicStaff)item;
            return staff.isChargeSpell(stack);
        }
        return false;
    }

    @Override
    public void consumeCharges(ItemStack stack, Player caster) {
        Item item = stack.getItem();
        if (item instanceof MagicStaff) {
            MagicStaff staff = (MagicStaff)item;
            staff.consumeCharge(stack, caster, null);
        }
    }

    public SpellValidityResult isCraftable(Level level, IPlayerProgression progression, boolean creative, boolean allowChanneled, boolean checkFaction) {
        if (progression == null) {
            return SpellValidityResult.LOW_TIER;
        }
        if (this.getShape() == null) {
            return SpellValidityResult.NO_SHAPE;
        }
        if (this.getComponent(0) == null) {
            return SpellValidityResult.NO_COMPONENT;
        }
        if (!allowChanneled && (((Shape)((ModifiedSpellPart)this.getShape()).getPart()).isChanneled() || !((Shape)((ModifiedSpellPart)this.getShape()).getPart()).allowChanneledComponents()) && this.components.stream().anyMatch(c -> !((SpellEffect)c.getPart()).canBeChanneled())) {
            return SpellValidityResult.CANNOT_CHANNEL;
        }
        if (creative) {
            return SpellValidityResult.READY;
        }
        if (progression.getTier() < this.getTier(level)) {
            return SpellValidityResult.LOW_TIER;
        }
        if (this.getComplexity() > (float)progression.getTierMaxComplexity()) {
            return SpellValidityResult.TOO_COMPLEX;
        }
        if (checkFaction && !this.canFactionCraft(progression)) {
            return SpellValidityResult.WRONG_FACTION;
        }
        return SpellValidityResult.READY;
    }
}

