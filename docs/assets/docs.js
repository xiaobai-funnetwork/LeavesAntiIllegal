(() => {
  const language = localStorage.getItem('leavesantiillegal-language') || 'zh';
  const originalText = new WeakMap();
  const translations = {
    'zh-CN': {
      '文档导航': 'Documentation navigation', '服主手册': 'Server Owner Guide', '开发者文档': 'Developer Guide',
      '文档首页': 'Documentation home', '返回文档首页': 'Back to documentation home', '筛选本文档': 'Filter this document',
      '共 12 个章节': '12 sections', '共 13 个章节': '13 sections', '打开目录': 'Open table of contents',
      '没有匹配的章节，请尝试材料名、配置键或命令。': 'No matching sections. Try a material name, configuration key, or command.',
      '没有匹配的章节，请尝试类名、配置键或 NBT 字段。': 'No matching sections. Try a class name, configuration key, or NBT field.',
      '复制': 'Copy', '已复制': 'Copied', '返回顶部': 'Back to top', '项目 README': 'Project README',
      '开始部署': 'Start deployment', '查看架构': 'View architecture', '选择适合你的文档': 'Choose your guide',
      '三步开始使用': 'Get started in three steps', '项目与上游资源': 'Project resources', '构建发布基线': 'Release baseline',
      '安装迁移、配置解释、低峰扫描、命令权限、备份恢复与性能调优。': 'Installation, migration, configuration, off-peak scans, permissions, recovery, and tuning.',
      '总体架构': 'Architecture', '线程约束': 'Threading constraints', '生命周期': 'Lifecycle', '在线检测链': 'Online detection path',
      '已加载容器': 'Loaded containers', '离线数据流水线': 'Offline data pipeline', 'NBT 兼容层': 'NBT compatibility layer',
      '规则快照': 'Rule snapshot', '源码索引': 'Source map', '构建与依赖': 'Build and dependencies', '扩展约束': 'Extension constraints',
      '验证与发布': 'Verification and release', '兼容契约': 'Compatibility contract', '配置索引': 'Configuration index',
      '扫描范围': 'Scan coverage', '全新安装': 'Fresh installation', '旧版迁移': 'Migration', '首次上线': 'First run',
      '离线低峰扫描': 'Off-peak offline scan', '命令与权限': 'Commands and permissions', '备份与恢复': 'Backup and recovery',
      '性能调优': 'Performance tuning', '故障排查': 'Troubleshooting', '上线检查表': 'Launch checklist', '数据共享': 'Data sharing',
      '适用范围': 'Scope', '部署与运维': 'Deployment and operations', '开发与发布': 'Development and release',
      '打开服主手册': 'Open server owner guide', '打开开发者文档': 'Open developer guide', '切换语言': 'Switch language',
      'Leaves 标识': 'Leaves logo', '兼容信息': 'Compatibility', '扫描范围摘要': 'Scan coverage summary',
      '技术基线': 'Technical baseline', '项目与上游资源': 'Project resources', '官方核心资料见': 'Official server references:',
      '最后更新：': 'Last updated: ', '部署与运维': 'Deployment and operations', '开发与发布': 'Development and release',
      '服主手册': 'Server Owner Guide', '开发者文档': 'Developer Guide', 'README': 'README', '插件': 'Plugin',
      'Java 包结构': 'Java package structure', '发布前快速检查': 'Pre-release checks', '发布产物选择': 'Release artifact choice',
      '复制': 'Copy', '构建与依赖': 'Build and dependencies', '双版本构建': 'Build commands',
      '打开目录': 'Open table of contents', '返回顶部': 'Back to top',
      '先确认运行环境': 'Confirm the runtime environment', '全新安装': 'Fresh installation', '从 FoliaAntiIllegal 升级': 'Upgrade from FoliaAntiIllegal',
      '推荐的分阶段上线': 'Recommended staged rollout', '扫描器如何覆盖物品': 'How scanners cover items', '关键配置怎么选': 'Choosing key settings',
      '在线检测链': 'Online detection path', '离线数据流水线': 'Offline data pipeline', '启动、重载与关闭': 'Startup, reload, and shutdown',
      '插件已经注册bStats': 'bStats registration', '服务器必须使用 Java 21。': 'The server must run Java 21.',
      '配置与全部扫描器已重新加载': 'Configuration and all scanners reloaded', '没有匹配的章节': 'No matching sections',
      '安装并生成配置': 'Install and generate configuration', '先演练离线扫描': 'Practice offline scanning first',
      '部署与运维': 'Deployment and operations', '开发与发布': 'Development and release'
    }
  };

  Object.assign(translations['zh-CN'], {
    '文档中心 | LeavesAntiIllegal 3.1.0': 'Documentation | LeavesAntiIllegal 3.1.0',
    'LeavesAntiIllegal文档中心：服主部署手册与开发者技术参考': 'LeavesAntiIllegal documentation: server administration and developer references',
    '从部署到扩展，快速了解 LeavesAntiIllegal': 'Learn LeavesAntiIllegal from deployment to extension',
    '面向 Leaves 1.21.10 / 1.21.11 的违禁物品扫描插件，覆盖在线玩家、已加载容器与离线玩家数据。': 'An illegal-item scanner for Leaves 1.21.10 / 1.21.11, covering online players, loaded containers, and offline player data.',
    '开始部署': 'Start deployment', '查看架构': 'View architecture', '四条检测链路': 'Four detection paths',
    '实时事件': 'Real-time events', '拾取、点击、换手、交互与玩家加入。': 'Pickup, clicks, hand swaps, interaction, and player joins.',
    '在线定时': 'Online schedule', '背包、盔甲、副手、光标与末影箱。': 'Inventory, armor, off-hand, cursor, and ender chest.',
    '已加载容器': 'Loaded containers', '分批扫描方块和容器实体，不加载新区块。': 'Batch-scan block and entity containers without loading new chunks.',
    '离线数据': 'Offline data', '低峰期安全处理 playerdata，支持备份与原子替换。': 'Safely process playerdata off-peak with backups and atomic replacement.',
    '两份手册可以直接在浏览器中打开，不需要部署 Web 服务。': 'Both guides open directly in a browser and do not require a web server.',
    '安装迁移、配置解释、低峰扫描、命令权限、备份恢复与性能调优。': 'Installation, migration, configuration, off-peak scans, command permissions, backup recovery, and performance tuning.',
    '总体架构、线程所有权、在线检测链、NBT 写回协议、构建与扩展约束。': 'Architecture, thread ownership, online detection, NBT write-back, builds, and extension constraints.',
    '三步开始使用': 'Get started in three steps', '准备运行环境': 'Prepare the runtime', '使用 Java 21 启动 Leaves 1.21.10 或 1.21.11。': 'Run Leaves 1.21.10 or 1.21.11 with Java 21.',
    '安装并生成配置': 'Install and generate the configuration', '把 JAR 放入': 'Put the JAR in', '启动一次服务器。': 'Start the server once.',
    '先演练离线扫描': 'Practice offline scanning first', '首次部署建议启用': 'Enable', '确认日志后再写回。': 'and only write changes after reviewing the logs.',
    '输出': 'Output', '需要 1.21.11 兼容编译？': 'Need a 1.21.11 compatibility build?', '项目与上游资源': 'Project resources',
    '功能、安装与迁移概览': 'Features, installation, and migration overview', '核心安装与运行参考': 'Server installation and runtime reference', 'API 与上游源码': 'API and upstream source',
    'LeavesAntiIllegal 3.1.0 · 文档中心': 'LeavesAntiIllegal 3.1.0 · Documentation', '最后更新：2026-08-15': 'Last updated: 2026-08-15',

    '服主手册 | LeavesAntiIllegal': 'Server Owner Guide | LeavesAntiIllegal', 'LeavesAntiIllegal 3.1.0 服主安装、配置、迁移与恢复手册': 'LeavesAntiIllegal 3.1.0 server installation, configuration, migration, and recovery guide',
    '运维参考 / Server owner guide': 'Operations reference / Server owner guide', 'LeavesAntiIllegal 服主手册': 'LeavesAntiIllegal Server Owner Guide',
    '用于部署、配置和维护 Leaves 1.21.10 / 1.21.11 上的在线玩家、已加载容器与离线玩家数据扫描。': 'Deploy, configure, and maintain online-player, loaded-container, and offline-player scanning on Leaves 1.21.10 / 1.21.11.',
    '配置版本 3': 'Config version 3', '插件版本 3.1.0': 'Plugin version 3.1.0', '共 12 个章节': '12 sections',
    '没有匹配的章节，请尝试材料名、配置键或命令。': 'No matching sections. Try a material name, configuration key, or command.',
    '先确认运行环境': 'Confirm the runtime environment', '此版本只以 Leaves 的标准 Bukkit 主线程模型为目标，不再使用 Folia 区域调度器。插件已分别通过 Leaves 1.21.10 与 1.21.11 API 编译，服务器必须使用 Java 21。': 'This version targets the standard Bukkit main-thread model and no longer uses Folia region schedulers. The plugin was compiled against Leaves 1.21.10 and 1.21.11 APIs, and the server must run Java 21.',
    '在线数据': 'Online data', '背包、盔甲、副手、光标和末影箱。': 'Inventory, armor, off-hand, cursor, and ender chest.',
    '世界容器': 'World containers', '只处理当前已加载区块，不主动加载新区块。': 'Only currently loaded chunks are processed; new chunks are never loaded on purpose.',
    '离线数据': 'Offline data', '低峰期分批处理各世界 playerdata 文件。': 'Process each world\'s playerdata files in batches during off-peak hours.',
    '不要在 Folia 上加载 2.0.0以及更高版本！': 'Do not load version 2.0.0 or later on Folia!',
    '本版的世界、实体和库存操作按 Leaves/Bukkit 单主线程约束实现。若以后重新切换 Folia，需要恢复区域与实体调度设计，不能只修改': 'World, entity, and inventory operations follow the Leaves/Bukkit single-main-thread model. Supporting Folia again would require region and entity scheduling; changing only',
    '全新安装': 'Fresh installation', '停止服务器并做世界备份': 'Stop the server and back up the worlds', '至少保留所有世界目录和现有': 'Keep all world directories and the existing',
    '检查 Java 与核心': 'Check Java and the server core', '核心版本为 Leaves 1.21.10 或 1.21.11。': 'The core must be Leaves 1.21.10 or 1.21.11.',
    '放入插件': 'Install the plugin', '放到服务器': 'in the server', '启动一次': 'Start once', '等待生成': 'Wait for', '确认控制台出现启用日志。': 'and confirm the enable message appears in the console.',
    '先做演练': 'Run a dry run first', '改成': 'set it to', '重载后观察一个低峰窗口。': 'then reload and observe one off-peak window.',
    '目录结果': 'Directory layout', '在一次离线全量扫描成功结束后生成，用于记录最近完成日期，避免同一天重复遍历。': 'is created after a successful full offline scan to record the last completion date and prevent duplicate scans on the same day.',
    '从 FoliaAntiIllegal 升级': 'Upgrade from FoliaAntiIllegal', '项目名、插件名、主类和 Java 包均已更改。Bukkit 会因此使用新的数据目录，旧配置不会自动进入新插件。': 'The project name, plugin name, main class, and Java package have changed. Bukkit therefore uses a new data directory, and the old configuration is not imported automatically.',
    '项目': 'Item', '旧版': 'Previous', '插件名': 'Plugin name', '数据目录': 'Data directory', '目标核心': 'Target core', '命令与权限': 'Commands and permissions', '保持不变': 'Unchanged',
    'Folia / Paper 旧目标': 'Former Folia / Paper target', '关闭服务器': 'Stop the server', '不能热替换插件 JAR，也不要同时保留新旧两份 JAR。': 'Do not hot-swap the plugin JAR or keep old and new JARs loaded together.',
    '移走旧 JAR': 'Remove the old JAR', '保留旧数据目录作为参考，但不要让旧插件再次加载。': 'Keep the old data directory for reference, but do not load the old plugin again.',
    '让新版本生成配置': 'Let the new version generate the configuration', '逐项合并业务规则': 'Merge business rules individually', '演练后启用写回': 'Enable write-back after the dry run',
    '同名配置不代表结构相同': 'The same file name does not mean the same structure', '推荐的分阶段上线': 'Recommended staged rollout',
    '第一阶段：只观察离线数据': 'Phase 1: observe offline data only', '第二阶段：核对误报': 'Phase 2: review false positives', '第三阶段：调整白名单和规则': 'Phase 3: adjust allowlists and rules', '第四阶段：启用离线写回': 'Phase 4: enable offline write-back',
    '扫描器如何覆盖物品': 'How scanners cover items', '来源': 'Source', '包含': 'Includes', '不会做什么': 'Does not do', '在线玩家': 'Online players', '方块容器': 'Block containers', '实体容器': 'Entity containers', '离线文件': 'Offline files',
    '关键配置怎么选': 'Choosing key settings', '发行包中的': 'The release package\'s', '扫描节奏': 'Scan cadence', '配置键': 'Configuration key', '默认': 'Default', '建议': 'Recommendation', '判定规则': 'Detection rules', '配置': 'Setting', '用途': 'Purpose', '容易误伤的场景': 'Common false-positive cases',
    '重载会重启全部扫描器': 'Reload restarts all scanners', '修改后执行': 'Run', '不需要重启服务器。': 'A server restart is not required.', '低峰扫描的边界与保护': 'Off-peak scan boundaries and protection',
    '窗口控制': 'Window control', '负载控制': 'Load control', '文件保护': 'File protection', '推荐窗口示例': 'Recommended window example', '每日 03:30 至 05:30，仅在线不超过 2 人': 'Daily 03:30 to 05:30, with no more than 2 online players',
    '命令与权限': 'Commands and permissions', '作用': 'Action', '说明': 'Notes', '权限': 'Permission', '离线扫描还会自动跳过服务器 OP 与 UUID 白名单。': 'Offline scanning also skips server operators and UUID allowlists automatically.',
    '备份与单玩家恢复': 'Backup and single-player recovery', '恢复必须在停服状态进行': 'Recovery must be performed while the server is stopped', '按症状调优': 'Tune by symptom', '症状': 'Symptom', '先调整': 'Adjust first', '取舍': 'Trade-off',
    '常见问题': 'Common issues', '插件未加载或显示红色': 'Plugin does not load or appears red', '离线扫描没有启动': 'Offline scanning did not start', '合法物品被删除': 'A legitimate item was removed', '配置重载后没有生效': 'Configuration changes did not take effect after reload',
    '正式上线检查表': 'Production launch checklist', '插件已经注册bStats': 'bStats registration', '数据共享': 'Data sharing', 'LeavesAntiIllegal· 服主手册': 'LeavesAntiIllegal · Server Owner Guide',

    '开发者文档 | LeavesAntiIllegal 3.1.0': 'Developer Guide | LeavesAntiIllegal 3.1.0', '开发者文档 · 3.1.0': 'Developer Guide · 3.1.0', 'LeavesAntiIllegal 开发者文档': 'LeavesAntiIllegal Developer Guide',
    '说明从 Folia 调度模型迁移到 Leaves/Bukkit 后的模块边界、线程所有权、离线 NBT 写入协议和双版本构建方法。': 'Explains module boundaries, thread ownership, offline NBT write-back, and compatibility builds after migrating from Folia scheduling to Leaves/Bukkit.',
    '兼容契约': 'Compatibility contract', '维度': 'Dimension', '约束': 'Constraint', '实现': 'Implementation', '服务端': 'Server', '线程': 'Threading', '离线 NBT': 'Offline NBT', '插件标识': 'Plugin identity',
    '不是 Folia 双平台插件': 'This is not a dual Folia plugin', '总体架构': 'Architecture', '触发源': 'Triggers', '事件、定时任务、命令、低峰窗口': 'Events, scheduled tasks, commands, off-peak windows', '数据适配': 'Data adapters', '规则判定': 'Rule checks', '处置与审计': 'Remediation and auditing',
    '在线 ItemStack 规则快照、递归库存清理与通知。': 'Online ItemStack rule snapshots, recursive inventory cleanup, and notifications.', '维护已加载区块索引，在主线程分批遍历库存。': 'Maintains the loaded-chunk index and scans inventories in main-thread batches.', '控制低峰调度、并发保护、文件提交和状态。': 'Controls off-peak scheduling, concurrency protection, file commits, and state.',
    '线程所有权': 'Thread ownership', '操作': 'Operation', '原因': 'Reason', '主线程': 'Main thread', '异步': 'Async', '文件锁域': 'File-lock scope', '可跨线程': 'Cross-thread safe',
    '启动、重载与关闭': 'Startup, reload, and shutdown', '读取配置': 'Load configuration', '注册入口': 'Register entry points', '标记在线 UUID': 'Mark online UUIDs', '启动扫描器': 'Start scanners',
    '在线检测链': 'Online detection path', '入口': 'Entry point', '时机': 'When', '处置': 'Action', '玩家拾取': 'Player pickup', '点击槽位或光标': 'Slot or cursor click', '创造栏生成物品': 'Creative inventory creation',
    '已加载容器扫描': 'Loaded container scanning', '离线数据流水线': 'Offline data pipeline', '窗口检查': 'Window check', '主线程快照': 'Main-thread snapshot', '异步批处理': 'Async batch', '事务式写回': 'Transactional write-back',
    'NBT 兼容层': 'NBT compatibility layer', '语义': 'Meaning', '现代字段': 'Modern field', '旧字段': 'Legacy field', '规则快照与判定顺序': 'Rule snapshot and check order',
    '材料黑名单': 'Material blacklist', '附魔等级': 'Enchantment level', '耐久': 'Durability', '堆叠': 'Stack size', '属性': 'Attributes', '不可破坏': 'Unbreakable', '源码索引': 'Source map', '职责': 'Responsibility', '主要协作者': 'Main collaborators',
    '构建与依赖': 'Build and dependencies', '发布行为': 'Release behavior', '扩展时必须保留的约束': 'Constraints to preserve when extending', '增加一种在线判定': 'Add an online check', '增加一种扫描来源': 'Add a scan source', '修改离线写回': 'Modify offline write-back', 'bStats上报': 'bStats reporting', '按照bStats要求添加相关内容': 'Add the required bStats integration', '验证与发布': 'Verification and release', '最后更新：2026-08-15 · API 来源见': 'Last updated: 2026-08-15 · API source:', 'LeavesAntiIllegal 3.1.0 · 开发者文档': 'LeavesAntiIllegal 3.1.0 · Developer Guide',
    '离线扫描、已加载容器、低峰窗口和文件保护均是新版字段。直接覆盖会让缺失项回落到代码默认值，但你会失去完整注释，也不便于审计实际行为。': 'Offline scanning, loaded containers, off-peak windows, and file protection are new settings. Replacing the file directly falls back to code defaults for missing fields and removes the comments needed to audit behavior.',
    '嵌套扫描默认最多 3 层，覆盖潜影盒、带库存的方块状态物品和收纳袋。增加深度会提高恶意复杂 NBT 的处理成本。': 'Nested scanning is limited to three levels by default and covers shulker boxes, block-state inventory items, and bundles. Increasing the depth raises the cost of processing complex malicious NBT.',
    '发行包中的': 'The release package\'s', '已为每个节点、字段和列表项提供中文解释、默认值与示例。以下是运维时最常调整的参数。': 'documents every node, field, and list item with defaults and examples. The following settings are adjusted most often.',
    '离线扫描只会在当前时间处于配置窗口、距离上次成功运行已满足天数且在线人数不高于阈值时开始。跨午夜窗口会归属到窗口开始日。': 'Offline scanning starts only inside the configured window, after the minimum number of days since the last successful run, and while the online count is within the limit. A window crossing midnight belongs to its start date.',
    '窗口结束时未完成的一轮会停止，不写入成功日期，下个合适窗口会重新枚举。': 'An unfinished sweep stops when the window ends. Its date is not recorded as successful, and files are enumerated again in the next eligible window.',
    '在线人数超过阈值时当前批次暂停，不会继续读下一个文件。': 'When the online count exceeds the limit, the current batch pauses and does not continue to the next file.',
    '玩家预登录、在线期间和退出后的保护时间内，其 UUID 文件会被跳过。': 'A player UUID file is skipped during pre-login, while online, and during the post-logout protection period.',
    'dry-run: true 只检测和记录，不创建备份、不写回原文件。': 'dry-run: true only detects and logs. It does not create backups or write to the original file.',
    '恢复必须在停服状态进行': 'Recovery must be performed while the server is stopped', '在线替换 playerdata 可能被服务器内存中的玩家数据覆盖，也可能造成文件损坏。大范围回滚优先恢复整服世界备份。': 'Replacing playerdata while online can be overwritten by data held in server memory and may corrupt the file. For broad rollbacks, restore a full world backup first.',
    '批次大小直接决定一次任务占用主线程或磁盘的工作量；间隔决定完成整轮扫描所需时间。每次只改一个变量，结合 spark 或服务端 timings 对比。': 'Batch size determines main-thread or disk work per task; the interval determines how long a full sweep takes. Change one variable at a time and compare with spark or server timings.',
    '静态文档在桌面与移动视口无横向正文溢出，搜索、目录和复制按钮可用。': 'Static documentation has no horizontal body overflow on desktop or mobile, and search, navigation, and copy buttons work.'
    , '代码以 Leaves 1.21.10 API 为发布基线，同时通过 Maven profile 使用 Leaves 1.21.11 API 重新编译。线上产物不直接调用 Leaves 私有实现，只依赖 Bukkit/Paper 公开类型和 Bukkit 调度器。': 'The code uses the Leaves 1.21.10 API as its release baseline and recompiles against the Leaves 1.21.11 API through a Maven profile. The artifact uses only public Bukkit/Paper types and the Bukkit scheduler.',
    '事件、定时任务、命令、低峰窗口': 'Events, scheduled tasks, commands, and off-peak windows', '材料、附魔、耐久、堆叠、属性': 'Materials, enchantments, durability, stack size, and attributes', '移除、通知、日志、备份、统计': 'Removal, notifications, logs, backups, and metrics',
    'Bukkit 活对象，不能并发访问。': 'Bukkit live objects must not be accessed concurrently.', '区块和实体生命周期归服务端主线程。': 'Chunk and entity lifecycles belong to the server main thread.', '避免在异步任务访问 Bukkit 全局状态。': 'Avoid accessing global Bukkit state from async tasks.', '文件系统和压缩 NBT 可能阻塞。': 'File I/O and compressed NBT may block.', '按玩家 UUID 串行，避开主线程。': 'Serialized by player UUID away from the main thread.', '使用并发集合、volatile 快照与原子类型。': 'Uses concurrent collections, volatile snapshots, and atomic types.',
    '注册表快照、OP 与世界目录获取': 'Registry snapshots, operator data, and world-directory lookup', '目录枚举与': 'Directory enumeration and', '备份、临时文件、原子替换': 'Backups, temporary files, and atomic replacement', '在线人数计数与保护表读取': 'Online-count and protection-table reads',
    '监听器、命令、Tab 补全': 'Listeners, commands, and tab completion', '建立离线文件保护': 'Establish offline-file protection', '玩家、容器、离线窗口任务': 'Player, container, and offline-window tasks',
    '按 UUID 写入登录保护截止时间': 'Write a login-protection deadline for the UUID', '扫描器不会碰正在登录的文件。': 'The scanner will not touch a file during login.', 'UUID 加入': 'Add the UUID to', '在线期间持续保护。': 'Protect it while online.', '移出在线集，保留退出缓冲时间': 'Remove it from the online set and keep a logout buffer', '等待服务端完成保存。': 'Wait for the server to finish saving.', '同一 UUID 不会并发写入。': 'The same UUID is never written concurrently.', '文件系统支持时原子提交，否则替换移动。': 'Commit atomically when supported; otherwise use a replacement move.',
    '玩家拾取': 'Player pickup', '取消拾取、删除物品实体、发送原因。': 'Cancel pickup, remove the item entity, and send the reason.', '点击槽位或光标': 'Slot or cursor click', '清空违规项并取消事件。': 'Clear the illegal item and cancel the event.', '创造栏生成物品': 'Creative item creation', '清空 cursor 并取消事件。': 'Clear the cursor and cancel the event.', '使用和转移物品': 'Item use and transfer', '移除对应槽位或实体。': 'Remove the corresponding slot or entity.', '全库存检查点': 'Full-inventory checkpoints', '递归扫描背包、末影箱、cursor。': 'Recursively scan inventory, ender chest, and cursor.',
    '已卸载引用立即从集合移除。': 'Remove unloaded references immediately.', '只处理实现': 'Only process block states implementing', '的方块状态。': 'as an inventory holder.', '要求': 'Requires', '跳过玩家。': 'Skip players.', '使用': 'Use', '支持双箱等共享库存对象。': 'Support shared inventories such as double chests.', '同步移除、通知并累加统计。': 'Remove, notify, and update statistics synchronously.', '不触发区块加载': 'Do not trigger chunk loading',
    '异步，每 60 秒，核对日期与人数': 'Async; every 60 seconds, check date and player count', 'OP UUID、世界名、playerdata 路径': 'Operator UUIDs, world names, and playerdata paths', '枚举、锁定 UUID、解析和判定': 'Enumerate, lock UUIDs, parse, and check', '备份、临时文件、原子替换、状态': 'Backups, temporary files, atomic replacement, and state',
    '并发保护协议': 'Concurrency protection protocol', '阶段': 'Stage', '保护': 'Protection', '结果': 'Result', '等待服务端完成保存。': 'Wait for the server to finish saving.',
    '现代 data components 与旧版 tag 结构': 'modern data components and legacy tag structures', '主入口只修改': 'The main entry modifies only', '两个 Compound 列表。': 'the two Compound lists.', '数量': 'Count', '附魔': 'Enchantments', '嵌套容器': 'Nested containers', '不可破坏': 'Unbreakable',
    '未知附魔的保守上限': 'Conservative limit for unknown enchantments', '规则不是修复器': 'Rules are not a repairer', '第一条违规原因决定处置日志。': 'The first violation determines the remediation log.',
    '按 modifier amount 绝对值比较。': 'Compare the absolute modifier amount.', '配置启用时，存在对应标记即判违。': 'When enabled, the corresponding marker is a violation.', '标准化为不含命名空间的小写 ID。': 'Normalize to a lowercase ID without a namespace.',
    '生命周期、任务编排、规则快照、玩家数据保护': 'Lifecycle, task orchestration, rule snapshots, and player-data protection', 'bStats状态上报功能类': 'bStats metrics reporting', '在线 ItemStack/Inventory 判定、递归与通知': 'Online ItemStack/Inventory checks, recursion, and notifications', '玩家事件和登录/退出保护': 'Player events and login/logout protection', '库存交互拦截与开关库存检查': 'Inventory interaction interception and inventory checks', '已加载区块索引和主线程批次': 'Loaded-chunk index and main-thread batches', '窗口、队列、锁、文件事务和统计': 'Windows, queues, locks, file transactions, and metrics', '现代/旧版 NBT 格式判定与列表清理': 'Modern/legacy NBT checks and list cleanup', '管理命令、状态与补全': 'Admin commands, status, and completion',
    'Maven 默认解析 Leaves 1.21.10 快照 API，': 'Maven resolves the Leaves 1.21.10 snapshot API by default, ', '双版本构建': 'Compatibility builds', '由服务器提供，不打入 JAR。': 'provided by the server and not bundled in the JAR.', 'shade 到 JAR，并重定位为': 'shaded into the JAR and relocated to', '仓库使用': 'The repository uses', 'API 是快照版本，CI 应缓存 Maven 仓库但仍定期刷新，并在刷新后重新跑双版本编译。': 'The API is a snapshot; CI should cache Maven dependencies, refresh them periodically, and rerun compatibility builds after refresh.',
    '增加一种在线判定': 'Add an online check', '把阈值读入': 'Read thresholds into', '的构造快照，不要在每件物品上反复查 YAML。': 'constructor snapshots instead of reading YAML for every item.', '返回可审计的中文原因': 'return an auditable reason', '若离线也应生效': 'if it should also apply offline', '增加等价格式解析和合成 NBT 测试。': 'add equivalent-format parsing and synthetic NBT tests.',
    '批次必须有上限与配置开关，不能一次遍历全服所有对象。': 'Batches need limits and a configuration switch; never traverse every server object at once.', '复用': 'Reuse', '保持通知和统计一致。': 'to keep notifications and metrics consistent.', '任何异常都必须清理临时文件，且不能推进成功日期。': 'Every exception must clean up temporary files and must not advance the success date.', '不得异步调用玩家、世界、注册表或插件管理器的活状态。': 'Never access live players, worlds, registries, or the plugin manager asynchronously.',
    'JAR 内': 'Inside the JAR,', '的 name、version、main 与 Maven 坐标一致。': 'name, version, and main match the Maven coordinates.', '默认': 'The default', '能被 SnakeYAML 解析，且关键扫描器默认开启。': 'can be parsed by SnakeYAML and the key scanners are enabled by default.', '合成 NBT 覆盖材料黑名单、32K 附魔、堆叠、属性、现代/旧版嵌套容器。': 'Synthetic NBT tests cover material blacklists, extreme enchantments, stack size, attributes, and modern/legacy nested containers.', '发布产物选择': 'Release artifact choice',
    'Leaves 1.21.10 / 1.21.11': 'Leaves 1.21.10 / 1.21.11', 'Java 21': 'Java 21', 'Maven': 'Maven', 'Querz NBT 6.1': 'Querz NBT 6.1',
    '把 JAR 放入': 'Put the JAR in', '启动一次服务器。': 'Start the server once.', '首次部署建议启用': 'For the first deployment, enable', '确认日志后再写回。': 'and write changes only after reviewing the logs.',
    '在一次离线全量扫描成功结束后生成，用于记录最近完成日期，避免同一天重复遍历。': 'is created after a successful full offline scan to record the latest completion date and prevent duplicate scans on the same day.',
    '新配置包含 2.0.0以及更高版本 的全部字段和逐行中文注释。': 'The new configuration contains all fields from version 2.0.0 and later, with line-by-line comments.', '迁移': 'Migrate', '附魔/属性阈值、白名单和自定义消息。': 'enchantment and attribute limits, allowlists, and custom messages.', '不要直接用旧配置覆盖配置版本 3。': 'Do not overwrite config version 3 with the old configuration.',
    '离线扫描、已加载容器、低峰窗口和文件保护均是新版字段。': 'Offline scanning, loaded containers, off-peak windows, and file protection are new fields.', '直接覆盖会让缺失项回落到代码默认值，但你会失去完整注释，也不便于审计实际行为。': 'A direct replacement falls back to code defaults for missing fields and removes the complete comments needed to audit behavior.',
    '在线与容器扫描仍会正常移除。': 'Online and container scans will still remove items normally.', '重点检查不可破坏物品、玩家头颅、自定义属性装备和插件附魔。': 'Pay special attention to unbreakable items, player heads, custom-attribute equipment, and plugin enchantments.', '合法运营道具应从禁止材料中删除或放宽对应检测项，不要长期给普通玩家绕过权限。': 'Remove legitimate operational items from the banned-material list or relax the relevant check; do not grant bypass permission to ordinary players permanently.', '保持': 'Keep', '把': 'set', '否则大量历史违规可能显著增加日志量。': 'otherwise a large number of historical violations may greatly increase log volume.',
    '默认每 100 tick 扫描玩家库存。': 'Scan player inventories every 100 ticks by default.', '默认每 200 tick 处理 16 个区块。': 'Process 16 chunks every 200 ticks by default.', '按窗口和在线人数处理 playerdata。': 'Process playerdata according to the time window and online count.',
    '背包、快捷栏、盔甲、副手、光标、末影箱': 'Inventory, hotbar, armor, off-hand, cursor, and ender chest', '绕过权限、OP、UUID 白名单不处理': 'Bypass permission, operators, and UUID allowlists are skipped', '箱子、木桶、漏斗、熔炉、方块潜影盒等': 'Chests, barrels, hoppers, furnaces, block shulker boxes, and more', '不会为扫描主动加载区块': 'Never loads chunks for scanning', '运输矿车、漏斗矿车等 InventoryHolder': 'Storage minecarts, hopper minecarts, and other InventoryHolders', '玩家实体由在线扫描器负责': 'Player entities are handled by the online scanner', '各世界标准 UUID': 'Standard UUID files in each world', '的背包、末影箱、嵌套物品': 'inventory, ender-chest, and nested-item data', '在线、登录中、刚退出、OP 和白名单玩家跳过': 'Online, logging-in, recently logged-out, operator, and allowlisted players are skipped',
    '5 秒一次；人数多可调到 200。': 'Every 5 seconds; increase to 200 on busy servers.', '批次间隔 10 秒。': 'Use a 10-second batch interval.', '卡顿时先降到 4 或 8。': 'Reduce to 4 or 8 if the server stutters.', '通常无需超过 3，最大建议 5。': 'Usually no more than 3 is needed; 5 is the recommended maximum.', '机械盘可降到 2；观察磁盘延迟再增加。': 'Use 2 on a hard disk; increase after checking disk latency.', '每批间隔 1 秒；越大越平缓。': 'One second between batches; larger values are smoother.',
    '直接禁止材料': 'Directly banned materials', '刷怪笼等合法玩法。': 'spawners and other legitimate gameplay.', '禁止不可破坏标记': 'Ban the unbreakable marker', '任务物品、菜单物品、特殊工具。': 'Quest items, menu items, and special tools.', '覆盖单个附魔上限': 'Override one enchantment limit', '自定义附魔或刻意放宽的原版附魔。': 'Custom enchantments or intentionally relaxed vanilla enchantments.', '按绝对值限制七类属性': 'Limit seven attribute types by absolute value', 'RPG 装备、负属性平衡道具。': 'RPG equipment and items balanced with negative attributes.', '按 UUID 完全跳过': 'Skip completely by UUID', '只适合受控的系统账号。': 'Only suitable for controlled system accounts.',
    '重载会重新读取规则快照、取消旧任务并按新设置启动': 'Reload rereads the rule snapshot, cancels old tasks, and starts with the new settings', '修改后执行': 'After changing settings, run', '用于判断任务是否启用/运行。': 'to check whether tasks are enabled and running.',
    '登录/退出保护、UUID 锁、备份、临时文件原子替换': 'Login/logout protection, UUID locks, backups, and atomic temporary-file replacement', '同一玩家下次被修改会覆盖该备份，因此它不是长期版本库。': 'A later modification for the same player overwrites this backup, so it is not a long-term version history.', '确保服务器不会同时保存该玩家数据。': 'Ensure the server is not saving that player data at the same time.', '从日志中的世界名和 UUID 找到对应': 'Use the world name and UUID in the log to locate the corresponding', '另行复制，以便反向恢复。': 'to a separate location for rollback.', '替换': 'replace', '若不调整误报规则，下一次低峰扫描仍会再次移除。': 'If false-positive rules are not adjusted, the next off-peak scan will remove it again.',
    '每 10 秒出现主线程尖峰': 'Main-thread spikes every 10 seconds', '在线人数多时定时扫描压力大': 'Scheduled scans are heavy with many online players', '低峰磁盘延迟升高': 'Disk latency rises during off-peak scans', '嵌套恶意物品处理慢': 'Nested malicious items process slowly', '通知刷屏': 'Notification spam', '完成一轮已加载区块扫描会更慢。': 'A full loaded-chunk scan will take longer.', '事件检测仍在，但静止库存发现延迟增加。': 'Event detection continues, but finding unchanged inventory items is delayed.', '可能无法在窗口内扫完全部历史玩家。': 'The full history may not finish within the window.', '更深层嵌套不再递归。': 'Deeper nesting is no longer recursive.', '检测和删除仍继续，仅减少通知。': 'Detection and removal continue; only notifications are reduced.',
    '插件未加载或显示红色': 'The plugin does not load or appears red', '离线扫描没有启动': 'Offline scanning did not start', '合法物品被删除': 'A legitimate item was removed', '配置重载后没有生效': 'Configuration changes did not take effect after reload', '从控制台第一段异常开始检查，不要只看最后一行。': 'Start with the first exception in the console rather than only reading the last line.', '核对': 'Check', '确认': 'Confirm', '从日志读取材料、路径和原因，定位对应规则。': 'Read the material, path, and reason from the log to locate the rule.', '运营头颅玩法时从': 'For player-head gameplay, remove', '或重新设计物品。': 'or redesign the item.', '明确设置允许值。': 'set the permitted value explicitly.', '先检查 YAML 缩进和控制台解析错误，列表只能保留一种写法。': 'Check YAML indentation and console parsing errors first; use only one list syntax.', '验证开关与任务状态。': 'to verify switches and task status.',
    '已备份全部世界与旧插件数据目录。': 'All worlds and the old plugin data directory are backed up.', '旧 FoliaAntiIllegal JAR 已移除，没有重复加载。': 'The old FoliaAntiIllegal JAR is removed and is not loaded twice.', '已从新配置出发逐项合并规则，而非覆盖整个文件。': 'Rules were merged into the new configuration individually instead of replacing the whole file.', '已检查': 'Checked', '完成一次演练。': 'completed one dry run.', '低峰窗口、时区、在线人数阈值和磁盘批次符合本服情况。': 'The off-peak window, time zone, online threshold, and disk batch size fit this server.', '保持开启，并验证过单玩家恢复流程。': 'remains enabled, and single-player recovery was verified.', '权限只授予需要的管理组，没有给普通玩家': 'Permissions are granted only to the required admin group; ordinary players do not have', '已执行': 'Ran', '和一次指定玩家扫描。': 'and one scan for a specified player.',
    '为了统计此插件的使用数据（包括使用次数、用户服务器的地理位置（精确到国家）等非细节信息，我们会将数据推送至bStats平台）': 'To measure plugin usage, including usage count and the user server country, we send non-sensitive statistics to bStats.', '请放心，此功能仅仅统计插件的使用次数，和您的服务器所在国家，不会上传其他任何数据': 'This only measures plugin usage and the country of your server; no other data is uploaded.',
    '代码以 Leaves 1.21.10 API 为发布基线': 'The code uses the Leaves 1.21.10 API as its release baseline', '默认 API 1.21.10，profile 覆盖到 1.21.11': 'API 1.21.10 by default, with a profile for 1.21.11', 'Bukkit 世界状态只在主线程访问': 'Bukkit world state is accessed only on the main thread', '文件 I/O 不阻塞主线程': 'File I/O does not block the main thread', '异步枚举、解析、备份、写回': 'Async enumeration, parsing, backup, and write-back', '与旧 Folia 项目隔离': 'Kept separate from the old Folia project',
    'NbtItemChecker': 'NbtItemChecker', '是离线格式适配器。它与': 'is the offline format adapter. It reads the same configuration as', '读取同一份配置，但不在异步线程调用 Bukkit 实体/世界 API；构造时在主线程把材料与附魔注册表快照成普通集合。': 'but never calls Bukkit entity/world APIs asynchronously; it snapshots materials and enchantment registries into ordinary collections on the main thread.', '区块和实体生命周期归服务端主线程。': 'Chunk and entity lifecycles belong to the server main thread.', '避免在异步任务访问 Bukkit 全局状态。': 'Avoid accessing global Bukkit state from async tasks.', '文件系统和压缩 NBT 可能阻塞。': 'File systems and compressed NBT may block.', '按玩家 UUID 串行，避开主线程。': 'Serialized by player UUID away from the main thread.', '使用并发集合、volatile 快照与原子类型。': 'Uses concurrent collections, volatile snapshots, and atomic types.',
    '本版的世界、实体和库存操作按 Leaves/Bukkit 单主线程约束实现。若以后重新切换 Folia，需要恢复区域与实体调度设计，不能只修改': 'This release uses the Leaves/Bukkit single-main-thread contract for world, entity, and inventory operations. Supporting Folia again requires region and entity scheduling; changing only',
    '至少保留所有世界目录和现有': 'Keep all world directories and the existing', '将': 'Move', '放到服务器': 'to the server', '等待生成': 'Wait for', '把离线扫描的': 'Set the offline scanner', '改成': 'to', '重载后观察一个低峰窗口。': 'then reload and observe one off-peak window.',
    '新配置包含 2.0.0以及更高版本 的全部字段和逐行中文注释。': 'The new configuration includes all fields from 2.0.0 and later, with line-by-line comments.', '附魔/属性阈值、白名单和自定义消息。': 'enchantment and attribute limits, allowlists, and custom messages.', '不要直接用旧配置覆盖配置版本 3。': 'Do not overwrite config version 3 with the old configuration.',
    '确认后将': 'After verification, set', '恢复为': 'back to', '否则大量历史违规可能显著增加日志量。': 'otherwise historical violations may greatly increase log volume.', '按窗口和在线人数处理 playerdata。': 'Process playerdata by window and online player count.', '标准 UUID': 'a standard UUID',
    '重载会重新读取规则快照、取消旧任务并按新设置启动': 'Reload rereads the rule snapshot, cancels old tasks, and starts with the new settings', '用于判断任务是否启用/运行。': 'to check whether the tasks are enabled or running.', '确保服务器不会同时保存该玩家数据。': 'Ensure the server is not saving this player data at the same time.', '从日志中的世界名和 UUID 找到对应': 'Use the world name and UUID in the log to locate the corresponding', '用': 'Use', '替换': 'to replace', '在线替换': 'Replacing while online',
    '事件检测仍在，但静止库存发现延迟增加。': 'Event detection continues, but finding unchanged inventory items is delayed.', '确认服务端是 Leaves 1.21.10 / 1.21.11，并使用 Java 21。': 'Confirm that the server is Leaves 1.21.10 / 1.21.11 and runs Java 21.', '确认 JAR 名为': 'Confirm that the JAR is named', '没有同时保留旧版 JAR。': 'and that the old JAR is not also present.', '核对': 'Check', '窗口起止、在线人数阈值和最近成功日期。': 'the window times, online-player limit, and last successful date.', '且文件名是标准 UUID。': 'and that the filenames are standard UUIDs.', '从日志读取材料、路径和原因，定位对应规则。': 'Read the material, path, and reason from the log to locate the rule.', '移除': 'remove', '任务物品使用不可破坏标记时关闭': 'If a quest item uses an unbreakable marker, disable', '或重新设计物品。': 'or redesign the item.', '自定义附魔使用': 'For custom enchantments, use', '明确设置允许值。': 'set the permitted value explicitly.', '确认命令返回“配置与全部扫描器已重新加载”。': 'Confirm that the command reports “configuration and all scanners reloaded”.', '验证开关与任务状态。': 'to verify switches and task status.',
    '服务器为 Leaves 1.21.10 / 1.21.11，运行 Java 21。': 'The server is Leaves 1.21.10 / 1.21.11 and runs Java 21.', '离线扫描已用': 'Offline scanning has used', '完成一次演练。': 'to complete one dry run.', '低峰窗口、时区、在线人数阈值和磁盘批次符合本服情况。': 'The off-peak window, time zone, online limit, and disk batch size fit this server.', '保持开启，并验证过单玩家恢复流程。': 'remains enabled, and single-player recovery has been verified.', '和一次指定玩家扫描。': 'and one scan for a specified player.',
    '移除、通知、日志、备份、统计': 'Removal, notifications, logs, backups, and metrics', '读取配置': 'Load configuration', '监听器、命令、Tab 补全': 'Listeners, commands, and tab completion', '建立离线文件保护': 'Establish offline-file protection', '玩家、容器、离线窗口任务': 'Player, container, and offline-window tasks', '异步，每 60 秒，核对日期与人数': 'Async, every 60 seconds; check the date and player count', 'OP UUID、世界名、playerdata 路径': 'Operator UUIDs, world names, and playerdata paths', '枚举、锁定 UUID、解析和判定': 'Enumerate, lock UUIDs, parse, and check', '备份、临时文件、原子替换、状态': 'Backups, temporary files, atomic replacement, and state',
    '扫描器不会碰正在登录的文件。': 'The scanner does not touch files for players who are logging in.', '在线期间持续保护。': 'Protection continues while online.', '移出在线集，保留退出缓冲时间': 'Remove from the online set and keep a logout buffer', '等待服务端完成保存。': 'Wait for the server to finish saving.', '按 UUID 加锁，再次检查保护': 'Lock by UUID and check protection again', '文件系统支持时原子提交，否则替换移动。': 'Commit atomically when supported; otherwise move as a replacement.',
    '离线线程不能动态查询注册表，启动时已注册附魔会快照真实上限；未知 ID 使用': 'The offline thread cannot query the registry dynamically. Registered enchantments are snapshotted at startup; unknown IDs use', '默认 10。新增自定义附魔集成时应把允许值写入': 'with a default of 10. Integrations should put allowed values in', '标准化为不含命名空间的小写 ID。': 'Normalize to a lowercase ID without a namespace.', '自定义上限优先，其次注册表最大等级乘倍率。': 'Use the custom limit first, then the registry maximum multiplied by the multiplier.', '拒绝负 damage 与超过材料最大耐久 + 10 的值。': 'Reject negative damage and values above the material maximum durability plus 10.', '拒绝负数和超过材料原版最大堆叠的数量。': 'Reject negative counts and counts above the material maximum stack size.', 'Leaves 1.21.10 的属性枚举已去除旧': 'Leaves 1.21.10 removed the old', '常量依赖。在线检查通过 namespaced key 的 key 部分映射阈值，同时接受': 'constant dependency. Online checks map limits using the key part of namespaced keys and also accept', '等旧键形式。': 'and similar legacy key forms.',
    '默认 API 1.21.10，profile 覆盖到 1.21.11': 'API 1.21.10 by default, with the profile covering 1.21.11', '与旧 Folia 项目隔离': 'Separated from the old Folia project', '维度': 'Dimension', '运行与编译均为 21': 'Runtime and compilation both use 21', 'Bukkit 世界状态只在主线程访问': 'Bukkit world state is accessed only on the main thread', '文件 I/O 不阻塞主线程': 'File I/O does not block the main thread', '异步枚举、解析、备份、写回': 'Async enumeration, parsing, backup, and write-back',
    '新增逻辑若需要玩家名、世界对象或 Bukkit 注册表，必须在': 'If new logic needs player names, world objects, or the Bukkit registry, collect an immutable snapshot in', '中先采集不可变快照。': 'first.', '若从非主线程进入，会先调度回主线程。真正重载按“取消旧任务 → reloadConfig → 重建规则与保护时长 → 重启扫描器”顺序执行。': 'If entered from a non-main thread, it is scheduled back to the main thread. Reload order is cancel old tasks, reloadConfig, rebuild rules and protection durations, then restart scanners.', '取消全部 BukkitTask、注销容器监听器并清空正在等待的离线文件。': 'Cancel all BukkitTasks, unregister the container listener, and clear pending offline files.', '通过 volatile 引用整体替换，事件处理始终看见完整快照。': 'Replace the snapshot through a volatile reference so event handling always sees a complete snapshot.', '复位，正在处理的单文件依靠 finally 清理临时文件。': 'reset; the file currently being processed cleans up temporary files in finally.',
    '同时接受现代 data components 与旧版 tag 结构，主入口只修改': 'Accept both modern data components and legacy tag structures; the main entry modifies only', '两个 Compound 列表。': 'the two Compound lists.', '第一条违规原因决定处置日志。': 'The first violation determines the remediation log.', '自定义上限优先，其次注册表最大等级乘倍率。': 'Use the custom limit first, then the registry maximum multiplied by the multiplier.',
    '默认构建通过 Leaves 1.21.10 API 编译。': 'The default build compiles against the Leaves 1.21.10 API.', 'profile 通过 1.21.11 API 编译。': 'The profile compiles against the 1.21.11 API.', '在隔离的 Leaves 测试服完成启动、重载、玩家扫描、容器扫描和离线备份恢复。': 'Complete startup, reload, player scanning, container scanning, and offline backup recovery on an isolated Leaves test server.', '推荐发布 1.21.10 基线产物，因为它使用两个目标中的较低 API 编译；1.21.11 profile 是 API 演进检查。若未来代码必须调用仅 1.21.11 存在的 API，应拆分适配层或单独发布版本。': 'Publish the 1.21.10 baseline artifact because it uses the lower of the two target APIs; the 1.21.11 profile checks API evolution. If code later needs an API available only in 1.21.11, split the adapter layer or publish a separate version.'
  });

  Object.assign(translations['zh-CN'], {
    'Leaves 官方文档': 'Official Leaves documentation', 'Leaves 官方仓库': 'Official Leaves repository',
    '02 / 安装': '02 / Installation', '04 / 首次运行': '04 / First run', '设置': 'Setting', '改为': 'set to',
    '首次演练配置': 'First dry-run configuration', '05 / 工作范围': '05 / Scan coverage', '拾取、点击、拖拽、换手与加入。': 'Pickup, clicks, drags, hand swaps, and joins.',
    '已加载区块': 'Loaded chunks', '离线低峰': 'Off-peak offline', '只检测和记录，不创建备份、不写回原文件。': 'Detects and records only; it creates no backup and does not write to the original file.',
    '08 / 管理入口': '08 / Management entry points', '命令': 'Command', '重载配置并重启扫描器': 'Reload configuration and restart scanners', '控制台或有管理权限的玩家可用。': 'Available from the console or to players with the management permission.',
    '立即扫描全部在线玩家': 'Scan all online players immediately', '返回玩家数与移除总数。': 'Returns the player count and total removals.', '/antiillegal scan &lt;玩家&gt;': '/antiillegal scan &lt;player&gt;', '/antiillegal scan <玩家>': '/antiillegal scan <player>',
    '扫描指定在线玩家': 'Scan a specified online player', '目标必须在线。': 'The target must be online.', '检查执行者主手物品': 'Check the executor\'s main-hand item', '仅玩家可用，不删除。': 'Players only; does not remove items.',
    '查看容器与离线扫描统计': 'View container and offline scan statistics', '使用所有管理命令。': 'Use all management commands.', '接收在线玩家与容器违禁通知。': 'Receive illegal-item notifications for online players and containers.',
    '在线扫描完全绕过；请谨慎授予。': 'Completely bypasses online scanning; grant it carefully.', '命令别名为': 'Command aliases are', '和': 'and', '09 / 数据安全': '09 / Data safety',
    '默认设置下，离线扫描仅在确实发现违禁物且准备写回时，把原文件复制为': 'By default, offline scanning copies the original file to', '停止服务器': 'Stop the server', '确认玩家 UUID 与世界': 'Confirm the player UUID and world',
    '保留当前文件': 'Keep the current file', '把当前': 'Set the current', '恢复备份': 'Restore the backup', '先修正规则': 'Correct the rules first', '10 / 性能': '10 / Performance',
    '降低': 'Reduce', '，再提高容器': ', then increase container', '提高在线': 'Increase the online interval', '到 200 或 400': 'to 200 or 400', '，提高': ', increase', '保持 2 至 3': 'Keep 2 to 3',
    '关闭': 'Disable', '或': 'or', '先减小批次，再拉长间隔': 'Reduce the batch size first, then lengthen the interval', '11 / 排障': '11 / Troubleshooting',
    '确认扫描器已启用且当前未在运行。': 'Confirm that scanners are enabled and are not currently running.', '检查各世界是否存在': 'Check whether each world contains', '12 / 交付': '12 / Delivery',
    '、不可破坏物品、自定义附魔和 RPG 属性。': ', unbreakable items, custom enchantments, and RPG attributes.', 'Leaves 文档': 'Leaves documentation',
    '移除 Folia 专用 scheduler 后，库存与区块访问采用单主线程假设。若要重新支持 Folia，应抽象调度后端并为每个实体/区块恢复所有权调度，不能把': 'After removing the Folia-specific scheduler, inventory and chunk access use a single-main-thread model. Supporting Folia again requires an abstract scheduler backend and ownership scheduling for every entity and chunk; adding',
    '加回描述文件就结束。': 'back to the descriptor is not sufficient.', '玩家 Inventory / EnderChest / Cursor': 'Player inventory / ender chest / cursor', '解析': 'Parsing', '异步边界不可外扩': 'The async boundary must not expand',
    '的异步批次只能操作路径、普通值对象、规则快照和 NBT。新增逻辑若需要玩家名、世界对象或 Bukkit 注册表，必须在': 'async batches may operate only on paths, plain value objects, rule snapshots, and NBT. If new logic needs player names, world objects, or the Bukkit registry, collect an immutable snapshot in',
    '离线扫描器停止后会把': 'After the offline scanner stops, it will', '与': 'and', '提供低延迟阻断，周期任务提供最终一致性。两者都委托给同一个': 'provide low-latency blocking and eventual consistency through periodic tasks. Both delegate to the same', '，因此不会出现两套判定标准。': ', so there are no two separate rule sets.', '的顺序是': 'The order is',
    '、OP、UUID 白名单。容器扫描不绑定玩家，因此不会应用玩家 bypass；离线扫描显式排除 OP 和 UUID 白名单。': ', operators, and UUID allowlists. Container scans are not tied to a player, so player bypass does not apply; offline scans explicitly exclude operators and UUID allowlists.',
    '在启动时快照所有已加载区块，此后通过': 'Snapshots all loaded chunks at startup, then uses', '维护': 'maintains', '集合。每个周期从游标位置抽取最多': 'the set. Each cycle takes at most', '个区块。': ' chunks from the cursor.',
    '检查区块仍加载': 'Check that the chunk is still loaded', '读取 tile entities': 'Read tile entities', '读取已加载实体': 'Read loaded entities', '按对象身份去重': 'Deduplicate by object identity', '委托 ItemChecker': 'Delegate to ItemChecker',
    '扫描器只从已维护集合中取引用，并在访问前调用': 'The scanner takes references only from the maintained set and calls', '。不要把它改成遍历全世界坐标或直接调用可能强制加载的 API。': '. Do not change it to iterate world coordinates or call an API that may force-load chunks.',
    'UUID 写入登录保护截止时间': 'Write the login protection deadline for the UUID', 'Quit / 登录失败': 'Quit / login failure', '单文件处理': 'Single-file processing', '写回': 'Write-back', '同目录临时文件 +': 'Same-directory temporary file +',
    '只有完整扫完队列才调用': 'Call only after the queue is fully scanned', '。窗口结束、禁用插件或枚举异常都以失败收尾，不推进运行日期。': '. Window expiry, plugin disablement, or enumeration errors finish as failures and do not advance the run date.', '存在': 'Exists',
    '在线与离线适配器遵循相同的早返回顺序，第一条违规原因决定处置日志。规则不是修复器：一旦判违，整个物品栈从所在列表移除。': 'Online and offline adapters use the same early-return order. The first violation determines the remediation log. Rules do not repair items: once an item is illegal, the entire stack is removed from its list.',
    '七类属性按 modifier amount 绝对值比较。': 'The seven attribute types are compared by the absolute modifier amount.', '文件': 'File', '全部模块': 'All modules', '主类': 'Main class', '监听器、两个扫描器': 'Listeners and two scanners', '主类、ItemChecker': 'Main class and ItemChecker', 'NbtItemChecker、主类': 'NbtItemChecker and main class', '主类、扫描器': 'Main class and scanners',
    'profile 只覆盖 API 版本和输出目录，确保同一份源码接受两个 API 编译器检查。': 'The profile changes only the API version and output directory, ensuring the same source passes both API compiler checks.',
    '依赖': 'Dependency', '范围': 'Scope', '在': 'in', '返回可审计的中文原因，不要直接在规则函数中删除物品。': 'Return an auditable reason; do not delete items directly inside the rule function.', '若离线也应生效，在': 'If it should also apply offline, add it to',
    'Bukkit 活对象必须在主线程获取和修改。': 'Live Bukkit objects must be obtained and modified on the main thread.', '保留“按 UUID 加锁 → 再检查保护 → 备份 → 临时文件 → 原子替换”的顺序。': 'Keep the order: lock by UUID, recheck protection, back up, write a temporary file, and replace atomically.',
    '根据bStats提供的指示，添加了类': 'Following the bStats instructions, add the class', '（主类）的': 'in the main class', '添加上报相关代码': 'and add the reporting code', 'JAR 不包含 Leaves API，但包含已重定位的 Querz NBT 类。': 'The JAR does not include the Leaves API, but it includes relocated Querz NBT classes.',
    '# 发布基线\nmvn clean package\n# => target/LeavesAntiIllegal-3.1.0.jar\n\n# 1.21.11 兼容编译\nmvn -Pleaves-1.21.11 clean package\n# => target/LeavesAntiIllegal-3.1.0.jar': '# Release baseline\nmvn clean package\n# => target/LeavesAntiIllegal-3.1.0.jar\n\n# 1.21.11 compatibility build\nmvn -Pleaves-1.21.11 clean package\n# => target/LeavesAntiIllegal-3.1.0.jar',
    'LeavesAntiIllegal 3.1.0 服主安装、配置、迁移与恢复手册': 'LeavesAntiIllegal 3.1.0 server installation, configuration, migration, and recovery guide',
    '配置、备份、命令…': 'Configuration, backups, commands...', '文档目录': 'Documentation contents', '物品扫描覆盖流程': 'Item scanning coverage flow',
    'LeavesAntiIllegal 3.1.0 架构、线程、构建与扩展文档': 'LeavesAntiIllegal 3.1.0 architecture, threading, build, and extension guide', '线程、NBT、构建…': 'Threading, NBT, builds...',
    '插件数据流': 'Plugin data flow', '插件启动流程': 'Plugin startup flow', '离线文件扫描流程': 'Offline file scanning flow'
  });

  Object.assign(translations['zh-CN'], {
    '面向 Minecraft 1.20 至 26.2 Bukkit API 服务端的违禁物品扫描插件，覆盖在线玩家、已加载容器与离线玩家数据。': 'An illegal-item scanner for Minecraft 1.20 through 26.2 Bukkit API servers, covering online players, loaded containers, and offline player data.',
    '使用 Java 17 或更高版本启动 1.20 至 26.2 的 Bukkit API 服务端。': 'Run a Bukkit API server from 1.20 through 26.2 with Java 17 or newer.',
    '需要指定 API 基线？': 'Need a specific API baseline?',
    '用于部署、配置和维护 Minecraft 1.20 至 26.2 Bukkit API 服务端上的在线玩家、已加载容器与离线玩家数据扫描。': 'Deploy, configure, and maintain online-player, loaded-container, and offline-player scanning on Minecraft 1.20 through 26.2 Bukkit API servers.',
    '此版本以标准 Bukkit 主线程模型为目标，不再使用 Folia 区域调度器。插件以 Bukkit API 1.20.1 编译，可运行于 1.20 至 26.2 的 Bukkit API 兼容服务端，服务器需要 Java 17 或更高版本。': 'This version targets the standard Bukkit main-thread model and no longer uses Folia region schedulers. It is compiled against Bukkit API 1.20.1 and runs on compatible Bukkit API servers from 1.20 through 26.2. The server must use Java 17 or newer.',
    '使用 Java 17 或更高版本，核心版本为 Minecraft 1.20 至 26.2 的 Bukkit API 兼容服务端。': 'Use Java 17 or newer with a Minecraft 1.20 through 26.2 server compatible with the Bukkit API.',
    '确认服务端版本在 1.20 至 26.2 范围内，并使用 Java 17 或更高版本。': 'Confirm that the server version is within 1.20 through 26.2 and uses Java 17 or newer.',
    '服务器版本为 1.20 至 26.2，运行 Java 17 或更高版本。': 'The server version is 1.20 through 26.2 and runs Java 17 or newer.',
    '代码以 Spigot Bukkit API 1.20.1 为发布基线。线上产物不调用任何核心私有实现，只依赖 Bukkit/Paper 公开类型和 Bukkit 调度器，因此可运行于 1.20 至 26.2 的兼容服务端。': 'The release baseline is Spigot Bukkit API 1.20.1. The artifact calls no server-private implementation and uses only public Bukkit/Paper types and the Bukkit scheduler, so it runs on compatible servers from 1.20 through 26.2.',
    '运行与编译均为 17+': 'Runtime and compilation use Java 17+', '以 Spigot API 1.20.1 编译，向前兼容公共 API': 'Compiled against Spigot API 1.20.1 with forward compatibility through the public API',
    '属性检查不依赖核心私有实现，通过 Bukkit 属性键映射阈值，同时接受': 'Attribute checks do not depend on server-private implementations. Limits are mapped through Bukkit attribute keys and also accept',
    'Maven 默认解析 Spigot Bukkit API 1.20.1，使用 Java 17 编译。运行时由 1.20 至 26.2 的兼容服务端提供 Bukkit API，不将服务端 API 打包进插件。': 'Maven resolves Spigot Bukkit API 1.20.1 by default and compiles with Java 17. Compatible servers from 1.20 through 26.2 provide Bukkit at runtime; server APIs are not bundled.',
    '# 发布基线\nmvn clean package\n# => plugin/target/LeavesAntiIllegal-3.1.0.jar\n\n# 兼容 1.20 - 26.2 的统一产物\nmvn clean package\n# => plugin/target/LeavesAntiIllegal-3.1.0.jar': '# Release baseline\nmvn clean package\n# => plugin/target/LeavesAntiIllegal-3.1.0.jar\n\n# Unified artifact for 1.20 - 26.2\nmvn clean package\n# => plugin/target/LeavesAntiIllegal-3.1.0.jar',
    '默认构建通过 Spigot Bukkit API 1.20.1 编译。': 'The default build compiles against Spigot Bukkit API 1.20.1.',
    '同一份 Java 17 字节码面向 1.20 至 26.2 的 Bukkit API 兼容服务端运行。': 'The same Java 17 bytecode runs on Bukkit API-compatible servers from 1.20 through 26.2.',
    'JAR 不包含 Bukkit API，但包含已重定位的 Querz NBT 类和版本实现。': 'The JAR does not include the Bukkit API, but includes relocated Querz NBT classes and version implementations.',
    '在隔离的 1.20、1.21 和 26.2 测试服完成启动、重载、玩家扫描、容器扫描和离线备份恢复。': 'Complete startup, reload, player scanning, container scanning, and offline backup recovery on isolated 1.20, 1.21, and 26.2 test servers.',
    '发布以 Bukkit API 1.20.1 编译的统一产物。不要直接调用某个核心的私有类；若未来 Bukkit API 删除或改变公共方法，应通过反射适配或拆分版本适配层。': 'Publish the unified artifact compiled against Bukkit API 1.20.1. Do not call server-private classes directly; if Bukkit changes a public method, use reflection or split the version adapter.',
    '版本实现位于': 'Version implementations are located in', '多版本构建': 'Multi-version build',
    '构建使用 Spigot 官方快照仓库。版本实现位于 <code>versions/v1_20_1</code>、<code>versions/v1_21</code> 和 <code>versions/v26_2</code>，CI 应分别编译这些模块并重新组装统一 JAR。': 'The build uses the official Spigot snapshot repository. Version implementations are in <code>versions/v1_20_1</code>, <code>versions/v1_21</code>, and <code>versions/v26_2</code>; CI should compile these modules and assemble the unified JAR.',
    '构建使用 Spigot 官方快照仓库。版本实现位于': 'The build uses the official Spigot snapshot repository. Version implementations are in',
    '，CI 应分别编译这些模块并重新组装统一 JAR。': '; CI should compile these modules and assemble the unified JAR.'
  });

  Object.assign(translations['zh-CN'], {
    '文档中心 | LeavesAntiIllegal 3.1.0': 'Documentation | LeavesAntiIllegal 3.1.0',
    'LeavesAntiIllegal 3.1.0 · 文档中心': 'LeavesAntiIllegal 3.1.0 · Documentation',
    'LeavesAntiIllegal 3.1.0 服主安装、配置、迁移与恢复手册': 'LeavesAntiIllegal 3.1.0 server installation, configuration, migration, and recovery guide',
    '插件版本 3.1.0': 'Plugin version 3.1.0',
    '开发者文档 | LeavesAntiIllegal 3.1.0': 'Developer Guide | LeavesAntiIllegal 3.1.0',
    '开发者文档 · 3.1.0': 'Developer Guide · 3.1.0',
    'LeavesAntiIllegal 3.1.0 · 开发者文档': 'LeavesAntiIllegal 3.1.0 · Developer Guide',
    '# 发布基线\nmvn clean package\n# => plugin/target/LeavesAntiIllegal-3.1.0.jar\n\n# 兼容 1.20 - 26.2 的统一产物\nmvn clean package\n# => plugin/target/LeavesAntiIllegal-3.1.0.jar': '# Release baseline\nmvn clean package\n# => plugin/target/LeavesAntiIllegal-3.1.0.jar\n\n# Unified artifact for 1.20 - 26.2\nmvn clean package\n# => plugin/target/LeavesAntiIllegal-3.1.0.jar'
  });

  const translateText = (value, targetLanguage) => {
    if (targetLanguage === 'zh') return value;
    return Object.entries(translations['zh-CN'])
      .sort(([left], [right]) => right.length - left.length)
      .reduce((translated, [source, target]) => translated.replaceAll(source, target), value);
  };

  const replaceNodeText = (value) => {
    const entries = Object.entries(translations['zh-CN']).sort(([left], [right]) => right.length - left.length);
    return entries.reduce((translated, [source, target]) => translated.replaceAll(source, target), value);
  };

  const reportUntranslatedEnglishText = () => {
    const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    const nodes = [];
    while (walker.nextNode()) nodes.push(walker.currentNode);
    const untranslated = nodes.map((node) => node.nodeValue.trim())
      .filter((value) => value && /[\u3400-\u9fff]/.test(value));
    const attributes = [...document.querySelectorAll('input[placeholder], [aria-label], [title], meta[name="description"]')]
      .flatMap((element) => ['placeholder', 'aria-label', 'title', 'content']
        .filter((attribute) => element.hasAttribute(attribute))
        .map((attribute) => element.getAttribute(attribute)))
      .filter((value) => value && /[\u3400-\u9fff]/.test(value));
    const values = [...new Set([...untranslated, ...attributes])];
    if (values.length > 0) {
      console.warn('[LeavesAntiIllegal] Untranslated English-mode text:', values);
    }
  };

  const restoreChineseText = () => {
    const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    const nodes = [];
    while (walker.nextNode()) nodes.push(walker.currentNode);
    nodes.forEach((node) => {
      if (node.parentElement?.closest('script, style')) return;
      const original = originalText.get(node);
      if (original !== undefined) node.nodeValue = original;
    });
  };

  const applyLanguage = (targetLanguage) => {
    document.documentElement.lang = targetLanguage === 'en' ? 'en' : 'zh-CN';
    document.documentElement.classList.toggle('language-en', targetLanguage === 'en');
    const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    const nodes = [];
    while (walker.nextNode()) nodes.push(walker.currentNode);
    nodes.forEach((node) => {
      if (!node.nodeValue.trim() || node.parentElement?.closest('script, style')) return;
      const original = originalText.get(node) ?? node.nodeValue;
      originalText.set(node, original);
      node.nodeValue = targetLanguage === 'zh' ? original : replaceNodeText(original);
    });
    document.querySelectorAll('input[placeholder], [aria-label], [title]').forEach((element) => {
      ['placeholder', 'aria-label', 'title'].forEach((attribute) => {
        if (!element.hasAttribute(attribute)) return;
        const key = `original${attribute.replace('-', '')}`;
        const original = element.dataset[key] ?? element.getAttribute(attribute);
        element.dataset[key] = original;
        element.setAttribute(attribute, targetLanguage === 'zh'
          ? original
          : translateText(original, targetLanguage));
      });
    });
    document.querySelectorAll('[data-language-switch]').forEach((button) => {
      button.textContent = targetLanguage === 'en' ? 'Switch to Chinese' : 'English';
      button.setAttribute('aria-label', targetLanguage === 'en' ? 'Switch language' : '切换语言');
      button.title = targetLanguage === 'en' ? 'Switch to Chinese' : 'Switch to English';
    });
    document.title = targetLanguage === 'en'
      ? document.title.replace('文档中心', 'Documentation').replace('服主手册', 'Server Owner Guide').replace('开发者文档', 'Developer Guide')
      : document.title.replace('Documentation', '文档中心').replace('Server Owner Guide', '服主手册').replace('Developer Guide', '开发者文档');
    document.querySelectorAll('meta[name="description"]').forEach((element) => {
      const original = element.dataset.originalDescription ?? element.getAttribute('content');
      element.dataset.originalDescription = original;
      element.setAttribute('content', targetLanguage === 'zh' ? original : translateText(original, targetLanguage));
    });
    if (targetLanguage === 'zh') restoreChineseText();
    if (targetLanguage === 'en') reportUntranslatedEnglishText();
    localStorage.setItem('leavesantiillegal-language', targetLanguage);
  };

  document.querySelectorAll('[data-language-switch]').forEach((button) => {
    button.addEventListener('click', () => {
      applyLanguage(document.documentElement.lang === 'en' ? 'zh' : 'en');
    });
  });

  const sidebar = document.querySelector('.sidebar');
  const toggle = document.querySelector('.nav-toggle');
  const search = document.querySelector('[data-doc-search]');
  const result = document.querySelector('[data-search-result]');
  const empty = document.querySelector('[data-empty-search]');
  const sections = [...document.querySelectorAll('.doc-section')];
  const navLinks = [...document.querySelectorAll('.sidebar nav a')];

  const closeSidebar = () => {
    sidebar?.classList.remove('open');
    toggle?.setAttribute('aria-expanded', 'false');
  };

  toggle?.addEventListener('click', () => {
    const open = sidebar?.classList.toggle('open') ?? false;
    toggle.setAttribute('aria-expanded', String(open));
  });

  navLinks.forEach((link) => link.addEventListener('click', closeSidebar));

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
      closeSidebar();
    }
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      search?.focus();
    }
  });

  search?.addEventListener('input', () => {
    const query = search.value.trim().toLocaleLowerCase('zh-CN');
    let matches = 0;
    sections.forEach((section) => {
      const visible = !query || section.textContent.toLocaleLowerCase('zh-CN').includes(query);
      section.hidden = !visible;
      if (visible) matches += 1;
    });
    navLinks.forEach((link) => {
      const target = document.querySelector(link.getAttribute('href'));
      link.hidden = Boolean(query && target?.hidden);
    });
    if (result) {
      result.textContent = query
        ? (document.documentElement.lang === 'en' ? `${matches} matching sections` : `找到 ${matches} 个章节`)
        : (document.documentElement.lang === 'en' ? `${sections.length} sections` : `共 ${sections.length} 个章节`);
    }
    empty?.classList.toggle('visible', matches === 0);
  });

  document.querySelectorAll('[data-copy-target]').forEach((button) => {
    button.addEventListener('click', async () => {
      const target = document.getElementById(button.dataset.copyTarget);
      if (!target) return;
      const value = target.textContent;
      try {
        await navigator.clipboard.writeText(value);
      } catch (_error) {
        const area = document.createElement('textarea');
        area.value = value;
        area.style.position = 'fixed';
        area.style.opacity = '0';
        document.body.append(area);
        area.select();
        document.execCommand('copy');
        area.remove();
      }
      const original = button.textContent;
      button.textContent = document.documentElement.lang === 'en' ? 'Copied' : '已复制';
      window.setTimeout(() => { button.textContent = original; }, 1300);
    });
  });

  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        navLinks.forEach((link) => {
          link.classList.toggle('active', link.getAttribute('href') === `#${entry.target.id}`);
        });
      });
    }, { rootMargin: '-15% 0px -72% 0px' });
    sections.forEach((section) => observer.observe(section));
  }

  applyLanguage(language);
})();
