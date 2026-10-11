package hua223.calamity.register.gui;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.packets.HyperplaneMatrixOperation;
import hua223.calamity.register.sounds.CalamitySounds;
import hua223.calamity.render.CircleBuffer;
import hua223.calamity.render.primitive.PrimitiveRenderer;
import hua223.calamity.render.primitive.PrimitiveSettings;
import hua223.calamity.render.primitive.SamplingPointMode;
import hua223.calamity.render.shaders.CalamityShaders;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.Vector2f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4i;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class HyperplaneMatrixOperatingInterface extends Screen {
    private OptionsUI[] optionsUIS;
    private float hologramRayWidth;
    protected float maxHologramWidth;
    protected float maxHologramHeight;
    private Vector2f rayStartingPoint;
    private float lastHologramRayWidth;
    private float hologramRayBrightness;
    private int[] initializationUISettings;
    private final HyperplaneMatrixSettings settings = new HyperplaneMatrixSettings();
    private final CircleBuffer<Vector2f> lightBand = CircleBuffer.fillFull(13, Vector2f::new);

    public HyperplaneMatrixOperatingInterface(int[] initializationSettings) {
        super(Component.translatable("menu.hyperplane_matrix_operating_interface"));
        CalamitySounds.HYPERPLANE_MATRIX_OPEN.playLocalSound();
        initializationUISettings = initializationSettings;
    }

    @Override
    protected void init() {
        rayStartingPoint = new Vector2f(width / 2f, height);
        maxHologramHeight = height * 1.1f;
        maxHologramWidth = width * .47f;
    }

    @Override
    //Update GameTime to enable texture scrolling
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        settings.partialTick = partialTick;
        settings.source = guiGraphics.bufferSource();
        PrimitiveRenderer.renderTrail(lightBand, settings, 99999999 /* 哎嘿 */, guiGraphics.pose().last().pose());
        if (optionsUIS != null) {
            Component component = null;
            for (OptionsUI optionsUI : optionsUIS) {
                optionsUI.render(guiGraphics, mouseX, mouseY, partialTick);
                if (component == null && optionsUI.isHovered())
                    component = optionsUI.getText();
            }

            if (component != null) guiGraphics.drawCenteredString(font, component, width / 2, (int) (height * 0.07f), 0xFFFFFFFF);
        }
    }

    @Override
    public void tick() {
        //prepareHologram
        //Make the hologram appear.
        if (hologramRayBrightness < 1) {
            hologramRayBrightness = Mth.clamp(hologramRayBrightness + 0.03f, 0f, 1f);
            float length = hologramRayBrightness * maxHologramHeight;
            int[] index = {0};
            lightBand.forAll(vector2f -> vector2f.set(rayStartingPoint.x, Mth.lerp(index[0]++ / 12f,rayStartingPoint.y - length, rayStartingPoint.y)));
        }

        if (lastHologramRayWidth < maxHologramWidth) {
            lastHologramRayWidth = hologramRayWidth;
            hologramRayWidth = Mth.clamp(hologramRayWidth + 4f, 1f, maxHologramWidth);

            if (lastHologramRayWidth == maxHologramWidth) {
                Vector2f left = new Vector2f();
                Vector2f right = new Vector2f(width * 0.7f, height * .25f);

                optionsUIS = new OptionsUI[6];
                int uiSize = Math.max((int) (width * 0.05f), (int) (height * 0.05f));
                for (int i = 0; i < optionsUIS.length; i++) {
                    left.set(width * 0.3f, height * .25f);
                    float indexCompletion = i / 5f;
                    left.lerp(right, indexCompletion);
                    float offset = Mth.sin(indexCompletion * Mth.PI) * (height * .08f);
                    optionsUIS[i] = addRenderableWidget(new OptionsUI((int) left.x, (int) (left.y - offset), initializationUISettings[i],i + 1, uiSize));
                }

                initializationUISettings = null;
            }
        }

        if (optionsUIS != null)
            for (OptionsUI optionsUI : optionsUIS)
                optionsUI.tick();
    }

    public static class OptionsUI extends AbstractWidget {
        private final int id;
        private int switchId;
        private int cooldown;
        private float scale = 1;
        private float hoveredScale;
        private float maxHoveredScale;
        private float lastHoveredScale;
        private static TextureAtlasSprite[] textures;

        public OptionsUI(int x, int y, int settings, int id, int size) {
            super(x, y, size, size, Component.empty());
            this.id = id;
            switchId = settings;
        }

        protected void tick() {
            if (isHovered) {
                if (maxHoveredScale < .3f) maxHoveredScale = .3f;
                lastHoveredScale = hoveredScale;
                if (lastHoveredScale != maxHoveredScale)
                    hoveredScale = Mth.clamp(hoveredScale + .05f, 0, maxHoveredScale);
            } else if (maxHoveredScale > 0) {
                lastHoveredScale = hoveredScale;
                if (lastHoveredScale != 0f) hoveredScale = Mth.clamp(hoveredScale - .05f, 0f, maxHoveredScale);
                else maxHoveredScale = 0;
            }

            if (cooldown > 0) cooldown--;
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (maxHoveredScale > 0) scale = 1f + Mth.lerp(partialTick, lastHoveredScale, hoveredScale);
            isHovered = hovered(mouseX, mouseY);
            renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }

        public Component getText() {
            String base = "menu.hyperplane_matrix_operating_interface.";
            MutableComponent component = Component.translatable(base + "operating" + id);
            if (id > 5 || id < 3) component.append(Component.translatable(base + (switchId < 0 ? "off" : "on")));
            return component.withStyle(ChatFormatting.GOLD);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int i1, float v) {
            float scale = cooldown == 0 ? this.scale : .8f;
            float w = width * scale;
            float h = height * scale;
            float bgW = w * 1.2f;
            float bgH = h * 1.2f;
            guiGraphics.blit(getX() - (int) (bgW / 2f), getY() - (int) (bgH / 2f),  0, (int) bgW, (int) bgH, textures[0]);
            guiGraphics.blit(getX() - (int) (w / 2f), getY() - (int) (h / 2f),  0, (int) w, (int) h, textures[id]);
        }

        private boolean hovered(double mouseX, double mouseY) {
            float w = width * scale;
            float h = height * scale;
            return cooldown == 0 && mouseX >= getX() - w / 2f && mouseY >= getY() - h / 2f && mouseX <= getX() + w && mouseY <= getY() + h;
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (cooldown == 0) {
                cooldown = 40;
                switchId = -switchId;
                CalamitySounds.HYPERPLANE_MATRIX_OPTIONS.playLocalSound();
                NetMessages.sendToServer(new HyperplaneMatrixOperation(switchId));
            }
        }

        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            return hovered(mouseX, mouseY);
        }

        public static void afterMainTextureLoad(TextureAtlas atlas) {
            textures = new TextureAtlasSprite[7];
            textures[0] = atlas.getSprite(CalamityCurios.ModResource("hyperplane_matrix_background"));
            textures[1] = atlas.getSprite(CalamityCurios.ModResource("cyborg_immortality"));
            textures[2] = atlas.getSprite(CalamityCurios.ModResource("exo_throne"));
            textures[3] = atlas.getSprite(CalamityCurios.ModResource("sunrise"));
            textures[4] = atlas.getSprite(CalamityCurios.ModResource("noon"));
            textures[5] = atlas.getSprite(CalamityCurios.ModResource("sunset"));
            textures[6] = atlas.getSprite(CalamityCurios.ModResource("atomize"));
        }
    }

    private class HyperplaneMatrixSettings extends PrimitiveSettings {
        private float partialTick;
        private MultiBufferSource source;
        private final Vector4i vertexColor = RenderUtil.black();
        private final Vector4i color = RenderUtil.fromColorGet(Color.CYAN);

        public HyperplaneMatrixSettings() {
            super(CalamityShaders.getHolographicFlow());
        }

        @Override
        public float vertexWidth(float completionRatio) {
            float widthOffset = (float) Math.cos(completionRatio * 73f - (RenderUtil.getLocalTick() + partialTick) * .8f)
                * RenderUtil.clampLerp(0f, 0.1f, completionRatio) * RenderUtil.clampLerp(1f, 0.9f, completionRatio);
            float width = lastHologramRayWidth == hologramRayWidth ? hologramRayWidth : Mth.lerp(partialTick, lastHologramRayWidth, hologramRayWidth);
            return Mth.lerp(completionRatio, 1f, width + 1.5f) + widthOffset * .5f;
        }

        @Override
        public Vector4i vertexColor(float completionRatio) {
            return RenderUtil.multiplyColor(RenderUtil.multiplyColor(color, RenderUtil.clampLerp(0.8f, 0.5f, completionRatio), vertexColor), .3f, vertexColor);
        }

        @Override
        public int getCapacity() {
            return 9;
        }

        @Override
        public SamplingPointMode sampling() {
            return SamplingPointMode.LINEAR;
        }

        @Override
        public VertexConsumer getConsumer() {
            return source.getBuffer(shader);
        }
    }
}
