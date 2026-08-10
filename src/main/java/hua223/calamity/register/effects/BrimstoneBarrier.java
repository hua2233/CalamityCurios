package hua223.calamity.register.effects;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.EventTypes;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.net.IEffectDataResponse;
import hua223.calamity.register.effects.factor.CountFactorEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class BrimstoneBarrier extends CountFactorEffects implements IEffectsCallBack, IEffectDataResponse {
    public BrimstoneBarrier() {
        super(MobEffectCategory.NEUTRAL, 11141120);
    }

    @Override
    protected CountFactor factory() {
        CountFactor factor = new CountFactor(1);
        factor.getFactor()[0] = 3;
        return factor;
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity instanceof ServerPlayer player) {
            EventTypes.applyEvent(this, player, true);
            getPack().putInt("id", player.getId());
            sendToAllClient();
        }
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            EventTypes.applyEvent(this, player, false);
            getPack().putInt("id", -player.getId());
            sendToAllClient();
        }
    }

    @ApplyEvent
    @SuppressWarnings("ConstantConditions")
    public final void onHurt(HurtListener listener) {
        listener.amplifier -= .8f;
        ServerPlayer player = listener.player;
        if (listener.isTriggerByLiving) CalamityHelp.blastingTheEnemy(player, listener.entity.position(), 6);
        MobEffectInstance instance = player.getEffect(this);
        if (--instance.calamity$GetUniversalFactor(this).getFactor()[0] == 0)
            DelayRunnable.currentTickEndRun(() -> player.removeEffect(this));
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        listener.amplifier -= .4f;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onClientResponse(CompoundTag tag) {
        int id = tag.getInt("id");
        Entity entity = Minecraft.getInstance().level.getEntity(Math.abs(id));
        if (entity instanceof AbstractClientPlayer player) player.Calamity$Player.crescent = id >= 0;
    }

    @Override
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("brimstone_barrier").withStyle(ChatFormatting.DARK_RED));
    }
}
