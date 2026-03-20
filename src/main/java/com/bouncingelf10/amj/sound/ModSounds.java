package com.bouncingelf10.amj.sound;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent STARTUP = register("startup");

    private static SoundEvent register(String id) {
        ResourceLocation location = new ResourceLocation(AnimatedMojangLogoClient.MOD_ID, id);
        SoundEvent event = new SoundEvent(location);
        Registry.register(Registry.SOUND_EVENT, location, event);
        return event;
    }

    public static void initialize() { }
}