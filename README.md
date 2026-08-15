# LeavesAntiIllegal 3.1.0

LeavesAntiIllegal is a Bukkit API based illegal-item scanner for Spigot, Paper, Purpur, Leaves, and other Bukkit-compatible servers. It requires Java 17 and supports Minecraft 1.20 through 26.2. The project was restored and migrated from FoliaAntiIllegal; it no longer depends on Folia-specific scheduling APIs.

## Features

- Checks items when they are picked up, clicked in inventories, moved, swapped, or added to a player's inventory.
- Periodically scans online player inventories, ender chests, off-hand items, cursor items, and player data.
- Scans loaded container blocks and container entities in batches without loading new chunks.
- Asynchronously scans offline player data in `playerdata/<UUID>.dat` during low-load windows.
- Recursively checks shulker boxes, bundles, and other nested containers.
- Detects illegal materials, excessive enchantments, abnormal stacks, durability or attribute values, and unbreakable markers.

Documentation:

- [Documentation Center](docs/index.html)
- [Server Owner Guide](docs/server-owner.html)
- [Developer Guide](docs/developer.html)

The documentation consists of static HTML files and can be opened directly without a web server.

## Installation

1. Install Java 17 or newer and use a Bukkit API compatible server from Minecraft 1.20 through 26.2.
2. Place `LeavesAntiIllegal-3.1.0.jar` in the server's `plugins/` directory.
3. Start the server once and inspect `plugins/LeavesAntiIllegal/config.yml`.
4. For the first deployment, set `scanners.offline-player-data.dry-run` to `true` and observe one low-load window.
5. After verifying the log output, set it to `false` and run `/antiillegal reload`.

All scanners are enabled by default. Before modifying offline player data, the plugin creates `<UUID>.dat.fai.bak` in the same directory and replaces the original file through a temporary file.

## Migration From FoliaAntiIllegal

The plugin directory changes from `plugins/FoliaAntiIllegal/` to `plugins/LeavesAntiIllegal/`. Do not directly overwrite the new configuration with the old one. Generate the complete 3.1.0 `config.yml` first, then merge illegal materials, attribute thresholds, whitelist entries, and messages from the old server configuration item by item.

Stop the server and back up all worlds before upgrading. Do not load the old and new JAR files together. After removing the old JAR, place the new version in the `plugins/` directory. The `/antiillegal` command, `/ai` and `/illegal` aliases, and `antiillegal.*` permission nodes remain unchanged.

## Building

The project uses the Spigot Bukkit API compatibility baseline and Java 17:

```bash
mvn clean package
```

The output file is:

```text
plugin/target/LeavesAntiIllegal-3.1.0.jar
```

The plugin depends only on the Bukkit public API. The Querz NBT library is shaded and relocated to `dev.leavesantiillegal.lib.querz`, so servers do not need to install another dependency.

Version-specific implementations are split under `versions/`:

- `v1_20_1`: Minecraft 1.20.x adapters
- `v1_21`: Minecraft 1.21.x adapters
- `v26_2`: Minecraft 26.2.x adapters

At startup, the plugin detects the Bukkit version and loads the matching adapter. Shared scanning logic does not depend on private server implementations.

## Runtime Constraints

Online players and loaded container blocks are scanned in batches from the Bukkit main thread. Offline `.dat` reads, NBT processing, backups, and writes run in asynchronous tasks. Login/logout protection and per-player file locks prevent the scanner and the server from modifying the same player data simultaneously.

Offline scans pause or stop when the online player count exceeds the configured threshold or the server leaves its low-load window. An incomplete scan is never recorded as successful.

The source code uses only Bukkit/Spigot public APIs. Paper, Purpur, Leaves, and other compatible servers provide those APIs themselves. The plugin does not call NMS, CraftBukkit internals, or server-specific implementation classes.

## GitLab CI Publishing

The release version is stored in `version.txt`, and the release description is stored in `Description.txt`. GitLab CI runs the clean stage, performs the standard Maven build, publishes the JAR to Modrinth, and creates the matching GitLab Release with the `v<version>` tag.

Set these protected and masked GitLab CI/CD variables:

- `MODRINTH_TOKEN`
- `MODRINTH_PROJECT_ID`

The Modrinth publication uses the `bukkit` loader and declares Minecraft versions 1.20 through 26.2.

---

# LeavesAntiIllegal 3.1.0

