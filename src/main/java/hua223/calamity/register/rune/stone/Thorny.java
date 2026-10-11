package hua223.calamity.register.rune.stone;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.HurtListener;
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

public class Thorny extends RuneStone {
    @Override
    protected String prefix() {
        return "thorny";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        addThornyEffect(player, .25f);
    }

    @ApplyEvent
    @SuppressWarnings("ConstantConditions")
    public final void onDamage(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            float[] data = listener.player.Calamity$Player.getRuneData(this);
            listener.entity.hurt(listener.player.damageSources().magic(), listener.baseAmount * data[0]);
        }
    }

    @Override
    public float[] addData() {
        return new float[1];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayThornyValue(tooltips, .25f);
    }
}
