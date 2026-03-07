package hua223.calamity.register.entity.projectiles;

import hua223.calamity.main.CalamityCurios;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public non-sealed class ExcelsusMain extends ExProjectile {
    public ExcelsusMain(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected ResourceLocation getTexture() {
        return CalamityCurios.ModResource("textures/entity/excelsus_main.png");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected ResourceLocation getGlowTexture() {
        return CalamityCurios.ModResource("textures/entity/excelsus_main_glow.png");
    }
}
