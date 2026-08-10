package hua223.calamity.mixins.client;

import hua223.calamity.mixed.ICalamityHeartType;
import hua223.calamity.render.font.CurseFont;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CalamityPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final protected RandomSource random;

    @Shadow protected abstract void renderHeart(GuiGraphics pGuiGraphics, Gui.HeartType pHeartType,
                                                int pX, int pY, int pYOffset, boolean pRenderHighlight, boolean pHalfHeart);

    @Shadow @Final protected static ResourceLocation GUI_ICONS_LOCATION;

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "INVOKE", target =
        "Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;of(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraftforge/client/extensions/common/IClientItemExtensions;"),
        remap = false) //FORGE METHOD
    private IClientItemExtensions getCurseFonts(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().getInt(CalamityHelp.FONT_FLAG) == 1)
            return CurseFont.getExtensions(stack);

        return IClientItemExtensions.of(stack);
    }

    /**
     * @author hua223
     * @reason Insert custom rendering heart
     */
    @Overwrite
    @SuppressWarnings("ALL")
    protected void renderHearts(GuiGraphics graphics, Player player, int x, int y,
                                int height, int offsetHeartIndex, float maxHealth, int currentHealth,
                                int displayHealth, int absorptionAmount, boolean renderHighlight) {
        CalamityPlayer calamity = player.Calamity$Player;
        ICalamityHeartType type = calamity.forPlayer(player);
        int i = 9 * (player.level().getLevelData().isHardcore() ? 5 : 0);
        int j = Mth.ceil((double)maxHealth / (double)2.0F);
        int k = Mth.ceil((double)absorptionAmount / (double)2.0F);
        int l = j * 2;

        for(int i1 = j + k - 1; i1 >= 0; --i1) {
            int j1 = i1 / 10;
            int k1 = i1 % 10;
            int l1 = x + k1 * 8;
            int i2 = y - j1 * height;
            if (currentHealth + absorptionAmount <= 4) i2 += random.nextInt(2);
            if (i1 < j && i1 == offsetHeartIndex) i2 -= 2;

            renderHeart(graphics, Gui.HeartType.CONTAINER, l1, i2, i, renderHighlight, false);
            int j2 = i1 * 2;
            boolean flag = i1 >= j;
            if (flag) {
                int k2 = j2 - l;
                if (k2 < absorptionAmount) {
                    boolean flag1 = k2 + 1 == absorptionAmount;
                    if (type.equals(Gui.HeartType.WITHERED))
                        calamity$RenderHeart(graphics, type, l1, i2, i, false, flag1);
                    else renderHeart(graphics, Gui.HeartType.ABSORBING, l1, i2, i, false, flag1);
                }
            }

            if (renderHighlight && j2 < displayHealth) {
                boolean flag2 = j2 + 1 == displayHealth;
                calamity$RenderHeart(graphics, type, l1, i2, i, true, flag2);
            }

            if (j2 < currentHealth) {
                boolean flag3 = j2 + 1 == currentHealth;
                calamity$RenderHeart(graphics, calamity.astrAmount > 0 && j2 >= currentHealth -
                    calamity.astrAmount ? calamity.astr : type, l1, i2, i, false, flag3);
            }
        }
    }


    @Unique
    protected void calamity$RenderHeart(GuiGraphics graphics, ICalamityHeartType type, int x, int y,
                                        int yOffset, boolean renderHighlight, boolean halfHeart) {
        graphics.blit(GUI_ICONS_LOCATION, x, y, type.calamity$GetX(halfHeart, renderHighlight), type.calamity$GetY(yOffset), 9, 9);
    }

    @Redirect(method = "renderEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;getDuration()I", ordinal = 0))
    private int canFlicker(MobEffectInstance instance) {
        if (instance.calamity$NoFlicker) return 999;
        else return instance.getDuration();
    }

    @Mixin(Gui.HeartType.class)
    public static abstract class CalamityHeartType implements ICalamityHeartType {
        @Shadow public abstract int getX(boolean pHalfHeart, boolean pRenderHighlight);

        @Override
        public int calamity$GetX(boolean halfHeart, boolean renderHighlight) {
            return getX(halfHeart, renderHighlight);
        }

        @Override
        public int calamity$GetY(int y) {
            return y;
        }
    }
}
