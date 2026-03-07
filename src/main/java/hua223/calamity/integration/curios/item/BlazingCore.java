package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.DeathListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.CrystallizationData;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityDamageSource;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class BlazingCore extends BaseCurio implements ICuriosStorage {
    public BlazingCore(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "CRYSTALLIZATION", true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityHelp.setKeyEvent(player, "CRYSTALLIZATION", false);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        float[] count = getCount(player);
        if (count[0] == 0) return;

        switch ((int) count[0]) {
            case 1 -> {
                listener.canceledEvent();
                count[0] = 2;

                Mob[] mobs = player.level.getEntities(player, new AABB(5, 5, 5, 5, 5, 5))
                    .stream().filter(entity -> entity instanceof Mob).toArray(Mob[]::new);
                CalamityDamageSource source = new CalamityDamageSource("core").setNotTriggerEvent();
                for (Mob mob : mobs) mob.hurt(source, listener.baseAmount);

                NetMessages.sendToClient(new CrystallizationData(1), player);
            }
            case 2 -> {
                player.heal(5 + listener.baseAmount / 2);
                player.getCooldowns().addCooldown(this, 300);
                count[0] = 0;
                NetMessages.sendToClient(new CrystallizationData(1), player);
            }
        }
    }

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath) NetMessages.sendToClient(new CrystallizationData(3), listener.player);
    }

    public void active(ServerPlayer player) {
        float[] count = getCount(player);
        if (count == null || count[0] != 0) return;
        count[0] = 1;
        NetMessages.sendToClient(new CrystallizationData(0), player);
        DelayRunnable.addRunTask(300, () -> {
            if (count[0] != 0) {
                count[0] = 0;
                player.getCooldowns().addCooldown(this, 300);
                NetMessages.sendToClient(new CrystallizationData(2), player);
            }
        });
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("blazing_core", 2));
            tooltips.add(CMLangUtil.getTranslatable("blazing_core", 3));
            tooltips.add(CMLangUtil.getTranslatable("blazing_core", 4));
            tooltips.add(CMLangUtil.getTranslatable("blazing_core", 5));
            tooltips.add(CMLangUtil.getTranslatable("blazing_core", 6));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("blazing_core", 1).withStyle(ChatFormatting.YELLOW));
        }
        return tooltips;
    }
}
