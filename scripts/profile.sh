#!/bin/bash
# Launches retronism with Aero_Profiler enabled and Java Flight Recorder
# capturing the first 60s of the run into tests/data/aero.jfr.
# Open the resulting file in JDK Mission Control or `jfr print aero.jfr`.
#
# Usage: bash scripts/profile.sh [duration]
#   duration: JFR recording duration in seconds (default 60)
set -e
BASE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$BASE"

DURATION="${1:-60}"
JFR_FILE="$BASE/tests/data/aero.jfr"

echo "=== Transpiling ==="
bash scripts/transpile.sh

echo "=== Building ==="
cd "$BASE/mcp"
echo "build" | java -jar RetroMCP-Java-CLI.jar
cd "$BASE"

echo "=== Preparing test jar ==="
cp tests/data/minecraft_test.jar tests/data/minecraft_run.jar

REOBF="$BASE/mcp/minecraft/reobf"
if [ -d "$REOBF" ]; then
    (
        cd "$REOBF"
        for f in *.class; do
            [ -f "$f" ] || continue
            case "$f" in
                Retronism_*|Aero_*|mod_*|EntityRendererProxy*|dn.class|nw.class) ;;
                *) rm -f "$f" ;;
            esac
        done
    )
    KEPT_COUNT=$(find "$REOBF" -maxdepth 1 -name "*.class" | wc -l)
    if [ "$KEPT_COUNT" -gt 0 ]; then
        (cd "$REOBF" && jar uf "$BASE/tests/data/minecraft_run.jar" *.class)
        echo "Injected $KEPT_COUNT mod classes"
    fi
fi

if [ -d "$BASE/temp/merged" ]; then
    (cd "$BASE/temp/merged" && jar uf "$BASE/tests/data/minecraft_run.jar" .)
    echo "Injected custom textures and models"
fi

mkdir -p "$BASE/tests/data/tmp"

echo "=== Launching Minecraft (JFR -> $JFR_FILE, ${DURATION}s) ==="
LIBS="mcp/libraries"
java -Xms1024M -Xmx1024M \
  -Daero.dev=true \
  -Daero.profiler=true \
  -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints \
  -XX:StartFlightRecording=duration=${DURATION}s,filename=${JFR_FILE},settings=profile \
  -Djava.library.path="$LIBS/natives" \
  -Djava.io.tmpdir="$BASE/tests/data/tmp" \
  -cp "tests/data/minecraft_run.jar;$LIBS/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar;$LIBS/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar;$LIBS/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar;$LIBS/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar;$LIBS/com/paulscode/codecjorbis/20230120/codecjorbis-20230120.jar;$LIBS/com/paulscode/codecwav/20101023/codecwav-20101023.jar;$LIBS/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar;$LIBS/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar;$LIBS/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar" \
  net.minecraft.client.Minecraft

echo "=== JFR recording at: $JFR_FILE ==="
