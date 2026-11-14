**简体中文** | [English](README.md)

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

猪圈界面的翻页是**循环**的：在首页继续往上滚（或点底部的 `<`）会直接跳到最后一页，也就是最后一只猪；在末页继续往下滚会回到首页。键盘上 `End` 一次跳到最后一页并选中最后一只猪，`Home` 回到第一页，`PageUp` / `PageDown` 也是循环翻页。

### 聊天贴纸

打开聊天输入框，右下角有个小猪圈图标，点开是已收集猪猪的选图面板。点一只猪会在输入框插入 `[猪猪名]`，发送后聊天栏里会自动渲染成那只猪的小头像。

> 只有**已收集**的猪猪才能作为贴纸发出。

---

## 指令

- **`/rollpig`**、**`/今日猪猪`** —— 所有人可用，抽一次今日猪猪
- **`/rollpig force`** —— 需要 OP，给自己强制抽一次，不消耗次数
- **`/rollpig force <目标>`** —— 需要 OP，给指定玩家强制抽一次
- **`/rollpig unlock`** —— 需要 OP，解锁猪圈里全部猪猪
- **`/rollpig unlock <目标>`** —— 需要 OP，解锁指定玩家的全部猪猪

`unlock` 只补不删：没抽到的猪补成 1 次，已经抽到更多次的保持原值，所以「总次数」会变成「原本的次数 + 新解锁的只数」。解锁后会立刻把新的收集情况同步给客户端，聊天贴纸面板马上就能用；猪圈界面开着的话也会原地刷新。

---

## 自定义猪猪

模组自带 165 只猪猪。加自己的猪、或者改内置猪的名字和文案，都只需要往两个文件夹里丢文件。这两个文件夹在游戏启动时**自动创建**，不用手动建：

```
config/roll_pig/
├─ README.txt      模组写的说明，删了也会再生成
├─ pigsty/         每只猪一个 json：名字、短描述、概述
└─ image/          每只猪一张 png，文件名要和 json 里的 id 相同
```

一只猪 = 一个 json + 一张 png，两边用同一个 `id` 对应。

### 1. 加一只自己的猪

1. 先启动一次游戏（客户端或服务端都行）。日志里出现 `已创建自定义猪猪目录` 就说明 `pigsty/` 和 `image/` 建好了。
2. 在 `pigsty/` 里新建 `my-pig.json`：

```json
{
  "id": "my-pig",
  "name": "我的猪",
  "description": "看起来很普通",
  "analysis": "你是一只在猪圈里长大的猪，最大的爱好是晒太阳和拱土。"
}
```

也可以写成只含一个对象的数组（`[{ ... }]`），两种格式都认。

1. 把图片命名为 `my-pig.png` 放进 `image/`，名字必须和 `id` 一致。
2. 重启游戏 / 服务器，猪猪资料只在启动时读一次。
3. 看日志里有没有 `猪圈里共 166 只猪猪（其中自定义 1 只）`。
4. 进游戏用 `/rollpig` 或右键打开猪圈，翻到最后一页就能看到它。

### 2. 字段说明

- `id` —— 唯一标识，同时是图片文件名。建议只用小写英文、数字、`-` 和 `_`；空格、中文等字符会被替换成 `_`。
- `name` —— 显示名，也是聊天贴纸的关键词 `[名字]`。
- `description` —— 一行短描述，显示在名字下面。
- `analysis` —— 概述，可以很长，详情页放不下时可以滚动。

### 3. 图片要求

- **必须是真正的 PNG**。把 jpg / gif 改个扩展名会在游戏里显示成问号方块，日志会写明「只支持真正的 PNG 文件」。
- 尺寸随意，正方形最好（推荐 240×240 或更大）。长方形会等比缩放、居中显示，不会被拉扁。
- 建议用透明背景，深色界面下更好看。
- 文件名 = `<id>.png`。

### 4. 替换或改造内置猪

