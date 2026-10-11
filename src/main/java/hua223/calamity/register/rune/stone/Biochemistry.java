package hua223.calamity.register.rune.stone;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Biochemistry extends RuneStone {
    @Override
    protected String prefix() {
        return "biochemistry";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        removeOrApplyEventsBasedOnData(0, .15f, to, player);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancelHarmfulOnes(((float[]) listener.player.Calamity$Player.getRuneData(this))[0]);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable(prefix()).withStyle(ChatFormatting.GOLD));
    }
}
