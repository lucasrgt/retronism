"""Asset-level regression checks for centered connectors and unobstructed mouths."""
import hashlib
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
MODEL = json.loads((ROOT / 'assets/refinery.bbmodel').read_text(encoding='utf-8'))
ELEMENTS = MODEL['elements']
PORTS = {port['id']: port for port in MODEL['pixel_harness']['ports']}

class RefineryPorts(unittest.TestCase):
    def test_visible_rings_are_centered_on_runtime_cells(self):
        rings = [('feed_left', 'Feed inlet left flange', [0, 8, 24], 0),
                 ('feed_right', 'Feed inlet right flange', [80, 8, 24], 0),
                 ('product_output', 'Product outlet flange', [40, 8, 48], 2),
                 ('energy_input', 'Rear energy socket ', [40, 24, 0], 2)]
        for port, prefix, expected, axis in rings:
            with self.subTest(port=port):
                parts = [e for e in ELEMENTS if e['name'].startswith(prefix) and not e['name'].endswith('recess')]
                self.assertEqual(len(parts), 4)
                low = [min(e['from'][i] for e in parts) for i in range(3)]
                high = [max(e['to'][i] for e in parts) for i in range(3)]
                for i in range(3):
                    if i != axis:
                        self.assertEqual((low[i] + high[i]) / 2, expected[i])
                self.assertIn(expected[axis], (low[axis], high[axis]))

    def test_no_structure_blocks_the_open_volume(self):
        mouths = [([0,6,22], [1,10,26]), ([79,6,22], [80,10,26]),
                  ([38,6,47], [42,10,48]), ([38,22,0], [42,26,2])]
        for low, high in mouths:
            with self.subTest(mouth=low):
                blocked = [e['name'] for e in ELEMENTS if all(e['from'][i] < high[i] and e['to'][i] > low[i] for i in range(3))]
                self.assertEqual(blocked, [])

    def test_semantics_follow_retronism_transfer_types_and_faces(self):
        for port, cell, face in [('feed_left', [0,0,1], 'west'), ('feed_right', [4,0,1], 'east'),
                                 ('product_output', [2,0,2], 'south'), ('energy_input', [2,1,0], 'north')]:
            self.assertEqual(PORTS[port]['cell'], cell)
            self.assertEqual(PORTS[port]['face'], face)
            self.assertEqual(PORTS[port]['type'], 'energy' if port == 'energy_input' else 'item')

    def test_export_matches_native_source_and_contains_every_face(self):
        receipt = json.loads((ROOT / 'src/main/resources/refinery/model-receipt.json').read_text())
        self.assertEqual(receipt['sourceSha256'], hashlib.sha256((ROOT / 'assets/refinery.bbmodel').read_bytes()).hexdigest())
        self.assertEqual(receipt['cubes'], len(ELEMENTS))
        self.assertEqual(len(ELEMENTS), 483)
        faces = sum((ROOT / f'src/main/resources/refinery/model-{i}.obj').read_text().count('\nf ') for i in range(4))
        self.assertEqual(faces, len(ELEMENTS) * 6)
        for name in ['model-receipt.json'] + [f'model-{i}.obj' for i in range(4)]:
            self.assertNotIn(b'\r\n', (ROOT / 'src/main/resources/refinery' / name).read_bytes())

if __name__ == '__main__':
    unittest.main()
