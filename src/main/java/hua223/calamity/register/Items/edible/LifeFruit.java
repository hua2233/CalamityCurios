package hua223.calamity.register.Items.edible;

import hua223.calamity.register.Items.AvailableItem;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class LifeFruit extends AvailableItem {
    private final String text;
    private float maxValue;

    public LifeFruit(Properties properties, String text) {
        super(properties);
        this.text = text;
        maxValue += 0.25f;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level pLevel, LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (instance != null) {
            UUID uuid = UUID.nameUUIDFromBytes("LifeFruit".getBytes());
            VariableAttributeModifier modifier = VariableAttributeModifier.getModifierInInstance(instance, uuid);
            if (modifier == null) instance.addPermanentModifier(
                VariableAttributeModifier.createRetainVariable(uuid, "LifeFruit", 0.25, AttributeModifier.Operation.MULTIPLY_BASE));
            else if (modifier.getAmount() < maxValue) modifier.setValue(modifier.getAmount() + 0.25f, instance);

            stack.shrink(1);
        }

        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.EAT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltips, TooltipFlag pIsAdvanced) {
        if (Screen.hasShiftDown()) tooltips.add(CMLangUtil.getTranslatable("life_fruit"));
        else tooltips.add(CMLangUtil.getTranslatable(text));
    }
}
