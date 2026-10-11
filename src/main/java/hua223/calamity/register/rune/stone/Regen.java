package hua223.calamity.register.rune.stone;

import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.delaytask.DelayRunnable;
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

public class Regen extends RuneStone {
    @Override
    protected String prefix() {
        return "regen";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        removeOrApplyEventsBasedOnData(0, 1, to, player);
    }

    @Override
    protected void effectApply(boolean to, ServerPlayer player) {
        if (to) {
            DelayRunnable.conditionsLoop(() -> {
                float[] data = player.Calamity$Player.getRuneData(this);
                if (data[0] < 1 || player.isDeadOrDying()) return true;

                player.heal(data[0]);
                return false;
            }, 60);
        }
    }

    @Override
    public float[] addData() {
        return new float[1];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        tooltips.add(CMLangUtil.getTranslatable(prefix()).withStyle(ChatFormatting.GOLD));
    }
}
