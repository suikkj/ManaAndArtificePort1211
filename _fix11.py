# _fix11.py - CommonEventHandler + globais: ItemStack.of, getEnchantmentLevel, MNADyeableItem
import os
import re

FAILS = []
CEH = 'src/main/java/com/mna/events/CommonEventHandler.java'
PROV = 'com.mna.api.tools.MNAItemData.provider()'


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


def all_java():
    out = []
    for dp, _, fns in os.walk('src/main/java'):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


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


def wrap_calls(t, needle, wrapper):
    """Acha needle(...) balanceado e troca por wrapper(args)."""
    out = []
    i = 0
    n = 0
    while True:
        k = t.find(needle, i)
        if k < 0:
            out.append(t[i:])
            break
        out.append(t[i:k])
        j = k + len(needle)
        depth = 1
        a = ''
        while depth > 0 and j < len(t):
            c = t[j]
            if c == '(':
                depth += 1
            elif c == ')':
                depth -= 1
            if depth > 0:
                a += c
            j += 1
        out.append(wrapper(a))
        i = j
        n += 1
    return ''.join(out), n


# ============================================================
# 1) CommonEventHandler
# ============================================================
print('== CommonEventHandler ==')
t = load(CEH)

t = rep(t, 'java.util.Optional<ChunkMagicProvider> chunkmagic = MNACap.get(chunkAccess, ChunkMagicProvider.MAGIC);',
        'java.util.Optional<com.mna.api.capabilities.IChunkMagic> chunkmagic = MNACap.get(chunkAccess, ChunkMagicProvider.MAGIC);', '237 chunkmagic')

t = rep(t, 'headslot.hurtAndBreak(1, (LivingEntity)event.getEntity(), e -> {});',
        'headslot.hurtAndBreak(1, (LivingEntity)event.getEntity(), EquipmentSlot.HEAD);', '379 hurtAndBreak')

n = t.count('EnchantmentHelper.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.FROST_WALKER, player)')
t = t.replace('EnchantmentHelper.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.FROST_WALKER, player)',
              'EnchantmentHelper.getEnchantmentLevel(player.level().registryAccess().holderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FROST_WALKER), player)')
print('  387 frostwalker x' + str(n))

t = rep(t, 'event.getEntity().autoSpinAttackTicks > 0', 'mnaSpinAttackTicks(event.getEntity()) > 0', '403 spin')
t = rep(t, '''    @SubscribeEvent
    public static void onPlayerPickupItem(ItemEntityPickupEvent event) {''',
        '''    private static int mnaSpinAttackTicks(LivingEntity entity) {
        try {
            java.lang.reflect.Field f = LivingEntity.class.getDeclaredField("autoSpinAttackTicks");
            f.setAccessible(true);
            return (Integer)f.get(entity);
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    @SubscribeEvent
    public static void onPlayerPickupItem(ItemEntityPickupEvent.Pre event) {''', 'pickup sig + helper')

# bloco do pickup: substituicoes so dentro do metodo
start = t.find('public static void onPlayerPickupItem(ItemEntityPickupEvent.Pre event) {')
end = t.find('@SubscribeEvent', start)
block = t[start:end]
b2 = block.replace('event.getItem()', 'event.getItemEntity()')
b2 = b2.replace('event.getEntity()', 'event.getPlayer()')
b2 = b2.replace('event.setCanceled(true);', 'event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);')
print('  pickup block: item=%d entity=%d cancel=%d' % (block.count('event.getItem()'), block.count('event.getEntity()'), block.count('event.setCanceled(true);')))
t = t[:start] + b2 + t[end:]

t = rep(t, 'event.getNewTarget()', 'event.getNewAboutToBeSetTarget()', '607 newtarget')
t = rep(t, 'event.getEntity().isAddedToWorld()', 'event.getEntity().isAddedToLevel()', '639 addedto')
t = rep(t, '''            event.setNewSize(event.getNewSize().scale(scale));
            event.setNewEyeHeight(event.getNewEyeHeight() * scale);''',
        '''            event.setNewSize(event.getNewSize().scale(scale));''', '641 eyeheight')

t = rep(t, '''                        if (!(newPos.distanceToSqr(((Player)player.get()).position()) >= 36.0) || !creeper.randomTeleport(x, y, z, true)) continue;
                        event.setCanceled(true);''',
        '''                        if (!(newPos.distanceToSqr(((Player)player.get()).position()) >= 36.0) || !creeper.randomTeleport(x, y, z, true)) continue;
                        ((net.neoforged.bus.api.ICancellableEvent)event).setCanceled(true);''', '727 explosion cancel')

t = rep(t, 'EnchantmentHelper.getEnchantmentLevel((Enchantment)(EnchantmentInit.XP_TO_SOULS.get()), (LivingEntity)player)',
        'EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.XP_TO_SOULS, (LivingEntity)player)', '774 xp_to_souls')
t = rep(t, 'EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.RETURNING.get(), (LivingEntity)event.getEntity())',
        'EnchantmentHelper.getEnchantmentLevel(EnchantmentInit.RETURNING, (LivingEntity)event.getEntity())', '839 returning')

t = rep(t, 'if (event.getEntity() instanceof AreaEffectCloud && ((AreaEffectCloud)event.getEntity()).effects.stream().anyMatch(e -> e.getEffect() instanceof INoCreeperLingering)) {',
        'if (event.getEntity() instanceof AreaEffectCloud && mnaHasNoCreeperLingering((AreaEffectCloud)event.getEntity())) {', '792 aec')
