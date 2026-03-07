package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(value = MirageMirror.class, isRoot = true)
public class MirageMirror extends BaseCurio {
    public MirageMirror(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.calamity$Invisible = 0.5f;
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.calamity$Invisible = 0f;
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        if (listener.player.walkDist == listener.player.walkDistO)
            listener.probability += 0.25f;
        else listener.probability += 0.12f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("mirage_mirror", 1));
        tooltips.add(CMLangUtil.getTranslatable("mirage_mirror", 2));
        tooltips.add(CMLangUtil.getTranslatable("mirage_mirror", 3));
        return tooltips;//
    }
}
