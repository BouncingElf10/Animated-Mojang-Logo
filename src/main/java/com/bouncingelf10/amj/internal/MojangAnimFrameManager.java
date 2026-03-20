package com.bouncingelf10.amj.internal;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.sound.ModSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import dev.bouncingelf10.timelesslib.TimelessLibClient;
import dev.bouncingelf10.timelesslib.api.animation.AnimationTimeline;
import dev.bouncingelf10.timelesslib.api.time.Duration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;

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
    public static final int LOGO_IMAGE_WIDTH  = 1200 * 4;
    public static final int LOGO_IMAGE_HEIGHT = 257 * 4;

    public static final int STUDIOS_IMAGE_WIDTH  = 560 * 4;
    public static final int STUDIOS_IMAGE_HEIGHT = 90 * 4;

    public static void start() {
        hasStarted = true;
        float animDuration = (float)(FRAMES / FPS);

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

    public static void render(PoseStack poseStack, int w, int h) {
        renderLogo(poseStack, w, h);
        renderStudios(poseStack, w, h);
    }

    public static void renderStudios(PoseStack poseStack, int w, int h) {
        double logoScale = Math.min(w * 0.75, h) * 0.3;
        int logoHeight = (int) logoScale;
        int logoY = h / 2 - logoHeight / 2;

        double studiosScale = logoHeight * 0.3;
        float studiosAspect = (float) STUDIOS_IMAGE_WIDTH / STUDIOS_IMAGE_HEIGHT;
        int studiosHeight = (int) studiosScale;
        int studiosWidth = (int)(studiosHeight * studiosAspect);

        int studiosX = w / 2 - studiosWidth / 2;
        int studiosY = logoY + logoHeight - 25;

        ResourceLocation frameLocation = new ResourceLocation(AnimatedMojangLogoClient.MOD_ID, "textures/gui/studios.png");

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Vector3f color = ColorManager.getColorVec(ColorManager.getStudios());
        RenderSystem.setShaderColor(color.x(), color.y(), color.z(), opacityStudios);
        RenderSystem.setShaderTexture(0, frameLocation);
        GuiComponent.blit(poseStack, studiosX, studiosY, 0, 0, studiosWidth, studiosHeight, studiosWidth, studiosHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public static void renderLogo(PoseStack poseStack, int w, int h) {
        double logoScale = Math.min(w * 0.75, h) * 0.24;
        float aspect = (float) LOGO_IMAGE_WIDTH / (float) LOGO_IMAGE_HEIGHT;
        int renderHeight = (int) logoScale;
        int renderWidth = (int)(renderHeight * aspect);

        int x = w / 2 - renderWidth / 2;
        int y = (h - 28) / 2 - renderHeight / 2;

        ResourceLocation frameLocation = new ResourceLocation(AnimatedMojangLogoClient.MOD_ID,
                "textures/gui/frames/frame_" + String.format("%04d", frame + 1) + ".png");

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Vector3f color = ColorManager.getColorVec(ColorManager.getLogo());
        RenderSystem.setShaderColor(color.x(), color.y(), color.z(), opacityLogo);
        RenderSystem.setShaderTexture(0, frameLocation);
        GuiComponent.blit(poseStack, x, y, 0, 0, renderWidth, renderHeight, renderWidth, renderHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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