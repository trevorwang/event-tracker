#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
from pathlib import Path
from PIL import Image

# macOS ICNS 完整尺寸（包含 @2x 尺寸）
ICON_SIZES = [
    (16, 16),
    (32, 32),      # 16@2x
    (32, 32),
    (64, 64),      # 32@2x
    (128, 128),
    (256, 256),    # 128@2x
    (256, 256),
    (512, 512),    # 256@2x
    (512, 512),
    (1024, 1024)   # 512@2x
]

def generate_icns(input_png: str, output_icns: str):
    input_path = Path(input_png)
    output_path = Path(output_icns)

    if not input_path.exists():
        print(f"错误: 文件 {input_png} 不存在")
        return

    img = Image.open(input_path).convert("RGBA")
    # 保存 icns，Pillow 会将 sizes 列表中的所有尺寸打包
    img.save(output_path, format='ICNS', sizes=ICON_SIZES)
    print(f"生成完成: {output_icns}")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("用法: python png2icns.py input.png output.icns")
        sys.exit(1)

    generate_icns(sys.argv[1], sys.argv[2])