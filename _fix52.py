# _fix52.py - MACapability LF fixes, ManaAndArtifice ctor/configs, SpellConfig/APIBridge container,
#             ClientEventHandler parens/scroll/noJumpDelay/CLOUDSTEP, VillagerRegistry enchants
import os
import re

S = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rl(t, old, new):
    """replace line-ending agnostic for a single logical text block"""
    return t.replace(old, new).replace(old.replace('\n', '\r\n'), new.replace('\n', '\r\n'))


print('== A. MACapabilityForgeEventHandlers ==')
p = S + 'capabilities/MACapabilityForgeEventHandlers.java'
t = load(p)
o = t
t = t.replace('import net.neoforged.neoforge.event.AttachCapabilitiesEvent;\n', '').replace('import net.neoforged.neoforge.event.AttachCapabilitiesEvent;\r\n', '')
t = t.replace('        original.reviveCaps();\n', '').replace('        original.reviveCaps();\r\n', '')
t = t.replace('        event.getOriginal().invalidateCaps();\n', '').replace('        event.getOriginal().invalidateCaps();\r\n', '')
t = rl(t, '        if (event.phase == TickEvent.Phase.START) {\n            return;\n        }\n', '')
t = t.replace('event.player', 'event.getEntity()')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== B. ManaAndArtifice ==')
p = S + 'ManaAndArtifice.java'
t = load(p)
o = t
t = t.replace('import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;\n', '').replace('import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;\r\n', '')
t = t.replace('    public final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();', '    public final IEventBus modEventBus;')
t = t.replace('    public ManaAndArtifice() {', '    public ManaAndArtifice(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {\n        this.modEventBus = modEventBus;')
t = t.replace('ModLoadingContext.get().registerConfig(', 'modContainer.registerConfig(')
t = t.replace('        GeckoLib.initialize();\n', '').replace('        GeckoLib.initialize();\r\n', '')
t = t.replace('living != null ? living : stack.getEntityRepresentation()', 'living != null ? living : (LivingEntity)stack.getEntityRepresentation()')
t = t.replace('APIBridge.earlyInit();', 'APIBridge.earlyInit(modContainer);')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== C. APIBridge + SpellConfig ==')
p = S + 'apibridge/APIBridge.java'
t = load(p)
o = t
t = t.replace('public static void earlyInit() {', 'public static void earlyInit(net.neoforged.fml.ModContainer container) {')
t = t.replace('SpellConfig.finalizeServerConfig();', 'SpellConfig.finalizeServerConfig(container);')
if t != o:
    save(p, t)
    print('  ok APIBridge')
p = S + 'config/SpellConfig.java'
t = load(p)
o = t
t = t.replace('public static void finalizeServerConfig() {', 'public static void finalizeServerConfig(net.neoforged.fml.ModContainer container) {')
t = t.replace('ModLoadingContext.get().registerConfig(', 'container.registerConfig(')
if t != o:
    save(p, t)
    print('  ok SpellConfig')

print('== D. ClientEventHandler ==')
p = S + 'events/ClientEventHandler.java'
t = load(p)
o = t
t = t.replace('(LivingEntity)event.getEntity().distanceToSqr(', '((LivingEntity)event.getEntity()).distanceToSqr(')
t = t.replace('(LivingEntity)event.getEntity().getEffect(', '((LivingEntity)event.getEntity()).getEffect(')
t = t.replace('(float)event.getScrollDelta()', '(float)event.getScrollDeltaY()')
t = t.replace(' && mc.player.noJumpDelay == 0', '')
t = t.replace('EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.CLOUDSTEP, player)', 'EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.CLOUDSTEP, mc.player)')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== E. VillagerRegistry ==')
p = S + 'villagers/VillagerRegistry.java'
t = load(p)
o = t
t = t.replace('Registries.SpellEffect.getValues()', 'com.mna.Registries.SpellEffect.getValues()')
old = '''                HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
                if (r.nextBoolean()) {
                    enchants.put((Enchantment)EnchantmentInit.MANA_REPAIR.get(), 1);
                } else {
                    enchants.put(Enchantments.UNBREAKING, 3 + r.nextInt(3));
                }
                EnchantmentHelper.setEnchantments(enchants, (ItemStack)stack);'''
new = '''                it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<net.minecraft.core.Holder<Enchantment>> enchants = new it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<net.minecraft.core.Holder<Enchantment>>();
                if (r.nextBoolean()) {
                    enchants.put(EnchantmentInit.MANA_REPAIR, 1);
                } else {
                    enchants.put(Enchantments.UNBREAKING, 3 + r.nextInt(3));
                }
                EnchantmentHelper.setEnchantments((ItemStack)stack, new net.minecraft.world.item.enchantment.ItemEnchantments(enchants, true));'''
t = rl(t, old, new)
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')
print()
print('FIM')
