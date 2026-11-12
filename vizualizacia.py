"""
Interaktívna vizualizácia KDTree, QuadTree a QuadKDTree (kopíruje logiku z Java kódu v sk.fri.structures).

Spustenie:  python vizualizacia.py      (iba štandardný tkinter, nič sa neinštaluje)

Ovládanie:
  Režim "Vlož"   - klik do plochy vloží bod, v logu uvidíš každé rozhodnutie
  Režim "Nájdi"  - klik hľadá presný bod (find)
  Režim "Oblasť" - ťahaním myši vyznačíš obdĺžnik (rangeSearch)
  MAX_DEPTH a LEAF_CAPACITY zmeň a stlač "Použiť" -> strom sa postaví nanovo z rovnakých bodov.
  Tip: pre QuadTree skús MAX_DEPTH=2, pre QuadKD LEAF_CAPACITY=2 alebo 3.
"""
import random
import tkinter as tk
from tkinter import ttk

CFG = {"max_depth": 32, "cap": 32}
DOMAIN = 100
SCALE = 5.2
MARGIN = 30


def noop(_msg):
    pass


def quadrant(p, c, log=noop):
    """Rovnako ako Java quadrant(): >= ide doprava/hore. index = (x>=cx ? 1 : 0) + (y>=cy ? 2 : 0)."""
    idx = 0
    parts = []
    if p[0] >= c[0]:
        idx += 1
        parts.append(f"x: {p[0]} >= {c[0]} -> +1")
    else:
        parts.append(f"x: {p[0]} <  {c[0]} -> +0")
    if p[1] >= c[1]:
        idx += 2
        parts.append(f"y: {p[1]} >= {c[1]} -> +2")
    else:
        parts.append(f"y: {p[1]} <  {c[1]} -> +0")
    log("    " + ";  ".join(parts) + f"  => kvadrant {idx}")
    return idx


QUAD_NAMES = {0: "0 (vľavo dole)", 1: "1 (vpravo dole)", 2: "2 (vľavo hore)", 3: "3 (vpravo hore)"}


def intersects_quadrant(center, q, mn, mx):
    right, upper = (q & 1) != 0, (q & 2) != 0
    ix = mx[0] >= center[0] if right else mn[0] < center[0]
    iy = mx[1] >= center[1] if upper else mn[1] < center[1]
    return ix and iy


def explain_quadrant(center, q, mn, mx):
    """Rozpíše presne tie isté porovnania ako intersectsQuadrant v Jave."""
    right, upper = (q & 1) != 0, (q & 2) != 0
    if right:
        ix, tx = mx[0] >= center[0], f"max.x {mx[0]} >= center.x {center[0]}"
    else:
        ix, tx = mn[0] < center[0], f"min.x {mn[0]} < center.x {center[0]}"
    if upper:
        iy, ty = mx[1] >= center[1], f"max.y {mx[1]} >= center.y {center[1]}"
    else:
        iy, ty = mn[1] < center[1], f"min.y {mn[1]} < center.y {center[1]}"
    mark = lambda ok: "ÁNO" if ok else "NIE"
    side = f"{'pravá' if right else 'ľavá'}/{'horná' if upper else 'dolná'}"
    verdict = "-> NAVŠTÍVIM" if ix and iy else "-> PRESKOČÍM"
    return f"kvadrant {q} ({side}): x: {tx} {mark(ix)};  y: {ty} {mark(iy)}  {verdict}"


def draw_skipped(d, root, query):
    """Vyšrafuje kvadranty, ktoré range search preskočil (a do ktorých už nezostupoval)."""
    mn, mx = query

    def rec(n, x0, y0, x1, y1):
        if n is None or (hasattr(n, "is_leaf") and n.is_leaf()):
            return
        cx, cy = n.point
        for q in range(4):
            qx0, qx1 = (cx, x1) if q & 1 else (x0, cx)
            qy0, qy1 = (cy, y1) if q & 2 else (y0, cy)
            if intersects_quadrant(n.point, q, mn, mx):
                rec(n.children[q], qx0, qy0, qx1, qy1)
            else:
                d.shade(qx0, qy0, qx1, qy1)

    rec(root, 0, 0, DOMAIN, DOMAIN)


