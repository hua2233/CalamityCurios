package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(value = DarkMatterSheath.class)
public class RuinMedallion extends BaseCurio {
    public RuinMedallion(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setCalamityFlag(player, 9, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setCalamityFlag(player, 9, false);
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.applyAmplifier(0.06f);
        listener.probability += 0.06f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ruin_medallion", 1));
        tooltips.add(CMLangUtil.getTranslatable("ruin_medallion", 2));
        return tooltips;
    }
}
