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

往游戏目录里放文件就行。
真正需要你操心的只有三样东西：

- `config/roll_pig/pigsty/<id>.json` —— 这只猪的文案
- `config/roll_pig/image/<id>.png` —— 这只猪的图
- （可选）一个资源包 + `config/roll_pig/pig_glyphs.json` —— 只影响**聊天卡片左上角那个 36 像素的小头像**

**先把「三处图」分清楚**，大部分人卡在这一步：

- **猪圈 GUI 里的大图和列表小头像** —— 直接读 `config/roll_pig/image/<id>.png`，所以**客户端**必须放这张图。
- **聊天栏里的贴纸**（发 `[猪猪名]` 变成小图） —— 也是直接读那张原图，按 12 像素画出来，同样要**客户端**有图。
- **聊天卡片左上角的 36px 头像**（`【xxx 的今日猪猪】` 那张卡） —— 这个**不是图片，是字体里的一个字符**，来自图集 `pig_icons.png`。想让它显示新猪，必须用资源包换图集 + 用对照表告诉模组「哪个 id 对应哪一格」。

结论：

- 只想让猪能抽、能看、能当贴纸 → 做完第 1 节就结束了。
- 还想让聊天卡片左上角也是这只新猪 → 再看第 5、6 节（或者第 7 节手画）。

### 1. 加一只自己的猪

**第 1 步：建目录。** **不用自己建**——第一次启动游戏（或服务器）时，模组会自动在 `config/roll_pig/` 下建好 `pigsty/` 和 `image/`，还会放一份 `README.txt` 说明这两个目录是干什么的。你只要往里丢文件：

```
config/roll_pig/
├─ README.txt              ← 自动生成，删掉下次启动还会再写一份
├─ pigsty/                 ← 一只猪一个 .json
│   └─ my-pig.json
└─ image/                  ← 图片，文件名 = id
    └─ my-pig.png
```

注意是**两个平级目录**：文案进 `pigsty/`，图片进 `image/`。图片不要塞进 `pigsty/`。

**第 2 步：写文案。** 新建 `config/roll_pig/pigsty/my-pig.json`，用 **UTF-8** 保存：

```json
{
  "id": "my-pig",
  "name": "我的猪猪",
  "description": "它的小简介",
  "analysis": "关于这只猪的长篇概述。可以写很长，GUI 里会自动折行，装不下的部分可以用滚轮往下翻。"
}
```

**第 3 步：放图。** 图片放到 `config/roll_pig/image/my-pig.png`，文件名必须和 `id` 完全一致。必须是**真正的 PNG** 文件（判断方法见第 3 节），建议 240×240 或更大、正方形、带透明背景。

**第 4 步：重启。** 猪猪资料只在启动时读一次，图片也是进世界时加载，所以改完要重启客户端 / 服务器（单人游戏退出到标题再进也行）。

**第 5 步：看日志。** 启动时控制台会打印：

```
[co.ch.ro.RollPig/]: 猪圈里共 166 只猪猪（其中自定义 1 只）
```

看到「其中自定义 1 只」就说明读到了。如果只显示 165 只、且没有后面的括号，说明 json 没被找到——回去检查路径、扩展名（是不是 `my-pig.json.txt`）、编码。

**第 6 步：进游戏确认三件事。**

- 右键「猪圈」物品，翻到最后一页，列表里应该出现「我的猪猪」，点开详情有图、有描述、有概述。
- `/rollpig force`（OP）或直接等抽到它，确认播放图腾动画、弹出聊天卡片。
- 抽到之后打开聊天输入框，点右下角的小猪图标，面板里应该能点到它，发送后在聊天栏显示成小图。

三件事都正常，这只猪就彻底接进来了。

### 2. 字段说明

- **`id`** —— 唯一标识，同时是**图片文件名**。不填时用 json 的文件名。
- **`name`** —— 显示名。列表、详情标题、指令结果、聊天卡片都用它。必填。
- **`description`** —— 短描述。详情页名字下面那行，也是聊天卡片里的「描述：「…」」。
- **`analysis`** —— 长概述。详情页下方可滚动区域，也是聊天卡片【概述】的正文。

