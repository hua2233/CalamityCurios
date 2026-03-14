package hua223.calamity.util;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.entity.projectiles.BaseProjectile;
import hua223.calamity.register.recipe.CalamityCurseRecipe;
import hua223.calamity.render.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.ItemDecoratorHandler;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLivingEvent;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public final class RenderUtil {
    public static final Vector4f MAGENTA = new Vector4f(255f, 0f, 255f, 255f);
    public static final Vector4f CYAN = new Vector4f(0f, 255f, 255f, 255f);
    public static final Vector4f BLUE = new Vector4f(0f, 0f, 255f, 255f);
    public static final Vector4f RED = new Vector4f(255f, 0f, 0f, 255f);
    public static final Vector4f WHITE = new Vector4f(255f, 255f, 255f, 255f);
    public static final Vector4f DARK_VIOLET = new Vector4f(148, 0, 211, 255f);
    public static final Vector4f DARK_ORCHID = new Vector4f(153f, 50f, 204f, 255f);
    public static final Vector4f INDIAN_RED = new Vector4f(205f, 92f, 204f, 255f);
    public static final Vector4f TRANSPARENT = new Vector4f(0f, 0f, 0f, 0f);
    public static final Vector4f DARK_RED = new Vector4f(139f, 0f, 0f, 255f);
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

    public static boolean renderGuiEnchantParticle;
    public static final IItemDecorator EXHUMED_DECORATOR = (font, itemStack, x, y, blitOffset) -> {
        if (renderGuiEnchantParticle) {
            EnchantedParticleSet.drawSet(x + 8, y + 8, blitOffset - 2F);
            return true;
        }

        return false;
    };

    private static short rainbowR = 255;
    private static short rainbowG = 0;
    private static short rainbowB = 0;
    private static byte rainbowStyle = 0;
    private static boolean init = true;
    public static int astrAmount;

    private static short tick;

    private RenderUtil() {
    }

    public static void updateGlobal() {
        if (tick++ == 3600) tick = 0;
        EnchantedParticleSet.update();
        CurseFont.updateTick();
        switch (rainbowStyle) {
            case 0 -> {
                rainbowG += 7;
                if (rainbowG >= 255) {
                    rainbowG = 255;
                    rainbowStyle++;
                }
            }

            case 1 -> {
                rainbowR -= 7;
                if (rainbowR <= 0) {
                    rainbowR = 0;
                    rainbowStyle++;
                }
            }

            case 2 -> {
                rainbowB += 7;
                if (rainbowB >= 255) {
                    rainbowB = 255;
                    rainbowStyle++;
                }
            }

            case 3 -> {
                rainbowG -= 7;
                if (rainbowG <= 0) {
                    rainbowG = 0;
                    rainbowStyle++;
                }
            }

            case 4 -> {
                rainbowR += 7;
                if (rainbowR >= 255) {
                    rainbowR = 255;
                    rainbowStyle++;
                }
            }

            case 5 -> {
                rainbowB -= 7;
                if (rainbowB <= 0) {
                    rainbowB = 0;
                    rainbowStyle = 0;
                }
            }
        }
    }

    public static Component getRainbow(MutableComponent component) {
        component.setStyle(Style.EMPTY.withColor(((rainbowR & 0xFF) << 16) |
            ((rainbowG & 0xFF) << 8) |
            ((rainbowB & 0xFF))));

        return component;
    }

    public static short getLocalTick() {
        return tick;
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

    public static void crossTextureRendering(BaseProjectile projectile, VertexConsumer consumer, PoseStack poseStack, int packedLight) {
        poseStack.mulPose(Vector3f.YP.rotationDegrees(projectile.tickCount * 2));
        PoseStack.Pose last = poseStack.last();
        renderTexture(last.pose(), last.normal(), consumer, packedLight);

        poseStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        renderTexture(last.pose(), last.normal(), consumer, packedLight);
    }

    public static int interpolateColor(int startColor, int endColor, float progress) {
        int r = (int) (getRed(startColor) + (getRed(endColor)
            - getRed(startColor)) * progress);

        int g = (int) (getGreen(startColor) + (getGreen(endColor)
            - getGreen(startColor)) * progress);

        int b = (int) (getBlue(startColor) + (getBlue(endColor)
            - getBlue(startColor)) * progress);
        return (r << 16) | (g << 8) | b;
    }

    public static Vector2d directionTo(Vec3 vec3, double x, double y) {
        double dx = vec3.x - x;
        double dy = vec3.y - y;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) {
            return new Vector2d(0, 0);
        }

        return new Vector2d(dx / length, dy / length);
    }

    public static Vector4f black() {
        return new Vector4f(0f, 0f, 0f, 255f);
    }

    //handler r, g, b color component and reset a
    public static Vector4f interpolateColor(Vector4f start, Vector4f end, float delta, Vector4f container) {
        float red = Mth.lerp(delta, start.x(), end.x());
        float green = Mth.lerp(delta, start.y(), end.y());
        float blue = Mth.lerp(delta, start.z(), end.z());

        if (container != null) {
            container.set(red, green, blue, 255f);
            return container;
        }

        return new Vector4f(red, green, blue, 255f);
    }

    public static Vector4f multiplyColor(Vector4f color, float factor, Vector4f container) {
        container.set(Math.min(255f, color.x() * factor), Math.min(255f, color.y() * factor),
            Math.min(255f, color.z() * factor), Math.min(255f, color.w() * factor));
        return container;
    }

    public static float clampLerp(float from, float to, float t, boolean clamped) {
        if (clamped) {
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
        }
        return (t - from) / (to - from);
    }

    public static float piecewiseAnimation(float progress, CurveSegment... segments) {
        if (segments.length == 0) return 0f;

        //If for whatever reason you try to not play by the rules, get fucked
        if (segments[0].startingX != 0) segments[0].startingX = 0;

        //Clamp the progress
        progress = Mth.clamp(progress, 0f, 1f);
        float ratio = 0f;

        for (int i = 0; i <= segments.length - 1; i++) {
            CurveSegment segment = segments[i];
            float startPoint = segment.startingX;
            float endPoint = 1f;

            //Too early. This should never get reached,
            //Since by the time you'd have gotten there you'd have found the appropriate segment and broken out of the for loop
            if (progress < segment.startingX) continue;

            if (i < segments.length - 1) {
                //Too late
                if (segments[i + 1].startingX <= progress) continue;
                endPoint = segments[i + 1].startingX;
            }

            float segmentLength = endPoint - startPoint;
            float segmentProgress = (progress - segment.startingX) / segmentLength; //How far along the specific segment
            ratio = segment.startingHeight;

            //Failsafe because somehow it can fail? what
            ratio += Objects.requireNonNullElse(segment.easing, CurveSegment.EasingType.LINEAR)
                .easingFunction(segmentProgress, segment.degree) * segment.elevationShift;

            break;
        }
        return ratio;
    }

    public static void setShaderInterpolateColor(int startColor, int endColor, float progress) {
        float r;
        float g;
        float b;

        if (progress >= 1f) {
            r = getRed(endColor);
            g = getGreen(endColor);
            b = getBlue(endColor);
        } else if (progress <= 0) {
            r = getRed(startColor);
            g = getGreen(startColor);
            b = getBlue(startColor);
        } else {
            r = (getRed(startColor) + (getRed(endColor) - getRed(startColor)) * progress);
            g = (getGreen(startColor) + (getGreen(endColor) - getGreen(startColor)) * progress);
            b = (getBlue(startColor) + (getBlue(endColor) - getBlue(startColor)) * progress);
        }

        RenderSystem.setShaderColor(r / 255f, g / 255f, b / 255f, 1f);
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static Vec3 subtractVec2(Vec3 start, Vec3 end) {
        return new Vec3(end.x - start.x, end.y - start.y, end.z);
    }

    public static void reuseQuaternions(Quaternion quaternion, Vector3f axis, float degrees) {
        float rotationAngle = degrees * ((float) Math.PI / 180F);
        float f = (float) Math.sin(rotationAngle / 2.0d);

        quaternion.set(axis.x() * f, axis.y() * f, axis.z() * f, (float) Math.cos(rotationAngle / 2f));
    }

    public static float smoothStep(float from, float to, float amount) {
//        amount = Mth.clamp(amount, 0, 1f);
//        amount = amount * amount * (3f - 2f * amount);
//        return from + (to - from) * amount;
        return hermite(from, 0f, to, 0f, Mth.clamp(amount, 0f, 1f));
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

    public static Vec3 catmullRomVec(Vec3 value1, Vec3 value2, Vec3 value3, Vec3 value4, float amount, boolean calculationZAxis) {
        float squared = amount * amount;
        float cubed = amount * squared;
        return new Vec3(
            0.5f * ((2.0f * value2.x) + (-value1.x + value3.x) *
                amount + (2.0f * value1.x - 5.0f * value2.x + 4.0f * value3.x - value4.x)
                * squared + (-value1.x + 3.0f * value2.x - 3.0f * value3.x + value4.x) * cubed),

            0.5f * ((2.0f * value2.y) + (-value1.y + value3.y) *
                amount + (2.0f * value1.y - 5.0f * value2.y + 4.0f * value3.y - value4.y)
                * squared + (-value1.y + 3.0f * value2.y - 3.0f * value3.y + value4.y) * cubed),

            calculationZAxis ? 0.5f * ((2.0f * value2.z) + (-value1.z + value3.z) *
                amount + (2.0f * value1.z - 5.0f * value2.z + 4.0f * value3.z - value4.z)
                * squared + (-value1.z + 3.0f * value2.z - 3.0f * value3.z + value4.z) * cubed) : value3.z);
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

    public static Vec3[] sampleRadialPosAndTangentVel(Vec3[] axis, RandomSource source, Vector2d direction,
                                                      float pi, double radius, double minSpeed, double maxSpeed) {
        Vector2d pos2D = direction.rotatedByRandom(source, Vector2d.ZERO, pi, false).mul(radius);
        Vec3[] posAndVelocity = new Vec3[2];
        posAndVelocity[0] = sampleRadialPos(axis, source, null, pi, pos2D, radius);
        Vector2d speed = pos2D.rotatedBy(Mth.HALF_PI, Vector2d.ZERO, true);

        Vec3 velocityDir = axis[0].scale(speed.x).add(axis[1].scale(speed.y));
        double speedValue = minSpeed + source.nextDouble() * (maxSpeed - minSpeed);
        posAndVelocity[1] = velocityDir.scale(speedValue);
        return posAndVelocity;
    }

    public static Vec3 sampleRadialPos(Vec3[] axis, RandomSource source, Vector2d direction, float pi, Vector2d pos, double radius) {
        Vector2d pos2D = pos == null ? direction.rotatedByRandom(source, Vector2d.ZERO, pi, false).mul(radius) : pos;
        return mapToRelativePlaneCoordinates(axis, pos2D);
    }

    public static Vec3 mapToRelativePlaneCoordinates(Vec3[] axis, Vector2d pos2D) {
        return axis[0].scale(pos2D.x).add(axis[1].scale(pos2D.y));
    }

    @SuppressWarnings("all")
    public static void registerExhumedItemDecorator(RecipeManager manager) {
        //Error! Please do not do this in the Register Item Decorations Event, as the recipe does not exist on the client side at this time,
        //and do not attempt to obtain it locally, as the recipe should be synchronized from the server side
        //Minecraft.getBlackInstance().level.getRecipeManager().getAllRecipesFor(CalamityCurseRecipe.CurseRecipeType.INSTANCE);

        //Register after migrating to recipes update event, as the client has already been synchronized at this time
        List<CalamityCurseRecipe> recipes = manager.getAllRecipesFor(CalamityCurseRecipe.CurseRecipeType.INSTANCE);
        Map<Item, ItemDecoratorHandler> decoratorMap = ItemDecoratorHandler.DECORATOR_LOOKUP;
        if (recipes.isEmpty()) {
            ImmutableMap.Builder<Item, ItemDecoratorHandler> builder = ImmutableMap.builder();
            ItemDecoratorHandler.DECORATOR_LOOKUP = builder.putAll(decoratorMap).build();
            return;
        }


        ItemDecoratorHandler handler = new ItemDecoratorHandler(List.of(EXHUMED_DECORATOR));
        for (CalamityCurseRecipe recipe : recipes) {
            Item item = recipe.getReactant().getItem();
            decoratorMap.compute(item, (k, v) -> {
                //Each item should have only one decorative renderer
                if (v == null) return handler;
                else if (!v.itemDecorators.contains(EXHUMED_DECORATOR))
                    v.itemDecorators.add(EXHUMED_DECORATOR);

                return v;
            });
        }

        //After the setting is completed, it will be reverted back to immutable
        ImmutableMap.Builder<Item, ItemDecoratorHandler> builder = ImmutableMap.builder();
        ItemDecoratorHandler.DECORATOR_LOOKUP = builder.putAll(decoratorMap).build();
    }

    @SuppressWarnings("all")
    public static void clearOldDecorator(RecipeManager manager, boolean isLogout) {
        HashMap<Item, ItemDecoratorHandler> map = new HashMap<>(ItemDecoratorHandler.DECORATOR_LOOKUP);
        ItemDecoratorHandler.DECORATOR_LOOKUP = map;

        if (init) {
            //When entering the world and not yet initialized, the client has not accepted synchronization,
            //has no old values, and is only set as HashMap, providing a mutable container for future synchronization changes
            init = false;
            return;
        }

        for (CalamityCurseRecipe recipe : manager.getAllRecipesFor(CalamityCurseRecipe.CurseRecipeType.INSTANCE)) {
            Item reactant = recipe.getReactant().getItem();
            if (map.containsKey(reactant)) {
                List<IItemDecorator> decorators = map.get(reactant).itemDecorators;

                if (decorators.size() == 1) map.remove(reactant);
                else decorators.remove(EXHUMED_DECORATOR);
            }
        }

        if (isLogout) {
            //Restore initialization upon exit and set it to immutable
            //Client data is automatically reset only upon JVM restart,
            //so manually set to an immutable default value upon exit to prevent confusion of client data caused by entering multiple servers
            init = true;
            ImmutableMap.Builder<Item, ItemDecoratorHandler> builder = ImmutableMap.builder();
            ItemDecoratorHandler.DECORATOR_LOOKUP = builder.putAll(map).build();
        }
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

    @SuppressWarnings("unchecked")
    public static void onlyThirdPersonRender(AbstractClientPlayer player, boolean showRightArm,
                                             boolean showLeftArm, boolean showRightItem, boolean showLeftItem) {
        try {
            Class<KeyframeAnimation> animationClass = KeyframeAnimation.class;
            Constructor<KeyframeAnimation> constructor =
                (Constructor<KeyframeAnimation>) animationClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            HashMap<?, ?> empty = new HashMap<>();
            KeyframeAnimation emptyKey = constructor.newInstance(0, 9999, 9999, true, 9999, empty, true, false, null, AnimationFormat.UNKNOWN, empty);
            KeyframeAnimationPlayer key = new KeyframeAnimationPlayer(emptyKey);
            key.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
            key.setFirstPersonConfiguration(new FirstPersonConfiguration(showRightArm, showLeftArm, showRightItem, showLeftItem));
            PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(18, key);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void cancelThirdPersonRendering(AbstractClientPlayer player) {
        PlayerAnimationAccess.getPlayerAnimLayer(player).removeLayer(18);
    }

    public static Quaternion directionToQuaternion(Vec3 direction) {
        Vec3 dir = direction.normalize();

        Vector3f defaultForward = Vector3f.ZP;
        Vector3f target = new Vector3f((float) dir.x, (float) dir.y, (float) dir.z);

        float dot = defaultForward.dot(target);
        if (dot > 0.9999f) return Quaternion.ONE; // 单位四元数

        if (dot < -0.9999f) return new Quaternion(Vector3f.YP, 180.0f, true);

        Vector3f axis = new Vector3f(
            defaultForward.y() * target.z() - defaultForward.z() * target.y(),
            defaultForward.z() * target.x() - defaultForward.x() * target.z(),
            defaultForward.x() * target.y() - defaultForward.y() * target.x()
        );

        axis.normalize();
        float angleRad = (float) Math.acos(Math.max(-1.0f, Math.min(1.0f, dot)));

        // 创建四元数：绕 axis 旋转 angleRad 弧度
        return new Quaternion(axis, angleRad, false);
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
        private static final ResourceLocation FLASH = CalamityCurios.ModResource("textures/misc/flash.png");
        private static ShaderInstance FADED_UV_MAP_STREAK;
        private static final ShaderStateShard FADED_UV_MAP_STREAK_STATE_SHARD =
            new ShaderStateShard(() -> FADED_UV_MAP_STREAK);

        private static ShaderInstance RANCOR_MAGIC_CIRCLE;
        private static final ShaderStateShard RANCOR_STATE_SHARD =
            new ShaderStateShard(() -> RANCOR_MAGIC_CIRCLE);

        private static ShaderInstance FLAME;
        private static final ShaderStateShard FLAME_STATE_SHARD =
            new ShaderStateShard(() -> FLAME);

        private static ShaderInstance BASE;
        private static final ShaderStateShard BASE_SHARD =
            new ShaderStateShard(() -> BASE);

        private static final ShaderStateShard BLACK_HOLE_SHARD =
            new ShaderStateShard(CalamityCelestialBodyShader::getBlackInstance);

        private static ShaderInstance SHINE;
        private static final ShaderStateShard RADIAL_SHINE =
            new ShaderStateShard(() -> SHINE);

        private static final ShaderStateShard SUN_SHARD =
            new ShaderStateShard(CalamityCelestialBodyShader::getSunInstance);
        //It should just be a shader, borrowed for post-processing, because Minecraft's post-processing is singleton
        private static PostChain LIGHT;

        private static long flashStartTime;
        private static float baseFlashIntensity;
        private static int fadeInTime;
        private static boolean flashEffect;
        private static double flashTime;
        private static boolean notRenderBlock = true;
        private static int framebufferWidth;
        private static int framebufferHeight;

        public static final ParticleRenderType GENERIC_BLOOM = new ParticleRenderType() {
            @Override
            public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
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
                RenderSystem.enableCull();
                RenderSystem.depthMask(true);
                RenderSystem.defaultBlendFunc();
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
                ResourceManager manager = event.getResourceManager();

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("faded_uv_map_streak"),
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> FADED_UV_MAP_STREAK = shader);

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("flame"),
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> FLAME = shader);

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("rancor_magic_circle"),
                    DefaultVertexFormat.NEW_ENTITY), shader -> RANCOR_MAGIC_CIRCLE = shader);

                event.registerShader(new ShaderInstance(manager, CalamityCurios.ModResource("base"),
                    DefaultVertexFormat.POSITION_COLOR_TEX), shader -> BASE = shader);

                event.registerShader(new CalamityCelestialBodyShader.BlackHoleShader(manager, CalamityCurios.ModResource("real_black_hole"),
                    DefaultVertexFormat.NEW_ENTITY), CalamityCelestialBodyShader::setInstance);

                event.registerShader(CalamityCelestialBodyShader.SunShader.createRadialShineShader(manager), shader -> SHINE = shader);

                event.registerShader(new CalamityCelestialBodyShader.SunShader(manager), CalamityCelestialBodyShader::setInstance);
            } catch (Exception e) {
                throw new RuntimeException("a fatal error occurred when registering shaders", e);
            }
        }

        public static RenderType getLemniscateRenderType(ResourceLocation texture) {
            return RenderType.create("lemniscate_shader", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.TRIANGLES,
                256, false, false, CompositeState.builder()
                    .setShaderState(FADED_UV_MAP_STREAK_STATE_SHARD)
                    .setTextureState(new TextureStateShard(texture, false, true))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
            );
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
            return RenderType.create("black_hole", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, false, false, CompositeState.builder()
                    .setShaderState(BLACK_HOLE_SHARD)
                    .setTextureState(new MultiTextureStateShard.Builder()
                        .add(CalamityCelestialBodyShader.BASE_TEXTURE, true, false)
                        .add(CalamityCelestialBodyShader.FIRE_NOISE, true, false).build())
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
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
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(false));
        }

        public static final TransparencyStateShard COLOR_BLEND_ALPHA_WRITE = new TransparencyStateShard("color_blend_alpha_write", () -> {
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
        });

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
                    .setTransparencyState(COLOR_BLEND_ALPHA_WRITE)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(CULL)
                    .createCompositeState(false));
        }

        public static void setScreenFlashEffect(int time, float intensity) {
            flashStartTime = Minecraft.getInstance().level.getGameTime();
            flashTime = time;
            fadeInTime = (int) (time * 0.5f);
            baseFlashIntensity = intensity;
            flashEffect = true;
        }

        public static void preScreenRender(float partialTick, Minecraft minecraft) {
            if (flashEffect) {
                ClientLevel level = minecraft.level;
                if (level == null) {
                    flashEffect = false;
                    return;
                }

                long age = level.getGameTime() - flashStartTime;
                if (age >= flashTime) {
                    flashEffect = false;
                    return;
                }

                int width = minecraft.getWindow().getGuiScaledWidth();
                int height = minecraft.getWindow().getGuiScaledHeight();

                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, getIntensity(partialTick, age, minecraft));
                RenderSystem.setShaderTexture(0, FLASH);

                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tesselator.getBuilder();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                bufferbuilder.vertex(0.0D, height, -90.0D).uv(0.0F, 1.0F).endVertex();
                bufferbuilder.vertex(width, height, -90.0D).uv(1.0F, 1.0F).endVertex();
                bufferbuilder.vertex(width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
                bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
                tesselator.end();

                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        private static float getIntensity(float partialTick, long age, Minecraft minecraft) {
            float adjustedAge = age + partialTick;
            float screen = minecraft.options.screenEffectScale().get().floatValue();

            framebufferHeight = framebufferWidth = -1;

            if (age <= fadeInTime) {
                return baseFlashIntensity * (adjustedAge / fadeInTime) * screen;
            } else {
                float fadeOutDuration = (float) (flashTime - fadeInTime);
                float fadeOutProgress = (adjustedAge - fadeInTime) / fadeOutDuration;
                return baseFlashIntensity * (1.0f - fadeOutProgress) * screen;
            }
        }

        @SuppressWarnings("removal")
        public static void renderBlockPerspective(net.minecraftforge.client.event.RenderLevelLastEvent event) {
            if (notRenderBlock || CalamityOutlineRenderer.notRender()) return;
            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();
            int width = window.getScreenWidth();
            int height = window.getScreenHeight();
            if ((width != framebufferWidth || height != framebufferHeight) && width * height > 0) {
                framebufferWidth = width;
                framebufferHeight = height;
                LIGHT.resize(framebufferWidth, framebufferHeight);
            }

            RenderTarget lightFbo = LIGHT.getTempTarget("final");
            lightFbo.clear(Minecraft.ON_OSX);
            lightFbo.bindWrite(false);

            //不再禁用深度，它们之间会有远近的关系.
            RenderSystem.disableBlend();
            RenderSystem.disableTexture();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            CalamityOutlineRenderer.renderOutlineList(minecraft, event.getPoseStack(), LIGHT, event.getPartialTick());

            minecraft.getMainRenderTarget().bindWrite(false);
            //内部已禁用
            //RenderSystem.disableDepthTest();
            //RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            lightFbo.blitToScreen(framebufferWidth, framebufferHeight, false);

            RenderSystem.enableTexture();
//            RenderSystem.depthMask(true);
        }

        public static void renderHighlightBlocks(boolean close) {
            if (close) {
                RenderSystem.recordRenderCall(() -> {
                    notRenderBlock = true;
                    CalamityOutlineRenderer.close();
                    if (LIGHT != null) {
                        LIGHT.close();
                        DelayRunnable.removeTask(LIGHT);
                        LIGHT = null;
                    }
                });
            } else if (notRenderBlock) {
                RenderSystem.recordRenderCall(() -> {
                    final Minecraft minecraft = Minecraft.getInstance();
                    try {
                        LIGHT = new PostChain(minecraft.textureManager, minecraft.getResourceManager(),
                            minecraft.getMainRenderTarget(), CalamityCurios.ModResource("shaders/post/outline.json"));
                        LIGHT.resize(minecraft.getWindow().getScreenWidth(), minecraft.getWindow().getScreenHeight());
                    } catch (IOException e) {
                        CalamityCurios.LOGGER.error("Cannot find outline shader file!!!");
                        return;
                    }

                    notRenderBlock = false;
                    CalamityOutlineRenderer.init(minecraft);
                    DelayRunnable.addUniqueLoopTask(() -> {
                        if (minecraft.player == null || !minecraft.player.hasEffect(CalamityEffects.OMNISCIENCE.get())) {
                            RenderSystem.recordRenderCall(() -> {
                                CalamityOutlineRenderer.close();
                                LIGHT.close();
                                LIGHT = null;
                                notRenderBlock = true;
                            });
                            return true;
                        } else {
                            CalamityOutlineRenderer.updateOutlineTarget(minecraft);
                            return false;
                        }
                    }, 1, LIGHT);
                });
            }
        }

        //Hallucinogenic
        private static boolean trippy;
        private static boolean notTrippyRender = true;
        private static Random random;

        public static void startHallucinogenic() {
            if (!trippy) {
                MobEffect effect = CalamityEffects.TRIPPY.get();
                final Minecraft minecraft = Minecraft.getInstance();
                RenderSystem.recordRenderCall(() -> {
                    minecraft.gameRenderer.loadEffect(CalamityCurios.ModResource("shaders/post/heat_distortion.json"));
                    trippy = true;
                    random = new Random();
                    IllusionBufferSource.create();
                });

                DelayRunnable.conditionsLoop(() -> {
                    Player player = minecraft.player;
                    if (player == null || !player.hasEffect(effect)) {
                        RenderSystem.recordRenderCall(() -> {
                            PostChain shader = minecraft.gameRenderer.currentEffect();
                            if (shader != null && shader.getName().equals("calamity_curios:shaders/post/heat_distortion.json"))
                                minecraft.gameRenderer.shutdownEffect();
                            trippy = false;
                            random = null;
                            IllusionBufferSource.destroy();
                        });
                        return true;
                    }

                    IllusionBufferSource.setColor(rainbowR, rainbowG, rainbowB, 255);
                    return false;
                }, 1);
            }
        }

        public static void psychedelic(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
            if (trippy && notTrippyRender) {
                event.setCanceled(true);
                MultiBufferSource buffer = IllusionBufferSource.getSource(event.getMultiBufferSource());
                PoseStack pose = event.getPoseStack();
                LivingEntityRenderer<LivingEntity, ? extends EntityModel<? extends LivingEntity>> renderer =
                    (LivingEntityRenderer<LivingEntity, ? extends EntityModel<? extends LivingEntity>>) event.getRenderer();
                float partialTicks = event.getPartialTick();
                int packedLight = event.getPackedLight();
                LivingEntity entity = event.getEntity();
                boolean transformable = tick % 100 == 0;
                Vector2d[] offsets = entity.calamity$GetPhantomOffset();
                notTrippyRender = false;
                for (Vector2d offset : offsets) {
                    pose.pushPose();
                    if (transformable) offset.set((random.nextDouble(1.6) + 0.8) * (random.nextBoolean() ? 1 : -1),
                        (random.nextDouble(1.6) + 0.8) * (random.nextBoolean() ? 1 : -1));
                    pose.translate(offset.x, 0, offset.y);
                    renderer.render(entity, entity.getYRot(), partialTicks, pose, buffer, packedLight);
                    pose.popPose();
                }
                notTrippyRender = true;
            }
        }
    }
}
