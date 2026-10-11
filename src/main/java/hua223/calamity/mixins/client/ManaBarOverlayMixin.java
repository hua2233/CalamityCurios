package hua223.calamity.mixins.client;

import hua223.calamity.util.CalamityHelp;
import io.redspace.ironsspellbooks.config.ClientConfigs;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ManaBarOverlay.class, remap = false)
//OK，although I don't t know how to create textures，but it's better if it still has some visual elements
//Otherwise, after using the magical item, you may not even know that you have already used it
public class ManaBarOverlayMixin {
    @Shadow @Final public static ResourceLocation TEXTURE;

    @Inject(method = "render", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void renderMagicStar(ForgeGui gui, GuiGraphics guiHelper, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci,
                                 LocalPlayer player, int maxMana, int mana, int barX, int barY, int configOffsetY, int configOffsetX,
                                 ManaBarOverlay.Anchor anchor, int imageWidth, int spriteX, int spriteY) {
        byte level = player.Calamity$Player.getMagicItemLevel();
        if (level != 0) {
            float magicStarFrame = 22 / 256f;
            int frameSerialNumber = Integer.bitCount(level) - 1;
            float u0 = magicStarFrame * frameSerialNumber;
            int xOffset = barX + (anchor == ManaBarOverlay.Anchor.XP ? 90 : 44);

            //Subpixel offset，centered draw, otherwise it would be strange
            guiHelper.pose().translate(.5f, 0, 0);
            guiHelper.innerBlit(TEXTURE, xOffset, xOffset + 9, barY, barY + 9, 0, u0, u0 + magicStarFrame, 138 / 256f, 162 / 256f);
            guiHelper.pose().translate(-.5f, 0, 0);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void renderMagicStyleBar(GuiGraphics instance, ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
        byte level = CalamityHelp.getClientCalamity().getMagicItemLevel();

        if (level == 0) instance.blit(atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight);
        else {
            float magicStyleBarFrame = 12 / 256f;
            int imageWidth = ClientConfigs.MANA_BAR_ANCHOR.get() == ManaBarOverlay.Anchor.XP ? 188 : 98;
            //uWidth? this is not its full width，
            //It's just the pixel position after the full width * progress ratio
            float proportion = uWidth / (float) imageWidth;
            int frameSerialNumber = Integer.bitCount(level) - 1;
            float u0 = magicStyleBarFrame * frameSerialNumber;

            //Remove the width of the front and rear borders
            instance.innerBlit(atlasLocation, x + 4, x + uWidth - 4, y + 8, y + 13, 0, u0, u0 + magicStyleBarFrame * proportion,162 / 256f, 174 / 256f);

            //Render logo
            instance.blit(atlasLocation, x + (imageWidth / 2 - 5), y, 0, 174, 10, 11);
        }
    }
}
