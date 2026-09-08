package refinery;

import aero.machineapi.*;
import net.minecraft.src.*;

/** Retronism transport addresses the actual part, never the master's unrelated faces. */
public final class NetworkPorts {
    private NetworkPorts() {}
    public static int face(RefineryTile part) {
        for (int side = 0; side < 6; side++) if (Ports.kind(part, side) >= 0) return side;
        return -1;
    }
    public static void configure(RefineryTile part, int[] modes) {
        java.util.Arrays.fill(modes, 0);
        int face = face(part), kind = Ports.kind(part, face);
        if (kind < 0) return;
        Aero_SideConfig.set(modes, face, kind == 3 ? Aero_SideConfig.TYPE_ENERGY : Aero_SideConfig.TYPE_ITEM,
            kind == 2 ? Aero_SideConfig.MODE_OUTPUT : Aero_SideConfig.MODE_INPUT);
    }
    public static ItemStack insert(RefineryTile part, ItemStack stack) {
        Ports.insert(part, face(part), stack);
        return stack.stackSize == 0 ? null : stack;
    }
    public static int receive(RefineryTile part, int amount) {
        return Ports.receiveEnergy(part, face(part), amount);
    }
}
