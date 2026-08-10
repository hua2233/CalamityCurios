package hua223.calamity.events.levelevent.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.screen.FlashScreenRenderer;
import hua223.calamity.render.screen.ScreenMaskRenderer;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.joml.Vector4i;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@OnlyIn(Dist.CLIENT)
public class ClientRushEvent extends ClientLevelEvent {
    private final BossRushSoundManager manager = new BossRushSoundManager(this);
    private final ScreenMaskRenderer mask = new ScreenMaskRenderer(CalamityCurios.ModResource("textures/misc/black.png"));

    private int tier = -1;
    private BossRushSky sky;
    protected boolean pre = true;

    public ClientRushEvent(CompoundTag tag, IDataPackResponse parser) {
        handlerDataPack(tag, parser);
        mask.setSingleChannel(3, .125f);
    }

    @SuppressWarnings("ConstantConditions")
    public void handlerDataPack(CompoundTag tag, IDataPackResponse parser) {
        if (tag.contains("power")) {
            float power = tag.getFloat("power");
            setScreenShakePower(power);
            setSkyDarkenGradient(power);
        } else if (tag.contains("progress")) {
            float progress = tag.getFloat("progress");
            mask.setSingleChannel(3, Mth.lerp(progress, .1f, .3f));
            setTier(1 + (int) (progress / .25f));
            sky.setCurrentInterest(Math.min(1f, sky.currentInterest + tag.getFloat("interest")));
        }

        if (tag.contains("state")) {
            switch (tag.getInt("state")) {
                case 0 -> startEvent();
                case 1 -> interruptEvent();
                case 2 -> failureEvent();
                case 3 -> victoryEvent();
            }
        }

        if (tag.contains("sound")) manager.fromDataPlaySound(tag);

        if (tag.contains("border")) {
            WorldBorder border = Minecraft.getInstance().level.getWorldBorder();
            if (tag.getString("border").equals("default")) {
                WorldBorder.Settings settings = WorldBorder.DEFAULT_SETTINGS;
                border.calamity$BossRushBox(settings.getCenterX(), settings.getCenterZ(), settings.getSize());
                border.calamity$Shape = null;
                borderRenderDistance = 0;
                border.getStatus().color = switch (border.getStatus()) {
                    case GROWING -> 4259712;
                    case SHRINKING -> 16724016;
                    case STATIONARY -> 2138367;
                };
            } else {
                borderRenderDistance = 1;
                Vec3 position = parser.readVec3("border", tag);
                border.calamity$BossRushBox(position.x, position.y, position.z);
                border.getStatus().color = 0XFF00FF;
                //Make intersection box
                border.calamity$Shape = Shapes.box(Math.floor(border.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(border.getMinZ()),
                    Math.ceil(border.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(border.getMaxZ()));
            }
        }
    }

    protected void setSkyDarkenGradient(float power) {
        oldDarkenValue = skyDarkenValue;
        skyDarkenValue = (10 - power) * 0.1f;
        mask.setSingleChannel(3, Mth.lerp(power / 4f, .125f, .3f));
    }

    @SubscribeEvent
    public final void screenShake(ViewportEvent.ComputeCameraAngles event) {
        screenShakeHandle(event);
    }

    @Override
    public float getSkyDarkenValue(float partialTick) {
        return pre ? Mth.lerp(partialTick, oldDarkenValue, skyDarkenValue) : skyDarkenValue;
    }

    @Override
    protected void victoryEvent() {
        playPhaseDialogue(5, false);
    }

    @Override
    public void interruptEvent() {
        manager.stopInPlaySound();
        if (pre) super.interruptEvent();
        else {
            new FlashScreenRenderer(20, 0.85f);
            DelayRunnable.addRunTask(13, super::interruptEvent);
        }

        mask.stop();
        stop = true;
    }

    public void startEvent() {
        pre = false;
        int[] tick = {0};
        new FlashScreenRenderer(20, 0.85f);
        sky = new BossRushSky();
        render = sky;
        sky.currentInterest = 0.8f;
        sky.incrementalInterest = 0.8f;
        DelayRunnable.conditionsLoop(() -> {
            sky.incrementalInterest = sky.currentInterest;
            sky.currentInterest -= 0.02f;
            if (++tick[0] > 20) {
                mask.setSingleChannel(3, Mth.lerp((tick[0] - 20) / 20f, .3f, .1f));
                if (tick[0] == 40) {
                    setTier(0);
                    return true;
                }
            }

            return stop;
        }, 1);
    }

    protected void setTier(int tier) {
        if (tier > this.tier) {
            this.tier = tier;
            playPhaseDialogue(tier, true);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void playPhaseDialogue(int state, final boolean nextMusic) {
        List<Component> texts = switch (state) {
            case 0 -> batchColorTexts(1, 15);
            case 1 -> batchColorTexts(16, 17);
            case 2 -> batchColorTexts(28, 29);
            case 3 -> batchColorTexts(30, 31);
            case 4 -> batchColorTexts(32, 34);
            case 5 -> batchColorTexts(18, 25);
            default -> throw new IllegalStateException();
        };

        BooleanSupplier supplier = () -> {
            if (stop) return true;
            int last = texts.size() - 1;
            Minecraft.getInstance().player.displayClientMessage(texts.get(last), true);
            texts.remove(last);
            if (texts.isEmpty()) {
                if (nextMusic) manager.startPlayBackgroundMusic();
                return true;
            }
            return false;
        };

        manager.fadeOutMusic();
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
    public class BossRushSky extends EventSkyRender {
        @SuppressWarnings("ALL")
        private float currentInterestMin;
        private float currentInterest;
        private float incrementalInterest;
        private final ResourceLocation XEROC_EYE = CalamityCurios.ModResource("textures/misc/xeroc_eye.png");
        private final Vector4i baseColorDraw = new Vector4i();
        private final Vector4i coral = new Vector4i(255, 128, 79, 255);
        private final Vector4i red = RenderUtil.fromColorGet(Color.RED);
        private final Vector4i white = RenderUtil.fromColorGet(Color.WHITE);

        private void setCurrentInterest(float value) {
            incrementalInterest = currentInterest;
            currentInterest += value;
            DelayRunnable.addUniqueLoopTask(() -> {
                incrementalInterest = currentInterest;
                currentInterest = Mth.clamp(currentInterest - 0.01f, currentInterestMin, 1f);
                return isStop() || incrementalInterest == 0;
            }, 1, BossRushSky.class);
        }

        @Override
        public void setSkyFogColor(ViewportEvent.ComputeFogColor event) {
            event.setRed(Mth.lerp((float) Mth.lerp(event.getPartialTick(),
                incrementalInterest, currentInterest), 0.85f, 0.96f));
            event.setBlue(0.8275f);
            event.setGreen(0.8275f);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void renderSky(PoseStack poseStack, Minecraft minecraft, Matrix4f projectionMatrix,
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
        private void renderXerocEye(Matrix4f matrix4f, float interest) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            float size = Mth.lerp(interest, 25f, 30f) +
                (float) Math.sin(Mth.lerp(interest, 0.04f, 0.1f)) * 0.01f;
            RenderUtil.interpolateColor(white, red, interest, baseColorDraw);
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
                Vector2f vector2F = Vector2f.toRotationVector2(fourPi * i / backInstances + time).mul(backEyeOutwardness);
                matrix4f.translate(vector2F.x, 0, vector2F.y);
                bufferBuilder.vertex(matrix4f, -size, 100.0F, -zSize).color(baseColorDraw.x, baseColorDraw.y,
                    baseColorDraw.z, 0).uv(0.0F, 0.0F).endVertex();
                bufferBuilder.vertex(matrix4f, size, 100.0F, -zSize).color(baseColorDraw.x, baseColorDraw.y,
                    baseColorDraw.z, 0).uv(1.0F, 0.0F).endVertex();
                bufferBuilder.vertex(matrix4f, size, 100.0F, zSize).color(baseColorDraw.x, baseColorDraw.y,
                    baseColorDraw.z, 0).uv(1.0F, 1.0F).endVertex();
                bufferBuilder.vertex(matrix4f, -size, 100.0F, zSize).color(baseColorDraw.x, baseColorDraw.y,
                    baseColorDraw.z, 0).uv(0.0F, 1.0F).endVertex();
                BufferUploader.drawWithShader(bufferBuilder.end());
                matrix4f.translate(-vector2F.x, 0, -vector2F.y);
            }
        }
    }
}
