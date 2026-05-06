package hua223.calamity.register.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.C2SPacket.CurseEnchantmentPack;
import hua223.calamity.net.NetMessages;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class EnchantmentButton extends AbstractWidget {
    private static TextureAtlasSprite BUTTON;
    private static TextureAtlasSprite HOVER;
    private static TextureAtlasSprite CLICK;

    private static boolean buttonClick;
    private final CalamityCurseScreen screen;

    public EnchantmentButton(int x, int y, int width, int height, CalamityCurseScreen screen) {
        super(x, y, width, height, Component.empty());
        this.screen = screen;

    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.isHovered = clicked(mouseX, mouseY);
        this.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        TextureAtlasSprite sprite;
        if (buttonClick) sprite = CLICK;
        else sprite = isHovered ? HOVER : BUTTON;

        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        stack.scale(0.5f, 0.5f, 0);
        guiGraphics.blit(getX(), getY(), 0, width, height, sprite);
        stack.popPose();
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

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        BUTTON = atlas.getSprite(CalamityCurios.ModResource("calamity_curse_button"));
        HOVER = atlas.getSprite(CalamityCurios.ModResource("button_hovered"));
        CLICK = atlas.getSprite(CalamityCurios.ModResource("button_clicked"));
    }
}
