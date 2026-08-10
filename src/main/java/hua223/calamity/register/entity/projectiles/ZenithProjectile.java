package hua223.calamity.register.entity.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.register.entity.AutoEntityRegister;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;

import static hua223.calamity.generators.DamageMapping.*;
import static hua223.calamity.register.items.CalamityItems.ZENITH;
import static hua223.calamity.register.sounds.CalamitySounds.ZENITH_ATTACK;

@AutoEntityRegister(sized = {0.5f, 0.5f}, trackingRange = 32)
public final class ZenithProjectile extends Projectile implements IEntityAdditionalSpawnData {
    @DamageRequester(key = "zenith", tags = {IS_PROJ, BYPASSES_COOLDOWN,
        BYPASSES_SHIELD}, style = ChatFormatting.GOLD, zh_cn = "%s死于天顶剑刃")
    public static DamageSupplier supplier;

    private final float[] SCALE;
    private static final byte LIFE = 20;

    @OnlyIn(Dist.CLIENT)
    private final RenderType type = RenderType.entityTranslucent(CalamityCurios.ModResource("textures/entity/zenith_projectile.png"));
    @OnlyIn(Dist.CLIENT)
    private final ItemStack defaultInstance = ZENITH.get().getDefaultInstance();
    private @NotNull Map<Enchantment, Integer> enchantments;
    private @NotNull Vec3 ODirection;
    private @NotNull Vec3 OPosition;
    private float acceleration;
    private float curvature;
    private float radius;
    private float yVelocity;
    private float yRotB;

    public ZenithProjectile(EntityType<? extends ZenithProjectile> entityType, Level level) {
        super(entityType, level);
        enchantments = Map.of();
        ODirection = Vec3.ZERO;
        OPosition = Vec3.ZERO;
        SCALE = new float[20];
        for (byte b = 0; b < 20; ++b) SCALE[b] = (float) Math.sin(Math.toRadians(18 * b));

        if (!level.isClientSide) {
            acceleration = 1.0F;
            curvature = 0.5f + random.nextFloat() * 2.2f;
            radius = (float) ((LIFE * curvature) / Math.PI / 2.0);
        }

        noPhysics = true;
    }

    @SuppressWarnings("ConstantConditions")
    private static @NotNull ZenithProjectile newZenithProjectile(Level level, @NotNull Player player) {
        var projectile = CalamityCurios.getEntityType(ZenithProjectile.class).create(level);
        projectile.setEnchantments(EnchantmentHelper.getEnchantments(player.getMainHandItem()));
        projectile.setOwner(player);

        projectile.setPos(player.position().add(0.0, 0.8, 0.0)
            .add(projectile.calculateViewVector(0.0F, player.getYRot()).scale(-1.0)));

        projectile.OPosition = projectile.position();
        projectile.setYRot((player.getYRot() + 90.0f) % 360.0f);
        projectile.yRotB = projectile.getYRot();
        return projectile;
    }

    public static @NotNull ZenithProjectile of(Level level, @NotNull Player player) {
        ZenithProjectile zenithProjectile = newZenithProjectile(level, player);

        zenithProjectile.setODirection(zenithProjectile.calculateViewVector(0.0F, player.getYRot()));

        zenithProjectile.acceleration = 20.0f * Mth.cos((float) Math.toRadians(player.getXRot())) / zenithProjectile.radius;

        zenithProjectile.yVelocity = (float) (-(Mth.sin((float) Math.toRadians(player.getXRot())) * 4.0));

        zenithProjectile.setXRot(-player.getXRot());
        return zenithProjectile;
    }

