package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@CurioRepel(EclipseMirror.class)
public class DarkMatterSheath extends BaseCurio {
    public DarkMatterSheath(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        RuinMedallion.sprintingHit(player.Calamity$Player, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        RuinMedallion.sprintingHit(player.Calamity$Player, false);
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
        else listener.probability = 0.1f;
        listener.applyAmplifier(0.1f);
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "dark_matter_sheath", 1, 2, 3);
        return tooltips;
    }
}
