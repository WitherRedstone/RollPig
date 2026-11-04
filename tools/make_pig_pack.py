"""给「加了很多只猪」的整合包作者用的工具：生成聊天图标资源包。

背景：聊天卡片上的猪头，是把「字体图集」里的一个字符画出来的。
模组自带的图集只画了内置的 165 只猪，外加 1 个通用灰色占位。
整合包自己加了猪，默认只能用那个灰色占位。

这个脚本会：
  1. 从模组 jar 里取出内置图集与内置 id → 码位 对照表；
  2. 把你的自定义猪（json + png）一张张贴到图集后面；
  3. 输出一个资源包目录 resourcepacks/roll_pig_custom/（含新图集与字体定义）；
  4. 顺手写好 config/roll_pig/pig_glyphs.json（新 id → 新码位）。

资源包放好后进游戏启用，自定义猪在聊天卡片里就有自己的头像了。
服务器也要放同一份 config/roll_pig/pig_glyphs.json（卡片文字是服务端拼的）。

用法：
    python tools/make_pig_pack.py \
        --jar  build/libs/roll_pig-1.0.0-beta5-mc1.21.1-neoforge.jar \
        --extra config/roll_pig \
        --pack  resourcepacks/roll_pig_custom

  --extra 的目录结构跟 config/roll_pig 完全一样：
        <extra>/pigsty/<id>.json
        <extra>/image/<id>.png

依赖 Pillow：pip install pillow
"""
import argparse
import json
import math
import os
import sys
import zipfile

from PIL import Image

CELL = 64
GLYPH_HEIGHT = 36
GLYPH_ASCENT = 8
BUILTIN_MANIFEST = "assets/roll_pig/pig_glyphs.json"
BUILTIN_ATLAS = "assets/roll_pig/textures/font/pig_icons.png"
PACK_FORMAT = 34          # Minecraft 1.21.1 资源包版本号


def load_builtin(jar_path):
    """从 jar 里读内置图集与内置对照表。"""
    with zipfile.ZipFile(jar_path) as jar:
        manifest = json.loads(jar.read(BUILTIN_MANIFEST).decode("utf-8"))
        with jar.open(BUILTIN_ATLAS) as handle:
            atlas = Image.open(handle).convert("RGBA")
            atlas.load()
    return manifest, atlas


def collect_extra(extra_dir):
    """读 --extra 里的自定义猪，返回 [(id, png_path), ...]，按 id 排序。"""
    pigsty = os.path.join(extra_dir, "pigsty")
    images = os.path.join(extra_dir, "image")
    if not os.path.isdir(pigsty):
        return []

    found = {}
    for name in sorted(os.listdir(pigsty)):
        if not name.endswith(".json"):
            continue
        path = os.path.join(pigsty, name)
        fallback_id = name[:-len(".json")]
        try:
            with open(path, encoding="utf-8-sig") as handle:
                data = json.load(handle)
        except Exception as error:
            print("  跳过（读取失败）:", path, error)
            continue
        entries = data if isinstance(data, list) else [data]
        for entry in entries:
            if not isinstance(entry, dict):
                continue
            pig_id = entry.get("id", fallback_id)
            png = os.path.join(images, pig_id + ".png")
            if not os.path.isfile(png):
                print("  跳过（没有图片）:", pig_id, "->", png)
                continue
            found[pig_id] = png
    return sorted(found.items())


