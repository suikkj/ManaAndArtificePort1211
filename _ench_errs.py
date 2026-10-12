import re

log = open('_build_last.log', encoding='utf-8', errors='replace').read().split('\n')
pats = ['Aura.java', 'Beheading.java', 'Bludgeoning.java', 'Bouncing.java', 'Cloudstep.java',
        'ExperienceToSouls.java', 'Fireproof.java', 'Gilded.java', 'ManaRepair.java', 'Returning.java',
        'Soulbound.java', 'TransitoryStep.java', 'DurationModifier.java', 'RangeModifier.java', 'SpeedModifier.java']
seen = set()
for i, l in enumerate(log):
    fm = re.search(r'\\([A-Za-z]+\.java):(\d+): error: (.*)$', l)
    if not fm:
        continue
    f = fm.group(1)
    if f not in pats:
        continue
    k = (f, fm.group(2))
    if k in seen:
        continue
    seen.add(k)
    src = log[i + 1].strip() if i + 1 < len(log) else ''
    det = ''
    for j in range(i + 1, min(i + 4, len(log))):
        s = log[j].strip()
        if 'symbol' in s or 'required' in s or 'found' in s:
            det = s
            break
        if not s:
            break
    print(f, fm.group(2), '|', fm.group(3)[:60], '|', det[:60])
    print('   ', src[:100])
