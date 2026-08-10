package hua223.calamity.register.effects;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.EventTypes;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Calcium extends CalamityEffect implements IEffectsCallBack {
    public Calcium(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer) EventTypes.applyEvent(this, (ServerPlayer) entity, true);
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        if (entity.calamity$IsPlayer) EventTypes.applyEvent(this, (ServerPlayer) entity, false);
    }

    @ApplyEvent(120)
    public final void onHurt(HurtListener listener) {
        if (listener.source.is(DamageTypeTags.IS_FALL)) listener.canceledEvent();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("calcium").setStyle(Style.EMPTY.withColor(9801814)));
    }
}
