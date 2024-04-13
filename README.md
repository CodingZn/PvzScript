# pvzscript 脚本式助手

### 简介

植物大战僵尸Online脚本式助手，您可以通过命令来完成游戏当中的操作，亦可编写自定义脚本配置定时运行来解决手搓游戏的烦恼。

当前项目目前仅适配1区，可在一定程度上应对官方的拦截机制。

### 免责及版权声明

使用本助手时请谨慎，开发者不承担由于您误操作导致的任何损失，也不为任何可能存在的代码bug导致的后果负责。

禁止使用此开源代码进行营利，包括但不限于重新开发编译后设置付费版本或打赏等行为。

### 运行方式

本助手构建语言为Java 17，请确保您的电脑上安装有17版本以上的jdk或jre。

1. 源码运行

    如您配有Java开发环境，可使用您熟悉的IDE直接运行项目。项目的主类是 `src.App`

2. 编译并打jar包

    如您的电脑安装有`GNU Make`，可通过项目的Makefile文件来编译并生成jar包。请在项目根目录输入`make`命令来生成。

    `make update`命令可用于在生成jar包之后进行后续操作。可通过编辑`build.sh`来自行定义。

    也可以通过IDE自带的打包命令进行打包。但这样并不保证您的jar包是最精简的或可运行的。

3. 运行jar包

    运行前，请确保您的电脑配有jre 17版本及以上的环境。

    jar包的运行命令是`java -jar path/to/jarpackage.jar args...`。对于直接通过`make`生成的jar包，您可在项目根目录通过`java -jar pvzscript.jar`命令来（无参）运行项目。

    本项目支持有参和无参运行。有参运行时，在原命令后加一个空格，然后输入参数，如`java -jar pvzscript.jar cmd/daily`。参数具体意义参见下文。

    您也可以通过编辑batch或shell脚本来实现双击运行。

4. 运行exe可执行文件（暂未完成）

### 使用说明

1. 运行界面

    ![alt text](image.png)
    打开程序后，运行界面大致如图所示。

2. 基本操作

    无参数运行jar包时，启动的是交互模式的应用。支持交互输入命令执行和按序串行执行命令脚本。
    
    进入程序时，默认是单行命令模式（Command Mode）：每次输入一条命令，执行完后可继续输入；左侧有`>>> `提示。可通过`file`命令切换为脚本模式（FileBatch Mode）。脚本模式下，左侧有`batch filename: `提示，单次输入一个文件名，将按序执行该文件中所有命令后返回。脚本模式下可通过`cmd`命令切换回单行命令模式。

    在单条命令中，有一个特殊命令`execfile <filename>`，它支持以单条命令的方式来执行一个脚本。`<filename>`是要执行脚本的文件名（ **相对于运行路径，所有文件名相关的都是相对于运行路径** ）。此种命令也可以被写入到脚本中，以实现多级嵌套的脚本。请注意，不要试图循环嵌套，否则会执行失败。

    有参数运行jar包时，参数是一个文件名（不能有空格），程序将只执行这一个脚本文件，运行完后退出。没有任何的交互。

3. 日志查看

    默认情况下，运行并输入有效命令后，会在运行路径下生成一个log文件夹，内部是按年月命名的文件夹，文件夹内存放一个以开始运行时间+pid命名的log文件。若不想生成日志，可通过日志命令手动关闭。

4. 报错处理

    当输入命令后，运行出现`Exception in thread "main" xxxxException`时，说明程序内部或您输入的命令有误。特对一些常见的可能出现的错误举例如下：

    `Exception in thread "main" java.lang.NumberFormatException: For input string: ` 通常是由于参数输入有误，在本该是数字的参数位置输入了其他字符。

    `[Fatal Error] :1:1: 文件提前结束。`或`org.xml.sax.SAXParseException` 可能因为没有加载/设置cookie，或cookie已失效。

    `Exception in thread "main" java.lang.NullPointerException: Cannot invoke "src.Organism.toShortString()" because the return value of "src.Organism.getOrganism(int)" is null` 或 `Exception in thread "main" java.lang.NullPointerException: Cannot read field "hp_now" because "plant" is null` 可能因为要操作的植物与cookie不对应（试图操作别人的植物），或要操作的植物已消失（被合成或卖掉）。

    `错误：连接通道关闭。可能由于代理未开启导致。已关闭代理。`代理未开启或代理端口设置不对。（不需费心，程序会自动关闭使用代理并重发请求）

    (待补充...)

    如果您遇到了其他的报错，请联系开发者。

