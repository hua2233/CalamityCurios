    package hua223.calamity.register.Items.edible;

    import hua223.calamity.capability.CalamityCapProvider;
    import hua223.calamity.register.effects.CalamityEffects;
    import hua223.calamity.util.CMLangUtil;
    import net.minecraft.ChatFormatting;
    import net.minecraft.network.chat.Component;
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

    public class WhiteWine extends Beer {
        public WhiteWine(Properties properties) {
            super(properties.food(new FoodProperties.Builder()
                .effect(() -> new MobEffectInstance(CalamityEffects.WHITE_WINE.get(), 400), 1f)
                .build()));
        }

        @Override
        protected void endOfUse(ItemStack stack, Level level, LivingEntity entity) {
            if (!level.isClientSide) CalamityCapProvider.safetyRunCalamityMagic(entity,
                expand -> expand.calamity$ChangeMana(300, true));
        }

        @Override
        protected int getCooldown() {
            return 1200;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag isAdvanced) {
            tooltips.add(CMLangUtil.getTranslatable("white_wine").withStyle(ChatFormatting.WHITE));
            super.appendHoverText(stack, level, tooltips, isAdvanced);
        }
    }
