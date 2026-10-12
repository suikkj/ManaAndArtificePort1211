# _fix3.py - lote 3: WorldRenderUtils (Pose), ImageButton->MNAImageButton,
# GuiGuideBook (casts), MAArmorMaterial -> ArmorMaterialInit (record 1.21.1)
import os
import re

FAILS = []
ROOT = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag, expect=None):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = n.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    c = t.count(o)
    print('  ok [' + tag + '] x' + str(c))
    return t.replace(o, n)


def all_java():
    out = []
    for dp, _, fns in os.walk('src/main/java'):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


# ============================================================
# A) WorldRenderUtils: helpers createVertex/addVertex passam a receber Pose
# ============================================================
print('== WorldRenderUtils ==')
p = 'src/main/java/com/mna/tools/render/WorldRenderUtils.java'
t = load(p)
t = rep(t, ', Matrix3f normalMatrix, ', ', PoseStack.Pose normalMatrix, ', 'WRU helpers')
t = re.sub(r'Matrix3f normalMatrix = (\w+(?:\$\w+)?)\.normal\(\);', r'PoseStack.Pose normalMatrix = \1;', t)
if 'PoseStack.Pose normalMatrix' in t:
    print('  ok [WRU decls]')
else:
    print('  FALHOU [WRU decls]')
    FAILS.append('WRU decls')
save(p, t)

# ============================================================
# B) ImageButton -> MNAImageButton
# ============================================================
print('== ImageButton ==')
# B1) ctor de 9 args no MNAImageButton
p = ROOT + 'gui/widgets/MNAImageButton.java'
t = load(p)
if 'int hoverOffsetV, ResourceLocation texture, Button.OnPress onPress)' not in t.replace('\r\n', '\n'):
    old = '''    public MNAImageButton(int x, int y, int width, int height,
                          int u, int v, int hoverOffsetV,
                          ResourceLocation texture, int texWidth, int texHeight,
                          Button.OnPress onPress) {'''
    new = '''    /** Variante antiga sem texWidth/texHeight (assume 256x256). */
    public MNAImageButton(int x, int y, int width, int height,
                          int u, int v, int hoverOffsetV,
                          ResourceLocation texture, Button.OnPress onPress) {
        this(x, y, width, height, u, v, hoverOffsetV, texture, 256, 256, onPress, Component.empty());
    }

    public MNAImageButton(int x, int y, int width, int height,
                          int u, int v, int hoverOffsetV,
                          ResourceLocation texture, int texWidth, int texHeight,
                          Button.OnPress onPress) {'''
    if old in t:
        t = t.replace(old, new)
        print('  ok [MNAImageButton 9-arg ctor]')
    else:
        print('  FALHOU [MNAImageButton 9-arg ctor]')
        FAILS.append('MNAImageButton ctor')
    save(p, t)

# B2) swap extends
swap_files = []
for p in all_java():
    t = load(p)
    if 'extends ImageButton' not in t:
        continue
    if p.endswith('MNAImageButton.java'):
        continue
    t = rep(t, 'extends ImageButton', 'extends MNAImageButton', 'swap ' + os.path.basename(p))
    # imports
    imp_old = 'import net.minecraft.client.gui.components.ImageButton;'
    if imp_old in t:
        if 'gui/widgets/' in p.replace('\\', '/'):
            t = t.replace(imp_old + '\r\n', '').replace(imp_old + '\n', '')
            print('  -import (mesmo pacote) ' + os.path.basename(p))
        else:
            t = t.replace(imp_old, 'import com.mna.gui.widgets.MNAImageButton;')
            print('  ~import ' + os.path.basename(p))
    save(p, t)
    swap_files.append(p)

print('  arquivos trocados:', len(swap_files))

# B3) scan de sobras ImageButton nos arquivos trocados
print('  -- sobras ImageButton:')
for p in swap_files:
    t = load(p)
    for ln, line in enumerate(t.split('\n'), 1):
        if re.search(r'\bImageButton\b', line) and 'MNAImageButton' not in line:
            print('    ', p, ':', ln, '->', line.strip()[:100])