### pvzscript 脚本命令介绍

输入到程序中的所有“命令”“脚本”均指的是pvzscript专用的脚本，即本章所介绍内容。下文所有“命令”也均指的是pvzscript脚本命令。

所有命令都由两部分组成：`<命令名> <参数>`，两者之间用**一个空格**隔开，参数之间也使用**一个空格**隔开。命令名是一个单词，不区分大小写，不包含空格。

请保证**空格**的准确性。命令开头和结尾不要有多余的空格。分隔的空格必须为精确的一个。

*在交互模式下，输入`<命令名>`可获取对该命令参数格式的简略说明。对于命令参数格式的解释，放在<>之内的表示根据意义替换的参数，未加<>的表示按照原样输入。方括号[ ]表示该参数是可选的。*

**命令中，对于植物列表、洞口列表等的表示基本是以数据文件的形式：将id每行一个地存到文本文件中。而脚本文件要求每行只能有一个命令。两种文件均可以有空行，均可以用井号#开头来表示单行注释。**

以下是所有命令的介绍：

#### Cookie

```
args: load <filename> 
```
从文件中加载cookie字符串。（文本文件，内容只包含cookie字符串。


```
or  : set <cookieString>
```
直接通过参数设置cookie字符串。

---

#### Request （请求）
```
args: proxy on|off
```
开启或关闭代理。（默认开启）
```
or  : port <number>
```
设置代理端口。（默认8887）
```
or  : interval <ms>
```
设置请求间隔（毫秒为单位）。
```
or  : setblock <ms>
```
设置被验证码拦截后的等待时间（毫秒为单位）。
```
or  : setamfblock <ms>
```
设置被提示频繁后的等待时间（毫秒为单位）。
```
or  : retry <max_count> <interval_ms>
```
设置请求失败后重发请求的次数和重发请求的间隔（毫秒为单位）。

---

#### Log （日志）
```
args: on|off
```
开启或关闭文件日志。（默认开启）

#### Battle （打洞口）
```
args: <cave_file> <hard_level> <zhuli_file> [<paohui_file>]
```
按顺序刷一系列洞口，每个一次，不使用时之沙。参数分别为：要打的洞口列表的数据文件、难度等级（1为简单，2为普通，3为困难）、主力列表的数据文件、炮灰列表的数据文件（可选）
```
or  : repeat <count> <cave_id> <hard_level> <zhuli_file> [<paohui_file>]
```
重复打一个洞口，自动使用时之沙。可变参数：重复次数、洞口id，难度等级（1为简单，2为普通，3为困难）、主力列表的数据文件、炮灰列表的数据文件（可选）
```
or  : maxlevel <grade>
```
设置带级的最高等级。炮灰等级阈值。-1表示不启用，每次让所有炮灰上场。
grade>=0时，每次选择最低等级的炮灰，当无炮灰时停止。
grade==0表示没有阈值，可以无限带级。grade>=1表示设定有效炮灰的最大等级，超过该等级则不会上场。
```
or  : kpfull on|off
```
可带级炮灰无法填满时，是否使用带级完成的炮灰填满战斗格子
```
or  : updatefreq <freq>
```
战斗后同步仓库信息的频率。0表示不同步，1表示每次都同步，freq>=1表示每打n次后同步一次 。
```
or  : book <amount>|full
```
使用指定个数的挑战书，或将挑战次数填满至25。（会配对使用高级挑战书和挑战书）
```
or  : autobook <amount>
```
无挑战次数时自动使用挑战书的个数。amount=0表示不使用挑战书。要求1<=amount<=25（会配对使用高级挑战书和挑战书）

---
#### BuXie （补血）
```
args: <plantid> <xiepingid>|1|2|3
```
对指定id的植物补指定血瓶。13或1为低级血瓶，14或2为中级血瓶，15或3为高级血瓶。
```
or  : threshold <percent>
```
设置主力补血百分比阈值，**不加百分号**。如40指的是若主力血量小于40%则使用最少、最省的血瓶补到40%以上。支持小数（如0.01代表0.01%）。
```
or  : reserve <low_n> <mid_n> <high_n>
```
设置预留三种血瓶的个数，防止血瓶用尽。

