package dev.jeremyseq.impactscreens;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ImpactScreens.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BUILD_UP = SOUND_EVENTS.register(
            "build_up",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ImpactScreens.MODID, "build_up")
            )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> TYPING = SOUND_EVENTS.register(
            "typing",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ImpactScreens.MODID, "typing")
            )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> WAR_CRY = SOUND_EVENTS.register(
            "war_cry",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ImpactScreens.MODID, "war_cry")
            )
    );
}
