package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.entity.CalamityEntity;
import hua223.calamity.util.CalamityDamageSource;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Meteor extends BaseProjectile {
    private boolean sacred;
    @OnlyIn(Dist.CLIENT)
    private RenderType type;
    public Meteor(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setDamage(10);
        this.setDeltaMovement(0, -0.4, 0);
    }

    public static void of(LivingEntity target, ServerPlayer player, boolean isSacred) {
        Level level = target.level;
        Meteor projectile = CalamityEntity.METEOR.get().create(level);
        Vec3 spawnPos;

        if (isSacred) {
            spawnPos = new Vec3(target.getX(), target.getY() + level.random.nextInt(6, 12), target.getZ());
            projectile.sacred = true;
        } else {
            spawnPos = new Vec3(target.getRandomX(1.3),
                target.getY() + level.random.nextInt(6, 12), target.getRandomZ(1.3));
        }

        projectile.setPos(spawnPos);
        projectile.setOwner(player);
        level.addFreshEntity(projectile);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, sacred ? 0 : 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        int data = packet.getData();
        type = RenderType.entityCutoutNoCull(data == 0 ? CalamityCurios.ModResource("textures/entity/sacred_meteor.png")
            : CalamityCurios.ModResource("textures/entity/meteor.png"));
    }

    @Override
    protected void attack(LivingEntity target) {
        target.hurt(CalamityDamageSource.getMagicProjectile(this), damage);
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Render extends EntityRenderer<Meteor> {
        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        @Override
        public ResourceLocation getTextureLocation(Meteor entity) {
            return entity.sacred ? CalamityCurios.ModResource("textures/entity/sacred_meteor.png") :
                CalamityCurios.ModResource("textures/entity/meteor.png");
        }

        @Override
        public void render(Meteor meteor, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
            poseStack.pushPose();

            poseStack.translate(0F, 0.1F, 0F);
            poseStack.scale(0.2F, 0.2F, 0.2F);

            RenderUtil.crossTextureRendering(meteor, bufferSource.getBuffer(meteor.type), poseStack, packedLight);

            poseStack.popPose();
        }
    }
}
