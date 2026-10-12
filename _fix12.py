# _fix12.py - B19c: behaviors por ResourceKey, AuraEvent, Aura, classes de ench, RecipeInit, RunicAnvilTile
import os
import re

FAILS = []
ROOT = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = n.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + '] x' + str(t.count(o)))
    return t.replace(o, n)


def add_import(t, imp):
    line = 'import ' + imp + ';'
    if line in t:
        return t
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    idx = None
    for i, l in enumerate(lines):
        if l.startswith('import '):
            idx = i
    lines.insert(idx + 1, line)
    return sep.join(lines)


# ============================================================
# 1) MAEnchantmentBridge: helpers de item/tag
# ============================================================
print('== MAEnchantmentBridge helpers ==')
p = ROOT + 'api/enchantments/MAEnchantmentBridge.java'
t = load(p)
if 'isStaffOrWeapon' not in t:
    helpers = '''    /** [PORT] Equivalente a EnchantmentCategory.WEAPON (tag vanilla). */
    public static boolean isWeapon(Item item) {
        return item.builtInRegistryHolder().is(net.minecraft.tags.ItemTags.WEAPON_ENCHANTABLE);
    }

    /** Tag mna:staves. */
    public static boolean isStaff(Item item) {
        return item.builtInRegistryHolder().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, com.mna.api.tools.MATags.Items.STAVES));
    }

    public static boolean isStaffOrWeapon(Item item) {
        return isStaff(item) || isWeapon(item);
    }

    /** Equivalente a EnchantmentCategory.ARMOR_FEET. */
    public static boolean isBoots(Item item) {
        return item.builtInRegistryHolder().is(net.minecraft.tags.ItemTags.FOOT_ARMOR_ENCHANTABLE);
    }

    /** HolderSet de itens a partir de uma tag (para EnchantmentDefinition). */
    public static HolderSet<Item> itemTag(net.minecraft.tags.TagKey<Item> tag) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getOrThrow(tag);
    }

    public static HolderSet<Item> itemTag(net.minecraft.resources.ResourceLocation tag) {
        return itemTag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, tag));
    }

    /** Cria um Enchantment minimo (sem efeitos) a partir de uma definicao. */'''
    t = rep(t, '    /** Cria um Enchantment minimo (sem efeitos) a partir de uma definicao. */', helpers, 'helpers')
    save(p, t)

# ============================================================
# 2) AuraEvent: Enchantment -> MAEnchantmentBase
# ============================================================
print('== AuraEvent ==')
p = ROOT + 'api/events/AuraEvent.java'
t = load(p)
t = t.replace('private final Enchantment AuraEnchantment;', 'private final com.mna.enchantments.base.MAEnchantmentBase AuraEnchantment;')
t = t.replace('public AuraEvent(Enchantment auraEnchantment) {', 'public AuraEvent(com.mna.enchantments.base.MAEnchantmentBase auraEnchantment) {')
t = t.replace('public Enchantment getAura() {', 'public com.mna.enchantments.base.MAEnchantmentBase getAura() {')
t = t.replace('public Tick(Enchantment aurEnchantment, LivingEntity wearer) {', 'public Tick(com.mna.enchantments.base.MAEnchantmentBase aurEnchantment, LivingEntity wearer) {')
t = t.replace('public Compatibility(Enchantment auraEnchantment, @Nullable Enchantment otherEnchantment, ItemStack placedOn, LivingEntity enchanter) {',
              'public Compatibility(com.mna.enchantments.base.MAEnchantmentBase auraEnchantment, @Nullable Enchantment otherEnchantment, ItemStack placedOn, LivingEntity enchanter) {')
t = re.sub(r'public Radius\(Enchantment ', 'public Radius(com.mna.enchantments.base.MAEnchantmentBase ', t)
t = re.sub(r'public Numerics\(Enchantment ', 'public Numerics(com.mna.enchantments.base.MAEnchantmentBase ', t)
save(p, t)
print('  ok AuraEvent')

# ============================================================
# 3) Aura.java
# ============================================================
print('== Aura ==')
p = ROOT + 'enchantments/auras/Aura.java'
t = load(p)
t = rep(t, 'EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem((ItemStack)stack);',
        'EquipmentSlot slot = armor.getEquipmentSlot();', 'aura slot')
t = rep(t, 'import com.mna.api.events.RunicAnvilShouldActivateEvent.Result;',
        'import com.mna.api.events.AuraEvent.Compatibility.Result;', 'aura result import')
