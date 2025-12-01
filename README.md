# 🎓 智能教育APP (Intelligent Learning Partner)

> 基于AI技术的智能教育平台，支持学生端和教师端双角色系统，提供个性化学习、智能辅导、数据分析等功能。

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📖 项目简介

**智能教育APP** 是一款面向K12阶段（重点是初中）的智能教育平台，通过集成智谱AI的GLM-4-Flash模型，实现了AI驱动的个性化学习和智能教学管理。

### 🎯 核心价值

- **学生端**：个性化学习助手，提供AI辅导、资源推荐、学习分析
- **教师端**：智能教学工作台，提供班级管理、学生分析、教学建议
- **双端协同**：数据互通，师生互动，形成完整的教育闭环

---

## ✨ 主要功能

### 👨‍🎓 学生端功能

| 功能模块 | 描述 | 亮点 |
|---------|------|------|
| 🤖 AI智能辅导 | 24小时在线答疑，自然语言对话 | 智谱AI GLM-4-Flash模型 |
| 📚 学习资源管理 | 视频、文章、练习等多种资源 | 智能推荐，个性化匹配 |
| 📊 学习数据分析 | 学习时长、知识点掌握度、趋势分析 | 可视化图表，AI评价 |
| 👥 协作学习 | 小组讨论、小组竞赛 | 实时互动，团队协作 |
| 📈 个人中心 | 学习历史、成就系统、个人设置 | 完整的学习档案 |

### 👨‍🏫 教师端功能

| 功能模块 | 描述 | 亮点 |
|---------|------|------|
| 🏫 班级管理 | 创建班级、管理学生、查看统计 | AI管理建议 |
| 📊 学生数据分析 | 学习进度、成绩分析、对比分析 | 自动识别需要帮助的学生 |
| 📝 题目管理 | 题库管理、创建题目 | AI智能出题 |
| 📈 教学分析 | 教学效果评估、改进建议 | AI生成分析报告 |
| 🎯 协作活动管理 | 创建和监控协作活动 | 实时进展追踪 |
| 💾 数据管理中心 | 数据统计、导入导出、备份 | 完整的数据管理 |

---

## 🛠️ 技术栈

### 核心技术

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 开发语言 | Kotlin | 2.1.0 | Google官方推荐 |
| 架构模式 | MVVM | - | 清晰的架构设计 |
| UI框架 | Material Design | 1.10.0 | 现代化UI组件 |
| 数据库 | Room | 2.8.0 | 本地数据持久化 |
| 网络请求 | Retrofit | 2.9.0 | HTTP客户端 |
| 异步处理 | Coroutines | - | Kotlin协程 |
| 图片加载 | Glide | 4.16.0 | 高效图片加载 |
| AI服务 | 智谱AI | GLM-4-Flash | 大语言模型 |

### 架构设计

```
┌─────────────────────────────────┐
│      View Layer (UI)            │
│  Activity, Fragment, XML        │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│      ViewModel Layer            │
│  LiveData, 业务逻辑              │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│      Repository Layer           │
│  数据访问逻辑                    │
└──────────────┬──────────────────┘
               │
       ┌───────┴────────┐
       │                │
┌──────▼─────┐  ┌──────▼──────┐
│  Room DB   │  │  Retrofit   │
│  (本地)    │  │  (网络)     │
└────────────┘  └─────────────┘
```

---

## 📱 系统要求

