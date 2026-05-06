package hua223.calamity.render;

import hua223.calamity.register.gui.SpellType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CurseTooltipExtensions implements ClientTooltipComponent {
    //This cannot guarantee compatibility, and in the future, if there are any issues, it should be modified to tryExecute as an instance
    private static final CurseTooltipExtensions INSTANCE = new CurseTooltipExtensions();
    private static String component;
    private static CurseFont font;

    private CurseTooltipExtensions() {
    }

    public static CurseTooltipExtensions setContent(String text, String spell, CurseFont font) {
        component = spell.equals("EXHUMED") ? text : SpellType.valueOf(spell).getTypeComponent().getString() + ' ' + text;

        CurseTooltipExtensions.font = font;
        return INSTANCE;
    }

    @Override
    public int getHeight() {
        return font.lineHeight;
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return CurseTooltipExtensions.font.width(component);
    }

    @Override
    public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, MultiBufferSource.@NotNull BufferSource source) {
        CurseTooltipExtensions.font.reRender(matrix, component, x, y, source);
    }
}
