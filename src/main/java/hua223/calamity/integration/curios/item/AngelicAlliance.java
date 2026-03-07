package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;

public class AngelicAlliance extends BaseCurio {
    public AngelicAlliance(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        CuriosApi.getCuriosHelper().addSlotModifier(modifier, "curio", uuid, 2, AttributeModifier.Operation.ADDITION);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("angelic_alliance", 2).withStyle(ChatFormatting.GOLD));
            tooltips.add(CMLangUtil.getTranslatable("angelic_alliance", 3).withStyle(ChatFormatting.GOLD));
            tooltips.add(CMLangUtil.getTranslatable("angelic_alliance", 4).withStyle(ChatFormatting.GOLD));
            tooltips.add(CMLangUtil.getTranslatable("angelic_alliance", 5).withStyle(ChatFormatting.GOLD));
            tooltips.add(CMLangUtil.getTranslatable("angelic_alliance", 6).withStyle(ChatFormatting.GOLD));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("angelic_alliance", 1).withStyle(ChatFormatting.GOLD));
        }
        return tooltips;
    }
}
