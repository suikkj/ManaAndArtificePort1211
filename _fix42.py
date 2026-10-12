# _fix42.py - MNAPoseStackAccess, AttributeModifier UUID->RLoc, hasModifier(.id()), STEP_HEIGHT,
#             transformPosition Vector3f, writeItem codec, PortalRenderer pose
import os
import re

FAILS = []


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


S = 'src/main/java/com/mna/'

HELPER = '''package com.mna.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Field;
import java.util.Deque;

/**
 * [PORT 1.21.1] PoseStack.poseStack (Deque interno) virou private.
 * Acesso por reflexao (usado pelo ConstructRenderer p/ snapshot de pose).
 */
public final class MNAPoseStackAccess {
    private static final Field FIELD = field();

    private MNAPoseStackAccess() {
    }

    private static Field field() {
        try {
            Field f = PoseStack.class.getDeclaredField("poseStack");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("[MNA] campo nao encontrado: PoseStack.poseStack", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void add(PoseStack stack, PoseStack.Pose pose) {
        try {
            ((Deque<PoseStack.Pose>)FIELD.get(stack)).add(pose);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
'''

print('== MNAPoseStackAccess ==')
save(S + 'tools/MNAPoseStackAccess.java', HELPER)
print('  ok')

print('== globais ==')
total = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        p = os.path.join(dp, f)
        t = load(p)
        o = t
        t = re.sub(r'([\w.]+)\.poseStack\.add\(([^;]+)\);', r'com.mna.tools.MNAPoseStackAccess.add(\1, \2);', t)
        t = t.replace('NeoForgeMod.STEP_HEIGHT_ADDITION', 'Attributes.STEP_HEIGHT')
        t = t.replace('.hasModifier(modifier)', '.hasModifier(modifier.id())')
        t = re.sub(r'\.transformPosition\(([^()]+), ([^()]+), ([^()]+)\)', r'.transformPosition(new org.joml.Vector3f(\1, \2, \3))', t)
        t = re.sub(r'([\w.]+)\.writeItem\(([^)]+)\)', r'net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(\1, \2)', t)
        if t != o:
            save(p, t)
            total += 1
print('  arquivos:', total)

print('== PlayerProgression AttributeModifier ==')
p = S + 'capabilities/playerdata/progression/PlayerProgression.java'
t = load(p)
n = t.count('new AttributeModifier(UUID.fromString(Tier_Health_Boost_IDs[index]), "Tier Health Boost " + t, (double)modifierAmount, AttributeModifier.Operation.ADD_VALUE)')
t = t.replace('new AttributeModifier(UUID.fromString(Tier_Health_Boost_IDs[index]), "Tier Health Boost " + t, (double)modifierAmount, AttributeModifier.Operation.ADD_VALUE)',
              'new AttributeModifier(ResourceLocation.fromNamespaceAndPath("mna", "tier_health_boost_" + t), (double)modifierAmount, AttributeModifier.Operation.ADD_VALUE)')
save(p, t)
print('  sites:', n)

print('== PortalRenderer addVertex(pose ==')
p = S + 'entities/renderers/ritual/PortalRenderer.java'
t = load(p)
n = t.count('vertexBuilder.addVertex(pose, ')
t = t.replace('vertexBuilder.addVertex(pose, ', 'vertexBuilder.addVertex(matrixstack$entry, ')
save(p, t)
print('  sites:', n)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
