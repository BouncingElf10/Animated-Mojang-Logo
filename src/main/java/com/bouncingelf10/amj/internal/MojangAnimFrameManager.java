package com.bouncingelf10.amj.internal;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.sound.ModSounds;
import dev.bouncingelf10.timelesslib.TimelessLibClient;
import dev.bouncingelf10.timelesslib.api.animation.AnimationTimeline;
import dev.bouncingelf10.timelesslib.api.time.Duration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Vector3f;

public class MojangAnimFrameManager {
    public static final AnimationTimeline timeline = TimelessLibClient.animations().createTimeline(Identifier.fromNamespaceAndPath(AnimatedMojangLogoClient.MOD_ID, "mojang_logo"));
    public static boolean hasStarted = false;
    public static boolean hasFinished = false;
    public static boolean framesPreloaded = false;
    public static float opacityLogo = 0;
    public static float opacityStudios = 0;
    public static int frame = 0;
    public static int preloadIndex = 0;

    public static final float FADE_IN = 0.3f;

    public static final int FRAMES = 79;
    public static final int FPS = 25;
    public static final int LOGO_IMAGE_WIDTH = 1200 * 4;
    public static final int LOGO_IMAGE_HEIGHT = 257 * 4;

    public static final int STUDIOS_IMAGE_WIDTH = 560 * 4;
    public static final int STUDIOS_IMAGE_HEIGHT = 90 * 4;

    public static void tickPreload() {
        if (framesPreloaded) return;

        Minecraft minecraft = Minecraft.getInstance();

        int framesPerTick = 3;
        for (int i = 0; i < framesPerTick && preloadIndex < FRAMES; i++) {
            Identifier id = Identifier.fromNamespaceAndPath(
                    AnimatedMojangLogoClient.MOD_ID,
                    "textures/gui/frames/frame_" + String.format("%04d", preloadIndex + 1) + ".png"
            );

            minecraft.getTextureManager().getTexture(id);
            preloadIndex++;
        }

        if (preloadIndex >= FRAMES) {
            framesPreloaded = true;

            AnimatedMojangLogoClient.LOGGER.info(
                    "All {} Mojang logo frames have been preloaded",
                    FRAMES
            );
        }
    }

    public static boolean areFramesPreloaded() {
        return framesPreloaded;
    }

    public static void start() {
        if (hasStarted) return;

        if (!framesPreloaded) {
            AnimatedMojangLogoClient.LOGGER.warn("Tried to start Mojang animation before frames were preloaded");
            return;
        }

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
            TimelessLibClient.scheduler().after(Duration.SECOND, client -> stop());
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
        int studiosWidth = (int) (studiosHeight * studiosAspect);

        int studiosX = guiGraphics.guiWidth() / 2 - studiosWidth / 2;
        int studiosY = logoY + logoHeight - 25;

        Identifier frameLocation = Identifier.fromNamespaceAndPath(AnimatedMojangLogoClient.MOD_ID, "textures/gui/studios.png");

        Vector3f color = ColorManager.getColorVec(ColorManager.getStudios());
        int tint = ARGB.color(Math.round(opacityStudios * 255f), Math.round(color.x * 255f), Math.round(color.y * 255f), Math.round(color.z * 255f));
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, frameLocation, studiosX, studiosY, 0, 0, studiosWidth, studiosHeight, studiosWidth, studiosHeight, tint);
    }

    public static void renderLogo(GuiGraphics guiGraphics) {
        double logoScale = Math.min(guiGraphics.guiWidth() * 0.75, guiGraphics.guiHeight()) * 0.24;
        float aspect = (float) LOGO_IMAGE_WIDTH / (float) LOGO_IMAGE_HEIGHT;
        int renderHeight = (int) logoScale;
        int renderWidth = (int) (renderHeight * aspect);

        int x = guiGraphics.guiWidth() / 2 - renderWidth / 2;
        int y = (guiGraphics.guiHeight() - 28) / 2 - renderHeight / 2;

        Identifier frameLocation = Identifier.fromNamespaceAndPath(AnimatedMojangLogoClient.MOD_ID,
                "textures/gui/frames/frame_" + String.format("%04d", frame + 1) + ".png");

        Vector3f color = ColorManager.getColorVec(ColorManager.getLogo());
        int tint = ARGB.color(Math.round(opacityLogo * 255f), Math.round(color.x * 255f), Math.round(color.y * 255f), Math.round(color.z * 255f));
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, frameLocation, x, y, 0, 0, renderWidth, renderHeight, renderWidth, renderHeight, tint);
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