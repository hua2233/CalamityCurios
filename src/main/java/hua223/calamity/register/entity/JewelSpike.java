package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

@AutoEntityRegister(sized = {1f, 1f}, trackingRange = 16, velocityUpdates = false)
public class JewelSpike extends Entity {
    private int lifeTime;
    private boolean expanding = true;
    private Player owner;
    @OnlyIn(Dist.CLIENT)
    private float v = 0.8f;
    @OnlyIn(Dist.CLIENT)
    private float v1 = 1f;
    @OnlyIn(Dist.CLIENT)
    private final Quaternionf varQuaternion = new Quaternionf();
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutoutNoCull(CalamityCurios.ModResource("textures/entity/jewel_spike.png"));

    public JewelSpike(EntityType<?> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
        if (level.isClientSide) owner = Minecraft.getInstance().player;
    }

    public static void create(ServerPlayer player, LivingEntity attacker) {
        AABB aabb = player.getBoundingBox();
        AABB box = new AABB(aabb.minX - 4, aabb.minY, aabb.minZ - 4, aabb.maxX + 4, aabb.maxY, aabb.maxZ + 4);
        spawn(player, attacker);
        List<Entity> entities = player.level().getEntities(player, box);
        if (entities.isEmpty()) return;
        byte count = 0;
        for (Entity entity : entities) {
            if (entity instanceof Mob mob) {
                spawn(player, mob);
                if (++count == 3) return;
            }
        }
    }

    private static void spawn(ServerPlayer player, LivingEntity target) {
        JewelSpike spike = CalamityCurios.getEntityType(JewelSpike.class).create(player.level());
        if(spike != null) {
            spike.setPos(target.position());
            spike.owner = player;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
            player.level().addFreshEntity(spike);
        }
    }

        @Override
        public void tick() {
            if (++lifeTime % 4 == 0) {
                if (expanding) {
                    v -= 0.2f;
                    v1 -= 0.2f;
                    if (v <= 0.1) expanding = false;
                } else {
                    v += 0.2f;
                    v1 += 0.2f;
                    if (v >= 1f) discard();
                }

                if (!level().isClientSide) {
                    List<Entity> entities = level().getEntities(this, getBoundingBox());
                    if (entities.isEmpty()) return;
                    DamageSource source = owner.damageSources().playerAttack(owner);
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity)
                            entity.hurt(source, 3);
                    }
                }
            }
        }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<JewelSpike> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(JewelSpike entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            VertexConsumer consumer = buffer.getBuffer(entity.type);
            RenderUtil.reuseQuaternions(entity.varQuaternion, CalamityHelp.UNIT_Y,
                -Mth.rotLerp(partialTick, entity.owner.yRotO, entity.owner.getYRot()));

            //Afterwards, there was no further operation, just rotate the source matrix directly
            poseStack.mulPose(entity.varQuaternion);
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            consumer.vertex(matrix4f, 0, 1, 0)
                .color(255, 255, 255, 255)
                .uv(0, entity.v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, 1, 1, 0)
                .color(255, 255, 255, 255)
                .uv(1, entity.v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, 1, 0, 0)
                .color(255, 255, 255, 255)
                .uv(1, entity.v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, 0, 0, 0)
                .color(255, 255, 255, 255)
                .uv(0, entity.v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull JewelSpike jewelSpike) {
            return CalamityCurios.ModResource("textures/entity/jewel_spike.png");
        }
    }
}