内置猪的 id 可以在 `config/roll_pig/pigsty/` 或模组 jar 里看到（例如 `pig`、`wild-boar`、`muscle-pig`）。用同一个 id 写一份 json 丢进 `config/roll_pig/pigsty/` 就会覆盖内置的那只；只想改文案就只放 json，不用放图。

### 5. 聊天里的图到底从哪来

`[猪名]` 这种写法（例如 `[肌肉猪]`）在聊天里会被替换成图片，共两处：

- **行内贴纸**：12 像素，跟在文字里。
- **抽卡卡片**：`【xx 的今日猪猪】` 卡片左上角 36 像素的头像。

这两处画的都是 `config/roll_pig/image/<id>.png` **这张原图本身**（客户端加载后缩放绘制），所以：

- **不需要资源包、不需要图集、不需要往 jar 里塞任何东西。**
- 服务端负责发文字，客户端负责画图 —— 所以**客户端也要有这张 png**（同一个 `config/roll_pig/image/` 里）。
- 换了图之后重进世界就生效，不用重启客户端。

### 6. 数量与概率

- 抽奖是**等概率**的：每只猪被抽到的概率都是 `1 / 猪总数`，自定义猪一样参与。
- 没有「权重」配置项。要在别的模组里改抽取逻辑，用 `RollPigEvent.Pre` / `Post`，见下一章。
- 猪的 id 分配上限是 4096 只。

### 7. 码位是什么（可以跳过）

聊天栏只能画「字体里的字形」，画不了任意图片。所以模组在启动时给每只猪分配一个 Unicode **私用区**字符：`U+E000 + 序号` 给 12px 贴纸，`U+F000 + 序号` 给 36px 卡片头像。私用区（U+E000–U+F8FF）是标准永久预留的，不会和任何语言、任何模组撞车。

这套分配全自动，而且**不写进存档**（存档只记 id 和次数），所以增删猪、改顺序都不会让老存档错位。

## 注意事项

- **改完一定要重启。** 猪猪资料只在启动时读一次；`/reload` 和数据包覆写都不生效（资料走 config 目录，不走数据包）。
- `pigsty/` 放 json，`image/` 放 png，别放反；两边文件名和 `id` 必须一致。
- 图片必须是真 PNG，改扩展名没用，日志里会提示。
- `id` 只用小写英文、数字、`-`、`_`。
- 同 id 会覆盖内置猪；想「新增」就用没被占用的 id。
- 单人游戏客户端和服务端是同一个进程，放一份就行。多人服务器：**服务端放 json**（决定抽奖池），**客户端放 png**（决定聊天里的图），两边的 json 最好也一致，否则界面里看到的和实际抽到的会不一样。
- `image/` 里的图**按需加载**：只加载当前页面看到的几只，猪再多也不占显存。
- 删掉某只猪的 json 后，存档里它的收集记录还在（只是不再显示），不会报错。

## 常见问题 QA

**Q：文件放进 `config/roll_pig/` 了，游戏里没有这只猪？**
A：查三点：① 文件名和 `id` 是否一致；② 是否重启过（资料只在启动时读）；③ 日志里 `猪圈里共 N 只猪猪（其中自定义 M 只）` 的 N 有没有变大。

**Q：详情里显示的是问号？**
A：图片不是真 PNG（例如把 gif / jpg 改了扩展名）。日志里会有 `无法加载猪猪图片 …：只支持真正的 PNG 文件`。

**Q：日志说找不到图片？**
A：图片没放在 `config/roll_pig/image/`，或文件名和 `id` 不一致。日志里会打印它期望的完整路径。

**Q：图片会被拉扁吗？**
A：不会。非正方形的图会等比缩放并居中，两侧留空。

**Q：改了文案，进游戏还是旧的？**
A：没重启。猪猪资料只在启动时读一次。

**Q：json 里的中文变乱码？**
A：文件要存成 **UTF-8**（Windows 记事本选「UTF-8」，不要「ANSI」）。带 BOM 也能读。