def contains(mn, mx, p):
    return mn[0] <= p[0] <= mx[0] and mn[1] <= p[1] <= mx[1]


# ---------------------------------------------------------------- KD tree
class KDNode:
    def __init__(self, value):
        self.point = value[1]
        self.values = [value]
        self.left = None
        self.right = None

    def kids(self):
        return [(k, c) for k, c in (("L", self.left), ("R", self.right)) if c]

    def label(self):
        n = f"x{len(self.values)}" if len(self.values) > 1 else ""
        return f"{self.point} {n}".strip()


class KDTree:
    name = "KDTree"

    def __init__(self, log=noop):
        self.root = None
        self.log = log
        self.path = []

    def insert(self, value):
        p = value[1]
        self.path = []
        if self.root is None:
            self.root = KDNode(value)
            self.path.append(self.root)
            self.log("  strom je prázdny -> bod sa stane koreňom")
            return True
        cur, depth = self.root, 0
        while True:
            self.path.append(cur)
            dim = depth % 2
            self.log(f"  uzol {cur.point}, hĺbka {depth}, porovnávam os {'xy'[dim]}")
            if cur.point == p:
                cur.values.append(value)
                self.log("    rovnaké súradnice -> hodnota sa pridá do values (nový uzol nevznikne)")
                return True
            if cur.point[dim] > p[dim]:
                self.log(f"    {cur.point[dim]} > {p[dim]} -> ľavý potomok")
                if cur.left is None:
                    cur.left = KDNode(value)
                    self.log("    ľavý je prázdny -> vytvorím nový uzol")
                    return True
                cur = cur.left
            else:
                self.log(f"    {cur.point[dim]} <= {p[dim]} -> pravý potomok")
                if cur.right is None:
                    cur.right = KDNode(value)
                    self.log("    pravý je prázdny -> vytvorím nový uzol")
                    return True
                cur = cur.right
            depth += 1

    def find(self, p):
        self.path = []
        cur, depth = self.root, 0
        while cur is not None:
            self.path.append(cur)
            if cur.point == p:
                self.log(f"  uzol {cur.point} = hľadaný bod -> nájdené")
                return cur.values
            dim = depth % 2
            go_left = cur.point[dim] > p[dim]
            self.log(f"  uzol {cur.point}, os {'xy'[dim]}: {cur.point[dim]} {'>' if go_left else '<='} {p[dim]} -> {'ľavo' if go_left else 'pravo'}")
            cur = cur.left if go_left else cur.right
            depth += 1
        self.log("  narazil som na prázdne miesto -> bod neexistuje")
        return None

    def range_search(self, mn, mx):
        self.path = []
        matches = []
        stack = [(self.root, 0)] if self.root else []
        while stack:
            cur, depth = stack.pop()
            self.path.append(cur)
            dim = depth % 2
            split = cur.point[dim]
            inside = contains(mn, mx, cur.point)
            self.log(f"  navštívený {cur.point} (os {'xy'[dim]}, split {split}) {'-> V OBLASTI' if inside else '-> mimo'}")
            if inside:
                matches.extend(cur.values)
            if cur.left:
                if mn[dim] <= split:
                    stack.append((cur.left, depth + 1))
                else:
                    self.log(f"    ľavý podstrom preskočený (min {mn[dim]} > split {split})")
            if cur.right:
                if mx[dim] >= split:
                    stack.append((cur.right, depth + 1))
                else:
                    self.log(f"    pravý podstrom preskočený (max {mx[dim]} < split {split})")
        return matches

    def draw_regions(self, d, bounds=(0, 0, DOMAIN, DOMAIN)):
        def rec(n, depth, x0, y0, x1, y1):
            if n is None:
                return
            dim = depth % 2
            s = n.point[dim]
            if dim == 0:
                d.line(s, y0, s, y1, "#d33")
                rec(n.left, depth + 1, x0, y0, s, y1)
                rec(n.right, depth + 1, s, y0, x1, y1)
            else:
                d.line(x0, s, x1, s, "#36c")
                rec(n.left, depth + 1, x0, y0, x1, s)
                rec(n.right, depth + 1, x0, s, x1, y1)
        rec(self.root, 0, *bounds)


