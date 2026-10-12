# _fix5.py - lote 5: damageItem (Consumer<Item>), RecordItem->Item+JukeboxSong,
# render->renderWidget (final em AbstractWidget), AttributeModifier(String)
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
    if idx is None:
        return t
    lines.insert(idx + 1, line)
    print('  +import ' + imp)
    return sep.join(lines)


def rename_line(path, lineno, expected, new_line, tag):
    """Troca a linha `lineno` (1-based) se ela contiver `expected`."""
    t = load(path)
    sep = '\r\n' if '\r\n' in t else '\n'
    lines = t.split(sep)
    i = lineno - 1
    ok = False
    for probe in (i, i - 1, i + 1, i - 2, i + 2):
        if 0 <= probe < len(lines) and expected in lines[probe]:
            lines[probe] = new_line
            ok = True
            break
    if ok:
        save(path, sep.join(lines))
        print('  ok [' + tag + ']')
    else:
        print('  FALHOU [' + tag + '] (linha nao encontrada)')
        FAILS.append(tag)


# ============================================================
# A) damageItem: Consumer<T> -> Consumer<Item> (assinatura NeoForge)
# ============================================================
print('== damageItem ==')
A_FILES = ['api/items/ChargeableItem.java', 'items/armor/BoneArmorItem.java', 'items/armor/BrokenMageArmor.java',
           'items/armor/CouncilArmorItem.java', 'items/armor/DemonArmorItem.java', 'items/armor/DyeableMageArmor.java',
           'items/armor/FeyArmorItem.java', 'items/armor/IBrokenArmorReplaceable.java']
for rel in A_FILES:
    p = ROOT + rel
    t = load(p)
    if 'Consumer<T> onBroken' not in t:
        continue
    t = t.replace('Consumer<T> onBroken', 'Consumer<Item> onBroken')
    t = add_import(t, 'net.minecraft.world.item.Item')
    save(p, t)
    print('  ok damageItem ' + rel)

# ============================================================
# B) RecordItem -> Item + jukeboxPlayable
# ============================================================
print('== RecordItem ==')
p = ROOT + 'items/ItemInit.java'
t = load(p)
t = rep(t, 'import net.minecraft.world.item.RecordItem;\n', '', 'remove import RecordItem')
t = rep(t, 'public static final DeferredHolder<Item, ? extends RecordItem> DANCE_MIX = ITEMS.register("strange_record", () -> new RecordItem(13, () -> Music.Construct.DANCE_MIX, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 3293));',
        'public static final DeferredHolder<Item, ? extends Item> DANCE_MIX = ITEMS.register("strange_record", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.JUKEBOX_SONG, com.mna.api.tools.RLoc.create("strange_record")))));',
        'DANCE_MIX')
save(p, t)

# ============================================================
# C) render -> renderWidget
# ============================================================
print('== render->renderWidget ==')
SIG4 = '    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {'
SIG8 = '        public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {'
rename_line(ROOT + 'gui/widgets/ReagentList.java', 128, 'public void render(GuiGraphics', SIG4.replace('    public void render', '    public void renderWidget'), 'ReagentList')
rename_line(ROOT + 'gui/widgets/SpellPartList.java', 145, 'public void render(GuiGraphics', SIG4.replace('    public void render', '    public void renderWidget'), 'SpellPartList')
rename_line(ROOT + 'gui/block/GuiLodestarV2.java', 835, 'public void render(GuiGraphics', SIG8.replace('        public void render', '        public void renderWidget'), 'CommandScrollList')
rename_line(ROOT + 'gui/block/GuiOcculus.java', 601, 'public void render(GuiGraphics',
            '        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {', 'OcculusEntryList')
rename_line(ROOT + 'gui/entity/GuiWanderingWizard.java', 188, 'public void render(GuiGraphics', SIG8.replace('        public void render', '        public void renderWidget'), 'WanderingWizardOffersList')
rename_line(ROOT + 'gui/block/GuiRunescribingTable.java', 249, 'public void render(GuiGraphics',
            '        public void renderWidget(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {', 'ChiselImageButton')

# ChiselImageButton: this.renderWidget (recursao) -> super.renderWidget
p = ROOT + 'gui/block/GuiRunescribingTable.java'
t = load(p)
t = rep(t, 'this.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);', 'super.renderWidget(pGuiGraphics, mouseX, mouseY, partialTicks);', 'Chisel super')
save(p, t)

# Checkbox (GuiFilterItem)
p = ROOT + 'gui/item/GuiFilterItem.java'
t = load(p)
t = rep(t, '''        public void render(GuiGraphics pGuiGraphics, int mouse_x, int mouse_y, float partial_tick) {
            super.render(pGuiGraphics, mouse_x, mouse_y, partial_tick);''',
        '''        public void renderWidget(GuiGraphics pGuiGraphics, int mouse_x, int mouse_y, float partial_tick) {
            super.renderWidget(pGuiGraphics, mouse_x, mouse_y, partial_tick);''', 'Checkbox')
save(p, t)

# ImageButtonWithAlphaBlend
p = ROOT + 'gui/widgets/ImageButtonWithAlphaBlend.java'
t = load(p)
t = rep(t, '''    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend();
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        RenderSystem.disableBlend();
    }''', '''    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableBlend();
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        RenderSystem.disableBlend();
    }''', 'AlphaBlend')
save(p, t)

# CategoryItemButton (GuiGuideBook): remover o render() e portar o gate p/ renderWidget
p = ROOT + 'gui/item/GuiGuideBook.java'
t = load(p)
t = rep(t, '''        public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
            if (GuiGuideBook.this.page == this.page || GuiGuideBook.this.page == this.page - 1) {
                super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
            }
        }

''', '', 'CategoryItemButton render')
t = rep(t, '        public void renderWidget(GuiGraphics pGuiGraphics, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {',
        '''        public void renderWidget(GuiGraphics pGuiGraphics, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_) {
            if (GuiGuideBook.this.page != this.page && GuiGuideBook.this.page != this.page - 1) {
                return;
            }''', 'CategoryItemButton gate')
save(p, t)

# ============================================================
# D) new AttributeModifier("string", ...) -> ResourceLocation.parse
# ============================================================
print('== AttributeModifier strings ==')
for rel in ['entities/boss/WitherLich.java', 'entities/faction/base/BaseFlyingFactionMob.java', 'items/armor/DemonArmorItem.java']:
    p = ROOT + rel
    t = load(p)
    t2 = re.sub(r'new AttributeModifier\("([^"]+)"', r'new AttributeModifier(ResourceLocation.parse("\1")', t)
    if t2 != t:
        t2 = add_import(t2, 'net.minecraft.resources.ResourceLocation')
        save(p, t2)
        print('  ok ' + rel)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
