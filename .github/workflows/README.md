# GitHub Actions Workflows

本目录当前采用两层结构：

- `ci-cd.yml`
  入口编排器。负责触发、并发取消、变更检测，以及按服务矩阵调用 reusable workflow。
- `reusable-service-pipeline.yml`
  可复用的单服务流水线模板。负责构建、质量检查、镜像构建、安全扫描和 Manifest 更新。

## 设计目标

- 单模块提交只跑该模块。
- 修改 `nexusmall-common` 或根 `pom.xml` 时，自动跑受影响服务。
- 同一分支新提交自动取消旧 run。
- 主流程尽量薄，避免 12 个服务重复维护同一套 job。

## 当前执行模型

`ci-cd.yml` 的执行顺序：

1. `detect-changes`
   使用 `dorny/paths-filter` 判断哪些服务受本次提交影响。
2. `build-service-matrix`
   将服务元数据和变更结果组装成矩阵，并在这里就过滤掉不需要执行的服务。
3. `service-pipeline`
   只对筛选后的服务逐个调用 `reusable-service-pipeline.yml`。

`reusable-service-pipeline.yml` 的执行顺序：

1. `build`
2. `quality-check`
3. `build-docker`
4. `security-scan`
5. `update-manifest`

## 如何新增一个服务

需要改两个地方。

1. 在 `ci-cd.yml` 的 `paths-filter` 中新增该服务的变更规则。
2. 在 `build-service-matrix` 的 JSON 中新增一项：

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

如果未来某个服务需要特殊测试逻辑，再扩展矩阵字段并透传到 `reusable-service-pipeline.yml` 即可。

## Secrets

reusable workflow 依赖以下 secrets：

- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- `GITHUB_TOKEN`

主 workflow 通过 `secrets: inherit` 透传。

## 并发与取消策略

`ci-cd.yml` 使用：

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

效果是同一分支上后一次提交会取消前一次仍在执行的 run。

## 维护约束

- 不要再把公共逻辑复制回主 workflow。
- 服务差异优先通过矩阵字段表达，不要复制新的 job。
- 只有当某个服务确实存在不可抽象的特殊行为时，才在 reusable workflow 中引入可选输入。
