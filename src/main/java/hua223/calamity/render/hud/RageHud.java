package hua223.calamity.render.hud;

import hua223.calamity.capability.Rage;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.sounds.CalamitySounds;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4i;

@OnlyIn(Dist.CLIENT)
public class RageHud extends EnergyFrameType {
    public static final RageHud INSTANCE = new RageHud();

    private int currentDamage;
    private int levelUpDamage;
    private short shatteredLevel;

    protected RageHud() {
        super(36, 96, 2, 0.4f);
    }

    @Override
    protected Vector4i getBarWHAndBorderWH() {
        return new Vector4i(79, 8, 12, 18);
    }

    @Override
    protected Vector4i getBaseAnimation() {
        return new Vector4i(152, 380, 10, 2);
    }

    public void setShatteredLevel(byte shatteredLevel, int upDamage) {
        currentDamage = 0;
        levelUpDamage = upDamage;
        this.shatteredLevel = shatteredLevel;
    }

    public short getShatteredLevel() {
        return shatteredLevel;
    }

    public void setCurrentDamage(int damage) {
        currentDamage = damage;
    }

    public int getUpProgress() {
        return levelUpDamage == 0 ? 0 : currentDamage / levelUpDamage * 100;
    }

    public int getLevelBonus() {
        return 35 + shatteredLevel * 2;
    }

    @Override
    public void active() {
        active = true;
        CalamitySounds.RAGE_ACTIVATE.playLocalSound();
    }

    @Override
    public void playAnimation() {
        super.playAnimation();
        CalamitySounds.FULL_RAGE.playLocalSound();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    protected float getMaxValue() {
        return Rage.MAX_VALUE;
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        INSTANCE.BAR = atlas.getSprite(CalamityCurios.ModResource("rage_bar"));
        INSTANCE.MAIN = atlas.getSprite(CalamityCurios.ModResource("rage_hud"));
        INSTANCE.DISPLAY = atlas.getSprite(CalamityCurios.ModResource("rage_display"));
        INSTANCE.ANIMATIONS = atlas.getSprite(CalamityCurios.ModResource("rage_animation"));
    }
}
