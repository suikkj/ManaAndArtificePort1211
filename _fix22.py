# _fix22.py - FallingBlockEntity.blockState, EffectHelper, LanternWraith, WitchHunter Trickshot
import os

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = new.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + '] x' + str(t.count(o)))
    return t.replace(o, n)


print('== StationaryBlock ==')
p = 'src/main/java/com/mna/entities/utility/StationaryBlock.java'
t = load(p)
t = rep(t, 'this.blockState = fallingBlockState;',
        'com.mna.api.tools.MNAFallingBlocks.setBlockState(this, fallingBlockState);', 'sb write1')
t = rep(t, 'this.blockState = (BlockState)this.blockState.setValue((Property)BlockStateProperties.WATERLOGGED, (Comparable)Boolean.valueOf(true));',
        'com.mna.api.tools.MNAFallingBlocks.setBlockState(this, (BlockState)this.getBlockState().setValue((Property)BlockStateProperties.WATERLOGGED, (Comparable)Boolean.valueOf(true)));', 'sb write2')
n = t.count('this.blockState')
t = t.replace('this.blockState', 'this.getBlockState()')
print('  reads:', n)
save(p, t)

print('== ComponentEarthquake ==')
p = 'src/main/java/com/mna/spells/components/ComponentEarthquake.java'
t = load(p)
t = rep(t, 'fbe.blockState = state;', 'com.mna.api.tools.MNAFallingBlocks.setBlockState(fbe, state);', 'eq')
save(p, t)

print('== EffectHelper ==')
p = 'src/main/java/com/mna/effects/EffectHelper.java'
t = load(p)
t = rep(t, '''            MobEffect patt1067$temp = e.getEffect();
            return patt1067$temp instanceof IDoubleTapEndEarly && (iee = (IDoubleTapEndEarly)patt1067$temp).canEndEarly(living, (MobEffectInstance)e);''',
        '''            net.minecraft.core.Holder<MobEffect> patt1067$temp = e.getEffect();
            return patt1067$temp.value() instanceof IDoubleTapEndEarly && (iee = (IDoubleTapEndEarly)patt1067$temp.value()).canEndEarly(living, (MobEffectInstance)e);''', 'eh filter')
t = rep(t, '((IDoubleTapEndEarly)inst.getEffect()).onRemoved(living, inst);',
        '((IDoubleTapEndEarly)inst.getEffect().value()).onRemoved(living, inst);', 'eh onRemoved')
t = rep(t, 'BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect()).toString()',
        'BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect().value()).toString()', 'eh getKey')
save(p, t)

print('== LanternWraith ==')
p = 'src/main/java/com/mna/entities/boss/LanternWraith.java'
t = load(p)
t = rep(t, 'MobEffect goalEffect = MobEffects.MOVEMENT_SLOWDOWN;',
        'net.minecraft.core.Holder<MobEffect> goalEffect = MobEffects.MOVEMENT_SLOWDOWN;', 'lw decl')
t = rep(t, 'goalEffect = (MobEffect)EffectInit.LIFE_TAP.get();', 'goalEffect = EffectInit.LIFE_TAP;', 'lw life')
t = rep(t, 'goalEffect = (MobEffect)EffectInit.GRAVITY_WELL.get();', 'goalEffect = EffectInit.GRAVITY_WELL;', 'lw grav')
save(p, t)

print('== WitchHunter ==')
p = 'src/main/java/com/mna/entities/faction/WitchHunter.java'
t = load(p)
t = rep(t, 'private MobEffect potionEffect;', 'private net.minecraft.core.Holder<MobEffect> potionEffect;', 'wh field')
t = rep(t, '''        public TrickshotGoal(MobEffect effect, int duration, int magnitude) {
            this(effect, duration, magnitude, false);
        }

        public TrickshotGoal(MobEffect effect, int duration, int magnitude, boolean closeRangeOnly) {''',
        '''        public TrickshotGoal(net.minecraft.core.Holder<MobEffect> effect, int duration, int magnitude) {
            this(effect, duration, magnitude, false);
        }

        public TrickshotGoal(net.minecraft.core.Holder<MobEffect> effect, int duration, int magnitude, boolean closeRangeOnly) {''', 'wh ctors')
t = rep(t, 'BuiltInRegistries.MOB_EFFECT.getKey(this.potionEffect).toString()',
        'BuiltInRegistries.MOB_EFFECT.getKey(this.potionEffect.value()).toString()', 'wh key1')
t = rep(t, 'this.potionEffect == EffectInit.MANA_STUNT.get() || this.potionEffect == EffectInit.SILENCE.get()',
        'this.potionEffect == EffectInit.MANA_STUNT || this.potionEffect == EffectInit.SILENCE', 'wh cmp')
t = rep(t, 'WitchHunter.this.nextTrickshotEffect = this.potionEffect;',
        'WitchHunter.this.nextTrickshotEffect = this.potionEffect.value();', 'wh next')
t = rep(t, 'String effectID = BuiltInRegistries.MOB_EFFECT.getKey(this.potionEffect).toString();',
        'String effectID = BuiltInRegistries.MOB_EFFECT.getKey(this.potionEffect.value()).toString();', 'wh key2')
save(p, t)

print('== WitchhunterTrickshot ==')
p = 'src/main/java/com/mna/entities/projectile/WitchhunterTrickshot.java'
t = load(p)
t = rep(t, 'builder.define(EFFECT_ID, BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.MOVEMENT_SLOWDOWN).toString());',
        'builder.define(EFFECT_ID, BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.MOVEMENT_SLOWDOWN.value()).toString());', 'wt key')
save(p, t)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
