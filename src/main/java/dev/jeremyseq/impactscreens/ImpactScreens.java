package dev.jeremyseq.impactscreens;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ImpactScreens.MODID)
public class ImpactScreens {
    public static final String MODID = "impactscreens";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ImpactScreens(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(ModCommands.class);
        ModSounds.SOUND_EVENTS.register(modEventBus);
    }
}