LeavesAntiIllegal 是一个基于 Bukkit API 的违规物品扫描插件，兼容 Spigot、Paper、Purpur、Leaves 以及其他 Bukkit 兼容服务端。插件要求 Java 17，支持 Minecraft 1.20 至 26.2。本项目由 FoliaAntiIllegal 恢复并迁移而来，已经移除对 Folia 专用调度 API 的依赖。

## 功能特性

- 在拾取物品、点击容器、移动物品、交换手持物品以及物品加入玩家背包时进行检查。
- 定时扫描在线玩家背包、末影箱、副手、光标物品以及玩家数据。
- 分批扫描已经加载的方块容器和容器实体，不会为了扫描主动加载新区块。
- 在低负载时间异步扫描 `playerdata/<UUID>.dat` 中的离线玩家数据。
- 递归检查潜影盒、收纳袋以及其他嵌套容器。
- 检测违规材料、超限附魔、异常堆叠、耐久或属性异常以及不可破坏标记。

相关文档：

- [文档中心](docs/index.html)
- [服主文档](docs/server-owner.html)
- [开发者文档](docs/developer.html)

文档是静态 HTML 文件，可以直接打开，不需要 Web 服务器。

## 安装

1. 安装 Java 17 或更高版本，并使用 Minecraft 1.20 至 26.2 的 Bukkit API 兼容服务端。
2. 将 `LeavesAntiIllegal-3.1.0.jar` 放入服务端的 `plugins/` 目录。
3. 启动一次服务端，检查 `plugins/LeavesAntiIllegal/config.yml`。
4. 首次部署时，建议将 `scanners.offline-player-data.dry-run` 设置为 `true`，观察一个低负载时间窗口。
5. 确认日志符合预期后，将其改为 `false`，并执行 `/antiillegal reload`。

所有扫描器默认开启。离线玩家数据真正修改前，插件会在原目录创建 `<UUID>.dat.fai.bak` 备份，并通过临时文件替换原数据。

## 从 FoliaAntiIllegal 迁移

插件目录由 `plugins/FoliaAntiIllegal/` 改为 `plugins/LeavesAntiIllegal/`。不要直接使用旧配置覆盖新配置。请先生成完整的 3.1.0 `config.yml`，再逐项合并旧服务端配置中的违规材料、属性阈值、白名单和消息内容。

升级前请停止服务端并备份所有世界。旧 JAR 和新 JAR 不能同时加载。删除旧 JAR 后，再将新版本放入 `plugins/` 目录。`/antiillegal` 命令、`/ai` 和 `/illegal` 别名以及 `antiillegal.*` 权限节点保持不变。

## 构建

项目使用 Spigot Bukkit API 兼容基线和 Java 17：

```bash
mvn clean package
```

输出文件：

```text
plugin/target/LeavesAntiIllegal-3.1.0.jar
```

插件只依赖 Bukkit 公共 API。Querz NBT 库会被 shade 并重定位到 `dev.leavesantiillegal.lib.querz`，服务端不需要额外安装依赖。

不同版本的实现拆分在 `versions/` 目录：

- `v1_20_1`：Minecraft 1.20.x 适配器
- `v1_21`：Minecraft 1.21.x 适配器
- `v26_2`：Minecraft 26.2.x 适配器

插件启动时会检测 Bukkit 版本并加载对应适配器。公共扫描逻辑不依赖服务端私有实现。

## 运行约束

在线玩家和已加载的容器方块由 Bukkit 主线程分批扫描。离线 `.dat` 读取、NBT 处理、备份和写回都在异步任务中执行。登录/退出保护和每个玩家的数据文件锁会防止扫描器与服务端同时修改同一份玩家数据。

当在线人数超过配置阈值或服务端离开低负载时间窗口时，离线扫描会暂停或停止。未完成的扫描不会被记录为成功。

源代码只使用 Bukkit/Spigot 公共 API。Paper、Purpur、Leaves 以及其他兼容服务端会自行提供这些 API。插件不会调用 NMS、CraftBukkit 内部实现或服务端专用类。

## GitLab CI 发布

版本号保存在 `version.txt`，发布描述保存在 `Description.txt`。GitLab CI 会先执行 clean 阶段，再执行标准 Maven 构建，将 JAR 发布到 Modrinth，并使用 `v<版本号>` 标签创建对应的 GitLab Release。

请在 GitLab 项目的 CI/CD Variables 中配置以下受保护且隐藏的变量：

- `MODRINTH_TOKEN`
- `MODRINTH_PROJECT_ID`

Modrinth 发布使用 `bukkit` loader，并声明 Minecraft 1.20 至 26.2 的版本列表。
