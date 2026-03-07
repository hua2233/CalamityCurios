package hua223.calamity.register.effects;

import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.OmnisciencePerception;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Omniscience extends CalamityEffect implements IEffectsCallBack {
    public Omniscience(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer && !entity.hasEffect(this))
            NetMessages.sendToClient(new OmnisciencePerception(), (ServerPlayer) entity);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("omniscience").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
