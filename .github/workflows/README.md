# GitHub Actions 快速上手指南

## 🎯 这是什么？

GitHub Actions 是 GitHub 内置的 **自动化流水线**,让您的代码:
- ✅ 自动编译
- ✅ 自动测试
- ✅ 自动构建 Docker 镜像
- ✅ 自动部署

**触发条件**: 每次 push 代码或创建 Pull Request 时自动执行

---

## 🚀 快速开始 (3 步配置)

### **步骤 1: 提交新配置到 GitHub**

```bash
cd D:\IdeaProjects\nexusmall

# 添加新创建的 GitHub Actions 配置
git add .github/workflows/ci-cd.yml

# 提交
git commit -m "ci: 添加 GitHub Actions CI/CD 配置"

# 推送到 GitHub
git push origin master
```

### **步骤 2: 配置 Docker Hub 凭证**

1. 访问您的 GitHub 仓库: https://github.com/shudelin111/nexusmall
2. 点击 **Settings** → **Secrets and variables** → **Actions**
3. 点击 **New repository secret**
4. 添加以下 2 个密钥:

| Name | Value | 说明 |
|------|-------|------|
| `DOCKER_USERNAME` | 您的 Docker Hub 用户名 | 用于登录 Docker Hub |
| `DOCKER_PASSWORD` | Docker Hub Access Token | [点击生成](https://hub.docker.com/settings/security) |

**生成 Docker Token 步骤:**
- 登录 https://hub.docker.com
- Settings → Security → Create Access Token
- 权限勾选：Read & Write
- 复制生成的 Token，粘贴到 GitHub Secrets

### **步骤 3: 查看自动化执行**

推送代码后:
1. 访问 https://github.com/shudelin111/nexusmall/actions
2. 看到正在运行的流水线
3. 点击查看详情

---

## 📊 工作流程详解

### **当您 push 代码到 develop 分支**

```
push 代码
    ↓
GitHub Actions 触发
    ↓
┌─────────────────────────────┐
│ 1. Build & Test             │
│   - Maven 编译              │
│   - 运行单元测试            │
│   - 生成测试报告            │
└─────────────────────────────┘
    ↓
┌─────────────────────────────┐
│ 2. Build Docker Image       │
│   - 多阶段构建              │
│   - 推送到 Docker Hub       │
└─────────────────────────────┘
    ↓
┌─────────────────────────────┐
│ 3. Deploy to Dev            │
│   - 更新 K8s Deployment     │
│   - 等待滚动更新完成        │
│   - 健康检查                │
└─────────────────────────────┘
    ↓
✅ 开发环境部署完成!
```

### **当您 push 代码到 main 分支**

```
push 代码
    ↓
Build & Test ✓
    ↓
Quality Check (代码质量检查)
    ↓
Build Docker Image ✓
    ↓
Security Scan (漏洞扫描)
    ↓
✅ 准备就绪，等待手动部署到生产
```

### **当您打 Tag (v1.0.0)**

```
git tag v1.0.0
git push origin v1.0.0
    ↓
触发生产部署流程
    ↓
蓝绿部署 (Blue-Green)
    ↓
✅ 生产环境发布成功!
```

---

## 🔧 自定义配置

### **修改触发分支**

编辑 `.github/workflows/ci-cd.yml`:

```yaml
on:
  push:
    branches: [ main, develop ]  # 修改这里
    tags: [ 'v*' ]
```

### **添加更多测试**

在 `build-and-test` job 中添加:

```yaml
- name: Run Integration Tests
  run: mvn verify -P integration-test
```

### **配置通知**

添加钉钉/企业微信通知:

```yaml
- name: Send DingTalk Notification
  run: |
    curl 'https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN' \
      -H 'Content-Type: application/json' \
      -d '{
        "msgtype": "text",
        "text": {
          "content": "部署成功！版本：${{ github.ref_name }}"
        }
      }'
```

---

## 📈 查看执行结果

### **1. 查看流水线状态**

访问: https://github.com/shudelin111/nexusmall/actions

绿色 ✅ = 成功  
红色 ❌ = 失败  
黄色 ⏳ = 运行中

### **2. 查看详细日志**

点击任意一次运行 → 点击具体 Job → 查看每一步输出

### **3. 查看测试覆盖率**

下载 `test-results` 工件 → 打开 HTML 报告

### **4. 查看安全扫描结果**

仓库页面 → Security → Code scanning alerts

---

## 🎯 常见场景

### **场景 1: 只想编译测试，不部署**

```bash
# 推送到 feature 分支即可
git checkout -b feature/new-feature
git push origin feature/new-feature
```

### **场景 2: 跳过 CI 直接推送**

在 commit message 中添加 `[skip ci]`:

```bash
git commit -m "docs: 更新文档 [skip ci]"
git push
```

### **场景 3: 手动重新运行失败的 Job**

Actions 页面 → 点击失败的运行 → 点击 "Re-run jobs"

---

## 🔐 安全最佳实践

### **1. 不要硬编码密码**

❌ **错误做法**:
```yaml
- name: Deploy
  run: docker login -u admin -p 123456
```

✅ **正确做法**:
```yaml
- name: Deploy
  run: docker login -u ${{ secrets.DOCKER_USER }} -p ${{ secrets.DOCKER_PASS }}
```

### **2. 使用 Environment Protection Rules**

Settings → Environments → production
- 要求审查者批准
- 限制分支
- 等待延迟

### **3. 定期轮换密钥**

每 3 个月更新一次 Docker Hub Token 和 Kubeconfig

---

## 💡 与 GitLab CI/CD 的区别

| 特性 | GitHub Actions | GitLab CI/CD |
|------|----------------|--------------|
| **配置文件位置** | `.github/workflows/*.yml` | `.gitlab-ci.yml` |
| **触发语法** | `on: push` | `rules: if` |
| **Job 定义** | `jobs:` | `job_name:` |
| **步骤语法** | `steps:` | `script:` |
| **免费额度** | 2000 分钟/月 | 400 分钟/月 |
| **市场插件** | GitHub Marketplace | GitLab Components |

**建议**: 既然代码在 GitHub，就用 GitHub Actions 更原生！

---

## 🚨 常见问题

### **Q: 为什么 Actions 没有触发？**

A: 检查以下几点:
1. 配置文件是否在 `.github/workflows/` 目录下
2. 文件名是否以 `.yml` 或 `.yaml` 结尾
3. YAML 缩进是否正确
4. 分支名是否匹配

### **Q: 如何取消正在运行的流水线？**

A: Actions 页面 → 点击运行中的任务 → 点击 "Cancel workflow"

### **Q: 构建太慢了怎么办？**

A: 启用缓存:
```yaml
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: maven-${{ hashFiles('**/pom.xml') }}
```

### **Q: 如何并行运行多个任务？**

A: GitHub Actions 默认并行，无需特殊配置

---

## 📚 下一步

1. ✅ 提交配置到 GitHub
2. ✅ 配置 Docker Hub 密钥
3. ✅ 推送代码测试
4. ✅ 查看 Actions 执行结果
5. 📖 学习更多高级用法:
   - [GitHub Actions 官方文档](https://docs.github.com/en/actions)
   - [Awesome Actions](https://github.com/sdras/awesome-actions)

---

**祝您使用愉快!** 🎉

有任何问题随时问我！
