package hua223.calamity.register.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CalamityCurseScreen extends AbstractContainerScreen<CalamityCurseMenu> {
    private static final ResourceLocation BG = CalamityCurios.ModResource("textures/gui/calamity_curse_background.png");
    private static final ResourceLocation INVENTORY = CalamityCurios.ModResource("textures/gui/inventory.png");
    private final CalamityCurseArrow[] buttons = new CalamityCurseArrow[2];
    public boolean canRenderContent;

    public CalamityCurseScreen(CalamityCurseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 392;
        this.imageHeight = 324;
        menu.screen = this;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BG);

        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0);
        blit(poseStack, 280, 10, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        poseStack.popPose();

        poseStack.pushPose();
        RenderSystem.setShaderTexture(0, INVENTORY);
        blit(poseStack, 151, 167, 0, 0, 176, 88, 176, 88);
        poseStack.popPose();

        if (canRenderContent) {
            SpellType type = menu.type;
            int color = 0xFFFF00;

            poseStack.pushPose();
            float xPos = (90f - font.width(type.type)) / 2;
            this.font.draw(poseStack, type.type, 203 + xPos, 38, color);

            int yPos = 75;
            int lineHeight = font.lineHeight;
            font.draw(poseStack, "咒引:", 162, yPos, color);

            yPos = 90;
            List<FormattedCharSequence> lines = this.font.split(type.description, 150);
            for (FormattedCharSequence line : lines) {
                this.font.draw(poseStack, line, 162, yPos, color);
                yPos += lineHeight;
            }
            poseStack.popPose();

            poseStack.pushPose();
            RenderSystem.setShaderTexture(0, type.texture);
            poseStack.scale(0.5f, 0.5f, 0);
            blit(poseStack, 602, 68, 0, 0, 40, 40, 40, 40);
            renderItem();
            poseStack.popPose();
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new EnchantmentButton(364, 70, 26, 28, this));
        createArrowButton();
    }

    private void createArrowButton() {
        CalamityCurseArrow down = new CalamityCurseArrow(490, 100, true, menu);
        CalamityCurseArrow up = new CalamityCurseArrow(490, 61, false, menu);
        down.setCorrespondsKey(up);
        up.setCorrespondsKey(down);

        buttons[0] = up;
        buttons[1] = down;

        this.addRenderableWidget(down);
        this.addRenderableWidget(up);
    }

    public void notEnableButtonState() {
        buttons[0].notEnable = true;
        buttons[1].notEnable = true;
    }

    public void initButtonState() {
        buttons[0].notEnable = true;
        buttons[1].notEnable = false;
    }

    private void renderItem() {
        int x = 185;
        int y = 70;
        int yOffset = 72;
        ItemStack[] stacks = menu.spend;

        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            int xOffset = x + 20 * i;
            itemRenderer.renderGuiItem(stack, xOffset, y);
            itemRenderer.renderGuiItemDecorations(font, stack, xOffset + 2, yOffset, null);
        }
    }
}
