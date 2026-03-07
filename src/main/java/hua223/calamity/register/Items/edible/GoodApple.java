package hua223.calamity.register.Items.edible;

import com.mojang.datafixers.util.Pair;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.util.RenderUtil;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class GoodApple extends Item {
    public GoodApple(Properties properties) {
        super(properties.food(new FoodProperties.Builder().alwaysEat()
            .nutrition(10).saturationMod(10).build()));
        GlobalLoot.mountTo(this);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity.calamity$IsPlayer) {
            if (!entity.calamity$Player.getAbilities().instabuild)
                stack.shrink(1);

            AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
            UUID id = UUID.nameUUIDFromBytes("GoodApple".getBytes());
            VariableAttributeModifier modifier = VariableAttributeModifier.getModifierInInstance(instance, id);

            if (modifier == null) instance.addPermanentModifier(
                VariableAttributeModifier.createRetainVariable(id, "GoodApple", 2, AttributeModifier.Operation.ADDITION));
            else modifier.setValue(modifier.getAmount() + 2, instance);
            eatApple(stack, level, entity.calamity$Player);
        }

        return stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getName(ItemStack stack) {
        return RenderUtil.getRainbow(Component.literal("Good Apple"));
    }

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    private static void eatApple(ItemStack stack, Level level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), player.getEatingSound(stack),
            SoundSource.NEUTRAL, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
        boolean enchantedGold = false;
        Item apple = switch (stack.getOrCreateTag().getInt("appleType")) {
            case 1 : yield Items.APPLE;
            case 2 : yield Items.GOLDEN_APPLE;
            case 3 : {
                enchantedGold = true;
                yield Items.ENCHANTED_GOLDEN_APPLE;
            }
            default: yield null;
        };

        int nutrition = 10;
        float saturationMod = 10;
        if (apple != null) {
            FoodProperties properties = apple.getFoodProperties();
            for(Pair<MobEffectInstance, Float> pair : properties.getEffects())
                if (level.random.nextFloat() < pair.getSecond()) {
                    MobEffectInstance instance = pair.getFirst();
                    if (enchantedGold && instance.getEffect() == MobEffects.REGENERATION)
                        instance = new MobEffectInstance(MobEffects.REGENERATION, 600, 4);
                    //Please ensure that you are using a supplier to obtain it
                    //Otherwise, it may modify the original copy
                    player.addEffect(instance);
                }
            //Cross their recovery values
            nutrition += (int) Math.ceil(properties.getSaturationModifier());
            saturationMod += properties.getNutrition();
        }

        FoodData data = player.getFoodData();
        if (data.needsFood()) data.setFoodLevel(nutrition);
        float saturationLevel = data.getSaturationLevel();
        if (saturationLevel < 20) data.setSaturation(saturationLevel + saturationMod);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("foil");
    }

    @ApplyGlobalLoot
    public final void onGlobalChestLoot(ChestLootContext context) {
        //You're lucky!
        if (context.chance(0.05f)) {
            final int[] info = {0, 1, 0};
            if (context.getGeneratedLoot().removeIf(
                stack -> {
                    if (info[0] == 0) {
                        Item loot = stack.getItem();
                        if (loot == Items.GOLDEN_APPLE) {
                            info[1] = 2;
                            info[0] = 1;
                            return true;
                        } else if (loot == Items.ENCHANTED_GOLDEN_APPLE) {
                            info[1] = 3;
                            info[2] = 1;
                            info[0] = 1;
                            return true;
                        } else if (loot == Items.APPLE) {
                            info[0] = 1;
                            return true;
                        }
                    }

                    return false;
                })) {
                ItemStack stack = new ItemStack(this);
                CompoundTag tag = stack.getOrCreateTag();
                tag.putInt("appleType", info[1]);
                tag.putBoolean("foil", info[2] == 1);
                context.addLoot(stack);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(Component.literal("Hey, this apple isn't bad. It's quite good actually... 流れてく時の中ででも 気だるさが ほらグルグル廻って")
            .withStyle(ChatFormatting.GOLD));
    }
}
