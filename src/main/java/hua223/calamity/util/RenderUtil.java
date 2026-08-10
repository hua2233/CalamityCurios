package hua223.calamity.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.*;
import hua223.calamity.render.screen.ErosionScreenRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.awt.*;
import java.lang.Math;
import java.util.function.Supplier;

//To be refactored...
@OnlyIn(Dist.CLIENT)
public final class RenderUtil {
    public static final Matrix4f TRANSIENT_MATRIX = new Matrix4f();

    public static final HumanoidModel.ArmPose HOLD_POSE = HumanoidModel.ArmPose.create(
        "HOLD", true, (model, entity, arm) -> {
            float xRot = (entity.getXRot() + 8) * Mth.DEG_TO_RAD;
            float armXRot = xRot - 1.5708f;
            model.rightArm.xRot = armXRot;
            model.leftArm.xRot = armXRot;

            float offsetsYRot = xRot * 0.4f;

            model.rightArm.yRot = -0.6981f + offsetsYRot;
            model.leftArm.yRot = 0.6981f + offsetsYRot;
        });


    private static short tick;

    private RenderUtil() {
    }

    public static void updateGlobal() {
        if (tick++ == 3600) tick = 0;
    }

    private static int rainbowStyle = 0x00FF0000;
    private static short lastRainbowTime;

    private static void rainbowTick() {
        if (lastRainbowTime != tick) {
            lastRainbowTime = tick;
            int currentStyle = FastColor.ARGB32.alpha(rainbowStyle);
            //Rotational chromatography
            switch (currentStyle) {
                case 0 -> {
                    int  rainbowG = Math.min(255, FastColor.ARGB32.green(rainbowStyle) + 7) << 8;
                    if (rainbowG >> 8 == 255) {
                        //默认的G分量，其他位信息为0x00
                        rainbowG |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x00FF00FF;
                    } else rainbowStyle &= 0xFFFF00FF;

                    rainbowStyle |= rainbowG;
                }

                case 1 -> {
                    int rainbowR = Math.max(0, FastColor.ARGB32.red(rainbowStyle) - 7) << 16;
                    if (rainbowR >> 16 == 0) {
                        rainbowR |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x0000FFFF;
                    } else rainbowStyle &= 0xFF00FFFF;

                    rainbowStyle |= rainbowR;
                }

                case 2 -> {
                    int rainbowB = Math.min(255, FastColor.ARGB32.blue(rainbowStyle) + 7);
                    if (rainbowB == 255) {
                        rainbowB |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x00FFFF00;
                    } else rainbowStyle &= 0xFFFFFF00;

                    rainbowStyle |= rainbowB;
                }

                case 3 -> {
                    int rainbowG = Math.max(0, FastColor.ARGB32.green(rainbowStyle) - 7) << 8;
                    if (rainbowG >> 8 == 0) {
                        rainbowG |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x00FF00FF;
                    } else rainbowStyle &= 0xFFFF00FF;

                    rainbowStyle |= rainbowG;
                }

                case 4 -> {
                    int rainbowR = Math.min(255, FastColor.ARGB32.red(rainbowStyle) + 7) << 16;
                    if (rainbowR >> 16 == 255) {
                        rainbowR |= ((currentStyle + 1) << 24);
                        rainbowStyle &= 0x0000FFFF;
                    } else rainbowStyle &= 0xFF00FFFF;

                    rainbowStyle |= rainbowR;
                }

                case 5 -> {
                    int rainbowB = Math.max(0, FastColor.ARGB32.blue(rainbowStyle) - 7);
                    if (rainbowB == 0) rainbowStyle &= 0x00FFFF00;
                    else rainbowStyle &= 0xFFFFFF00;
                    rainbowStyle |= rainbowB;
                }
            }
        }
    }

    public static Component getRainbow(MutableComponent component) {
        return component.setStyle(Style.EMPTY.withColor(getRainbowStyle()));
    }

    public static int getRainbowStyle() {
        rainbowTick();
        //It won't use the Alpha channel
        return rainbowStyle;
    }

    public static short getLocalTick() {
        return tick;
    }

    public static int processingCycleTime(short startTime) {
        short localTick = tick;
        if (localTick < startTime) localTick += 3600;
        return localTick - startTime;
    }

