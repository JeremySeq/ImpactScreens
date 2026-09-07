package dev.jeremyseq.impactscreens.networking;

import dev.jeremyseq.impactscreens.ImpactScreens;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import dev.jeremyseq.impactscreens.overlays.ImpactOverlay;

@EventBusSubscriber(modid = ImpactScreens.MODID)
public class ModNetworking {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                TriggerOverlayPacket.TYPE,
                TriggerOverlayPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> ImpactOverlay.start(payload.imageName(), payload.text(), payload.color()));
                }
        );
    }
}
