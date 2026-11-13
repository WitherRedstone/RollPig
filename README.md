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

Paging in the pigpen GUI **wraps around**: scrolling up while on the first page (or clicking `<` in the footer) jumps straight to the last page — the very last pig; scrolling down on the last page returns to the first. On the keyboard, `End` jumps to the last page and selects the last pig, `Home` goes back to the first page, and `PageUp` / `PageDown` also cycle with wrap-around.

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

The mod ships with 165 pigs. Adding your own, or rewriting a built-in pig's name and text, only means dropping files into two folders. Both folders are **created automatically** on startup, so you never have to make them by hand:

```
config/roll_pig/
├─ README.txt      a note written by the mod (it comes back if you delete it)
├─ pigsty/         one json per pig: name, short description, overview
└─ image/          one png per pig, named after the json's id
```

One pig = one json + one png, tied together by the same `id`.

### 1. Add a pig of your own

1. Start the game once (client or server). When the log shows `已创建自定义猪猪目录`, `pigsty/` and `image/` exist.
2. Create `my-pig.json` inside `pigsty/`:

```json
{
  "id": "my-pig",
  "name": "My Pig",
  "description": "Looks pretty ordinary",
  "analysis": "You grew up in the pen. Your greatest joys are sunbathing and rooting around in the dirt."
}
```

An array holding a single object (`[{ ... }]`) works too.

1. Name your picture `my-pig.png` and put it in `image/`. The file name must match `id`.
2. Restart the game / server. Pig data is read once, at startup.
3. Check the log for `猪圈里共 166 只猪猪（其中自定义 1 只）`.
4. In game, run `/rollpig` or right-click the pigpen item and flip to the last page.

### 2. Fields

- `id` — unique key and image file name. Stick to lowercase letters, digits, `-` and `_`; spaces and other characters are replaced with `_`.
- `name` — display name, and the keyword used by chat stickers: `[name]`.
- `description` — one short line, shown under the name.
- `analysis` — the overview. It can be long; the detail page scrolls when it does not fit.

### 3. Picture requirements

- **It must be a real PNG.** Renaming a jpg or gif to `.png` shows a question-mark block in game, and the log says `只支持真正的 PNG 文件`.
- Any size works; square is best (240×240 or larger recommended). Non-square images are scaled proportionally and centred, never stretched.
- A transparent background looks best on the dark UI.
- File name = `<id>.png`.

### 4. Replace or rework a built-in pig

Built-in ids are visible in `config/roll_pig/pigsty/` or inside the jar (for example `pig`, `wild-boar`, `muscle-pig`). Write a json with the same id and drop it into `config/roll_pig/pigsty/` to override that pig. Text-only changes need no picture at all.

### 5. Where the chat picture comes from

`[Pig name]` (for example `[Muscle Pig]`) is replaced by a picture in two places:

- **Inline sticker**: 12 pixels, sitting inside the text.
- **Roll card**: the 36-pixel portrait in the top-left corner of `【xxx's Today's Pig】`.

Both draw `config/roll_pig/image/<id>.png` **directly** — the client loads that image and scales it. So:

- **No resource pack, no atlas, nothing packed into the jar.**
- The server sends the text, the client paints the picture — so **the client needs the png too** (same `config/roll_pig/image/`).
- After swapping an image, rejoin the world; no client restart needed.

### 6. Count and odds

- Rolls are **uniform**: every pig has a `1 / total` chance, custom ones included.
- There is no weight config. To change rolling logic from another mod, use `RollPigEvent.Pre` / `Post` (next chapter).
- Up to 4096 pigs are supported.

### 7. What codepoints are (skippable)

Chat can only draw *glyphs from a font*, not arbitrary pictures. So at startup the mod hands every pig a Unicode **Private Use Area** character: `U+E000 + index` for the 12px sticker, `U+F000 + index` for the 36px card portrait. The PUA (U+E000–U+F8FF) is permanently reserved for private use, so it never collides with real text or other mods.

The mapping is automatic and **never written to a save** (saves only store ids and counts), so adding, removing or reordering pigs never breaks older worlds.

## Notes

- **Restart after any change.** Pig data is read once, at startup; `/reload` and datapack overrides do nothing (data lives in the config folder, not in a datapack).
- `pigsty/` takes json, `image/` takes png. Do not mix them up, and keep file names equal to `id`.
- The picture must be a real PNG. Renaming does not work; the log will tell you.
- Use lowercase letters, digits, `-` and `_` in `id`.
- The same id overrides a built-in pig; use an unused id to add one.
- Single-player runs client and server in one process, so one copy is enough. On a server: **the server needs the json** (it owns the roll pool) and **every client needs the png** (the client paints the chat picture). Keep the json identical on both sides, otherwise the GUI and the actual roll can disagree.
- Images are loaded **on demand**: only the few on the current page, so a huge pig list costs no extra VRAM.
- Deleting a pig's json keeps its collection count in the save (it just stops showing), and nothing breaks.

