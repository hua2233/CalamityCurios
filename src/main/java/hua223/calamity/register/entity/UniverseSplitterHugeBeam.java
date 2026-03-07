package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UniverseSplitterHugeBeam extends Entity {
    public static final byte TIME_LEFT = 60;
    private static final byte TOTAL_FADEIN_TIME = 50;
    private static final byte MAX_LENGTH = 60;
    private static final float LASER_SIZE = 4;
    Player owner;
    private float length;
    private float lengthOld;
    private int strikePoint;
    private AABB box;
    private AABB scopeBox;
    @OnlyIn(Dist.CLIENT)
    private float scale;
    @OnlyIn(Dist.CLIENT)
    private float scaleOld;
    @OnlyIn(Dist.CLIENT)
    private float animation;
    @OnlyIn(Dist.CLIENT)
    private RenderType body = RenderType.entityCutoutNoCull(
        CalamityCurios.ModResource("textures/entity/universe_splitter_huge_beam_mid.png"));
    @OnlyIn(Dist.CLIENT)
    private RenderType end = RenderType.entityCutoutNoCull(
        CalamityCurios.ModResource("textures/entity/universe_splitter_huge_beam_end.png"));

    public UniverseSplitterHugeBeam(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        if (tickCount > TIME_LEFT) {
            discard();
            return;
        }

        if (tickCount < 10) {
            lengthOld = length;
            length = Mth.lerp(tickCount / 9f, 0.01f, strikePoint);

            box = new AABB(getX() - 2, getY(), getZ() + 0.5, getX() + 2, getY() - length, getZ() - 0.5);
            if (tickCount == 9) lengthOld = length;
        }


        if (level.isClientSide) {
            if (animation >= 0.8f) animation = 0f;
            else animation += 0.2f;

            if (tickCount > TOTAL_FADEIN_TIME) {
                scaleOld = scale;
                scale = Mth.lerp(((TIME_LEFT - tickCount) / 10f), 0.01f, LASER_SIZE);
            }//If you're seeing this entity
            else if (tickCount == 42 && Minecraft.getInstance().levelRenderer.cullingFrustum.isVisible(box)) {
                RenderUtil.Shaders.setScreenFlashEffect(18, 0.6f);
            }
        } else if (tickCount % 5 == 0) {
            //damage creatures in diameter
            MobEffect effect = CalamityEffects.ELECTRIFIED.get();
            UniverseSplitterSmallBeam.onHit(level, this, owner, box, 300f, effect);

            if (tickCount >= 10) {
                if (scopeBox == null) {
                    double endPos = getY() - length;
                    scopeBox = new AABB(getX() + 3, endPos + 1, getZ() + 3, getX() - 3, endPos, getZ() - 3);

                    //on first hitting ground, spawn some colliding dust
                    ServerLevel serverLevel = ((ServerLevel) level);
                    RandomSource random = serverLevel.random;
                    int yPos = (int) (endPos - 1);
                    double x = getX();
                    double z = getZ();

                    for (int i = 0; i < 20; i++) {
                        float xOffset = random.nextInt(0, 6) + random.nextFloat() - 3;
                        float zOffset = random.nextInt(0, 6) + random.nextFloat() - 3;
                        BlockPos pos = new BlockPos(x + xOffset, yPos, z + zOffset);
                        BlockState state = serverLevel.getBlockState(pos);
                        if (!state.isAir()) {
                            serverLevel.sendParticles((new BlockParticleOption(ParticleTypes.BLOCK, state)).setPos(pos),
                                pos.getX(), pos.getY(), pos.getZ(), 6, 0f, 1, 0f, 0.15f);
                        }
                    }
                }

                //deal with some aftermath damage
                UniverseSplitterSmallBeam.onHit(level, this, owner, scopeBox, 30f, effect);
            }
        }
    }

    public void setStrikePoint() {
        BlockPos pos = getOnPos();
        for (int y = 0; y < MAX_LENGTH; y++) {
            BlockPos checkPosition = pos.offset(0, -y, 0);
            if (cannotPenetrate(checkPosition, level)) return;

            for (int x = 1; x < 3; x++)
                if (cannotPenetrate(checkPosition.offset(-x, 0, 0), level)
                    || cannotPenetrate(checkPosition.offset(x, 0, 0), level)) return;

            strikePoint++;
        }
    }

    private static boolean cannotPenetrate(BlockPos pos, Level level) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty()
            && !state.getCollisionShape(level, pos).isEmpty();
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
        return new ClientboundAddEntityPacket(this, strikePoint);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        strikePoint = packet.getData();
        scale = LASER_SIZE;
    }

    @OnlyIn(Dist.CLIENT)
    public float getWidth(float partialTick) {
        if (tickCount > TOTAL_FADEIN_TIME) return Mth.lerp(partialTick, scaleOld, scale);
        else return LASER_SIZE;
    }

    @OnlyIn(Dist.CLIENT)
    public float getHeight(float partialTick) {
        if (tickCount < 10) return Mth.lerp(partialTick, lengthOld, length);
        else return strikePoint;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<UniverseSplitterHugeBeam> {


        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        private static void draw(Matrix4f matrix4f, Matrix3f normal, VertexConsumer consumer, float w, float h, float v) {
            consumer.vertex(matrix4f, w, 0, 0)
                .color(255, 255, 255, 255)
                .uv(0, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, w, -h, 0)
                .color(255, 255, 255, 255)
                .uv(0, v + 0.2f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, -w, -h, 0)
                .color(255, 255, 255, 255)
                .uv(1, v + 0.2f)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, -w, 0, 0)
                .color(255, 255, 255, 255)
                .uv(1, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(normal, 0, 1, 0)
                .endVertex();
        }

        @Override
        public void render(UniverseSplitterHugeBeam entity, float entityYaw, float partialTick, PoseStack pose, MultiBufferSource buffer, int packedLight) {
            if (entity.lengthOld != 0) {
                pose.pushPose();
                VertexConsumer body = buffer.getBuffer(entity.body);
                Matrix4f matrix4f = pose.last().pose();
                Matrix3f normal = pose.last().normal();
                float w = entity.getWidth(partialTick);
                float h = entity.getHeight(partialTick);
                float section = h / 10;
                draw(matrix4f, normal, body, w, section, entity.animation);

                for (int i = 1; i < 9; i++) {
                    pose.translate(0, -section, 0);
                    draw(matrix4f, normal, body, w, section, entity.animation + i % 5 * 0.2f);
                }

                VertexConsumer tail = buffer.getBuffer(entity.end);
                pose.translate(0, -section, 0);
                draw(matrix4f, normal, tail, w, section, entity.animation);
                pose.popPose();
            }
        }

        @Override
        public ResourceLocation getTextureLocation(UniverseSplitterHugeBeam universeSplitterHugeBeam) {
            return CalamityCurios.ModResource("textures/entity/universe_splitter_huge_beam_mid.png");
        }

        @Override
        public boolean shouldRender(UniverseSplitterHugeBeam livingEntity, Frustum camera, double pCamX, double pCamY, double pCamZ) {
            return livingEntity.box != null && camera.isVisible(livingEntity.box);
        }
    }
}
