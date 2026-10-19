package client.confis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Hud {
    private static int frames, fps;
    private static long lastFpsTime = System.nanoTime();
    private static boolean shiftWasDown;
    private static Object lastLevel;

    private Hud() {}

    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        pollMenuKey(mc);
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        if (mc.level != lastLevel) {
            lastLevel = mc.level;
            TpsTracker.reset();
        }
        countFrame();
        Cps.poll(mc);
        Config c = Config.get();

        if (c.minimap) Minimap.render(g, mc, c);
        if (c.waypoints) Waypoints.render(g, mc, c);
        renderInfo(g, mc, c);
        if (c.keystrokes) renderKeystrokes(g, mc, c);
        if (c.armor) renderArmor(g, mc);
    }

    /** Right Shift opens / closes the menu. Polled from the render thread's GL context. */
    private static void pollMenuKey(Minecraft mc) {
        long window = GLFW.glfwGetCurrentContext();
        if (window == 0L) return;
        boolean down = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (down && !shiftWasDown) {
            if (mc.screen == null && mc.player != null) mc.setScreen(new ConfigScreen());
            else if (mc.screen instanceof ConfigScreen) mc.setScreen(null);
        }
        shiftWasDown = down;
    }

    private static void countFrame() {
        frames++;
        long now = System.nanoTime();
        if (now - lastFpsTime >= 1_000_000_000L) {
            fps = frames;
            frames = 0;
            lastFpsTime = now;
        }
    }

    private static void renderInfo(GuiGraphics g, Minecraft mc, Config c) {
        List<String> lines = new ArrayList<>();
        if (c.fps) lines.add("FPS: " + fps);
        if (c.ping) {
            int ping = -1;
            if (mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
                if (info != null) ping = info.getLatency();
            }
            lines.add("Ping: " + (ping < 0 ? "--" : ping + " ms"));
        }
        if (c.tps) {
            double t = TpsTracker.get();
            lines.add("TPS: " + (t < 0 ? "--" : String.format("%.1f", t)));
        }
        if (c.cps) lines.add("CPS: " + Cps.left() + " | " + Cps.right());
        if (c.clock) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern(c.clock24h ? "HH:mm" : "hh:mm a");
            lines.add("Time: " + LocalTime.now().format(f));
        }
        if (c.day) lines.add("Day: " + (mc.level.getGameTime() / 24000L));
        if (lines.isEmpty()) return;

        Font font = mc.font;
        int w = 0;
        for (String s : lines) w = Math.max(w, font.width(s));
        w += 8;
        int h = lines.size() * 11 + 4;
        int x = (c.infoCorner % 2 == 0) ? 4 : g.guiWidth() - w - 4;
        int y = (c.infoCorner < 2) ? 4 : g.guiHeight() - h - 4;

        g.fill(x, y, x + w, y + h, 0x80000000);
        int ty = y + 3;
        for (String s : lines) {
            g.drawString(font, s, x + 4, ty, 0xFFFFFFFF, true);
            ty += 11;
        }
    }

    private static void renderKeystrokes(GuiGraphics g, Minecraft mc, Config c) {
        int box = 22, gap = 2;
        int x0 = 6;
        int y0 = g.guiHeight() - 6 - (box * 3 + gap * 3 + 14);
        var o = mc.options;

        key(g, mc, "W", x0 + box + gap, y0, box, box, o.keyUp.isDown());
        key(g, mc, "A", x0, y0 + box + gap, box, box, o.keyLeft.isDown());
        key(g, mc, "S", x0 + box + gap, y0 + box + gap, box, box, o.keyDown.isDown());
        key(g, mc, "D", x0 + 2 * (box + gap), y0 + box + gap, box, box, o.keyRight.isDown());

        int my = y0 + 2 * (box + gap);
        int mw = (box * 3 + gap * 2 - gap) / 2;
        key(g, mc, c.cps ? "LMB " + Cps.left() : "LMB", x0, my, mw, box, o.keyAttack.isDown());
        key(g, mc, c.cps ? "RMB " + Cps.right() : "RMB", x0 + mw + gap, my, mw, box, o.keyUse.isDown());

        key(g, mc, "", x0, my + box + gap, box * 3 + gap * 2, 12, o.keyJump.isDown());
    }

    private static void key(GuiGraphics g, Minecraft mc, String label, int x, int y, int w, int h, boolean down) {
        g.fill(x, y, x + w, y + h, down ? 0xB0FFFFFF : 0x80000000);
        if (!label.isEmpty()) {
            g.drawCenteredString(mc.font, label, x + w / 2, y + (h - 8) / 2, down ? 0xFF000000 : 0xFFFFFFFF);
        }
    }

    private static void renderArmor(GuiGraphics g, Minecraft mc) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
        int x = 6;
        int y = g.guiHeight() / 2 - 45;
        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            g.fill(x - 2, y - 1, x + 54, y + 17, 0x66000000);
            g.renderItem(stack, x, y);
            if (stack.isDamageableItem()) {
                int max = stack.getMaxDamage();
                int left = max - stack.getDamageValue();
                float frac = max == 0 ? 1f : (float) left / max;
                int r = (int) (255 * Math.min(1f, (1f - frac) * 2f));
                int gr = (int) (255 * Math.min(1f, frac * 2f));
                int color = 0xFF000000 | (r << 16) | (gr << 8) | 0x40;
                g.drawString(mc.font, String.valueOf(left), x + 20, y + 4, color, true);
            } else if (stack.getCount() > 1) {
                g.drawString(mc.font, "x" + stack.getCount(), x + 20, y + 4, 0xFFFFFFFF, true);
            }
            y += 19;
        }
    }
}
