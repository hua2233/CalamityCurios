package hua223.calamity.mixins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.main.CalamityCurios;
import hua223.calamity.render.CurseFont;
import hua223.calamity.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow protected int lastHealth;

    @Redirect(method = "renderSelectedItemName", at = @At(value = "INVOKE", target =
        "Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;of(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;", remap = false))
    private IClientItemExtensions getCurseFont(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(EnchantmentProvider.FONT_FLAG)) {
                return CurseFont.getExtensions(stack);
            }
        }

        return IClientItemExtensions.of(stack);
    }

    @Inject(method = "renderHearts", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Gui$HeartType;IIIZZ)V",
        shift = At.Shift.BEFORE, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderAstrHealth(PoseStack poseStack, Player player, int x, int y, int height, int ii, float i2,
                                  int i3, int i4, int i5, boolean b1, CallbackInfo ci, Gui.HeartType type,
                                  int i, int j, int k, int l, int i1) {
        //Extra life will not be contaminated, to be honest, I quite like that yellow heart-shaped one
        if (RenderUtil.astrAmount > 0) {
            int heal = Mth.ceil(lastHealth / 2f) - 1;
            if (i1 == heal) {
                RenderSystem.setShaderTexture(0,
                    CalamityCurios.ModResource("textures/gui/calamity_overlay.png"));
            } else if (i1 == heal - RenderUtil.astrAmount) {
                RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
            }
        }
    }

    @Redirect(method = "renderEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getDuration()I", ordinal = 0))
    private int canFlicker(MobEffectInstance instance) {
        if (instance.calamity$NoFlicker) return 999;
        else return instance.getDuration();
    }
}