    public static @NotNull ZenithProjectile of(Level level, @NotNull Player player, Vec3 vec3) {
        ZenithProjectile zenithProjectile = newZenithProjectile(level, player);

        vec3 = player.position().vectorTo(vec3);

        double y = vec3.y - 0.8;
        vec3 = new Vec3(vec3.x, 0.0, vec3.z);

        zenithProjectile.setODirection(vec3);
        double length = vec3.length();

        zenithProjectile.setXRot((float) (Mth.atan2(y, length) * (180F / Math.PI)));
        if (length > (2.0 * zenithProjectile.radius)) {
            zenithProjectile.acceleration = (float) (length / zenithProjectile.radius / 2.0);
        }

        zenithProjectile.yVelocity = (float) (y / 10.0);
        return zenithProjectile;
    }

    private void setCurvature(float f) {
        curvature = f;
        radius = (float) ((LIFE * curvature) / Math.PI / 2.0);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (!enchantments.isEmpty()) {
            ListTag listTag = new ListTag();
            enchantments.forEach((enchantment, integer) -> {
                CompoundTag tag = new CompoundTag();
                tag.putString("id", String.valueOf(EnchantmentHelper.getEnchantmentId(enchantment)));
                tag.putInt("lvl", integer);
                listTag.add(tag);
            });
            compoundTag.put("enchantments", listTag);
        }

        compoundTag.putDouble("OX", ODirection.x);
        compoundTag.putDouble("OY", ODirection.y);
        compoundTag.putDouble("OZ", ODirection.z);
        compoundTag.putInt("tickCount", tickCount);
        compoundTag.putDouble("OPX", OPosition.x);
        compoundTag.putDouble("OPY", OPosition.y);
        compoundTag.putDouble("OPZ", OPosition.z);
        compoundTag.putFloat("curvature", curvature);
        compoundTag.putFloat("acceleration", acceleration);
        compoundTag.putFloat("yVelocity", yVelocity);
        compoundTag.putFloat("yRotB", yRotB);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (!compoundTag.getList("enchantments", 10).isEmpty()) {
            enchantments = EnchantmentHelper.deserializeEnchantments(compoundTag.getList("enchantments", 10));
        }
        ODirection = new Vec3(compoundTag.getDouble("OX"), compoundTag.getDouble("OY"), compoundTag.getDouble("OZ"));
        OPosition = new Vec3(compoundTag.getDouble("OPX"), compoundTag.getDouble("OPY"), compoundTag.getDouble("OPZ"));
        tickCount = compoundTag.getByte("tickCount");
        setCurvature(compoundTag.getFloat("curvature"));
        acceleration = compoundTag.getFloat("acceleration");
        yVelocity = compoundTag.getFloat("yVelocity");
        yRotB = compoundTag.getFloat("yRotB");
    }

    public void setODirection(@NotNull Vec3 vec3) {
        double length = vec3.length();
        ODirection = Math.abs(1.0 - length) > 0.01 ? vec3.scale(1.0 / length) : vec3;
    }

    public void setEnchantments(@NotNull Map<Enchantment, Integer> map) {
        enchantments = map;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount == 10) yVelocity *= -1.0F;

        Entity owner = getOwner();
        if (owner instanceof ServerPlayer player && player.getMainHandItem().is(CalamityItems.ZENITH.get())) {
            BlockPos pos = blockPosition();
            AABB aabb = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).inflate(2.5);
            float f = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            ItemStack handItem = player.getMainHandItem();
            List<LivingEntity> list = player.serverLevel().getEntitiesOfClass(LivingEntity.class, aabb);

