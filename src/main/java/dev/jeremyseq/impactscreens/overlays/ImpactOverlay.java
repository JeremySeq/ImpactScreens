package dev.jeremyseq.impactscreens.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.jeremyseq.impactscreens.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ImpactOverlay {

    private static long startTime = -1;

    // phase durations, tweak freely
    private static final long FADE_IN_DURATION_MS = 1000;
    private static final long IMAGE_HOLD_DURATION_MS = 6000;
    private static final long IMAGE_FADE_OUT_DURATION_MS = 500;
    private static final long TEXT_DELAY_DURATION_MS = 2000;
    private static final long MS_PER_CHARACTER = 130;
    private static final long TEXT_HOLD_DURATION_MS = 1000;
    private static final long FINAL_FADE_OUT_DURATION_MS = 1000;

    private static String imageName = null;
    private static String text = null;
    private static int textColor = 0xFFFFFF;
    private static long textTypeDurationMs = 0;

    private static SoundInstance typingSoundInstance = null;
    private static boolean typingSoundStarted = false;

    private static final int IMAGE_SIZE = 128;

    // floating text config
    private static final String[] FLOATING_PHRASES = {
            "\"I must defend my home.\"",
            "Fight for freedom.",
            "Defend what's right!",
            "\"I'm stronger than the rest.\"",
            "Don't falter.",
            "You can't fall back, keep fighting.",
            "\"I can surpass my limits!\""
    };

    private static final long FLOAT_SPAWN_INTERVAL_MS = 400; // how often a new one can spawn
    private static final long FLOAT_FADE_IN_MS = 250;
    private static final long FLOAT_HOLD_MS = 900;
    private static final long FLOAT_FADE_OUT_MS = 250;
    private static final int FLOAT_COLOR = 0xFFFFFF;
    private static final int MAX_CONCURRENT_FLOATING_TEXTS = 5; // how many can be on screen at once

    private static final Random RANDOM = new Random();
    private static final List<FloatingText> floatingTexts = new ArrayList<>();
    private static long nextFloatSpawnTime = -1;

    // screen shake config
    private static final float SHAKE_AMPLITUDE_PX = 0.25f; // max pixel offset in any direction

    private static final Random SHAKE_RANDOM = new Random();

    private static class FloatingText {
        final String text;
        final int x;
        final int y;
        final int width;
        final int height;
        final long spawnTime;

        FloatingText(String text, int x, int y, int width, int height, long spawnTime) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.spawnTime = spawnTime;
        }
    }

    public static void start(String image, String text, int textColor) {
        startTime = System.currentTimeMillis();
        imageName = image;
        ImpactOverlay.text = text;
        ImpactOverlay.textColor = textColor;
        ImpactOverlay.textTypeDurationMs = text != null ? (long) text.length() * MS_PER_CHARACTER : 0;

        floatingTexts.clear();
        nextFloatSpawnTime = startTime;

        typingSoundStarted = false;
        typingSoundInstance = null;

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.BUILD_UP.get(), 1.0f, 1.0f));
    }

    public static void stop() {
        startTime = -1;
        imageName = null;
        text = null;
        floatingTexts.clear();

        stopTypingSound();
    }

    private static void stopTypingSound() {
        if (typingSoundInstance != null) {
            Minecraft.getInstance().getSoundManager().stop(typingSoundInstance);
            typingSoundInstance = null;
        }
        typingSoundStarted = false;
    }

    public static boolean isActive() {
        return startTime != -1;
    }

    public static void drawImpactOverlay(GuiGraphics guiGraphics, Window window) {
        if (startTime == -1) return;

        long now = System.currentTimeMillis();
        long elapsed = now - startTime;

        long imageFadeOutStart = FADE_IN_DURATION_MS + IMAGE_HOLD_DURATION_MS;
        long textDelayStart = imageFadeOutStart + IMAGE_FADE_OUT_DURATION_MS;
        long textTypeStart = textDelayStart + TEXT_DELAY_DURATION_MS;
        long textHoldStart = textTypeStart + textTypeDurationMs;
        long finalFadeOutStart = textHoldStart + TEXT_HOLD_DURATION_MS;
        long totalDuration = finalFadeOutStart + FINAL_FADE_OUT_DURATION_MS;

        if (elapsed >= totalDuration) {
            stop();
            return;
        }

        float bgAlpha;
        float imageAlpha;
        float textAlpha;
        int visibleChars;

        if (elapsed < FADE_IN_DURATION_MS) {
            float t = (float) elapsed / (float) FADE_IN_DURATION_MS;
            bgAlpha = t;
            imageAlpha = t;
            textAlpha = 0f;
            visibleChars = 0;
        } else if (elapsed < imageFadeOutStart) {
            bgAlpha = 1f;
            imageAlpha = 1f;
            textAlpha = 0f;
            visibleChars = 0;
        } else if (elapsed < textDelayStart) {
            long t = elapsed - imageFadeOutStart;
            bgAlpha = 1f;
            imageAlpha = 1f - ((float) t / (float) IMAGE_FADE_OUT_DURATION_MS);
            textAlpha = 0f;
            visibleChars = 0;
        } else if (elapsed < textTypeStart) {
            bgAlpha = 1f;
            imageAlpha = 0f;
            textAlpha = 1f;
            visibleChars = 0;
        } else if (elapsed < textHoldStart) {
            // typing out characters
            bgAlpha = 1f;
            imageAlpha = 0f;
            textAlpha = 1f;

            if (!typingSoundStarted) {
                typingSoundInstance = SimpleSoundInstance.forUI(ModSounds.TYPING.get(), 1.0f, 1.0f);
                Minecraft.getInstance().getSoundManager().play(typingSoundInstance);

                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(ModSounds.WAR_CRY.get(), 1.0f, 1.0f)
                );

                typingSoundStarted = true;
            }


            long t = elapsed - textTypeStart;
            int totalChars = text != null ? text.length() : 0;
            float progress = textTypeDurationMs > 0 ? (float) t / (float) textTypeDurationMs : 1f;
            visibleChars = Math.round(progress * totalChars);
        } else if (elapsed < finalFadeOutStart) {
            // full text held, stop the sound
            bgAlpha = 1f;
            imageAlpha = 0f;
            textAlpha = 1f;
            visibleChars = text != null ? text.length() : 0;

            if (typingSoundStarted) {
                stopTypingSound();
            }
        } else {
            long t = elapsed - finalFadeOutStart;
            float fadeOut = 1f - ((float) t / (float) FINAL_FADE_OUT_DURATION_MS);
            bgAlpha = fadeOut;
            imageAlpha = 0f;
            textAlpha = fadeOut;
            visibleChars = text != null ? text.length() : 0;
        }

        bgAlpha = Math.max(0f, Math.min(1f, bgAlpha));
        imageAlpha = Math.max(0f, Math.min(1f, imageAlpha));
        textAlpha = Math.max(0f, Math.min(1f, textAlpha));

        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();

        int bgAlphaInt = (int) (bgAlpha * 255) & 0xFF;
        int bgColor = (bgAlphaInt << 24);

        // shake
        boolean shaking = elapsed < textDelayStart;
        float shakeX = shaking ? (SHAKE_RANDOM.nextFloat() * 2f - 1f) * SHAKE_AMPLITUDE_PX : 0f;
        float shakeY = shaking ? (SHAKE_RANDOM.nextFloat() * 2f - 1f) * SHAKE_AMPLITUDE_PX : 0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        guiGraphics.fill(0, 0, width, height, bgColor);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(shakeX, shakeY, 0);

        if (imageName != null && imageAlpha > 0f) {
            ImpactImages.ImpactImage image = ImpactImages.get(imageName);
            if (image != null && image.width() > 0 && image.height() > 0) {
                // scale to fit within IMAGE_SIZE x IMAGE_SIZE, preserving aspect ratio
                float scale = Math.min(
                        (float) IMAGE_SIZE / image.width(),
                        (float) IMAGE_SIZE / image.height()
                );
                int drawWidth = Math.round(image.width() * scale);
                int drawHeight = Math.round(image.height() * scale);

                int x = (width - drawWidth) / 2;
                int y = (height - drawHeight) / 2;

                drawTexturedQuad(guiGraphics, image.location(), x, y, drawWidth, drawHeight, imageAlpha);
            }
        }

        // floating text
        boolean imagePhaseActive = elapsed < textDelayStart;
        updateAndDrawFloatingTexts(guiGraphics, window, now, imagePhaseActive);

        guiGraphics.pose().popPose();

        if (text != null && !text.isEmpty() && textAlpha > 0f && visibleChars > 0) {
            String shown = text.substring(0, Math.min(visibleChars, text.length()));
            drawFadedText(guiGraphics, window, shown, textColor, textAlpha);
        }
    }

    private static void updateAndDrawFloatingTexts(GuiGraphics guiGraphics, Window window, long now, boolean spawningAllowed) {
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();

        if (spawningAllowed
                && nextFloatSpawnTime != -1
                && now >= nextFloatSpawnTime
                && floatingTexts.size() < MAX_CONCURRENT_FLOATING_TEXTS) {

            String phrase = FLOATING_PHRASES[RANDOM.nextInt(FLOATING_PHRASES.length)];

            Font font = Minecraft.getInstance().font;
            int textWidth = font.width(phrase);
            int textHeight = font.lineHeight;

            int margin = 10;
            int maxX = Math.max(margin, width - textWidth - margin);
            int maxY = Math.max(margin, height - textHeight - margin);

            int exclusionPadding = 20;
            int exclusionLeft = (width - IMAGE_SIZE) / 2 - exclusionPadding;
            int exclusionTop = (height - IMAGE_SIZE) / 2 - exclusionPadding;
            int exclusionRight = exclusionLeft + IMAGE_SIZE + exclusionPadding * 2;
            int exclusionBottom = exclusionTop + IMAGE_SIZE + exclusionPadding * 2;

            int spacing = 4; // min gap between floating texts
            int x = margin;
            int y = margin;
            boolean found = false;

            for (int attempt = 0; attempt < 30; attempt++) {
                int candidateX = margin + (maxX > margin ? RANDOM.nextInt(maxX - margin) : 0);
                int candidateY = margin + (maxY > margin ? RANDOM.nextInt(maxY - margin) : 0);

                boolean overlapsImage = candidateX < exclusionRight
                        && candidateX + textWidth > exclusionLeft
                        && candidateY < exclusionBottom
                        && candidateY + textHeight > exclusionTop;

                boolean overlapsExisting = false;
                for (FloatingText existing : floatingTexts) {
                    boolean intersects = candidateX < existing.x + existing.width + spacing
                            && candidateX + textWidth + spacing > existing.x
                            && candidateY < existing.y + existing.height + spacing
                            && candidateY + textHeight + spacing > existing.y;
                    if (intersects) {
                        overlapsExisting = true;
                        break;
                    }
                }

                if (!overlapsImage && !overlapsExisting) {
                    x = candidateX;
                    y = candidateY;
                    found = true;
                    break;
                }
            }

            if (found) {
                floatingTexts.add(new FloatingText(phrase, x, y, textWidth, textHeight, now));
                nextFloatSpawnTime = now + FLOAT_SPAWN_INTERVAL_MS;
            } else {
                nextFloatSpawnTime = now + FLOAT_SPAWN_INTERVAL_MS;
            }
        }

        long floatTotalMs = FLOAT_FADE_IN_MS + FLOAT_HOLD_MS + FLOAT_FADE_OUT_MS;

        Iterator<FloatingText> it = floatingTexts.iterator();
        while (it.hasNext()) {
            FloatingText ft = it.next();
            long age = now - ft.spawnTime;

            if (age >= floatTotalMs) {
                it.remove();
                continue;
            }

            float alpha;
            if (age < FLOAT_FADE_IN_MS) {
                alpha = (float) age / (float) FLOAT_FADE_IN_MS;
            } else if (age < FLOAT_FADE_IN_MS + FLOAT_HOLD_MS) {
                alpha = 1f;
            } else {
                long t = age - (FLOAT_FADE_IN_MS + FLOAT_HOLD_MS);
                alpha = 1f - ((float) t / (float) FLOAT_FADE_OUT_MS);
            }
            alpha = Math.max(0f, Math.min(1f, alpha)) * 0.7f;

            drawFloatingText(guiGraphics, ft.text, ft.x, ft.y, FLOAT_COLOR, alpha);
        }
    }

    private static void drawFloatingText(GuiGraphics guiGraphics, String text, int x, int y, int rgb, float alpha) {
        Font font = Minecraft.getInstance().font;

        int a = (int) (alpha * 255) & 0xFF;
        if (a < 25) return; // stupid low alpha workaround

        int color = (a << 24) | (rgb & 0x00FFFFFF);
        guiGraphics.drawString(font, text, x, y, color, false);
    }

    private static void drawFadedText(GuiGraphics guiGraphics, Window window, String shownText, int rgb, float alpha) {
        Font font = Minecraft.getInstance().font;

        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();

        int x = (width - font.width(shownText)) / 2;
        int y = (height - font.lineHeight) / 2;

        int a = (int) (alpha * 255) & 0xFF;
        if (a < 25) {
            return;
        }
        int color = (a << 24) | (rgb & 0x00FFFFFF);

        guiGraphics.drawString(font, shownText, x, y, color, false);
    }

    private static void drawTexturedQuad(GuiGraphics guiGraphics, ResourceLocation texture,
                                         int x, int y, int width, int height, float alpha) {
        Matrix4f matrix = guiGraphics.pose().last().pose();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
        );

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);

        int a = (int) (alpha * 255) & 0xFF;
        int color = (a << 24) | 0x00FFFFFF;

        var buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        buffer.addVertex(matrix, x, y + height, 0).setUv(0f, 1f).setColor(color);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(1f, 1f).setColor(color);
        buffer.addVertex(matrix, x + width, y, 0).setUv(1f, 0f).setColor(color);
        buffer.addVertex(matrix, x, y, 0).setUv(0f, 0f).setColor(color);

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
    }
}