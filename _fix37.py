# _fix37.py - addVertex helpers: PoseStack.Pose (assinatura+corpo+callers) / transformPosition p/ os sem normal
import os
import re

FAILS = []
POSE_FQN = 'com.mojang.blaze3d.vertex.PoseStack.Pose'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def all_java():
    out = []
    for dp, _, fns in os.walk('src/main/java'):
        for f in fns:
            if f.endswith('.java'):
                out.append(os.path.join(dp, f))
    return out


S = 'src/main/java/com/mna/'

print('== 2 arquivos sem normal: transformPosition ==')
for p in [S + 'blocks/tileentities/renderers/ConstructWorkbenchRenderer.java',
          S + 'entities/renderers/ritual/TimeChangeBallRenderer.java']:
    t = load(p)
    t2, n = re.subn(
        r'vertexBuilder\.addVertex\(new com\.mojang\.blaze3d\.vertex\.PoseStack\.Pose\(renderMatrix, normalMatrix\), ([^;]+?)\)\.setColor',
        r'{ org.joml.Vector3f mnaV = renderMatrix.transformPosition(\1); vertexBuilder.addVertex(mnaV.x(), mnaV.y(), mnaV.z()).setColor',
        t)
    if n:
        save(p, t2)
    print('  ' + os.path.basename(p) + ': ' + str(n))

print('== WorldRenderUtils ==')
p = S + 'tools/render/WorldRenderUtils.java'
t = load(p)
t = t.replace('(VertexConsumer vertexBuilder_, Matrix4f renderMatrix, PoseStack.Pose normalMatrix, ',
              '(VertexConsumer vertexBuilder_, ' + POSE_FQN + ' pose, ')
t = t.replace('addVertex(new com.mojang.blaze3d.vertex.PoseStack.Pose(renderMatrix, normalMatrix), ', 'addVertex(pose, ')
t = t.replace('.setNormal(normalMatrix, nrmH, nrmV, nrmH);', '.setNormal(pose, nrmH, nrmV, nrmH);')
t = t.replace('addVertex(vertexBuilder, renderMatrix, normalMatrix, ', 'addVertex(vertexBuilder, normalMatrix, ')
save(p, t)
print('  ok')

print('== ConstructRenderer ==')
p = S + 'entities/renderers/construct/ConstructRenderer.java'
t = load(p)
t = t.replace('(VertexConsumer bufferBuilder, Matrix4f pose, Matrix3f normal, ', '(VertexConsumer bufferBuilder, ' + POSE_FQN + ' pose, ')
t = t.replace('bufferBuilder.addVertex(new com.mojang.blaze3d.vertex.PoseStack.Pose(pose, normal), ', 'bufferBuilder.addVertex(pose, ')
t = t.replace('.setNormal(normal, 0.0f, 1.0f, 0.0f);', '.setNormal(pose, 0.0f, 1.0f, 0.0f);')
t = t.replace('ConstructRenderer.vertex(vertexconsumer, matrix4f, matrix3f, ', 'ConstructRenderer.vertex(vertexconsumer, posestack$pose, ')
save(p, t)
print('  ok')

print('== 12 arquivos padrao ==')
total = 0
for p in all_java():
    t = load(p)
    o = t
    t = re.sub(r'\(VertexConsumer (\w+), Matrix4f renderMatrix, Matrix3f normalMatrix, ', r'(VertexConsumer \1, ' + POSE_FQN + r' pose, ', t)
    t = t.replace('addVertex(new com.mojang.blaze3d.vertex.PoseStack.Pose(renderMatrix, normalMatrix), ', 'addVertex(pose, ')
    t = t.replace('.setNormal(normalMatrix, nrmH, nrmV, nrmH);', '.setNormal(pose, nrmH, nrmV, nrmH);')
    t = t.replace('.setNormal(normalMatrix, 0.0f, 1.0f, 0.0f);', '.setNormal(pose, 0.0f, 1.0f, 0.0f);')
    t = t.replace(', renderMatrix, normalMatrix, ', ', matrixstack$entry, ')
    if t != o:
        save(p, t)
        total += 1
print('  arquivos:', total)

print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
