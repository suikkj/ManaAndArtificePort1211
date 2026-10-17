import os, re, json, io

ROOT = 'src/main/resources/data/mna/loot_table'
# {"function":"minecraft:set_nbt","tag":"{Potion:\\\"ID\\\"}"}
# -> {"function":"minecraft:set_components","components":{"minecraft:potion_contents":{"potion":"ID"}}}
changed = 0
fn_renamed = 0
for dp, _, fns in os.walk(ROOT):
    for fn in fns:
        if not fn.endswith('.json'):
            continue
        p = os.path.join(dp, fn)
        try:
            with io.open(p, encoding='utf-8-sig') as f:
                data = json.load(f)
        except Exception as e:
            print('SKIP', p, e); continue
        hit = [False]
        def walk(o):
            if isinstance(o, dict):
                fnt = o.get('function')
                if fnt in ('minecraft:set_nbt', 'set_nbt') and isinstance(o.get('tag'), str):
                    m = re.search(r'Potion"?\s*:\s*"?([a-z0-9_:]+)"?', o['tag'])
                    if m:
                        potion_id = m.group(1)
                        # normaliza namespace
                        if ':' not in potion_id:
                            potion_id = 'minecraft:' + potion_id
                        o.clear()
                        o['function'] = 'minecraft:set_components'
                        o['components'] = {'minecraft:potion_contents': {'potion': potion_id}}
                        hit[0] = True
                        return
                for v in list(o.values()):
                    walk(v)
            elif isinstance(o, list):
                for v in o:
                    walk(v)
        walk(data)
        if hit[0]:
            with io.open(p, 'w', encoding='utf-8', newline='\n') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            changed += 1
print('arquivos alterados:', changed)
