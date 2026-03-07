package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.PlayerAutoJump;
import hua223.calamity.net.S2CPacket.PlayerClimbable;
import hua223.calamity.net.S2CPacket.PlayerJumpPower;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(value = SprintCurio.class)
public class StatisNinjaBelt extends SprintCurio {
    @OnlyIn(Dist.CLIENT)
    public static boolean autoJump;
    public final boolean immuneFall;

    public StatisNinjaBelt(Properties properties, boolean immuneFall) {
        super(properties);
        this.immuneFall = immuneFall;
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving && CalamityHelp.isCanDodge(
            listener.player, listener.baseAmount, 2, (int) Mth.clamp(listener.baseAmount * 40, 300, 1800))) {
            listener.canceledEvent();
        } else if (listener.source.isFall()) {
            if (immuneFall) {
                listener.canceledEvent();
            } else {
                float d = listener.baseAmount / 2 + 5;
                if (d >= listener.baseAmount) listener.canceledEvent();
                else listener.floating -= d;
            }
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        super.equipHandle(player, stack);
        NetMessages.sendToClient(new PlayerJumpPower(0.32f), player);
        NetMessages.sendToClient(new PlayerClimbable(true), player);
        NetMessages.sendToClient(new PlayerAutoJump(true), player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        super.unEquipHandle(player, stack);
        NetMessages.sendToClient(new PlayerJumpPower(-0.32f), player);
        NetMessages.sendToClient(new PlayerClimbable(false), player);
        NetMessages.sendToClient(new PlayerAutoJump(false), player);
    }

    @Override
    public int getTime() {
        return 6;
    }

    @Override
    public double getSpeed() {
        return 1.3;
    }

    @Override
    public int getCooldownTime() {
        return 240;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("statis_ninja_belt", 1));
        tooltips.add(CMLangUtil.getTranslatable(immuneFall ? "statis_void_sash" : "statis_ninja_belt", 2));
        tooltips.add(CMLangUtil.getTranslatable("statis_ninja_belt", 3));
        tooltips.add(CMLangUtil.getTranslatable("statis_ninja_belt", 4));
        tooltips.add(CMLangUtil.getTranslatable("statis_ninja_belt", 5));
        return tooltips;
    }
}
