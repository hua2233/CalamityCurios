package hua223.calamity.register.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.C2SPacket.SpellTypeSync;
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
public class CalamityCurseArrow extends AbstractWidget {
    private final ResourceLocation textures;
    private final short minY;
    private final short maxY;
    private final boolean isDown;
    private final CalamityCurseMenu menu;
    public boolean notEnable;
    private boolean buttonClick;
    private CalamityCurseArrow key;

    public CalamityCurseArrow(int x, int y, boolean isDown, CalamityCurseMenu menu) {
        super(x, y, 42, 10, Component.empty());
        if (isDown) {
            textures = CalamityCurios.ModResource("textures/gui/calamity_curse_arrow_down.png");
            ;
            minY = 50;
            maxY = 56;
        } else {
            textures = CalamityCurios.ModResource("textures/gui/calamity_curse_arrow_up.png");
            minY = 30;
            maxY = 36;
            notEnable = true;
        }

        this.isDown = isDown;
        this.menu = menu;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (notEnable) return;
        this.isHovered = clicked(mouseX, mouseY);
        this.renderButton(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, textures);
        int index;
        if (buttonClick) index = 2;
        else index = isHovered ? 1 : 0;

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0);
        blit(poseStack, x, y, index * 14, 0, 14, height, width, height);
        poseStack.popPose();
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (notEnable) return;

        buttonClick = true;
        DelayRunnable.addRunTask(3, () -> buttonClick = false);

        menu.type = isDown ? SpellType.nextSpell() : SpellType.previousSpell();
        NetMessages.sendToServer(new SpellTypeSync(menu.type.name()));
        key.notEnable = false;
        if (SpellType.isBoundary(isDown)) notEnable = true;
    }

    public void setCorrespondsKey(CalamityCurseArrow key) {
        this.key = key;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return mouseX >= 245 && mouseY >= minY && mouseX < 251 && mouseY < maxY;
    }
}