# ============================================================
# C) GuiGuideBook: widget.active/visible precisam de cast
# ============================================================
print('== GuiGuideBook ==')
p = ROOT + 'gui/item/GuiGuideBook.java'
t = load(p)
t2 = re.sub(r'(?<![)\w])widget\.(active|visible) = (true|false);', r'((AbstractWidget)widget).\1 = \2;', t)
c = len(re.findall(r'\(\(AbstractWidget\)widget\)\.(active|visible) = ', t2)) - len(re.findall(r'\(\(AbstractWidget\)widget\)\.(active|visible) = ', t))
print('  casts adicionados:', c)
save(p, t2)

# ============================================================
# D) Armaduras
# ============================================================
print('== ArmorMaterial ==')
# D1) MAArmorMaterial vira enum de dados
p = ROOT + 'items/armor/MAArmorMaterial.java'
t = load(p)
t = rep(t, 'public enum MAArmorMaterial implements ArmorMaterial', 'public enum MAArmorMaterial', 'enum decl')
t = rep(t, 'private final SoundEvent soundEvent;', 'private final net.minecraft.core.Holder<SoundEvent> soundEvent;', 'sound field')
t = rep(t, 'private MAArmorMaterial(String name, int maxDamageFactor, int[] damageReductionAmountArray, int enchantability, SoundEvent soundEvent, float toughness, float knockbackResistance, Supplier<Ingredient> repairMaterial) {',
        'private MAArmorMaterial(String name, int maxDamageFactor, int[] damageReductionAmountArray, int enchantability, net.minecraft.core.Holder<SoundEvent> soundEvent, float toughness, float knockbackResistance, Supplier<Ingredient> repairMaterial) {', 'ctor param')
t = rep(t, 'public SoundEvent getEquipSound() {', 'public net.minecraft.core.Holder<SoundEvent> getEquipSound() {', 'sound getter')
save(p, t)

# D2) ArmorMaterialInit
p = ROOT + 'items/armor/ArmorMaterialInit.java'
if not os.path.exists(p):
    save(p, '''package com.mna.items.armor;

import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * [PORT 1.21.1] {@code ArmorMaterial} virou um record data-driven registrado.
 * Os valores antigos do enum {@link MAArmorMaterial} sao convertidos aqui.
 */
public class ArmorMaterialInit {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, "mna");

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BROKEN = MATERIALS.register("broken", () -> build(MAArmorMaterial.BROKEN));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> INFUSED_SILK = MATERIALS.register("infused_silk", () -> build(MAArmorMaterial.INFUSED_SILK));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BONE = MATERIALS.register("bone", () -> build(MAArmorMaterial.BONE));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> DEMON = MATERIALS.register("demon", () -> build(MAArmorMaterial.DEMON));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FEY = MATERIALS.register("fey", () -> build(MAArmorMaterial.FEY));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> COUNCIL = MATERIALS.register("council", () -> build(MAArmorMaterial.COUNCIL));

    private static ArmorMaterial build(MAArmorMaterial mat) {
        Map<ArmorItem.Type, Integer> defense = Map.of(
                ArmorItem.Type.BOOTS, mat.getDefenseForType(ArmorItem.Type.BOOTS),
                ArmorItem.Type.LEGGINGS, mat.getDefenseForType(ArmorItem.Type.LEGGINGS),
                ArmorItem.Type.CHESTPLATE, mat.getDefenseForType(ArmorItem.Type.CHESTPLATE),
                ArmorItem.Type.HELMET, mat.getDefenseForType(ArmorItem.Type.HELMET));
        return new ArmorMaterial(
                defense,
                mat.getEnchantmentValue(),
                mat.getEquipSound(),
                mat::getRepairIngredient,
                List.of(new ArmorMaterial.Layer(ResourceLocation.parse(mat.getName()))),
                mat.getToughness(),
                mat.getKnockbackResistance());
    }
}
''')
    print('  criado ArmorMaterialInit.java')

