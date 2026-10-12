import re

log = open('_build_last.log', encoding='utf-8', errors='replace').read()
lines = log.split('\n')
BS = chr(92)
out = []
for i, l in enumerate(lines):
    m = re.search(r'(src' + BS + BS + 'main' + BS + BS + 'java' + BS + BS + 'com' + BS + BS + 'mna' + BS + BS + r'[^:]+):(\d+): error: (.*)$', l)
    if m and ('converted to Provider' in m.group(3) or 'method load' in m.group(3) or 'method save' in m.group(3)):
        src = lines[i + 1].strip() if i + 1 < len(lines) else ''
        f = m.group(1).replace(BS, '/')
        f = f.split('workspace/')[-1]
        out.append((f, m.group(2), m.group(3)[:60], src[:130]))
seen = set()
for f, ln, msg, src in out:
    k = (f, ln)
    if k in seen:
        continue
    seen.add(k)
    print(f + ':' + ln + '  [' + msg + ']')
    print('    ' + src)
