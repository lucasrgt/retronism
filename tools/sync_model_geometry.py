"""Copy native geometry changes while retaining this game's established material/UV palette."""
import argparse
import copy
import json
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument('native', type=Path)
parser.add_argument('--provenance', type=Path, help='Native cut receipt mapping new fragments to their original material source')
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
path = root / 'assets/refinery.bbmodel'
game = json.loads(path.read_text(encoding='utf-8'))
native = json.loads(args.native.read_text(encoding='utf-8'))
existing = {element['uuid']: element for element in game['elements']}
incoming = {element['uuid']: element for element in native['elements']}
missing = existing.keys() - incoming.keys()
if missing:
    raise ValueError(f'Native model unexpectedly removed {len(missing)} game elements')
original_textures = copy.deepcopy(game['textures'])
original_faces = {key: copy.deepcopy(element['faces']) for key, element in existing.items()}
fragment_sources = {}
if args.provenance:
    receipt = json.loads(args.provenance.read_text(encoding='utf-8'))
    fragment_sources = {part: entry['source'] for entry in receipt['provenance'] for part in entry['parts']}
texture_map = {}
for i, texture in enumerate(native['textures']):
    matches = [j for j, candidate in enumerate(game['textures']) if candidate['name'] == texture['name']]
    if len(matches) != 1 or game['textures'][matches[0]]['source'] != texture['source']:
        raise ValueError(f"Native texture does not match the game's palette: {texture['name']}")
    texture_map[i] = matches[0]

def groups(nodes):
    for node in nodes:
        if isinstance(node, dict):
            yield node
            yield from groups(node.get('children', []))

parents = {child: group['uuid'] for group in groups(native['outliner'])
           for child in group['children'] if isinstance(child, str)}
game_groups = {group['uuid']: group for group in groups(game['outliner'])}
added = 0
for key, element in incoming.items():
    if key in existing:
        for field in ('from', 'to', 'origin', 'rotation'):
            if field in element:
                existing[key][field] = copy.deepcopy(element[field])
            else:
                existing[key].pop(field, None)
        continue
    if parents.get(key) not in game_groups:
        raise ValueError(f'New geometry has an unknown group: {element["name"]}')
    new = copy.deepcopy(element)
    if key in fragment_sources:
        new['faces'] = copy.deepcopy(original_faces[fragment_sources[key]])
    else:
        for face in new['faces'].values():
            if face.get('texture') is not None:
                face['texture'] = texture_map[face['texture']]
    game['elements'].append(new)
    game_groups[parents[key]]['children'].append(key)
    added += 1
game['pixel_harness']['ports'] = copy.deepcopy(native['pixel_harness']['ports'])
for port in game['pixel_harness']['ports']:
    # This Retronism recipe uses item pipes; the modeling study can retain fluid labels.
    if port['id'] != 'energy_input':
        port['type'] = 'item'
    port['code_ref'] = 'refinery.Ports'
assert game['textures'] == original_textures
assert all(existing[key]['faces'] == faces for key, faces in original_faces.items())
path.write_text(json.dumps(game, ensure_ascii=False, separators=(',', ':')), encoding='utf-8')
print(f'Synchronized {len(existing)} existing elements and {added} new elements; original textures and UVs preserved')
