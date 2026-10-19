package client.confis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** The Right-Shift hub. */
public class ConfigScreen extends Screen {
    private static final String[] CORNERS = {"Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right"};

    public ConfigScreen() {
        super(Component.literal(ConfisClient.NAME));
    }

    @Override
    protected void init() {
        Config c = Config.get();
        int bw = 150, bh = 20, gap = 4;
        int left = this.width / 2 - bw - gap / 2;
        int right = this.width / 2 + gap / 2;
        int y = this.height / 2 - 100;

        toggle(left, y, bw, bh, "FPS", () -> c.fps, v -> c.fps = v);
        toggle(right, y, bw, bh, "Armor Status", () -> c.armor, v -> c.armor = v);
        y += bh + gap;
        toggle(left, y, bw, bh, "Ping", () -> c.ping, v -> c.ping = v);
        toggle(right, y, bw, bh, "Waypoints", () -> c.waypoints, v -> c.waypoints = v);
        y += bh + gap;
        toggle(left, y, bw, bh, "Clock", () -> c.clock, v -> c.clock = v);
        toggle(right, y, bw, bh, "Day", () -> c.day, v -> c.day = v);
        y += bh + gap;
        toggle(left, y, bw, bh, "Server TPS", () -> c.tps, v -> c.tps = v);
        toggle(right, y, bw, bh, "Keystrokes", () -> c.keystrokes, v -> c.keystrokes = v);
        y += bh + gap;
        toggle(left, y, bw, bh, "CPS", () -> c.cps, v -> c.cps = v);
        toggle(right, y, bw, bh, "Minimap", () -> c.minimap, v -> c.minimap = v);
        y += bh + gap + 6;

        addRenderableWidget(Button.builder(Component.literal("Clock: " + (c.clock24h ? "24-hour" : "12-hour")), b -> {
            c.clock24h = !c.clock24h;
            b.setMessage(Component.literal("Clock: " + (c.clock24h ? "24-hour" : "12-hour")));
        }).bounds(left, y, bw, bh).build());
        addRenderableWidget(Button.builder(Component.literal("Info Panel: " + CORNERS[c.infoCorner]), b -> {
            c.infoCorner = (c.infoCorner + 1) % 4;
            b.setMessage(Component.literal("Info Panel: " + CORNERS[c.infoCorner]));
        }).bounds(right, y, bw, bh).build());
        y += bh + gap;

        addRenderableWidget(Button.builder(Component.literal("Minimap Zoom: " + c.mapZoom + "x"), b -> {
            c.mapZoom = c.mapZoom % 4 + 1;
            b.setMessage(Component.literal("Minimap Zoom: " + c.mapZoom + "x"));
        }).bounds(left, y, bw, bh).build());
        Button saved = Button.builder(Component.literal("Waypoints saved: " + c.waypointList.size()), b -> {})
                .bounds(right, y, bw, bh).build();
        saved.active = false;
        addRenderableWidget(saved);
        y += bh + gap;

        addRenderableWidget(Button.builder(Component.literal("Add Waypoint Here"), b -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            String name = "Waypoint " + (c.waypointList.size() + 1);
            c.waypointList.add(new Waypoint(name, mc.player.getBlockX(), mc.player.getBlockY(),
                    mc.player.getBlockZ(), mc.level.dimension().toString(), Waypoints.nextColor(c)));
            Config.save();
            rebuildWidgets();
        }).bounds(left, y, bw, bh).build());
        addRenderableWidget(Button.builder(Component.literal("Remove Last Waypoint"), b -> {
            if (!c.waypointList.isEmpty()) {
                c.waypointList.remove(c.waypointList.size() - 1);
                Config.save();
                rebuildWidgets();
            }
        }).bounds(right, y, bw, bh).build());
        y += bh + gap + 6;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width / 2 - 50, y, 100, bh).build());
    }

    private void toggle(int x, int y, int w, int h, String name, Supplier<Boolean> get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(label(name, get.get()), b -> {
            set.accept(!get.get());
            b.setMessage(label(name, get.get()));
        }).bounds(x, y, w, h).build());
    }

    private static Component label(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "ON" : "OFF"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(this.font, ConfisClient.NAME, this.width / 2, this.height / 2 - 126, 0xFFFFFFFF);
        g.drawCenteredString(this.font, "Right Shift to close", this.width / 2, this.height / 2 - 114, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        Config.save();
    }
}
