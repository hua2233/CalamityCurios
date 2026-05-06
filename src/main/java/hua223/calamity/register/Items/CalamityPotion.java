package hua223.calamity.register.Items;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CalamityPotion extends Item {
    private final Supplier<MobEffectInstance> effect;
    private final String text;
    private int color;
    public CalamityPotion(Properties properties, String text, Supplier<MobEffectInstance> supplier) {
        super(properties);
        this.text = text;
        effect = supplier;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity.calamity$IsPlayer && !level.isClientSide) {
            ServerPlayer player = (ServerPlayer) entity;
//            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.addEffect(effect.get());
            player.awardStat(Stats.ITEM_USED.get(this));

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack glass = new ItemStack(Items.GLASS_BOTTLE);
                player.getInventory().add(glass);
                return glass;
            }
        }

        return stack;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 32;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    public CalamityPotion setTextColor(int formatting) {
        color = formatting;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        MobEffectInstance instance = effect.get();
        MobEffect effect = instance.getEffect();
        MutableComponent IC = Component.translatable(instance.getDescriptionId());

        if (instance.getAmplifier() > 0)
            IC = Component.translatable("potion.withAmplifier", IC, Component.translatable("potion.potency." + instance.getAmplifier()));

        if (instance.getDuration() > 20)
            IC = Component.translatable("potion.withDuration", IC, MobEffectUtil.formatDuration(instance, 1f));

        tooltip.add(IC.withStyle(effect.getCategory().getTooltipFormatting()));

        Map<Attribute, AttributeModifier> map = effect.getAttributeModifiers();
        if (!map.isEmpty()) {
            tooltip.add(CMLangUtil.blankLine());
            tooltip.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            for(Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                AttributeModifier value = entry.getValue();
                AttributeModifier.Operation operation = value.getOperation();
                AttributeModifier modifier = new AttributeModifier(value.getName(), effect.getAttributeModifierValue(
                    instance.getAmplifier(), value), operation);

                double v1 =  modifier.getAmount();
                double v2 = operation == AttributeModifier.Operation.ADDITION ? v1 : v1 * (double)100.0F;

                if (v1 > 0.0) tooltip.add(Component.translatable("attribute.modifier.plus." + operation.toValue(),
                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(v2), Component.translatable(
                        (entry.getKey()).getDescriptionId())).withStyle(ChatFormatting.BLUE));
                else if (v1 < 0) tooltip.add(Component.translatable("attribute.modifier.take." + operation.toValue(),
                    ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(v2 * -1.0F), Component.translatable(
                        (entry.getKey()).getDescriptionId())).withStyle(ChatFormatting.RED));
            }
        }

        tooltip.add(CMLangUtil.blankLine());
        MutableComponent component = CMLangUtil.getTranslatable(text);
        if (color > -1) component.setStyle(Style.EMPTY.withColor(color));
        tooltip.add(component);
    }
}
