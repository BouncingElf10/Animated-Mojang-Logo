package com.bouncingelf10.amj.internal;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.sound.ModSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.bouncingelf10.timelesslib.TimelessLibClient;
import dev.bouncingelf10.timelesslib.api.animation.AnimationTimeline;
import dev.bouncingelf10.timelesslib.api.time.Duration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.Vector;

public class MojangAnimFrameManager {
    public static final AnimationTimeline timeline = TimelessLibClient.getClientAnimationManager().createTimeline("mojang_logo");
    public static boolean hasStarted = false;
    public static boolean hasFinished = false;
    public static float opacityLogo = 0;
    public static float opacityStudios = 0;
    public static int frame = 0;

    public static final float FADE_IN = 0.3f;

    public static final int FRAMES = 79;
    public static final int FPS = 25;
    public static final int LOGO_IMAGE_WIDTH = 1200 * 4;
    public static final int LOGO_IMAGE_HEIGHT = 257 * 4;

    public static final int STUDIOS_IMAGE_WIDTH = 560 * 4;
    public static final int STUDIOS_IMAGE_HEIGHT = 90 * 4;

    public static void start() {
        hasStarted = true;
        float animDuration = (float) (FRAMES / FPS); // seconds

        timeline.channelDouble("opacity_logo")
                .keyframe(0, 0)
                .keyframe(FADE_IN, 1)
                .bind(value -> opacityLogo = value.floatValue());

        timeline.channelDouble("opacity_studios")
                .keyframe(animDuration + 0.2f, 0)
                .keyframe(animDuration + 0.6f, 1)
                .keyframe(animDuration + 0.6f + ModConfig.getHoldSeconds(), 1)
                .bind(value -> opacityStudios = value.floatValue());

        timeline.channelDouble("frame_logo")
                .keyframe(0, 0)
                .keyframe(FADE_IN, 0)
                .keyframe(animDuration, FRAMES - 1)
                .bind(value -> frame = value.intValue());

        timeline.onFinish(() -> {
            hasFinished = true;
            TimelessLibClient.getClientScheduler().after(Duration.SECOND, client -> stop());
        });
        timeline.play();

        AnimatedMojangLogoClient.LOGGER.info("Playing sound and starting Mojang logo animation");
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(ModSounds.STARTUP, 1.0f)
        );
    }

    public static void render(GuiGraphics guiGraphics) {
        renderLogo(guiGraphics);
        renderStudios(guiGraphics);
    }

    public static void renderStudios(GuiGraphics guiGraphics) {
        double logoScale = Math.min(guiGraphics.guiWidth() * 0.75, guiGraphics.guiHeight()) * 0.3;
        int logoHeight = (int) logoScale;
        int logoY = guiGraphics.guiHeight() / 2 - logoHeight / 2;

        double studiosScale = logoHeight * 0.3;
        float studiosAspect = (float) STUDIOS_IMAGE_WIDTH / STUDIOS_IMAGE_HEIGHT;
        int studiosHeight = (int) studiosScale;
        int studiosWidth = (int)(studiosHeight * studiosAspect);

        int studiosX = guiGraphics.guiWidth() / 2 - studiosWidth / 2;
        int studiosY = logoY + logoHeight - 25;

        ResourceLocation frameLocation = ResourceLocation.fromNamespaceAndPath(AnimatedMojangLogoClient.MOD_ID, "textures/gui/studios.png");

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Vector3f color = ColorManager.getColorVec(ColorManager.getStudios());
        guiGraphics.setColor(color.x, color.y, color.z, opacityStudios);
        guiGraphics.blit(frameLocation, studiosX, studiosY, 0, 0, studiosWidth, studiosHeight, studiosWidth, studiosHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public static void renderLogo(GuiGraphics guiGraphics) {
        double logoScale = Math.min(guiGraphics.guiWidth() * 0.75, guiGraphics.guiHeight()) * 0.24;
        float aspect = (float) LOGO_IMAGE_WIDTH / (float) LOGO_IMAGE_HEIGHT;
        int renderHeight = (int) logoScale;
        int renderWidth = (int) (renderHeight * aspect);

        int x = guiGraphics.guiWidth() / 2 - renderWidth / 2;
        int y = (guiGraphics.guiHeight() - 28) / 2 - renderHeight / 2;

        ResourceLocation frameLocation = ResourceLocation.fromNamespaceAndPath(AnimatedMojangLogoClient.MOD_ID,
                "textures/gui/frames/frame_" + String.format("%04d", frame + 1) + ".png");

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Vector3f color = ColorManager.getColorVec(ColorManager.getLogo());
        guiGraphics.setColor(color.x, color.y, color.z, opacityLogo);
        guiGraphics.blit(frameLocation, x, y, 0, 0, renderWidth, renderHeight, renderWidth, renderHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public static void stop() {
        if (!hasStarted) return;

        timeline.stop();

        hasStarted = false;
        hasFinished = false;
        opacityLogo = 0;
        opacityStudios = 0;
        frame = 0;
        AnimatedMojangLogoClient.hasRunOnce = true;
    }
}
