**English** | [简体中文](README_ZH.md)

# Roll Pig · Today's Pig

## Introduction

Roll one pig every day, keep it in your pigpen, and send it as a chat sticker.

---

## Gameplay

### Rolling

Each player can roll **once** per day. The chance refreshes at dawn (`dayTime % 24000 == 0`), and **chances do not stack** — skipping today does not give you two tomorrow.

- Every pig in the pool has **the same probability**, and **duplicates are allowed** — rolling the same pig again increases its collected count (`Collected ×N`).
- A **Totem of Undying** animation plays when you roll; the chat card only appears after the animation finishes.
- Progress is stored per player in the save (`roll_pig:roll_state` inside `neoforge:attachments`), is kept on death, and is independent per world.

There are three ways to roll:

- **Right-click the "Pigpen" item** — opens the full GUI, where you can page through every pig and your progress
- **`/rollpig` or `/今日猪猪`** — rolls immediately, plays the totem animation and shows the result screen
- **`/rollpig force [target]`** — for OPs: forces a roll **without** consuming today's chance

### Chat stickers

Open the chat input box: there is a small pigpen icon in the bottom-right corner. Click it to open a picker of the pigs you have collected. Clicking a pig inserts `[pig name]` into the input box; once sent, the chat renders it as that pig's little portrait.

> Only **collected** pigs can be sent as stickers.

---

## Commands

- **`/rollpig`**, **`/今日猪猪`** — available to everyone, rolls today's pig
- **`/rollpig force`** — requires OP, forces a roll for yourself without consuming a chance
- **`/rollpig force <targets>`** — requires OP, forces a roll for the given players
- **`/rollpig unlock`** — requires OP, unlocks every pig in the pigpen
- **`/rollpig unlock <targets>`** — requires OP, unlocks every pig for the given players

`unlock` only adds, never removes: pigs you never rolled are bumped to 1, pigs you already rolled keep their higher count — so your "total count" becomes "previous count + number of newly unlocked pigs". After unlocking, the new progress is synced to clients immediately, so the sticker picker works right away; if the pigpen GUI is open, it refreshes in place.

---

## Custom pigs

Just drop files into the game directory.
There are only three things you actually need to care about:

- `config/roll_pig/pigsty/<id>.json` — this pig's text
- `config/roll_pig/image/<id>.png` — this pig's picture
- (optional) a resource pack + `config/roll_pig/pig_glyphs.json` — only affects **the 36-pixel portrait in the top-left of the chat card**

**First, tell the "three images" apart** — this is where most people get stuck:

- **The big portrait and the list thumbnails in the pigpen GUI** — read straight from `config/roll_pig/image/<id>.png`, so the **client** must have that file.
- **Chat stickers** (sending `[pig name]` turns into a small picture) — also read from that same original file, drawn at 12 pixels, so the **client** needs the file too.
- **The 36px portrait in the top-left of the chat card** (the `【xxx's Today's Pig】` card) — this is **not a picture but a character in a font**, taken from the atlas `pig_icons.png`. To show a new pig there you must replace the atlas with a resource pack **and** tell the mod "which id maps to which cell" via the lookup table.

In short:

- If you only want the pig to be rollable, viewable and usable as a sticker → section 1 is all you need.
- If you also want the new pig on the chat card → continue with sections 5 and 6 (or draw it by hand, section 7).

### 1. Add a pig of your own

**Step 1: create the directories.** **You don't have to** — on the first launch of the game (or server) the mod creates `pigsty/` and `image/` under `config/roll_pig/` automatically, plus a `README.txt` explaining what the two folders are for. You just drop files in:

```
config/roll_pig/
├─ README.txt              ← generated automatically; deleted? it comes back next launch
├─ pigsty/                 ← one .json per pig
│   └─ my-pig.json
└─ image/                  ← pictures, file name = id
    └─ my-pig.png
```

Note that these are **two sibling directories**: text goes into `pigsty/`, pictures go into `image/`. Do not put pictures inside `pigsty/`.

**Step 2: write the text.** Create `config/roll_pig/pigsty/my-pig.json` and save it as **UTF-8**:

```json
{
  "id": "my-pig",
  "name": "My Pig",
  "description": "A short one-liner",
  "analysis": "A long description of this pig. It can be very long: the GUI wraps it automatically, and anything that does not fit can be scrolled with the mouse wheel."
}
```

