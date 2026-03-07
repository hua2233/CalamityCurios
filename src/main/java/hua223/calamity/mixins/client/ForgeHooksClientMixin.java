package hua223.calamity.mixins.client;

import hua223.calamity.capability.EnchantmentProvider;
import hua223.calamity.render.CurseFont;
import hua223.calamity.render.CurseTooltipExtensions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
@Mixin(ForgeHooksClient.class)
public class ForgeHooksClientMixin {
    @Inject(method = "gatherTooltipComponents(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;Ljava/util/Optional;IIILnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/Font;)Ljava/util/List;",
        at = @At(value = "RETURN"), remap = false)
    private static void setCuresText(ItemStack stack, List<? extends FormattedText> textElements, Optional itemComponent, int mouseX, int screenWidth,
                                     int screenHeight, @Nullable Font forcedFont, Font fallbackFont, CallbackInfoReturnable<List<ClientTooltipComponent>> cir) {
        if (stack.hasTag()) {
            String tag = stack.getTag().getString(EnchantmentProvider.FONT_FLAG);
            if (!tag.isEmpty()) {
                cir.getReturnValue().set(0, CurseTooltipExtensions.setRender(textElements.get(0),
                    tag, CurseFont.getOrCreateFont(stack)));
            }
        }
    }

    @Redirect(method = "gatherTooltipComponents(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;Ljava/util/Optional;IIILnet/minecraft/client/gui/Font;Lnet/minecraft/client/gui/Font;)Ljava/util/List;",
        at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"), remap = false)
    private static List<?> variableList(Stream<?> instance) {
        return instance.collect(Collectors.toCollection(ArrayList::new));
    }
}
