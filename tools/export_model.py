"""Export the native reference without changing its textures, UV rectangles or scale."""
import argparse
import base64
import hashlib
import json
import math
from pathlib import Path
import struct

parser = argparse.ArgumentParser()
parser.add_argument('model', type=Path)
args = parser.parse_args()
raw = args.model.read_bytes()
model = json.loads(raw)
root = Path(__file__).resolve().parents[1]
out = root / 'src/main/resources/refinery'
out.mkdir(parents=True, exist_ok=True)
textures = model['textures']
groups = [[] for _ in textures]
collisions = [[] for _ in range(45)]

def rotate(point, element, normal=False):
    origin = [0, 0, 0] if normal else element.get('origin', [0, 0, 0])
    x, y, z = [point[i] - origin[i] for i in range(3)]
    for axis, degrees in enumerate(element.get('rotation', [0, 0, 0])):
        a = math.radians(degrees)
        c, s = math.cos(a), math.sin(a)
        if axis == 0:
            y, z = y*c - z*s, y*s + z*c
        elif axis == 1:
            x, z = x*c + z*s, -x*s + z*c
        else:
            x, y = x*c - y*s, x*s + y*c
    return [x + origin[0], y + origin[1], z + origin[2]]

for element in model['elements']:
    a, b, c = element['from']
    d, e, f = element['to']
    faces = {
        'north': ([0,0,-1], [(d,e,c),(a,e,c),(a,b,c),(d,b,c)]),
        'south': ([0,0,1], [(a,e,f),(d,e,f),(d,b,f),(a,b,f)]),
        'east': ([1,0,0], [(d,e,f),(d,e,c),(d,b,c),(d,b,f)]),
        'west': ([-1,0,0], [(a,e,c),(a,e,f),(a,b,f),(a,b,c)]),
        'up': ([0,1,0], [(a,e,c),(d,e,c),(d,e,f),(a,e,f)]),
        'down': ([0,-1,0], [(a,b,f),(d,b,f),(d,b,c),(a,b,c)])
    }
    corners = [rotate((x,y,z), element) for x in (a,d) for y in (b,e) for z in (c,f)]
    low = [min(p[i] for p in corners)/16 for i in range(3)]
    high = [max(p[i] for p in corners)/16 for i in range(3)]
    for cy in range(3):
        for cz in range(3):
            for cx in range(5):
                cell = [cx, cy, cz]
                lo = [max(0, low[i]-cell[i]) for i in range(3)]
                hi = [min(1, high[i]-cell[i]) for i in range(3)]
                if all(hi[i] > lo[i] for i in range(3)):
                    collisions[cy*15+cz*5+cx].append(lo+hi)
    for name, face in element['faces'].items():
        index = face.get('texture')
        if index is None:
            continue
        texture = textures[index]
        u, v, u2, v2 = face['uv']
        uv = [(u,v),(u2,v),(u2,v2),(u,v2)]
        shift = (face.get('rotation', 0) // 90) % 4
        uv = uv[-shift:] + uv[:-shift] if shift else uv
        normal, points = faces[name]
        values = rotate(normal, element, True)
        for point, coord in zip(points, uv):
            values += [n / 16 for n in rotate(point, element)]
            values += [coord[0] / texture['width'], coord[1] / texture['height']]
        groups[index].append(values)

for index, faces in enumerate(groups):
    # AeroModelLib ignores OBJ materials: one cached mesh per texture keeps each native atlas exact.
    lines = ['# Refinery: block units, origin at master cell [2,1,2].']
    vertex = 1
    for face in faces:
        corners = [face[3+i*5:8+i*5] for i in range(4)][::-1]
        for x, y, z, u, v in corners:
            lines.append(f'v {x-2:.9g} {y-1:.9g} {z-2:.9g}')
            lines.append(f'vt {u:.9g} {1-v:.9g}')
        lines.append('f ' + ' '.join(f'{v}/{v}' for v in range(vertex, vertex+4)))
        vertex += 4
    (out / f'model-{index}.obj').write_text('\n'.join(lines)+'\n', encoding='utf-8')
for i, texture in enumerate(textures):
    (out / f'texture-{i}.png').write_bytes(base64.b64decode(texture['source'].split(',', 1)[1]))
with (out / 'collision.bin').open('wb') as output:
    output.write(struct.pack('>I', 0x52464331))
    for boxes in collisions:
        output.write(struct.pack('>I', len(boxes)))
        for box in boxes:
            output.write(struct.pack('>6f', *box))
receipt = {
    'sourceSha256': hashlib.sha256(raw).hexdigest(),
    'cubes': len(model['elements']), 'faces': sum(map(len, groups)),
    'scale': '16 native pixels = 1 Minecraft block', 'boundsBlocks': [5, 3, 3],
    'renderer': 'AeroModelLib Aero_MeshRenderer.renderModelAtRest', 'masterLocal': [2,1,2],
    'textures': [{'name': t['name'], 'width': t['width'], 'height': t['height'],
                  'sha256': hashlib.sha256((out / f'texture-{i}.png').read_bytes()).hexdigest()}
                 for i, t in enumerate(textures)]
}
(out / 'model-receipt.json').write_text(json.dumps(receipt, indent=2) + '\n')
print(json.dumps(receipt, indent=2))