---
#### Cave （洞口操作）
```
args: save <dir_to> [<least_grade>]
```
将好友中所有大于等于 <least_grade> 等级对应的公共洞口信息保存到 <dir_to> 文件夹内。如 `save data/1/cave 81`表示将好友中所有拥有大于等于81级洞口的好友的公共洞口信息保存到data/1/cave中。（配合其他命令使用）

```
or  : extract <dir> <cave_public_no>...
```
对save命令保存的洞口信息进行提取，dir需与save命令的dir一致。提取特定某些洞口的id到当前文件夹dir，以public__.txt的格式命名。参数<cave_public_no>可有多个，表示洞口序号。如22就代表公22。如`extract data/1/cave 22 23 24`表示从已保存的公洞信息中提取出22/23/24洞口。

```
or  : savean <filename> <min_no> <max_no>
```
将某个序号范围内的暗洞口id保存到一个文件。
如`savean data/1/andong 10 17`表示将暗10到暗17 八个洞id保存到 data/1/andong 文件中。

```
or  : savege <filename> <min_no> <max_no>
```
将某个序号范围内的个人洞口id保存到一个文件。
如`savean data/1/gedong 18 22`表示将个18到个22 五个洞id保存到 data/1/gedong 文件中。

---
#### DailyReward （日常领奖）
```
args: [1][2][3][4][5][6][7][8][9]
```
1:签到 2:vip 3:世界树 4:登录奖 5:斗技场排名奖 6:矿坑 7:打斗技场 8:领任务 9:花园挂机。输入对应的数字来领对应的奖励。可一次输入多个。

---
#### Route （进化路线）
```
args: search <begin_id> <end_id>
```
给定进化的起始原型id和目标原型id，输出进化路线。原型id可通过`orid`命令查询。
```
or  : save <route_number> <filename>
```
将某条进化路线（通过序号指定）保存到指定文件。

---
#### Evolution （进化）
```
args: <plantId> <route_number>
```
对某植物，用某条进化路线（通过序号指定）进化。
```
or  : file <plantId> <filename>
```
对某植物，用某条进化路线（通过文件加载）进化。
```
or  : batch <plant_file> <route_file>
```
对某些植物（通过文件加载），用某条进化路线（通过文件加载）进化。

---
#### Friend （好友）
```
args: save <filename>
```
将好友信息保存到某个文件。
```
or  : loadby remote|<filename>
```
设置好友信息加载模式，通过远程加载或通过文件加载。

---
#### FubenBattle （打世界副本）
```
args: <caveid> <plantFile> <count_n>
```
对某副本（指定id），使用某些植物（指定文件），打指定次数。

副本关卡的id可通过抓包查找。
```
or  : strategy <number>
```
更改打副本策略。0表示不适用副本书和怀表，1表示打之前使用对应数量的副本书，2表示打之前使用对应数量的副本书和怀表。

---
#### Garden （打花园怪）
```
args: battle auto|<count>
```
打指定次数的花园怪，或将次数用光为止。
```
or  : bplant <plant_filename>
```
指定打花园怪时的植物（数据文件）。
```
or  : bgrade <grade_to_beat>...
```
指定优先打哪些等级的花园怪。如```bgrade 125 91```表示先打121-125级的花园怪，没有了之后再打91-95级的。

---
#### MyTool （仓库道具）
```
args: show
```
列出仓库中的所有道具。
```
or  : search <name>
```
模糊搜索仓库中的道具。
```
or  : sell <id> <count>
```
卖出指定数量个指定道具（通过id指定）