**Step 3: add the picture.** Put it at `config/roll_pig/image/my-pig.png`; the file name must match the `id` exactly. It must be a **real PNG** file (see section 3 for how to tell), 240×240 or larger is recommended, square, with a transparent background.

**Step 4: restart.** Pig data is read once at startup and pictures are loaded when you enter a world, so restart the client / server after changing anything (in single player, quitting to the title and re-entering also works).

**Step 5: check the log.** At startup the console prints:

```
[co.ch.ro.RollPig/]: 猪圈里共 166 只猪猪（其中自定义 1 只）
```

If you see "其中自定义 1 只" (of which 1 custom), it was loaded. If it only says 165 pigs with no parentheses, the json was not found — double-check the path, the extension (is it `my-pig.json.txt`?) and the encoding.

**Step 6: confirm three things in game.**

- Right-click the "Pigpen" item, page to the last page: "My Pig" should be in the list, and its detail should have a picture, a description and an analysis.
- `/rollpig force` (OP) or simply roll until you get it: confirm the totem animation plays and the chat card appears.
- After collecting it, open the chat input and click the small pig icon in the bottom-right: the pig should be clickable in the panel, and sending it should render as a small picture in chat.

When all three work, the pig is fully hooked up.

### 2. Field reference

- **`id`** — unique identifier, and also the **picture file name**. If omitted, the json file name is used.
- **`name`** — display name. Used in the list, the detail title, command output and the chat card. Required.
- **`description`** — short description. The line under the name on the detail page, and the "Description: 「…」" in the chat card.
- **`analysis`** — long description. The scrollable area at the bottom of the detail page, and the body of 【Overview】 in the chat card.

Two formatting rules:

- A file may contain **a single object** or **an array of objects**, defining several pigs at once.
- Save files as UTF-8. A BOM is tolerated (the mod strips it), but no BOM is recommended.

**`id` must use lowercase ASCII letters, digits, `-` and `_`.** Why: the mod lowercases the id, replaces every character that is not `a-z`, `0-9`, `-`, `_`, `.` or `/` with `_`, and then looks for the picture using the result. So an `id` of `我的猪` becomes `__`, and the picture would have to be named `__.png` — it works, but the bookkeeping is a mess, so avoid non-ASCII ids (put Chinese in `name` instead).

### 3. Picture requirements

- **It must be a real PNG.** This is the most common pitfall: browser "Save as" often gives you webp / gif / avif, and renaming the extension to `.png` does not help — the game only accepts PNG bytes.
  - First check: open it with the system image viewer. If it opens, the format is at least common, but the safer move is to **save it again as PNG** and use that.
  - Second check: look at the file header. A PNG starts with the 8 bytes `89 50 4E 47 0D 0A 1A 0A`; if you see `47 49 46 38` (`GIF8`) or `52 49 46 46` (`RIFF`, webp) it is not a PNG.
  - Third check: read the log. On failure the mod prints `无法加载猪猪图片 xxx：只支持真正的 PNG 文件` together with the full path it was looking at.
- Any size is fine; the image is scaled **proportionally** and centered inside the box, so it is never squashed. **240×240 or 512×512 square** is recommended; very wide or very tall images look small inside the 48×48 box.
- **A transparent background (RGBA) is recommended.** An opaque white square is very visible against the dark GUI.
- One picture per id, file name must match: if `id` is `my-pig`, the picture is `my-pig.png`.
- Having lots of pictures is fine — the mod only loads them when they actually need to be displayed, so hundreds of images are not dumped into VRAM at once.

### 4. Replacing a built-in pig

If you only want to change the text or swap the picture without adding a new id, simply **override with the same id**:

```json
// config/roll_pig/pigsty/pig.json
{ "id": "pig", "name": "Pig", "description": "Ordinary little pig", "analysis": "……" }
```

Then drop `config/roll_pig/image/pig.png` in place to replace the built-in "Pig". Progress is tracked by id, so players keep their existing counts.

A built-in pig's id is the file name under `data/roll_pig/pigsty/*.json` inside the jar (165 pigs in total). To check a specific id, look at the name in the in-game pigpen, or unzip the jar and read the file names.

