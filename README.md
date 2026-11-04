# Roll Pig · 今日猪猪

## 介绍

每天抽一只猪，收进猪圈，还能当聊天贴纸发出去。

---

## 玩法

### 抽猪

每天每名玩家可以抽 **一次** 猪猪，游戏内天亮（`dayTime % 24000 == 0`）后自动刷新机会，**次数不叠加**——今天没抽，攒不到明天。

- 全库每只猪 **等概率**，**可重复抽到**，重复抽到的会累加收集次数（`已收集 ×N`）。
- 抽中时会播放**不死图腾**动画，动画结束后才弹出聊天卡片。
- 收集记录按玩家存档（`neoforge:attachments` 里的 `roll_pig:roll_state`），死亡不掉落，换存档互不影响。

抽取入口有三个：

- **右键「猪圈」物品** —— 打开完整 GUI，可以翻页浏览全部猪猪和收集情况
- **`/rollpig` 或 `/今日猪猪`** —— 直接抽一次，播放图腾动画 + 结果页
- **`/rollpig force [目标]`** —— OP 用，强制抽一次，**不消耗**当天次数

### 聊天贴纸

打开聊天输入框，右下角有个小猪圈图标，点开是已收集猪猪的选图面板。点一只猪会在输入框插入 `[猪猪名]`，发送后聊天栏里会自动渲染成那只猪的小头像。

> 只有**已收集**的猪猪才能作为贴纸发出。

---

## 指令

- **`/rollpig`**、**`/今日猪猪`** —— 所有人可用，抽一次今日猪猪
- **`/rollpig force`** —— 需要 OP，给自己强制抽一次，不消耗次数
- **`/rollpig force <目标>`** —— 需要 OP，给指定玩家强制抽一次

---

## 自定义猪猪

下面全部是「不改模组源码、不重新打包」的做法，只要往游戏目录或服务器目录里放文件。

### 1. 加一只自己的猪

在游戏（或服务器）根目录建两个文件夹：

```
config/roll_pig/
├─ pigsty/     ← 猪猪资料，一只猪一个 .json
│   └─ my-pig.json
└─ image/      ← 猪猪图片，文件名必须等于 id
    └─ my-pig.png
```

`config/roll_pig/pigsty/my-pig.json`：

```json
{
  "id": "my-pig",
  "name": "我的猪猪",
  "description": "它的小简介",
  "analysis": "关于这只猪的一段长篇概述，可以写很长，GUI 里会自动折行、可以滚动查看。"
}
```

`config/roll_pig/image/my-pig.png`：一张 PNG，建议 **240×240 正方形**。

重启游戏 / 服务器，控制台会打印：

```
[co.ch.ro.RollPig/]: 猪圈里共 166 只猪猪（其中自定义 1 只）
```

看到「其中自定义 1 只」就成功了——这只猪立刻参与抽奖、出现在 GUI、可以当贴纸发送。

### 2. 字段说明

- **`id`** —— 唯一标识，也是**图片文件名**。不填时用 json 的文件名。建议只用小写字母、数字、`-`、`_`。
- **`name`** —— 显示名，列表、详情、指令结果、聊天里都用它。必填。
- **`description`** —— 短描述，详情页名字下面那行，也是聊天卡片里的「描述：「…」」。
- **`analysis`** —— 长概述，详情页下方（可滚动），也是聊天卡片【概述】的正文。

另外两条规则：

- 一个文件可以只写**一个对象**，也可以写**对象数组**，一次定义好几只猪。
- 文件用 **UTF-8** 保存；带不带 BOM 都能读，但建议用无 BOM。

### 3. 图片要求

- PNG，建议带透明背景（卡片和列表里都好看）。
- 尺寸不限，会等比缩放到方框；建议 **240×240 正方形**，非正方形会被拉伸，可能变形。
- **文件名必须完全等于 id**：`id` 是 `my-pig`，图片就叫 `my-pig.png`。
- json 和图片缺一不可，缺图片时 GUI 里是灰块 + `?`。

### 4. 替换内置猪

只想改文案或换图，不用新增 id，**用同名 id 覆盖**即可：

```json
// config/roll_pig/pigsty/pig.json
{ "id": "pig", "name": "猪", "description": "普通小猪", "analysis": "……" }
```

再把 `config/roll_pig/image/pig.png` 放进去，就替换掉了内置那只「猪」。收集记录按 id 走，玩家原来的次数保留。

