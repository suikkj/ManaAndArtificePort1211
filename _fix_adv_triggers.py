import io

# 1) TriggerPredicates.java (novo)
tp = '''package com.mna.advancements.triggers;

import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.component.DataComponentPredicate;

/**
 * [PORT 1.21.1] O 1.21.1 removeu a constante ItemPredicate.ANY (no 1.20.1,
 * ItemPredicate.fromJson(null) devolvia ANY). Varias advancements do MNA omitem
 * "item" nas conditions; com fieldOf("item") obrigatorio elas falhavam o parse.
 * Este e o equivalente do ANY para usar como default de optionalFieldOf.
 */
public final class TriggerPredicates {
    public static final ItemPredicate ANY = new ItemPredicate(Optional.empty(), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Map.of());
    private TriggerPredicates() {
    }
}
'''
io.open('src/main/java/com/mna/advancements/triggers/TriggerPredicates.java', 'w', encoding='utf-8', newline='').write(tp)
print('TriggerPredicates.java criado')

# 2) 7 triggers: fieldOf("item") -> optionalFieldOf("item", TriggerPredicates.ANY)
n = 0
for f in ['ArcaneFurnaceSmeltTrigger', 'DisenchantTrigger', 'EldrinAltarCraftTrigger', 'ManaweaveAltarCraftTrigger', 'RunescribingTableCraftTrigger', 'RunicAnvilCraftTrigger', 'UseCharmTrigger']:
    p = 'src/main/java/com/mna/advancements/triggers/' + f + '.java'
    t = io.open(p, encoding='utf-8').read()
    o = t
    t = t.replace('ItemPredicate.CODEC.fieldOf("item")', 'ItemPredicate.CODEC.optionalFieldOf("item", TriggerPredicates.ANY)')
    if t != o:
        io.open(p, 'w', encoding='utf-8', newline='').write(t)
        n += 1
print('triggers item->optional:', n)

# 3) 4 triggers: null-default -> Optional
def fix(path, old, new):
    t = io.open(path, encoding='utf-8').read()
    assert t.count(old) == 1, (path, 'not found or dup')
    io.open(path, 'w', encoding='utf-8', newline='').write(t.replace(old, new))
    print('ok:', path.split('/')[-1])

fix('src/main/java/com/mna/advancements/triggers/CaptureWellspringTrigger.java',
'''    public record Instance(Optional<ContextAwarePredicate> player, @Nullable Affinity type) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            CaptureWellspringTrigger.AFFINITY_CODEC.optionalFieldOf("type", (Affinity)null).forGetter(Instance::type)
        ).apply(inst, Instance::new));
        public boolean matches(Affinity type) {
            if (this.type != null) return type == this.type;
            return true;
        }
    }''',
'''    public record Instance(Optional<ContextAwarePredicate> player, Optional<Affinity> type) implements SimpleCriterionTrigger.SimpleInstance {
        // [PORT 1.21.1] optionalFieldOf("type", (Affinity)null) -> DataResult.success(null) -> NPE no
        // RecordCodecBuilder quando conditions vem sem "type" (3 advancements quebravam). Optional evita.
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            CaptureWellspringTrigger.AFFINITY_CODEC.optionalFieldOf("type").forGetter(Instance::type)
        ).apply(inst, Instance::new));
        public boolean matches(Affinity type) {
            return this.type.isEmpty() || type == this.type.get();
        }
    }''')

fix('src/main/java/com/mna/advancements/triggers/SetBonusTrigger.java',
'''    public record Instance(Optional<ContextAwarePredicate> player, @Nullable ResourceLocation setID) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("set", (ResourceLocation)null).forGetter(Instance::setID)
        ).apply(inst, Instance::new));
        public boolean test(ResourceLocation setID) {
            if (setID == null) return false;
            if (this.setID != null) return this.setID.equals(setID);
            return true;
        }
    }''',
'''    public record Instance(Optional<ContextAwarePredicate> player, Optional<ResourceLocation> setID) implements SimpleCriterionTrigger.SimpleInstance {
        // [PORT 1.21.1] optionalFieldOf com default null -> NPE no RecordCodecBuilder (conditions sem "set"). Optional evita.
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("set").forGetter(Instance::setID)
        ).apply(inst, Instance::new));
        public boolean test(ResourceLocation setID) {
            if (setID == null) return false;
            return this.setID.isEmpty() || this.setID.get().equals(setID);
        }
    }''')

fix('src/main/java/com/mna/advancements/triggers/DrawManaweavingPatternTrigger.java',
'''    public record Instance(Optional<ContextAwarePredicate> player, @Nullable ResourceLocation patternId) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("patternId", (ResourceLocation)null).forGetter(Instance::patternId)
        ).apply(inst, Instance::new));
        public boolean matches(ResourceLocation patternId) {
            if (this.patternId != null) return this.patternId.equals(patternId);
            return true;
        }
    }''',
'''    public record Instance(Optional<ContextAwarePredicate> player, Optional<ResourceLocation> patternId) implements SimpleCriterionTrigger.SimpleInstance {
        // [PORT 1.21.1] optionalFieldOf com default null -> NPE no RecordCodecBuilder (conditions sem "patternId"). Optional evita.
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("patternId").forGetter(Instance::patternId)
        ).apply(inst, Instance::new));
        public boolean matches(ResourceLocation patternId) {
            return this.patternId.isEmpty() || this.patternId.get().equals(patternId);
        }
    }''')

fix('src/main/java/com/mna/advancements/triggers/FactionJoinTrigger.java',
'''    public record Instance(Optional<ContextAwarePredicate> player, @Nullable ResourceLocation faction) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("faction", (ResourceLocation)null).forGetter(Instance::faction)
        ).apply(inst, Instance::new));
        public boolean test(ResourceLocation factionID) {
            if (factionID == null) return false;
            if (this.faction != null) return this.faction.equals(factionID);
            return true;
        }
    }''',
'''    public record Instance(Optional<ContextAwarePredicate> player, Optional<ResourceLocation> faction) implements SimpleCriterionTrigger.SimpleInstance {
        // [PORT 1.21.1] optionalFieldOf com default null -> NPE no RecordCodecBuilder (conditions sem "faction"). Optional evita.
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceLocation.CODEC.optionalFieldOf("faction").forGetter(Instance::faction)
        ).apply(inst, Instance::new));
        public boolean test(ResourceLocation factionID) {
            if (factionID == null) return false;
            return this.faction.isEmpty() || this.faction.get().equals(factionID);
        }
    }''')

# 4) upgrade_runeforge: material 0 -> "0"
p = 'src/main/resources/data/mna/advancement/tier_4/upgrade_runeforge.json'
t = io.open(p, encoding='utf-8-sig').read()
assert '"material": 0' in t
io.open(p, 'w', encoding='utf-8', newline='').write(t.replace('"material": 0', '"material": "0"'))
print('upgrade_runeforge: material -> "0"')