### 5. Where the chat images actually come from

This is the most confusing part, so it gets its own section. The mod uses two completely different things in chat:

**Stickers (inline pictures)** — when you send `[pig name]`, a special 12×12 character slot is inserted into the message, and the client draws the **original picture texture** at that position. Therefore:

- It uses the original `config/roll_pig/image/<id>.png`.
- It shows up as long as the client has that file; **no resource pack needed**.
- It only works for **collected** pigs.

**Card portrait (font glyph)** — the 36-pixel picture in the top-left of the `【xxx's Today's Pig】` card. Minecraft's chat cannot draw arbitrary images, only **glyphs from a font**, so the mod ships an atlas `pig_icons.png` (13×13 cells of 64×64), each cell registered as a Private Use Area character, and the card contains "that character".

- It goes through the **font**, not a texture, so the client must be able to resolve that glyph.
- The bundled atlas only covers the 165 built-in pigs; newly added pigs are not in it → they fall back to the **generic grey pig head** (a reserved placeholder cell).
- To give a new pig its own card portrait you must **replace the atlas** (resource pack) and register id → cell in the **lookup table**.

The trade-off is clear: **stickers are free to add, card portraits need extra packaging.** If a whole modpack's pigs need their own portraits, use the script in section 6; if you only add two or three and want to draw by hand, see section 7.

### 6. Give custom pigs a card portrait (recommended: one command)

The tool is `tools/make_pig_pack.py` in the mod. It will:

1. read the built-in atlas and lookup table from the jar;
2. take every new pig under `config/roll_pig/image/` that is not in the built-in table, scale each to 64×64 and append it after the atlas;
3. relayout the atlas into a square, assigning the new cells codepoints after `U+E000` (built-in cell numbers stay unchanged);
4. output a **resource pack directory** and write `config/roll_pig/pig_glyphs.json`.

**Step 1: make sure the pigs are there.** Put the new pigs' json and pictures into `config/roll_pig/` (as in section 1) and launch once to confirm the count in the log is right.

**Step 2: run the script.** From the mod directory (Windows Command Prompt or PowerShell, same thing):

```bash
python tools/make_pig_pack.py --jar mods/roll_pig-1.0.0-mc1.21.1-neoforge.jar --extra config/roll_pig --pack resourcepacks/roll_pig_custom
```

What the arguments mean:

- `--jar` — path to the mod jar; the script reads the built-in atlas from it.
- `--extra` — the directory holding your custom pigs; it must contain `pigsty/` and `image/`.
- `--pack` — resource pack output directory; the script creates it.
- `--config` — optional, output directory for the lookup table; defaults to `--extra`.

Typical output:

```
资源包: resourcepacks/roll_pig_custom
  图集: (896, 832) 格数 170 (169 内置 + 1 自定义)
对照表: config/roll_pig/pig_glyphs.json 共 167 条
  test-pig                 U+E0A9
```

How to read it: the 169 built-in cells are kept as-is, your pigs start at cell 170; the table gained an entry `test-pig → U+E0A9`, which is its character in the atlas.

**Step 3: install the resource pack.** Put the whole `roll_pig_custom` folder into the game's `resourcepacks/`, then enable it in "Options → Resource Packs" (click the arrow to move it to the right-hand list). After changing images, press `F3 + T` to reload resource packs; no game restart needed.

**Step 4: place the lookup table.** The script already wrote `config/roll_pig/pig_glyphs.json`. **Both the client and the server need the same copy** (the card text is composed on the server while the glyph is drawn on the client, and the codepoints must match).

**Step 5: restart and verify.**

- The startup log gains a line: `扩展头像对照表: 167 条（新增 1）`. Seeing it means the table was read.
- Roll a custom pig and check whether the card's top-left shows its own portrait.
- If the line is there but the portrait is still grey: the resource pack is not enabled, or the atlas path inside the pack is wrong.

**Step 6: what about adding or removing pigs later?** Every time you change `config/roll_pig/`, re-run the script, re-enable the resource pack (`F3 + T`) and restart the server. The script always **recomputes everything**, so it cannot accumulate errors — but that also means **it rewrites `pig_glyphs.json` wholesale**, overwriting anything you wrote by hand. Do not mix script and hand-drawn workflows.

