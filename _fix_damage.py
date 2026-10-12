# _fix_damage.py - conserta os arquivos afetados pelo stripper de comentarios
# e as migracoes pendentes (DistExecutor -> FMLEnvironment, ITag -> TagKey/HolderSet,
# IPlantable/PlantType -> TriState, URL truncada).
import sys

FAILS = []


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


W = 'src/main/java/com/mna/'

# ================= APIBridge =================
print('APIBridge.java')
p = W + 'apibridge/APIBridge.java'
t = load(p)
t = rep(t, 'import net.neoforged.fml.DistExecutor;\n', '', 'APIBridge bad import')
t = rep(t, '''        DistExecutor.runWhenOn((Dist)Dist.CLIENT, () -> () -> {
            ManaAndArtificeMod.setAPIHelper(WorldRenderUtils.INSTANCE);
            ManaAndArtificeMod.setAPIHelper(new GuiRenderHelper());
            ManaAndArtificeMod.setAPIHelper(CastingResourceGuiRegistry.Instance);
        });''', '''        if (FMLEnvironment.dist == Dist.CLIENT) {
            ManaAndArtificeMod.setAPIHelper(WorldRenderUtils.INSTANCE);
            ManaAndArtificeMod.setAPIHelper(new GuiRenderHelper());
            ManaAndArtificeMod.setAPIHelper(CastingResourceGuiRegistry.Instance);
        }''', 'APIBridge runWhenOn')
t = add_import(t, 'net.neoforged.fml.loading.FMLEnvironment')
save(p, t)

# ================= ManaAndArtifice =================
print('ManaAndArtifice.java')
p = W + 'ManaAndArtifice.java'
t = load(p)
t = rep(t, '        DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> {\n',
        '        if (FMLEnvironment.dist == Dist.CLIENT) {\n', 'MAA client block')
t = rep(t, '''            this.proxy = new ClientProxy();
        });
        DistExecutor.unsafeRunWhenOn((Dist)Dist.DEDICATED_SERVER, () -> () -> {
            this.proxy = new ServerProxy();
        });''', '''            this.proxy = new ClientProxy();
        } else {
            this.proxy = new ServerProxy();
        }''', 'MAA server block')
sep = '\r\n' if '\r\n' in t else '\n'
lines = t.split(sep)
fixed = 0
for i, l in enumerate(lines):
    if 'new URL("https:' in l and 'manaandartifice.com' not in l:
        lines[i] = '            URL url = new URL("https://manaandartifice.com/auras_enabled.txt");'
        fixed += 1
print('  URL line fixed:', fixed)
if fixed == 0:
    FAILS.append('MAA url')
t = sep.join(lines)
t = add_import(t, 'net.neoforged.fml.loading.FMLEnvironment')
save(p, t)

# ================= MATags =================
print('MATags.java')
p = W + 'api/tools/MATags.java'
t = load(p)
t = rep(t, 'import net.neoforged.neoforge.registries.tags.TagKey;\n', '', 'MATags bad import')
t = rep(t, '''            TagKey tag = BuiltInRegistries.ITEM.tags().getTag(BuiltInRegistries.ITEM.tags().createTagKey(tagID));
            if (tag != null) {
                return tag.stream().collect(Collectors.toList());
            }''', '''            Optional<HolderSet.Named<Item>> tag = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tagID));
            if (tag.isPresent()) {
                return tag.get().stream().map(Holder::value).collect(Collectors.toList());
            }''', 'MATags ITEM')
t = rep(t, '''            TagKey tag = BuiltInRegistries.BLOCK.tags().getTag(BuiltInRegistries.BLOCK.tags().createTagKey(tagID));
            if (tag != null) {
                return tag.stream().collect(Collectors.toList());
            }''', '''            Optional<HolderSet.Named<Block>> tag = BuiltInRegistries.BLOCK.getTag(TagKey.create(Registries.BLOCK, tagID));
            if (tag.isPresent()) {
                return tag.get().stream().map(Holder::value).collect(Collectors.toList());
            }''', 'MATags BLOCK')