t = rep(t, 'return !(ench instanceof Aura);', 'return !(EnchantmentBehaviors.get(ench) instanceof Aura);', 'aura compat')
t = rep(t, '''    public boolean isCompatibleWith(Enchantment other, Player enchanter) {''',
        '''    public boolean isCompatibleWith(Enchantment other) {
        return this.checkCompatibility(other);
    }

    public boolean isCompatibleWith(Enchantment other, Player enchanter) {''', 'aura 1arg compat')
t = rep(t, '''    private MobEffect _effect;
    private DeferredHolder<MobEffect, ? extends MobEffect> _deferredEffect;''',
        '''    private net.minecraft.core.Holder<MobEffect> _effectHolder;''', 'aura fields')
t = rep(t, '''    public Aura withEffect(MobEffect effect) {
        this._effect = effect;
        return this;
    }

    public Aura withEffect(DeferredHolder<MobEffect, ? extends MobEffect> effect) {
        this._deferredEffect = effect;
        return this;
    }''',
        '''    public Aura withEffect(net.minecraft.core.Holder<MobEffect> effect) {
        this._effectHolder = effect;
        return this;
    }''', 'aura withEffect')
t = rep(t, '''    private MobEffect getEffect() {
        if (this._effect == null && this._deferredEffect != null && this._deferredEffect.isPresent()) {
            this._effect = (MobEffect)this._deferredEffect.get();
        }
        return this._effect;
    }''',
        '''    private net.minecraft.core.Holder<MobEffect> getEffect() {
        return this._effectHolder;
    }''', 'aura getEffect')
t = rep(t, 'List players = null;', 'List<Player> players = null;', 'aura players')
t = add_import(t, 'com.mna.enchantments.framework.EnchantmentBehaviors')
save(p, t)

# ============================================================
# 4) classes pequenas de ench
# ============================================================
print('== classes de ench ==')
for rel in ['enchantments/Beheading.java', 'enchantments/staves/Bludgeoning.java',
            'enchantments/staves/DurationModifier.java', 'enchantments/staves/RangeModifier.java',
            'enchantments/staves/SpeedModifier.java']:
    p = ROOT + rel
    t = load(p)
    t = t.replace('EnchantmentEnumExtender.StaffOrWeapon().canEnchant(stack.getItem())', 'EnchantmentEnumExtender.staffOrWeapon(stack.getItem())')
    t = t.replace('EnchantmentEnumExtender.Staves().canEnchant(stack.getItem())', 'EnchantmentEnumExtender.staves(stack.getItem())')
    save(p, t)
print('  ok extender sites')

# Bludgeoning: DamageEnchantment -> behaviors
p = ROOT + 'enchantments/staves/Bludgeoning.java'
t = load(p)
t = rep(t, '''    public boolean checkCompatibility(Enchantment pEnch) {
        return !(pEnch instanceof DamageEnchantment);
    }''',
        '''    public boolean checkCompatibility(Enchantment pEnch) {
        return !(com.mna.enchantments.framework.EnchantmentBehaviors.get(pEnch) instanceof com.mna.enchantments.base.MADamageEnchantmentBase);
    }''', 'bludgeoning compat')
save(p, t)

# ModifierEnchantment + subclasses
p = ROOT + 'enchantments/base/ModifierEnchantment.java'
t = load(p)
t = rep(t, 'return !(pOther instanceof ModifierEnchantment);',
        'return !(com.mna.enchantments.framework.EnchantmentBehaviors.get(pOther) instanceof ModifierEnchantment);', 'modifier compat')
save(p, t)

for name, attr in [('DurationModifier', 'DURATION'), ('RangeModifier', 'RANGE'), ('SpeedModifier', 'SPEED')]:
    p = ROOT + 'enchantments/staves/' + name + '.java'
    t = load(p)
    t = rep(t, '    public ' + name + '() {\n    }',
            '''    public ''' + name + '''() {
        super(Attribute.''' + attr + ''', 1, com.mna.api.enchantments.MAEnchantmentBridge.Rarity.UNCOMMON, com.mna.api.enchantments.MAEnchantmentBridge.Category.STAVES, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }''', name + ' ctor')
    save(p, t)

# Bouncing/Cloudstep/TransitoryStep: category -> isBoots
for rel in ['enchantments/Bouncing.java', 'enchantments/Cloudstep.java', 'enchantments/TransitoryStep.java']:
    p = ROOT + rel
    t = load(p)
    t = rep(t, 'return this.category.canEnchant(pStack.getItem());',
            'return com.mna.api.enchantments.MAEnchantmentBridge.isBoots(pStack.getItem());', rel + ' boots')
    save(p, t)

