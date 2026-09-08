"""Export only a fully qualified, closed Worldline workshop and its native screenshots."""
from pathlib import Path
import hashlib
import json
import shutil
import zipfile

root = Path(__file__).resolve().parents[1]
run = Path((root / 'build/latest-proof.txt').read_text())
evidence = (run / 'runtime.properties').read_text()
if 'status=PASS' not in evidence or 'network-orientations\\=4' not in evidence:
    raise RuntimeError('A passing four-orientation delivery run is required')
receipt = json.loads((root / 'dist/package-receipt.json').read_text())
release = root / 'dist' / receipt['release']
if hashlib.sha256(release.read_bytes()).hexdigest() != receipt['sha256']:
    raise RuntimeError('Release bytes changed after packaging')
runtime = run / 'world/forge-f01/game'
with zipfile.ZipFile(release) as product, zipfile.ZipFile(runtime / 'mods/refinery-proof.jar') as tested:
    for name in product.namelist():
        if product.read(name) != tested.read(name):
            raise RuntimeError(f'Installed proof differs from delivered product: {name}')
source = runtime / 'saves/worldline-f01'
world = root / 'dist/retronism-refinery-world.zip'
with zipfile.ZipFile(world, 'w', zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(source.rglob('*')):
        if path.is_file() and path.name != 'session.lock':
            archive.write(path, 'RetronismRefineryWorkshop/' + path.relative_to(source).as_posix())
images = sorted((run / 'screenshots').glob('*.png'), key=lambda path: path.stat().st_mtime_ns)
if len(images) != 4:
    raise RuntimeError('Expected the two machine proofs and two workshop views')
for image, name in zip(images[-2:], ('retronism-refinery-front.png', 'retronism-refinery-rear.png')):
    shutil.copyfile(image, root / 'dist' / name)
summary = {'run': run.name, 'releaseSha256': receipt['sha256'], 'orientations': 4,
           'runtimeTicks': int(next(line.split('=', 1)[1] for line in evidence.splitlines() if line.startswith('ticks='))),
           'testedProductEntriesEqualRelease': True,
           'worldSha256': hashlib.sha256(world.read_bytes()).hexdigest()}
(root / 'dist/proof-receipt.json').write_text(json.dumps(summary, indent=2) + '\n')
print(json.dumps(summary, indent=2))
