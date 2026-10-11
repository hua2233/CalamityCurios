package hua223.calamity.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.capability.Adrenaline;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.sounds.CalamitySounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.joml.Vector4i;

public class AdrenalineHud extends EnergyFrameType {
    public static final AdrenalineHud INSTANCE = new AdrenalineHud();
    private static final NanoMachinesHud NANO_MACHINES_HUD = new NanoMachinesHud();

    private boolean nanoMode;

    protected AdrenalineHud() {
        super(32, 145, 4, 0.4f);
    }

    @Override
    public void render(ForgeGui forgeGui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (nanoMode) NANO_MACHINES_HUD.render(forgeGui, guiGraphics, partialTick, screenWidth, screenHeight);
        else super.render(forgeGui, guiGraphics, partialTick, screenWidth, screenHeight);
    }

    @Override
    public void setProgress(float value) {
        if (nanoMode) NANO_MACHINES_HUD.setProgress(value);
        else super.setProgress(value);
    }

    @Override
    public void playAnimation() {
        if (nanoMode) NANO_MACHINES_HUD.playAnimation();
        else super.playAnimation();
        CalamitySounds.FULL_ADRENALINE.playLocalSound();
    }

    @Override
    protected Vector4i getBarWHAndBorderWH() {
        return new Vector4i(80, 8, 12, 14);
    }

    @Override
    protected Vector4i getBaseAnimation() {
        return new Vector4i(172, 700, 10, 2);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (nanoMode) NANO_MACHINES_HUD.setEnabled(enabled);
        else {
            this.enabled = enabled;
            if (!enabled) INSTANCE.clear();
        }
    }

    @Override
    public void setDisplayCount(int count) {
        super.setDisplayCount(count);
        NANO_MACHINES_HUD.displayCount = displayCount;
    }

    public void setModel(boolean model) {
        if (!enabled) nanoMode = model;
    }

    public boolean isNanoMode() {
        return nanoMode;
    }

    @Override
    protected float getMaxValue() {
        return Adrenaline.MAX;
    }

    @Override
    public void active() {
        if (nanoMode) NANO_MACHINES_HUD.active();
        else {
            active = true;
            CalamitySounds.ADRENALINE_ACTIVATE.playLocalSound();
        }
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        INSTANCE.BAR = atlas.getSprite(CalamityCurios.ModResource("adrenaline_bar"));
        INSTANCE.MAIN = atlas.getSprite(CalamityCurios.ModResource("adrenaline_hud"));
        INSTANCE.DISPLAY = atlas.getSprite(CalamityCurios.ModResource("adrenaline_display"));
        INSTANCE.ANIMATIONS = atlas.getSprite(CalamityCurios.ModResource("adrenaline_animation"));

        NANO_MACHINES_HUD.DISPLAY = INSTANCE.DISPLAY;
        NANO_MACHINES_HUD.BAR = atlas.getSprite(CalamityCurios.ModResource("nano_bar"));
        NANO_MACHINES_HUD.FULL = atlas.getSprite(CalamityCurios.ModResource("nano_full"));
        NANO_MACHINES_HUD.MAIN = atlas.getSprite(CalamityCurios.ModResource("nano_machines"));
        NANO_MACHINES_HUD.ANIMATIONS = atlas.getSprite(CalamityCurios.ModResource("nano_animation"));
    }
}

class NanoMachinesHud extends EnergyFrameType {
    TextureAtlasSprite FULL;

    private boolean fullMode;
    private short mainTextureTick;
    private final float[] nanoFullInfo;
    private final float[] nanoMachinesInfo;

    protected NanoMachinesHud() {
        super(36, 145, 2, 0.4f);
        nanoFullInfo = createAnimationTextureInfo(mainTextureInfo, new Vector4i(104, 180, 5, 3));
        nanoMachinesInfo = createAnimationTextureInfo(mainTextureInfo, new Vector4i(104, 396, 11, 3));
        animationTextureInfo[1] += 1.6f;
        animationTextureInfo[3] += 1.6f;
    }

    @Override
    protected Vector4i getBarWHAndBorderWH() {
        return new Vector4i(80, 10, 12, 16);
    }

    @Override
    public void setProgress(float value) {
        super.setProgress(value);

        if (finalProgress == 1) {
            fullMode = true;
            mainTextureTick = (short) (nanoFullInfo[4] * nanoFullInfo[5]);
        } else if (finalProgress == 0) {
            fullMode = false;
            mainTextureTick = (short) (nanoMachinesInfo[4] * nanoMachinesInfo[5]);
        }
    }

    @Override
    protected void onTick() {
        super.onTick();

        if (--mainTextureTick < 0) {
            float[] modeInfo = fullMode ? nanoFullInfo : nanoMachinesInfo;
            mainTextureTick = (short) (modeInfo[4] * modeInfo[5]);
        }
    }

    @Override
    protected void renderMain(float[] mainTextureInfo, PoseStack stack) {
        renderAnimation(mainTextureTick, fullMode ? FULL : MAIN, stack, fullMode ? nanoFullInfo : nanoMachinesInfo);
    }

    @Override
    protected Vector4i getBaseAnimation() {
        return new Vector4i(172, 700, 10, 2);
    }

    @Override
    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) clear();
    }

    @Override
    protected void clear() {
        super.clear();
        mainTextureTick = -1;
    }

    @Override
    protected float getMaxValue() {
        return Adrenaline.MAX;
    }

    @Override
    public void active() {
        active = true;
        CalamitySounds.NANO_ACTIVATE.playLocalSound();
    }
}
