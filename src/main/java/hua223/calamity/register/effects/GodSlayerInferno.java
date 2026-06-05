package hua223.calamity.register.effects;

import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GodSlayerInferno extends CalamityEffect implements IEffectsCallBack {
    public GodSlayerInferno(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().magic(), 4 * ++amplifier);
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        IDataPackResponse response = CalamityItems.NEBULOUS_CORE.asPackHandler();
        CompoundTag tag = response.getPack();
        tag.putInt("id", entity.getId());
        tag.putBoolean("flag", true);
        inactivationEffect(entity, true);
    }

    @Override
    public void onRemove(MobEffectInstance effect, LivingEntity entity) {
        if (entity.isAlive()) {
            IDataPackResponse response = CalamityItems.NEBULOUS_CORE.asPackHandler();
            CompoundTag tag = response.getPack();
            tag.putInt("id", entity.getId());
            tag.putBoolean("flag", false);
            inactivationEffect(entity, false);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("god_slayer_inferno").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
