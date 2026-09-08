package refinery;

import java.io.DataInputStream;
import java.io.IOException;

/** Per-cell collision volumes, clipped from the same source cuboids as the renderer. */
public final class Geometry {
    private static final float[][][][] BOXES = load();
    private Geometry() {}
    public static float[][] boxes(RefineryTile tile) { return BOXES[tile.facing][tile.ly * 15 + tile.lz * 5 + tile.lx]; }
    private static float[][][][] load() {
        float[][][][] result = new float[4][45][][];
        try {
            DataInputStream input = new DataInputStream(Geometry.class.getResourceAsStream("/refinery/collision.bin"));
            if (input.readInt() != 0x52464331) throw new IOException("Invalid collision mesh");
            for (int cell = 0; cell < 45; cell++) {
                int count = input.readInt();
                if (count < 0 || count > 2048) throw new IOException("Invalid collision count");
                for (int f = 0; f < 4; f++) result[f][cell] = new float[count][6];
                for (int box = 0; box < count; box++) {
                    float[] original = result[0][cell][box];
                    for (int i = 0; i < 6; i++) original[i] = input.readFloat();
                    for (int f = 1; f < 4; f++) {
                        float[] a = result[f-1][cell][box], b = result[f][cell][box];
                        b[0] = 1-a[5]; b[1] = a[1]; b[2] = a[0]; b[3] = 1-a[2]; b[4] = a[4]; b[5] = a[3];
                    }
                }
            }
            input.close(); return result;
        } catch (IOException error) { throw new IllegalStateException("Cannot load collision mesh", error); }
    }
}
