package dev.jeremyseq.impactscreens.networking;

import dev.jeremyseq.impactscreens.ImpactScreens;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record TriggerOverlayPacket(String imageName, String text, int color) implements CustomPacketPayload {

    public static final Type<TriggerOverlayPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ImpactScreens.MODID, "trigger_overlay"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, TriggerOverlayPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, TriggerOverlayPacket::imageName,
                    ByteBufCodecs.STRING_UTF8, TriggerOverlayPacket::text,
                    ByteBufCodecs.INT, TriggerOverlayPacket::color,
                    TriggerOverlayPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}