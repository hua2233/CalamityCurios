package hua223.calamity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import hua223.calamity.register.gui.SpellType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CurseTooltipExtensions implements ClientTooltipComponent {
    //This cannot guarantee compatibility, and in the future, if there are any issues, it should be modified to execute as an instance
    private static final CurseTooltipExtensions INSTANCE = new CurseTooltipExtensions();
    private static String component;
    private static CurseFont font;

    private CurseTooltipExtensions() {
    }

    public static CurseTooltipExtensions setRender(FormattedText text, String spell, CurseFont font) {
        if (!spell.equals("EXHUMED")) component = (SpellType.valueOf(spell).type.getString() + "  " + text.getString());
        else component = text.getString();

        CurseTooltipExtensions.font = font;
        return INSTANCE;
    }

    @Override
    public int getHeight() {
        return font.lineHeight;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(component);
    }

    public void render(PoseStack stack, int x, int y) {
        font.reRender(stack, component, x, y);
    }
}
