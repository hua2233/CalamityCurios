package hua223.calamity.register.effects;

import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Bounding extends CalamityEffect implements IEffectsCallBack {
    @OnlyIn(Dist.CLIENT)
    public static float jumpPower = 1f;

    public Bounding(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (entity.calamity$IsPlayer && (!entity.hasEffect(this) ||
            entity.getEffect(this).getAmplifier() < effect.getAmplifier())) {
            //It must require a specific Item object as the data processing carrier
            //We are unable to process deserializing the IdMap table
            IDataPackResponse response = (IDataPackResponse) CalamityItems.BOUNDING.get();
            response.getPack().putFloat("bounding", 0.3f * Math.min(3, effect.getAmplifier() + 1));
            response.sendToClient((ServerPlayer) entity);
        }
    }

    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        if (entity.calamity$IsPlayer) {
            IDataPackResponse response = (IDataPackResponse) CalamityItems.BOUNDING.get();
            response.getPack().putFloat("bounding", -0.3f * Math.min(3, effect.getAmplifier() + 1));
            response.sendToClient((ServerPlayer) entity);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("bounding").setStyle(Style.EMPTY.withColor(3255451)));
    }
}
