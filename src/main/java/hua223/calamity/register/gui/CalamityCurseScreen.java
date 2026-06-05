package hua223.calamity.register.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CalamityCurseScreen extends AbstractContainerScreen<CalamityCurseMenu> {
    private static TextureAtlasSprite BG;
    private static TextureAtlasSprite INVENTORY;
    private final CalamityCurseArrow[] buttons = new CalamityCurseArrow[2];
    public boolean canRenderContent;

    public CalamityCurseScreen(CalamityCurseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 392;
        this.imageHeight = 324;
        menu.screen = this;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        PoseStack stack = guiGraphics.pose();

        stack.pushPose();
        stack.scale(0.5f, 0.5f, 0);
        guiGraphics.blit(280, 10, 0, imageWidth, imageHeight, BG);
        stack.popPose();

        stack.pushPose();
        guiGraphics.blit(151, 167, 0, 176, 88, INVENTORY);
        stack.popPose();

        if (canRenderContent) {
            SpellType type = menu.type;
            int color = 0xFFFF00;

            stack.pushPose();
            Component t = type.getTypeComponent();
            float xPos = (90f - font.width(t)) / 2;
            guiGraphics.drawString(font, t.getString(), (int) (203 + xPos), 38, color);

            int yPos = 75;
            int lineHeight = font.lineHeight;
            guiGraphics.drawString(font, "咒引:", 162, yPos, color);

            yPos = 90;
            List<FormattedCharSequence> lines = font.split(type.getDescriptionComponent(), 150);
            for (FormattedCharSequence line : lines) {
                guiGraphics.drawString(font, line, 162, yPos, color);
                yPos += lineHeight;
            }
            stack.popPose();

            stack.pushPose();
            stack.scale(0.5f, 0.5f, 0);
            guiGraphics.blit(602, 68, 0, 40, 40, type.getTexture());
            stack.popPose();
            renderItem(guiGraphics, stack, mouseX, mouseY);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new EnchantmentButton(364, 70, 26, 28, this));
        createArrowButton();
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    private void createArrowButton() {
        CalamityCurseArrow down = new CalamityCurseArrow(490, 100, true, menu);
        CalamityCurseArrow up = new CalamityCurseArrow(490, 61, false, menu);
        down.setCorrespondsKey(up);
        up.setCorrespondsKey(down);

        buttons[0] = up;
        buttons[1] = down;

        addRenderableWidget(down);
        addRenderableWidget(up);
    }

    public void notEnableButtonState() {
        buttons[0].notEnable = true;
        buttons[1].notEnable = true;
    }

    public void initButtonState() {
        buttons[0].notEnable = true;
        buttons[1].notEnable = false;
    }

    private void renderItem(GuiGraphics guiGraphics, PoseStack pose, int mouseX, int mouseY) {
        ItemStack[] stacks = menu.spend;
        pose.pushPose();
        pose.translate(185, 70, 0);

        boolean renderText = mouseY > 70 && mouseY < 86;
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            int xOffset = 20 * i;
            guiGraphics.renderItem(stack, xOffset, 0);

            if (renderText && mouseX > 202 + xOffset - 18 && mouseX < 202 + xOffset - 2)
                guiGraphics.renderComponentTooltip(font, List.of(stack.getItem().getName(stack)), xOffset - 18, 9, stack);

            pose.translate(0, 0, 200);
            String count = String.valueOf(stack.getCount());
            guiGraphics.drawString(font, count, xOffset + 17 -
                font.width(count), 9, menu.enough[i] ? 5636095 : 11141120, true);
        }
        pose.popPose();
    }

    public static void afterMainTextureLoad(TextureAtlas atlas) {
        BG = atlas.getSprite(CalamityCurios.ModResource("calamity_curse_background"));
        INVENTORY = atlas.getSprite(CalamityCurios.ModResource("inventory"));
        EnchantmentButton.afterMainTextureLoad(atlas);
        CalamityCurseArrow.afterMainTextureLoad(atlas);
    }
}