格式上的两条规则：

- 一个文件可以只写**一个对象**，也可以写**对象数组**，一次定义好几只猪。
- 文件用 UTF-8 保存。带 BOM 也能读（模组会清掉），但建议无 BOM。

**`id` 一定要用小写英文、数字、连字符 `-` 和下划线 `_`。** 原因：模组会先把 id 转小写、再把「不是 `a-z`、`0-9`、`-`、`_`、`.`、`/` 的字符」统统换成 `_`，然后用这个结果去找图片。也就是说 `id` 写 `我的猪` 会变成 `__`，图片就得叫 `__.png`——能跑但对不上账，所以别用中文 id（中文写进 `name` 里就行）。

### 3. 图片要求

- **必须是真正的 PNG**。这是最常见的坑：浏览器「另存为」经常给你 webp / gif / avif，只改扩展名为 `.png` 是没用的，游戏只认 PNG 字节流。
  - 判断方法一：用系统「画图」打开，如果打得开说明格式常见，但更稳妥的是**另存为 PNG** 再放进去。
  - 判断方法二：看文件头。PNG 的前 8 个字节是 `89 50 4E 47 0D 0A 1A 0A`；如果是 `47 49 46 38`（`GIF8`）或 `52 49 46 46`（`RIFF`，webp）就不是 PNG。
  - 判断方法三：直接看日志。加载失败时模组会打印 `无法加载猪猪图片 xxx：只支持真正的 PNG 文件`，并带上它正在找的完整路径。
- 尺寸不限，会**按比例**缩放到方框里居中显示，不会被拉扁。建议 **240×240 或 512×512 的正方形**；特别宽或特别高的图在 48×48 的框里会显得小。
- **建议带透明背景**（RGBA）。不透明的白底图在深色 GUI 里会看到一个白方块。
- 一张图只对应一个 id，文件名必须和 id 一致：`id` 是 `my-pig`，图片就叫 `my-pig.png`。
- 多放几张也没关系——模组只在真正需要显示时才加载，不会一次性把几百张图全塞进显存。

### 4. 替换内置猪

只想改文案或换图、不想新增 id，就直接**用同名 id 覆盖**内置猪：

```json
// config/roll_pig/pigsty/pig.json
{ "id": "pig", "name": "猪", "description": "普通小猪", "analysis": "……" }
```

再把 `config/roll_pig/image/pig.png` 放进去，就替换掉了内置那只「猪」。收集记录是按 id 记的，玩家原来的次数不会丢。

内置猪的 id 就是 jar 里 `data/roll_pig/pigsty/*.json` 的文件名（一共 165 只）。想确认某只的 id，看游戏里猪圈界面的名字，或者解压 jar 看文件名。

### 5. 聊天里的图到底从哪来

这里最容易搞混，单独说清楚。模组在聊天里用到两种完全不同的东西：

**贴纸（直接贴图）** —— 你发 `[猪猪名]`，消息里会插入一个 12×12 的特殊字位，客户端渲染到那个位置时把**原图纹理**画上去。所以：

- 用的是 `config/roll_pig/image/<id>.png` 原图。
- 只要客户端有这张图就能显示，**不需要资源包**。
- 只对**已收集**的猪生效。

**卡片头像（字体字形）** —— `【xxx 的今日猪猪】` 那张卡左上角 36 像素的图。Minecraft 的聊天栏画不了任意图片，只能画**字体里的字形**，所以模组做了一张 `pig_icons.png` 图集（13×13 个 64×64 的格子），每个格子注册成一个私用区字符，卡片里放的就是「那个字符」。

- 它走的是**字体**，不是纹理，所以客户端必须能读到对应的字形。
- 模组自带的图集只画了内置的 165 只猪，新加的猪不在里面 → 默认退回**灰色通用猪头**（图集里预留的占位格）。
- 想让新猪有自己的卡片头像，就得**换一张图集**（资源包）并在**对照表**里登记 id → 格子。