    /**
     * 渲染其常规三角纹理
     */
    public static void addVertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer,
                                 float x, float y, float z, float u, float v, int light) {
        consumer.vertex(pose, x, y, z).color(255, 255, 255, 255).uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }

    /**
     * 渲染正常的材质纹理，其纹理总是与你的材质相同，且法线向量总是指向Y轴。
     */
    public static void renderTexture(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, int light) {
        addVertex(pose, normal, consumer, 0.5f, 0.5f, 0.0f, 1f, 0f, light);
        addVertex(pose, normal, consumer, -0.5f, 0.5f, 0.0f, 0f, 0f, light);
        addVertex(pose, normal, consumer, -0.5f, -0.5f, 0.0f, 0f, 1f, light);
        addVertex(pose, normal, consumer, 0.5f, -0.5f, 0.0f, 1f, 1f, light);
    }

    public static void renderItemModelList(ItemRenderer renderer, BakedModel model, ItemStack stack,
                                           PoseStack pose, MultiBufferSource source, int combinedLight, int combinedOverlay) {
        boolean fabulous = Minecraft.useFancyGraphics();
        for (BakedModel bakedModel : model.getRenderPasses(stack, fabulous)) {
            for (RenderType type : bakedModel.getRenderTypes(stack, fabulous)) {
                    VertexConsumer consumer = ItemRenderer.getFoilBuffer(source, type, true, stack.hasFoil());
                renderer.renderModelLists(bakedModel, stack, combinedLight, combinedOverlay, pose, consumer);
            }
        }
    }

    public static void crossTextureRendering(Projectile projectile, VertexConsumer consumer, PoseStack poseStack, int packedLight) {
        poseStack.mulPose(Axis.YP.rotationDegrees(projectile.tickCount * 6));
        PoseStack.Pose last = poseStack.last();
        renderTexture(last.pose(), last.normal(), consumer, packedLight);

        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        renderTexture(last.pose(), last.normal(), consumer, packedLight);
    }

    public static Vector2f directionTo(Vec3 vec3, double x, double y) {
        double dx = vec3.x - x;
        double dy = vec3.y - y;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) {
            return new Vector2f(0, 0);
        }

        return new Vector2f(dx / length, dy / length);
    }

    public static Vector4i black() {
        return new Vector4i(0, 0, 0, 255);
    }

    public static Vector4i fromColorGet(Color color) {
        return new Vector4i(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    //handler r, g, b color component and reset a
    public static Vector4i interpolateColor(Vector4i start, Vector4i end, float delta, Vector4i container) {
        int red = (int) Mth.lerp(delta, start.x(), end.x());
        int green = (int) Mth.lerp(delta, start.y(), end.y());
        int blue = (int) Mth.lerp(delta, start.z(), end.z());

        if (container != null) {
            container.set(red, green, blue, 255);
            return container;
        }

        return new Vector4i(red, green, blue, 255);
    }

    public static void interpolateToTransparent(Vector4i start, float delta, Vector4i container) {
        container.x = (int) Mth.lerp(delta, start.x, 0);
        container.y = (int) Mth.lerp(delta, start.y, 0);
        container.z = (int) Mth.lerp(delta, start.z, 0);
        container.w = (int) Mth.lerp(delta, start.w, 0);
    }

    public static Vector4i multiplyColor(Vector4i color, float factor, Vector4i container) {
        container.set((int) Math.min(255, color.x() * factor), (int) Math.min(255, color.y() * factor),
            (int) Math.min(255, color.z() * factor), (int) Math.min(255, color.w() * factor));
        return container;
    }

    public static float clampLerp(float from, float to, float t) {
        if (from < to) {
            if (t < from) {
                return 0f;
            }
            if (t > to) {
                return 1f;
            }
        } else {
            if (t < to) {
                return 1f;
            }
            if (t > from) {
                return 0f;
            }
        }
        return (t - from) / (to - from);
    }

    public static void reuseQuaternions(Quaternionf quaternion, Vec3 vec3, float degrees) {
        radianQuaternions(quaternion, vec3, degrees * ((float) Math.PI / 180F));
    }

    public static void radianQuaternions(Quaternionf quaternion, Vec3 vec3, float radian) {
        float f = (float) Math.sin(radian / 2.0d);
        quaternion.set((float) (vec3.x * f), (float) (vec3.y * f), (float) (vec3.z * f), (float) Math.cos(radian / 2f));
    }

    public static float smoothStep(float from, float to, float amount) {
        return hermite(from, 0f, to, 0f, Mth.clamp(amount, 0f, 1f));
    }

    public static float inverseLerpBump(float start, float riseEnd, float plateauEnd, float end, float value) {
        if (value < start) return 0;
        if (value > end) return 0;

        if (value <= riseEnd)
            return Mth.inverseLerp(value, start, riseEnd);

        if (value <= plateauEnd)
            return 1;

        return Mth.inverseLerp(value, end, plateauEnd);
    }

    public static float hermite(float value1, float tangent1, float value2, float tangent2, float amount) {
        float cubed = amount * amount * amount;
        float squared = amount * amount;

        if (amount == 0f) return value1;
        else if (amount == 1f) return value2;
        else return (2f * value1 - 2f * value2 + tangent2 + tangent1) * cubed +
                (3f * value2 - 3f * value1 - 2f * tangent1 - tangent2) * squared +
                tangent1 * amount + value1;
    }

    public static Vector2f catmullRomVec(Vector2f value1, Vector2f value2, Vector2f value3, Vector2f value4, float amount) {
        float squared = amount * amount;
        float cubed = amount * squared;
        return new Vector2f(
            0.5f * ((2.0f * value2.x) + (-value1.x + value3.x) *
                amount + (2.0f * value1.x - 5.0f * value2.x + 4.0f * value3.x - value4.x)
                * squared + (-value1.x + 3.0f * value2.x - 3.0f * value3.x + value4.x) * cubed),

            0.5f * ((2.0f * value2.y) + (-value1.y + value3.y) *
                amount + (2.0f * value1.y - 5.0f * value2.y + 4.0f * value3.y - value4.y)
                * squared + (-value1.y + 3.0f * value2.y - 3.0f * value3.y + value4.y) * cubed));
    }

    public static float rotLerpRadians(float delta, float start, float end) {
        return start + delta * wrapRadians(end - start);
    }

    public static float wrapRadians(float radians) {
        float result = radians % Mth.TWO_PI;
        if (result >= Mth.PI) {
            result -= Mth.TWO_PI;
        }
        if (result < -Mth.PI) {
            result += Mth.TWO_PI;
        }
        return result;
    }

    public static Vec3[] sampleRadialPosAndTangentVel(Vec3[] axis, RandomSource source, Vector2f direction,
                                                      float pi, double radius, double minSpeed, double maxSpeed) {
        Vector2f pos2D = direction.rotatedByRandom(source, Vector2f.ZERO, pi, false).mul(radius);
        Vec3[] posAndVelocity = new Vec3[2];
        posAndVelocity[0] = sampleRadialPos(axis, source, null, pi, pos2D, radius);
        Vector2f speed = pos2D.rotatedBy(Mth.HALF_PI, Vector2f.ZERO, true);

        Vec3 velocityDir = axis[0].scale(speed.x).add(axis[1].scale(speed.y));
        double speedValue = minSpeed + source.nextDouble() * (maxSpeed - minSpeed);
        posAndVelocity[1] = velocityDir.scale(speedValue);
        return posAndVelocity;
    }

    public static Vec3 sampleRadialPos(Vec3[] axis, RandomSource source, Vector2f direction, float pi, Vector2f pos, double radius) {
        Vector2f pos2D = pos == null ? direction.rotatedByRandom(source, Vector2f.ZERO, pi, false).mul(radius) : pos;
        return mapToRelativePlaneCoordinates(axis, pos2D);
    }

    public static Vec3 mapToRelativePlaneCoordinates(Vec3[] axis, Vector2f pos2D) {
        return axis[0].scale(pos2D.x).add(axis[1].scale(pos2D.y));
    }

    public static float angleLerp(float	curAngle, float	targetAngle, float amount) {
        float angle;
        if (targetAngle < curAngle) {
            float num = targetAngle + (float)Math.PI * 2f;
            angle = Mth.lerp(amount, curAngle, (num - curAngle > curAngle - targetAngle) ? targetAngle : num);
        } else {
            if (!(targetAngle > curAngle)) {
                return curAngle;
            }
            float num2 = targetAngle - (float)Math.PI * 2f;
            angle = Mth.lerp(amount, curAngle, (targetAngle - curAngle > curAngle - num2) ? num2 : amount);
        }
        return Mth.wrapDegrees(angle);
    }

    public static Quaternionf directionToQuaternion(Vec3 direction) {
        Vec3 dir = direction.normalize();

        Vector3f defaultForward = new Vector3f(0f, 0f, 1f);
        Vector3f target = new Vector3f((float) dir.x, (float) dir.y, (float) dir.z);

        float dot = defaultForward.dot(target);
        if (dot > 0.9999f) return new Quaternionf(); // 单位四元数

        if (dot < -0.9999f) return new Quaternionf().rotateAxis(Mth.PI, new Vector3f(0, 1, 0));

        Vector3f axis = new Vector3f(
            defaultForward.y() * target.z() - defaultForward.z() * target.y(),
            defaultForward.z() * target.x() - defaultForward.x() * target.z(),
            defaultForward.x() * target.y() - defaultForward.y() * target.x()
        );

        axis.normalize();
        float angleRad = (float) Math.acos(Math.max(-1.0f, Math.min(1.0f, dot)));

        return new Quaternionf().rotateAxis(angleRad, axis);
    }

    public static Vec3 slerp(Vec3 start, Vec3 end, float t) {
        Vec3 v0 = start.normalize();
        Vec3 v1 = end.normalize();

        double dot = v0.dot(v1);
        dot = Math.max(-1.0, Math.min(1.0, dot));

        if (dot > 0.9995) return v0.scale(1 - t).add(v1.scale(t)).normalize();

        double theta = Math.acos(dot) * t;
        Vec3 relativeVec = v1.subtract(v0.scale(dot)).normalize();

        return v0.scale(Math.cos(theta)).add(relativeVec.scale(Math.sin(theta)));
    }

    //My previous knowledge of shaders was only in the hints for game loading. I don't even know what it is. this is too bad
    @OnlyIn(Dist.CLIENT)
    public static final class Shaders extends RenderType {
        private static ShaderStateShard FADED_UV_MAP_STREAK_STATE_SHARD;
        private static ShaderStateShard RANCOR_STATE_SHARD;
        private static ShaderStateShard FLAME_STATE_SHARD;
        public static ShaderStateShard BASE_SHARD;
        private static ShaderStateShard RADIAL_SHINE;
        private static ShaderStateShard DISINTEGRATION;
        private static ShaderStateShard ENERGY;
        private static Supplier<ShaderInstance> SCARLET_LIGHTNING_SHADER;

        private static final ShaderStateShard BLACK_HOLE_SHARD =
            new ShaderStateShard(CalamityCelestialBodyShader::getBlackInstance);
        private static final ShaderStateShard SUN_SHARD =
            new ShaderStateShard(CalamityCelestialBodyShader::getSunInstance);

        @SuppressWarnings("deprecation")
        public static final ParticleRenderType GENERIC_BLOOM = new ParticleRenderType() {
            @Override
            public void begin(BufferBuilder bufferBuilder, @NotNull TextureManager textureManager) {
                RenderSystem.enableBlend();
                RenderSystem.depthMask(false);
                RenderSystem.disableCull();
                RenderSystem.setShader(GameRenderer::getParticleShader);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            }

            @Override
            public void end(Tesselator tesselator) {
                tesselator.end();
            }

            @Override
            public String toString() {
                return "GENERIC_BLOOM";
            }
        };

        private Shaders(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling,
                        boolean sortOnUpload, Runnable setupState, Runnable clearState) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        }

        public static void registerShaders(RegisterShadersEvent event) {
            try {
                ResourceProvider manager = event.getResourceProvider();
                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("faded_uv_map_streak"),
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader ->
                    FADED_UV_MAP_STREAK_STATE_SHARD = new ShaderStateShard(() -> shader));

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("flame"),
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader ->
                    FLAME_STATE_SHARD = new ShaderStateShard(() -> shader));

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("rancor_magic_circle"),
                    DefaultVertexFormat.NEW_ENTITY), shader ->
                    RANCOR_STATE_SHARD = new ShaderStateShard(() -> shader));

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("base"),
                    DefaultVertexFormat.POSITION_COLOR_TEX),
                    shader -> BASE_SHARD = new ShaderStateShard(() -> shader));

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("disintegration"),
                    DefaultVertexFormat.POSITION_COLOR_TEX),
                    shader -> DISINTEGRATION = new ShaderStateShard(() -> shader));

                event.registerShader(new CalamityCelestialBodyShader.BlackHoleShader(manager, CalamityCurios.ModResource("real_black_hole"),
                    DefaultVertexFormat.NEW_ENTITY), CalamityCelestialBodyShader::setInstance);

                event.registerShader(CalamityCelestialBodyShader.SunShader.createRadialShineShader(manager),
                    shader -> RADIAL_SHINE = new ShaderStateShard(() -> shader));

                event.registerShader(new CalamityCelestialBodyShader.SunShader(manager), CalamityCelestialBodyShader::setInstance);

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("converging_genesis_energy"),
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> ENERGY = new ShaderStateShard(() -> shader));

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("fleshy_vignette"),
                    DefaultVertexFormat.POSITION_TEX), ErosionScreenRenderer::setErosionShader);

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("lightning_arc"),
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> SCARLET_LIGHTNING_SHADER = () -> shader);
            } catch (Exception e) {
                throw new RuntimeException("a fatal error occurred when registering shaders", e);
            }
        }

        public static Supplier<ShaderInstance> getScarletLightningShader() {
            return SCARLET_LIGHTNING_SHADER;
        }

        public static RenderType getLemniscateRenderType(ResourceLocation texture) {
            return RenderType.create("primitive", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES,
                256, false, false, CompositeState.builder()
                    .setShaderState(FADED_UV_MAP_STREAK_STATE_SHARD)
                    .setTextureState(new TextureStateShard(texture, false, true))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
            );
        }

        public static RenderType getDisintegration() {
            return RenderType.create("disintegration", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
                256, false, false, CompositeState.builder()
                    .setTextureState(new TextureStateShard(CalamityCelestialBodyShader.PERLIN, false, false))
                    .setShaderState(DISINTEGRATION)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));
        }

        public static RenderType getRancorLaserRenderType(ResourceLocation texture) {
            return RenderType.create("flame_shader", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES,
                256, false, false, CompositeState.builder()
                    .setShaderState(FLAME_STATE_SHARD)
                    .setTextureState(new TextureStateShard(texture, false, true))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
            );
        }

        public static RenderType getRancorCircleRenderType(ResourceLocation texture, boolean isGlowMask, boolean noDepthWrite) {
            return RenderType.create("rancor_magic_circle", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, false, false, CompositeState.builder()
                    .setShaderState(RANCOR_STATE_SHARD)
                    .setTextureState(new TextureStateShard(texture, false, true))
                    .setTransparencyState(isGlowMask ? RenderStateShard.ADDITIVE_TRANSPARENCY : RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(noDepthWrite ? RenderStateShard.COLOR_WRITE : RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
            );
        }

        public static RenderType getBlackHole() {
            return RenderType.create("black_hole", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
                256, false, false, CompositeState.builder()
                    .setShaderState(BLACK_HOLE_SHARD)
                    .setTextureState(new MultiTextureStateShard.Builder()
                        .add(CalamityCelestialBodyShader.BASE_TEXTURE, true, false)
                        .add(CalamityCelestialBodyShader.FIRE_NOISE, true, false).build())
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setCullState(RenderStateShard.CULL)
                    .createCompositeState(false)
            );
        }

        public static RenderType getConvergingGenesisEnergy() {
            return RenderType.create("converging_genesis_energy", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.TRIANGLES, 256, false, false, CompositeState.builder()
                    .setShaderState(ENERGY)
                    .setTextureState(new TextureStateShard(CalamityCelestialBodyShader.PERLIN, false, false))
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.CULL)
                    .createCompositeState(false)
            );
        }

        public static RenderType getGlowRenderType(ResourceLocation texture) {
            return RenderType.create("glow_entity", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, true, true, CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)//
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(false));
        }

        public static RenderType getStormMaidensGlow() {
            return create("storm_maidens", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, true, true, CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                    .setTextureState(new TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY).setLightmapState(LIGHTMAP)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setOverlayState(OVERLAY).createCompositeState(true));
        }

        public static RenderType getEnchanmentRenderType(ResourceLocation texture) {
            return RenderType.create("enchanted_particle", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
                256, false, false, CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .createCompositeState(false));
        }

        public static RenderType getRadialShineRenderType() {
            return RenderType.create("radial_shine", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
                256, false, false, CompositeState.builder()
                    .setShaderState(RADIAL_SHINE)
                    .setTextureState(new TextureStateShard(CalamityCelestialBodyShader.WAVY_BLOTCH_NOISE, true, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(CULL)
                    .createCompositeState(false));
        }

        public static RenderType getSunRenderType() {
            return RenderType.create("sun", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
                256, false, false, CompositeState.builder()
                    .setShaderState(SUN_SHARD)
                    .setTextureState(new MultiTextureStateShard.Builder()
                        .add(CalamityCelestialBodyShader.DENDRITIC_NOISE, true, false)
                        .add(CalamityCelestialBodyShader.WAVY_BLOTCH_NOISE, true, false)
                        .add(CalamityCelestialBodyShader.PSYCHEDELIC, true, false).build())
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(CULL)
                    .createCompositeState(false));
        }

        public static RenderType getCircleSmall() {
            return RenderType.create("circleSmall", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
                256, false, false, CompositeState.builder()
                    .setShaderState(BASE_SHARD)
                    .setTextureState(new TextureStateShard(CalamityCelestialBodyShader.BLOOM_CIRCLE_SMALL, true, false))
                    .setTransparencyState(new TransparencyStateShard("color_blend_alpha_write", () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(
                            GlStateManager.SourceFactor.SRC_COLOR,
                            GlStateManager.DestFactor.ONE,
                            GlStateManager.SourceFactor.ONE,
                            GlStateManager.DestFactor.ONE
                        );
                    }, () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    })).setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(CULL)
                    .createCompositeState(false));
        }
    }
}
