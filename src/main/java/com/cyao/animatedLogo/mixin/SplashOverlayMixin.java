package com.cyao.animatedLogo.mixin;

import com.cyao.animatedLogo.AnimatedLogo;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.resource.ResourceReload;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
    @Mutable
    @Shadow @Final private ResourceReload reload;
    @Shadow private float progress;

    @Unique private static final float ANIMATION_SPEED = 0.5f;
    @Unique private float animationTick = 0.0f;
    @Unique private int count = 0;
    @Unique private Identifier[] frames;
    @Unique private boolean inited = false;
    @Unique private static final int FRAMES = 12;
    @Unique private static final int IMAGE_PER_FRAME = 4;
    @Unique private static final int FRAMES_PER_FRAME = 2;
    @Unique private float f = 0;
    @Unique private boolean animationDone = false;
    @Unique private static final int MOJANG_RED = ColorHelper.getArgb(255, 239, 50, 61);

    @Unique private long animationDelayStartTime = -1;
    @Unique private static final long ANIMATION_DELAY_MS = 2000;

    @Unique private boolean soundPlayed = false;


    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(MinecraftClient client, ResourceReload monitor, Consumer<Throwable> exceptionHandler, boolean reloading, CallbackInfo ci) {
        animationDelayStartTime = System.currentTimeMillis();
    }

    @ModifyArg(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V", ordinal = 0),
            index = 7)
    private int removeText1(int i) {
        return 0;
    }

    @ModifyArg(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V", ordinal = 1),
            index = 7)
    private int removeText2(int u) {
        return 0;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void preRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        long elapsed = System.currentTimeMillis() - animationDelayStartTime;

        // Don't render animation until delay has passed
        if (elapsed < ANIMATION_DELAY_MS) {
            // optionally, just draw the background red during the wait
            context.fill(RenderLayer.getGuiOverlay(), 0, 0,
                    context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                    MOJANG_RED);
            ci.cancel();
            return;
        }

        if (!animationDone) {
            drawAnimatedIntro(context, mouseX, mouseY, delta);
            ci.cancel();
        }
    }

    @Unique
    private void drawAnimatedIntro(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!soundPlayed) {
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(AnimatedLogo.STARTUP_SOUND_EVENT, 1.0F)
            );
            soundPlayed = true;
        }

        if (!inited) {
            this.frames = new Identifier[FRAMES];
            for (int i = 0; i < FRAMES; i++) {
                this.frames[i] = Identifier.of("animated-logo", "textures/gui/frame_" + i + ".png");
            }
            inited = true;
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int width = screenWidth / 2;
        int height = width * 256 / 1024;
        int x = (screenWidth - width) / 2;
        int y = (screenHeight - height) / 2;

        int frameIndex = count / IMAGE_PER_FRAME / FRAMES_PER_FRAME;
        int subFrameY = 256 * ((count % (IMAGE_PER_FRAME * FRAMES_PER_FRAME)) / FRAMES_PER_FRAME);

        context.fill(RenderLayer.getGuiOverlay(), 0, 0,
                context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                MOJANG_RED);

        context.drawTexture(RenderLayer::getGuiTextured, frames[frameIndex], x, y,
                0, subFrameY, width, height,
                1024, 256, 1024, 1024, ColorHelper.getWhite(1.0f));

        animationTick += ANIMATION_SPEED;
        count = (int) animationTick;
        if (animationTick >= FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME) {
            animationDone = true;
            count = FRAMES * IMAGE_PER_FRAME * FRAMES_PER_FRAME - 1; // Force last frame
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIIIIII)V",
            ordinal = 1, shift = At.Shift.AFTER))
    private void onAfterRenderLogo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci,
                                   @Local(ordinal = 2) int scaledWidth, @Local(ordinal = 3) int scaledHeight,
                                   @Local(ordinal = 3) float alpha, @Local(ordinal = 4) int x, @Local(ordinal = 5) int y,
                                   @Local(ordinal = 0) double height, @Local(ordinal = 6) int halfHeight,
                                   @Local(ordinal = 1) double width, @Local(ordinal = 7) int halfWidth) {
        if (!animationDone) return;

        // Studios.png
        float progress = MathHelper.clamp(this.progress * 0.95F + this.reload.getProgress() * 0.050000012F, 0.0F, 1.0F);
        if (progress >= 0.8) {
            f = Math.min(alpha, f + 0.2f);
            int sw = (int) (width * 0.45);
            context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("animated-logo", "textures/gui/studios.png"),
                    x - sw / 2, (int) (y - halfHeight + height - height / 12),
                    0, 0, sw, (int) (height / 5.0), 450, 50, 512, 512, ColorHelper.getWhite(f));
        }

        // Title (last frame)
        int finalFrameScreenWidth = context.getScaledWindowWidth();
        int finalFrameScreenHeight = context.getScaledWindowHeight();
        int finalFrameWidth = finalFrameScreenWidth / 2;
        int finalFrameHeight = finalFrameWidth * 256 / 1024;
        int finalFrameX = (finalFrameScreenWidth - finalFrameWidth) / 2;
        int finalFrameY = (finalFrameScreenHeight - finalFrameHeight) / 2;
        int finalSubFrameY = 256 * ((count % (IMAGE_PER_FRAME * FRAMES_PER_FRAME)) / FRAMES_PER_FRAME);

        Identifier finalFrame = frames[FRAMES - 1];
        context.drawTexture(RenderLayer::getGuiTextured, finalFrame, finalFrameX, finalFrameY,
                0, finalSubFrameY, (int) finalFrameWidth, (int) finalFrameHeight,
                1024, 256, 1024, 1024, ColorHelper.getWhite(alpha));
    }
}
