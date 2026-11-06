# 文档索引 · 完整教程与图文资料

本目录收录**本分支（二开版）**的完整中文教程、功能说明与图文排障资料。上游官方各端说明仍见 [`../../integrations/`](../../integrations/) 与 [根 README](../../README.md)。

## 从哪篇开始

| 想做什么 | 看哪篇 |
| --- | --- |
| 第一次接触，想从头装好并用起来 | **[完整使用教程.md](./完整使用教程.md)** ← 从这里开始 |
| 想知道到底有哪些功能、哪些是这次新加的 | **[功能总览.md](./功能总览.md)** |
| 手机上看不到悬浮球 / 装不上 APK | **[悬浮窗排障-图文卡.html](./悬浮窗排障-图文卡.html)** |
| 想把源码推到 GitHub、用 Actions 出 APK | **[路线B-图文教程.html](./路线B-图文教程.html)** · [路线B-推送与出包操作卡.txt](./路线B-推送与出包操作卡.txt) |
| 不想编译，想在官方 APK 里注入军师规则 | [狗头军师-手机常驻笔记.txt](./狗头军师-手机常驻笔记.txt) |
| 只配 DeepSeek 三个接口 | [android-deepseek/README.md](./android-deepseek/README.md)（上游官方图文教程） |

> 两个 `.html` 是**自包含单文件**，没有外部依赖，可离线打开。在 GitHub 网页上请点 **Raw** 或下载到本地用浏览器打开；也可直接打印出来对照手机操作。

## 内容一览

**完整使用教程.md** — 上游与 Jev 的关系、正确工作流、四端支持矩阵、安卓从零跑通一轮（安装 / 三项权限 / 三处接口配置 / 悬浮球与常驻 / 截屏识别 / 核对 → 分析 → 候选 → 手动发送）、完整 Skill 包在 Mac 与 Android 的用法、无编译 workaround、源码构建、隐私边界、常见问题速查表。

**功能总览.md** — 按 A–L 分模块列出全部功能，逐项标注【上游】/【新增】/【修复】；含本次四项二开的技术实现、CI 与测试清单、明确不支持的范围、关键文件地图。

**悬浮窗排障-图文卡.html** — 7 节图文：根因结论、30 秒分支自测图、设置页必查项、强制重绑流程、源码修复说明、装机与签名警告、常驻与保活、说话人判错。

**路线B-图文教程.html** — 从本地改好的源码推到 GitHub、由 Actions 云端编译并取回 APK 的图文过程，含命令代码块与排错表。

## GitHub 界面点击位置（标注图）

推源码时最容易卡在「不知道点哪」，这三张是实际操作截图标注：

| 场景 | 标注图 | 要点 |
| --- | --- | --- |
| 新建空仓库 | [01-新建仓库入口.png](./images/01-新建仓库入口.png) | 右上角 `＋ ▼` → `New repository`，不要勾任何初始化文件 |
| 空仓库页 | [02-空仓库页与Actions.png](./images/02-空仓库页与Actions.png) | 只复制 HTTPS 地址行；不要照抄 GitHub 默认的两段命令；推完点顶部 `Actions` |
| PAT 二次验证 | [03-sudo二次验证.png](./images/03-sudo二次验证.png) | `Sudo authentication failed.` 是 GitHub 敏感操作二次验证，点 `Verify via email`，不是封号也不是推送失败 |

也可以不走 PAT：用 Git Credential Manager 的 OAuth 或设备码授权。PAT 若用于推送，需要勾 `repo` 与 `workflow` 两个 scope；用完请在 Settings 里 Revoke。

## 快速命令

```bash
# 离线校验（仓库根目录）
python3 -B scripts/validate_skill.py
python3 -B -m unittest discover -s tests -q        # 181 tests

# 重新生成 Android 内置 Skill 资源
python3 -B scripts/sync_android_skill.py
python3 -B -m unittest tests.test_android_skill_assets -v

# Mac：切到完整上游 Skill
GOUTOU_SKILL_MODE=full ./start.command            # 在 integrations/jev_mac/ 下
```

## APK 从哪拿

- **[v1.0-android-preview Release（推荐）](https://github.com/clearlove6938/goutoujunshi-geren/releases/tag/v1.0-android-preview)**：下载附件 [`goutoujunshi-android-debug-v1.0-speaker-fix.apk`](https://github.com/clearlove6938/goutoujunshi-geren/releases/download/v1.0-android-preview/goutoujunshi-android-debug-v1.0-speaker-fix.apk)（27.2 MB / SHA256 `f9ee19af…`）。附件名是 ASCII 安全名（GitHub 上传接口会剥掉中文名），内容与本机 `狗头军师-说话人修正版.apk` 完全相同，含完整 Skill、悬浮球兜底与常驻、OCR 说话人分边。
- 或本仓库 **Actions → `Platform preview packages` → `android` → artifact `goutoujunshi-jev-chat-android-debug`**（CI 重新跑过的版本，签名会与 Release 附件不同）。
- ⚠️ 每次 CI 构建都会生成新的 debug keystore，**换版本必须先卸载旧版**（会清空本地设置）。若还没装过，直接装最新版即可。

离线检查与 CI 不能替代真实设备、模型接口与聊天应用的兼容性测试。仓库主体采用 [MIT 许可证](../../LICENSE)。
