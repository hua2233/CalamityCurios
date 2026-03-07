package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.integration.curios.item.NebulousCore;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.util.CalamityDamageSource;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.RenderUtil;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Nebula extends BaseProjectile {
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutout(CalamityCurios.ModResource("textures/entity/nebula.png"));
    private Vec3 lastPos = Vec3.ZERO;

    public Nebula(EntityType<? extends Projectile> entityType, Level pLevel) {
        super(entityType, pLevel);
        this.setNoGravity(true);
    }

    public static void spawnAroundPlayer(Player player) {
        Level level = player.level;
        Nebula nebula = CalamityEntity.NEBULA.get().create(level);
        nebula.setOwner(player);
        nebula.lastPos = player.getEyePosition();

        Vec3 offset = new Vec3(
            (level.random.nextDouble() - 0.5) * 2,
            0,
            (level.random.nextDouble() - 0.5) * 2
        );

        nebula.setPos(player.getX() + offset.x,
            player.getY(),
            player.getZ() + offset.z);

        Vec3 motion = offset.normalize().scale(0.15);
        nebula.setDeltaMovement(new Vec3(motion.x, 0.15, motion.z));

        level.addFreshEntity(nebula);
    }

    @Override
    public void tick() {
        superTick();
        if (tickCount++ >= lifeTime || getOwner() == null) {
            removeCount();
            discard();
            return;
        }

        logic();
        if (level.isClientSide) return;

        AABB boundingBox = new AABB(position(), position()).inflate(0.5); // 0.3是检测半径
        List<Entity> entities = level.getEntities(this, boundingBox);

        if (!entities.isEmpty()) {
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity && entity != getOwner()) {
                    onHitEntity(new EntityHitResult(entity));
                    return;
                }
            }
        }
    }

    @Override
    protected void logic() {
        if (level.isClientSide) {
            if (lSteps > 0) {
                double d5 = getX() + (lx - getX()) / (double) lSteps;
                double d6 = getY() + (ly - getY()) / (double) lSteps;
                double d7 = getZ() + (lz - getZ()) / (double) lSteps;
                --lSteps;
                setPos(d5, d6, d7);
            }
        } else {
            setPos(position().add(getMove()));

            Vec3 motion = this.getDeltaMovement();
            setPos(position().add(motion));
            this.setDeltaMovement(motion.scale(0.9));

            if (random.nextFloat() < 0.1f) {
                this.setDeltaMovement(motion.add(
                    (random.nextFloat() - 0.5f) * 0.02f,
                    (random.nextFloat() - 0.5f) * 0.02f,
                    (random.nextFloat() - 0.5f) * 0.02f
                ));
            }
        }
    }

    private Vec3 getMove() {
        Vec3 position = getOwner().getEyePosition();
        double x = position.x - lastPos.x,
            y = position.y - lastPos.y,
            z = position.z - lastPos.z;
        lastPos = position;
        return new Vec3(x, y, z);
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(CalamityDamageSource.getMagicProjectile(this), 12);
        if (!target.hasEffect(CalamityEffects.GOD_SLAYER_INFERNO.get())) {
            target.addEffect(new MobEffectInstance(CalamityEffects.GOD_SLAYER_INFERNO.get(), 100, 0));
        }
        removeCount();
    }

    private void removeCount() {
        float[] counters = GlobalCuriosStorage.getCountStorages(getOwner(), NebulousCore.class);
        if (counters != null) counters[1]--;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Render extends EntityRenderer<Nebula> {
        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public ResourceLocation getTextureLocation(Nebula pEntity) {
            return CalamityCurios.ModResource("textures/entity/nebula.png");
        }

        @Override
        public void render(Nebula nebula, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
            poseStack.pushPose();

            poseStack.translate(0F, 0.1F, 0F);
            poseStack.scale(0.1F, 0.1F, 0.1F);

            RenderUtil.crossTextureRendering(nebula, bufferSource.getBuffer(nebula.type), poseStack, packedLight);

            poseStack.popPose();
        }
    }
}
