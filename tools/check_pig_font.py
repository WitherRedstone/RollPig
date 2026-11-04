"""检查手工改过的聊天图标图集对不对。

如果你不想用脚本自动拼图，而是自己在 Photoshop / Aseprite 里往
pig_icons.png 里画猪，那就用它检查有没有画歪：

    python tools/check_pig_font.py --pack resourcepacks/roll_pig_custom \
                                   --config config/roll_pig

检查项：
  - 图集尺寸是不是 64 的整数倍
  - font json 的 height / ascent / chars 结构是否正确
  - chars 里每一行长度是否一致、字符是否重复
  - 每个码位能否落到图集里的某一格（码位 = U+E000 + 格子序号）
  - pig_glyphs.json 里的 id → 码位 是否都在图集范围内
  - 哪些格子还是空的（空格子可以直接拿来画新猪）
  - 对照表里的 id 在 config 的 pigsty 里有没有对应 json

依赖 Pillow：pip install pillow
"""
import argparse
import json
import os
import sys

from PIL import Image

CELL = 64


def fail(message):
    print("  [错误]", message)
    return 1


def main():
    parser = argparse.ArgumentParser(description="校验聊天图标图集资源包")
    parser.add_argument("--pack", required=True, help="资源包目录（含 assets/roll_pig/font 与 textures/font）")
    parser.add_argument("--config", default=None, help="config/roll_pig 目录（读 pig_glyphs.json 与 pigsty/）")
    args = parser.parse_args()

    pack = args.pack
    font_path = os.path.join(pack, "assets", "roll_pig", "font", "pig_icons.json")
    atlas_path = os.path.join(pack, "assets", "roll_pig", "textures", "font", "pig_icons.png")

    problems = 0
    for path in (font_path, atlas_path):
        if not os.path.isfile(path):
            problems += fail("找不到文件: " + path)
    if problems:
        return 1

    with open(font_path, encoding="utf-8-sig") as handle:
        font = json.load(handle)
    providers = font.get("providers") or []
    if len(providers) != 1:
        return fail("font json 里应该只有一个 provider，实际 %d 个" % len(providers))
    provider = providers[0]
    if provider.get("type") != "bitmap":
        return fail("provider type 必须是 bitmap，实际是 %r" % provider.get("type"))

    atlas = Image.open(atlas_path).convert("RGBA")
    if atlas.width % CELL or atlas.height % CELL:
        return fail("图集尺寸 %s 不是 %d 的整数倍" % (atlas.size, CELL))
    cols = atlas.width // CELL
    rows = atlas.height // CELL
    print("图集: %dx%d 像素 = %d 列 x %d 行 = %d 格" % (atlas.width, atlas.height, cols, rows, cols * rows))

    chars = provider.get("chars") or []
    if len(chars) != rows:
        problems += fail("chars 应该有 %d 行，实际 %d 行" % (rows, len(chars)))
    for index, line in enumerate(chars):
        if len(line) != cols:
            problems += fail("chars 第 %d 行应该有 %d 个字符，实际 %d 个" % (index + 1, cols, len(line)))
    if problems:
        return 1

    codepoints = [ord(ch) for line in chars for ch in line]
    if len(set(codepoints)) != len(codepoints):
        problems += fail("chars 里有重复字符")
    outside = [cp for cp in codepoints if not (0xE000 <= cp <= 0xF8FF)]
    if outside:
        problems += fail("有 %d 个码位不在私用区 U+E000–U+F8FF 内，例如 U+%04X" % (len(outside), outside[0]))

    print("字体: height=%s ascent=%s 行数=%d" % (provider.get("height"), provider.get("ascent"), len(chars)))

    # 逐格判断有没有内容
    empty_cells = []
    for index in range(cols * rows):
        x, y = (index % cols) * CELL, (index // cols) * CELL
        cell = atlas.crop((x, y, x + CELL, y + CELL))
        if cell.getbbox() is None:
            empty_cells.append(index)
    print("空格子: %d 个%s" % (len(empty_cells),
                          ("（序号 " + ", ".join(str(i) for i in empty_cells[:12]) +
                           ("..." if len(empty_cells) > 12 else "") + "，可以直接画新猪）") if empty_cells else ""))

    manifest = {}
    if args.config:
        manifest_path = os.path.join(args.config, "pig_glyphs.json")
        if os.path.isfile(manifest_path):
            with open(manifest_path, encoding="utf-8-sig") as handle:
                manifest = json.load(handle)
            print("对照表: %d 条" % len(manifest))
            cell_of_char = {codepoint: index for index, codepoint in enumerate(codepoints)}
            for pig_id, codepoint in manifest.items():
                if codepoint not in cell_of_char:
                    problems += fail("%s 的码位 U+%04X 不在 chars 里（字符表里没有这个字符）" % (pig_id, codepoint))
                    continue
                index = cell_of_char[codepoint]
                if index in empty_cells:
                    problems += fail("%s 指向第 %d 格，但那一格是空的（还没画图）" % (pig_id, index))

            pigsty = os.path.join(args.config, "pigsty")
            if os.path.isdir(pigsty):
                known = set()
                for name in os.listdir(pigsty):
                    if not name.endswith(".json"):
                        continue
                    fallback = name[:-len(".json")]
                    try:
                        with open(os.path.join(pigsty, name), encoding="utf-8-sig") as handle:
                            data = json.load(handle)
                    except Exception:
                        continue
                    for entry in (data if isinstance(data, list) else [data]):
                        if isinstance(entry, dict):
                            known.add(entry.get("id", fallback))
                missing = sorted(set(manifest) - known - {"__custom__"})
                if missing:
                    print("提示: 对照表里有 %d 个 id 在 pigsty 里找不到 json: %s"
                          % (len(missing), ", ".join(missing[:8]) + ("..." if len(missing) > 8 else "")))
        else:
            print("提示: 没有找到 %s（只检查图集与字体，不检查 id 映射）" % manifest_path)

    if problems:
        print("\n共 %d 个问题，按上面逐条修。" % problems)
        return 1
    print("\n全部检查通过。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
