package com.bouncingelf10.amj.mixin;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.config.ModConfig;
import com.bouncingelf10.amj.internal.ColorManager;
import com.bouncingelf10.amj.internal.MojangAnimFrameManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void onExtractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta,
			CallbackInfo ci) {
		if (!ModConfig.isEnabled())
			return;
		if (AnimatedMojangLogoClient.hasRunOnce && ModConfig.shouldOnlyPlayOnce())
			return;
		ci.cancel();

		LoadingOverlayAccessor self = (LoadingOverlayAccessor) this;
		long now = net.minecraft.util.Util.getMillis();

		tickFadeIn(self, now);
		smoothProgress(self);
		MojangAnimFrameManager.tickPreload();

		float fadeOut = getFadeOutProgress(self, now);
		float fadeIn = getFadeInProgress(self, now);

		if (!MojangAnimFrameManager.hasFinished && self.getFadeOutStart() != -1L) {
			fadeOut = 0.0F;
		}

		if (fadeOut >= 1.0F) {
			if (self.getMinecraft().screen != null) {
				self.getMinecraft().screen.extractRenderStateWithTooltipAndSubtitles(graphics, 0, 0, delta);
			} else {
				self.getMinecraft().gui.extractDeferredSubtitles();
			}
			graphics.nextStratum();

			int alpha = Mth.ceil((1.0F - Mth.clamp(fadeOut - 1.0F, 0.0F, 1.0F)) * 255.0F);
			int bg = (ColorManager.getBackground() & 0x00FFFFFF) | (alpha << 24);
			graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), bg);

			if (fadeOut >= 2.0F) {
				removeOverlay(self);
			}
			return;
		}

		graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), ColorManager.getBackground());
		renderProgressBar(graphics, self, fadeOut);

		if (AnimatedMojangLogoClient.isInit
				&& self.getReload().isDone()
				&& MojangAnimFrameManager.areFramesPreloaded()) {
			renderMojangAnim(graphics);
		}
	}

	@Unique
	private boolean isReadyToFadeOut(LoadingOverlayAccessor self, long now) {
		return !self.getFadeIn() || (self.getFadeInStart() > -1L && now - self.getFadeInStart() >= 1000L);
	}

	@Unique
	private void renderMojangAnim(GuiGraphicsExtractor graphics) {
		if (!MojangAnimFrameManager.hasStarted) {
			MojangAnimFrameManager.start();
		}
		MojangAnimFrameManager.render(graphics);
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
	private void renderProgressBar(GuiGraphicsExtractor graphics, LoadingOverlayAccessor self, float fadeOut) {
		if (fadeOut >= 1.0F)
			return;

		int w = graphics.guiWidth();
		int h = graphics.guiHeight();
		int centerX = w / 2;
		int barHalfWidth = (int) (Math.min(w * 0.75, h) * 0.25 * 4.0 * 0.5);
		int barY = (int) (h * 0.8325);
		float alpha = 1.0F - Mth.clamp(fadeOut, 0.0F, 1.0F);

		drawProgressBar(graphics,
				centerX - barHalfWidth, barY - 5,
				centerX + barHalfWidth, barY + 5,
				alpha, self.getCurrentProgress());
	}

	@Unique
	private void drawProgressBar(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, float alpha,
			float progress) {
		int width = x2 - x1;
		int filled = Math.min(Mth.ceil((width - 2) * progress), width - 4);
		int barColor = ColorManager.applyAlpha(ColorManager.getBar(), alpha);
		int backgroundColor = ColorManager.applyAlpha(ColorManager.getBarBackground(), alpha);
		int borderColor = ColorManager.applyAlpha(ColorManager.getBorder(), alpha);

		graphics.fill(x1 + 1, y1, x2 - 1, y1 + 1, borderColor);
		graphics.fill(x1 + 1, y2 - 1, x2 - 1, y2, borderColor);
		graphics.fill(x1, y1, x1 + 1, y2, borderColor);
		graphics.fill(x2 - 1, y1, x2, y2, borderColor);
		graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, backgroundColor);
		graphics.fill(x1 + 2, y1 + 2, x1 + 2 + filled, y2 - 2, barColor);
	}

	@Unique
	private void removeOverlay(LoadingOverlayAccessor self) {
		self.getMinecraft().setOverlay(null);
	}

	@Unique
	private boolean canFinish(LoadingOverlayAccessor self, float fadeIn) {
		return self.getFadeOutStart() == -1L && self.getReload().isDone() && MojangAnimFrameManager.hasFinished
				&& (!self.getFadeIn() || fadeIn >= 2.0F);
	}
}