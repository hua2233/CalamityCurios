package hua223.calamity.register.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.render.item.EmptinessSprayerLoop;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.render.shaders.VolumetricSprayShader;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@AutoEntityRegister(sized = {.7F, .7F}, trackingRange = 32, velocityUpdates = false)
public class EmptinessSpray extends Entity {
    private int lSteps;
    @OnlyIn(Dist.CLIENT)
    private double lx;
    @OnlyIn(Dist.CLIENT)
    private double ly;
    @OnlyIn(Dist.CLIENT)
    private double lz;
    private final float size = 1f;
    private Player owner;
    public EmptinessSpray(EntityType<?> entityType, Level level) {
        super(entityType, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {

    }

    public static void create(Player player) {
        EmptinessSpray spray = CalamityCurios.getEntityType(EmptinessSpray.class).create(player.level());
        if (spray != null) {
            spray.setPos(player.getEyePosition().add(player.getLookAngle().normalize().scale(.5f)));
            spray.owner = player;
            player.level().addFreshEntity(spray);
        }
    }

    @Override
    public void tick() {
        if (owner == null || !owner.isAlive() || !owner.isUsingItem()
            || !owner.getUseItem().is(CalamityItems.EMPTINESS_SPRAYER.get())) discard();
        else if (level().isClientSide) {
            if (this.lSteps > 0) {
                double x = (this.lx - this.getX()) / (double) this.lSteps;
                double d5 = this.getX() + x;
                double d6 = this.getY() + (this.ly - this.getY()) / (double) this.lSteps;
                double z = ((this.lz - this.getZ()) / (double) this.lSteps);
                double d7 = this.getZ() + z;
                --this.lSteps;
                this.setPos(d5, d6, d7);
            }
        } else {
            setPos(owner.getEyePosition().add(owner.getLookAngle().normalize().scale(.5f)));
            if (tickCount % 5 == 0) sprayHits();
        }
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
    public boolean fireImmune() {
        return true;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, owner == null ? -1 : owner.getId());
    }

    private void sprayHits() {
        Vec3 dir = owner.getLookAngle().normalize();
        Vec3 nozzle = owner.getEyePosition().add(dir.scale(0.5f));
        float len = size * 8f;
        // 粗略获取实体：线段包围盒，向外膨胀最大锥半径
        AABB search = new AABB(nozzle, nozzle.add(dir.scale(len))).inflate(size * 2);
        for (Entity e : CalamityHelp.getAttackableEntity(Entity.class, owner, search)) {
            // 再次确认：把实体中心投影到喷射轴上
            Vec3 rel = e.position().add(0, e.getBbHeight() * 0.5, 0).subtract(nozzle);
            double axial = rel.dot(dir);
            if (axial < 0 || axial > len) continue; //是否在射程内(喷口后方 / 超出射程)
            double radial = rel.subtract(dir.scale(axial)).length();
            float t = (float) (axial / len);
            double coneR = size * Mth.lerp(Math.pow(t, 1.15), 0.04, 0.82) + e.getBbWidth() * 0.5;
            //是否在半径之内
            if (radial <= coneR) e.discard();
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level.getEntity(packet.getData()) instanceof AbstractClientPlayer player) {
            owner = player;
            minecraft.getSoundManager().play(new EmptinessSprayerLoop(this));
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
    }

    @OnlyIn(Dist.CLIENT)
    public static class Renderer extends EntityRenderer<EmptinessSpray> {
        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(@NotNull EmptinessSpray entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            float h = entity.size * 2;   // 截面半边长（锥体远端能达到的最大半径）
            float len = entity.size * 8f; // 射流长度：独立倍数，与横截面无关
            Vec3 jet = entity.owner.getViewVector(partialTick).normalize();
            Vec3 helper = Math.abs(jet.y) < 0.99 ? CalamityHelp.UNIT_Y : CalamityHelp.UNIT_X;
            Vec3 t1 = jet.cross(helper).normalize();
            Vec3 t2 = jet.cross(t1);

            VolumetricSprayShader.setHalfSize(h);
            VolumetricSprayShader.setJetDir((float) jet.x, (float) jet.y, (float) jet.z);
            VolumetricSprayShader.setJetLength(len);

            VertexConsumer vc = buffer.getBuffer(CalamityShaders.getVolumetricSpray());
            Matrix4f m = poseStack.last().pose();
            VolumetricSprayShader.setNozzlePos(m.getTranslation(new Vector3f()));

            // 远端帽（z = len）
            sprayVertex(vc, m, t1, t2, jet, -h, -h, len);
            sprayVertex(vc, m, t1, t2, jet, -h, h, len);
            sprayVertex(vc, m, t1, t2, jet, h, h, len);
            sprayVertex(vc, m, t1, t2, jet, h, -h, len);
            // 喷口帽（z = 0）
            sprayVertex(vc, m, t1, t2, jet, -h, -h, 0);
            sprayVertex(vc, m, t1, t2, jet, h, -h, 0);
            sprayVertex(vc, m, t1, t2, jet, h, h, 0);
            sprayVertex(vc, m, t1, t2, jet, -h, h, 0);
            // +X 侧面
            sprayVertex(vc, m, t1, t2, jet, h, -h, 0);
            sprayVertex(vc, m, t1, t2, jet, h, -h, len);
            sprayVertex(vc, m, t1, t2, jet, h, h, len);
            sprayVertex(vc, m, t1, t2, jet, h, h, 0);
            // -X 侧面
            sprayVertex(vc, m, t1, t2, jet, -h, -h, 0);
            sprayVertex(vc, m, t1, t2, jet, -h, h, 0);
            sprayVertex(vc, m, t1, t2, jet, -h, h, len);
            sprayVertex(vc, m, t1, t2, jet, -h, -h, len);
            // +Y 侧面
            sprayVertex(vc, m, t1, t2, jet, -h, h, 0);
            sprayVertex(vc, m, t1, t2, jet, h, h, 0);
            sprayVertex(vc, m, t1, t2, jet, h, h, len);
            sprayVertex(vc, m, t1, t2, jet, -h, h, len);
            // -Y 侧面
            sprayVertex(vc, m, t1, t2, jet, -h, -h, 0);
            sprayVertex(vc, m, t1, t2, jet, -h, -h, len);
            sprayVertex(vc, m, t1, t2, jet, h, -h, len);
            sprayVertex(vc, m, t1, t2, jet, h, -h, 0);
        }

        private static void sprayVertex(VertexConsumer vc, Matrix4f m, Vec3 t1, Vec3 t2, Vec3 jet, float x, float y, float z) {
            vc.vertex(m, (float) (t1.x * x + t2.x * y + jet.x * z), (float) (t1.y * x + t2.y * y + jet.y * z), (float) (t1.z * x + t2.z * y + jet.z * z)).endVertex();
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(@NotNull EmptinessSpray emptinessSpray) {
            return CalamityCurios.ModResource("textures/entity/emptiness_spray.png");
        }
    }
}
