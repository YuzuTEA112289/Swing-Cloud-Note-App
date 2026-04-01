# Swing Cloud Note - 云同步备忘录

基于 Swing + Spring Boot 的简易备忘录应用，支持本地文件存储与 MySQL 云端同步。

---

## 功能概述

- 文本编辑（剪切、复制、粘贴、删除）
- 自定义背景色、字体颜色、字体大小
- 本地文件保存/打开
- 云端笔记保存/加载（通过 REST API）

---

## 部署教程（4 步）

### 1️⃣ 准备 MySQL 数据库
- 安装 MySQL 并创建数据库（例如 `note_db`）
  ```sql
  CREATE DATABASE note_db CHARACTER SET utf8mb4;
  ```

### 2️⃣ 修改数据库配置
- **文件位置**：`src/main/resources/application.yml`
- **修改内容**：填写你的数据库连接信息（文件中有具体注释）
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/数据库名称?serverTimezone=UTC
      username: root
      password: 数据库密码
  ```

### 3️⃣ 启动后端服务
- **文件位置**：`src/main/java/com/menu/note/SpringBootNoteApp.java`
- **操作**：在 IDE 中右键运行该类，控制台输出 `Spring Boot Note App Started!` 即成功（端口 `8080`）

### 4️⃣ 启动客户端
- **文件位置**：`src/main/java/com/menu/note/ui/Swing_Cloud_Note_App.java`
- **操作**：右键运行该类，弹出图形界面后即可使用

---

## 核心文件索引

| 文件 | 路径 | 说明 |
|------|------|------|
| `application.yml` | `src/main/resources/` | 数据库连接配置 |
| `SpringBootNoteApp.java` | `src/main/java/com/menu/note/` | 后端启动入口 |
| `NoteController.java` | `src/main/java/com/menu/note/controller/` | API 接口（`/api/notes`） |
| `Swing_Cloud_Note_App.java` | `src/main/java/com/menu/note/ui/` | 客户端启动入口 |

---

## 使用提示

- 云端功能必须先启动后端服务（步骤 3）
- 确保 JDK 版本 ≥ 11（`java.net.http.HttpClient` 需要）
- 若修改端口，需同步修改客户端中的 `API_BASE_URL` 常量

---
