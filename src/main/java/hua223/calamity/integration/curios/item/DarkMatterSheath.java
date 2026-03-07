package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@ConflictChain(value = DarkMatterSheath.class, isRoot = true)
public class DarkMatterSheath extends BaseCurio {
    public DarkMatterSheath(Properties properties) {
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
    public final void onAttack(PlayerAttackListener listener) {
        if (!listener.player.getCooldowns().isOnCooldown(this)) {
            listener.amplifier += 1f;
            listener.player.getCooldowns().addCooldown(this, 200);
        }
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        if (listener.player.walkDistO != listener.player.walkDist) listener.probability += 0.16f;
        else listener.probability = 0.06f;
        listener.applyAmplifier(0.06f);
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("dark_matter_sheath", 1));
        tooltips.add(CMLangUtil.getTranslatable("dark_matter_sheath", 2));
        tooltips.add(CMLangUtil.getTranslatable("dark_matter_sheath", 3));
        return tooltips;
    }
}
