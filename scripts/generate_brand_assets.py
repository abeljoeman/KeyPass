"""Generate deterministic Android bitmap assets from the approved RAHSA logo.

The master artwork lives at branding/logo_RAHSA.png. The in-app logo is PNG;
launcher assets are lossless WebP to match the repository's Android resources.
"""

from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "branding" / "logo_RAHSA.png"

DENSITIES = {
    "mdpi": (48, 108),
    "hdpi": (72, 162),
    "xhdpi": (96, 216),
    "xxhdpi": (144, 324),
    "xxxhdpi": (192, 432),
}

# Android launchers may enlarge adaptive foreground layers. Keep extra breathing
# room around the shield so it does not crowd the launcher mask on physical
# devices, while retaining a legible mark at every density.
LEGACY_LAUNCHER_OCCUPANCY = 0.78
ADAPTIVE_FOREGROUND_OCCUPANCY = 0.52


def load_clean_logo() -> Image.Image:
    logo = Image.open(SOURCE).convert("RGBA")
    alpha = logo.getchannel("A")
    visible = alpha.point(lambda value: 255 if value > 8 else 0)
    bounds = visible.getbbox()
    if bounds is None:
        raise ValueError(f"Logo has no visible pixels: {SOURCE}")
    alpha = alpha.point(lambda value: value if value > 8 else 0)
    logo.putalpha(alpha)
    return logo.crop(bounds)


def fit_on_square(artwork: Image.Image, size: int, occupancy: float) -> Image.Image:
    max_extent = round(size * occupancy)
    scale = min(max_extent / artwork.width, max_extent / artwork.height)
    resized = artwork.resize(
        (max(1, round(artwork.width * scale)), max(1, round(artwork.height * scale))),
        Image.Resampling.LANCZOS,
    )
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    offset = ((size - resized.width) // 2, (size - resized.height) // 2)
    canvas.alpha_composite(resized, offset)
    return canvas


def make_monochrome(logo: Image.Image) -> Image.Image:
    """Keep the blue shield as a mask and cut the white monogram/lock out."""
    result = Image.new("RGBA", logo.size, (255, 255, 255, 0))
    source_pixels = logo.load()
    result_pixels = result.load()
    for y in range(logo.height):
        for x in range(logo.width):
            red, green, blue, alpha = source_pixels[x, y]
            blue_separation = blue - max(red, green)
            mask_strength = max(0.0, min(1.0, (blue_separation - 5) / 40))
            result_pixels[x, y] = (255, 255, 255, round(alpha * mask_strength))
    return result


def write_png(image: Image.Image, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination, format="PNG", optimize=True)


def write_webp(image: Image.Image, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination, format="WEBP", lossless=True, method=6)


def main() -> None:
    logo = load_clean_logo()
    monochrome = make_monochrome(logo)

    write_png(
        fit_on_square(logo, size=512, occupancy=0.90),
        ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi" / "logo_rahsa.png",
    )

    for build_type in ("debug", "release"):
        for density, (legacy_size, foreground_size) in DENSITIES.items():
            directory = ROOT / "app" / "src" / build_type / "res" / f"mipmap-{density}"
            for legacy_name in ("ic_launcher", "ic_launcher_round"):
                write_webp(
                    fit_on_square(logo, legacy_size, occupancy=LEGACY_LAUNCHER_OCCUPANCY),
                    directory / f"{legacy_name}.webp",
                )
            write_webp(
                fit_on_square(
                    logo,
                    foreground_size,
                    occupancy=ADAPTIVE_FOREGROUND_OCCUPANCY,
                ),
                directory / "ic_launcher_foreground.webp",
            )
            write_webp(
                fit_on_square(
                    monochrome,
                    foreground_size,
                    occupancy=ADAPTIVE_FOREGROUND_OCCUPANCY,
                ),
                directory / "ic_launcher_monochrome.webp",
            )


if __name__ == "__main__":
    main()