### 7. Drawing the atlas by hand (advanced, no script)

The atlas is just **a PNG arranged on a grid** — there is no magic, and drawing it yourself is perfectly valid. Three rules:

1. The atlas is made of **64×64 cells**; width and height must both be divisible by 64; cells are numbered "left to right, top to bottom".
2. `chars` in `pig_icons.json` is an array of strings: **the n-th string is the n-th row of cells, and the n-th character in a row is the n-th cell**; every row must be as long as the number of columns.
3. `pig_glyphs.json` maps "pig id" to "the character in some cell".

**The built-in atlas leaves 3 free cells you can use for nothing.** The atlas is 13×13 = 169 cells: 0–164 are the 165 built-in pigs, 165 is the generic grey head, and **cells 166, 167 and 168 are empty**, with characters already reserved:

```
cell 166 → U+E1A6 (57766), top-left pixel (640, 768)
cell 167 → U+E1A7 (57767), top-left pixel (704, 768)
cell 168 → U+E1A8 (57768), top-left pixel (768, 768)
```

So for 1–3 hand-drawn pigs, **you don't even have to touch `pig_icons.json`**:

1. Extract `assets/roll_pig/textures/font/pig_icons.png` from the jar (a jar is a zip; any unzip tool opens it).
2. Paste your 64×64 picture at pixel `(640, 768)`. Do not rescale, do not move the grid lines.
3. Create a resource pack containing only that file: `roll_pig_custom/assets/roll_pig/textures/font/pig_icons.png`. No font definition needed — the one inside the jar is used. Set `pack_format` to `34` (1.21.1) in `pack.mcmeta`.
4. Add an entry `{ "my-pig": 57766 }` to `config/roll_pig/pig_glyphs.json`.
5. Place the pig's own json and picture as usual.

**More than 3 pigs means editing `pig_icons.json`**: add a row of cells (height +64), add an equally long line of characters to `chars`, assign unused codepoints to the new cells (continue with `U+E1A9`, `U+E1AA`, … — anything inside the Private Use Area), and point the lookup table at them.

After editing, run the checker; it will point out problems:

```bash
python tools/check_pig_font.py --pack resourcepacks/roll_pig_custom --config config/roll_pig
```

It checks: whether the atlas size is a multiple of 64, the width of each `chars` row, duplicate characters, whether all codepoints are inside the Private Use Area, whether every id in the lookup table points at an existing and non-empty cell, and whether the matching json exists under `pigsty/`. When everything passes it prints "all checks passed".

### 8. Why codepoints start at 57344

`57344` = `U+E000`, the start of Unicode's **Private Use Area (U+E000–U+F8FF)**. These codepoints are permanently reserved by the standard for private use and will never be assigned to real text, so using them as "picture characters" cannot collide with any language or any other mod.

The mod does not hardcode an order; it looks things up in `pig_glyphs.json` (id → codepoint), so adding or removing pigs and relaying out the atlas never scrambles icons in existing saves. The built-in table lives in the jar, yours lives in `config/roll_pig/pig_glyphs.json`, and when reading they are merged: **same id overrides, new ids are appended** to the built-in table.

---

## Notes

- **You don't have to create the directories.** On startup `config/roll_pig/pigsty` and `config/roll_pig/image` are created, along with a `README.txt`; if the log says "已创建自定义猪猪目录", they are ready.
- **Restart after every change.** Pig data is read once at startup; pictures are loaded when entering a world, so re-enter the save or restart the client after adding pictures (textures are cached). Only resource packs can be hot-reloaded with `F3 + T`.
- **Do not mix up the two directories.** `config/roll_pig/pigsty/` holds only `.json`, `config/roll_pig/image/` holds only `.png`.
- **Pictures must be real PNGs.** Renaming gif / jpg / webp does not work; the log will say `只支持真正的 PNG 文件`.
- **Use lowercase ASCII for `id`.** Chinese or uppercase gets turned into underscores internally, which changes the picture file name and makes bookkeeping painful.
- **File name = id.** One letter off and you get "a name without a picture".
- **Same id overrides a built-in pig.** Custom pigs win, and the override is by id, not by display name.
- **Single player and multiplayer differ** (listed as "where to put what"):
  - Single player: both the json and the pictures must be in `config/roll_pig/`.
  - Server: the **server** must have `pigsty/*.json` (the roll pool and the text live there); the **client** must have `image/*.png` and the same `pigsty/*.json` (the GUI and stickers need local pictures).
  - Chat card portraits: both sides need `pig_glyphs.json`, and the client must enable the resource pack.
