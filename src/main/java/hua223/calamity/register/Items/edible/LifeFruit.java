package hua223.calamity.register.Items.edible;

import hua223.calamity.register.Items.AvailableItem;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class LifeFruit extends AvailableItem implements IDataPackResponse {
    public LifeFruit(Rarity rarity, int level) {
        super(new Item.Properties()
            .rarity(rarity)
            .stacksTo(1)
            .food(new FoodProperties
            .Builder()
            .saturationMod(level * 3)
            .nutrition(level * 6)
            .alwaysEat()
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, level), 1)
            .build()));
    }

    @Override
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity entity) {
        if (entity.calamity$IsPlayer && !world.isClientSide) {
            int level = stack.getItem().getFoodProperties().getNutrition() / 6;
            UUID uuid = UUID.nameUUIDFromBytes("LifeFruit".getBytes());
            AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
            VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(uuid);
            byte texture = -1;

            if (modifier == null && level == 1) {
                instance.addPermanentModifier(new VariableAttributeModifier(uuid,
                    "LifeFruit", 0.25f, AttributeModifier.Operation.MULTIPLY_BASE));
                texture = 0;
            } else if (modifier != null) {
                float value = (float) modifier.getAmount();
                float levelValue = level * 0.25f;
                if (value + 0.25f == levelValue) {
                    texture = (byte) (level - 1);
                    modifier.setValue(levelValue, instance);
                }
            }

            if (texture > -1) {
                getPack().putByte("level", texture);
                sendToClient((ServerPlayer) entity);
            }
        }

        return super.finishUsingItem(stack, world, entity);
    }

    @SuppressWarnings({"ConstantConditions"})
    public static void setTexture(ServerPlayer player) {
        UUID uuid = UUID.nameUUIDFromBytes("LifeFruit".getBytes());
        AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
        VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(uuid);

        if (modifier != null) {
            IDataPackResponse lifeFruit = CalamityItems.SANGUINE_TANGERINE.asPackHandler();
            lifeFruit.getPack().putByte("level", (byte) (modifier.getAmount() / 0.25 - 1));
            lifeFruit.sendToClient(player);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().setFromLifeFruitLevel(tag.getByte("level"));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        int leve = stack.getItem().getFoodProperties().getNutrition() / 6;
        tooltips.add(CMLangUtil.getTranslatable("life_fruit" + leve).withStyle(stack.getRarity().getStyleModifier()));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("life_fruit").withStyle(ChatFormatting.YELLOW));
    }
}
