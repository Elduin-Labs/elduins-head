"""Draws every Elduin's Head texture and the mod icon. Run from the repo root: python3 tools/textures.py

All drawn from scratch here (no Mojang art). Edit and run again.
"""
import math
import os
import struct
import zlib

HERE = os.path.dirname(__file__)
TEX = os.path.join(HERE, "..", "src", "main", "resources", "assets", "elduins_head", "textures", "block")


def write_png(path, pixels, w, h):
    raw = b"".join(b"\x00" + bytes(c for px in pixels[y * w:(y + 1) * w] for c in px) for y in range(h))

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def hash01(x, y, seed):
    n = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65535.0


def mix(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def head_wall():
    """Soft pink flesh with a few darker veins."""
    base, dark, light = (226, 132, 128), (196, 98, 104), (240, 160, 152)
    px = []
    for y in range(16):
        for x in range(16):
            r = hash01(x, y, 1)
            c = light if r < 0.25 else dark if r > 0.85 else base
            # two wiggly veins that tile across the edges
            if (y - int(3 * math.sin((x + 2) / 2.5))) % 16 in (4,) or (x + int(2 * math.sin(y / 2.0))) % 16 == 11:
                c = (178, 74, 92)
            px.append(c + (255,) if len(c) == 3 else c)
    return px


def brain():
    """Pink with winding darker wrinkles, like a real brain. Tiles across the edges."""
    base, light = (244, 156, 182), (252, 190, 208)
    groove = (190, 88, 128)
    px = []
    for y in range(16):
        for x in range(16):
            c = light if hash01(x, y, 2) < 0.2 else base
            a = math.sin(x * math.pi / 4)  # repeats every 8 pixels, so it tiles
            b = math.sin(y * math.pi / 4 + 1.3)
            wiggle_rows = (y - round(2.0 * a)) % 8 == 2
            wiggle_cols = (x - round(2.0 * b)) % 8 == 6 and (y % 8) in (3, 4, 5)
            if wiggle_rows or wiggle_cols:
                c = groove
            px.append(c + (255,))
    return px


def portal_frames(n=16):
    """A pink-and-purple swirl that turns. Frames stacked top to bottom."""
    a, b = (255, 120, 180), (170, 70, 200)
    px = []
    for f in range(n):
        turn = f / n * 2 * math.pi
        for y in range(16):
            for x in range(16):
                dx, dy = x - 7.5, y - 7.5
                r = math.hypot(dx, dy)
                ang = math.atan2(dy, dx)
                t = 0.5 + 0.5 * math.sin(ang * 2 + r * 0.8 - turn * 2)
                t = t * 0.85 + 0.15 * hash01(x, y, f + 3)
                px.append(mix(a, b, t))
    return px, 16 * n


def icon():
    """A big pink brain on a light blue background, 32x32 scaled up 8x."""
    n = 32
    px = [(0, 0, 0, 0)] * (n * n)
    bg, edge = (170, 214, 245, 255), (120, 170, 214, 255)
    for y in range(n):
        for x in range(n):
            corner = min(x, n - 1 - x) + min(y, n - 1 - y)
            if corner < 2:
                continue
            px[y * n + x] = edge if (x in (0, n - 1) or y in (0, n - 1) or corner == 2) else bg
    b = brain()
    cx, cy, rx, ry = 15.5, 15.0, 12.5, 10.0
    for y in range(n):
        for x in range(n):
            nx, ny = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
            d = nx * nx + ny * ny
            if d <= 1.0:
                c = b[(y % 16) * 16 + (x % 16)]
                if abs(x + 0.5 - cx) < 1.0 and y < cy + 6:
                    c = (176, 80, 118, 255)  # the middle groove
                if d > 0.82:
                    c = (186, 90, 128, 255)  # outline
                px[y * n + x] = c
    # brain stem
    for y in range(int(cy + ry) - 1, n - 3):
        for x in (15, 16):
            px[y * n + x] = (214, 120, 150, 255)
    # little sparks: it's thinking
    for (x, y) in [(5, 6), (26, 5), (28, 18), (3, 20)]:
        px[y * n + x] = (255, 250, 150, 255)
        px[y * n + x + 1] = (255, 250, 150, 255)
    return [px[(y // 8) * n + x // 8] for y in range(n * 8) for x in range(n * 8)], n * 8


if __name__ == "__main__":
    write_png(os.path.join(TEX, "head_wall.png"), head_wall(), 16, 16)
    write_png(os.path.join(TEX, "brain.png"), brain(), 16, 16)
    frames, h = portal_frames()
    write_png(os.path.join(TEX, "head_portal.png"), frames, 16, h)
    big, size = icon()
    write_png(os.path.join(HERE, "..", "src", "main", "resources", "assets", "icon.png"), big, size, size)
    print("done")
