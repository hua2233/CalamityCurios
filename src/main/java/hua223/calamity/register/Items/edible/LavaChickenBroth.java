package hua223.calamity.register.Items.edible;

import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LavaChickenBroth extends Item {
    public LavaChickenBroth(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(CalamityEffects.EXQUISITELY_STUFFED.get(), 36000), 1f)
            .nutrition(30).saturationMod(20).meat().alwaysEat().build()));
        GlobalLoot.mountTo(this);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(CalamityEffects.EXQUISITELY_STUFFED.get(), 36000));
            entity.setSecondsOnFire(1800);
        }
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EAT,
            SoundSource.NEUTRAL, 1.0F, 1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
        entity.gameEvent(GameEvent.EAT);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 48;
    }

    @ApplyGlobalLoot//1.19.2 No new bosses have been updated, and there will be no handling for creature drops temporarily
    public final void onGetChestLoot(ChestLootContext context) {
        if (context.fromSpecificName("battleground/burial_loot") && context.chance(0.005f))
            context.addLoot(this, 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("lava_chicken_broth").withStyle(ChatFormatting.RED));
    }
}
