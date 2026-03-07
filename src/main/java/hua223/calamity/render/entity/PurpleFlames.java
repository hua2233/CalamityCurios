package hua223.calamity.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;

@OnlyIn(Dist.CLIENT)
public class PurpleFlames {
    private static final Material FLAMES_0 = new Material(InventoryMenu.BLOCK_ATLAS, CalamityCurios.ModResource("block/fire_0"));
    private static final Material FLAMES_1 = new Material(InventoryMenu.BLOCK_ATLAS, CalamityCurios.ModResource("block/fire_1"));

    @SuppressWarnings("resource")
    public static void renderFlame(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (CalamityHelp.getCalamityFlag(entity, 5)) {
            PoseStack matrixStack = event.getPoseStack();
            MultiBufferSource buffer = event.getMultiBufferSource();
            //This is a trap that should not be released, or rather it implements the AutoCloseable interface and is not meant to be released for you.
            //It is scheduled by the native rendering system. If forcibly released due to a warning, some inexplicable errors may occur
            //try (TextureAtlasSprite sprite = FLAMES_0.sprite(); TextureAtlasSprite sprite1 = FLAMES_1.sprite()) {}
            TextureAtlasSprite sprite = FLAMES_0.sprite();
            TextureAtlasSprite sprite1 = FLAMES_1.sprite();
            matrixStack.pushPose();
            float f = entity.getBbWidth() * 1.4F;
            matrixStack.scale(f, f, f);
            float f1 = 0.5F;
            float f3 = entity.getBbHeight() / f;
            float f4 = 0.0F;
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));
            matrixStack.translate(0.0D, 0.0D, -0.3F + (float) ((int) f3) * 0.02F);
            float f5 = 0.0F;
            int i = 0;
            VertexConsumer vertexconsumer = buffer.getBuffer(Sheets.cutoutBlockSheet());

            for (PoseStack.Pose pose = matrixStack.last(); f3 > 0.0F; ++i) {
                TextureAtlasSprite sprite2 = i % 2 == 0 ? sprite : sprite1;
                float f6 = sprite2.getU0();
                float f7 = sprite2.getV0();
                float f8 = sprite2.getU1();
                float f9 = sprite2.getV1();
                if (i / 2 % 2 == 0) {
                    float f10 = f8;
                    f8 = f6;
                    f6 = f10;
                }

                fireVertex(pose, vertexconsumer, f1 - 0.0F, 0.0F - f4, f5, f8, f9);
                fireVertex(pose, vertexconsumer, -f1 - 0.0F, 0.0F - f4, f5, f6, f9);
                fireVertex(pose, vertexconsumer, -f1 - 0.0F, 1.4F - f4, f5, f6, f7);
                fireVertex(pose, vertexconsumer, f1 - 0.0F, 1.4F - f4, f5, f8, f7);
                f3 -= 0.45F;
                f4 -= 0.45F;
                f1 *= 0.9F;
                f5 += 0.03F;
            }
            matrixStack.popPose();
        }
    }

    private static void fireVertex(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, float pX, float pY, float pZ, float pTexU, float pTexV) {
        pBuffer.vertex(pMatrixEntry.pose(), pX, pY, pZ).color(188, 40, 201, 255).uv(pTexU, pTexV)
            .overlayCoords(0, 10).uv2(240).normal(pMatrixEntry.normal(), 0.0F, 1.0F, 0.0F).endVertex();
    }

    public static void applySprite(TextureStitchEvent.Pre event) {
        event.addSprite(FLAMES_0.texture());
        event.addSprite(FLAMES_1.texture());
    }
}