# D3) DyeableMageArmor
p = ROOT + 'items/armor/DyeableMageArmor.java'
t = load(p)
t = rep(t, 'import net.minecraft.world.item.DyeableArmorItem;', 'import com.mna.api.tools.MNADyeableItem;', 'DM import')
t = rep(t, 'extends DyeableArmorItem\r\nimplements ISetItem,', 'extends ArmorItem\r\nimplements MNADyeableItem, ISetItem,', 'DM decl')
t = rep(t, 'public DyeableMageArmor(ArmorMaterial armorMaterial, ArmorItem.Type slot, Item.Properties properties, float manaPerRepairTick, float repairPerTick) {\n        super(armorMaterial, slot, properties);',
        'public DyeableMageArmor(net.minecraft.core.Holder<ArmorMaterial> armorMaterial, ArmorItem.Type slot, Item.Properties properties, float manaPerRepairTick, float repairPerTick) {\n        super(armorMaterial, slot, properties.durability(slot.getDurability(5)));', 'DM ctor')
t = rep(t, '''    public int getColor(ItemStack stack) {
        CompoundTag compoundnbt = stack.getTagElement("display");
        return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") : -13495757;
    }''', '''    public int getColor(ItemStack stack) {
        net.minecraft.world.item.component.DyedItemColor c = stack.get(net.minecraft.core.component.DataComponents.DYED_COLOR);
        return c != null ? c.rgb() : -13495757;
    }''', 'DM getColor')
save(p, t)

# D4) BrokenMageArmor
p = ROOT + 'items/armor/BrokenMageArmor.java'
t = load(p)
t = rep(t, 'import net.minecraft.world.item.DyeableArmorItem;', 'import com.mna.api.tools.MNADyeableItem;', 'BM import')
t = rep(t, 'extends DyeableArmorItem\r\nimplements ITieredItem<BrokenMageArmor>,', 'extends ArmorItem\r\nimplements MNADyeableItem, ITieredItem<BrokenMageArmor>,', 'BM decl')
t = rep(t, 'super((ArmorMaterial)MAArmorMaterial.BROKEN, slot, new Item.Properties().stacksTo(1).setNoRepair());',
        'super(ArmorMaterialInit.BROKEN, slot, new Item.Properties().stacksTo(1).setNoRepair().durability(slot.getDurability(5)));', 'BM ctor')
t = rep(t, '''        if (other.getItem() instanceof DyeableArmorItem) {
            ((DyeableArmorItem)other.getItem()).setColor(outputStack, ((DyeableArmorItem)other.getItem()).getColor(other));
        }''', '''        if (other.getItem() instanceof MNADyeableItem) {
            MNADyeableItem.setColor(outputStack, MNADyeableItem.getColor(other));
        }''', 'BM convertFrom')
save(p, t)

# D5) Bone/Council/Demon/Fey
for name in ['BoneArmorItem', 'CouncilArmorItem', 'DemonArmorItem', 'FeyArmorItem']:
    p = ROOT + 'items/armor/' + name + '.java'
    t = load(p)
    t = rep(t, 'public ' + name + '(ArmorMaterial materialIn, ArmorItem.Type slot, Item.Properties builder) {',
            'public ' + name + '(net.minecraft.core.Holder<ArmorMaterial> materialIn, ArmorItem.Type slot, Item.Properties builder) {', name + ' ctor')
    t = rep(t, 'super(materialIn, slot, builder.rarity(Rarity.EPIC));',
            'super(materialIn, slot, builder.rarity(Rarity.EPIC).durability(slot.getDurability(33)));', name + ' super')
    save(p, t)

# D6) ItemInit
p = ROOT + 'items/ItemInit.java'
t = load(p)
n = t.count('MAArmorMaterial.')
t = t.replace('MAArmorMaterial.', 'ArmorMaterialInit.')
print('  ItemInit refs trocadas:', n)
if 'import com.mna.items.armor.ArmorMaterialInit;' not in t:
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    idx = None
    for i, l in enumerate(lines):
        if l.startswith('import '):
            idx = i
    lines.insert(idx + 1, 'import com.mna.items.armor.ArmorMaterialInit;')
    t = sep.join(lines)
    print('  +import ArmorMaterialInit')
save(p, t)

# D7) registro no mod bus
p = 'src/main/java/com/mna/ManaAndArtifice.java'
t = load(p)
t = rep(t, '        ItemInit.ITEMS.register(this.modEventBus);',
        '        ItemInit.ITEMS.register(this.modEventBus);\r\n        com.mna.items.armor.ArmorMaterialInit.MATERIALS.register(this.modEventBus);', 'bus register')
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