- **最低SDK版本**: Android 8.0 (API 26)
- **目标SDK版本**: Android 15 (API 35)
- **开发工具**: Android Studio Hedgehog | 2023.1.1 或更高版本
- **JDK版本**: JDK 17

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/Fisevone/Intelligent-Learning-Partner.git
cd Intelligent-Learning-Partner
```

### 2. 配置环境

1. 安装 [Android Studio](https://developer.android.com/studio)
2. 打开项目：`File -> Open -> 选择项目目录`
3. 等待Gradle同步完成

### 3. 配置API密钥

在 `EducationAPP/app/src/main/java/com/example/educationapp/network/ApiConstants.kt` 中配置您的智谱AI API密钥：

```kotlin
object ApiConstants {
    const val DEEPSEEK_API_KEY = "your_api_key_here"  // 替换为您的API密钥
}
```

> 💡 获取API密钥：访问 [智谱AI开放平台](https://open.bigmodel.cn/) 注册并获取免费API密钥

### 4. 运行项目

1. 连接Android设备或启动模拟器
2. 点击 `Run` 按钮或按 `Shift + F10`
3. 选择目标设备运行

### 5. 测试账号

**学生账号**:
- 用户名: `student01`
- 密码: `123456`

**教师账号**:
- 用户名: `teacher01`
- 密码: `123456`

---

## 📂 项目结构

```
EducationAPP/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/educationapp/
│   │   │   │   ├── ai/              # AI功能模块
│   │   │   │   ├── auth/            # 认证模块
│   │   │   │   ├── data/            # 数据层
│   │   │   │   │   ├── dao/         # 数据访问对象
│   │   │   │   │   └── entity/      # 数据实体
│   │   │   │   ├── network/         # 网络层
│   │   │   │   ├── repository/      # 数据仓库
│   │   │   │   ├── service/         # 服务层
│   │   │   │   ├── ui/              # UI层
│   │   │   │   │   ├── main/        # 学生主界面
│   │   │   │   │   ├── teacher/     # 教师界面
│   │   │   │   │   ├── learning/    # 学习功能
│   │   │   │   │   └── collaboration/ # 协作功能
│   │   │   │   └── utils/           # 工具类
│   │   │   ├── res/                 # 资源文件
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # 单元测试
│   └── build.gradle.kts
├── docs/                            # 答辩文档
│   ├── 答辩文档.md
│   ├── 完整答辩文档-精简版.md
│   ├── 答辩速查手册.md
│   ├── 答辩PPT大纲.md
│   └── 答辩常见问题解答.md
└── README.md
```

---

## 💾 数据库设计

项目使用Room数据库，包含7张核心表：

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| users | 用户信息 | id, username, userType, grade |
| learning_records | 学习记录 | userId, resourceId, duration, score |
| resources | 学习资源 | id, title, type, subject, difficulty |
| learning_progress | 学习进度 | userId, knowledgePoint, masteryLevel |
| learning_statistics | 学习统计 | userId, date, totalTime, averageScore |
| recommendations | 推荐记录 | userId, resourceId, reason, score |
| learning_behaviors | 学习行为 | userId, behaviorType, timestamp |

---

## 🤖 AI功能详解

### 智谱AI集成

本项目集成了智谱AI的GLM-4-Flash模型，提供以下AI功能：

1. **智能对话**
   - 自然语言问答
   - 学科知识解答
   - 学习建议生成

2. **学习分析**
   - 分析学习数据
   - 生成个性化报告
   - 预测学习效果

3. **智能出题**
   - 根据知识点生成题目
   - 自动设置难度
   - 提供详细解析

4. **教学建议**
   - 分析教学数据
   - 生成改进建议
   - 个别辅导策略

### Prompt工程

项目采用精心设计的Prompt模板，确保AI回复的质量和准确性：

```kotlin
val prompt = """
你是一位专业的初中教育AI助手。

学生信息：
- 年级：${user.grade}
- 学习风格：${user.learningStyle}

学生问题：${userMessage}

请提供简洁、有针对性的回答（200字以内）。
"""
```

---

## 📊 项目亮点

### 🎯 技术亮点

1. **MVVM架构** - 清晰的架构设计，易于维护和扩展
2. **Kotlin协程** - 高效的异步处理，避免回调地狱
3. **Room数据库** - 类型安全的数据持久化
4. **AI深度集成** - 智谱AI GLM-4-Flash模型，提供智能化功能
5. **响应式编程** - LiveData/Flow实现数据驱动

### 💡 功能亮点

1. **双角色系统** - 学生端和教师端分离，满足不同用户需求
2. **个性化学习** - 基于用户数据的智能推荐和学习路径规划
3. **数据驱动决策** - 多维度数据分析，支持精准教学
4. **AI智能辅导** - 24小时在线，即时解答学习疑问
5. **协作学习** - 小组讨论和竞赛，提升学习兴趣

### 🔒 安全设计

1. **密码加密** - SHA-256哈希算法
2. **数据隔离** - 基于角色的权限控制
3. **API安全** - HTTPS加密传输
4. **异常处理** - 完善的容错机制

---

## 📸 应用截图

> 💡 提示：可以在这里添加应用的实际截图

### 学生端

- 登录界面
- 主页（学习资源）
- AI对话界面
- 学习分析页面
- 个人中心

### 教师端

- 教师工作台
- 班级管理
- 学生数据分析
- 题目管理
- 教学分析报告

---

## 🧪 测试

### 运行单元测试

```bash
./gradlew test
```

### 运行UI测试

```bash
./gradlew connectedAndroidTest
```

---

## 📝 开发文档

项目包含完整的答辩文档，位于根目录：

- **答辩文档.md** - 完整的项目文档（1000+行）
- **完整答辩文档-精简版.md** - 精简版文档，逻辑清晰
- **答辩速查手册.md** - 快速参考手册
- **答辩PPT大纲.md** - PPT制作指南
- **答辩常见问题解答.md** - 常见问题库

---

## 🗺️ 开发路线图

### ✅ 已完成

- [x] 用户认证系统
- [x] 学生端核心功能
- [x] 教师端核心功能
- [x] AI对话功能
- [x] 学习数据分析
- [x] 协作学习基础功能
- [x] 数据库设计与实现

### 🚧 进行中

- [ ] 性能优化
- [ ] UI/UX优化
- [ ] 单元测试补充

### 📅 计划中

- [ ] 语音识别与合成
- [ ] 视频通话功能
- [ ] AR/VR学习体验
- [ ] iOS版本开发
- [ ] Web版本开发
- [ ] 数据云端同步

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 代码规范

- 遵循Kotlin编码规范
- 添加必要的注释
- 编写单元测试
- 保持代码简洁

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👥 作者

**Fisevone**

- GitHub: [@Fisevone](https://github.com/Fisevone)
- 项目链接: [Intelligent-Learning-Partner](https://github.com/Fisevone/Intelligent-Learning-Partner)

---

## 🙏 致谢

- [智谱AI](https://open.bigmodel.cn/) - 提供AI能力支持
- [Android Jetpack](https://developer.android.com/jetpack) - 提供架构组件
- [Material Design](https://material.io/) - 提供UI设计指南
- [Kotlin](https://kotlinlang.org/) - 优秀的开发语言

---

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 提交 [Issue](https://github.com/Fisevone/Intelligent-Learning-Partner/issues)
- 发送邮件至项目维护者

---

## ⭐ Star History

如果这个项目对您有帮助，请给个Star ⭐️

---

<div align="center">

**让每个学生都能获得个性化的学习指导**

**让每位教师都能拥有智能化的教学助手**

Made with ❤️ by Fisevone

</div>
