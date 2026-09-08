"""Package only owned and MIT dependency classes after RetroMCP reobfuscation."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('runtime', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
reobf = args.runtime / 'minecraft/reobf'
release = root / 'dist/retronism-0.2.0-b1.7.3.jar'
product = {}
for source in (root / 'build/classes').rglob('*.class'):
    name = source.relative_to(root / 'build/classes').as_posix()
    if name.startswith('net/minecraft/src/') and name.rsplit('/', 1)[-1].startswith(('Retronism_', 'mod_Retronism')):
        name = name.rsplit('/', 1)[-1]
    if not (name.startswith(('aero/modellib/', 'aero/machineapi/', 'refinery/', 'Retronism_')) or name == 'mod_Retronism.class'):
        raise RuntimeError(f'Unexpected product class: {name}')
    product[name] = (reobf / name).read_bytes()
for source in (root / 'src/retronism/assets').rglob('*'):
    if source.is_file():
        product[source.relative_to(root / 'src/retronism/assets').as_posix()] = source.read_bytes()
for source in (root / 'src/main/resources').rglob('*'):
    if source.is_file():
        product[source.relative_to(root / 'src/main/resources').as_posix()] = source.read_bytes()
product['AEROMODELLIB-LICENSE.md'] = (root / 'build/classes/AEROMODELLIB-LICENSE.md').read_bytes()
if (root / 'CREDITS.md').is_file():
    product['CREDITS.md'] = (root / 'CREDITS.md').read_bytes()

def archive(path, entries):
    path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED) as output:
        for name, content in sorted(entries.items()):
            info = zipfile.ZipInfo(name, (2026, 9, 7, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            output.writestr(info, content)

archive(release, product)
# A separate ignored runtime uses the shipped obfuscated bytes and original historical loader.
obf = root / '.local/obfuscated-runtime'
shutil.copytree(args.runtime / 'libraries', obf / 'libraries', dirs_exist_ok=True)
with zipfile.ZipFile(args.runtime / 'jars/minecraft.jar') as source:
    base = {name: source.read(name) for name in source.namelist()
            if not name.endswith('/') and not name.upper().startswith('META-INF/')}
for name in ['net/minecraft/client/Minecraft.class', 'px.class', 'n.class', 'dk.class']:
    base[name] = (reobf / name).read_bytes()
archive(obf / 'minecraft.jar', base)
fixture = dict(product)
for name in ['mod_RefineryProof.class', 'RefineryFixture.class', 'RefineryRenderProof.class', 'RetronismNetworkProof.class', 'RetronismNetworkScene.class', 'mod_WorldlineTestKitProbe.class']:
    fixture[name] = (reobf / name).read_bytes()
for source in (reobf / 'worldline').rglob('*.class'):
    fixture[source.relative_to(reobf).as_posix()] = source.read_bytes()
archive(obf / 'refinery-proof.jar', fixture)
classpath = ['minecraft.jar'] + [path.relative_to(obf).as_posix() for path in sorted((obf / 'libraries').rglob('*.jar')) if not path.name.endswith('-sources.jar')]
manifest = ['schema=worldline.legacy-testkit-client.v1', 'loader=forge', 'natives=libraries/natives',
            'probe.source=refinery-proof.jar', 'probe.target=mods/refinery-proof.jar', f'classpath.count={len(classpath)}']
manifest += [f'classpath.{i+1}={path}' for i, path in enumerate(classpath)]
(obf / 'worldline-testkit.properties').write_text('\n'.join(manifest)+'\n')
receipt = {'release': release.name, 'sha256': hashlib.sha256(release.read_bytes()).hexdigest(),
           'classCount': sum(name.endswith('.class') for name in product),
           'minecraftClassesIncluded': False, 'testClassesIncluded': False,
           'aeroModelLibIncluded': True, 'obfuscatedRuntime': str(obf)}
(root / 'dist/package-receipt.json').write_text(json.dumps(receipt, indent=2)+'\n')
print(json.dumps(receipt, indent=2))
