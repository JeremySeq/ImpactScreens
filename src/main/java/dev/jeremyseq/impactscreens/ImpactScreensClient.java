package dev.jeremyseq.impactscreens;

import dev.jeremyseq.impactscreens.overlays.ImpactImages;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

@Mod(value = ImpactScreens.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ImpactScreens.MODID, value = Dist.CLIENT)
public class ImpactScreensClient {

    @SubscribeEvent
    public static void onAddReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(
                new net.minecraft.server.packs.resources.SimplePreparableReloadListener<Void>() {
                    @Override
                    protected @NotNull Void prepare(@NotNull ResourceManager resourceManager,
                                                    net.minecraft.util.profiling.@NotNull ProfilerFiller profiler) {
                        return null;
                    }

                    @Override
                    protected void apply(@NotNull Void object, @NotNull ResourceManager resourceManager,
                                         net.minecraft.util.profiling.@NotNull ProfilerFiller profiler) {
                        ImpactImages.reload(resourceManager);
                    }
                }
        );
    }
}
