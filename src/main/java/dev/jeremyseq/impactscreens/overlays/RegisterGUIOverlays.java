package dev.jeremyseq.impactscreens.overlays;

import dev.jeremyseq.impactscreens.ImpactScreens;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = ImpactScreens.MODID, value = Dist.CLIENT)
public class RegisterGUIOverlays {
    @SubscribeEvent
    public static void onGui(RenderGuiLayerEvent.Pre e) {
        if (e.getName().equals(VanillaGuiLayers.CHAT)) {
            Window w = Minecraft.getInstance().getWindow();
            ImpactOverlay.drawImpactOverlay(e.getGuiGraphics(), w);
        }
    }
}
