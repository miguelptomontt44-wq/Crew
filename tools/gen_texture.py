"""Genera resourcepack/assets/crews/textures/font/menu.png (176x222) estilo GUI vanilla."""
import zlib, struct, sys

W, H = 176, 222
px = [[(0, 0, 0, 0)] * W for _ in range(H)]

def rect(x, y, w, h, c):
    for j in range(y, y + h):
        for i in range(x, x + w):
            px[j][i] = c

BLACK = (0, 0, 0, 255); WHITE = (255, 255, 255, 255)
GRAY = (198, 198, 198, 255); DARK = (85, 85, 85, 255)
SLOT_BG = (139, 139, 139, 255); SLOT_DK = (55, 55, 55, 255)

rect(0, 0, W, H, BLACK)
rect(1, 1, W - 2, H - 2, GRAY)
rect(1, 1, W - 3, 2, WHITE); rect(1, 1, 2, H - 3, WHITE)          # bisel claro
rect(3, H - 3, W - 4, 2, DARK); rect(W - 3, 3, 2, H - 4, DARK)    # bisel oscuro
for (x, y) in [(0, 0), (W - 1, 0), (0, H - 1), (W - 1, H - 1)]:   # esquinas
    px[y][x] = (0, 0, 0, 0)

def slot(x, y):  # x,y = esquina de la caja 18x18
    rect(x, y, 18, 18, SLOT_BG)
    rect(x, y, 17, 1, SLOT_DK); rect(x, y, 1, 17, SLOT_DK)
    rect(x + 1, y + 17, 17, 1, WHITE); rect(x + 17, y + 1, 1, 17, WHITE)

for r in range(6):
    for c in range(9):
        slot(7 + 18 * c, 17 + 18 * r)
for r in range(3):
    for c in range(9):
        slot(7 + 18 * c, 139 + 18 * r)
for c in range(9):
    slot(7 + 18 * c, 197)

raw = b"".join(b"\x00" + bytes(v for p in row for v in p) for row in px)
def chunk(t, d):
    c = struct.pack(">I", len(d)) + t + d
    return c + struct.pack(">I", zlib.crc32(t + d) & 0xffffffff)
png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0)) \
      + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
out = sys.argv[1] if len(sys.argv) > 1 else "resourcepack/assets/crews/textures/font/menu.png"
open(out, "wb").write(png)
print("ok", out)
