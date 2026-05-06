package hua223.calamity.mixins.client;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.CurseFont;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public class GuiMixin {
    @Unique
    private boolean calamity$RenderAstr;

    @Unique
    private static final ResourceLocation CALAMITY$ASTR =
        CalamityCurios.ModResource("textures/calamity_gui/calamity_overlay.png");

    @Shadow @Final protected static ResourceLocation GUI_ICONS_LOCATION;

    @Redirect(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "INVOKE", target =
        "Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;of(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;"),
        remap = false) //FORGE METHOD
    private IClientItemExtensions getCurseFonts(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(EnchantmentProvider.FONT_FLAG))
            return CurseFont.getExtensions(stack);

        return IClientItemExtensions.of(stack);
    }

    @Inject(method = "renderHearts", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V",
        shift = At.Shift.BEFORE, ordinal = 3), locals = LocalCapture.CAPTURE_FAILHARD)
    private void setHealthValue(GuiGraphics guiGraphics, Player player, int x, int y, int height, int ii, float max,
                                int currentHealth, int i4, int i5, boolean b1, CallbackInfo ci, Gui.HeartType type,
                                int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2) {
        calamity$RenderAstr = RenderUtil.astrAmount > 0 && j2 >= currentHealth - RenderUtil.astrAmount;
    }

    @Redirect(method = "renderHearts", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V", ordinal = 3))
    private void renderAstrHealths(Gui instance, GuiGraphics graphics, Gui.HeartType type,
                                   int x, int y, int yOffset, boolean renderHighlight, boolean halfHeart) {
        graphics.blit(calamity$RenderAstr ? CALAMITY$ASTR : GUI_ICONS_LOCATION, x, y, type.getX(halfHeart, renderHighlight), yOffset, 9, 9);
    }

    @Redirect(method = "renderEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getDuration()I", ordinal = 0))
    private int canFlicker(MobEffectInstance instance) {
        if (instance.calamity$NoFlicker) return 999;
        else return instance.getDuration();
    }
}
