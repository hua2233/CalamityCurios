package hua223.calamity.register.items;

import com.google.common.collect.ImmutableMultimap;
import hua223.calamity.register.RegisterList;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.backwards_compat.IBackwardsCompatDefaultNbtItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StarlessNight extends SwordItem implements IBackwardsCompatDefaultNbtItem {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public StarlessNight(Properties properties) {
        super(RegisterList.MOON, 15, -2.8f, properties);
        defaultModifiers = (ImmutableMultimap) ImmutableMultimap.builder().putAll(
            defaultModifiers).put(CalamityAttributes.ARMOR_PENETRATE.get(),
            new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "StarlessNight",
                10, AttributeModifier.Operation.ADDITION)).build();
    }

    @Override
    public void setupItem(ItemStack itemStack) {
        itemStack.enchant(RegisterList.SHARED_PAIN.get(), 3);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("starless_night").withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
    }
}
