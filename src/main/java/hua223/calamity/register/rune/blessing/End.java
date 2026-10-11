package hua223.calamity.register.rune.blessing;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class End extends Blessing {
    @Override
    protected String prefix() {
        return "end";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        super.onSlotChange(to, stack, player);
        addThornyEffect(player, .3f);
        addVoidTouched(player, .1f);
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(.12, AttributeModifier.Operation.MULTIPLY_TOTAL),
            Attributes.ARMOR_TOUGHNESS, new Tuple<>(.07, AttributeModifier.Operation.MULTIPLY_TOTAL),
            Attributes.ATTACK_KNOCKBACK, new Tuple<>(.2, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.DAMAGE_UP.get(), new Tuple<>(.06, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.CRITICAL_STRIKE_CHANCE.get(), new Tuple<>(.05, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayAttributeList(tooltips);
        displayThornyValue(tooltips, .3f);
        displayVoidTouchedValue(tooltips, .1f);
    }
}
