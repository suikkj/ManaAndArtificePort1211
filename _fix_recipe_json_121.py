import os, json, io

ROOT = 'src/main/resources/data/mna/recipe'
fixed_result_str = 0
fixed_item_key = 0
files_changed = 0

for dp, _, fns in os.walk(ROOT):
    for fn in fns:
        if not fn.endswith('.json'):
            continue
        p = os.path.join(dp, fn)
        try:
            with io.open(p, encoding='utf-8-sig') as f:
                data = json.load(f)
        except Exception as e:
            print('SKIP (parse):', p, e)
            continue
        if not isinstance(data, dict):
            continue
        rtype = data.get('type', '')
        if not (isinstance(rtype, str) and rtype.startswith('minecraft:')):
            continue
        changed = False
        result = data.get('result')
        # caso 1: result string (stonecutting/smelting antigo)
        if isinstance(result, str):
            count = data.pop('count', 1)
            data['result'] = {'id': result, 'count': count}
            fixed_result_str += 1
            changed = True
        # caso 2: result {"item": ...} -> {"id": ...}
        elif isinstance(result, dict) and 'item' in result and 'id' not in result:
            result['id'] = result.pop('item')
            fixed_item_key += 1
            changed = True
        if changed:
            with io.open(p, 'w', encoding='utf-8', newline='\n') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            files_changed += 1

print('arquivos alterados:', files_changed)
print('  result string->obj:', fixed_result_str)
print('  result item->id:', fixed_item_key)
