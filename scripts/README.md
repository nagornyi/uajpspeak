# Scripts

Utility scripts for the SayUA project.

## generate_flag_pngs.py

Universal flag PNG generator for Android. Converts SVG flags to PNG files in all Android density buckets.

### Usage

```bash
cd scripts
python3 ./generate_flag_pngs.py <svg_file> <flag_code> [options]
```

### Arguments

- `svg_file` - Path to the SVG flag file
- `flag_code` - Flag code (e.g., uk, de, ua, jp)

### Options

- `--no-regular` - Skip generating regular flag (e.g., uk.png)
- `--no-large` - Skip generating large flag (e.g., large_flag_uk.png)
- `--output-base` - Output base directory (default: app/src/main/res)
- `--preserve-aspect` - Preserve SVG aspect ratio (add padding for square PNGs)

### Examples

Generate both regular and large UK flags:
```bash
python3 ./generate_flag_pngs.py united-kingdom.svg uk
```

Generate only regular German flags:
```bash
python3 ./generate_flag_pngs.py germany.svg de --no-large
```

Preserve aspect ratio for Japanese flag:
```bash
python3 ./generate_flag_pngs.py japan.svg jp --preserve-aspect
```

### Requirements

One of the following tools must be installed (works on macOS and Linux):
- **cairosvg** (Python library): `pip3 install cairosvg`
- **Inkscape**: `brew install inkscape` (macOS) or `sudo apt install inkscape` (Linux)
- **ImageMagick**: `brew install imagemagick` (macOS) or `sudo apt install imagemagick` (Linux)
- **librsvg**: `brew install librsvg` (macOS) or `sudo apt install librsvg2-bin` (Linux)

### Output

The script generates PNG files in the following sizes:

**Regular flags** (e.g., uk.png, de.png):
- drawable-mdpi: 24×24px
- drawable-hdpi: 36×36px
- drawable-xhdpi: 48×48px
- drawable-xxhdpi: 72×72px
- drawable-xxxhdpi: 96×96px

**Large flags** (e.g., large_flag_uk.png, large_flag_de.png):
- drawable-mdpi: 64×64px
- drawable-hdpi: 96×96px
- drawable-xhdpi: 128×128px
- drawable-xxhdpi: 192×192px
- drawable-xxxhdpi: 256×256px
