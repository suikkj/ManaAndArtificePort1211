# _fix33.py - placeLiquid delegations removidas, getBlockEfficiency helper, getLootData->reloadableRegistries,
#             getTooltipLines 3-arg, Fluid var
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


S = 'src/main/java/com/mna/'

print('== placeLiquid ==')
n = 0
for p in ['api/blocks/WaterloggableBlock', 'blocks/artifice/FluidJugBlock', 'blocks/ritual/ChalkRuneBlock',
          'blocks/sorcery/SpectralAnvilBlock', 'blocks/sorcery/SpectralCraftingTableBlock',
          'blocks/sorcery/SpectralStonecutterBlock', 'blocks/sorcery/SpectralWebBlock']:
    fp = S + p + '.java'
    t = load(fp)
    t2, c = re.subn(r'\r?\n\s*public boolean placeLiquid\(LevelAccessor worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn\) \{\r?\n\s*return super\.placeLiquid\(worldIn, pos, state, fluidStateIn\);\r?\n\s*\}', '', t)
    if c:
        save(fp, t2)
        n += c
    print('  ' + p + ': ' + str(c))
print('  total:', n)

print('== MNAEnchantmentHelper.getBlockEfficiency ==')
p = S + 'api/enchantments/MNAEnchantmentHelper.java'
t = load(p)
if 'getBlockEfficiency' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** [PORT 1.21.1] EnchantmentHelper.getBlockEfficiency(LivingEntity) foi removido. */
    public static int getBlockEfficiency(net.minecraft.world.entity.LivingEntity entity) {
        return (int)EnchantmentHelper.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.EFFICIENCY, entity);
    }
''' + t[idx:]
    save(p, t)
    print('  ok helper')

print('== globais ==')
total = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = t.replace('EnchantmentHelper.getBlockEfficiency(', 'com.mna.api.enchantments.MNAEnchantmentHelper.getBlockEfficiency(')
        t = t.replace('.getLootData().getLootTable(', '.reloadableRegistries().getLootTable(')
        t = re.sub(r'\.getTooltipLines\(([^,()]+(?:\.[\w]+)*), \(TooltipFlag\)',
                   r'.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, \1, (TooltipFlag)', t)
        t = re.sub(r'\.getTooltipLines\(\(Player\)([\w.()]+), \(TooltipFlag\)',
                   r'.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, (Player)\1, (TooltipFlag)', t)
        t = re.sub(r'\.getTooltipLines\(([\w.]+\(\)), \(TooltipFlag\)',
                   r'.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, \1, (TooltipFlag)', t)
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print('== ConstructIsFluidInContainer ==')
p = S + 'entities/constructs/ai/conditionals/ConstructIsFluidInContainer.java'
t = load(p)
old = 'FlowingFluid fluid = state.getBlock() == Blocks.LAVA_CAULDRON ? Fluids.LAVA : (state.getBlock() == Blocks.WATER_CAULDRON ? Fluids.WATER : Fluids.EMPTY);'
new = 'net.minecraft.world.level.material.Fluid fluid = state.getBlock() == Blocks.LAVA_CAULDRON ? Fluids.LAVA : (state.getBlock() == Blocks.WATER_CAULDRON ? Fluids.WATER : Fluids.EMPTY);'
if old in t:
    t = t.replace(old, new)
    save(p, t)
    print('  ok')
else:
    print('  FALHOU')
    FAILS.append('fluid')

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
