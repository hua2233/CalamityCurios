package hua223.calamity.register.Items;

import hua223.calamity.events.BossRushEvent;
import hua223.calamity.events.ClientRushEvent;
import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//This End Position Of You Journey
public class Terminus extends Item implements IDataPackResponse {
    public Terminus() {
        super(RegisterList.ITEM_CALAMITY);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (!level.isClientSide && (level.dimension() == Level.OVERWORLD || BossRushEvent.isBossRushEventActivating())
        && usedHand == InteractionHand.MAIN_HAND && !player.getCooldowns().isOnCooldown(this)) {
            player.startUsingItem(usedHand);
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) BossRushEvent.siteChanges(remainingUseDuration);
    }
//
    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (!entity.level().isClientSide && count > 0) {
            getPack().putString("sound", "stop");
            sendToAllClient();
            if (!BossRushEvent.isBossRushEventActivating())
                BossRushEvent.interruptEvent();
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        if (!level.isClientSide) {
            ServerPlayer player = (ServerPlayer) livingEntity;
            player.getCooldowns().addCooldown(this, 200);
            if (BossRushEvent.isBossRushEventActivating())
                BossRushEvent.interruptEvent();
            else BossRushEvent.startEvent(player);
        }

        return stack;
    }

    @Override
    public boolean canBeHurtBy(@NotNull DamageSource damageSource) {

        return damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 40;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        ClientRushEvent.handlerDataPack(tag, this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack sack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("terminus", 1).withStyle(ChatFormatting.DARK_RED));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("terminus", 2).withStyle(ChatFormatting.DARK_RED));
    }
}
