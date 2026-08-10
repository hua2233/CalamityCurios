package hua223.calamity.mixins;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    private static Vec3 collideWithShapes(Vec3 pDeltaMovement, AABB pEntityBB, List<VoxelShape> pShapes) {
        return null;
    }

    /**
     * @author test
     * @reason test
     */
    @Overwrite
    public static Vec3 collideBoundingBox(@Nullable Entity entity, Vec3 vec, AABB collisionBox, Level level, List<VoxelShape> potentialHits) {
        List<VoxelShape> shapes = potentialHits;
        if (entity != null) {
            shapes = new ArrayList<>(potentialHits.size());
            WorldBorder border = level.getWorldBorder();
            AABB aabb = collisionBox.expandTowards(vec);
            double d1 = Math.max(Mth.absMax(aabb.getXsize(), aabb.getZsize()), 1.0F);
            if (border.getDistanceToBorder(entity) < d1 && border.isWithinBounds(entity.getX(), entity.getZ(), d1)) {
                shapes.add(border.getCollisionShape());
            }

            for (VoxelShape shape : level.getBlockCollisions(entity, collisionBox.expandTowards(vec)))
                shapes.add(shape);

            if (border.calamity$Shape != null) shapes.add(border.calamity$Shape);
        }

        return collideWithShapes(vec, collisionBox, shapes);
    }
}
