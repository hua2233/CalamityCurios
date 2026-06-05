package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(value = SprintCurio.class)
public class ElysianAegis extends SprintCurio {
    public ElysianAegis(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        super.equipHandle(player, stack);
        player.Calamity$Player.fireImmune = true;
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        super.unEquipHandle(player, stack);
        player.Calamity$Player.fireImmune = false;
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.BRIMSTONE_FLAMES.get(), CalamityEffects.DRAGON_BURN.get());
    }

    @Override
    public int getTime() {
        return 7;
    }

    @Override
    public double getSpeed() {
        return 1.3;
    }

    @Override
    public int getCooldownTime() {
        return 200;
    }

    @Override
    public void onCollision(ServerPlayer player, LivingEntity target) {
        target.invulnerableTime = 0;
        target.hurt(player.damageSources().playerAttack(player), 15f);
        player.calamity$SetInvulnerableTime(10);
        player.level().explode(player, player.getX(), player.getY(), player.getZ(), 2, Level.ExplosionInteraction.NONE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("elysian_aegis", 1));
            tooltips.add(CMLangUtil.getTranslatable("elysian_aegis", 2));
            tooltips.add(CMLangUtil.getTranslatable("elysian_aegis", 3));
            tooltips.add(CMLangUtil.getTranslatable("elysian_aegis", 4));
        } else tooltips.add(CMLangUtil.getTranslatable("elysian_aegis").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
