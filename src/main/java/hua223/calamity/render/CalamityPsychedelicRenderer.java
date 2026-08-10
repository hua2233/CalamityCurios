package hua223.calamity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.events.LogoutRelease;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class CalamityPsychedelicRenderer {
    private int color;
    private final String rawShader;
    private boolean notTrippyRender = true;
    private final Minecraft minecraft = Minecraft.getInstance();

    private static CalamityPsychedelicRenderer renderer;
    private static final String HEAT_DISTORTION_SHADER = "calamity_curios:shaders/post/heat_distortion.json";

    public CalamityPsychedelicRenderer() {
        if (renderer == null) IllusionBufferSource.create();

        renderer = this;
        color = RenderUtil.getRainbowStyle();
        PostChain shader = renderer.minecraft.gameRenderer.currentEffect();
        rawShader = shader != null ? shader.getName() : null;
        if (!HEAT_DISTORTION_SHADER.equals(rawShader))
            renderer.minecraft.gameRenderer.loadEffect(CalamityCurios.resource(HEAT_DISTORTION_SHADER));
    }

    @LogoutRelease
    public static void stop(@Nullable LocalPlayer player) {
        if (renderer != null) {
            PostChain shader = renderer.minecraft.gameRenderer.currentEffect();
            if (shader != null && shader.getName().equals(HEAT_DISTORTION_SHADER)) {
                if (renderer.rawShader == null) renderer.minecraft.gameRenderer.shutdownEffect();
                else renderer.minecraft.gameRenderer.loadEffect(CalamityCurios.resource(renderer.rawShader));
            }
            IllusionBufferSource.destroy();
            renderer = null;
        }
    }

    @SuppressWarnings("unchecked")
    public static void psychedelic(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        if (renderer != null && renderer.notTrippyRender) {
            event.setCanceled(true);
            MultiBufferSource buffer = IllusionBufferSource.getSource(event.getMultiBufferSource());
            PoseStack pose = event.getPoseStack();
            LivingEntityRenderer<LivingEntity, ? extends EntityModel<? extends LivingEntity>> eventRenderer =
                (LivingEntityRenderer<LivingEntity, ? extends EntityModel<? extends LivingEntity>>) event.getRenderer();
            float partialTicks = event.getPartialTick();
            int packedLight = event.getPackedLight();
            LivingEntity entity = event.getEntity();
            renderer.notTrippyRender = false;
            float time = entity.tickCount * 0.08F;
            float tick = RenderUtil.getLocalTick();

            for (int i = 0; i < 3; i++) {
                pose.pushPose();
                pose.translate(Math.cos(tick * 0.3) * (Math.sin(time + i) * 2),
                    0, Math.sin(tick * 0.3) * (Math.cos(time + i) * 2));
                eventRenderer.render(entity, entity.getYRot(), partialTicks, pose, buffer, packedLight);
                pose.popPose();
            }

            renderer.notTrippyRender = true;

            if (RenderUtil.getRainbowStyle() != renderer.color) {
                renderer.color = RenderUtil.getRainbowStyle();
                IllusionBufferSource.setColor(
                    FastColor.ARGB32.red(renderer.color),
                    FastColor.ARGB32.green(renderer.color),
                    FastColor.ARGB32.blue(renderer.color), 255);
            }
        }
    }
}
