import os, re

W = r'C:\Users\lucas\Documents\coisas\Entre Mods e Códigos\Mana And Artifice 1.21.1\mna-port-workspace'
p = os.path.join(W, 'src', 'main', 'java', 'com', 'mna', 'events', 'DamageEventHandler.java')
t = open(p, encoding='utf-8').read()
lines = t.split('\n')

# localizar bloco onLivingHurt (linhas 225..326 -> idx 224..326)
i = next(k for k, l in enumerate(lines) if 'void onLivingHurt(' in l)
j = i + 1
while not lines[j].strip().startswith('@SubscribeEvent'):
    j += 1
blk = lines[i:j]
print('bloco onLivingHurt linhas', i + 1, '-', j)
body = blk[1:]
txt = '\n'.join(body)

# migracoes de API
txt = txt.replace('event.getDamageSource()', 'event.getSource()')
txt = txt.replace('event.getAmount()', 'event.getNewDamage()')
txt = txt.replace('event.setAmount(', 'event.setNewDamage(')
txt = txt.replace('event.setCanceled(true);', 'event.setNewDamage(0.0f);')
# o evento nao e cancelavel no Pre -> para os "return" apos setNewDamage(0) mantemos

new_body = 'public static void onLivingHurt(LivingDamageEvent.Pre event) {\n' + txt

lines = lines[:i] + new_body.split('\n') + lines[j:]
open(p, 'w', encoding='utf-8').write('\n'.join(lines))
print('migrado para LivingDamageEvent.Pre')
