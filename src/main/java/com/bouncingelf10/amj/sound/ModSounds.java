package com.bouncingelf10.amj.sound;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, AnimatedMojangLogoClient.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> STARTUP =
            SOUND_EVENTS.register("startup",
                    () -> SoundEvent.createVariableRangeEvent(
                            Identifier.fromNamespaceAndPath(AnimatedMojangLogoClient.MOD_ID, "startup")));

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}