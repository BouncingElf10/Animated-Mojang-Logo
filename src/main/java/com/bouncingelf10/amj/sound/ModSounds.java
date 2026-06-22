package com.bouncingelf10.amj.sound;

import org.jspecify.annotations.NonNull;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent STARTUP = register("startup");

    private static SoundEvent register(@NonNull String id) {
        Identifier location = Identifier.fromNamespaceAndPath(AnimatedMojangLogoClient.MOD_ID, id);
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(location);
        Registry.register(BuiltInRegistries.SOUND_EVENT, location, soundEvent);
        return soundEvent;
    }

    public static void initialize() {
    }
}