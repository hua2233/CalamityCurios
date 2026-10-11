package hua223.calamity.register.rune.blessing;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
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

public class LastStand extends Blessing {
    @Override
    protected String prefix() {
        return "last_stand";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        super.onSlotChange(to, stack, player);
        removeOrApplyEventsBasedOnData(0, 1f, to, player);
    }

    @ApplyEvent(150)
    public final void onDeath(DeathListener listener) {
        listener.canceledPlayerDeathIfNotCooldowns(this, 1f, 12000, new int[] {});
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
        return Map.of(Attributes.ARMOR, new Tuple<>(.15, AttributeModifier.Operation.MULTIPLY_TOTAL),
            Attributes.ARMOR_TOUGHNESS, new Tuple<>(.08, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public float[] addData() {
        return new float[1];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayAttributeList(tooltips);
        tooltips.add(CMLangUtil.getTranslatable(prefix()).withStyle(ChatFormatting.GOLD));
    }
}
