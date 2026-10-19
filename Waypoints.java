package client.confis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

/** Draws waypoint markers projected onto the screen. */
public final class Waypoints {
    private static final int[] PALETTE = {0xFF5555, 0x55FF55, 0x5555FF, 0xFFFF55, 0xFF55FF, 0x55FFFF, 0xFFAA00};

    private Waypoints() {}

    public static int nextColor(Config c) {
        return PALETTE[c.waypointList.size() % PALETTE.length];
    }

    public static void render(GuiGraphics g, Minecraft mc, Config c) {
        if (c.waypointList.isEmpty()) return;
        String dim = mc.level.dimension().toString();

        Vec3 eye = mc.player.getEyePosition();
        Vec3 f = mc.player.getViewVector(1.0F);
        // right = forward x up(0,1,0) ; up = right x forward
        double rx = -f.z, rz = f.x;
        double rl = Math.sqrt(rx * rx + rz * rz);
        if (rl < 1e-6) return;
        rx /= rl; rz /= rl;
        // up = right x forward, with right = (rx, 0, rz)
        double ux = -rz * f.y;
        double uy = rz * f.x - rx * f.z;
        double uz = rx * f.y;

        int W = g.guiWidth(), H = g.guiHeight();
        double tanHalf = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
        double aspect = (double) W / H;

        for (Waypoint w : c.waypointList) {
            if (!dim.equals(w.dim)) continue;
            double dx = w.x + 0.5 - eye.x, dy = w.y + 0.5 - eye.y, dz = w.z + 0.5 - eye.z;
            double cz = dx * f.x + dy * f.y + dz * f.z;
            if (cz < 0.1) continue;
            double cx = dx * rx + dz * rz;
            double cy = dx * ux + dy * uy + dz * uz;
            int sx = (int) (W / 2.0 + (cx / cz) / (tanHalf * aspect) * (W / 2.0));
            int sy = (int) (H / 2.0 - (cy / cz) / tanHalf * (H / 2.0));
            if (sx < 4 || sx > W - 4 || sy < 12 || sy > H - 12) continue;

            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            String label = w.name + " [" + (int) dist + "m]";
            int col = 0xFF000000 | w.color;
            g.fill(sx - 3, sy - 3, sx + 4, sy + 4, 0xFF000000);
            g.fill(sx - 2, sy - 2, sx + 3, sy + 3, col);
            g.drawCenteredString(mc.font, label, sx, sy - 14, col);
        }
    }
}
