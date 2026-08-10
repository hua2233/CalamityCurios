package hua223.calamity.register.items;

import hua223.calamity.events.levelevent.BossRushEvent;
import hua223.calamity.events.levelevent.LevelEvent;
import hua223.calamity.events.levelevent.client.ClientRushEvent;
import hua223.calamity.events.levelevent.LevelEventActiveItem;
import hua223.calamity.register.RegisterList;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//This End Position Of You Journey
public class Terminus extends LevelEventActiveItem<BossRushEvent> {
    public Terminus() {
        super(RegisterList.ITEM_CALAMITY);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity livingEntity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) getEvent().siteChanges(remainingUseDuration);
    }
//
    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (!entity.level().isClientSide && count > 0) {
            getPack().putString("sound", "stop");
            sendToAllClient();
            if (!getEvent().inProgress()) getEvent().interruptEvent();
        }
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        if (!level.isClientSide) {
            ServerPlayer player = (ServerPlayer) livingEntity;
            player.getCooldowns().addCooldown(this, 200);
            BossRushEvent event = getEvent();
            if (event.inProgress()) event.interruptEvent();
            else event.start();
        }

        return stack;
    }

    @Override
    protected void activeEvent(ServerPlayer player) {
        new BossRushEvent(player, this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void createClientEvent(CompoundTag tag) {
        new ClientRushEvent(tag, this);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        BossRushEvent event = getEvent();
        return event != null && getEvent().inProgress() ? 40 : 100;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack sack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable("terminus", 1).withStyle(ChatFormatting.DARK_RED));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("terminus", 2).withStyle(ChatFormatting.DARK_RED));
    }
}
