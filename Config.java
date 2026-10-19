package client.confis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("confis0oooooo.json");
    private static Config instance;

    public boolean fps = true, armor = true, ping = true, waypoints = true, clock = true,
            day = true, tps = true, keystrokes = true, cps = true, minimap = true;
    public boolean clock24h = true;
    /** 0 = top-left, 1 = top-right, 2 = bottom-left, 3 = bottom-right */
    public int infoCorner = 0;
    public int mapZoom = 2;
    public List<Waypoint> waypointList = new ArrayList<>();

    public static Config get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        try (Reader r = Files.newBufferedReader(FILE)) {
            instance = GSON.fromJson(r, Config.class);
        } catch (Exception ignored) {
        }
        if (instance == null) instance = new Config();
        if (instance.waypointList == null) instance.waypointList = new ArrayList<>();
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(get(), w);
        } catch (Exception ignored) {
        }
    }
}
