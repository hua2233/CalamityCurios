package hua223.calamity.render.font;

import hua223.calamity.util.ICalamityFont;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class BurnishedAuric extends Font implements ICalamityFont {
    public static final BurnishedAuric INSTANCE = new BurnishedAuric();

    private BurnishedAuric() {
        super(Minecraft.getInstance().font.fonts, false);
    }

    static float lastFlashTime;
    static boolean isFlashing;

    @Override
    @SuppressWarnings("ConstantConditions")
    //Should I render it?
    public void reRender(Matrix4f matrix, String describe, float x, float y, MultiBufferSource buffer) {
        final int time = RenderUtil.getLocalTick();
        if (Math.abs(time - lastFlashTime) > 6) {
            if (random.nextFloat() < 0.002f) {
                isFlashing = true;
                lastFlashTime = time;
            } else isFlashing = false;
        }

        float pulsing = 1.1F + (float) Math.sin(time * 0.2f) * 0.25F;
        Vector2d p = new Vector2d(0, 0);
        int textColor = ARGB32.color(255, 78, 55, 6);
        Matrix4f transientMatrix = RenderUtil.TRANSIENT_MATRIX;
        transientMatrix.set(matrix);
        for (float f = 0f; f < Mth.TWO_PI; f += 0.79f) {
            p.x = pulsing;
            p.rotatedBy(f + time * 0.05 % Mth.TWO_PI, Vector2d.ZERO, true);
            renderText(describe, x + (float) p.x, y + (float) p.y, textColor, false,
                transientMatrix, buffer, DisplayMode.NORMAL, 0, 15728880);

            if (isFlashing) {
                Vector2d offset = Vector2d.nextVector2Circular(1.2f, .6f, random);
                transientMatrix.translate((float) offset.x, (float) offset.y, 0);
            }
        }

        textColor = isFlashing ? ARGB32.color(255, 0, 255, 255)
            : ARGB32.color(255, 255, 220, 22);

        //BaseColor
        drawMultiShadow(describe, x, y, textColor, transientMatrix, buffer, DisplayMode.NORMAL);
        renderText(describe, x, y, ARGB32.color(255, 77, 0, 33), false,
            transientMatrix, buffer, DisplayMode.NORMAL, 0, 15728880);

        float shineWidth = 9f; //set to charSize
        float shineSpeed = 1f;

        float shineDisp = time * shineSpeed; //predicted current location of the shine based on the time and speed
        float shinePos = (shineDisp % (width(describe) + shineWidth));

        if (isFlashing) {
            Vector2d offset = Vector2d.nextVector2Circular(1f, 2.2F, random);
            x += (float) offset.x;
            y += (float) offset.y;
        }

        Vector2d charSize = new Vector2d(0, 0);
        Vector4i color = new Vector4i();
        float charOffsetX = x;
        for (int i = 0; i < describe.length(); i++) {
            String c = String.valueOf(describe.charAt(i));
            charSize.set(width(c), lineHeight);

            float centerX = (float) (charOffsetX + charSize.x / 2f + 2f);
            float dist = Math.abs(centerX - (x + shinePos - shineWidth * 0.15f));
            float intensity = 1f - Mth.clamp(dist / shineWidth, 0f, 1f);

            if (intensity > 0f) {
                if (isFlashing) color.set(255, 90, 207, 255);
                else color.set(255,254, 231, 117);
                RenderUtil.multiplyColor(color, isFlashing ? intensity * 2f : intensity, color);

                int shineColor = ARGB32.color(255, color.y, color.z, color.w);
                renderText(c, charOffsetX, y, shineColor, false, transientMatrix
                    , buffer, DisplayMode.NORMAL, 0, 15728880);
            }

            charOffsetX += (float) (charSize.x - describe.length() * 0.0085f);
        }
    }

    @Override
    public int getLineHeight() {
        return lineHeight;
    }

    @Override
    public int getWidth(String text) {
        return width(text);
    }
}