---
#### Organism （植物管理）
```
args: show [id]
```
不加 id 时，按等级倒序展示所有植物。加 id 时，按照id顺序展示。
```
or  : search <name>
```
模糊搜索自己的植物名。
```
or  : sell <id>
```
卖掉某个id的植物。
```
or  : sellall <filename>
```
卖掉某个数据文件中的所有植物。
```
or  : filter <conditions>...
or  : filterp <from_group> <conditions>...
filters: nm == ={ <value>
filters: gr == <= >= << >> != <value>
filters: id == <= >= << >> != <value>
filters: ql == <= >= << >> != <stringvalue>
```
过滤展示某些植物。conditions条件可为下面展示的四种：通过名称（nm）精确等于（==）或包含（={）来过滤；通过等级（gr）等于（==）、小于等于（<=）、大于等于（>=）、小于（<<）、大于（>>）、不等于（!=）来过滤；通过id（操作符同等级）来过滤；通过品质（操作符同等级）来过滤。品质输入汉字名（如魔神）即可。

filter指从所有植物中过滤，filterp只从某个组（程序运行时生成的某些植物组合）内部过滤。

```
or  : save <group_no> <filename>
```
将某个植物组保存为数据文件（存id）。
```
or  : showf <filename>
```
展示某个数据文件中的植物组。

---
#### Orid （植物原型）
```
args: orid show <id>
```
展示某个植物原型。
```
or  : orid search <name>
```
通过名称模糊搜索植物原型。

---
#### Quality （品质）
```
args: <plantid> <quality_name> [max_usage]
```
对某个植物，（最多使用指定本书），提升到目标品质（汉字）。
```
or  : moshen <plantid>
```
对某个植物使用魔神刷新书刷到魔神。
```
or  : batch <plant_file> <quality_name>
```
将一批植物（指定数据文件）刷到目标品质。
```
or  : mbatch <plant_file>
```
将一批植物（指定数据文件）使用魔神刷新书刷到魔神。

---
#### ServerBattle （跨服战）
```
args: <count>|auto
```
进行指定次数跨服预赛，或次数用尽为止。
```
or  : award
```
领取跨服预赛奖励。
```
or  : add <count>
```
增加指定次跨服挑战次数。

---
#### Shop （购买或兑换物品）
```
args: buy <buy_id> <amount>
礼券购买: 1006.挑战书 1018.时之沙 1015.高级血瓶 1014.中级血瓶 1013.低级血瓶
金币购买: 218.蓝叶草 219.双叶草
荣誉购买: 4450.增强卷轴
```
可通过提示查询常见物品的购买id（**注意，并非道具id！**）。也可通过Charles抓包获取某些物品的购买id来写入。

此模块暂不完善。

---
#### Skill （普通技能）
```
args: skill show <id>
```
通过指定技能id展示某个技能。
```
or  : skill search <name>
```
模糊搜索展示某个技能。

---
#### StoneBattle （宝石副本）
```
args: <caveid> <hard_level> <total_count> <zhuli_file> <paohui_file>
```
对某个洞口，指定难度等级（1-3分别对应1-3星），打指定次数。
区分参战主力和炮灰（通过文件指定），自动选择炮灰中等级最低的来打洞，并根据伤亡情况预测升级之后的等级（默认存活的炮灰都会升2.2级）。不支持设置炮灰的等级上限，默认会填满战斗格子。相当于```battle```中的```kpfull on```和```maxlevel 0```

洞口id=(n-1)*12+m，n为宝石序号，m为关卡。
红宝石n=1，蓝宝石n=2，烟晶石n=3，白宝石n=4，绿宝石n=5.
紫晶石n=6，日光石n=7，黑耀石n=8，天河石n=9.

```
or  : checkcount on|off
```
设置策略：在挑战次数不够命令的次数时是否打洞。

---
#### Territory （领地）
```
args: award
```
领取领地奖励
```
or  : battle <userid> <plant1> [<plant2>]
```
使用一个或两个植物（指定id），占领某个用户的领地。（可能是占领空的，也可能是抢夺）
```
or  : keep <userid_file> <plant_file>
```
保持用某些植物占领某些用户的领地。（植物自动分配）
```
or  : save <userid_file> <plant_file>
```
将当前的占领情况（用户列表和植物列表）分别保存到指定文件。

---
#### Tool （道具）
```
args: show <id>
```
展示某个道具简介。
```
or  : search <name>
```
通过名称模糊搜索道具。

---
#### Warehouse （仓库）
```
args: open [<goal>]
```
将仓库的生物格子开到指定个数。如不指定，将开到最大（192）。

### 版本更新历史。

- 2024.04.13：v1.3.x版本。宝石副本增加炮灰选择、检查剩余次数功能。去除作弊模式。

- 2024.03.21：v1.2.x版本。修复bug，添加了花园战斗相关功能。添加md文档。

- 2024.03.14：v1.1.x版本。修复一些bug，添加了打跨服和领取跨服奖励的功能。

- 2024.03.09：v1.0.x版本，支持大部分功能。

- 2024.02：v0.9，添加Makefile，完善功能。

- 2024.01：完善功能，将不同模块的脚本整合为一个独立程序。

- 2023.12：实现进化模块和刷品模块。

### 作者信息

在程序启动时以及输入命令`info`可查看应用简略信息。

CodingZn@GitHub

官服1区“陈年老榴莲”

[百度贴吧记录贴](https://tieba.baidu.com/p/8946953897)