### 5. 加了很多只猪（整合包）

这是整合包作者最常遇到的：一次塞几十上百只猪。分两种情况。

##### 情况 A：不介意聊天卡片头像

**什么都不用做。** 自定义猪能正常抽、正常显示、正常当贴纸，只是聊天卡片左上角那个 36 像素的小头像会用**通用灰色猪头**（图集里预留的占位格）。

##### 情况 B：自己在图片编辑器里画

可以。图集就是一张**按网格排的 PNG**，没有任何魔法，自己画完全合法。规则只有三条：

1. 图集是 **64×64 的格子**，横竖都必须能被 64 整除；格子按「从左到右、从上到下」编号。
2. `pig_icons.json` 里的 `chars` 是一个字符串数组，**第几行就是第几排格子，一行里第几个字符就是第几格**；每行长度必须和列数一样。
3. `pig_glyphs.json` 把「猪的 id」映射到「某一格里那个字符」，覆盖读取时生效。

**好消息：内置图集已经留了 3 个空格子，可以白嫖。**

内置图集是 13×13 = 169 格，其中 0–165 是 165 只内置猪 + 1 个通用头，**166、167、168 三格是空的**，而且对照表里已经给它们分配好字符了：

```
第 166 格 → U+E1A6 (57766)
第 167 格 → U+E1A7 (57767)
第 168 格 → U+E1A8 (57768)
```

所以想手画 1–3 只猪，**连 `pig_icons.json` 都不用改**：

1. 从 jar 里把 `assets/roll_pig/textures/font/pig_icons.png` 拷出来（jar 就是 zip，解压工具都能打开）。
2. 在第 166 格的左上角坐标 `(166 % 13) * 64 = (10 * 64, 12 * 64)` 即像素 `(640, 768)` 处，贴上你的 64×64 图。别缩放、别移动网格线。
3. 建一个资源包，只放这一张图：`roll_pig_custom/assets/roll_pig/textures/font/pig_icons.png`（字体定义不用放，直接用 jar 里的）。`pack.mcmeta` 里 `pack_format` 填 `34`（1.21.1）。
4. 在 `config/roll_pig/pig_glyphs.json` 里加一条：

   ```json
   { "my-pig": 57766 }
   ```

5. 猪本身的资料照常放 `config/roll_pig/pigsty/my-pig.json`、图片放 `config/roll_pig/image/my-pig.png`。

**超过 3 只就得改 `pig_icons.json` 了**：加一排格子（图集高度 +64）、在 `chars` 里加一行等长的字符、给新格子分配没用过的码位（继续往下用 `U+E1A9`、`U+E1AA`…… 都在私用区内就行），再在对照表里指过去。这里最容易翻车的是 `chars` 行宽不一致或字符重复，所以画完建议跑一遍检查：

```bash
python tools/check_pig_font.py --pack resourcepacks/roll_pig_custom --config config/roll_pig
```

它会告诉你：图集一共几格、每一行宽度对不对、哪些格子还是空的、对照表里哪条 id 指向了空格子或图集外、某个 id 在 `pigsty/` 里有没有对应 json。全部通过时最后打印「全部检查通过。」

想图省事就还是用情况 B 的脚本——它做的正是上面这些事，而且每次全量重算，不容易错。

### 7. 常见问题

**加了猪但不出现？** 检查是不是放进了 `config/roll_pig/pigsty/`（而不是 `src/...`）、扩展名是不是 `.json`、有没有和内置猪重名被覆盖。日志搜「猪圈里共」看数量，搜「读取自定义猪猪资料失败」看错误。

**改了文案没反应？** 猪猪资料只在**启动时读一次**，必须重启游戏 / 服务器。

**中文乱码？** 用 UTF-8 保存，别用 ANSI/GBK。

**有名字但没图、或详情的图是灰块？** 图片没放到 `config/roll_pig/image/`，或者文件名和 id 对不上。

**聊天卡片头像还是灰的？** 走情况 B：跑 `tools/make_pig_pack.py` 生成资源包 + 对照表，两样都放好。

**聊天里 `[猪猪名]` 没变成图片？** 名字打错，或你还没收集过这只猪——贴纸只对**已收集**的猪生效。

**抽不了，提示没次数？** 今天已经抽过了，等天亮；或 OP 用 `/rollpig force`。

