# LumaLogApp

LumaLogApp 是 LumaLog 的 Android 离线版应用。它延续 Web 端“贡献热力图”的核心视觉，把 habit 的坚持过程做成一格一格被点亮的记录：用户无需登录，也不需要连接外部数据库，即可在手机本地创建 habit、每日签到、查看热力图，并通过 JSON 文件完成数据导出与迁移。

LumaLog 的含义来自 `Luma` 与 `Log`：前者代表光与点亮，后者代表记录。这个 App 的目标也很克制：用足够轻的操作，帮助用户持续记录阅读、健身、学习、戒断、健康管理等长期目标。

## 项目预览

### 首页

首页展示所有 habit，每个 habit 以热力图作为主体内容。亮色与暗色主题都保持简洁、克制，让用户可以快速看到最近几个月的坚持情况。

![首页中文亮色](resource/首页-中文-亮色.jpg)

![首页中文暗色](resource/首页-中文-暗色.jpg)

App 同时支持中英文切换。

![首页英文亮色](resource/首页-英文-亮色.jpg)

![首页英文暗色](resource/首页-英文-暗色.jpg)

### 签到页

签到页围绕一个核心动作设计：点亮今天。页面会根据 habit 的规则展示今日是否可签到、是否已完成，以及对应的热力图记录。

![签到页未签到](resource/签到页-中文-亮色-未签到.jpg)

![签到页已签到](resource/签到页-中文-亮色-已签到.jpg)

### 新增与编辑

用户可以创建和编辑 habit，包括名称、分类、颜色主题、有效时间段、每日目标次数、开始日期、是否不限天数等配置。

![新增页中文亮色](resource/新增页-中文-亮色.jpg)

![编辑页中文亮色](resource/编辑页-中文-亮色.jpg)

### 设置页

设置页集中管理主题、语言、首页展示模式、统计信息开关，以及数据导入导出。

![设置页中文亮色](resource/设置页-中文-亮色.jpg)

![设置页中文暗色](resource/设置页-中文-暗色.jpg)

## 核心功能

- 离线使用：无需登录，无需后端服务，无需外接数据库。
- 本地持久化：habit、分类、签到记录和偏好设置保存在手机本地。
- Habit 管理：支持创建、编辑、删除 habit。
- 分类管理：内置戒断、健康、健身、学习、阅读、工作、创作、生活等分类。
- 贡献热力图：用类似 GitHub Contribution Graph 的方式展示每日签到状态。
- 签到规则：支持全天或指定时间段签到，支持每日目标次数。
- 首页模式：支持全部模式和分类模式。
- 统计显示：连续天数、最长连续、完成率、总次数、今日已点亮等信息可在设置中开关。
- 主题切换：支持亮色、暗色和跟随系统。
- 多语言：支持中文和英文。
- 数据迁移：支持导出 JSON 备份文件，也支持从 JSON 文件导入恢复。

## 技术栈

- Kotlin
- Android Gradle Plugin
- Jetpack Compose
- Material 3
- AndroidX Activity Compose
- AndroidX Lifecycle
- JSON 本地序列化
- Storage Access Framework 文件导入导出

## 本地数据

LumaLogApp 采用本地 JSON 文件持久化，不依赖 Room、SQLite 或远程数据库。默认数据文件位于应用私有目录：

```text
filesDir/lumalog-data.json
```

导出时会生成类似下面名称的备份文件：

```text
lumalog-backup-YYYY-MM-DD.json
```

该设计适合个人离线使用，也方便用户在换机或重装应用时迁移数据。

## 项目结构

```text
LumaLogApp
├─ app
│  ├─ src/main/java/com/example/lumalogapp
│  │  ├─ data             # 数据模型、本地存储、统计与签到逻辑
│  │  ├─ ui
│  │  │  ├─ components    # Logo、卡片、热力图、表单控件等通用组件
│  │  │  ├─ i18n          # 中英文文案
│  │  │  ├─ screens       # 首页、签到页、编辑页、设置页
│  │  │  ├─ theme         # Material 3 主题、颜色和字体
│  │  │  ├─ utils         # UI 辅助方法
│  │  │  └─ LumaLogApp.kt # App 导航与状态入口
│  │  └─ MainActivity.kt
│  └─ src/main/res        # 图标、主题和资源文件
├─ resource               # README 展示截图
├─ build.gradle.kts
├─ settings.gradle.kts
└─ README.md
```

## 本地构建

Windows:

```bash
.\gradlew.bat assembleDebug
```

macOS / Linux:

```bash
./gradlew assembleDebug
```

生成的 Debug APK 通常位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 设计原则

LumaLogApp 的设计重点不是复杂的任务管理，而是让“坚持”变得可见。

- 热力图是视觉主角，卡片和控件尽量保持安静。
- 签到动作足够轻，打开 habit 后可以快速完成点亮。
- 首页信息默认克制，统计信息交给用户自行配置。
- 亮色主题清爽，暗色主题柔和，避免过重装饰。
- 数据保存在本地，让用户可以离线、私密地记录自己的长期目标。

## 关联项目

- `LumaLogFrontEnd`：基于 Vue 3 + Vite 的 Web 前端。
- `LumaLogBackEnd`：基于 Gin + PostgreSQL 的 Web 后端服务。
