package hua223.calamity.register.effects;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.EventTypes;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.net.IEffectDataResponse;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.nbt.CompoundTag;
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

public class Bounding extends CalamityEffect implements IEffectsCallBack, IEffectDataResponse {
    public Bounding(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer) {
            EventTypes.applyEvent(this, (ServerPlayer) entity, true);
            getPack().putFloat("bounding", 0.3f * Math.min(3, effect.getAmplifier() + 1));
            sendToClient((ServerPlayer) entity);
        }
    }

    @Override
    public void onEffectRemoved(LivingEntity entity, int amplifier) {
        if (entity.calamity$IsPlayer) {
            EventTypes.applyEvent(this, (ServerPlayer) entity, false);
            getPack().putFloat("bounding", -0.3f * Math.min(3, amplifier + 1));
            sendToClient((ServerPlayer) entity);
        }
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.source.is(DamageTypeTags.IS_FALL)) listener.amplifier -= 0.4f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().jumpPower += tag.getFloat("bounding");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("bounding").setStyle(Style.EMPTY.withColor(3255451)));
    }
}
