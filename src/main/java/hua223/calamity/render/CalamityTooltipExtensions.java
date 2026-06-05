package hua223.calamity.render;

import hua223.calamity.register.gui.SpellType;
import hua223.calamity.render.font.BurnishedAuric;
import hua223.calamity.render.font.CurseFont;
import hua223.calamity.util.ICalamityFont;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CalamityTooltipExtensions implements ClientTooltipComponent {
    //This cannot guarantee compatibility, and in the future, if there are any issues, it should be modified to tryExecute as an instance
    private static final CalamityTooltipExtensions INSTANCE = new CalamityTooltipExtensions();
    private static String component;
    private static ICalamityFont font;

    private CalamityTooltipExtensions() {
    }

    public static CalamityTooltipExtensions fromTypeSetting(String text, CompoundTag tag, int type, ItemStack stack) {
        switch (type) {
            case 1 -> {
                String spell = tag.getString("spell");
                component = spell.equals("EXHUMED") ? text : SpellType.valueOf(spell)
                    .getTypeComponent().getString() + ' ' + text;
                font = CurseFont.setOrCreateFormat(stack);
            }

            case 2 -> {
                component = text;
                font = BurnishedAuric.INSTANCE;
            }
        }

        return INSTANCE;
    }

    @Override
    public int getHeight() {
        return font.getLineHeight();
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return CalamityTooltipExtensions.font.getWidth(component);
    }

    @Override
    public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, MultiBufferSource.@NotNull BufferSource source) {
        CalamityTooltipExtensions.font.reRender(matrix, component, x, y, source);
    }
}
