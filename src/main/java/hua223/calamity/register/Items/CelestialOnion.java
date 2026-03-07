package hua223.calamity.register.Items;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

public class CelestialOnion extends AvailableItem {
    public CelestialOnion(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && !entity.getTags().contains("celestial_onion")) {
            CuriosApi.getSlotHelper().growSlotType("curio", entity);
            stack.shrink(1);
            entity.getTags().add("celestial_onion");
        }
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (Screen.hasShiftDown()) {
            pTooltipComponents.add(CMLangUtil.getTranslatable("celestial_onion", 2));
        } else
            pTooltipComponents.add(CMLangUtil.getTranslatable("celestial_onion", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
