package com.bouncingelf10.amj.sound;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static final SoundEvent STARTUP = register("startup");

    private static SoundEvent register(String id) {
        ResourceLocation location = new ResourceLocation(AnimatedMojangLogoClient.MOD_ID, id);
        Registry.register(BuiltInRegistries.SOUND_EVENT, location, SoundEvent.createVariableRangeEvent(location));
        return SoundEvent.createVariableRangeEvent(location);
    }

    public static void initialize() { }
}