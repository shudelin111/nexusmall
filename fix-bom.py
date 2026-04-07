#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Fix UTF-8 BOM issue in Java files
"""
import os
import sys

def fix_bom(file_path):
    """Remove BOM from file if present"""
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return False
    
    with open(file_path, 'rb') as f:
        content = f.read()
    
    # Check if BOM is present
    if content.startswith(b'\xef\xbb\xbf'):
        # Remove BOM
        content = content[3:]
        with open(file_path, 'wb') as f:
            f.write(content)
        print(f"OK - BOM removed: {os.path.basename(file_path)}")
        return True
    else:
        print(f"OK - No BOM: {os.path.basename(file_path)}")
        return False

if __name__ == '__main__':
    files = [
        r'nexusmall-auth\src\main\java\com\nexusmall\auth\util\RsaKeyGenerator.java',
        r'nexusmall-auth\src\main\java\com\nexusmall\auth\util\JwtUtil.java'
    ]
    
    print("=== Fixing UTF-8 BOM ===")
    for file_path in files:
        try:
            fix_bom(file_path)
        except Exception as e:
            print(f"ERROR: {file_path} - {e}")
    
    print("\nDone!")