t = rep(t, '''            TagKey tag = BuiltInRegistries.ENTITY_TYPE.tags().getTag(BuiltInRegistries.ENTITY_TYPE.tags().createTagKey(tagID));
            if (tag != null) {
                return true;
            }''', '''            Optional<HolderSet.Named<EntityType<?>>> tag = BuiltInRegistries.ENTITY_TYPE.getTag(TagKey.create(Registries.ENTITY_TYPE, tagID));
            if (tag.isPresent()) {
                return true;
            }''', 'MATags ENTITY exist')
t = rep(t, '''            TagKey tag = BuiltInRegistries.ENTITY_TYPE.tags().getTag(BuiltInRegistries.ENTITY_TYPE.tags().createTagKey(tagID));
            if (tag != null) {
                return tag.stream().collect(Collectors.toList());
            }''', '''            Optional<HolderSet.Named<EntityType<?>>> tag = BuiltInRegistries.ENTITY_TYPE.getTag(TagKey.create(Registries.ENTITY_TYPE, tagID));
            if (tag.isPresent()) {
                return tag.get().stream().map(Holder::value).collect(Collectors.toList());
            }''', 'MATags ENTITY list')
save(p, t)

# ================= RitualRenderer =================
print('RitualRenderer.java')
p = W + 'entities/renderers/ritual/RitualRenderer.java'
t = load(p)
t = rep(t, 'import net.neoforged.neoforge.registries.tags.TagKey;\n', '', 'RR bad import')
t = rep(t, '''            Optional rnd;
            TagKey tags;''', '''            Optional<Holder<Item>> rnd;
            HolderSet.Named<Item> tags;''', 'RR decls')
t = rep(t, '(tags = BuiltInRegistries.ITEM.tags().getTag(BuiltInRegistries.ITEM.tags().createTagKey(posData.getReagent().getResourceLocation())))',
        '(tags = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, posData.getReagent().getResourceLocation())).orElse(null))',
        'RR tag fetch')
t = rep(t, 'renderItem = (Item)rnd.get();', 'renderItem = rnd.get().value();', 'RR rnd.get')
t = add_import(t, 'net.minecraft.core.Holder')
t = add_import(t, 'net.minecraft.core.HolderSet')
t = add_import(t, 'net.minecraft.core.registries.Registries')
save(p, t)

# ================= ComponentGust =================
print('ComponentGust.java')
p = W + 'spells/components/ComponentGust.java'
t = load(p)
t = rep(t, 'state.getBlock() instanceof IPlantable', 'state.getBlock() instanceof BushBlock', 'Gust instanceof')
t = add_import(t, 'net.minecraft.world.level.block.BushBlock')
save(p, t)

# ================= Flower blocks =================
flower = {
    'MASandFlowerBlock': ('DESERT', 'this.mayPlaceOn(soil, (BlockGetter)worldIn, pos.below());'),
    'MAStoneFlowerBlock': ('CAVE', 'Block.isFaceFull(soil.getCollisionShape((BlockGetter)worldIn, pos.below()), Direction.UP);'),
    'MAWaterFlowerBlock': ('WATER', 'this.mayPlaceOn(soil, (BlockGetter)worldIn, pos.below());'),
}
for name, (ptype, fallback) in flower.items():
    print(name + '.java')
    p = W + 'blocks/worldgen/' + name + '.java'
    t = load(p)
    old = '''    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        return soil.canSustainPlant((BlockGetter)worldIn, pos.below(), Direction.UP, (IPlantable)this);
    }

    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return PlantType.''' + ptype + ''';
    }'''
    new = '''    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        // [PORT 1.21.1] IPlantable/PlantType foram removidos: canSustainPlant agora devolve
        // TriState (TRUE/FALSE mandam; DEFAULT deixa a planta decidir a propria regra).
        TriState sustains = soil.canSustainPlant((BlockGetter)worldIn, pos.below(), Direction.UP, state);
        if (sustains != TriState.DEFAULT) {
            return sustains.isTrue();
        }
        return ''' + fallback + '''
    }'''
    t = rep(t, old, new, name + ' canSurvive')
    t = add_import(t, 'net.neoforged.neoforge.common.util.TriState')
    save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
sys.exit(1 if FAILS else 0)
