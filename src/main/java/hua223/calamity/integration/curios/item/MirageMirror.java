package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@CurioRepel(AbyssalMirror.class)
public class MirageMirror extends BaseCurio {
    public MirageMirror(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.changeInvisible(-.4f);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.changeInvisible(.4f);
    }

    @ApplyEvent
    public final void onCriticalCheck(CriticalHitCheckListener listener) {
        if (listener.player.walkDist == listener.player.walkDistO)
            listener.probability += 0.25f;
        else listener.probability += 0.12f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "mirage_mirror", 1, 2, 3);
        return tooltips;//
    }
}
