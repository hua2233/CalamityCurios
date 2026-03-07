package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CalamityDamageSource;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2d;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class UniverseSplitterSmallBeam extends Entity implements IEntityAdditionalSpawnData {
    @OnlyIn(Dist.CLIENT)
    public static final int TIME_LIFT = 40;
    @OnlyIn(Dist.CLIENT)
    private static final float FADEIN_TIME = 8;
    @OnlyIn(Dist.CLIENT)
    private float scale;
    @OnlyIn(Dist.CLIENT)
    private float rotationOld;
    @OnlyIn(Dist.CLIENT)
    private final RenderType head = RenderType.entityTranslucent(
        CalamityCurios.ModResource("textures/entity/universe_splitter_small_beam_begin.png"));
    @OnlyIn(Dist.CLIENT)
    private final RenderType body = RenderType.entityTranslucent(
        CalamityCurios.ModResource("textures/entity/universe_splitter_small_beam_mid.png"));
    @OnlyIn(Dist.CLIENT)
    private final RenderType tail = RenderType.entityTranslucent(
        CalamityCurios.ModResource("textures/entity/universe_splitter_small_beam_end.png"));

    private static final float MOVEMENT_TIME = 20;
    private static final float ANGLE_MAX = Mth.TWO_PI / 15f;
    float ai;
    Vector2d velocity;
    Player owner;
    private float rotation;

    public UniverseSplitterSmallBeam(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static void onHit(Level level, Entity attacker, Player owner, AABB box, float hurt, MobEffect effect) {
        List<Entity> targets = level.getEntities(attacker, box,
            target -> target.isPickable() && target.isAlive() && !(target instanceof Player));

        if (!targets.isEmpty()) {
            DamageSource source = new CalamityDamageSource("player")
                .setOwnerAndIndirect(owner, attacker).setNoDecay(hurt).setMagic();
            for (Entity target : targets)
                if (target instanceof LivingEntity living) {
                    living.hurt(source, hurt);
                    if (effect != null && living.isAlive() && !living.hasEffect(effect))
                        living.addEffect(new MobEffectInstance(effect, 100, 0));
                }
        }
    }

    @Override
    public void tick() {
        if (tickCount > TIME_LIFT) {
            discard();
            return;
        }

        if (tickCount < MOVEMENT_TIME) {
            double v = tickCount / MOVEMENT_TIME;

            velocity = Vector2d.toRotationVector2(ai + (float) Math.cos(v * 4d) *
                CalamityHelp.cosineInterpolation(1f, 0f, (float) v) * ANGLE_MAX);
        }

        if (level.isClientSide) rotationOld = rotation;
        rotation = velocity.toRotation() - Mth.HALF_PI;

        if (level.isClientSide) {
            if (tickCount < FADEIN_TIME)
                scale = Mth.lerp(tickCount / FADEIN_TIME, 0.01f, 0.3f);
            else if (tickCount > MOVEMENT_TIME && tickCount <= MOVEMENT_TIME + 8)
                scale = Mth.lerp((tickCount - MOVEMENT_TIME) / 8, 0.3f, 0.6f);
            else if (tickCount > 35)
                scale = Mth.lerp((tickCount - 35) / 5f, 0.6f, 0.01f);
        } else if (tickCount % 5 == 0)
            onHit(level, this, owner, CalamityHelp.rotateAABBAroundZAxis(getBoundingBox(), position(), rotation), 14f, null);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.getUUID());
        buffer.writeInt(this.getId());
        buffer.writeDouble(velocity.x);
        buffer.writeDouble(velocity.y);
        buffer.writeFloat(ai);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readSpawnData(FriendlyByteBuf friendlyByteBuf) {
        this.setUUID(friendlyByteBuf.readUUID());
        this.setId(friendlyByteBuf.readInt());
        velocity = new Vector2d(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
        ai = friendlyByteBuf.readFloat();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<UniverseSplitterSmallBeam> {
        public Render(EntityRendererProvider.Context pContext) {
            super(pContext);
        }

        private static void draw(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f normal, float length, UniverseSplitterSmallBeam beam) {
            consumer.vertex(matrix4f, beam.scale, 0f, 0)
                .color(230, 230, 230, 230)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 0, 1)
                .endVertex();

            consumer.vertex(matrix4f, beam.scale, length, 0)
                .color(230, 230, 230, 230)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 0, 1)
                .endVertex();

            consumer.vertex(matrix4f, -beam.scale, length, 0)
                .color(230, 230, 230, 230)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 0, 1)
                .endVertex();

            consumer.vertex(matrix4f, -beam.scale, 0f, 0)
                .color(230, 230, 230, 230)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 0, 1)
                .endVertex();
        }

        @Override
        public void render(UniverseSplitterSmallBeam entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
            if (entity.rotationOld != 0) {
                pose.pushPose();
                pose.mulPose(Vector3f.ZP.rotation(RenderUtil.rotLerpRadians(partialTick, entity.rotationOld, entity.rotation)));
                Matrix4f matrix4f = pose.last().pose();
                Matrix3f normal = pose.last().normal();
                //render head
                draw(buffer.getBuffer(entity.head), matrix4f, normal, 2, entity);
                pose.translate(0, 2, 0);

                //render body
                draw(buffer.getBuffer(entity.body), matrix4f, normal, 36, entity);
                pose.translate(0, 36, 0);

                //render tail
                draw(buffer.getBuffer(entity.tail), matrix4f, normal, 2, entity);
                pose.popPose();
            }
        }

        @Override
        public boolean shouldRender(UniverseSplitterSmallBeam pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
            return true;
        }

        @Override
        public ResourceLocation getTextureLocation(UniverseSplitterSmallBeam universeSplitterSmallBeam) {
            return CalamityCurios.ModResource("textures/entity/universe_splitter_small_beam_mid.png");
        }
    }
}
