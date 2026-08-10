package hua223.calamity.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.register.entity.ColorfulLightningBolt;
import hua223.calamity.render.IllusionBufferSource;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ColorfulLightningBoltRenderer extends EntityRenderer<ColorfulLightningBolt> {
    private LightningBoltRenderer renderer;

    public ColorfulLightningBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
        //Waiting for resource loading to complete
        DelayRunnable.currentTickEndRun(() ->
            renderer = (LightningBoltRenderer) context.getEntityRenderDispatcher().renderers.get(EntityType.LIGHTNING_BOLT));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ColorfulLightningBolt colorfulLightningBolt) {
        return renderer.getTextureLocation(colorfulLightningBolt);
    }

    @Override
    public void render(@NotNull ColorfulLightningBolt entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        entity.applyColor();
        renderer.render(entity, entityYaw, partialTick, poseStack, IllusionBufferSource.getSource(buffer), packedLight);
    }
}