取舍很清楚：**贴纸随便加，卡片头像要额外打包。** 如果一整套整合包的猪都要有自己的卡片头像，用第 6 节的脚本；只加两三只、又想手动画，看第 7 节。

### 6. 让自定义猪有卡片头像（推荐：跑一条命令）

工具在模组的 `tools/make_pig_pack.py`。它会：

1. 从 jar 里取出内置图集和对照表；
2. 把 `config/roll_pig/image/` 里所有「不在内置对照表里」的新猪，逐张缩放成 64×64，追加到图集后面；
3. 重新排布成正方形图集，给新格子分配 `U+E000` 之后的码位（内置格子编号不变）；
4. 输出一个**资源包目录**，同时写好 `config/roll_pig/pig_glyphs.json`。

**第 1 步：确认猪都在。** 先把新猪的 json 和图片放进 `config/roll_pig/`（第 1 节那套），启动一次确认日志里数量对得上。

**第 2 步：跑脚本。** 在模组目录下执行（Windows 命令提示符或 PowerShell 都一样）：

```bash
python tools/make_pig_pack.py --jar mods/roll_pig-1.0.0-mc1.21.1-neoforge.jar --extra config/roll_pig --pack resourcepacks/roll_pig_custom
```

参数含义：

- `--jar` —— 模组 jar 的路径，脚本要从里面读内置图集。
- `--extra` —— 你放自定义猪的目录，里面要有 `pigsty/` 和 `image/`。
- `--pack` —— 资源包输出目录，脚本会新建。
- `--config` —— 可选，对照表输出目录，默认就是 `--extra`。

正常输出类似：

```
资源包: resourcepacks/roll_pig_custom
  图集: (896, 832) 格数 170 (169 内置 + 1 自定义)
对照表: config/roll_pig/pig_glyphs.json 共 167 条
  test-pig                 U+E0A9
```

读法：内置 169 格原样保留，你的猪从第 170 格开始；对照表里多了一条 `test-pig → U+E0A9`，这就是它在图集里的字符。

**第 3 步：装资源包。** 把整个 `roll_pig_custom` 文件夹放进游戏的 `resourcepacks/`，然后在「选项 → 资源包」里启用它（左侧列表里点箭头挪到右侧）。改过图之后按 `F3 + T` 重载资源包即可，不用重启游戏。

**第 4 步：放对照表。** 脚本已经把 `config/roll_pig/pig_glyphs.json` 写好了。**客户端和服务器都要有这同一份**（卡片文字是服务端拼的，字形是客户端画的，两边码位必须一致）。

**第 5 步：重启并验证。**

- 启动日志多一行：`扩展头像对照表: 167 条（新增 1）`。看到它说明对照表被读到了。
- 抽一只自定义猪，看卡片左上角是不是它自己的头像。
- 如果日志有这行、头像还是灰的：资源包没启用，或者资源包里图集路径不对。

**第 6 步：以后增删猪怎么办。** 每次改完 `config/roll_pig/` 就重跑一次脚本，重新启用一次资源包（`F3 + T`），重启服务器。脚本每次都是**全量重算**，不会叠加出错，但这也意味着：**它会把 `pig_glyphs.json` 整个重写**，你手写的内容会被覆盖，别和手画混着用。

### 7. 手画图集（进阶，不跑脚本）

图集就是一张**按网格排的 PNG**，没有魔法，自己画完全合法。规则三条：

1. 图集是 **64×64 的格子**，宽高都必须能被 64 整除；格子按「从左到右、从上到下」编号。
2. `pig_icons.json` 里的 `chars` 是字符串数组：**第几行对应第几排格子，一行里第几个字符对应第几格**；每行长度必须等于列数。
3. `pig_glyphs.json` 把「猪的 id」映射到「某一格里的那个字符」。

**内置图集留了 3 个空格子，可以白嫖。** 图集是 13×13 = 169 格：0–164 是 165 只内置猪，165 是通用灰色头，**166、167、168 三格是空的**，而且字符已经预先分配好：

```
第 166 格 → U+E1A6 (57766)，左上角像素 (640, 768)
第 167 格 → U+E1A7 (57767)，左上角像素 (704, 768)
第 168 格 → U+E1A8 (57768)，左上角像素 (768, 768)
```

