package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.render.entity.CrystallizationRenderLayer;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import hua223.calamity.util.IDataPackResponse;
import hua223.calamity.util.damage.CalamityDamageSource;
import hua223.calamity.util.damage.CalamityDamageTypes;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class BlazingCore extends BaseCurio implements
    ICuriosStorage, IKeyDataPackResponse, IDataPackResponse {
    public BlazingCore(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        setKeyMapping(player, true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        getCount(player)[0] = 0;
        setKeyMapping(player, false);
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        float[] count = getCount(player);
        if (count[0] == 0) return;

        getPack().putByte("state", (byte) 1);
        sendToClient(player);

        switch ((int) count[0]) {
            case 1 -> {
                listener.canceledEvent();
                count[0] = 2;

                List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(5));
                DamageSource source = CalamityDamageSource.source(CalamityDamageTypes.BLAZING_CORE, player);;
                for (Mob mob : mobs) mob.hurt(source, listener.baseAmount);
            }
            case 2 -> {
                player.heal(5 + listener.baseAmount / 2);
                player.getCooldowns().addCooldown(this, 300);
                count[0] = 0;
            }
        }
    }

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath) {
            getPack().putByte("state", (byte) 3);
            sendToClient(listener.player);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onLogOut(Player player) {
        if (player.isLocalPlayer()) CrystallizationRenderLayer.stop();
    }

    @Override
    public void onServerResponse(ServerPlayer player, CompoundTag tag) {
        float[] count = getCount(player);
        if (count[0] == 0) {
            count[0] = 1;
            getPack().putByte("state", (byte) 0);
            sendToClient(player);
            DelayRunnable.addRunTask(300, () -> {
                if (count[0] != 0) {
                    count[0] = 0;
                    player.getCooldowns().addCooldown(this, 300);
                    getPack().putByte("state", (byte) 2);
                    sendToClient(player);
                }
            });
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        switch (tag.getByte("state")) {
            case 0 -> CrystallizationRenderLayer.start();
            case 1 -> CrystallizationRenderLayer.startChange();
            case 2 -> CrystallizationRenderLayer.notStopChange();
            case 3 -> CrystallizationRenderLayer.stop();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getKeyCode() {
        return GLFW.GLFW_KEY_C;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean accept(Minecraft minecraft) {
        return notInCooling(minecraft);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "blazing_core", 2, 3, 4, 5, 6);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("blazing_core", 1).withStyle(ChatFormatting.YELLOW));
        return tooltips;
    }
}