- **Do not mix "hand-drawn atlas" with "run the script".** The script rewrites the whole lookup table, overwriting your hand-written entries.
- **Deleting a pig does not clear saves.** Progress is stored by id; remove a pig and the record stays, add it back and it is recognized again.
- **Large collections are cheap on VRAM.** The GUI only loads the few pages you are looking at; the font atlas, however, is a single image — beyond 169 pigs one more column is added, growing it from 832×832 to 896×832, which costs just over a hundred KB.

---

## FAQ

**Q: I put the json and the picture in place, but the pig is not in game?**
A: Check in order: ① is it in `config/roll_pig/pigsty/` (not `image/`, not the mod source tree); ② is the extension really `.json` (Windows hides extensions, so it easily becomes `my-pig.json.txt`); ③ did you restart; ④ look at the log line "猪圈里共 N 只猪猪" — if the number did not change it was not read, so search for `读取自定义猪猪资料失败` to see the actual error.

**Q: The name and text are there, but the picture is a question-mark block?**
A: The picture is not a real PNG. Most often it is a gif / webp saved from a web page and renamed to `.png`. Open it in the system image viewer or Photoshop and "Save As PNG" — any converter works. The log will contain `无法加载猪猪图片 xxx：只支持真正的 PNG 文件` with the full path.

**Q: The picture shows as a grey block instead of a question mark?**
A: The grey block is the generic head used when "the atlas has no such pig" — that is the **chat card** fallback, not a picture problem. Follow section 6 to build a resource pack.

**Q: The picture is squashed / distorted?**
A: It is now scaled proportionally and centered, so it cannot distort. If you see distortion you are on an old jar — update. Non-square images are letterboxed inside the box; crop to a square if you want it to fill.

**Q: I changed the text in the json but nothing changed in game?**
A: You did not restart. Data is read once at startup; restart the server / client after editing.

**Q: Chinese shows up as garbled text?**
A: The file is not UTF-8. Re-save it as UTF-8 in VS Code / Notepad++ (no BOM is safer).

**Q: The chat card's top-left is still the generic grey head?**
A: The lookup table has no entry for that pig, or the resource pack is not enabled. Two cases: pack without table → grey head; table without pack → blank (the font has no such glyph).

**Q: The portrait is wrong — it shows another pig's head?**
A: The client's and the server's `pig_glyphs.json` are not the same file. Both must be identical; after re-running the script replace both.

**Q: Sending `[pig name]` in chat does not turn into a picture?**
A: Three causes: ① the name is misspelled (it must equal `name` exactly); ② you have not collected that pig (stickers only work for collected pigs — use `/rollpig unlock`); ③ the client lacks the matching `image/<id>.png`.

**Q: The sticker picker does not list my new pig?**
A: The picker only lists **collected** pigs. Unlock with `/rollpig unlock`, or roll it.

**Q: I added 50 pigs — do I have to make resource packs for all 50?**
A: Yes, but the only thing needed is the "card portrait", and one script run handles all of them: point `--extra` at the whole `config/roll_pig` directory, no one-by-one work.

**Q: The resource pack does not take effect?**
A: Check three things: ① the hierarchy must be `roll_pig_custom/assets/roll_pig/textures/font/pig_icons.png` (one level too many, or a missing `assets`, breaks it); ② `pack_format` in `pack.mcmeta` is `34` (1.21.1); ③ it is **actually enabled** in "Options → Resource Packs" (moved to the right-hand list), and press `F3 + T` afterwards.

**Q: Are the chances uniform? Why do I keep getting the same pig?**
A: Uniform across the whole pool, duplicates allowed. Pigs you already own do not get a lower weight; the `Collected ×N` in the record is just a tally.

**Q: What happens to player records if I delete a custom pig?**
A: The record stays in the save keyed by id, it just no longer appears in the pool or the GUI; add a json with the same id back and the count is still there.

