#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
全面检查所有模块的环境变量配置
"""

import os
import re
import yaml
import glob

def extract_env_vars_from_file(file_path):
    """从文件中提取所有 ${VAR_NAME:default} 格式的环境变量"""
    env_vars = set()
    
    if not os.path.exists(file_path):
        return env_vars
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    pattern = r'\$\{([A-Z_][A-Z0-9_]*)(?::[^}]*)?\}'
    matches = re.findall(pattern, content)
    
    for match in matches:
        env_vars.add(match)
    
    return env_vars

def read_deploy_config(configmap_path, secret_path):
    """读取 deploy 目录中的 ConfigMap 和 Secret"""
    configured_vars = set()
    
    if os.path.exists(configmap_path):
        with open(configmap_path, 'r', encoding='utf-8') as f:
            try:
                config = yaml.safe_load(f)
                if config and 'data' in config:
                    configured_vars.update(config['data'].keys())
            except Exception as e:
                print(f"   Warning: 读取 ConfigMap 失败: {e}")
    
    if os.path.exists(secret_path):
        with open(secret_path, 'r', encoding='utf-8') as f:
            try:
                config = yaml.safe_load(f)
                if config and 'data' in config:
                    configured_vars.update(config['data'].keys())
            except Exception as e:
                print(f"   Warning: 读取 Secret 失败: {e}")
    
    return configured_vars

def check_module(module_name, module_dir, deploy_dir, nacos_configs_dir):
    """检查单个模块"""
    all_env_vars = set()
    sources = []
    
    # 1. application.yaml
    app_yaml = os.path.join(module_dir, 'src', 'main', 'resources', 'application.yaml')
    if os.path.exists(app_yaml):
        vars = extract_env_vars_from_file(app_yaml)
        if vars:
            all_env_vars.update(vars)
            sources.append(("application.yaml", vars))
    
    # 2. bootstrap.yml
    bootstrap_yml = os.path.join(module_dir, 'src', 'main', 'resources', 'bootstrap.yml')
    if os.path.exists(bootstrap_yml):
        vars = extract_env_vars_from_file(bootstrap_yml)
        if vars:
            all_env_vars.update(vars)
            sources.append(("bootstrap.yml", vars))
    
    # 3. Nacos 配置文件
    nacos_files = [
        os.path.join(nacos_configs_dir, f"{module_name}-dev.yaml"),
        os.path.join(nacos_configs_dir, f"{module_name}-test.yaml"),
        os.path.join(nacos_configs_dir, f"{module_name}-prod.yaml"),
        os.path.join(nacos_configs_dir, f"{module_name}.yaml"),
    ]
    
    for nacos_file in nacos_files:
        if os.path.exists(nacos_file):
            vars = extract_env_vars_from_file(nacos_file)
            if vars:
                all_env_vars.update(vars)
                sources.append((os.path.basename(nacos_file), vars))
    
    if not all_env_vars:
        return True, []
    
    # 读取 deploy 配置
    configmap_path = os.path.join(deploy_dir, 'configmap.yaml')
    secret_path = os.path.join(deploy_dir, 'secret.yaml')
    configured_vars = read_deploy_config(configmap_path, secret_path)
    
    missing_vars = all_env_vars - configured_vars
    
    if missing_vars:
        missing_details = []
        for var in sorted(missing_vars):
            source_files = []
            for source_name, vars in sources:
                if var in vars:
                    source_files.append(source_name)
            missing_details.append((var, source_files))
        return False, missing_details
    else:
        return True, []

def main():
    base_dir = "D:/IdeaProjects/nexusmall"
    nacos_configs_dir = os.path.join(base_dir, "docs/nacos-configs")
    deploy_base = os.path.join(base_dir, "deploy")
    
    module_dirs = sorted(glob.glob(os.path.join(base_dir, "nexusmall-*")))
    
    results = {}
    
    for module_dir in module_dirs:
        module_name = os.path.basename(module_dir)
        
        if 'common' in module_name:
            continue
        
        service_name = module_name.replace('nexusmall-', '') + '-service'
        deploy_dir = os.path.join(deploy_base, service_name)
        
        if not os.path.exists(deploy_dir):
            continue
        
        passed, missing_details = check_module(module_name, module_dir, deploy_dir, nacos_configs_dir)
        results[module_name] = (passed, missing_details)
    
    # 输出结果
    print("="*70)
    print("环境变量配置检查结果")
    print("="*70)
    
    total = len(results)
    passed = sum(1 for v, _ in results.values() if v)
    failed = total - passed
    
    print(f"\n检查模块总数: {total}")
    print(f"通过: {passed}")
    print(f"失败: {failed}")
    
    if failed > 0:
        print(f"\n{'='*70}")
        print("失败的模块详情:")
        print(f"{'='*70}")
        
        for module_name, (passed, missing_details) in sorted(results.items()):
            if not passed:
                print(f"\n❌ {module_name} (缺失 {len(missing_details)} 个变量):")
                for var, sources in missing_details:
                    print(f"   - {var}")
                    print(f"     来源: {', '.join(sources)}")
    else:
        print("\n✅ 所有模块的环境变量配置都完整！")

if __name__ == '__main__':
    main()
