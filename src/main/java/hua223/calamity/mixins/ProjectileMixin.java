package hua223.calamity.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Projectile.class})
public abstract class ProjectileMixin extends Entity {
    @Unique
    public boolean calamity$Indestructible;

    public ProjectileMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    public void discard() {
        if (calamity$Indestructible) {
            calamity$Indestructible = false;
        } else super.discard();
    }
}
