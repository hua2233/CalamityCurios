package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AcidVenom extends CalamityEffect implements IEffectsCallBack {
    protected AcidVenom(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        super.applyEffectTick(pLivingEntity, pAmplifier);
        for (ItemStack armor : pLivingEntity.getArmorSlots()) {
            if (armor.isEmpty()) break;
            armor.hurt(2 * (pAmplifier + 1), pLivingEntity.getRandom(), null);
        }
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        inactivationEffect(entity, true);
    }


    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        inactivationEffect(entity, false);
    }

    @Override
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("acid_venom").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
