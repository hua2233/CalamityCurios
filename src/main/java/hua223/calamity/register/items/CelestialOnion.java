package hua223.calamity.register.items;

import com.google.common.collect.HashMultimap;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CelestialOnion extends AvailableItem {
    public CelestialOnion(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, Level level, @NotNull LivingEntity entity) {
        if (!level.isClientSide) {
            //there should only be one piece of data
            Optional<ICuriosItemHandler> optional =  CuriosApi.getCuriosInventory(entity).resolve();
            if (optional.isPresent()) {
                ICuriosItemHandler handler = optional.get();
                HashMultimap<String, AttributeModifier> attribute = HashMultimap.create();
                attribute.put("curio", new AttributeModifier(UUID.nameUUIDFromBytes("CelestialOnion".getBytes())
                    , "CelestialOnion", 1, AttributeModifier.Operation.ADDITION));
                handler.addPermanentSlotModifiers(attribute);
            }

            stack.shrink(1);
        }
        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("celestial_onion", 2).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("celestial_onion", 1).withStyle(ChatFormatting.LIGHT_PURPLE));

    }
}
