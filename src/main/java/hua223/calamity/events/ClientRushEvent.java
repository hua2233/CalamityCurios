package hua223.calamity.events;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2d;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import org.joml.Matrix4f;
import org.joml.Vector4i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@OnlyIn(Dist.CLIENT)
//客户端版本的BossRush映射
public class ClientRushEvent {
    private static boolean bossRushEventActivating = false;
    private static float screenShakePower;
    private static boolean screenShakeProcessed;
    private static float skyDarkenValue;
    private static float oldDarkenValue;
    private static SoundInstance playSounds;
    private static float dim;
    private static VoxelShape shape;

    @SuppressWarnings("ConstantConditions")
    public static void handlerDataPack(CompoundTag tag, IDataPackResponse parser) {
        if (tag.contains("power")) {
            float power = tag.getFloat("power");
            ClientRushEvent.setScreenShakePower(power);
            ClientRushEvent.setSkyDarkenGradient(power);
        } else if (tag.contains("onKiller")) {
            dim = Mth.lerp(tag.getFloat("progress"), 0.1f, 1f);
            BossRushSky.setCurrentInterest(Math.min(1f, BossRushSky.currentInterest + tag.getFloat("interest")));
        }

        if (tag.contains("state")) {
            switch (tag.getInt("state")) {
                case 0 -> ClientRushEvent.startEvent();
                case 1 -> ClientRushEvent.interruptEvent();
                case 2 -> ClientRushEvent.failureEvent();
                case 3 -> ClientRushEvent.victoryEvent();
            }
        }

        if (tag.contains("sound")) {
            String type = tag.getString("sound");
            if (type.equals("stop")) {
                if (playSounds != null) {
                    Minecraft.getInstance().getSoundManager().stop(playSounds);
                    playSounds = null;
                }
            } else {
                CalamitySounds sound = CalamitySounds.valueOf(tag.getString("sound"));
                Minecraft mc = Minecraft.getInstance();
                Vec3 player = mc.player.getEyePosition();
                playSounds = new SimpleSoundInstance(sound.get(), SoundSource.AMBIENT,
                    1f, 1f, RandomSource.create(mc.level.random.nextInt()), player.x, player.y, player.z);
                mc.getSoundManager().play(playSounds);
            }
        }

        if (tag.contains("border")) {
            WorldBorder border = Minecraft.getInstance().level.getWorldBorder();
            if (tag.getString("border").equals("default")) {
                WorldBorder.Settings settings = WorldBorder.DEFAULT_SETTINGS;
                border.calamity$BossRushBox(settings.getCenterX(), settings.getCenterZ(), settings.getSize());
                border.getStatus().color = switch (border.getStatus()) {
                    case GROWING -> 4259712;
                    case SHRINKING -> 16724016;
                    case STATIONARY -> 2138367;
                };
            } else {
                Vec3 position = parser.readVec3("border", tag);
                border.calamity$BossRushBox(position.x, position.y, position.z);
                border.getStatus().color = 0XFF00FF;
                //Make intersection box
                shape = Shapes.box(Math.floor(border.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(border.getMinZ()),
                    Math.ceil(border.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(border.getMaxZ()));
            }
        }
    }

    private static void victoryEvent() {
        playPhaseDialogue(2, ClientRushEvent::interruptEvent);
    }

    private static void failureEvent() {
    }

    public static boolean bossRushSkyDarken() {
        return skyDarkenValue > 0;
    }

    public static boolean isBossRushEventActivating() {
        return bossRushEventActivating;
    }

    public static VoxelShape getVenueShape() {
        return shape;
    }

    public static void interruptEvent() {
        dim = 0;
        skyDarkenValue = 0;
        oldDarkenValue = 0;
        BossRushSky.BLACK = null;
        if (playSounds != null) {
            Minecraft.getInstance().getSoundManager().stop(playSounds);
            playSounds = null;
        }

        if (bossRushEventActivating) { 
            DelayRunnable.removeTask(ClientRushEvent.class);
            RenderUtil.Shaders.setScreenFlashEffect(20, 0.85f);
            DelayRunnable.addRunTask(13, () -> {
                bossRushEventActivating = false;
                BossRushSky.currentInterest = 0f;
                BossRushSky.baseColorDraw = null;
                BossRushSky.coral = null;
                BossRushSky.XEROC_EYE = null;
            });
        }
    }

    public static float getSkyDarkenValue(float partialTick) {
        return bossRushEventActivating ? skyDarkenValue : Mth.lerp(partialTick, oldDarkenValue, skyDarkenValue);
    }

    public static void setScreenShakePower(float power) {
        screenShakeProcessed = false;
        screenShakePower = power;

    }

    @SuppressWarnings("ConstantConditions")
    public static void screenShakeHandle(ViewportEvent.ComputeCameraAngles event) {
        if (screenShakePower > 0) {
            Vector2d offset = Vector2d.nextVector2Circular(screenShakePower,
                screenShakePower, Minecraft.getInstance().level.random);
            event.setRoll(event.getRoll() + (float)  offset.x);
            event.setYaw(event.getYaw() + (float)  offset.y);
            if (!screenShakeProcessed) {
                screenShakeProcessed = true;
                DelayRunnable.addRunTask(3, () -> {
                    if (screenShakeProcessed) {
                        screenShakeProcessed = false;
                        screenShakePower = Mth.clamp(screenShakePower - 0.185f, 0f, 10f);
                    }
                });
            }
        }
    }

    public static void setSkyDarkenGradient(float power) {
        oldDarkenValue = skyDarkenValue;
        skyDarkenValue = (10 - power) * 0.1f;
        if (BossRushSky.BLACK == null) BossRushSky.BLACK = CalamityCurios.ModResource("textures/misc/black.png");
        dim = Mth.lerp(power / 4f, 0.57f, 1f);
    }

    public static void startEvent() {
        shape = Shapes.empty();
        bossRushEventActivating = true;
        int[] tick = {0};
        BossRushSky.currentInterest = 0.8f;
        BossRushSky.incrementalInterest = 0.8f;
        DelayRunnable.conditionsLoop( () -> {
            BossRushSky.setCurrentInterest(BossRushSky.currentInterest - 0.02f);
            if (++tick[0] > 20) {
                dim = Mth.lerp((tick[0] - 20) / 20f, 1f, 0.1f);
                if (tick[0] == 40) {
                    playPhaseDialogue(0, null);
                    return true;
                }
            }

            return !bossRushEventActivating;
        }, 1);

        RenderUtil.Shaders.setScreenFlashEffect(20, 0.85f);
        BossRushSky.baseColorDraw = new Vector4i();
        BossRushSky.coral = new Vector4i(255, 128, 79, 255);
        BossRushSky.XEROC_EYE = CalamityCurios.ModResource("textures/misc/xeroc_eye.png");
    }

    @SuppressWarnings("ConstantConditions")
    public static void playPhaseDialogue(int state, Runnable onDialogueEnd) {
        List<Component> texts = switch (state) {
            case 0 -> batchColorTexts(1, 15);
            case 1 -> batchColorTexts(16, 17);
            case 2 -> batchColorTexts(18, 25);
            default -> throw new IllegalStateException();
        };

        BooleanSupplier supplier = () -> {
            int last = texts.size() - 1;
            Minecraft.getInstance().player.displayClientMessage(texts.get(last), true);
            texts.remove(last);
            if (texts.isEmpty()) {
                if (onDialogueEnd != null)
                    onDialogueEnd.run();
                return true;
            }
            return false;
        };

        if (supplier.getAsBoolean()) return;
        if (!DelayRunnable.addUniqueLoopTask(supplier, 60, ClientRushEvent.class)) {
            DelayRunnable.removeTask(ClientRushEvent.class);
            DelayRunnable.addUniqueLoopTask(supplier, 60, ClientRushEvent.class);
        }
    }

    public static List<Component> batchColorTexts(int start, int end) {
        Style style = Style.EMPTY.withColor(0xFAD54D);
        List<Component> texts = new ArrayList<>();
        for (int i = end; i >= start; i--)
            texts.add(CMLangUtil.getTranslatable("boss_rush_state", i).setStyle(style));

        return texts;
    }

    @OnlyIn(Dist.CLIENT)
    public static class BossRushSky {
        @SuppressWarnings("ALL")
        private static ResourceLocation XEROC_EYE;
        private static ResourceLocation BLACK;
        private static float currentInterestMin;
        private static float currentInterest;
        private static float incrementalInterest;
        private static Vector4i baseColorDraw;
        private static Vector4i coral;

        private static void update() {
            setCurrentInterest(Mth.clamp(currentInterest - 0.01f, currentInterestMin, 1f));
        }

        private static void setCurrentInterest(float value) {
            incrementalInterest = currentInterest;
            currentInterest = value;
        }

        public static void setSkyFogColor(ViewportEvent.ComputeFogColor event) {
            event.setRed(Mth.lerp((float) Mth.lerp(event.getPartialTick(),
                incrementalInterest, currentInterest), 0.85f, 0.96f));
            event.setBlue(0.8275f);
            event.setGreen(0.8275f);
        }

        public static void applyFilter(Minecraft minecraft) {
            if (dim > 0) {
                RenderSystem.setShaderColor(0.75F, 0.75F, 0.75F,
                    Mth.lerp(dim, 0.1F, 0.3F));
                RenderSystem.setShaderTexture(0, BLACK);
                RenderUtil.Shaders.renderMask(minecraft);
            }
        }

        @SuppressWarnings("ConstantConditions")
        public static void renderSky(PoseStack poseStack, Minecraft minecraft, Matrix4f projectionMatrix,
                                     float partialTick, VertexBuffer skyBuffer, VertexBuffer darkBuffer, ClientLevel level) {
            float interest = Mth.lerp(partialTick, incrementalInterest, currentInterest);
            float skyColor = Mth.lerp(interest, 0.8275f, 0.96F);
            RenderSystem.depthMask(false);
            RenderSystem.setShaderColor(skyColor, skyColor, skyColor, 0.75f);
            ShaderInstance shaderinstance = RenderSystem.getShader();
            skyBuffer.bind();
            skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shaderinstance);
            VertexBuffer.unbind();
            RenderSystem.enableBlend();
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(60f));
            renderXerocEye(poseStack.last().pose(), interest);
            poseStack.popPose();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.disableBlend();

            RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
            if (minecraft.player.getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level) < 0) {
                poseStack.pushPose();
                poseStack.translate(0.0F, 12.0F, 0.0F);
                darkBuffer.bind();
                darkBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shaderinstance);
                VertexBuffer.unbind();
                poseStack.popPose();
            }