            if (!list.isEmpty()) {
                DamageSource source = supplier.get(this, player);
                for (LivingEntity potentialTarget : list) {
                    if (potentialTarget.isAttackable() && potentialTarget != player && potentialTarget.isAlive()) {
                        float f1 = f;
                        int value;
                        for(Enchantment enchantment : enchantments.keySet()) {
                            value = enchantments.get(enchantment);
                            f1 += enchantment.getDamageBonus(value, potentialTarget.getMobType(), handItem);
                            enchantment.doPostHurt(player, potentialTarget, value);
                            if (enchantment == Enchantments.FIRE_ASPECT && potentialTarget.isOnFire())
                                potentialTarget.setSecondsOnFire(4 * enchantments.get(enchantment));
                        }

                        potentialTarget.hurt(source, f1);
                    }
                }
            }
        }


        if (tickCount < LIFE && tickCount > -1) {
            float f1 = (float) (-getYRot() * (Math.PI / 180));
            Vector3d v3d = new Vector3d(Mth.sin(f1), 0, Mth.cos(f1)).mul(curvature);
            f1 = (float) (SCALE[tickCount] * (acceleration - 1.0) * curvature);
            v3d.add(ODirection.x * f1, 0, ODirection.z * f1).y += yVelocity;

            if (tickCount > LIFE / 2 && owner != null) {
                var v3 = OPosition.vectorTo(owner.position().add(0.0, 0.8, 0.0)
                    .add(calculateViewVector(0.0F, owner.getYRot()).scale(-1.0))).toVector3f();

                if (v3.lengthSquared() > 6400) {
                    discard();
                    return;
                }

                v3 = v3.mul(1.0f / (LIFE - tickCount));
                OPosition = OPosition.add(v3.x, v3.y, v3.z);
                v3d.add(v3);
            }

            Vec3 vec3 = new Vec3(v3d.x, v3d.y, v3d.z);
            setDeltaMovement(vec3);
            setPos(position().add(vec3));
            setYRot(getYRot() - 360.0f / LIFE);
        } else if (tickCount > LIFE + 1) discard();
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void move(@NotNull MoverType moverType, @NotNull Vec3 vec3) {
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeUUID(getUUID());
        buffer.writeInt(getId());
        buffer.writeInt(getOwner() == null ? 0 : getOwner().getId());
        buffer.writeDouble(ODirection.x);
        buffer.writeDouble(ODirection.y);
        buffer.writeDouble(ODirection.z);
        buffer.writeFloat(curvature);
        buffer.writeFloat(acceleration);
        buffer.writeFloat(getYRot());
        buffer.writeFloat(getXRot());
        buffer.writeFloat(yRotB);
        buffer.writeDouble(OPosition.x);
        buffer.writeDouble(OPosition.y);
        buffer.writeDouble(OPosition.z);
        buffer.writeFloat(yVelocity);
        buffer.writeByte(tickCount);
    }

    @OnlyIn(Dist.CLIENT)
    public void readSpawnData(@NotNull FriendlyByteBuf additionalData) {
        setUUID(additionalData.readUUID());
        setId(additionalData.readInt());
        setOwner(level().getEntity(additionalData.readInt()));
        if (getOwner() instanceof Player player)
            ZENITH_ATTACK.playSound(player);

        ODirection = new Vec3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        setCurvature(additionalData.readFloat());
        acceleration = additionalData.readFloat();
        setYRot(additionalData.readFloat());
        setXRot(additionalData.readFloat());
        yRotB = additionalData.readFloat();
        OPosition = new Vec3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        yVelocity = additionalData.readFloat();
        tickCount = additionalData.readByte();
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Renderer extends EntityRenderer<ZenithProjectile> {
        public static final Quaternionf AXIS;
        public static BakedModel model;
        public static final double SIN45 = Math.sin(Math.toRadians(45.0));

        static {
            double a = Math.toRadians(90.0) * 0.5;
            double sin = Math.sin(a);
            double cos = Math.cos(a);
            AXIS = new Quaternionf(-sin * SIN45, sin * SIN45, 0.0, cos);
        }

        public Renderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        @SuppressWarnings("ALL")
        public void render(@NotNull ZenithProjectile zenithProjectile, float yaw, float tickDelta,
                           @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int light) {
            int currentAngle = (18 * zenithProjectile.tickCount);
            if (currentAngle > 360) currentAngle = 0;
            renderEntity(multiBufferSource.getBuffer(zenithProjectile.type), zenithProjectile, poseStack, currentAngle);
            poseStack.pushPose();
            poseStack.translate(0.0, 0.2, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-zenithProjectile.yRotB));
            poseStack.mulPose(Axis.ZP.rotationDegrees(45 + zenithProjectile.getXRot()));
            poseStack.mulPose(AXIS);
            if (currentAngle > 0) poseStack.mulPose(Axis.ZP.rotationDegrees(currentAngle));
            poseStack.scale(2.0F, 2.0F, 2.0F);
            RenderUtil.renderItemModelList(Minecraft.getInstance().getItemRenderer(), ForgeHooksClient.handleCameraTransforms(
                poseStack, model, ItemDisplayContext.FIXED, false), zenithProjectile.defaultInstance,
                poseStack, multiBufferSource, light, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        private void renderEntity(VertexConsumer vertexconsumer, @NotNull ZenithProjectile zenithProjectile, @NotNull PoseStack poseStack, int currentAngle) {
            int s = Math.min(45, currentAngle);
            if (s > 0) {
                poseStack.pushPose();
                PoseStack.Pose posestack$pose = poseStack.last();
                Matrix4f matrix4f = posestack$pose.pose();
                Matrix3f matrix3f = posestack$pose.normal();
                float radians = (float) Math.toRadians(-zenithProjectile.getYRot() - 90.0F);
                poseStack.mulPose(Axis.YP.rotation(radians));
                poseStack.translate(0.0, 0.2, 0.8);
                double r = zenithProjectile.curvature + (Math.PI / 10D);
                double dr = 1.0F;
                float y_s = 0.0F;
                float y_e = 0.4F;
                float x_s = 0.0F;
                float u_s = 0.0F;
                float v_s = 1.0F;
                float v_e = 0.0F;
                double a = Math.toRadians(zenithProjectile.getYRot());
                Vector2f v2d = Vector2f.vector2dMultiply((float) Math.sin(a), (float) Math.cos(a),
                    (float) zenithProjectile.ODirection.x, (float) zenithProjectile.ODirection.z);

                double du = 1.0 / Math.sin(Math.toRadians(s));
                float radians2;
                double cos;
                double sin;
                float u_e;
                Vector2f add = new Vector2f(0, 0);
                for(double i1 = 0.0; i1 < s; i1 += dr) {
                    radians2 = (float) Math.toRadians(i1);
                    cos = Mth.cos(radians2);
                    sin = Mth.sin(radians2);
                    u_e = (float)(sin * du);
                    add.set(v2d);
                    add = add.mul(Mth.sin((float) Math.toRadians(currentAngle - i1)) *
                        (zenithProjectile.acceleration - 1.0f)).add((float) -cos, (float) -sin).mul((float) (dr * r / (byte) 18));

                    float x_e = (float)((double)x_s + add.x);
                    vertex(matrix4f, matrix3f, vertexconsumer, x_e, y_s, u_e, v_s);
                    vertex(matrix4f, matrix3f, vertexconsumer, x_s, y_s, u_s, v_s);
                    vertex(matrix4f, matrix3f, vertexconsumer, x_s, y_e, u_s, v_e);
                    vertex(matrix4f, matrix3f, vertexconsumer, x_e, y_e, u_e, v_e);
                    poseStack.translate(0.0, 0.0, add.y);
                    u_s = u_e;
                    x_s = x_e;
                }

                poseStack.popPose();
            }
        }

        private void vertex(Matrix4f matrix4f, Matrix3f matrix3f, @NotNull VertexConsumer vertexConsumer, float x, float z, float u, float v) {
            vertexConsumer.vertex(matrix4f, x, 0f, z).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 1.0F, 1.0F, -1.0F).endVertex();
        }

        public @NotNull ResourceLocation getTextureLocation(@NotNull ZenithProjectile zenithProjectile) {
            return null;
        }
    }
}
