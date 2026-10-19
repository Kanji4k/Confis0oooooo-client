package client.confis;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public class ConfisClient implements ClientModInitializer {
    public static final String MOD_ID = "confis0oooooo";
    public static final String NAME = "Confis0oooooo Client";

    @Override
    public void onInitializeClient() {
        Config.load();
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
                (graphics, deltaTracker) -> Hud.render(graphics));
    }
}
