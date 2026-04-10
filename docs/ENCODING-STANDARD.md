# 代码编码规范

## 📋 重要说明

本项目**严格使用UTF-8编码（无BOM）**，所有Java源文件、配置文件必须遵循此规范。

## ⚠️ 禁止事项

1. **严禁使用Windows记事本编辑任何代码文件**
   - Windows记事本会自动添加BOM字符
   - BOM会导致Java编译失败

2. **严禁在IDE中更改文件编码为GBK/GB2312等其他编码**
   - 会导致中文注释乱码
   - 破坏跨平台兼容性

3. **严禁手动添加BOM字符**
   - Java编译器不支持带BOM的源文件
   - 会导致 `非法字符: '\ufeff'` 编译错误

## ✅ 正确做法

### 1. IDEA设置（一次性配置）

```
File → Settings → Editor → File Encodings
- Global Encoding: UTF-8
- Project Encoding: UTF-8
- Default encoding for properties files: UTF-8
- ✓ Transparent native-to-ascii conversion
```

### 2. 提交前检查

运行编码检查脚本：
```powershell
.\check-encoding.ps1
```

如果发现BOM文件，自动修复：
```powershell
# 扫描并移除所有Java文件的BOM
$files = Get-ChildItem -Path . -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
foreach($file in $files) {
    $bytes = [System.IO.File]::ReadAllBytes($file)
    if($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $newBytes = $bytes[3..($bytes.Length-1)]
        [System.IO.File]::WriteAllBytes($file, $newBytes)
        Write-Host "✓ 已修复: $file"
    }
}
```

### 3. Git配置

项目已配置 `.gitattributes` 强制UTF-8编码：
```
*.java text diff=java encoding=utf-8
```

本地Git配置：
```bash
git config core.autocrlf input  # 已在项目中配置
```

## 🔧 配置文件说明

### .gitattributes
- 定义所有Java文件使用UTF-8编码
- 防止Git在不同系统间转换时引入编码问题
- 二进制文件标记为binary避免转换

### .editorconfig
- 统一所有编辑器的编码设置
- 明确指定 `charset = utf-8-bom` (禁止BOM)
- 支持VSCode、IntelliJ IDEA、Eclipse等主流IDE

### .idea/encodings.xml
- IDEA项目级编码配置
- 确保所有模块使用UTF-8

## 🐛 常见问题

### Q1: 为什么会出现 `非法字符: '\ufeff'` 错误？
**A:** 文件包含UTF-8 BOM字符。运行上述修复脚本移除BOM。

### Q2: 中文注释显示为问号或乱码？
**A:** 文件被用非UTF-8编码保存。需要：
1. 从Git恢复文件：`git checkout -- <file>`
2. 重新用UTF-8编码编辑

### Q3: 如何预防此类问题？
**A:** 
1. 始终使用IDEA等专业编辑器
2. 定期运行 `check-encoding.ps1` 检查
3. 提交前确认没有BOM文件
4. 团队成员统一IDE编码设置

## 📊 历史问题统计

- **2026-04-09**: 发现76个含BOM的文件，已全部修复
- **根本原因**: Git配置不当 + 部分文件被非UTF-8编辑器修改
- **解决方案**: 添加 `.gitattributes` + `.editorconfig` + 自动化检查脚本

## 👥 团队协作要求

1. **新成员加入时**：必须配置IDE编码为UTF-8
2. **代码审查时**：检查是否有BOM或乱码
3. **CI/CD流程**：建议添加编码检查步骤
4. **文档更新时**：确保Markdown文件也是UTF-8

---

**最后更新**: 2026-04-09  
**维护者**: shudl