# ---------------------------------------------------------------- QuadTree
class QuadNode:
    def __init__(self, value):
        self.point = value[1]
        self.values = [value]
        self.children = [None] * 4

    def kids(self):
        return [(str(i), c) for i, c in enumerate(self.children) if c]

    def label(self):
        n = f"x{len(self.values)}" if len(self.values) > 1 else ""
        return f"{self.point} {n}".strip()


class QuadTree:
    name = "QuadTree"

    def __init__(self, log=noop):
        self.root = None
        self.log = log
        self.path = []

    def insert(self, value):
        p = value[1]
        self.path = []
        if self.root is None:
            self.root = QuadNode(value)
            self.path.append(self.root)
            self.log("  strom je prázdny -> bod sa stane koreňom")
            return True
        cur, depth = self.root, 0
        while True:
            self.path.append(cur)
            self.log(f"  uzol {cur.point}, hĺbka {depth} (MAX_DEPTH={CFG['max_depth']})")
            if cur.point == p:
                cur.values.append(value)
                self.log("    rovnaké súradnice -> hodnota ide do values (nový uzol nevznikne)")
                return True
            if depth >= CFG["max_depth"]:
                self.log("    hĺbka >= MAX_DEPTH -> vloženie ODMIETNUTÉ (insert vráti false)")
                return False
            q = quadrant(p, cur.point, self.log)
            if cur.children[q] is None:
                cur.children[q] = QuadNode(value)
                self.log(f"    kvadrant {q} je prázdny -> vytvorím tam nový uzol")
                return True
            self.log(f"    kvadrant {q} už existuje -> pokračujem do neho")
            cur = cur.children[q]
            depth += 1

    def find(self, p):
        self.path = []
        cur = self.root
        while cur is not None:
            self.path.append(cur)
            if cur.point == p:
                self.log(f"  uzol {cur.point} = hľadaný bod -> nájdené")
                return cur.values
            self.log(f"  uzol {cur.point}:")
            cur = cur.children[quadrant(p, cur.point, self.log)]
        self.log("  prázdne miesto -> bod neexistuje")
        return None

    def range_search(self, mn, mx):
        self.path = []
        matches = []
        stack = [self.root] if self.root else []
        while stack:
            cur = stack.pop()
            self.path.append(cur)
            inside = contains(mn, mx, cur.point)
            self.log(f"  navštívený {cur.point} {'-> V OBLASTI' if inside else '-> mimo'}")
            if inside:
                matches.extend(cur.values)
            for q in range(4):
                ch = cur.children[q]
                if ch is None:
                    self.log(f"    kvadrant {q}: nemá potomka (nič na preskúmanie)")
                    continue
                self.log("    " + explain_quadrant(cur.point, q, mn, mx))
                if intersects_quadrant(cur.point, q, mn, mx):
                    stack.append(ch)
        return matches

    range_query = None

    def draw_regions(self, d):
        def rec(n, x0, y0, x1, y1):
            if n is None:
                return
            cx, cy = n.point
            d.line(cx, y0, cx, y1, "#888")
            d.line(x0, cy, x1, cy, "#888")
            for q in range(4):
                rec(n.children[q],
                    cx if q & 1 else x0, cy if q & 2 else y0,
                    x1 if q & 1 else cx, y1 if q & 2 else cy)
        rec(self.root, 0, 0, DOMAIN, DOMAIN)
        if self.range_query and self.root:
            draw_skipped(d, self.root, self.range_query)


# ---------------------------------------------------------------- QuadKDTree
class QuadKDNode:
    def __init__(self, value):
        self.point = value[1]
        self.data = KDTree()
        self.data.insert(value)
        self.values = [value]
        self.distinct = 1
        self.children = [None] * 4

    def is_leaf(self):
        return self.data is not None

    def kids(self):
        return [(str(i), c) for i, c in enumerate(self.children) if c]

    def label(self):
        if self.is_leaf():
            return f"LIST {self.point}\n{self.distinct}/{CFG['cap']}"
        return f"{self.point}"


