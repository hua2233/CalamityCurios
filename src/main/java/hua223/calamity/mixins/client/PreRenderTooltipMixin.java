package hua223.calamity.mixins.client;

import hua223.calamity.mixed.ICalamityFontSetting;
import hua223.calamity.render.font.CalamityFont;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(RenderTooltipEvent.Pre.class)
public class PreRenderTooltipMixin extends RenderTooltipEvent implements ICalamityFontSetting {
    @Unique
    private CalamityFont calamity$Font;

    @Unique
    private boolean calamity$AllowFontGet;


    protected PreRenderTooltipMixin(@NotNull ItemStack itemStack, GuiGraphics graphics, int x, int y, @NotNull Font font, @NotNull List<ClientTooltipComponent> components) {
        super(itemStack, graphics, x, y, font, components);
    }

    @Override
    public @NotNull Font getFont() {
        if (calamity$AllowFontGet && calamity$Font != null) {
            CalamityFont result = calamity$Font;
            calamity$Font = null;
            return result;
        }

        return font;
    }

    @Override
    public void calamity$SetFont(CalamityFont font) {
        calamity$Font = font;
    }

    @Override
    public void calamity$AllowGetFont() {
        calamity$AllowFontGet = true;
    }
}