所以手画 1–3 只，**连 `pig_icons.json` 都不用改**：

1. 从 jar 里取出 `assets/roll_pig/textures/font/pig_icons.png`（jar 就是 zip，解压工具都能打开）。
2. 在像素 `(640, 768)` 处贴上你的 64×64 图。不缩放、不移动网格线。
3. 建资源包，只放这一张图：`roll_pig_custom/assets/roll_pig/textures/font/pig_icons.png`。字体定义不用放，直接用 jar 里的。`pack.mcmeta` 里 `pack_format` 填 `34`（1.21.1）。
4. 在 `config/roll_pig/pig_glyphs.json` 里加一条 `{ "my-pig": 57766 }`。
5. 猪本身的 json 和图片照常放。

**超过 3 只就得改 `pig_icons.json`**：加一排格子（高度 +64）、在 `chars` 里加一行等长字符、给新格子分配没用过的码位（接着用 `U+E1A9`、`U+E1AA`……，都在私用区内就行），再到对照表里指过去。

改完跑一遍检查，它会把问题指出来：

```bash
python tools/check_pig_font.py --pack resourcepacks/roll_pig_custom --config config/roll_pig
```

它检查：图集尺寸是否 64 的整数倍、`chars` 每行宽度、字符是否重复、码位是否都在私用区、对照表里每条 id 指向的格子存不存在、是不是空的，以及 `pigsty/` 里有没有对应 json。全部通过时打印「全部检查通过」。

### 8. 码位为什么从 57344 开始

`57344` = `U+E000`，是 Unicode **私用区（Private Use Area，U+E000–U+F8FF）** 的起点。这段码位被标准永久保留给私有用途，永远不会分配给真实文字，所以拿它们当「图片字符」不会和任何语言、任何模组撞车。

模组不按顺序硬编码，而是用 `pig_glyphs.json` 查表（id → 码位），这样以后增删猪、图集重排时，旧存档里的图标不会错位。内置那份在 jar 里，你的那份在 `config/roll_pig/pig_glyphs.json`，读取时会**覆盖同名、追加新的**到内置表上。

---

## 注意事项

- **目录不用自己建。** 启动时会自动生成 `config/roll_pig/pigsty`、`config/roll_pig/image`，并写一份 `README.txt`；日志里出现「已创建自定义猪猪目录」就说明建好了。
- **改完一定要重启。** 猪猪资料只在启动时读一次；图片是进世界时加载的，加完图片要重进存档或重启客户端（纹理有缓存）。只有资源包可以用 `F3 + T` 热重载。
- **两个目录别搞混。** `config/roll_pig/pigsty/` 只放 `.json`，`config/roll_pig/image/` 只放 `.png`。
- **图片必须是真 PNG。** gif / jpg / webp 改扩展名不行，日志会报 `只支持真正的 PNG 文件`。
- **`id` 用小写英文。** 中文或大写会在内部被转成下划线，图片文件名会跟着变，很难对账。
- **文件名 = id。** 差一个字母就是「有名字没图」。
- **同名会覆盖内置猪。** 自定义优先，并且是按 id 覆盖，不是按显示名。
- **单机和联机的分工不一样**（列表按「放哪、放什么」看）：
  - 单人游戏：`config/roll_pig/` 里 json + 图片都要有。
  - 服务器：**服务端**必须有 `pigsty/*.json`（抽奖池和文案在这里）；**客户端**必须有 `image/*.png` 和同样的 `pigsty/*.json`（GUI 和贴纸要本地图）。
  - 聊天卡片头像：两端都要有 `pig_glyphs.json`，客户端还要启用资源包。
- **别把「手画图集」和「跑脚本」混用。** 脚本会整份重写对照表，覆盖你手写的条目。
- **删掉一只猪不会清存档。** 玩家收集记录按 id 存在存档里，猪删了记录还在；把猪加回来还认。
- **大批量不用担心显存。** GUI 只加载当前翻到的几页图片；但字体图集是一整张图，猪总数超过 169 只时要加一列，图集从 832×832 变成 896×832，显存只多一百多 KB。

