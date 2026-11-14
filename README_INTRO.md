**简体中文** | [English](#english)

# 今日猪猪 / Roll Pig

适用版本：Minecraft 1.21.1 · NeoForge

---

## 简介

每天一次，从一个装着一百多只猪的猪圈里抽一只出来。

不是抽奖机，是每天给你一份「今天你是哪只猪」的说明。

## 玩法

- **一天一次**：天亮之后刷新一次抽取机会。当天不用也不会累积，第二天还是只有一次。
- **抽的方式**：输入 `/rollpig`（也可以直接打 `/今日猪猪`）。先播不死图腾的动画，再弹出结果页，念出今天这只猪的名字、一句描述和一段性格分析。
- **右键「猪圈」物品**随时打开猪圈界面：左边是猪猪列表和每只被抽到的次数，右边是立绘、描述和完整分析，顶部显示总抽取次数。

## 每只猪都是一份性格说明

内置 **165 只猪**。可能是普通的粉红小猪，也可能是 UFO 猪、恶魔猪、天使猪、肌肉猪、诸葛亮猪……全部等概率，同一只也可以重复抽到。每只猪都配了一段「你是……」的自我分析，抽到的时候像在看一份专门为你写的今日运势。

## 收集

抽过的猪会永久记录在玩家存档里，死亡也不会丢。界面上能看到每只抽到过几次、总共抽了多少次。抽完之后，聊天栏还会弹出一张风格化的卡片：名字、描述、收集次数，以及完整的概述。

## 自定义猪猪

想加自己的猪，只要把文案放进 `config/roll_pig/pigsty/<id>.json`，把立绘放进 `config/roll_pig/image/<id>.png`。两个文件夹第一次启动会自动创建，并附带一份说明文件，玩家自己也能照着加。

整合包作者想一次加很多只，可以用附带的 `tools/make_pig_pack.py` 一条命令生成资源包和聊天头像对照表；纯手画也有对应的检查工具。

## 指令

- `/rollpig`、`/今日猪猪` — 抽一次（抽完之后才会显示结果）
- `/rollpig unlock` — 解锁全部猪猪（OP）
- `/rollpig force` — 强制抽取，不消耗当天机会（OP）

## 其它

模组自带 API 和可取消的抽取事件，其它模组可以在抽取时插一脚。两条配置分别控制「天亮提示」和「是否广播抽奖结果」，都默认开启。

---

<a id="english"></a>

**English** | [简体中文](#今日猪猪--roll-pig)

# Roll Pig

Minecraft 1.21.1 · NeoForge

---

## About

Once a day, reach into a pen full of over a hundred pigs and pull one out.

It is not a gacha machine — it is a daily note telling you which pig you are today.

## How it works

- **One roll a day**: a new chance appears at dawn. An unused chance does not stack; tomorrow you still get exactly one.
- **Rolling**: type `/rollpig`. The totem-of-undying animation plays first, then a result screen names today's pig together with a one-line description and a personality read.
- **Right-click the Pigpen item** at any time to open the pen screen: the left column lists every pig with how many times you have rolled it, the right column shows the portrait, the description and the full analysis, and the header shows your total number of rolls.

## Every pig is a personality read

165 pigs are built in. You might get a plain pink piglet, or UFO Pig, Demon Pig, Angel Pig, Muscle Pig, Zhuge Liang Pig... All of them have exactly the same chance and can repeat. Each one comes with a "you are..." analysis, so a roll reads like a horoscope written just for you.

## Collection

Every pig you roll is stored permanently in your player data, and it survives death. The screen shows how many times you have rolled each one and your total. After a roll, a styled card also appears in chat: name, description, collection count and the full write-up.

## Custom pigs

Add your own by dropping text into `config/roll_pig/pigsty/<id>.json` and art into `config/roll_pig/image/<id>.png`. Both folders are created automatically on first launch and come with a readme inside, so players can add pigs by themselves.

Modpack authors who want to add many pigs at once can run the bundled `tools/make_pig_pack.py` to generate a resource pack and the chat-avatar table in one command; there is also a checker for hand-drawn atlases.

## Commands

- `/rollpig` — roll once (the result is shown after the roll finishes)
- `/rollpig unlock` — unlock every pig (OP)
- `/rollpig force` — force a roll without using today's chance (OP)

## Misc

The mod ships an API and a cancellable roll event, so other mods can hook into the roll. Two config options control the dawn notification and whether rolls are broadcast; both are on by default.
