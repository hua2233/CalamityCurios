    package hua223.calamity.render.screen;

import com.mojang.blaze3d.platform.Window;
import hua223.calamity.render.screen.particleset.ConvergingEnergySet;
import hua223.calamity.render.screen.particleset.LineStreakParticleSet;
import hua223.calamity.util.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: It may be used in the future
@OnlyIn(Dist.CLIENT)
public class ConvergingEnergyRenderer extends ScreenEffectRenderer {//
    public static final int BACKGROUND_DIM_TIME = 630;
    public static final int ENERGY_CHARGE_UP_TIME = 160;
    public static final int IDLE_ENERGY_TIME = 138 * 3;

    public final Window window = Minecraft.getInstance().getWindow();
    private final Vector2f center = new Vector2f();
    public final RandomSource random = RandomSource.create(322);
    public int time;
    private float partialTick;
    private final LineStreakParticleSet streakParticleSet;
    private final ConvergingEnergySet convergingEnergySet;
    private float energyChargeUpCompletion;

    public ConvergingEnergyRenderer() {
        this.convergingEnergySet = new ConvergingEnergySet(this);
        streakParticleSet = new LineStreakParticleSet(this);
    }

    public Vector2f getCenter() {
        center.set(window.getGuiScaledWidth() / 2f, window.getGuiScaledHeight() / 2f);
        return center;
    }

    @Override
    protected boolean enableTick() {
        return true;
    }

    public float getPartialTick() {
        return partialTick;
    }

    @Override
    public void tick() {
        if (++time < ENERGY_CHARGE_UP_TIME)
            energyChargeUpCompletion = Mth.inverseLerp(time, 0f, ConvergingEnergyRenderer.ENERGY_CHARGE_UP_TIME);
//        streakParticleSet.update();
        convergingEnergySet.update();
    }

    public float getEnergyChargeUpCompletion() {
        return energyChargeUpCompletion;
    }

    @Override
    public boolean render(float partialTick, Minecraft minecraft) {
        this.partialTick = partialTick;
        convergingEnergySet.drawSet();
//        streakParticleSet.drawSet();


//        if (time % 2 == 0 && time < ENERGY_CHARGE_UP_TIME + IDLE_ENERGY_TIME - 105) {
//            for (int i = 0; i < 5; i++) {
//                if (ENERGY_CHARGE_UP_TIME <= 0.25f || !Main.rand.NextBool(energyChargeUpCompletion * energyChargeUpCompletion * energyChargeUpCompletion))
//                    continue;
//
//                float energySpawnRadius = 1000f + Main.rand.NextFloat(energyChargeUpCompletion * 1550f);
//                Vector2 energySpawnPosition = Position + Main.rand.NextVector2CircularEdge(energySpawnRadius, energySpawnRadius) + Main.rand.NextVector2Circular(100f, 100f);
//                NewProjectileBetter(new EntitySource_WorldEvent(), energySpawnPosition, Vector2.Zero, ModContent.ProjectileType<GenesisConvergingEnergy>(), 0, 0f);
//            }
//        }

        return false;
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
