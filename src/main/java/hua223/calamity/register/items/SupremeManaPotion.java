package hua223.calamity.register.items;

import hua223.calamity.register.recipe.brewing.CalamityPotionBrewing;
import hua223.calamity.register.recipe.brewing.IBrewingAble;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.PotionRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//Refactoring, Merge into the magic potion set of ISS
public class SupremeManaPotion extends Item implements IBrewingAble {
    public SupremeManaPotion() {
        super(new Properties().rarity(Rarity.EPIC).stacksTo(16));
        CalamityPotionBrewing.addPotionBrewing(this);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        if (!level.isClientSide) livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.INSTANT_MANA.get(), 0, 7));
        stack.shrink(1);
        return stack;
    }

    @Nullable
    public static Tuple<ItemStack, Integer> queryManaPotion(ServerPlayer player) {
        int level = -1;
        ItemStack manaPotion = null;
        Item supreme = CalamityItems.SUPREME_MANA_POTION.get();
        MobEffect instantMana = MobEffectRegistry.INSTANT_MANA.get();

        for (ItemStack item : player.getInventory().items) {
            if (item.is(Items.POTION)) {
                CompoundTag tag = item.getTag();
                if (tag == null) continue;

                Potion potion = PotionUtils.getPotion(tag);

                if (potion != Potions.EMPTY) {
                    for (MobEffectInstance effect : potion.getEffects()) {
                        if (effect.getEffect() == instantMana && effect.getAmplifier() > level) {
                            level = effect.getAmplifier();
                            manaPotion = item;
                            break;
                        }
                    }
                }
                //SupremeManaPotion
            } else if (item.is(supreme)) {
                level = 7;
                manaPotion = item;
                break;
            }
        }

        return manaPotion == null ? null : new Tuple<>(manaPotion, level);
    }

    @Override
    public Potion getPotion() {
        return PotionRegistry.INSTANT_MANA_FOUR.get();
    }

    @Override
    public Item getInput() {
        return CalamityItems.NECROPLASM.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(CMLangUtil.blankLine());
        tooltip.add(CMLangUtil.getTranslatable("supreme_mana_potion").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.irons_spellbooks.instant_mana_description", 200, 40).withStyle(ChatFormatting.BLUE));
    }
}