            RenderSystem.depthMask(true);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        //渲染克希洛克之眼
        public static void renderXerocEye(Matrix4f matrix4f, float interest) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            float size = Mth.lerp(interest, 25f, 30f) +
                (float) Math.sin(Mth.lerp(interest, 0.04f, 0.1f)) * 0.01f;
            RenderUtil.interpolateColor(RenderUtil.WHITE, RenderUtil.RED, interest, baseColorDraw);
            //Base
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            //不要被Mojang的着色器剔除，这会导致非常混乱的杂边
            RenderUtil.Shaders.BASE_SHARD.setupRenderState();
            RenderSystem.setShaderTexture(0, XEROC_EYE);

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            float zSize = size * 1.2f;
            bufferBuilder.vertex(matrix4f, -size, 100.0F, -zSize).color(baseColorDraw.x, baseColorDraw.y,
                baseColorDraw.z, 84).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix4f, size, 100.0F, -zSize).color(baseColorDraw.x, baseColorDraw.y,
                baseColorDraw.z, 84).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix4f, size, 100.0F, zSize).color(baseColorDraw.x, baseColorDraw.y,
                baseColorDraw.z, 84).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix4f, -size, 100.0F, zSize).color(baseColorDraw.x, baseColorDraw.y,
                baseColorDraw.z, 84).uv(0.0F, 1.0F).endVertex();
            BufferUploader.drawWithShader(bufferBuilder.end());


            RenderUtil.multiplyColor(RenderUtil.interpolateColor(baseColorDraw, coral, 0.35f, baseColorDraw),
                Mth.lerp(interest, 0.12f, 0.24f), baseColorDraw);
            float backEyeOutwardness = Mth.lerp(interest, 0.6f, 0.32f);
            int backInstances = Mth.lerpInt(interest, 6, 14);
            float fourPi = Mth.TWO_PI * 2f;
            float time = RenderUtil.getLocalTick() * 0.1f;
            //Glow
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            for (int i = 0; i < backInstances; i++) {
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                Vector2d vector2d = Vector2d.toRotationVector2(fourPi * i / backInstances + time).mul(backEyeOutwardness);
                matrix4f.translate((float) vector2d.x, 0, (float) vector2d.y);
                bufferBuilder.vertex(matrix4f, -size, 100.0F, -zSize).color(baseColorDraw.x, baseColorDraw.y,
                    baseColorDraw.z, 0).uv(0.0F, 0.0F).endVertex();
                bufferBuilder.vertex(matrix4f, size, 100.0F, -zSize).color(baseColorDraw.x, baseColorDraw.y,
                    baseColorDraw.z, 0).uv(1.0F, 0.0F).endVertex();
                bufferBuilder.vertex(matrix4f, size, 100.0F, zSize).color(baseColorDraw.x, baseColorDraw.y,
                    baseColorDraw.z, 0).uv(1.0F, 1.0F).endVertex();
                bufferBuilder.vertex(matrix4f, -size, 100.0F, zSize).color(baseColorDraw.x, baseColorDraw.y,
                    baseColorDraw.z, 0).uv(0.0F, 1.0F).endVertex();
                BufferUploader.drawWithShader(bufferBuilder.end());
                matrix4f.translate((float) -vector2d.x, 0, (float) -vector2d.y);
            }
        }
    }
}