---

## 给模组开发者：API

`com.chinaex123.roll_pig.api.RollPigApi` 提供了静态门面（写操作请在**服务端主线程**传 `ServerPlayer`）：

```java
// 查询
List<Pig> all = RollPigApi.pigs();
Optional<Pig> pig = RollPigApi.pig("pig");
int count = RollPigApi.countOf(player, "pig");
int total = RollPigApi.total(player);
boolean can = RollPigApi.canRoll(player);
long ticks = RollPigApi.ticksUntilReset(level);

// 操作
RollResult result = RollPigApi.roll(serverPlayer);        // 正常抽一次（消耗当天次数）
RollResult forced = RollPigApi.forceRoll(serverPlayer);   // 强制抽一次（不消耗次数）
RollPigApi.rollAndNotify(serverPlayer);                  // 抽 + 发卡片
RollPigApi.openScreen(serverPlayer);                     // 打开猪圈 GUI
RollPigApi.setCount(player, "pig", 10);
RollPigApi.clearCounts(player);
RollPigApi.resetDaily(player);
```

抽奖前后会各发一个事件（挂在 `NeoForge.EVENT_BUS`）：

```java
@SubscribeEvent
public static void onPreRoll(RollPigEvent.Pre event) {
    if (event.getPlayer().getGameMode() == GameType.SPECTATOR) {
        event.setCanceled(true);   // 拦截：不消耗次数，也不发结果
    }
}

@SubscribeEvent
public static void onPostRoll(RollPigEvent.Post event) {
    Pig pig = event.getPig();
    RollPigState state = event.getState();
}
```

`RollResult` 是 `record(boolean success, Pig pig, Reason reason)`，`Reason` 取值 `SUCCESS`、`ALREADY_ROLLED`、`NO_PIGS`、`CANCELLED`。

---

## 项目结构

```
src/main/java/com/chinaex123/roll_pig/
├─ api/          RollPigApi、RollPigEvent、RollResult —— 对外接口
├─ client/
│  ├─ chat/      PigChatTokens、PigChatRenderer、ChatPigPanel、RPChatEvents —— 聊天贴纸
│  ├─ gui/       PigPenScreen（猪圈界面）、PigTextures（纹理缓存）
│  ├─ ClientPigData            客户端收集情况缓存
│  └─ RPClientPayloadHandler   处理服务端下发的界面/同步包
├─ command/      RPCommands —— /rollpig、/今日猪猪
├─ config/       RPConfig —— 公共配置
├─ data/         ModDataGenerator、ModItemModelsProvider —— 数据生成
├─ data/state/   PigRegistry（读猪猪资料）、RollPigState（收集进度）、RPGlyphs（字体图标）
├─ event/        RollPigService（抽奖核心）、RPChatQueue（延迟发卡片）、
│                RPDayWatcher（天亮推送）、RPPlayerSync（进服同步）
├─ init/         RPAttachments、RPCreativeTabs、RPItems
├─ item/         PigPenItem —— 「猪圈」物品
├─ mixin/        ChatComponentMixin、ChatScreenMixin —— 聊天内联图片
├─ network/      RPNetwork、PigPenPayload、RollRequestPayload
└─ util/         Pig（数据模型）、RPChatCard（卡片排版）

src/main/resources/
├─ data/roll_pig/pigsty/                    165 只猪的资料
└─ assets/roll_pig/
   ├─ textures/pigsty/image/                165 张立绘
   ├─ textures/font/pig_icons.png           聊天图标图集
   ├─ font/pig_icons.json                   字体定义（bitmap provider）
   ├─ font/card_indent.json                 卡片缩进用的空白字符
   ├─ pig_glyphs.json                       id → 码位 对照表
   └─ lang/                                 zh_cn / en_us

tools/
├─ gen_pig_font.py     重画内置图集（改了 jar 内猪列表时用）
├─ make_pig_pack.py    给整合包生成自定义猪资源包 + 对照表
└─ check_pig_font.py   校验手工改过的图集/字体/对照表
```

---

## 构建

```bash
# Windows
gradlew.bat build

# macOS / Linux
./gradlew build
```

产物在 `build/libs/roll_pig-<版本>.jar`。

开发环境热运行：

```bash
gradlew.bat runClient    # 客户端
gradlew.bat runServer    # 服务端
```

---

## 许可

All Rights Reserved.
