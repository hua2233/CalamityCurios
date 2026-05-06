package hua223.calamity.register.Items.edible;

import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DeliciousMeat extends Item {
    public DeliciousMeat(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .nutrition(10)
            .saturationMod(8)
            .effect(() -> new MobEffectInstance(CalamityEffects.PLENTY_SATISFIED.get(), 600), 1)
            .meat().build()));
        GlobalLoot.mountTo(this);
    }

    @ApplyGlobalLoot
    public final void onGlobalEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityRegistry.CRYOMANCER.get())&& context.chance(0.4f))
            context.addLoot(this, context.getRandomCount(1, 3));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level pLevel, List<Component> tooltips, TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("delicious_meat").withStyle(ChatFormatting.AQUA));
    }
}
