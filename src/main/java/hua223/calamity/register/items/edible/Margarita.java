package hua223.calamity.register.items.edible;

import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Margarita extends Beer {
    public Margarita(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(CalamityEffects.MARGARITA.get(), 100), 1f).build()));
    }

    @Override
    protected void endOfUse(ItemStack stack, Level level, LivingEntity entity) {
        for (MobEffectInstance instance : entity.getActiveEffects())
            if (!instance.getEffect().isBeneficial())
                instance.calamity$SetProperties(instance.getDuration() / 2, instance.getAmplifier(), entity);

        if (!level.isClientSide && entity.calamity$IsPlayer) {
            entity.heal(14);
            entity.calamity$Player.Calamity$Player.changeMana(200, true);
        }
    }

    @Override
    protected int getCooldown() {
        return 900;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag isAdvanced) {
        MutableComponent component = CMLangUtil.getTranslatable("margarita", 1);
        Style style = component.getStyle().withColor(12238227);
        tooltips.add(component.setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("margarita", 2).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("margarita", 3).setStyle(style));
        super.appendHoverText(stack, level, tooltips, isAdvanced);
    }
}
