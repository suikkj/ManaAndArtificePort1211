# _fix48.py - lote grande: Lodestar node type, MNAImageButton.resourceLocation, maxUpStep,
#             isBeneficial().value(), event.getLevel(), getTimer partialTick, Enchantments.FORTUNE,
#             MNAEnchantmentHelper doPost*/hasChanneling, spawn data RegistryFriendlyByteBuf,
#             writeItemStack -> OPTIONAL_STREAM_CODEC, codec() additions
import os
import re

S = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


print('== A. GuiLodestarV2 Object node ==')
p = S + 'gui/block/GuiLodestarV2.java'
t = load(p)
imported = 'import net.minecraft.client.gui.components.AbstractWidget;' in t
newtype = 'AbstractWidget' if imported else 'net.minecraft.client.gui.components.AbstractWidget'
t2 = t.replace('Object node;', newtype + ' node;')
if t2 != t:
    save(p, t2)
    print('  ok (import?', imported, ')')
else:
    print('  FALHOU')

print('== B. MNAImageButton resourceLocation ==')
p = S + 'gui/widgets/MNAImageButton.java'
t = load(p)
o = t
t = t.replace('private final ResourceLocation texture;', 'protected final ResourceLocation resourceLocation;')
t = t.replace('this.texture = texture;', 'this.resourceLocation = texture;')
t = t.replace('this.texture', 'this.resourceLocation')
if t != o:
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')

print('== C. globais ==')
n = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = t.replace('.getStepHeight()', '.maxUpStep()')
        t = t.replace('getEffect().isBeneficial()', 'getEffect().value().isBeneficial()')
        t = t.replace('.getFrameTime()', '.getTimer().getGameTimeDeltaPartialTick(false)')
        t = t.replace('stack.enchant(Enchantments.BLOCK_FORTUNE, ', 'stack.enchant(BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(Enchantments.BLOCK_FORTUNE), ')
        t = t.replace('Enchantments.BLOCK_FORTUNE', 'Enchantments.FORTUNE')
        t = t.replace('EnchantmentHelper.getItemEnchantmentLevel(', 'com.mna.api.enchantments.MNAEnchantmentHelper.getItemEnchantmentLevel(')
        t = t.replace('EnchantmentHelper.doPostHurtEffects(', 'com.mna.api.enchantments.MNAEnchantmentHelper.doPostHurtEffects(')
        t = t.replace('EnchantmentHelper.doPostDamageEffects(', 'com.mna.api.enchantments.MNAEnchantmentHelper.doPostDamageEffects(')
        t = t.replace('EnchantmentHelper.hasChanneling(', 'com.mna.api.enchantments.MNAEnchantmentHelper.hasChanneling(')
        if f in ('DelayedEventQueue.java', 'CommonEventHandler.java'):
            t = re.sub(r'\bevent\.level\b', 'event.getLevel()', t)
        if t != o:
            save(p, t)
            n += 1
print('  arquivos:', n)

print('== D. helpers MNAEnchantmentHelper ==')
p = S + 'api/enchantments/MNAEnchantmentHelper.java'
t = load(p)
add = ''
if 'doPostHurtEffects' not in t:
    add += '''
    /** [PORT 1.21.1] EnchantmentHelper.doPostHurtEffects removido -> no-op. */
    public static void doPostHurtEffects(net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.Entity attacker) {
    }

    /** [PORT 1.21.1] EnchantmentHelper.doPostDamageEffects removido -> no-op. */
    public static void doPostDamageEffects(net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.Entity attacker) {
    }

    /** [PORT 1.21.1] EnchantmentHelper.hasChanneling removido -> false (comportamento via enchantment effect). */
    public static boolean hasChanneling(ItemStack stack) {
        return false;
    }
'''
if add:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + add + t[idx:]
    save(p, t)
    print('  ok')
else:
    print('  já tinha')

print('== E. spawn data ==')
n = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = t.replace('writeSpawnData(FriendlyByteBuf ', 'writeSpawnData(net.minecraft.network.RegistryFriendlyByteBuf ')
        t = t.replace('readSpawnData(FriendlyByteBuf ', 'readSpawnData(net.minecraft.network.RegistryFriendlyByteBuf ')
        t = re.sub(r'(\w+)\.writeItemStack\(([^,]+), (?:true|false)\);', r'net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(\1, \2);', t)
        if t != o:
            save(p, t)
            n += 1
print('  arquivos:', n)

print('== F. codec() additions ==')
codec_simple = '''
    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return simpleCodec(%s::new);
    }
'''
codec_unit = '''
    @Override
    protected com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.block.Block> codec() {
        return com.mojang.serialization.MapCodec.unit(this);
    }
'''
loot_unit = '''
    @Override
    public com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.storage.loot.functions.LootItemFunction> codec() {
        return com.mojang.serialization.MapCodec.unit(this);
    }
'''
targets = [
    (S + 'blocks/worldgen/MAFlowerBlock.java', codec_simple % 'MAFlowerBlock'),
    (S + 'blocks/worldgen/MATarmaRootBlock.java', codec_simple % 'MATarmaRootBlock'),
    (S + 'blocks/decoration/ClayMugBlock.java', codec_unit),
    (S + 'blocks/artifice/FluidJugBlock.java', codec_unit),
    (S + 'blocks/sorcery/HellfireBlock.java', codec_unit),
    (S + 'loot/modifiers/AddChimeriteModifier.java', loot_unit),
    (S + 'loot/modifiers/BeheadingModifier.java', loot_unit),
]
for p, block in targets:
    t = load(p)
    if 'codec()' in t and 'MapCodec' in t:
        print('  já tinha:', os.path.basename(p))
        continue
    idx = t.rstrip().rfind('}')
    t = t[:idx] + block + t[idx:]
    save(p, t)
    print('  ok', os.path.basename(p))
print()
print('FIM')
