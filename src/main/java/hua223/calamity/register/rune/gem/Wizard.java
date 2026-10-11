package hua223.calamity.register.rune.gem;

import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IServantsReinforcement;
import hua223.calamity.util.PlayerServantsManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
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

public class Wizard extends OriginGem implements IServantsReinforcement {
    @Override
    protected String prefix() {
        return "wizard";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        super.onSlotChange(to, stack, player);
        if (to) PlayerServantsManager.addServantsReinforcement(player, this);
        else PlayerServantsManager.removeServantsReinforcement(player, this);
    }

    @Override
    public void modifyServant(boolean apply, ServerPlayer player, LivingEntity servant) {
        PlayerServantsManager.changeAttribute(apply, servant, Attributes.ARMOR, 30, AttributeModifier.Operation.ADDITION);
        PlayerServantsManager.changeAttribute(apply, servant, Attributes.ARMOR, 1, AttributeModifier.Operation.MULTIPLY_BASE);

        PlayerServantsManager.changeAttribute(apply, servant, Attributes.ARMOR_TOUGHNESS, 15, AttributeModifier.Operation.ADDITION);
        PlayerServantsManager.changeAttribute(apply, servant, Attributes.ARMOR_TOUGHNESS, 1, AttributeModifier.Operation.MULTIPLY_BASE);

        PlayerServantsManager.changeAttribute(apply, servant, Attributes.MAX_HEALTH, 50, AttributeModifier.Operation.ADDITION);
        PlayerServantsManager.changeAttribute(apply, servant, Attributes.MAX_HEALTH, 1, AttributeModifier.Operation.MULTIPLY_BASE);

        PlayerServantsManager.changeAttribute(apply, servant, Attributes.ATTACK_DAMAGE, 10, AttributeModifier.Operation.ADDITION);
        PlayerServantsManager.changeAttribute(apply, servant, Attributes.ATTACK_DAMAGE, 1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    @Override
    protected Map<Attribute, Tuple<Double, AttributeModifier.Operation>> getAttribute() {
       return Map.of(Attributes.MAX_HEALTH, new Tuple<>(-.2, AttributeModifier.Operation.MULTIPLY_BASE),
            Attributes.ATTACK_DAMAGE, new Tuple<>(-.2, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayAttributeList(tooltips);
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, prefix(), 1, 2, 3, 4, 5, 6, 7, 8);
    }
}
