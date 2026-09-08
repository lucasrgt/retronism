"""Preserve Retronism's legacy flat game package without copying library sources."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
target = root / 'build/generated-java'
target.mkdir(parents=True, exist_ok=True)
for path in (root / 'src/retronism').rglob('*.java'):
    if 'aerotest' in path.parts or path.name in ('mod_RetronismAeroTest.java', 'Retronism_BlockVFXDemo.java'):
        continue
    source = re.sub(r'(?m)^package [\w.]+;', 'package net.minecraft.src;', path.read_text())
    source = re.sub(r'(?m)^import (?:static )?retronism\.[^;]+;\n?', '', source)
    (target / path.name).write_text(source)