**Q：聊天里的 `[猪名]` 没变成图？**
A：① 名字要和 `name` 完全一致（含空格、标点）；② 客户端要有这只猪的 png；③ 替换只发生在客户端显示层 —— 服务器和没装模组的玩家看到的是纯文本 `[猪名]`。

**Q：卡片头像是一个灰方块？**
A：客户端没有这张 png，或者图加载失败。检查 `config/roll_pig/image/<id>.png`。

**Q：卡片头像偏了、和文字重叠？**
A：头像按 36 像素、底边对齐绘制。图里如果有一大片透明边距，看起来就会偏；把主体画满一些、或让留白对称。

**Q：加 50 只猪会卡吗？**
A：不会。GUI 只加载当前页的图，聊天用图也是用到才加载。

**Q：能热加载吗？**
A：不能。加 / 改猪都要重启（客户端重启后重进世界即可看到新图）。`/reload` 只对数据包有效，而猪猪资料不走数据包。

**Q：多人服里别人看不到我的猪？**
A：抽奖池在服务端，所以服务端要有 json；聊天里的图在客户端画，所以每个客户端都要有 png。

**Q：能改抽取概率吗？**
A：配置里不能改，默认等概率。想在别的模组里自定义，用 `RollPigApi` 和 `RollPigEvent`。

**Q：删了一只猪，存档里的次数会没吗？**
A：不会。次数按 id 存在玩家数据里，删掉只是不再显示；把 json 放回去又能看到。

**Q：能让某只猪只在特定条件下出现吗？**
A：当前不支持。可以在自己的模组里监听 `RollPigEvent.Pre` 取消某次抽取，或用 API 手动发奖。

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

// 解锁与同步
int newly = RollPigApi.unlockAll(player);                 // 全部猪补到至少 1 次，返回新解锁只数
int got = RollPigApi.collected(player);                  // 已收集只数
RollPigApi.sync(serverPlayer);                           // 把收集情况推给客户端（不开界面）
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
│  ├─ chat/      PigChatTokens、ChatPigPanel、RPChatEvents —— 聊天贴纸与选猪面板
│  ├─ font/      PigGlyphInfo —— 猪猪字形（宽高与基线）
│  ├─ gui/       PigPenScreen（猪圈界面）、PigTextures（纹理缓存）
│  ├─ ClientPigData            客户端收集情况缓存
│  └─ RPClientPayloadHandler   处理服务端下发的界面/同步包
├─ command/      RPCommands —— /rollpig、/今日猪猪
├─ config/       RPConfig —— 公共配置
├─ data/         ModDataGenerator、ModItemModelsProvider —— 数据生成
├─ data/state/   PigRegistry（读猪猪资料）、RollPigState（收集进度）
├─ event/        RollPigService（抽奖核心）、RPChatQueue（延迟发卡片）、
│                RPDayWatcher（天亮推送）、RPPlayerSync（进服同步）
├─ init/         RPAttachments、RPCreativeTabs、RPItems
├─ item/         PigPenItem —— 「猪圈」物品
├─ mixin/        ChatScreenMixin（输入框腾位）、FontSetMixin（字形查询）、
│                PigFontRenderMixin（字形绘制）
├─ network/      RPNetwork、PigPenPayload、RollRequestPayload
└─ util/         Pig（数据模型）、PigGlyphs（id → 字形分配）、RPChatCard（卡片排版）

src/main/resources/
├─ data/roll_pig/pigsty/                    165 只猪的资料
└─ assets/roll_pig/
   ├─ textures/pigsty/image/                165 张立绘（GUI 与聊天共用）
   ├─ textures/item/pigpen.png              「猪圈」物品贴图
   └─ lang/                                 zh_cn / en_us

tools/
└─ （无。自定义只需要往 config/roll_pig 里丢 json + png）
```

---

## 相关链接

灵感来源[RollPig Plus](https://github.com/Felis2026/nonebot-plugin-rollpig-plus)

---

## 许可

All Rights Reserved.
