"""重新生成聊天卡片用的猪猪字体图标。

用法（在项目根目录执行）：
    python tools/gen_pig_font.py

会把 src/main/resources 里的三份产物一起刷新：
    assets/roll_pig/textures/font/pig_icons.png   图集（每格 64px）
    assets/roll_pig/font/pig_icons.json           字体定义（bitmap provider）
    assets/roll_pig/pig_glyphs.json               id -> 码位 对照表

码位从 U+E000 起，因为 E000-F8FF 是 Unicode 私用区（PUA），
永远不会被分配成真正的文字，跟任何语言、任何模组都不冲突。

依赖 Pillow：  pip install pillow
"""
import json
import math
import os
import sys

from PIL import Image

CELL = 64            # 图集里每格边长
GLYPH_HEIGHT = 36    # 聊天栏里一个猪头占的高度（像素）
GLYPH_ASCENT = 8
FALLBACK_KEY = "__custom__"   # 自定义猪猪的通用灰色占位头像

# 项目根目录 = 本脚本所在目录的上一级；也可用命令行参数指定
ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
IMG_DIR = os.path.join(RES, "assets", "roll_pig", "textures", "pigsty", "image")
OUT_ATLAS = os.path.join(RES, "assets", "roll_pig", "textures", "font", "pig_icons.png")
OUT_FONT = os.path.join(RES, "assets", "roll_pig", "font", "pig_icons.json")
OUT_MANIFEST = os.path.join(RES, "assets", "roll_pig", "pig_glyphs.json")


def fallback_art(size):
    """灰掉的经典猪，用作自定义猪猪在聊天里的通用头像。"""
    src = Image.open(os.path.join(IMG_DIR, "pig.png")).convert("RGBA").resize((size, size), Image.LANCZOS)
    px = src.load()
    for y in range(size):
        for x in range(size):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            lum = (r * 299 + g * 587 + b * 114) // 1000
            px[x, y] = ((lum + 150) // 3, (lum + 150) // 3, (lum + 170) // 3, a)
    return src


def main():
    ids = sorted(f[:-4] for f in os.listdir(IMG_DIR) if f.lower().endswith(".png"))
    cells = ids + [FALLBACK_KEY]          # 最后一格留给自定义猪猪的占位图
    total = len(cells)
    cols = math.ceil(math.sqrt(total))
    rows = math.ceil(total / cols)

    atlas = Image.new("RGBA", (cols * CELL, rows * CELL), (0, 0, 0, 0))
    manifest = {}

    for index, key in enumerate(cells):
        if key == FALLBACK_KEY:
            art = fallback_art(CELL)
        else:
            art = Image.open(os.path.join(IMG_DIR, key + ".png")).convert("RGBA")
            if art.size != (CELL, CELL):
                art = art.resize((CELL, CELL), Image.LANCZOS)
        atlas.alpha_composite(art, ((index % cols) * CELL, (index // cols) * CELL))
        manifest[key] = 0xE000 + index

    chars = []
    for row in range(rows):
        line = []
        for col in range(cols):
            i = row * cols + col
            # 多出来的空格位用另一个私用区字符占位，避免重复字符
            line.append(chr(0xE000 + i) if i < total else chr(0xE100 + i))
        chars.append("".join(line))

    os.makedirs(os.path.dirname(OUT_ATLAS), exist_ok=True)
    os.makedirs(os.path.dirname(OUT_FONT), exist_ok=True)
    atlas.save(OUT_ATLAS)

    font = {
        "providers": [
            {
                "type": "bitmap",
                "file": "roll_pig:font/pig_icons.png",
                "height": GLYPH_HEIGHT,
                "ascent": GLYPH_ASCENT,
                "chars": chars,
            }
        ]
    }
    with open(OUT_FONT, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(font, handle, ensure_ascii=True, indent=2)
        handle.write("\n")

    with open(OUT_MANIFEST, "w", encoding="utf-8", newline="\n") as handle:
        json.dump(manifest, handle, ensure_ascii=True, indent=0, sort_keys=True)
        handle.write("\n")

    print("pigs:", len(ids), "cells:", total, "grid:", cols, "x", rows, "atlas:", atlas.size)
    print("fallback codepoint: U+%04X" % manifest[FALLBACK_KEY])


if __name__ == "__main__":
    main()