class QuadKDTree:
    name = "QuadKDTree"

    def __init__(self, log=noop):
        self.root = None
        self.log = log
        self.path = []
        self.last_split = []

    def insert(self, value):
        p = value[1]
        self.path = []
        self.last_split = []
        if self.root is None:
            self.root = QuadKDNode(value)
            self.path.append(self.root)
            self.log("  strom je prázdny -> vznikne koreň (list s vlastným KD stromom)")
            return True
        cur, depth = self.root, 0
        while True:
            self.path.append(cur)
            if cur.is_leaf():
                exists = cur.data.find(p) is not None
                self.log(f"  LIST {cur.point}, hĺbka {depth}/{CFG['max_depth']}, rôznych bodov {cur.distinct}/{CFG['cap']}, bod už existuje: {exists}")
                if exists or cur.distinct < CFG["cap"] or depth >= CFG["max_depth"]:
                    why = ("bod už existuje" if exists else
                           "list ešte nie je plný" if cur.distinct < CFG["cap"] else
                           "MAX_DEPTH dosiahnutý, list už nerastie do hĺbky")
                    cur.data.insert(value)
                    cur.values.append(value)
                    if not exists:
                        cur.distinct += 1
                    self.log(f"    -> vložené do KD stromu v liste ({why})")
                    return True
                self.log("    list je PLNÝ a bod je nový -> SPLIT listu")
                self.split(cur)
            else:
                self.log(f"  vnútorný uzol {cur.point}, hĺbka {depth}")
            q = quadrant(p, cur.point, self.log)
            child = cur.children[q]
            if child is None:
                cur.children[q] = QuadKDNode(value)
                self.log(f"    kvadrant {q} je prázdny -> nový list s týmto bodom")
                return True
            self.log(f"    kvadrant {q} existuje -> pokračujem do neho")
            cur = child
            depth += 1

    def split(self, node):
        self.last_split.append(node)
        self.log(f"    SPLIT: stred = {node.point} (bod, ktorý list vytvoril); presúvam {len(node.values)} hodnôt:")
        for v in node.values:
            q = quadrant(v[1], node.point)
            self.log(f"      hodnota #{v[0]} {v[1]} -> kvadrant {q}")
            ch = node.children[q]
            if ch is None:
                node.children[q] = QuadKDNode(v)
                continue
            exists = ch.data.find(v[1]) is not None
            ch.data.insert(v)
            ch.values.append(v)
            if not exists:
                ch.distinct += 1
        node.data = None
        node.values = None
        node.distinct = 0
        sizes = {q: c.distinct for q, c in enumerate(node.children) if c}
        self.log(f"    starý list sa stal vnútorným uzlom (data=null, values=null); nové listy (kvadrant: rôznych bodov) {sizes}")
        if any(c.distinct >= CFG["cap"] for c in node.children if c):
            self.log("    pozor: niektorý nový list je znova plný, ďalší nový bod tam spôsobí ďalší split")

    def find(self, p):
        self.path = []
        cur = self.root
        while cur is not None:
            self.path.append(cur)
            if cur.is_leaf():
                res = cur.data.find(p)
                self.log(f"  dorazil som do listu {cur.point} -> hľadám v jeho KD strome: {'nájdené' if res else 'nenájdené'}")
                return res
            self.log(f"  vnútorný uzol {cur.point}:")
            cur = cur.children[quadrant(p, cur.point, self.log)]
        self.log("  prázdne miesto -> bod neexistuje")
        return None

    def range_search(self, mn, mx):
        self.path = []
        matches = []
        stack = [self.root] if self.root else []
        while stack:
            node = stack.pop()
            self.path.append(node)
            if node.is_leaf():
                res = node.data.range_search(mn, mx)
                self.log(f"  LIST {node.point}: KD range search našiel {len(res)} hodnôt")
                matches.extend(res)
                continue
            self.log(f"  vnútorný uzol {node.point}")
            for q in range(4):
                ch = node.children[q]
                if ch is None:
                    self.log(f"    kvadrant {q}: nemá potomka (nič na preskúmanie)")
                    continue
                self.log("    " + explain_quadrant(node.point, q, mn, mx))
                if intersects_quadrant(node.point, q, mn, mx):
                    stack.append(ch)
        return matches

    range_query = None

    def draw_regions(self, d, selected=None):
        path_ids = {id(n) for n in self.path}
        split_ids = {id(n) for n in self.last_split}

        def fullness_color(n):
            r = min(1.0, n.distinct / CFG["cap"])
            red, green = int(150 + 100 * r), int(230 - 90 * r)
            return f"#{red:02x}{green:02x}a0"

        def rec(n, x0, y0, x1, y1):
            if n is None:
                return
            if n.is_leaf():
                chosen = n is selected
                d.rect(x0, y0, x1, y1, fullness_color(n),
                       outline="#06c" if chosen else "#f80" if id(n) in path_ids else "#6a6",
                       width=3 if chosen or id(n) in path_ids else 1)
                d.text((x0 + x1) / 2, (y0 + y1) / 2,
                       f"LIST\n{n.distinct}/{CFG['cap']} rôznych")
                n.data.draw_regions(d, (x0, y0, x1, y1))
                return
            if id(n) in split_ids:
                d.rect(x0, y0, x1, y1, "", outline="#f80", width=3, dash=(6, 3))
            cx, cy = n.point
            d.line(cx, y0, cx, y1, "#222", 2)
            d.line(x0, cy, x1, cy, "#222", 2)
            d.marker(cx, cy)
            for q in range(4):
                rec(n.children[q],
                    cx if q & 1 else x0, cy if q & 2 else y0,
                    x1 if q & 1 else cx, y1 if q & 2 else cy)
        rec(self.root, 0, 0, DOMAIN, DOMAIN)
        if self.range_query and self.root:
            draw_skipped(d, self.root, self.range_query)


