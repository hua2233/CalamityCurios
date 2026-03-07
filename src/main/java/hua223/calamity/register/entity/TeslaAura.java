package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.effects.SurvivableEffectInstance;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class TeslaAura extends Entity {
    private LivingEntity owner;
    private boolean isBlunderBooster;
    @OnlyIn(Dist.CLIENT)
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    @OnlyIn(Dist.CLIENT)
    private int u;
    @OnlyIn(Dist.CLIENT)
    private final float uStep = 1 / 3f;
    @OnlyIn(Dist.CLIENT)
    private int v;
    @OnlyIn(Dist.CLIENT)
    private final float vStep = 1 / 6f;
    @OnlyIn(Dist.CLIENT)
    private RenderType type;

    public TeslaAura(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public static TeslaAura create(LivingEntity player, boolean isBlunderBooster) {
        Level level = player.getLevel();
        TeslaAura aura = CalamityEntity.TESLA_AURA.get().create(level);
        aura.owner = player;
        aura.setPos(player.position().add(0, 0.1, 0));
        aura.isBlunderBooster = isBlunderBooster;
        level.addFreshEntity(aura);
        return aura;
    }

    @Override
    public void tick() {
        if (level.isClientSide) {
            if (this.lSteps > 0) {
                double d5 = this.getX() + (this.lx - this.getX()) / (double) this.lSteps;
                double d6 = this.getY() + (this.ly - this.getY()) / (double) this.lSteps;
                double d7 = this.getZ() + (this.lz - this.getZ()) / (double) this.lSteps;
                --this.lSteps;
                this.setPos(d5, d6, d7);
            }

            if (tickCount % 2 == 0 && ++v == 6) {
                v = 0;
                if (++u == 3) u = 0;
            }
        } else {
            if (owner.isDeadOrDying() || owner.isSpectator() ||
                (!isBlunderBooster && !owner.hasEffect(CalamityEffects.TESLA.get()))) {
                kill();
                return;
            }

            setPos(owner.position().add(0, 0.1, 0));

            if (tickCount % 20 == 0) {
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox());
                if (entities.isEmpty()) return;
                MobEffect effect = CalamityEffects.GALVANIC_CORROSION.get();
                for (LivingEntity entity : entities) {
                    if (entity == owner || entity.isAlliedTo(owner) || !(entity instanceof Enemy)
                        || entity.getEffect(effect) instanceof SurvivableEffectInstance) continue;

                    entity.addEffect(new SurvivableEffectInstance(effect, 9999, 0,
                        () -> isAlive() && entity.distanceToSqr(owner.position()) < 25)
                        .addSubEffects(CalamityEffects.ELECTRIFIED.get()));
                }
            }
        }
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
    @OnlyIn(Dist.CLIENT)
    public void lerpTo(double x, double y, double z, float yr, float xr, int steps, boolean b) {
        this.lx = x;
        this.ly = y;
        this.lz = z;
        this.lSteps = steps;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        type = RenderType.entityTranslucent(packet.getData() == 0 ? CalamityCurios.ModResource("textures/entity/blunder_booster.png") :
            CalamityCurios.ModResource("textures/entity/tesla_aura.png"));
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, isBlunderBooster ? 0 : -1);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Render extends EntityRenderer<TeslaAura> {
        public Render(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(TeslaAura entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            poseStack.pushPose();
            VertexConsumer consumer = buffer.getBuffer(entity.type);
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();

            float u = entity.u * entity.uStep;
            float u1 = u + entity.uStep;
            float v = entity.v * entity.vStep;
            float v1 = v + entity.vStep;
            consumer.vertex(matrix4f, -5, 0, -5)
                .color(255, 255, 255, 255)
                .uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, -5, 0, 5)
                .color(255, 255, 255, 255)
                .uv(u1, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, 5, 0, 5)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();

            consumer.vertex(matrix4f, 5, 0, -5)
                .color(255, 255, 255, 255)
                .uv(u, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0, 1, 0)
                .endVertex();
            poseStack.popPose();
        }

        @Override
        public ResourceLocation getTextureLocation(TeslaAura teslaAura) {
            return CalamityCurios.ModResource(teslaAura.isBlunderBooster ?
                "textures/entity/blunder_booster.png" : "textures/entity/tesla_aura.png");
        }
    }
}