t = rep(t, '''    private static int mnaSpinAttackTicks(LivingEntity entity) {''',
        '''    private static boolean mnaHasNoCreeperLingering(AreaEffectCloud cloud) {
        try {
            java.lang.reflect.Field f = AreaEffectCloud.class.getDeclaredField("potionContents");
            f.setAccessible(true);
            net.minecraft.world.item.alchemy.PotionContents pc = (net.minecraft.world.item.alchemy.PotionContents)f.get(cloud);
            return pc != null && pc.getAllEffects().stream().anyMatch(e -> e.getEffect().value() instanceof INoCreeperLingering);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private static int mnaSpinAttackTicks(LivingEntity entity) {''', 'aec helper')

t = rep(t, '(Tag)toReturn.save(new CompoundTag())', '(Tag)com.mna.api.tools.MNAItemData.save(toReturn, new CompoundTag())', '843 save')

t = rep(t, 'List items = dragon.level().getEntities((Entity)dragon, search, e -> e instanceof ItemEntity).stream().map(e -> (ItemEntity)e).collect(Collectors.toList());',
        'List<ItemEntity> items = dragon.level().getEntities((Entity)dragon, search, e -> e instanceof ItemEntity).stream().map(e -> (ItemEntity)e).collect(Collectors.toList());', '865 list generico')

t = rep(t, 'int newLoyalty = event.getFrom().getEnchantmentLevel(Enchantments.LOYALTY);',
        'int newLoyalty = EnchantmentHelper.getItemEnchantmentLevel(event.getEntity().level().registryAccess().holderOrThrow(Enchantments.LOYALTY), event.getFrom());', '882 loyalty')

t = rep(t, 'ServerMessageDispatcher.sendAdvancementUnlocked(sp, event.getAdvancement());',
        'ServerMessageDispatcher.sendAdvancementUnlocked(sp, event.getAdvancement().id());', '928 advancement')

t = rep(t, '((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).setColor(outputStack, ((ItemRuneMarking)ItemInit.RUNE_MARKING.get()).getColor(inputRune));',
        'MNADyeableItem.setColor(outputStack, MNADyeableItem.getColor(inputRune));', '965 dyeable')
t = add_import(t, 'com.mna.api.tools.MNADyeableItem')

t = rep(t, '''        if (event.phase == TickEvent.Phase.END) {''', '''        if (true) {''', '971 phase')
save(CEH, t)

# ============================================================
# 2) global: ItemStack.of -> parseOptional
# ============================================================
print('== ItemStack.of global ==')
total = 0
for p in all_java():
    t = load(p)
    if 'ItemStack.of(' not in t:
        continue
    t2, n = wrap_calls(t, 'ItemStack.of(', lambda a: 'ItemStack.parseOptional(' + PROV + ', ' + a + ').orElse(ItemStack.EMPTY)')
    if n:
        save(p, t2)
        total += n
print('  sites:', total)

# ============================================================
# 3) global: getEnchantmentLevel args
# ============================================================
print('== getEnchantmentLevel global ==')
total = 0
for p in all_java():
    t = load(p)
    if 'getEnchantmentLevel(' not in t:
        continue
    o = t
    # caso especial EnchantmentInit: loop raw RegistryObject
    t = t.replace('for (RegistryObject enchantment : ENCHANTMENTS.getEntries()) {',
                  'for (net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.enchantment.Enchantment, ? extends net.minecraft.world.item.enchantment.Enchantment> enchantment : ENCHANTMENTS.getEntries()) {')
    t = t.replace('(net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>)enchantment.get()', 'enchantment')
    # A) (Enchantment)(X.get()) -> X
    t = re.sub(r'\(Enchantment\)\((\w+\.\w+)\.get\(\)\)', r'\1', t)
    # B) getEnchantmentLevel(X.get(), ...) -> getEnchantmentLevel(X, ...)
    t = re.sub(r'getEnchantmentLevel\((\w+\.\w+)\.get\(\), ', r'getEnchantmentLevel(\1, ', t)
    # C) ItemStack receiver .getEnchantmentLevel(EnchantmentInit.X.get()) -> helper
    t = re.sub(r'([\w.()]+)\.getEnchantmentLevel\((\w+\.\w+)\.get\(\)\)',
               r'EnchantmentHelper.getItemEnchantmentLevel(\2, \1)', t)
    # D) ItemStack receiver .getEnchantmentLevel(Enchantments.X) -> helper com BuiltInRegistries
    t = re.sub(r'([\w.()]+)\.getEnchantmentLevel\((net\.minecraft\.world\.item\.enchantment\.Enchantments\.\w+)\)',
               r'EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(\2), \1)', t)
    # E) EnchantmentHelper.getEnchantmentLevel(Enchantments.X, RECV) -> holderOrThrow
    t = re.sub(r'EnchantmentHelper\.getEnchantmentLevel\((net\.minecraft\.world\.item\.enchantment\.Enchantments\.\w+), ([\w.]+|\([\w.]+\)[\w.]+)\)',
               r'EnchantmentHelper.getEnchantmentLevel((\2).level().registryAccess().holderOrThrow(\1), \2)', t)
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total)

# ============================================================
# 4) global: MNADyeableItem instance-style
# ============================================================
print('== MNADyeableItem ==')
total = 0
for p in all_java():
    t = load(p)
    o = t
    t = re.sub(r'\(\((\w+)\)(\w+\.\w+\.get\(\)|[\w.]+)\)\.setColor\(', r'MNADyeableItem.setColor(', t)
    t = re.sub(r'\(\((\w+)\)(\w+\.\w+\.get\(\)|[\w.]+)\)\.getColor\(', r'MNADyeableItem.getColor(', t)
    if t != o:
        t = add_import(t, 'com.mna.api.tools.MNADyeableItem')
        save(p, t)
        total += 1
print('  arquivos:', total)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
