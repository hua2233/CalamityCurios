package hua223.calamity.register.rune.blessing;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.ProjectileSpawnListener;
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

public class Void extends Blessing {
    @Override
    protected String prefix() {
        return "void";
    }

    @Override
    public void onSlotChange(boolean to, ItemStack stack, ServerPlayer player) {
        addVoidTouched(player, to ? .1f : -.1f);
        addThornyEffect(player, to ? .2f : -2f);
        removeOrApplyEventsBasedOnData(0, .5f, to, player);
    }

    @ApplyEvent
    public final void onProjectile(ProjectileSpawnListener listener) {
        float[] data = listener.player.Calamity$Player.getRuneData(this);
        listener.speedVectorAmplifier += data[0];
    }

    @Override
    public float[] addData() {
        return new float[1];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag advanced) {
        displayThornyValue(tooltips, .2f);
        displayVoidTouchedValue(tooltips, .1f);
        tooltips.add(CMLangUtil.getTranslatable(prefix()).withStyle(ChatFormatting.GOLD));
    }
}
