package com.bouncingelf10.amj.mixin;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.internal.ColorManager;
import com.bouncingelf10.amj.internal.MojangAnimFrameManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ModConfig.isEnabled()) return;
        if (AnimatedMojangLogoClient.hasRunOnce && ModConfig.shouldOnlyPlayOnce()) return;
        ci.cancel();

        LoadingOverlayAccessor self = (LoadingOverlayAccessor) this;
        long now = Util.getMillis();

        tickFadeIn(self, now);
        smoothProgress(self);

        float fadeOut = getFadeOutProgress(self, now);
        float fadeIn = getFadeInProgress(self, now);

        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), ColorManager.getBackground());
        renderProgressBar(guiGraphics, self, fadeOut);

        if (fadeOut >= 2.0F && AnimatedMojangLogoClient.isInit) {
            renderMojangAnim(guiGraphics);
            if (MojangAnimFrameManager.hasFinished) removeOverlay(self);
        }

        if (canFinish(self, fadeIn)) {
            finish(self, guiGraphics, now);
        }
    }

    @Unique
    private static void debugRenderLogo(GuiGraphics guiGraphics) {
        int w = guiGraphics.guiWidth();
        int h = guiGraphics.guiHeight();
        int centerX = w / 2;
        int centerY = h / 2;
        double logoHeight = Math.min(w * 0.75, h) * 0.25;
        int halfHeight = (int) (logoHeight * 0.5);
        double logoWidth = logoHeight * 4.0;
        int halfWidth = (int) (logoWidth * 0.5);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/title/mojangstudios.png");
        guiGraphics.blit(MOJANG_STUDIOS_LOGO_LOCATION, centerX - halfWidth, centerY - halfHeight, halfWidth, (int) logoHeight, -0.0625F, 0F, 120, 60, 120, 120);
        guiGraphics.blit(MOJANG_STUDIOS_LOGO_LOCATION, centerX, centerY - halfHeight, halfWidth, (int) logoHeight, 0.0625F, 60F, 120, 60, 120, 120);
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @Unique
    private void renderMojangAnim(GuiGraphics guiGraphics) {
        if (!MojangAnimFrameManager.hasStarted) {
            MojangAnimFrameManager.start();
        }

        MojangAnimFrameManager.render(guiGraphics);
    }

    @Unique
    private void tickFadeIn(LoadingOverlayAccessor self, long now) {
        if (self.getFadeIn() && self.getFadeInStart() == -1L)
            self.setFadeInStart(now);
    }

    @Unique
    private float getFadeOutProgress(LoadingOverlayAccessor self, long now) {
        return self.getFadeOutStart() > -1L ? (float) (now - self.getFadeOutStart()) / 1000.0F : -1.0F;
    }

    @Unique
    private float getFadeInProgress(LoadingOverlayAccessor self, long now) {
        return self.getFadeInStart() > -1L ? (float) (now - self.getFadeInStart()) / 500.0F : -1.0F;
    }

    @Unique
    private void smoothProgress(LoadingOverlayAccessor self) {
        float raw = self.getReload().getActualProgress();
        self.setCurrentProgress(Mth.clamp(self.getCurrentProgress() * 0.95F + raw * 0.050000012F, 0.0F, 1.0F));
    }

    @Unique
    private void renderProgressBar(GuiGraphics guiGraphics, LoadingOverlayAccessor self, float fadeOut) {
        if (fadeOut >= 1.0F) return;

        int w = guiGraphics.guiWidth();
        int h = guiGraphics.guiHeight();
        int centerX = w / 2;
        int barHalfWidth = (int) (Math.min(w * 0.75, h) * 0.25 * 4.0 * 0.5);
        int barY = (int) (h * 0.8325);
        float alpha = 1.0F - Mth.clamp(fadeOut, 0.0F, 1.0F);

        drawProgressBar(guiGraphics,
                centerX - barHalfWidth, barY - 5,
                centerX + barHalfWidth, barY + 5,
                alpha, self.getCurrentProgress()
        );
    }

    @Unique
    private void drawProgressBar(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, float alpha, float progress) {
        int width = x2 - x1;
        int filled = Math.min(Mth.ceil((width - 2) * progress), width - 4);
        int barColor = ColorManager.applyAlpha(ColorManager.getBar(), alpha);
        int backgroundColor = ColorManager.applyAlpha(ColorManager.getBarBackground(), alpha);
        int borderColor = ColorManager.applyAlpha(ColorManager.getBorder(), alpha);

        guiGraphics.fill(x1 + 1, y1, x2 - 1, y1 + 1, borderColor);
        guiGraphics.fill(x1 + 1, y2 - 1, x2 - 1, y2, borderColor);
        guiGraphics.fill(x1, y1, x1 + 1, y2, borderColor);
        guiGraphics.fill(x2 - 1, y1, x2, y2, borderColor);
        guiGraphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, backgroundColor);
        guiGraphics.fill(x1 + 2, y1 + 2, x1 + 2 + filled, y2 - 2, barColor);
    }

    @Unique
    private void removeOverlay(LoadingOverlayAccessor self) {
        self.getMinecraft().setOverlay(null);
    }

    @Unique
    private boolean canFinish(LoadingOverlayAccessor self, float fadeIn) {
        return self.getFadeOutStart() == -1L && self.getReload().isDone() && (!self.getFadeIn() || fadeIn >= 2.0F);
    }

    @Unique
    private void finish(LoadingOverlayAccessor self, GuiGraphics guiGraphics, long now) {
        try {
            self.getReload().checkExceptions();
            self.getOnFinish().accept(Optional.empty());
        } catch (Throwable t) {
            self.getOnFinish().accept(Optional.of(t));
        }

        self.setFadeOutStart(now);

        if (self.getMinecraft().screen != null)
            self.getMinecraft().screen.init(self.getMinecraft(), guiGraphics.guiWidth(), guiGraphics.guiHeight());
    }
}