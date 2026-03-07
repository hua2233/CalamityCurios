package hua223.calamity.register.Items;

import hua223.calamity.register.entity.UniverseSplitterField;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UniverseSplitter extends Item {
    public UniverseSplitter(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (!level.isClientSide) {
            if (player.getCooldowns().isOnCooldown(this)) {
                Vec3 pos = player.getEyePosition();
                level.explode(player, pos.x, pos.y, pos.z, 10, Explosion.BlockInteraction.NONE);
            } else {
                LivingEntity entity = CalamityHelp.getSightDetectionEntityResult(player, level, 20);
                UniverseSplitterField.create(level, player, entity);
                player.getCooldowns().addCooldown(this, 1200);
            }
        }

        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced) {
        tooltip.add(CMLangUtil.getTranslatable("universe_splitter", 1));
        tooltip.add(CMLangUtil.getTranslatable("universe_splitter", 2));
        tooltip.add(CMLangUtil.getTranslatable("universe_splitter", 3));
        tooltip.add(CMLangUtil.getTranslatable("universe_splitter", 4));
        tooltip.add(CMLangUtil.getTranslatable("universe_splitter", 5));
        tooltip.add(CMLangUtil.getTranslatable("universe_splitter", 6).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(CMLangUtil.getTranslatable("universe_splitter", 7).withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
