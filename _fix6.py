# _fix6.py - GuiGuideBook: campo Type vs enum, csr nao declarado, gate duplicado,
# casts (Collection) -> (Collection<Renderable>)
import os
import re

FAILS = []
ROOT = 'src/main/java/com/mna/'


def load(p):
    return open(p, encoding='utf-8', newline='').read()


def save(p, t):
    open(p, 'w', encoding='utf-8', newline='').write(t)


def rep(t, old, new, tag, count=1):
    o, n = old, new
    if '\r\n' in t and '\r\n' not in o:
        o = o.replace('\n', '\r\n')
        n = n.replace('\n', '\r\n')
    if o not in t:
        print('  FALHOU [' + tag + ']')
        FAILS.append(tag)
        return t
    print('  ok [' + tag + '] x' + str(t.count(o)))
    return t.replace(o, n, count)


# 1) CodexBreadcrumb: renomear o CAMPO Type -> breadcrumbType (o campo sombreava o enum)
p = ROOT + 'api/capabilities/CodexBreadcrumb.java'
t = load(p)
t = rep(t, 'public final Type Type;', 'public final Type breadcrumbType;', 'CB field')
t = rep(t, 'this.Type = type;', 'this.breadcrumbType = type;', 'CB assign')
save(p, t)

# usos do campo em outros arquivos: X.Type (leitura) -> X.breadcrumbType
n = 0
for dp, _, fns in os.walk('src/main/java'):
    for f in fns:
        if not f.endswith('.java'):
            continue
        q = os.path.join(dp, f)
        if q.endswith('CodexBreadcrumb.java'):
            continue
        t = load(q)
        if '.Type ==' not in t and '.Type;' not in t and '.Type)' not in t:
            continue
        t2 = re.sub(r'(\w+)\.Type\b(?!\.)', r'\1.breadcrumbType', t)
        if t2 != t:
            save(q, t2)
            n += 1
            print('  ok campo Type ->', q)
print('  arquivos com uso do campo:', n)

# 2) GuiGuideBook
p = ROOT + 'gui/item/GuiGuideBook.java'
t = load(p)

# 2a) casts (Collection) -> (Collection<Renderable>)
t2 = t.replace('(Collection)this.pageWidgets', '(Collection<Renderable>)this.pageWidgets')
print('  casts Collection:', t.count('(Collection)this.pageWidgets'))
t = t2

# 2b) csr nao declarado: inserir antes do for de results
t = rep(t, '''        for (GuideBookEntries.GuidebookSearchResult e : results) {
            if (e.isEntry()) {
                csr = new CodexSearchResult(''', '''        CodexSearchResult csr = null;
        for (GuideBookEntries.GuidebookSearchResult e : results) {
            if (e.isEntry()) {
                csr = new CodexSearchResult(''', 'csr decl')

# 2c) remover o gate duplicado (ImageSpellPartButton nao tem campo 'page')
gate = '''            if (GuiGuideBook.this.page != this.page && GuiGuideBook.this.page != this.page - 1) {
                return;
            }
'''
g = gate.replace('\n', '\r\n') if '\r\n' in t else gate
parts = t.split(g)
print('  gates encontrados:', len(parts) - 1)
if len(parts) == 3:
    t = parts[0] + g + parts[1] + parts[2]
    print('  ok gate duplicado removido')
else:
    print('  FALHOU gate')
    FAILS.append('gate')

save(p, t)
print()
print('FALHAS:', FAILS if FAILS else 'nenhuma')
