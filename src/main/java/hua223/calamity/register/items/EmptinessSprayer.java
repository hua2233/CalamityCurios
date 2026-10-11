package hua223.calamity.register.items;

import hua223.calamity.register.RegisterList;
import hua223.calamity.register.entity.EmptinessSpray;
import hua223.calamity.render.font.CalamityFont;
import hua223.calamity.render.item.IAvatarBackgroundRender;
import hua223.calamity.render.item.EmptinessSprayerRenderer;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

//Developer's Gift
public class EmptinessSprayer extends Item implements IAvatarBackgroundRender {
    public EmptinessSprayer() {
        super(RegisterList.CALAMITY_ONE);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
        player.startUsingItem(usedHand);
        if (!level.isClientSide) EmptinessSpray.create(player);
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return CalamityFont.getAvatar(Component.translatable(getDescriptionId(stack)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new EmptinessSprayerRenderer();
            }
        });
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("emptiness_sprayer", 1).withStyle(ChatFormatting.RED));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("emptiness_sprayer").withStyle(ChatFormatting.DARK_RED));
    }
}
