package dev.jeremyseq.impactscreens.overlays;

import com.mojang.blaze3d.platform.NativeImage;
import dev.jeremyseq.impactscreens.ImpactScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImpactImages {

    private static final String FOLDER = "impact_images";

    public record ImpactImage(ResourceLocation location, int width, int height) {}

    private static Map<String, ImpactImage> IMAGES = new HashMap<>();

    public static void reload(ResourceManager resourceManager) {
        Map<String, ImpactImage> newMap = new HashMap<>();

        Map<ResourceLocation, ?> found = resourceManager.listResources(FOLDER, loc -> loc.getPath().endsWith(".png"));

        for (ResourceLocation loc : found.keySet()) {
            String path = loc.getPath();
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String key = fileName.substring(0, fileName.length() - ".png".length());

            int width = 0;
            int height = 0;
            try {
                Resource resource = resourceManager.getResource(loc).orElseThrow();
                try (InputStream stream = resource.open()) {
                    NativeImage image = NativeImage.read(stream);
                    width = image.getWidth();
                    height = image.getHeight();
                    image.close();
                }
            } catch (IOException e) {
                ImpactScreens.LOGGER.warn("Failed to read dimensions for impact image: {}", loc, e);
            }

            newMap.put(key, new ImpactImage(loc, width, height));
        }

        IMAGES = newMap;
    }

    public static ImpactImage get(String name) {
        return IMAGES.get(name);
    }

    public static Map<String, ImpactImage> getAll() {
        return IMAGES;
    }
}