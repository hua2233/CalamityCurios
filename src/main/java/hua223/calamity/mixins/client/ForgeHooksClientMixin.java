package hua223.calamity.mixins.client;

import com.mojang.datafixers.util.Either;
import hua223.calamity.render.CalamityTooltipExtensions;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {
    @Inject(method = "gatherTooltipComponentsFromElements", at = @At(value = "RETURN"))
    private static void setCuresText(ItemStack stack, List<Either<FormattedText, TooltipComponent>> elements, int mouseX, int screenWidth,
                                     int screenHeight, Font fallbackFont, CallbackInfoReturnable<List<ClientTooltipComponent>> cir) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            int i = tag.getInt(CalamityHelp.FONT_FLAG);
            if (i != 0) elements.get(0).left().ifPresent(formattedText ->
                    cir.getReturnValue().set(0, CalamityTooltipExtensions.fromTypeSetting(formattedText.getString(), tag, i, stack)));
        }
    }

    @Redirect(method = "gatherTooltipComponentsFromElements",
        at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"))
    private static List<?> variableList(Stream<?> instance) {
        return instance.collect(Collectors.toCollection(ArrayList::new));
    }
}
