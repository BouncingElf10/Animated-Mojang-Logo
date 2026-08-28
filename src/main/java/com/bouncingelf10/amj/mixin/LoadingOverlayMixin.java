package com.bouncingelf10.amj.mixin;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.internal.ColorManager;
import com.bouncingelf10.amj.internal.MojangAnimFrameManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
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
	private void onRender(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
		if (!ModConfig.isEnabled()) return;
		if (AnimatedMojangLogoClient.hasRunOnce && ModConfig.shouldOnlyPlayOnce()) return;
		ci.cancel();

		LoadingOverlayAccessor self = (LoadingOverlayAccessor) this;
		long now = Util.getMillis();

		tickFadeIn(self, now);
		smoothProgress(self);
		MojangAnimFrameManager.tickPreload();

		float fadeOut = getFadeOutProgress(self, now);
		float fadeIn = getFadeInProgress(self, now);

		int w = self.getMinecraft().getWindow().getGuiScaledWidth();
		int h = self.getMinecraft().getWindow().getGuiScaledHeight();

		GuiComponent.fill(poseStack, 0, 0, w, h, ColorManager.getBackground());
		renderProgressBar(poseStack, self, fadeOut, w, h);

		if (fadeOut >= 2.0F && AnimatedMojangLogoClient.isInit && MojangAnimFrameManager.areFramesPreloaded()) {
			renderMojangAnim(poseStack, w, h);
			if (MojangAnimFrameManager.hasFinished) removeOverlay(self);
		}

		if (canFinish(self, fadeIn)) {
			finish(self, poseStack, now, w, h);
		}
	}

	@Unique
	private static void debugRenderLogo(PoseStack poseStack, int w, int h) {
		int centerX = w / 2;
		int centerY = h / 2;
		double logoHeight = Math.min(w * 0.75, h) * 0.25;
		int halfHeight = (int)(logoHeight * 0.5);
		double logoWidth = logoHeight * 4.0;
		int halfWidth = (int)(logoWidth * 0.5);
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
		RenderSystem.setShaderTexture(0, MOJANG_STUDIOS_LOGO_LOCATION);
		GuiComponent.blit(poseStack, centerX - halfWidth, centerY - halfHeight, halfWidth, (int)logoHeight, -0.0625F, 0F, 120, 60, 120, 120);
		GuiComponent.blit(poseStack, centerX, centerY - halfHeight, halfWidth, (int)logoHeight, 0.0625F, 60F, 120, 60, 120, 120);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
	}

	@Unique
	private void renderMojangAnim(PoseStack poseStack, int w, int h) {
		if (!MojangAnimFrameManager.hasStarted) {
			MojangAnimFrameManager.start();
		}
		MojangAnimFrameManager.render(poseStack, w, h);
	}

	@Unique
	private void tickFadeIn(LoadingOverlayAccessor self, long now) {
		if (self.getFadeIn() && self.getFadeInStart() == -1L)
			self.setFadeInStart(now);
	}

	@Unique
	private float getFadeOutProgress(LoadingOverlayAccessor self, long now) {
		return self.getFadeOutStart() > -1L ? (float)(now - self.getFadeOutStart()) / 1000.0F : -1.0F;
	}

	@Unique
	private float getFadeInProgress(LoadingOverlayAccessor self, long now) {
		return self.getFadeInStart() > -1L ? (float)(now - self.getFadeInStart()) / 500.0F : -1.0F;
	}

	@Unique
	private void smoothProgress(LoadingOverlayAccessor self) {
		float raw = self.getReload().getActualProgress();
		self.setCurrentProgress(Mth.clamp(self.getCurrentProgress() * 0.95F + raw * 0.050000012F, 0.0F, 1.0F));
	}

	@Unique
	private void renderProgressBar(PoseStack poseStack, LoadingOverlayAccessor self, float fadeOut, int w, int h) {
		if (fadeOut >= 1.0F) return;

		int centerX = w / 2;
		int barHalfWidth = (int)(Math.min(w * 0.75, h) * 0.25 * 4.0 * 0.5);
		int barY = (int)(h * 0.8325);
		float alpha = 1.0F - Mth.clamp(fadeOut, 0.0F, 1.0F);

		drawProgressBar(poseStack,
				centerX - barHalfWidth, barY - 5,
				centerX + barHalfWidth, barY + 5,
				alpha, self.getCurrentProgress()
		);
	}

	@Unique
	private void drawProgressBar(PoseStack poseStack, int x1, int y1, int x2, int y2, float alpha, float progress) {
		int width = x2 - x1;
		int filled = Mth.ceil((width - 2) * progress);
		int barColor = ColorManager.applyAlpha(ColorManager.getBar(), alpha);
		int backgroundColor = ColorManager.applyAlpha(ColorManager.getBarBackground(), alpha);
		int borderColor = ColorManager.applyAlpha(ColorManager.getBorder(), alpha);

        GuiComponent.fill(poseStack, x1 + 1, y1, x2 - 1, y1 + 1, borderColor);
        GuiComponent.fill(poseStack, x1 + 1, y2 - 1, x2 - 1, y2, borderColor);
        GuiComponent.fill(poseStack, x1, y1, x1 + 1, y2, borderColor);
        GuiComponent.fill(poseStack, x2 - 1, y1, x2, y2, borderColor);
        GuiComponent.fill(poseStack, x1 + 1, y1 + 1, x2 - 1, y2 - 1, backgroundColor);
        GuiComponent.fill(poseStack, x1 + 2, y1 + 2, x1 + 2 + filled, y2 - 2, barColor);
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
	private void finish(LoadingOverlayAccessor self, PoseStack poseStack, long now, int w, int h) {
		try {
			self.getReload().checkExceptions();
			self.getOnFinish().accept(Optional.empty());
		} catch (Throwable t) {
			self.getOnFinish().accept(Optional.of(t));
		}

		self.setFadeOutStart(now);

		if (self.getMinecraft().screen != null)
			self.getMinecraft().screen.init(self.getMinecraft(), w, h);
	}
}