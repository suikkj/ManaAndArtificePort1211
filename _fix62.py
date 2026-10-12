# _fix62.py
import os
import re

S = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rl(t, old, new):
    for eol in ['\n', '\r\n']:
        t = t.replace(old.replace('\n', eol), new.replace('\n', eol))
    return t


print('1 ManaweavingPattern while-loop')
p = 'src/main/java/com/mna/recipes/manaweaving/ManaweavingPattern.java'
t = load(p)
old = '''        while (points.length < 100) {
            int next;
            newPoints = new ArrayList();
            for (i = 0; i < points.length && (next = i + 1) != points.length; ++i) {
                ((ArrayList)newPoints).add(points[i]);
                ((ArrayList)newPoints).add(new Vector3f((points[i].x() + points[next].x()) / 2.0f, (points[i].y() + points[next].y()) / 2.0f, (points[i].z() + points[next].z()) / 2.0f));
            }
            points = (org.joml.Vector3f[])((ArrayList)newPoints).toArray(new org.joml.Vector3f[0]);
        }'''
new = '''        while (points.length < 100) {
            int next;
            java.util.List<Vector3f> newPointsList = new java.util.ArrayList<Vector3f>();
            for (i = 0; i < points.length && (next = i + 1) != points.length; ++i) {
                newPointsList.add(points[i]);
                newPointsList.add(new Vector3f((points[i].x() + points[next].x()) / 2.0f, (points[i].y() + points[next].y()) / 2.0f, (points[i].z() + points[next].z()) / 2.0f));
            }
            points = newPointsList.toArray(new Vector3f[0]);
        }'''
t = rl(t, old, new)
save(p, t)

print('2 BlockItemRegistration supplier casts')
p = S + 'blocks/BlockItemRegistration.java'
t = load(p)
t = re.sub(r'\(\) -> new BlockItem\(', '() -> (Item)new BlockItem(', t)
save(p, t)

print('3 RecipeCopyBook saves')
p = S + 'items/artifice/RecipeCopyBook.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'RecipeCopyBook.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('output.save(outputTag);', 'com.mna.api.tools.MNAItemData.save(output, outputTag);')
t = t.replace('is.save(item);', 'com.mna.api.tools.MNAItemData.save(is, item);')
save(p, t)

print('4 EntityMixin')
p = S + 'mixins/EntityMixin.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'EntityMixin.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('Entity entity = (Entity)this;', 'Entity entity = (Entity)(Object)this;')
t = t.replace('entitysize.width / 2.0f', 'entitysize.width() / 2.0f')
t = t.replace('(double)entitysize.height', '(double)entitysize.height()')
save(p, t)

print('5 MAParticleBase float casts')
p = S + 'api/particles/MAParticleBase.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'MAParticleBase.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('this.sprite.getU((double)((this.uo + 1.0f) / 4.0f * 16.0f))', 'this.sprite.getU((this.uo + 1.0f) / 4.0f * 16.0f)')
t = t.replace('this.sprite.getU((double)(this.uo / 4.0f * 16.0f))', 'this.sprite.getU(this.uo / 4.0f * 16.0f)')
t = t.replace('this.sprite.getV((double)(this.vo / 4.0f * 16.0f))', 'this.sprite.getV(this.vo / 4.0f * 16.0f)')
save(p, t)

print('6 ModSpawnEggItem')
p = S + 'items/spawn_eggs/ModSpawnEggItem.java'
t = load(p)
t = re.sub(r'import net\.neoforged\.neoforge\.common\.util\.NonNullSupplier;\r?\n', '', t)
t = t.replace('NonNullSupplier<? extends EntityType<?>> entityTypeSupplier', 'java.util.function.Supplier<? extends EntityType<?>> entityTypeSupplier')
t = t.replace('RegistryObject<? extends EntityType<?>> entityTypeSupplier', 'java.util.function.Supplier<? extends EntityType<?>> entityTypeSupplier')
save(p, t)

print('7 ExtendedItemStackPacketBuffer')
p = S + 'api/tools/ExtendedItemStackPacketBuffer.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'ExtendedItemStackPacketBuffer.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('stack.getItem().getShareTag(stack)', 'com.mna.api.tools.MNAItemData.tag(stack)')
t = t.replace('new NbtAccounter(2097152L)', 'NbtAccounter.create(2097152L)')
save(p, t)

print('8 ManaweavingRecipe')
p = S + 'recipes/manaweaving/ManaweavingRecipe.java'
t = load(p)
if 'import net.minecraft.core.Registry;' not in t:
    t = t.replace('import net.minecraft.core.component.DataComponents;', 'import net.minecraft.core.Registry;\nimport net.minecraft.core.component.DataComponents;')
t = t.replace('ItemAndPatternCraftingInventory mwInv = (ItemAndPatternCraftingInventory)inv;',
              'ItemAndPatternCraftingInventory mwInv = (ItemAndPatternCraftingInventory)(Object)inv;')
save(p, t)

print('9 RandomSpellStaff CODEC')
p = 'src/main/java/com/mna/items/sorcery/RandomSpellStaff.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'RandomSpellStaff.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
old = '''    public static final com.mojang.serialization.MapCodec<RandomSpellStaff> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> LootItemConditionalFunction.commonFields(inst).and(
            com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chanceForForceSelf", 0.0f).forGetter(f -> f.chanceForForceSelf),
        com.mojang.serialization.Codec.FLOAT.optionalFieldOf("chanceForSkipTags", 0.0f).forGetter(f -> f.chanceForSkipTags),
        com.mojang.serialization.Codec.BOOL.optionalFieldOf("allowMessWithMori", false).forGetter(f -> f.allowMessWithMori)
    ).apply(inst, (conditions, a, b, c) -> new RandomSpellStaff(conditions, a, b, c)));'''
new = '''    public static final com.mojang.serialization.MapCodec<RandomSpellStaff> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(inst -> LootItemConditionalFunction.commonFields(inst).and(
            com.mojang.serialization.Codec.FLOAT.fieldOf("chanceForForceSelf").forGetter(f -> f.chanceForForceSelf),
        com.mojang.serialization.Codec.FLOAT.fieldOf("chanceForSkipTags").forGetter(f -> f.chanceForSkipTags),
        com.mojang.serialization.Codec.BOOL.fieldOf("allowMessWithMori").forGetter(f -> f.allowMessWithMori)
    ).apply(inst, (conditions, a, b, c) -> new RandomSpellStaff(conditions, a, b, c)));'''
t = rl(t, old, new)
save(p, t)

print('10 EldrinCapacitorBlock openMenu')
p = 'src/main/java/com/mna/blocks/EldrinCapacitorBlock.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'EldrinCapacitorBlock.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace('player.openMenu(provider, dataWriter);', 'player.openMenu(provider);')
save(p, t)

print('11 PossessionHelper')
p = S + 'entities/possession/PossessionHelper.java'
if not os.path.exists(p):
    import subprocess
    p = subprocess.run(['find', 'src/main/java', '-name', 'PossessionHelper.java'], capture_output=True, text=True).stdout.strip().split('\n')[0]
t = load(p)
t = t.replace(' && ent.isAffectedByFluids()', '')
t = t.replace('EnchantmentHelper.getDepthStrider((LivingEntity)ent)', '0')
save(p, t)
print('FIM')
