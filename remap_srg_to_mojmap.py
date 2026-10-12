#!/usr/bin/env python3
"""
remap_srg_to_mojmap.py — aplica um mapeamento SRG->mojmap (1.20.1) sobre toda a
árvore src/main/java do port, renomeando métodos/atributos m_XXXX_ / f_XXXX_.

POR QUE ISTO EXISTE
====================
O JAR 1.20.1 roda em namespaces SRG, então o CFR descompilou para nomes SRG
(ex.: stack.m_41609_(), level.m_5776_(), living.f_20883_). O NeoForge 1.21.1
usa Mojang (mojmap) — esses nomes não existem e geram ~45k "cannot find symbol".

COMO USAR
==========
1. Obtenha o mapeamento SRG->mojmap da 1.20.1 em formato TSRG. Fontes:
   - Gerar via MDK Forge 1.20.1 (ForgeGradle baixa p/ ~/.gradle/caches/forge_gradle).
   - Compor: client_mappings.txt (obf->mojmap) + joined.srg do Forge (obf->srg).
2. Salve o arquivo (ex.: srg_to_official.tsrg) e rode:
      python remap_srg_to_mojmap.py srg_to_official.tsrg [dir_raiz]
   (dir_raiz default: src/main/java)

FORMATO TSRG aceito (linhas):
    class net/minecraft/world/entity/Entity net/minecraft/world/entity/Entity
        field f_19815_ level
        method m_20185_ ()D getX
        method m_20272_ (Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3; collide
Atenção: o valor-alvo (mojmap) aqui é o da 1.20.1; alguns nomes mudaram para 1.21.1
e ainda precisarão de ajuste pontual.
"""
import os, re, sys

def parse_tsrg(path):
    """Retorna (classmap, fieldmap, methodmap). Mapas por chave:
       class: nome_SRG_slash -> nome_mojmap_simple (usado no rename do tipo)
       field: "cls#fld" -> novo_nome
       method: "cls#nome+desc" -> novo_nome
    """
    cmap, fmap, mmap = {}, {}, {}
    cur_cls = None
    with open(path, encoding='utf-8', errors='replace') as fh:
        for raw in fh:
            line = raw.split('#')[0].strip()
            if not line:
                continue
            if line.startswith('class '):
                parts = line.split()
                if len(parts) >= 2:
                    cur_cls = parts[1]
                    cmap[parts[1]] = parts[2] if len(parts) > 2 else parts[1]
            elif line.startswith('\t\t') or (line.startswith(('field ', 'method ')) and cur_cls):
                # nested: "field", name, desc, to  OR  "method", name, desc, to
                parts = line.split()
                if parts and parts[0] in ('field', 'method') and len(parts) == 4:
                    key = f"{cur_cls}#{parts[2]}"
                    if parts[0] == 'field':
                        fmap[key] = parts[3]
                    else:
                        # parts[2] is desc; name is parts[1]? handle both layouts
                        mmap[f"{cur_cls}#{parts[1]}+{parts[2]}"] = parts[3]
            elif line.startswith('\t') and cur_cls:
                parts = line.split()
                if parts and parts[0] in ('field', 'method'):
                    if len(parts) == 4:
                        if parts[0] == 'field':
                            fmap[f"{cur_cls}#{parts[1]}"] = parts[3]
                        else:
                            mmap[f"{cur_cls}#{parts[1]}+{parts[2]}"] = parts[3]
    return cmap, fmap, mmap

def rename_tree(root, cmap, fmap, mmap):
    total = [0]
    def dots(cls_slash):
        # map srg class to srg dotted name; cmap values are mojmap (not used for matching SRG refs)
        return {k: k.replace('/', '.') for k in cmap}

    for dp, dn, fn in os.walk(root):
        for f in fn:
            if not f.endswith('.java'):
                continue
            p = os.path.join(dp, f)
            s = open(p, encoding='utf-8').read()
            o = s

            # 1) fields f_XXXX_
            # match: simple name f_12345_ (letters/descriptor). Replace via map keyed cls#fld
            # We match tokens like "f_12345_" and validate against field map across any class.
            for m in set(re.findall(r'\bf_(\d+)_\b', s)):
                srg = 'f_%s_' % m
                # find matching entries in fmap
                hits = [v for k, v in fmap.items() if k.endswith('#' + srg)]
                if hits:
                    s = s.replace(srg, hits[0])

            # 2) methods m_XXXXX_
            for m in set(re.findall(r'\bm_(\d+)[a-z]?_', s)):
                pass
            # simpler robust pass for methods: match full m_<digits><letter>_ or m_<digits>_
            for tok in set(re.findall(r'\bm_(\d+)([a-z])?_', s)):
                srg_candidate = 'm_%s_%s' % (tok[0], tok[1]) if tok[1] else 'm_%s_' % tok[0]
                hits = [v for k, v in mmap.items() if k.split('#')[1].startswith(srg_candidate.split(' ')[0]) and False]
                # fallback: exact token match in method names
                hits = [v for k, v in mmap.items() if '#' + srg_candidate in k]
                if hits:
                    s = s.replace(srg_candidate, hits[0])

            if s != o:
                open(p, 'w', encoding='utf-8').write(s)
                total[0] += 1
    return total[0]

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    tsrg = sys.argv[1]
    root = sys.argv[2] if len(sys.argv) > 2 else 'src/main/java'
    cmap, fmap, mmap = parse_tsrg(tsrg)
    print(f"classes={len(cmap)} fields={len(fmap)} methods={len(mmap)}")
    n = rename_tree(root, cmap, fmap, mmap)
    print(f"arquivos alterados: {n}")