**Q: Can I add pigs without restarting?**
A: No. The roll pool is built at startup (server side) and pictures are loaded on world join (client side). Resource packs can be hot-reloaded; everything else needs a restart.

---

## For mod developers: API

`com.chinaex123.roll_pig.api.RollPigApi` provides a static facade (perform writes on the **server main thread**, passing a `ServerPlayer`):

```java
// queries
List<Pig> all = RollPigApi.pigs();
Optional<Pig> pig = RollPigApi.pig("pig");
int count = RollPigApi.countOf(player, "pig");
int total = RollPigApi.total(player);
boolean can = RollPigApi.canRoll(player);
long ticks = RollPigApi.ticksUntilReset(level);

// actions
RollResult result = RollPigApi.roll(serverPlayer);        // normal roll (consumes today's chance)
RollResult forced = RollPigApi.forceRoll(serverPlayer);   // forced roll (consumes nothing)
RollPigApi.rollAndNotify(serverPlayer);                  // roll + send the card
RollPigApi.openScreen(serverPlayer);                     // open the pigpen GUI
RollPigApi.setCount(player, "pig", 10);
RollPigApi.clearCounts(player);
RollPigApi.resetDaily(player);

// unlocking and syncing
int newly = RollPigApi.unlockAll(player);                 // bump every pig to at least 1; returns how many were new
int got = RollPigApi.collected(player);                  // number of collected pigs
RollPigApi.sync(serverPlayer);                           // push progress to the client (without opening the GUI)
```

An event is fired before and after each roll (on `NeoForge.EVENT_BUS`):

```java
@SubscribeEvent
public static void onPreRoll(RollPigEvent.Pre event) {
    if (event.getPlayer().getGameMode() == GameType.SPECTATOR) {
        event.setCanceled(true);   // veto: consumes no chance and sends no result
    }
}

@SubscribeEvent
public static void onPostRoll(RollPigEvent.Post event) {
    Pig pig = event.getPig();
    RollPigState state = event.getState();
}
```

`RollResult` is `record(boolean success, Pig pig, Reason reason)`, and `Reason` is one of `SUCCESS`, `ALREADY_ROLLED`, `NO_PIGS`, `CANCELLED`.

---

## Project layout

```
src/main/java/com/chinaex123/roll_pig/
├─ api/          RollPigApi, RollPigEvent, RollResult — public API
├─ client/
│  ├─ chat/      PigChatTokens, PigChatRenderer, ChatPigPanel, RPChatEvents — chat stickers
│  ├─ gui/       PigPenScreen (pigpen GUI), PigTextures (texture cache)
│  ├─ ClientPigData            client-side progress cache
│  └─ RPClientPayloadHandler   handles screen/sync packets from the server
├─ command/      RPCommands — /rollpig, /今日猪猪
├─ config/       RPConfig — common config
├─ data/         ModDataGenerator, ModItemModelsProvider — data generation
├─ data/state/   PigRegistry (reads pig data), RollPigState (progress), RPGlyphs (font icons)
├─ event/        RollPigService (roll logic), RPChatQueue (delayed card),
│                RPDayWatcher (dawn notification), RPPlayerSync (sync on join)
├─ init/         RPAttachments, RPCreativeTabs, RPItems
├─ item/         PigPenItem — the "Pigpen" item
├─ mixin/        ChatComponentMixin, ChatScreenMixin — inline chat images
├─ network/      RPNetwork, PigPenPayload, RollRequestPayload
└─ util/         Pig (data model), RPChatCard (card layout)

src/main/resources/
├─ data/roll_pig/pigsty/                    165 pigs' data
└─ assets/roll_pig/
   ├─ textures/pigsty/image/                165 portraits
   ├─ textures/font/pig_icons.png           chat icon atlas
   ├─ font/pig_icons.json                   font definition (bitmap provider)
   ├─ font/card_indent.json                 blank glyph used for card indentation
   ├─ pig_glyphs.json                       id → codepoint lookup table
   └─ lang/                                 zh_cn / en_us

tools/
├─ gen_pig_font.py     redraw the built-in atlas (when the in-jar pig list changes)
├─ make_pig_pack.py    build a custom pig resource pack + lookup table for modpacks
└─ check_pig_font.py   validate a hand-edited atlas / font / lookup table
```

---

## License

All Rights Reserved.
