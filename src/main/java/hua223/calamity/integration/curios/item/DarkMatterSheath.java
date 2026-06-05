package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import net.minecraft.ChatFormatting;
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
        ((RuinMedallion) CalamityItems.RUIN_MEDALLION.get()).equipHandle(player, stack);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        ((RuinMedallion) CalamityItems.RUIN_MEDALLION.get()).unEquipHandle(player, stack);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (!listener.player.getCooldowns().isOnCooldown(this)) {
            listener.amplifier += 1f;
            listener.player.getCooldowns().addCooldown(this, 200);
        }
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitCheckListener listener) {
        if (listener.player.walkDistO != listener.player.walkDist) listener.probability += 0.16f;
        else listener.probability = 0.06f;
        listener.applyAmplifier(0.06f);
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "dark_matter_sheath", 1, 2, 3);
        return tooltips;
    }
}
