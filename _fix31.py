# _fix31.py - ICancellableEvent nos eventos MNA, setResult->setCanceled (NeoForge),
#             broadcastBreakEvent->EquipmentSlot, doEnchantDamageEffects no-op, getOrCreateTagElement helper,
#             Tier.getLevel helper, brewing via RegisterBrewingRecipesEvent
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    if old not in t and '\r\n' in t:
        old2 = old.replace('\n', '\r\n')
        if old2 in t:
            old, new = old2, new.replace('\n', '\r\n')
    if old not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + ']')
    return t.replace(old, new)


S = 'src/main/java/com/mna/'
ICE = 'net.neoforged.bus.api.ICancellableEvent'

print('== helpers ==')
p = S + 'tools/MATools.java'
t = load(p)
if 'doEnchantDamageEffects' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** [PORT 1.21.1] doEnchantDamageEffects foi removido; efeitos de encantamento passam pelo pipeline de dano. */
    public static void doEnchantDamageEffects(net.minecraft.world.entity.LivingEntity attacker, net.minecraft.world.entity.Entity target) {
    }

    /** [PORT 1.21.1] Tier.getLevel() removido (Tiers virou enum). Mapeia tier -> nivel vanilla. */
    public static int getTierLevel(net.minecraft.world.item.Tier tier) {
        if (tier == net.minecraft.world.item.Tiers.WOOD) return 1;
        if (tier == net.minecraft.world.item.Tiers.GOLD) return 2;
        if (tier == net.minecraft.world.item.Tiers.STONE) return 2;
        if (tier == net.minecraft.world.item.Tiers.IRON) return 3;
        if (tier == net.minecraft.world.item.Tiers.DIAMOND) return 4;
        if (tier == net.minecraft.world.item.Tiers.NETHERITE) return 5;
        return 0;
    }
''' + t[idx:]
    save(p, t)
    print('  ok [MATools helpers]')

p = S + 'api/tools/MNAItemData.java'
t = load(p)
if 'getOrCreateTagElement' not in t:
    idx = t.rstrip().rfind('}')
    t = t[:idx] + '''
    /** Equivalente ao antigo {@code stack.getOrCreateTagElement(key)} (escritas persistem). */
    public static CompoundTag getOrCreateTagElement(ItemStack stack, String key) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        CompoundTag element = tag.getOrCreate(key);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return element;
    }
''' + t[idx:]
    save(p, t)
    print('  ok [MNAItemData helper]')

print('== eventos MNA: ICancellableEvent ==')
for f in ['api/events/construct/ConstructSprayTargetingEvent', 'api/events/EnderfeatherCharmUsedEvent', 'api/events/ManaweaveCraftingEvent',
          'api/events/ManaweavePatternDrawnEvent', 'api/events/PlayerMagicLevelUpEvent', 'api/events/RuneforgeCraftingEvent',
          'api/events/RuneforgeEnchantEvent', 'api/events/RunescribeCraftingEvent', 'api/events/SpellCastEvent']:
    p = S + f + '.java'
    t = load(p)
    t = rep(t, 'extends Event {', 'extends Event implements ' + ICE + ' {', os.path.basename(f))
    save(p, t)

print('== globais ==')
total = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = t.replace('.getOrCreateTagElement(', '.getOrCreateTagElement(')  # placeholder
        t = re.sub(r'([\w.()]+)\.getOrCreateTagElement\(', r'com.mna.api.tools.MNAItemData.getOrCreateTagElement(\1, ', t)
        t = re.sub(r'\.hurtAndBreak\((\d+), \(net\.minecraft\.server\.level\.ServerLevel\)(\w+)\.level\(\), (\w+), \w+ -> \w+\.broadcastBreakEvent\((\w+)\)\);',
                   r'.hurtAndBreak(\1, \3, \4 == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);', t)
        t = re.sub(r'([\w.()]+)\.broadcastBreakEvent\((\w+)\)',
                   r'\1.broadcastBreakEvent(\2 == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND)', t)
        t = re.sub(r'this\.doEnchantDamageEffects\(\(LivingEntity\)this, (\(Entity\))?(\w+)\);', r'com.mna.tools.MATools.doEnchantDamageEffects(this, \2);', t)
        t = t.replace('this.getMaterial().getEquivalentTier().getLevel()', 'com.mna.tools.MATools.getTierLevel(this.getMaterial().getEquivalentTier())')
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print('== setResult (sites NeoForge) ==')
for p, lines in [
    (S + 'capabilities/MACapabilityForgeEventHandlers.java', ['248']),
    (S + 'entities/renderers/living/LivingRenderEvents.java', ['28', '34', '37']),
    (S + 'entities/renderers/player/PlayerRenderEvents.java', ['32']),
    (S + 'events/ClientEventHandler.java', ['587']),
]:
    t = load(p)
    arr = t.split('\n')
    for ln in lines:
        i = int(ln) - 1
        if 'setResult(Result.DENY)' in arr[i]:
            arr[i] = arr[i].replace('setResult(Result.DENY)', 'setCanceled(true)')
            print('  ok [' + os.path.basename(p) + ':' + ln + ']')
        else:
            print('  FALHOU [' + os.path.basename(p) + ':' + ln + '] -> ' + arr[i].strip()[:60])
            FAILS.append(p + ln)
    save(p, '\n'.join(arr))

print('== DamageEventHandler:461 ==')
p = S + 'events/DamageEventHandler.java'
t = load(p)
t = rep(t, '''            if (!event.isCanceled()) {
                MobEffectInstance effect;''',
        '''            if (event.getNewDamage() > 0) {
                MobEffectInstance effect;''', 'dmg-461')
save(p, t)

print('== BrewingInit ==')
p = S + 'brewing/BrewingInit.java'
t = load(p)
t = rep(t, 'public static void initBrewingRecipes(FMLCommonSetupEvent event) {',
        'public static void initBrewingRecipes(net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent event) {', 'brew-sig')
t = re.sub(r'BrewingRecipeRegistry\.addRecipe\(\(IBrewingRecipe\)new BrewingRecipe\((.*)\)\);', r'event.getBuilder().addRecipe(\1);', t)
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
