package hua223.calamity.register.rune.scroll;

import hua223.calamity.register.attribute.CalamityAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Miracle extends EnchantedScroll {
    @Override
    protected String prefix() {
        return "miracle";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        super.onSlotChange(to, stack, player);
        applyArmorLight(to ? 3 : -3, player);
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(.05, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.DAMAGE_UP.get(), new Tuple<>(.02, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public float[] addData() {
        return new float[1];
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayAttributeList(tooltips);
        displayLightValue(tooltips, 3);
    }
}
