package hua223.calamity.register.effects;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.EventTypes;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.events.listeners.TrackedListener;
import hua223.calamity.net.IEffectDataResponse;
import hua223.calamity.register.effects.factor.CountFactorEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
            writeFlagAndSendToAllTracking(player, true);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
       AbstractClientPlayer clientPlayer = TrackedListener.getInitiator(tag);
       if (clientPlayer != null) clientPlayer.Calamity$Player.crescent = tag.getBoolean("flag");
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            EventTypes.applyEvent(this, player, false);
            writeFlagAndSendToAllTracking(player, false);
        }
    }

    @ApplyEvent
    @SuppressWarnings("ConstantConditions")
    public final void onHurt(HurtListener listener) {
        listener.amplifier -= .8f;
        ServerPlayer player = listener.player;
        if (listener.isTriggerByLiving) CalamityHelp.blasting(player, listener.entity.position(), 6);
        MobEffectInstance instance = player.getEffect(this);
        if (--instance.calamity$GetUniversalFactor(this).getFactor()[0] == 0)
            DelayRunnable.currentTickEndRun(() -> player.removeEffect(this));
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        listener.amplifier -= .4f;
    }

    @ApplyEvent
    public final void onTracked(TrackedListener listener) {
        CompoundTag tag = getPack();
        tag.putBoolean("flag", true);
        listener.sendTrackedToTheSelf(tag, this);
    }

    @Override
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("brimstone_barrier").withStyle(ChatFormatting.DARK_RED));
    }
}