STRUCTURES = {"KDTree": KDTree, "QuadTree": QuadTree, "QuadKDTree": QuadKDTree}


# ---------------------------------------------------------------- GUI
class Drawer:
    def __init__(self, canvas):
        self.c = canvas

    @staticmethod
    def px(x, y):
        return MARGIN + x * SCALE, MARGIN + (DOMAIN - y) * SCALE

    def line(self, x0, y0, x1, y1, color, width=1):
        a, b = self.px(x0, y0), self.px(x1, y1)
        self.c.create_line(*a, *b, fill=color, width=width)

    def rect(self, x0, y0, x1, y1, fill, outline="#6a6", width=1, dash=None):
        a, b = self.px(x0, y0), self.px(x1, y1)
        self.c.create_rectangle(*a, *b, fill=fill, outline=outline, width=width, dash=dash)

    def shade(self, x0, y0, x1, y1):
        a, b = self.px(x0, y0), self.px(x1, y1)
        self.c.create_rectangle(*a, *b, fill="#777", stipple="gray50", outline="")
        self.c.create_text((a[0] + b[0]) / 2, (a[1] + b[1]) / 2, text="PRESKOČENÉ",
                           fill="#333", font=("Segoe UI", 7, "bold"))

    def marker(self, x, y):
        a = self.px(x, y)
        self.c.create_rectangle(a[0] - 4, a[1] - 4, a[0] + 4, a[1] + 4, fill="black")

    def text(self, x, y, s):
        a = self.px(x, y)
        self.c.create_text(*a, text=s, fill="#363", font=("Segoe UI", 8))