## FAQ

**Q: I put the files in `config/roll_pig/` but the pig is not in game.**
A: Check three things: ① file name equals `id`; ② you restarted (data is read only at startup); ③ the log line `猪圈里共 N 只猪猪（其中自定义 M 只）` — did N grow?

**Q: The detail page shows a question mark.**
A: The picture is not a real PNG (a renamed gif/jpg, for example). The log will contain `无法加载猪猪图片 …：只支持真正的 PNG 文件`.

**Q: The log says the picture was not found.**
A: It is not in `config/roll_pig/image/`, or its name does not match `id`. The log prints the exact path it expected.

**Q: Will my picture be squashed?**
A: No. Non-square images are scaled proportionally and centred with empty space on the sides.

**Q: I changed the text but the game still shows the old one.**
A: You did not restart. Pig data is read once, at startup.

**Q: Chinese text in the json becomes garbled.**
A: Save the file as **UTF-8** (in Notepad pick "UTF-8", not "ANSI"). A BOM is tolerated.

**Q: `[Pig name]` in chat stays plain text.**
A: ① the text must match `name` exactly (spaces and punctuation included); ② the client needs that pig's png; ③ replacement happens only on the client's display layer — the server and players without the mod see plain `[Pig name]`.

**Q: The card portrait is a grey block.**
A: The client has no png for that pig, or loading failed. Check `config/roll_pig/image/<id>.png`.

**Q: The card portrait looks offset or overlaps the text.**
A: Portraits are drawn 36 px tall, bottom-aligned. If the image has a large transparent margin it will look off; fill the frame more, or keep the padding symmetric.

**Q: Will 50 extra pigs lag the game?**
A: No. The GUI only loads the current page, and chat images load on first use.

**Q: Can it hot-reload?**
A: No. Adding or editing pigs needs a restart (after restarting the client, rejoin the world to see new pictures). `/reload` only affects datapacks, and pig data is not a datapack.

**Q: Other players on my server cannot see my pig.**
A: The roll pool lives on the server, so the server needs the json; chat pictures are painted by each client, so every client needs the png.

**Q: Can I change the roll odds?**
A: Not through config; it is uniform by design. From another mod, use `RollPigApi` and `RollPigEvent`.

**Q: If I delete a pig, do I lose its count?**
A: No. Counts are stored per id in player data; deleting just hides it. Put the json back and it returns.

**Q: Can a pig appear only under certain conditions?**
A: Not built in. In your own mod, listen to `RollPigEvent.Pre` to cancel a roll, or award pigs manually through the API.

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
│  ├─ chat/      PigChatTokens, ChatPigPanel, RPChatEvents — chat stickers and pig picker
│  ├─ font/      PigGlyphInfo — pig glyph (advance and baseline)
│  ├─ gui/       PigPenScreen (pigpen GUI), PigTextures (texture cache)
│  ├─ ClientPigData            client-side progress cache
│  └─ RPClientPayloadHandler   handles screen/sync packets from the server
├─ command/      RPCommands — /rollpig, /今日猪猪
├─ config/       RPConfig — common config
├─ data/         ModDataGenerator, ModItemModelsProvider — data generation
├─ data/state/   PigRegistry (reads pig data), RollPigState (progress)
├─ event/        RollPigService (roll logic), RPChatQueue (delayed card),
│                RPDayWatcher (dawn notification), RPPlayerSync (sync on join)
├─ init/         RPAttachments, RPCreativeTabs, RPItems
├─ item/         PigPenItem — the "Pigpen" item
├─ mixin/        ChatScreenMixin (input space), FontSetMixin (glyph lookup),
│                PigFontRenderMixin (glyph rendering)
├─ network/      RPNetwork, PigPenPayload, RollRequestPayload
└─ util/         Pig (data model), PigGlyphs (id → glyph map), RPChatCard (card layout)

src/main/resources/
├─ data/roll_pig/pigsty/                    165 pigs' data
└─ assets/roll_pig/
   ├─ textures/pigsty/image/                165 portraits (GUI and chat)
   ├─ textures/item/pigpen.png              "Pigpen" item texture
   └─ lang/                                 zh_cn / en_us

tools/
└─ (none — customising only means dropping json + png into config/roll_pig)
```

---

## Related links

Source of inspiration[RollPig Plus](https://github.com/Felis2026/nonebot-plugin-rollpig-plus)

---

## License

All Rights Reserved.
