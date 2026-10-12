# _fix51.py - EVENT_BUS.post bool, MultiblockRenderer, MACapability, BlockUtils, GuideBookEntries
import os
import re

S = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


print('== A. EVENT_BUS.post boolean (global) ==')
n = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = re.sub(r'^([ \t]*)if \(NeoForge\.EVENT_BUS\.post\(([^)]+)\)\) \{$',
                   r'\1NeoForge.EVENT_BUS.post(\2);\n\1if (\2.isCanceled()) {', t, flags=re.M)
        if t != o:
            save(p, t)
            n += 1
print('  arquivos:', n)

print('== B. MultiblockRenderer ==')
p = S + 'tools/render/MultiblockRenderer.java'
t = load(p)
o = t
t = t.replace('import net.minecraftforge.event.TickEvent;\r\n', '')
t = t.replace('import net.minecraftforge.fml.util.ObfuscationReflectionHelper;', 'import net.neoforged.fml.util.ObfuscationReflectionHelper;')
t = t.replace('import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;', 'import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;')
t = t.replace('CustomizeGuiOverlayEvent.Post', 'RenderGuiLayerEvent.Post')
t = t.replace('if (((Boolean)mc.options.forceUnicodeFont.get()).booleanValue()) {', 'if (mc.options.forceUnicodeFont) {')
t = t.replace('block.getCloneItemStack(lookingState, mc.hitResult, (BlockGetter)mc.level, lookingPos, (Player)mc.player)',
              'block.getCloneItemStack((net.minecraft.world.level.LevelReader)mc.level, lookingPos, lookingState)')
t = t.replace('timeComplete == 0 ? 0.0f : event.getPartialTick()',
              'timeComplete == 0 ? 0.0f : event.getPartialTick().getGameTimeDeltaPartialTick(false)')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== C. MACapabilityForgeEventHandlers ==')
p = S + 'capabilities/MACapabilityForgeEventHandlers.java'
t = load(p)
o = t
# deletar metodos attach
i = t.find('attachEntityCapability')
if i > 0:
    i = t.rfind('@SubscribeEvent', 0, i)
    j = t.find('onPlayerClone')
    j = t.rfind('@SubscribeEvent', 0, j)
    if i > 0 and j > i:
        t = t[:i] + t[j:]
t = t.replace('import net.neoforged.neoforge.event.AttachCapabilitiesEvent;\r\n', '')
t = re.sub(r'import net\.neoforged\.neoforge\.capabilities\.ICapabilityProvider;\r?\n', '', t)
t = t.replace('        original.reviveCaps();\r\n', '')
t = t.replace('        event.getOriginal().invalidateCaps();\r\n', '')
t = t.replace('TickEvent.PlayerTickEvent', 'net.neoforged.neoforge.event.tick.PlayerTickEvent.Post')
t = t.replace('''        if (event.phase == TickEvent.Phase.START) {
            return;
        }
'''.replace('\n', '\r\n'), '')
t = t.replace('if (event.side == LogicalSide.SERVER) {', 'if (!event.getEntity().level().isClientSide()) {')
t = t.replace('MNACap.get(event.player, ', 'MNACap.get(event.getEntity(), ')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== D. BlockUtils ==')
p = S + 'tools/BlockUtils.java'
t = load(p)
o = t
t = t.replace('BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(', 'com.mna.api.enchantments.MNAEnchantmentRegistry.holder(')
t = t.replace('stack.enchant(Enchantments.SILK_TOUCH, 1);', 'stack.enchant(com.mna.api.enchantments.MNAEnchantmentRegistry.holder(Enchantments.SILK_TOUCH), 1);')
t = t.replace('state.getExpDrop((LevelReader)world, world.getRandom(), pos, 0, 0)', '0')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== E. MNAAdvancementAccess (novo) ==')
p = S + 'tools/MNAAdvancementAccess.java'
if not os.path.exists(p):
    save(p, '''package com.mna.tools;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;

/**
 * [PORT 1.21.1] ClientAdvancements.progress virou private. Acesso via reflexao.
 */
public class MNAAdvancementAccess {

    @SuppressWarnings("unchecked")
    public static AdvancementProgress getProgress(ClientAdvancements ca, AdvancementHolder holder) {
        try {
            java.lang.reflect.Field f = ClientAdvancements.class.getDeclaredField("progress");
            f.setAccessible(true);
            java.util.Map<AdvancementHolder, AdvancementProgress> map =
                (java.util.Map<AdvancementHolder, AdvancementProgress>) f.get(ca);
            return map == null ? null : map.get(holder);
        } catch (Throwable t) {
            return null;
        }
    }
}
''')
    print('  ok')
else:
    print('  já existe')

print('== F. GuideBookEntries ==')
p = S + 'guide/GuideBookEntries.java'
t = load(p)
o = t
old = '''            Advancement adv = clientAdvancements.getAdvancements().get(advancementID);
            if (adv == null) {
                return false;
            }
            AdvancementProgress advProgress = (AdvancementProgress)clientAdvancements.progress.get(adv);'''
new = '''            net.minecraft.advancements.AdvancementHolder adv = clientAdvancements.get(advancementID);
            if (adv == null) {
                return false;
            }
            AdvancementProgress advProgress = com.mna.tools.MNAAdvancementAccess.getProgress(clientAdvancements, adv);'''
t = t.replace(old, new)
t = re.sub(r'for \(Map\.Entry (\w+) : (\w+)\.entrySet\(\)\) \{',
           r'for (Object __e : \2.entrySet()) {\n                    Map.Entry \1 = (Map.Entry)__e;', t)
t = t.replace('((Enchantment)ench.getKey()).getFullname(((Integer)ench.getValue()).intValue())',
              'Enchantment.getFullname((net.minecraft.core.Holder<Enchantment>)ench.getKey(), ((Integer)ench.getValue()).intValue())')
t = t.replace('.map(r -> new GuidebookSearchResult((RelatedRecipe)r.getKey(), (ItemStack)r.getValue()))',
              '.map((Map.Entry r) -> new GuidebookSearchResult((RelatedRecipe)r.getKey(), (ItemStack)r.getValue()))')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')
print()
print('FIM')
