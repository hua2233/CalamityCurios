package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.damage.CalamityDamageSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class LunarFlare extends Entity {
    private ServerPlayer owner;
    private DamageSource source;

    @OnlyIn(Dist.CLIENT)
    private float v;
    @OnlyIn(Dist.CLIENT)
    private float v1 = 1 / 7f;
    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityCutout(CalamityCurios.ModResource("textures/entity/lunar_flare.png"));

    LunarFlare(EntityType<?> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public static void create(ServerPlayer player, Vec3 position) {
        ServerLevel level = player.serverLevel();
        LunarFlare flare = CalamityEntity.LUNAR_FLARE.get().create(level);
        if (flare != null) {
            flare.owner = player;
            flare.source = CalamityDamageSource.source(DamageTypes.MAGIC, player);
            flare.setPos(position);
            level.addFreshEntity(flare);
        }
    }

    @Override
    public void tick() {
        if (tickCount == 9) {
            if (level().isClientSide)
                level().playSound(null, this, CalamitySounds.LUNAR_FLARE.get(), SoundSource.AMBIENT, 1f, 1f);
             else {
                final float damage = (float) owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
                level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(1.5))
                    .stream().filter(target -> target.isPickable() && !target.isAlliedTo(owner))
                    .forEach(entity -> {
                        if (entity == owner) owner.heal(Math.max(owner.getMaxHealth() * 0.3f, damage));
                        else entity.hurt(source, damage);
                    });
            }
        }

        if (level().isClientSide && (tickCount & 1) == 0 && v1 < 0.95f) {
            //Do not put it together with the client side judgment,
            //as this will make the conditions confusing
            float frame = 1 / 7f;
            v += frame;
            v1 += frame;
        } else if (tickCount > 15) discard();
    }

    @Override
    protected @NotNull MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {}

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<LunarFlare> {
        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(LunarFlare entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            VertexConsumer consumer = buffer.getBuffer(entity.type);
            poseStack.pushPose();
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            Matrix4f matrix = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            consumer.vertex(matrix, -0.5f, -0.5f, 0)
                    .color(1f, 1f, 1f, 1f)
                    .uv(1, entity.v1)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(normal, 0, 1, 0)
                    .endVertex();

            consumer.vertex(matrix, -0.5f, 0.5f, 0)
                .color(1f, 1f, 1f, 1f)
                .uv(1, entity.v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix, 0.5f, 0.5f, 0)
                .color(1f, 1f, 1f, 1f)
                .uv(0, entity.v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix, 0.5f, -0.5f, 0)
                .color(1f, 1f, 1f, 1f)
                .uv(0, entity.v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0, 1, 0)
                .endVertex();

            poseStack.popPose();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull LunarFlare lunarFlare) {
            return CalamityCurios.ModResource("textures/entity/lunar_flare.png");
        }
    }
}
