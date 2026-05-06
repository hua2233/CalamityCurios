package hua223.calamity.register.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.C2SPacket.SpellTypeSync;
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
public class CalamityCurseArrow extends AbstractWidget {
    private static TextureAtlasSprite down;
    private static TextureAtlasSprite up;
    private static float frame;
    private final TextureAtlasSprite textures;
    private final short minY;
    private final short maxY;
    private final boolean isDown;
    private final CalamityCurseMenu menu;
    public boolean notEnable;
    private boolean buttonClick;
    private CalamityCurseArrow key;

    public CalamityCurseArrow(int x, int y, boolean isDown, CalamityCurseMenu menu) {
        super(x, y, 14, 10, Component.empty());
        if (isDown) {
            textures = down;
            minY = 50;
            maxY = 56;
        } else {
            textures = up;
            minY = 30;
            maxY = 36;
            notEnable = true;
        }

        this.isDown = isDown;
        this.menu = menu;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (notEnable) return;
        this.isHovered = clicked(mouseX, mouseY);
        this.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int x, int y, float v) {
        PoseStack pose = guiGraphics.pose();
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        float frameTexture = textures.getU0() + (frame * (buttonClick ? 2f : isHovered ? 1 : 0));

        pose.pushPose();
        pose.scale(0.5f, 0.5f, 0);
        guiGraphics.innerBlit(textures.atlasLocation(), getX(), getX() + width, getY(), getY() + height, 0,
            frameTexture, frameTexture + frame, textures.getV0(), textures.getV1());
        pose.popPose();

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (notEnable || buttonClick) return;

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

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        down = atlas.getSprite(CalamityCurios.ModResource("calamity_curse_arrow_down"));
        up = atlas.getSprite(CalamityCurios.ModResource("calamity_curse_arrow_up"));
        frame = (down.getU1() - down.getU0()) / 3;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return mouseX >= 245 && mouseY >= minY && mouseX < 251 && mouseY < maxY;
    }
}
