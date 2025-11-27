#!/usr/bin/env python3
"""
Universal Flag PNG Generator for Android
Converts any SVG flag to PNG files in all Android density buckets
"""

import subprocess
import os
import sys
import argparse

# Define the sizes for each density bucket (width x height)
# Regular flags are square (matching ua.png and de.png)
REGULAR_SIZES = {
    'mdpi': (24, 24),
    'hdpi': (36, 36),
    'xhdpi': (48, 48),
    'xxhdpi': (72, 72),
    'xxxhdpi': (96, 96)
}

# Large flags are also square but bigger
LARGE_SIZES = {
    'mdpi': (64, 64),
    'hdpi': (96, 96),
    'xhdpi': (128, 128),
    'xxhdpi': (192, 192),
    'xxxhdpi': (256, 256)
}

# Output paths
OUTPUT_BASE = 'app/src/main/res'

def get_svg_aspect_ratio(svg_path):
    """Parse SVG width and height to get aspect ratio"""
    import xml.etree.ElementTree as ET
    tree = ET.parse(svg_path)
    root = tree.getroot()
    width = int(root.attrib.get('width', '900'))
    height = int(root.attrib.get('height', '600'))
    return width, height, width / height

def convert_svg_to_png(svg_path, output_path, width, height, preserve_aspect=False):
    """Convert SVG to PNG using Inkscape, fallback to ImageMagick if needed, optionally preserving aspect ratio"""
    if preserve_aspect:
        svg_w, svg_h, svg_ratio = get_svg_aspect_ratio(svg_path)
        out_ratio = width / height
        # If output is square, create a 3:2 canvas, center the flag, add padding
        if abs(out_ratio - svg_ratio) > 0.01:
            pad_w = int(height * svg_ratio)
            pad_h = height
            if pad_w < width:
                pad_w = width
            temp_path = output_path + '.tmp.png'
            try:
                subprocess.run([
                    'inkscape',
                    svg_path,
                    '--export-type=png',
                    f'--export-filename={temp_path}',
                    f'--export-width={pad_w}',
                    f'--export-height={pad_h}'
                ], check=True, capture_output=True)
            except Exception:
                # Fallback to ImageMagick if Inkscape fails
                try:
                    subprocess.run([
                        'convert',
                        '-background', 'none',
                        '-resize', f'{pad_w}x{pad_h}',
                        svg_path,
                        temp_path
                    ], check=True, capture_output=True)
                except Exception:
                    return False
            from PIL import Image
            im = Image.open(temp_path)
            new_im = Image.new('RGBA', (width, height), (0,0,0,0))
            x = (width - pad_w) // 2
            y = (height - pad_h) // 2
            new_im.paste(im, (x, y))
            new_im.save(output_path)
            os.remove(temp_path)
            print(f"✅ Generated {output_path} (aspect ratio preserved)")
            return True
    # Default: aspect ratio not preserved, normal export
    try:
        subprocess.run([
            'inkscape',
            svg_path,
            '--export-type=png',
            f'--export-filename={output_path}',
            f'--export-width={width}',
            f'--export-height={height}'
        ], check=True, capture_output=True)
        print(f"✅ Generated {output_path} ({width}x{height}px) using Inkscape")
        return True
    except Exception:
        # Fallback to ImageMagick if Inkscape fails
        try:
            subprocess.run([
                'convert',
                '-background', 'none',
                '-resize', f'{width}x{height}',
                svg_path,
                output_path
            ], check=True, capture_output=True)
            print(f"✅ Generated {output_path} ({width}x{height}px) using ImageMagick")
            return True
        except Exception:
            return False

def main():
    parser = argparse.ArgumentParser(
        description='Generate Android flag PNGs from SVG files',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s uk.svg uk          # Generate uk.png and large_flag_uk.png
  %(prog)s de.svg de          # Generate de.png and large_flag_de.png
  %(prog)s flag.svg jp --no-large  # Generate only jp.png (no large version)
        """
    )

    parser.add_argument('svg_file', help='Path to the SVG flag file')
    parser.add_argument('flag_code', help='Flag code (e.g., uk, de, ua, jp)')
    parser.add_argument('--no-regular', action='store_true',
                       help='Skip generating regular flag')
    parser.add_argument('--no-large', action='store_true',
                       help='Skip generating large flag')
    parser.add_argument('--output-base', default=OUTPUT_BASE,
                       help=f'Output base directory (default: {OUTPUT_BASE})')
    parser.add_argument('--preserve-aspect', action='store_true',
                       help='Preserve SVG aspect ratio (add padding for square PNGs)')

    args = parser.parse_args()

    svg_path = args.svg_file
    flag_code = args.flag_code
    output_base = args.output_base
    preserve_aspect = args.preserve_aspect

    # Validate input
    if not os.path.exists(svg_path):
        print(f"❌ Error: SVG file '{svg_path}' not found")
        return 1

    if not svg_path.lower().endswith('.svg'):
        print(f"⚠️  Warning: '{svg_path}' doesn't have .svg extension")

    print(f"🎨 Generating {flag_code.upper()} flag PNGs from {svg_path}\n")

    total_success = 0
    total_count = 0

    # Generate regular flags
    if not args.no_regular:
        print(f"📝 Generating regular {flag_code}.png flags...\n")
        for density, (width, height) in REGULAR_SIZES.items():
            output_dir = os.path.join(output_base, f'drawable-{density}')
            output_file = os.path.join(output_dir, f'{flag_code}.png')

            if not os.path.exists(output_dir):
                os.makedirs(output_dir)
                print(f"📁 Created directory: {output_dir}")

            if convert_svg_to_png(svg_path, output_file, width, height, preserve_aspect):
                total_success += 1
            else:
                print(f"❌ Failed to generate {output_file}")
            total_count += 1

    # Generate large flags
    if not args.no_large:
        print(f"\n📝 Generating large_flag_{flag_code}.png flags...\n")
        for density, (width, height) in LARGE_SIZES.items():
            output_dir = os.path.join(output_base, f'drawable-{density}')
            output_file = os.path.join(output_dir, f'large_flag_{flag_code}.png')

            if not os.path.exists(output_dir):
                os.makedirs(output_dir)
                print(f"📁 Created directory: {output_dir}")

            if convert_svg_to_png(svg_path, output_file, width, height, preserve_aspect):
                total_success += 1
            else:
                print(f"❌ Failed to generate {output_file}")
            total_count += 1

    print(f"\n✅ Successfully generated {total_success}/{total_count} PNG files")

    if total_success == 0:
        print("\n⚠️  No conversion tools found. Please install one of:")
        print("  - cairosvg: pip3 install cairosvg")
        print("  - Inkscape: brew install inkscape")
        print("  - librsvg: brew install librsvg")
        return 1

    return 0

if __name__ == '__main__':
    sys.exit(main())