class App:
    def __init__(self, root):
        self.root = root
        root.title("Vizualizácia priestorových stromov")
        self.items = []          # (id, (x, y)) v poradí vkladania
        self.rejected = set()
        self.next_id = 1
        self.tree = KDTree(self.write)
        self.range_rect = None
        self.matches = []
        self.found_point = None
        self.drag = None
        self.selected_leaf = None
        self.node_boxes = []
        self.range_done = False

        top = ttk.Frame(root)
        top.pack(fill="x", padx=6, pady=4)
        self.struct_var = tk.StringVar(value="QuadTree")
        ttk.Label(top, text="Štruktúra:").pack(side="left")
        cb = ttk.Combobox(top, textvariable=self.struct_var, values=list(STRUCTURES), width=12, state="readonly")
        cb.pack(side="left", padx=4)
        cb.bind("<<ComboboxSelected>>", lambda e: self.rebuild())
        self.mode = tk.StringVar(value="insert")
        for t, v in (("Vlož", "insert"), ("Nájdi", "find"), ("Oblasť", "range")):
            ttk.Radiobutton(top, text=t, value=v, variable=self.mode).pack(side="left", padx=2)
        self.depth_var = tk.StringVar(value=str(CFG["max_depth"]))
        self.cap_var = tk.StringVar(value=str(CFG["cap"]))
        ttk.Label(top, text="MAX_DEPTH").pack(side="left", padx=(10, 0))
        ttk.Entry(top, textvariable=self.depth_var, width=4).pack(side="left")
        ttk.Label(top, text="LEAF_CAPACITY").pack(side="left", padx=(6, 0))
        ttk.Entry(top, textvariable=self.cap_var, width=4).pack(side="left")
        ttk.Button(top, text="Použiť", command=self.apply_cfg).pack(side="left", padx=4)
        ttk.Button(top, text="10 náhodných", command=self.add_random).pack(side="left", padx=2)
        ttk.Button(top, text="Reset", command=self.reset).pack(side="left", padx=2)
        self.xy = tk.StringVar()
        ttk.Entry(top, textvariable=self.xy, width=8).pack(side="left", padx=(10, 0))
        ttk.Button(top, text="Vlož x,y", command=self.insert_typed).pack(side="left", padx=2)

        mid = ttk.Frame(root)
        mid.pack(fill="both", expand=True, padx=6)
        size = int(DOMAIN * SCALE + 2 * MARGIN)
        self.canvas = tk.Canvas(mid, width=size, height=size, bg="white")
        self.canvas.pack(side="left")
        self.canvas.bind("<ButtonPress-1>", self.on_press)
        self.canvas.bind("<B1-Motion>", self.on_drag)
        self.canvas.bind("<ButtonRelease-1>", self.on_release)

        right = ttk.Frame(mid)
        right.pack(side="left", fill="both", expand=True, padx=(6, 0))
        tf = ttk.Frame(right)
        tf.pack(fill="both", expand=True)
        self.tcanvas = tk.Canvas(tf, bg="#fafafa", height=300, width=520)
        sx = ttk.Scrollbar(tf, orient="horizontal", command=self.tcanvas.xview)
        sy = ttk.Scrollbar(tf, orient="vertical", command=self.tcanvas.yview)
        self.tcanvas.configure(xscrollcommand=sx.set, yscrollcommand=sy.set)
        sx.pack(side="bottom", fill="x")
        sy.pack(side="right", fill="y")
        self.tcanvas.pack(side="left", fill="both", expand=True)
        self.tcanvas.bind("<Button-1>", self.on_tree_click)
        self.kcanvas = tk.Canvas(right, bg="#f4f8ff", height=150, width=520)
        self.kcanvas.pack(fill="x", pady=(4, 0))
        self.logbox = tk.Text(right, height=12, width=70, font=("Consolas", 9))
        self.logbox.pack(fill="both", expand=True, pady=(4, 0))

        self.rebuild()
        self.write("Klikni do plochy vľavo. Mód 'Vlož' vkladá body a v logu vidíš každý krok algoritmu.")

    # --- helpers
    def write(self, msg):
        self.logbox.insert("end", msg + "\n")
        self.logbox.see("end")

    def to_point(self, event):
        x = round((event.x - MARGIN) / SCALE)
        y = round(DOMAIN - (event.y - MARGIN) / SCALE)
        return max(0, min(DOMAIN, x)), max(0, min(DOMAIN, y))

    def apply_cfg(self):
        try:
            CFG["max_depth"] = max(0, int(self.depth_var.get()))
            CFG["cap"] = max(1, int(self.cap_var.get()))
        except ValueError:
            self.write("Neplatné nastavenie.")
            return
        self.rebuild()

    def reset(self):
        self.items.clear()
        self.rejected.clear()
        self.logbox.delete("1.0", "end")
        self.rebuild()

    def rebuild(self):
        self.tree = STRUCTURES[self.struct_var.get()](noop)
        self.rejected.clear()
        for it in self.items:
            if not self.tree.insert(it):
                self.rejected.add(it[0])
        self.tree.log = self.write
        self.tree.path = []
        self.range_rect = None
        self.matches = []
        self.found_point = None
        self.selected_leaf = None
        self.write(f"--- {self.tree.name} postavený nanovo z {len(self.items)} bodov "
                   f"(MAX_DEPTH={CFG['max_depth']}, LEAF_CAPACITY={CFG['cap']}) ---")
        self.redraw()

    def add_point(self, p):
        value = (self.next_id, p)
        self.next_id += 1
        self.items.append(value)
        self.write(f"\nINSERT #{value[0]} {p} do {self.tree.name}:")
        ok = self.tree.insert(value)
        if not ok:
            self.rejected.add(value[0])
        self.range_rect = None
        self.matches = []
        self.found_point = None
        self.redraw()

    def add_random(self):
        for _ in range(10):
            self.add_point((random.randint(0, DOMAIN), random.randint(0, DOMAIN)))

    def insert_typed(self):
        try:
            x, y = (int(s) for s in self.xy.get().replace(" ", "").split(","))
            self.add_point((max(0, min(DOMAIN, x)), max(0, min(DOMAIN, y))))
        except ValueError:
            self.write("Zadaj vo formáte: 50,70")

    # --- mouse
    def on_press(self, e):
        self.range_done = False
        self.drag = self.to_point(e)

    def on_drag(self, e):
        if self.mode.get() == "range" and self.drag:
            p = self.to_point(e)
            self.range_rect = (min(self.drag[0], p[0]), min(self.drag[1], p[1]),
                               max(self.drag[0], p[0]), max(self.drag[1], p[1]))
            self.redraw()

    def on_release(self, e):
        p = self.to_point(e)
        mode = self.mode.get()
        if mode == "insert":
            self.add_point(p)
        elif mode == "find":
            self.write(f"\nFIND {p}:")
            res = self.tree.find(p)
            self.found_point = p if res else None
            self.write(f"  výsledok: {[v[0] for v in res] if res else 'nič'}")
            self.matches = []
            self.range_rect = None
            self.redraw()
        elif mode == "range" and self.drag:
            r = self.range_rect or (p[0], p[1], p[0], p[1])
            self.range_rect = r
            mn, mx = (r[0], r[1]), (r[2], r[3])
            self.write(f"\nRANGE {mn} - {mx}:")
            self.matches = self.tree.range_search(mn, mx)
            self.write(f"  výsledok: {len(self.matches)} hodnôt {[v[0] for v in self.matches]}")
            self.found_point = None
            self.range_done = True
            self.redraw()
        self.drag = None

    # --- drawing
    def redraw(self):
        c = self.canvas
        c.delete("all")
        d = Drawer(c)
        a, b = d.px(0, 0), d.px(DOMAIN, DOMAIN)
        c.create_rectangle(*a, *b, outline="black")
        r = self.range_rect if self.mode.get() == "range" and self.range_done else None
        self.tree.range_query = ((r[0], r[1]), (r[2], r[3])) if r else None
        if isinstance(self.tree, QuadKDTree):
            self.tree.draw_regions(d, self.selected_leaf)
        else:
            self.tree.draw_regions(d)

        path_pts = {n.point for n in self.tree.path}
        match_pts = {v[1] for v in self.matches}
        counts = {}
        for it in self.items:
            counts.setdefault(it[1], []).append(it[0])
        for pt, ids in counts.items():
            x, y = d.px(*pt)
            rejected = all(i in self.rejected for i in ids)
            color = "#bbb" if rejected else "#222"
            if pt in path_pts:
                color = "#f80"
            if pt in match_pts:
                color = "#0a0"
            if pt == self.found_point:
                color = "#0a0"
            r = 4
            if rejected:
                c.create_oval(x - r, y - r, x + r, y + r, outline="#999")
            else:
                c.create_oval(x - r, y - r, x + r, y + r, fill=color, outline=color)
            label = f"{pt}" + (f" x{len(ids)}" if len(ids) > 1 else "")
            c.create_text(x + 6, y - 8, text=label, anchor="w", font=("Segoe UI", 7), fill="#555")
        if self.range_rect:
            r = self.range_rect
            p0, p1 = d.px(r[0], r[1]), d.px(r[2], r[3])
            c.create_rectangle(*p0, *p1, outline="#c0c", width=2, dash=(4, 2))
        if self.found_point is None and self.mode.get() == "find" and not self.tree.path:
            pass
        self.draw_tree()

    def on_tree_click(self, e):
        if not isinstance(self.tree, QuadKDTree):
            return
        x, y = self.tcanvas.canvasx(e.x), self.tcanvas.canvasy(e.y)
        for x0, y0, x1, y1, n in self.node_boxes:
            if x0 <= x <= x1 and y0 <= y <= y1 and n.is_leaf():
                self.selected_leaf = n
                self.write(f"\nVybraný list {n.point}: jeho KD strom je dole (klik na list v diagrame).")
                self.redraw()
                return

    def draw_tree(self):
        self.node_boxes = []
        leaf = None
        if isinstance(self.tree, QuadKDTree):
            leaves = [n for n in self.tree.path if n.is_leaf()]
            if leaves:
                self.selected_leaf = leaves[-1]
            if self.selected_leaf is not None and not self.selected_leaf.is_leaf():
                self.selected_leaf = None
            leaf = self.selected_leaf
        self.draw_generic(self.tcanvas, self.tree.root, {id(n) for n in self.tree.path},
                          record=True, selected=leaf)
        kc = self.kcanvas
        if leaf is None:
            kc.delete("all")
            note = "KD strom listu (len pri QuadKDTree)" if not isinstance(self.tree, QuadKDTree) \
                else "klikni na list v diagrame"
            kc.create_text(10, 10, anchor="nw", text=note, fill="#888")
        else:
            self.draw_generic(kc, leaf.data.root, set(), title=f"KD strom v liste {leaf.point} "
                              f"({leaf.distinct}/{CFG['cap']} rôznych bodov)")

    def draw_generic(self, tc, root, path_ids, record=False, selected=None, title=None):
        tc.delete("all")
        if root is None:
            return
        pos = {}
        counter = [0]
        max_depth = [0]
        top = 20 if title else 0
        if title:
            tc.create_text(6, 4, anchor="nw", text=title, fill="#036", font=("Segoe UI", 9, "bold"))

        def layout(n, depth):
            max_depth[0] = max(max_depth[0], depth)
            kids = n.kids()
            if not kids:
                x = counter[0]
                counter[0] += 1
            else:
                xs = [layout(c, depth + 1) for _, c in kids]
                x = sum(xs) / len(xs)
            pos[id(n)] = (x, depth)
            return x

        layout(root, 0)
        dx, dy = 70, 58

        def coord(n):
            x, depth = pos[id(n)]
            return 40 + x * dx, top + 30 + depth * dy

        def draw(n):
            x, y = coord(n)
            for key, ch in n.kids():
                cx, cy = coord(ch)
                tc.create_line(x, y, cx, cy, fill="#999")
                tc.create_text((x + cx) / 2, (y + cy) / 2, text=key, fill="#06c", font=("Segoe UI", 8, "bold"))
                draw(ch)
            hl = id(n) in path_ids
            leafnode = isinstance(n, QuadKDNode) and n.is_leaf()
            fill = "#ffd9a0" if hl else ("#d7f0d7" if leafnode else "white")
            tc.create_rectangle(x - 32, y - 14, x + 32, y + 14, fill=fill,
                                outline="#06c" if n is selected else "#555",
                                width=3 if n is selected else 1)
            tc.create_text(x, y, text=n.label(), font=("Segoe UI", 7))
            if record:
                self.node_boxes.append((x - 32, y - 14, x + 32, y + 14, n))

        draw(root)
        tc.configure(scrollregion=(0, 0, 80 + counter[0] * dx, top + 60 + (max_depth[0] + 1) * dy))


if __name__ == "__main__":
    tk_root = tk.Tk()
    App(tk_root)
    tk_root.mainloop()
