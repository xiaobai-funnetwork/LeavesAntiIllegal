# LeavesAntiIllegal 3.1.0

基于 Bukkit API 的违禁物品扫描插件，要求 Java 17，可运行于 Minecraft 1.20 至 26.2 的 Spigot、Paper、Purpur、Leaves
等 Bukkit API 兼容服务端。本项目从旧版 FoliaAntiIllegal 恢复并迁移而来，已移除 Folia
专用调度 API。

## 扫描范围

- 拾取、库存点击、拖拽、切换手持物和玩家加入时的实时检查。
- 定时扫描在线玩家背包、盔甲、副手、光标物品和末影箱。
- 分批扫描所有已加载区块中的方块容器与容器实体，不会为扫描加载新区块。
- 在低峰窗口异步遍历各世界 `playerdata/<UUID>.dat`，扫描离线背包与末影箱。
- 递归检查潜影盒、收纳袋等嵌套容器，深度可配置。
- 检测违禁材料、超限附魔、异常堆叠/耐久/属性以及不可破坏标记。

文档统一入口见 [文档中心](docs/index.html)。完整的部署、参数解释和恢复流程见
[服主文档](docs/server-owner.html)，线程模型、构建方式和代码结构见
[开发者文档](docs/developer.html)。这些文档都是可直接打开的静态 HTML，不需要 Web 服务器。

## 安装

1. 使用 Java 17 或更高版本启动 Bukkit API 兼容服务端（支持 1.20 至 26.2）。
2. 将 `LeavesAntiIllegal-3.1.0.jar` 放入服务器的 `plugins/`。
3. 启动一次服务器，检查 `plugins/LeavesAntiIllegal/config.yml`。
4. 首次部署建议先把 `scanners.offline-player-data.dry-run` 设为 `true`，观察一个低峰窗口。
5. 确认日志符合预期后改回 `false`，执行 `/antiillegal reload`。

插件的全部扫描器默认开启。离线扫描真正修改文件前默认创建同目录备份
`<UUID>.dat.fai.bak`，并通过临时文件原子替换原数据。

## 从 FoliaAntiIllegal 迁移

插件名已改变，因此数据目录从 `plugins/FoliaAntiIllegal/` 变为
`plugins/LeavesAntiIllegal/`。不要直接用旧配置覆盖新配置：先让 3.1.0 生成带完整中文
注释的新 `config.yml`，再把旧服的违禁材料、属性阈值、白名单和消息逐项合并。

升级前请停止服务器并备份所有世界。旧 JAR 与新 JAR 不能同时加载；移除旧 JAR 后再
放入新版本。命令 `/antiillegal`、别名 `/ai`、`/illegal` 和 `antiillegal.*` 权限节点保持
不变。

## 构建

默认按 Spigot Bukkit API 构建兼容基线：

```bash
mvn clean package
```

输出：`plugin/target/LeavesAntiIllegal-3.1.0.jar`

构建使用 Spigot 官方 Bukkit API 1.20.1 基线和 Java 17。插件只依赖 Bukkit 公共 API，面向 1.20 至 26.2 保持向后兼容；Querz NBT 会被 shade 并重定位到
`dev.leavesantiillegal.lib.querz`，服务器无需另装依赖。

版本相关实现按 Dominion 风格拆分在 `versions/`：`v1_20_1` 处理 1.20.x，`v1_21` 处理 1.21.x，`v26_2` 处理 26.2.x。启动时根据 Bukkit 版本加载对应适配器，公共扫描逻辑不依赖服务端私有实现。

## 运行约束

在线玩家和已加载区块容器由 Bukkit 主线程分批扫描。离线 `.dat` 的读取、NBT 处理、
备份和写回只在异步任务中执行；登录/退出保护与每玩家文件锁防止扫描器和服务器同时
操作同一玩家数据。离线扫描遇到在线人数超过阈值或低峰窗口结束时会暂停/停止，不会
把未完成的一轮记为成功。

项目源码只依赖 Bukkit/Spigot 公共 API；Paper、Purpur、Leaves 等兼容服务端由其自身提供
该 API。插件不调用 NMS、CraftBukkit 或任意服务端实现专用类。

## GitLab CI 发布

版本号维护在根目录 `version.txt`，描述维护在 `Description.txt`。GitLab CI 会先执行
clean，再执行标准 Maven 构建，最后把 `plugin/target/LeavesAntiIllegal-<version>.jar`
发布到 Modrinth。发布任务默认在默认分支手动执行，提交 Tag 时自动执行。

请在 GitLab 项目 CI/CD Variables 中配置 `MODRINTH_TOKEN` 和
`MODRINTH_PROJECT_ID`，其中 Token 应设为 Masked/Protected。Modrinth 发布使用
`bukkit` loader，并勾选 Minecraft 1.20 至 26.2 的版本列表。