# Cloudstep: noJumpDelay privado -> reflexao
p = ROOT + 'enchantments/Cloudstep.java'
t = load(p)
if 'mnaNoJumpDelay' not in t:
    t = rep(t, 'source.noJumpDelay == 0', 'mnaNoJumpDelay(source) == 0', 'cloudstep delay')
    # helper antes do ultimo }
    helper = '''
    private static int mnaNoJumpDelay(net.minecraft.world.entity.LivingEntity entity) {
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.LivingEntity.class.getDeclaredField("noJumpDelay");
            f.setAccessible(true);
            return (Integer)f.get(entity);
        } catch (ReflectiveOperationException e) {
            return 1;
        }
    }
}'''
    t = t.rstrip()
    if t.endswith('}'):
        t = t[:-1] + helper + '\n'
    save(p, t)
print('  ok pequenas')

# ============================================================
# 5) EnchantmentEventHandler
# ============================================================
print('== EnchantmentEventHandler ==')
p = ROOT + 'enchantments/framework/EnchantmentEventHandler.java'
t = load(p)
n = t.count('event.player')
t = t.replace('event.player', 'event.getEntity()')
print('  event.player:', n)
save(p, t)

# ============================================================
# 6) RecipeInit
# ============================================================
print('== RecipeInit ==')
p = ROOT + 'recipes/RecipeInit.java'
t = load(p)
helpers = '''
    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> DeferredHolder<RecipeType<T>, RecipeType<T>> type(String id) {
        return (DeferredHolder<RecipeType<T>, RecipeType<T>>)(DeferredHolder)TYPES.register(id, () -> RecipeType.simple(RLoc.create(id)));
    }

    @SuppressWarnings("unchecked")
    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> DeferredHolder<RecipeSerializer<T>, RecipeSerializer<T>> serializer(String id, java.util.function.Supplier<? extends RecipeSerializer<T>> supplier) {
        return (DeferredHolder<RecipeSerializer<T>, RecipeSerializer<T>>)(DeferredHolder)SERIALIZERS.register(id, supplier);
    }
'''
t = rep(t, '    public static DeferredHolder<RecipeType<RitualRecipe>', helpers + '    public static DeferredHolder<RecipeType<RitualRecipe>', 'recipe helpers')
t = re.sub(r'TYPES\.register\("([\w-]+)", \(\) -> RecipeType\.simple\(\(ResourceLocation\)RLoc\.create\("[\w-]+"\)\)\)', r'type("\1")', t)
t = re.sub(r'SERIALIZERS\.register\("([\w-]+)", \(\) -> new (\w+)\(\)\)', r'serializer("\1", \2::new)', t)
save(p, t)
print('  ok RecipeInit')

# ============================================================
# 7) RunicAnvilTile
# ============================================================
print('== RunicAnvilTile ==')
p = ROOT + 'blocks/tileentities/RunicAnvilTile.java'
t = load(p)
t = rep(t, 'if (player != null && (object = e2.getKey()) instanceof Aura && !(aura = (Aura)(object)).canEnchant(this.__cachedRecipeOutput, player)) {',
        'if (player != null && (object = com.mna.enchantments.framework.EnchantmentBehaviors.get((Enchantment)e2.getKey())) instanceof Aura && !(aura = (Aura)(object)).canEnchant(this.__cachedRecipeOutput, player)) {', 'anvil 1')
t = rep(t, 'if (player != null && (k = entry.getKey()) instanceof Aura && !(aura = (Aura)(k)).canEnchant(this.__cachedRecipeOutput, player)) {',
        'if (player != null && (k = com.mna.enchantments.framework.EnchantmentBehaviors.get((Enchantment)entry.getKey())) instanceof Aura && !(aura = (Aura)(k)).canEnchant(this.__cachedRecipeOutput, player)) {', 'anvil 2')
t = rep(t, 'if (player != null && (k = entry.getKey()) instanceof Aura && !(aura = (Aura)(k)).isCompatibleWith((Enchantment)ee.getKey(), player)) {',
        'if (player != null && (k = com.mna.enchantments.framework.EnchantmentBehaviors.get((Enchantment)entry.getKey())) instanceof Aura && !(aura = (Aura)(k)).isCompatibleWith((Enchantment)ee.getKey(), player)) {', 'anvil 3')
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
