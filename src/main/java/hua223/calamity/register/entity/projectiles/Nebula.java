package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.integration.curios.item.NebulousCore;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.damage.CalamityDamageTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Nebula extends Projectile {
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutoutNoCull(CalamityCurios.ModResource("textures/entity/nebula.png"));
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    private double offsetX;
    private double offsetZ;

    public Nebula(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public static UUID spawnAroundPlayer(Player player) {
        Level level = player.level();
        Nebula nebula = CalamityEntity.NEBULA.get().create(level);
        if (nebula != null) {
            nebula.setOwner(player);
            Vec3 position = player.getEyePosition();
            nebula.offsetX  = level.random.nextDouble() * 3 - 1.5;
            nebula.offsetX += nebula.offsetX < 0 ? -0.3 : 0.3;
            nebula.offsetZ  = level.random.nextDouble() * 3 - 1.5;
            nebula.offsetZ += nebula.offsetZ < 0 ? -0.3 : 0.3;

            nebula.setPos(position.x + nebula.offsetX,
                position.y, position.z + nebula.offsetZ);
            level.addFreshEntity(nebula);
            return nebula.uuid;
        }

        return null;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        Level level = level();
        if (level.isClientSide) {
            if (lSteps > 0) {
                double d5 = getX() + (lx - getX()) / (double) lSteps;
                double d6 = getY() + (ly - getY()) / (double) lSteps;
                double d7 = getZ() + (lz - getZ()) / (double) lSteps;
                --lSteps;
                setPos(d5, d6, d7);
            }
        } else {
            Entity owner = getOwner();
            if (tickCount >= 200 || owner == null) {
                discard();
                return;
            }

            setPos(owner.getEyePosition().add(offsetX, 0, offsetZ));
            if (tickCount % 3 != 0) return;
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox());
            boolean remove = false;
            for (LivingEntity target : entities)
                if (target.isAlive() && target != owner && !target.isAlliedTo(owner)) {
                    remove = true;
                    target.hurt(CalamityDamageSource.source(CalamityDamageTypes.MAGIC_PROJECTILE, this, owner), 12);
                    if (!target.hasEffect(CalamityEffects.GOD_SLAYER_INFERNO.get()))
                        target.addEffect(new MobEffectInstance(CalamityEffects.GOD_SLAYER_INFERNO.get(), 100, 0));
                }

            if (remove) discard();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yr, float xr, int steps, boolean b) {
        lx = x;
        ly = y;
        lz = z;
        lSteps = steps;
    }

    @Override
    public void discard() {
        super.discard();
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Render extends EntityRenderer<Nebula> {
        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull Nebula entity) {
            return CalamityCurios.ModResource("textures/entity/nebula.png");
        }

        @Override
        public void render(@NotNull Nebula nebula, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
            poseStack.pushPose();
            poseStack.translate(0F, 0.1F, 0F);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            RenderUtil.crossTextureRendering(nebula, bufferSource.getBuffer(nebula.type), poseStack, packedLight);
            poseStack.popPose();
        }
    }
}
