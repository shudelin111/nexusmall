# GitHub Actions Workflows

当前仓库的 CI/CD 采用“两层结构”：

- `ci-cd.yml`
  当前启用的主入口工作流，默认使用矩阵模式。
- `reusable-service-pipeline.yml`
  可复用的单服务流水线模板，负责构建、质量检查、镜像构建、安全扫描和 Manifest 更新。
- `../workflow-presets/ci-cd-matrix.yml`
  矩阵版主流程模板，适合长期维护。
- `../workflow-presets/ci-cd-visual.yml`
  可视化版主流程模板，每个服务一个独立 job，适合更看重 GitHub Actions 页面可读性的场景。

## 设计目标

- 单模块改动时，只运行受影响的服务。
- 修改 `nexusmall-common`、根 `pom.xml` 或 workflow 基础设施时，自动运行所有服务。
- 同一分支重复提交时，自动取消旧的运行实例。
- 公共逻辑尽量收敛到 reusable workflow，避免主流程重复维护。

## 当前执行模型

`ci-cd.yml` 的执行顺序：

1. `detect-changes`
   使用 `dorny/paths-filter` 判断哪些服务受本次提交影响。
2. `build-service-matrix`
   将服务元数据和变更结果组装成矩阵，并在这里过滤掉不需要执行的服务。
3. `service-pipeline`
   对筛选后的服务逐个调用 `reusable-service-pipeline.yml`。

`reusable-service-pipeline.yml` 的执行顺序：

1. `build`
2. `quality-check`
3. `build-docker`
4. `security-scan`
5. `update-manifest`

## 为什么保留两套主流程模板

仓库里保留了矩阵模式和可视化模式两套主流程写法，但默认只启用一套，避免同一次 `push` 触发两套 CI。

- 矩阵模式
  当前启用文件：`workflows/ci-cd.yml`
  优点：主流程短，维护成本低。
  缺点：GitHub Actions 图形界面可读性一般。
- 可视化模式
  备用文件：`workflow-presets/ci-cd-visual.yml`
  优点：每个服务单独展示，GitHub Actions 页面更直观。
  缺点：主流程更长，新增服务时改动点更多。

## 如何切换模式

只保留一个激活版本在 `.github/workflows/ci-cd.yml`。

从矩阵模式切到可视化模式：

1. 用 `workflow-presets/ci-cd-visual.yml` 的内容覆盖 `workflows/ci-cd.yml`
2. 提交并推送

从可视化模式切回矩阵模式：

1. 用 `workflow-presets/ci-cd-matrix.yml` 的内容覆盖 `workflows/ci-cd.yml`
2. 提交并推送

建议：

- 更看重维护性时，用矩阵模式
- 更看重 GitHub 页面展示效果时，用可视化模式

## 如何新增一个服务

需要改两个地方：

1. 在 `ci-cd.yml` 的 `paths-filter` 中新增该服务的变更规则
2. 在 `build-service-matrix` 的服务列表中新增一项，例如：

```json
{
  "key": "coupon",
  "changed": "__CHANGED__",
  "service_name": "Coupon Service",
  "module_dir": "nexusmall-coupon",
  "image_name": "nexusmall-coupon",
  "deployment_file": "deploy/coupon-service/deployment.yaml"
}
```

如果未来某个服务需要特殊测试命令，可以在主 workflow 调用 reusable workflow 时传入：

```yaml
# test_enabled: true
# test_command: mvn -pl nexusmall-coupon -am test -B
# test_artifact_name: nexusmall-coupon-test-report
```

当前这些测试配置默认保留为注释，后续需要时直接取消注释即可。

## Workflow 变更策略

以下文件发生变更时，会默认触发所有服务流水线：

- `.github/workflows/**`
- `.github/workflow-presets/**`
- 根 `pom.xml`
- `nexusmall-common/**`

这样可以保证 CI 基础设施代码变更后，至少有一次真实构建来验证配置没有写坏。

## Secrets

reusable workflow 依赖以下 secrets：

- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`

主 workflow 通过 `secrets: inherit` 透传。

## 并发与取消策略

`ci-cd.yml` 使用：

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

效果是同一分支上后一次提交会取消前一次仍在运行的同名 workflow。

## 维护约束

- 不要把公共逻辑复制回主 workflow。
- 服务差异优先通过矩阵字段或 reusable workflow 入参表达。
- 只有当某个服务确实存在不可抽象的特殊行为时，才为 reusable workflow 增加新的可选输入。
