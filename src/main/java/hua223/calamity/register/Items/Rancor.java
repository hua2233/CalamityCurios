package hua223.calamity.register.Items;

import hua223.calamity.register.entity.RancorMagicCircle;
import hua223.calamity.util.CMLangUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Rancor extends Item {
    private static final Map<ServerPlayer, RancorMagicCircle> magicMap = new Object2ObjectOpenHashMap<>();

    public Rancor(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && !player.isUsingItem()) {
            magicMap.put((ServerPlayer) player, RancorMagicCircle.create(player));
            player.startUsingItem(hand);
        }

        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10000;
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if (!player.level.isClientSide && !player.isAlive()) {
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!level.isClientSide) {
            magicMap.get(livingEntity).discard();
            magicMap.remove(livingEntity);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> components, TooltipFlag isAdvanced) {
        components.add(CMLangUtil.getTranslatable("rancor", 1).withStyle(ChatFormatting.DARK_RED));
        components.add(CMLangUtil.getTranslatable("rancor", 2).withStyle(ChatFormatting.DARK_RED));
    }
}