---

## 常见问题 QA

**Q：我把 json 和图片都放好了，游戏里却没有这只猪？**
A：按顺序排查：① 是否放在 `config/roll_pig/pigsty/`（不是 `image/`，也不是模组源码目录）；② 扩展名是不是真的 `.json`（Windows 常隐藏扩展名，容易变成 `my-pig.json.txt`）；③ 有没有重启；④ 看日志「猪圈里共 N 只猪猪」，数字没变就是没读到，搜 `读取自定义猪猪资料失败` 看具体报错。

**Q：名字和文案都有，图却是个问号方块？**
A：图片不是真正的 PNG。最常见的是从网页存下来的 gif / webp，被改成了 `.png`。用系统「画图」或 Photoshop 打开后「另存为 PNG」就行，任何能转格式的工具都可以。日志里会有 `无法加载猪猪图片 xxx：只支持真正的 PNG 文件` 并带上完整路径。

**Q：图片显示成灰色方块而不是问号？**
A：灰色方块是「图集里没有这只猪」的通用头，属于**聊天卡片**的降级表现，不是图片问题。走第 6 节做资源包即可。

**Q：图片被拉扁 / 变形了？**
A：现在是按比例缩放居中，不会变形。如果你看到变形，说明用的是旧版本 jar，换最新的。非正方形图在方框里会留白，想要满框就裁成正方形。

**Q：我改了 json 里的文案，进游戏没变化？**
A：没重启。资料启动时读一次，改完重启服务器 / 客户端。

**Q：中文显示成乱码？**
A：文件不是 UTF-8。用 VS Code / Notepad++ 另存为 UTF-8（无 BOM 更稳）。

**Q：聊天卡片左上角还是那个灰色通用头？**
A：说明对照表里没有这只猪，或者资源包没启用。分两种：只放资源包不放对照表 → 灰色头；只放对照表不放资源包 → 空白（字体里没这个字形）。

**Q：头像错位了，显示成另一只猪的头？**
A：客户端和服务器的 `pig_glyphs.json` 不是同一份。两边必须完全一致，重跑脚本后两处都替换。

**Q：聊天里发 `[猪猪名]` 没有变成图？**
A：三个原因：① 名字打错（要完全等于 `name`）；② 这只猪你还没收集到（贴纸只对已收集的猪生效，可以用 `/rollpig unlock` 解锁）；③ 客户端没有对应的 `image/<id>.png`。

**Q：贴纸面板里找不到新猪？**
A：面板只列出**已收集**的猪。用 `/rollpig unlock` 解锁，或者抽到它。

**Q：加了 50 只猪，难道要给 50 只都做资源包？**
A：是的，但要做的只有「卡片头像」这一件事，跑一次脚本就全解决了，`--extra` 指向整个 `config/roll_pig` 目录即可，不用一只一只来。

**Q：资源包放进去没生效？**
A：检查三点：① 目录层级必须是 `roll_pig_custom/assets/roll_pig/textures/font/pig_icons.png`（多套一层或少了 `assets` 都不行）；② `pack.mcmeta` 里的 `pack_format` 是 `34`（1.21.1）；③ 在「选项 → 资源包」里**真的启用**了（挪到右侧列表），改完按 `F3 + T`。

**Q：概率是平均的吗？怎么老是抽到同一只？**
A：全库等概率、可重复抽到。抽到过的猪不会降低权重，记录里显示的 `已收集 ×N` 只是次数统计。

**Q：删掉一只自定义猪，玩家的记录会怎样？**
A：记录按 id 留在存档里，只是不再出现在抽奖池和界面；把同 id 的 json 加回来，次数还在。

**Q：能不能不重启就加猪？**
A：不行。抽奖池在启动时构建（服务端），图片也在进世界时加载（客户端）。资源包可以热重载，其余都要重启。

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

## 相关链接

灵感来源[RollPig Plus](https://github.com/Felis2026/nonebot-plugin-rollpig-plus)

---

## 许可

All Rights Reserved.
