package hua223.calamity.register.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.C2SPacket.CurseEnchantmentPack;
import hua223.calamity.net.NetMessages;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentButton extends AbstractWidget {
    private static final ResourceLocation BUTTON = CalamityCurios.ModResource("textures/gui/calamity_curse_button.png");
    private static final ResourceLocation HOVER = CalamityCurios.ModResource("textures/gui/button_hovered.png");
    private static final ResourceLocation CLICK = CalamityCurios.ModResource("textures/gui/button_clicked.png");
    private static boolean buttonClick;
    private final CalamityCurseScreen screen;

    public EnchantmentButton(int x, int y, int width, int height, CalamityCurseScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        ResourceLocation t;
        if (buttonClick) t = CLICK;
        else t = isHovered ? HOVER : BUTTON;
        RenderSystem.setShaderTexture(0, t);

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0);
        blit(poseStack, x, y, 0, 0, width, height, width, height);
        poseStack.popPose();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.isHovered = clicked(mouseX, mouseY);
        this.renderButton(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        if (!screen.canRenderContent) return;

        buttonClick = true;
        NetMessages.sendToServer(new CurseEnchantmentPack());
        DelayRunnable.addRunTask(3, () -> buttonClick = false);
    }

    @Override
    public boolean clicked(double mouseX, double mouseY) {
        return mouseX >= 182 && mouseY >= 36 && mouseX < 194 && mouseY < 49;
    }
}
