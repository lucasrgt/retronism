#!/bin/bash
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE"

echo "=== Transpiling ==="
bash scripts/transpile.sh

echo "=== Building ==="
cd "$BASE/mcp"
echo "build" | java -jar RetroMCP-Java-CLI.jar
cd "$BASE"

echo "=== Preparing test jar ==="
cp tests/data/minecraft_test.jar tests/data/minecraft_run.jar

# Clean reobf: keep only mod classes + vanilla mods we explicitly patch.
# This prevents recompiled vanilla classes from overwriting the TMI/SPC-patched
# ones already baked into minecraft_test.jar.
REOBF="$BASE/mcp/minecraft/reobf"
if [ -d "$REOBF" ]; then
    (
        cd "$REOBF"
        for f in *.class; do
            [ -f "$f" ] || continue
            case "$f" in
                Retronism_*|Aero_*|AeroTest_*|mod_*|EntityRendererProxy*|dn.class|nw.class) ;;
                *) rm -f "$f" ;;
            esac
        done
    )
    # Inject mod classes (flat, default package) into run jar.
    # Single jar call with glob; only run if there is anything to inject.
    KEPT_COUNT=$(find "$REOBF" -maxdepth 1 -name "*.class" | wc -l)
    if [ "$KEPT_COUNT" -gt 0 ]; then
        (cd "$REOBF" && jar uf "$BASE/tests/data/minecraft_run.jar" *.class)
        echo "Injected $KEPT_COUNT mod classes"
    fi
fi

# Inject custom textures (and models) into run jar
if [ -d "$BASE/temp/merged" ]; then
    cd "$BASE/temp/merged"
    jar uf "$BASE/tests/data/minecraft_run.jar" .
    echo "Injected custom textures and models"
fi

cd "$BASE"

mkdir -p "$BASE/tests/data/tmp"
echo "=== Launching Minecraft ==="
LIBS="mcp/libraries"
java -Xms1024M -Xmx1024M \
  -Daero.dev=true \
  -Djava.library.path="$LIBS/natives" \
  -Djava.io.tmpdir="$BASE/tests/data/tmp" \
  -cp "tests/data/minecraft_run.jar;$LIBS/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar;$LIBS/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar;$LIBS/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar;$LIBS/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar;$LIBS/com/paulscode/codecjorbis/20230120/codecjorbis-20230120.jar;$LIBS/com/paulscode/codecwav/20101023/codecwav-20101023.jar;$LIBS/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar;$LIBS/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar;$LIBS/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar" \
  net.minecraft.client.Minecraft