def main():
    parser = argparse.ArgumentParser(description="生成包含自定义猪猪的聊天图标资源包")
    parser.add_argument("--jar", required=True, help="模组 jar 路径")
    parser.add_argument("--extra", required=True, help="含 pigsty/ 与 image/ 的目录（同 config/roll_pig）")
    parser.add_argument("--pack", required=True, help="资源包输出目录")
    parser.add_argument("--config", default=None,
                        help="config/roll_pig 输出目录（默认取 --extra 本身，用它自己的 config 目录）")
    parser.add_argument("--description", default="Roll Pig 自定义猪猪图标")
    args = parser.parse_args()

    builtin_manifest, builtin_atlas = load_builtin(args.jar)
    src_cols = max(1, builtin_atlas.width // CELL)
    src_rows = max(1, builtin_atlas.height // CELL)
    src_cells = src_cols * src_rows

    extras = collect_extra(args.extra)
    if not extras:
        print("没有找到自定义猪猪（需要 <extra>/pigsty/*.json 与 <extra>/image/<id>.png）")
        return 1

    # 内置格子原样保留（码位不变），自定义猪接在后面，码位顺延
    cells = []
    for index in range(src_cells):
        cx = (index % src_cols) * CELL
        cy = (index // src_cols) * CELL
        cells.append(builtin_atlas.crop((cx, cy, cx + CELL, cy + CELL)))

    manifest = dict(builtin_manifest)
    # 码位 = U+E000 + 图集格子序号，所以新猪紧接在内置格子之后顺延
    next_codepoint = 0xE000 + src_cells
    for pig_id, png in extras:
        art = Image.open(png).convert("RGBA")
        if art.size != (CELL, CELL):
            art = art.resize((CELL, CELL), Image.LANCZOS)
        cells.append(art)
        manifest[pig_id] = next_codepoint
        next_codepoint += 1

    total = len(cells)
    cols = math.ceil(math.sqrt(total))
    rows = math.ceil(total / cols)
    atlas = Image.new("RGBA", (cols * CELL, rows * CELL), (0, 0, 0, 0))
    for index, cell in enumerate(cells):
        atlas.alpha_composite(cell, ((index % cols) * CELL, (index // cols) * CELL))

    chars = []
    for row in range(rows):
        line = []
        for col in range(cols):
            index = row * cols + col
            # 空位继续用私用区占位字符，避免重复字符
            line.append(chr(0xE000 + index) if index < total else chr(0xF000 + (index - total)))
        chars.append("".join(line))

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

    pack_dir = args.pack
    pack_assets = os.path.join(pack_dir, "assets", "roll_pig")
    os.makedirs(os.path.join(pack_assets, "font"), exist_ok=True)
    os.makedirs(os.path.join(pack_assets, "textures", "font"), exist_ok=True)

    atlas.save(os.path.join(pack_assets, "textures", "font", "pig_icons.png"))
    with open(os.path.join(pack_assets, "font", "pig_icons.json"), "w", encoding="utf-8", newline="\n") as handle:
        json.dump(font, handle, ensure_ascii=True, indent=2)
        handle.write("\n")
    with open(os.path.join(pack_dir, "pack.mcmeta"), "w", encoding="utf-8", newline="\n") as handle:
        json.dump({"pack": {"pack_format": PACK_FORMAT, "description": args.description}},
                  handle, ensure_ascii=False, indent=2)
        handle.write("\n")

    config_dir = args.config or args.extra
    os.makedirs(config_dir, exist_ok=True)
    with open(os.path.join(config_dir, "pig_glyphs.json"), "w", encoding="utf-8", newline="\n") as handle:
        json.dump(manifest, handle, ensure_ascii=True, indent=0, sort_keys=True)
        handle.write("\n")

    print("资源包:", pack_dir)
    print("  图集:", atlas.size, "格数:", total, "(%d 内置 + %d 自定义)" % (src_cells, len(extras)))
    print("  字体行数:", rows)
    print("对照表:", os.path.join(config_dir, "pig_glyphs.json"), "共", len(manifest), "条")
    for pig_id, _ in extras:
        print("  %-24s U+%04X" % (pig_id, manifest[pig_id]))
    print()
    print("下一步：")
    print("  1. 把", pack_dir, "整个文件夹放进游戏目录的 resourcepacks/")
    print("  2. 游戏内 选项 → 资源包 启用它（改图后按 F3+T 重载）")
    print("  3. 服务器也要有同一份", os.path.join(config_dir, "pig_glyphs.json"))
    return 0


if __name__ == "__main__":
    sys.exit(main())
