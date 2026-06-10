# SIM Monitor

SIM Monitor（SIM 保号管家）是一款用于管理多张 SIM 卡保号状态的 Android 应用。它可以记录 SIM 卡信息、余额、充值/短信/通话/流量等活跃记录，并根据运营商保号规则计算卡片状态，在即将到期或存在失效风险时通过本地通知提醒用户。

这个项目适合管理海外实体卡、备用号码、长期低频使用号码，以及需要定期充值、发短信、拨打电话或产生流量来保持号码活跃的 SIM 卡。

## 功能特性

- 多 SIM 卡管理：维护号码、国家/地区、运营商、网络、卡类型、余额、币种、激活日期等信息。
- 保号规则计算：根据不同运营商规则计算下一次保号动作和预计失效日期。
- 默认规则模板：内置 Giffgaff UK、CTExcel UK 和通用 180 天保号规则。
- 自定义规则：支持配置活跃周期、提醒窗口、所需动作、首激活期限、付费保号服务等。
- 使用记录：记录充值、短信、通话、流量、套餐购买、余额变更、付费保号服务等操作。
- 状态分级：将 SIM 卡标记为 `HEALTHY`、`ATTENTION`、`RISK`、`EXPIRED` 或 `UNKNOWN`。
- 后台巡检：通过 WorkManager 每 24 小时执行一次本地检查。
- 本地提醒：通过 Android 通知提醒用户执行保号动作。
- 历史与搜索：查看操作历史，按 SIM 卡或类型筛选，并支持搜索卡片。
- 本地设置：支持通知、每日检查、号码隐藏、深色模式、默认国家/币种和语言设置。

## 技术栈

- Kotlin
- Android Jetpack Compose
- Material 3
- Room Database
- DataStore Preferences
- WorkManager
- Navigation Compose
- Coroutines / Flow
- Gradle Kotlin DSL

## 项目结构

```text
app/src/main/java/com/example/
+-- data/
|   +-- local/
|   |   +-- dao/            # Room DAO
|   |   +-- database/       # Room 数据库
|   |   +-- entity/         # SIM 卡、规则、记录、提醒实体
|   +-- repository/         # 数据访问与状态重算逻辑
+-- domain/rule/            # 保号规则引擎
+-- notification/           # 本地通知封装
+-- settings/               # 设置与本地化
+-- ui/                     # Compose 页面与组件
|   +-- addsim/
|   +-- dashboard/
|   +-- history/
|   +-- navigation/
|   +-- rules/
|   +-- search/
|   +-- settings/
|   +-- simdetail/
+-- worker/                 # 后台定期巡检任务
+-- MainActivity.kt
+-- SIMMonitorApp.kt
```

## 运行项目

### 环境要求

- Android Studio
- JDK 11 或更高版本
- Android SDK，项目当前使用 `compileSdk 36`

### 本地运行

1. 克隆仓库：

   ```bash
   git clone https://github.com/kadadaf/SIM-Monitor.git
   cd SIM-Monitor
   ```

2. 使用 Android Studio 打开项目根目录。

3. 等待 Gradle 同步完成。

4. 在项目根目录创建 `.env` 文件：

   ```env
   GEMINI_API_KEY=YOUR_GEMINI_API_KEY
   ```

   当前核心功能主要是本地 SIM 管理和规则提醒；该变量来自 AI Studio 项目模板。

5. 如需本地调试，可根据 Android Studio 提示处理签名配置。若导入时遇到 debug 签名相关问题，可以临时移除 `app/build.gradle.kts` 中 debug 构建的自定义签名配置：

   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```

6. 选择模拟器或真机运行应用。

## 核心模型

应用主要包含四类本地数据：

- `SIMCard`：SIM 卡基础信息、余额、规则绑定和当前状态。
- `RuleTemplate`：运营商或用户自定义的保号规则。
- `UsageRecord`：充值、短信、通话、流量等使用记录。
- `ReminderRecord`：根据规则生成的提醒记录。

## 保号状态说明

- `HEALTHY`：距离失效时间较远，当前状态健康。
- `ATTENTION`：需要关注，建议提前准备下一次保号动作。
- `RISK`：临近失效，需要尽快操作。
- `EXPIRED`：规则判断已过期或已失效。
- `UNKNOWN`：未绑定有效规则，无法判断状态。

## 注意事项

- 当前应用主要依赖用户手动录入 SIM 卡和使用记录。
- 应用未申请读取短信、通话记录或 SIM 状态等敏感权限，因此不会自动读取运营商短信或真实余额。
- 余额、保号动作和到期判断基于本地记录与规则模板，仅供管理提醒使用。
- 部分中文本地化字符串可能存在编码异常，后续可统一修复。

## License

当前仓库暂未声明开源许可证。使用、分发或二次开发前请先确认项目授权。
