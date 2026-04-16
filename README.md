# DisableSoulRender 插件

Minecraft 1.20.1 插件，阻止玩家在鞘翅飞行时使用 `cataclysm:soul_render`（Cataclysm Mod 的灵魂渲染器，右键可给鞘翅加速）。

## 🚀 使用 GitHub Actions 编译（推荐，无需本地环境）

### 步骤 1：创建 GitHub 仓库

1. 登录 https://github.com
2. 点右上角 `+` → **New repository**
3. 仓库名随意，比如 `DisableSoulRender`
4. 点 **Create repository**

### 步骤 2：上传代码

**方法 A — 网页直接上传（最简单）**：
1. 在刚创建的仓库页面，点 **uploading an existing file**
2. 把解压后的所有文件**拖进去**（注意：拖里面的内容，不是整个文件夹，保证 `pom.xml` 在仓库根目录）
3. 下面填个 commit message，点 **Commit changes**

**方法 B — 用 Git 命令**：
```bash
cd DisableSoulRender
git init
git add .
git commit -m "initial commit"
git branch -M main
git remote add origin https://github.com/你的用户名/DisableSoulRender.git
git push -u origin main
```

### 步骤 3：等待自动编译

1. 推送完成后，进入仓库页面
2. 点顶部的 **Actions** 标签
3. 会看到一个正在运行的 workflow（黄色圆点 = 进行中，绿色对勾 = 成功）
4. 大约 1-2 分钟跑完

### 步骤 4：下载 jar

1. 点进刚跑完的 workflow 运行记录
2. 页面底部找到 **Artifacts** 部分
3. 点 **DisableSoulRender** 下载 zip
4. 解压出来就是可用的 jar！

> 💡 **小提示**：之后修改代码推送到 GitHub，Actions 会自动重新编译。

---

## 前置要求（服务器端）

- Minecraft 1.20.1
- **混合服务端**：Mohist / Arclight / Banner 等（需要同时运行 Forge Mod + Bukkit 插件）

> ⚠️ 纯 Paper/Spigot 装不了 Forge Mod，纯 Forge 装不了 Bukkit 插件。必须用混合端。

## 安装

把 jar 扔进服务端 `plugins/` 目录，重启服务器。

## 配置

首次启动后会生成 `plugins/DisableSoulRender/config.yml`：

```yaml
message: "鞘翅飞行时无法使用 Soul Render！"
```

改完 `/reload` 或重启生效。

## 工作原理

插件监听 `PlayerInteractEvent` 和 `PlayerInteractEntityEvent`：
1. 检测玩家是否处于鞘翅飞行 (`player.isGliding()`)
2. 检测手持物品是否为 `cataclysm:soul_render`
3. 两条件都满足 → 取消事件 → 物品不会被使用
4. 通过 ActionBar 显示提示（2 秒冷却，不刷屏）

## 注意事项

- 如果 Mod 物品 ID 不是 `cataclysm:soul_render`，改 `SoulRenderListener.java` 里的 `soulRenderKey`。
- 插件用了三重匹配（NamespacedKey / 字符串 / 翻译键）兜底。
- 如果 Mod 是客户端按键直接触发加速、不经过服务端 interact 事件，插件拦不住。

## 本地编译（可选）

如果你本地装了 JDK 17 和 Maven：
```bash
mvn clean package
```
jar 生成在 `target/` 目录。
