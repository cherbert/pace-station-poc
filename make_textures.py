#!/usr/bin/env python3
"""Generates the mod's placeholder textures. Requires Pillow.
Run from the project root: python3 make_textures.py
"""
import random
from PIL import Image, ImageDraw

random.seed(42)
TEX = "src/main/resources/assets/spacemod/textures"


def noisy_fill(img, box, base, spread=8, cell=2):
    d = ImageDraw.Draw(img)
    x0, y0, x1, y1 = box
    for y in range(y0, y1, cell):
        for x in range(x0, x1, cell):
            j = random.randint(-spread, spread)
            c = tuple(max(0, min(255, v + j)) for v in base)
            d.rectangle([x, y, min(x + cell - 1, x1 - 1), min(y + cell - 1, y1 - 1)], fill=c + (255,))


# ---- gravity_generator.png (16x16) ----
img = Image.new("RGBA", (16, 16))
noisy_fill(img, (0, 0, 16, 16), (52, 58, 76), spread=6, cell=2)
d = ImageDraw.Draw(img)
d.rectangle([0, 0, 15, 15], outline=(94, 102, 124, 255))
d.rectangle([1, 1, 14, 14], outline=(74, 80, 100, 255))
# glowing core
d.rectangle([5, 5, 10, 10], fill=(0, 190, 235, 255))
d.rectangle([6, 6, 9, 9], fill=(120, 235, 255, 255))
d.rectangle([7, 7, 8, 8], fill=(235, 255, 255, 255))
# corner rivets
for x, y in [(2, 2), (13, 2), (2, 13), (13, 13)]:
    d.point((x, y), fill=(160, 168, 188, 255))
img.save(f"{TEX}/block/gravity_generator.png")

# ---- item/ship.png (16x16) ----
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
hull = (168, 174, 186, 255)
dark = (110, 116, 130, 255)
# side view: nose to the right
d.polygon([(1, 8), (11, 6), (14, 8), (11, 10)], fill=hull, outline=dark)
d.rectangle([2, 9, 6, 11], fill=dark)            # skid
d.rectangle([1, 6, 3, 8], fill=dark)             # engine
d.point((2, 7), (255, 140, 60, 255))             # engine glow
d.rectangle([10, 7, 11, 8], fill=(80, 210, 240, 255))  # cockpit window
img.save(f"{TEX}/item/ship.png")

# ---- entity/ship.png (128x128) ----
img = Image.new("RGBA", (128, 128))
noisy_fill(img, (0, 0, 128, 128), (165, 170, 182), spread=7, cell=2)
d = ImageDraw.Draw(img)
# panel seams
for _ in range(40):
    x = random.randint(0, 120)
    y = random.randint(0, 120)
    if random.random() < 0.5:
        d.line([(x, y), (min(127, x + random.randint(4, 14)), y)], fill=(128, 133, 146, 255))
    else:
        d.line([(x, y), (x, min(127, y + random.randint(4, 14)))], fill=(128, 133, 146, 255))
# cockpit region -> glassy cyan
noisy_fill(img, (88, 0, 125, 14), (70, 195, 230), spread=10, cell=2)
d.rectangle([88, 0, 124, 13], outline=(40, 120, 150, 255))
# nose region -> hull with a red stripe
noisy_fill(img, (0, 36, 32, 50), (172, 176, 188), spread=6, cell=2)
d.rectangle([0, 40, 31, 43], fill=(198, 62, 54, 255))
# wing region -> darker gray with red tip stripe
noisy_fill(img, (32, 36, 80, 51), (140, 145, 157), spread=6, cell=2)
d.rectangle([32, 48, 79, 50], fill=(198, 62, 54, 255))
# engine region -> dark metal
noisy_fill(img, (80, 36, 103, 48), (62, 64, 74), spread=6, cell=2)
# skid region -> mid dark gray
noisy_fill(img, (0, 50, 47, 77), (94, 99, 110), spread=6, cell=2)
img.save(f"{TEX}/entity/ship.png")

print("textures written")
