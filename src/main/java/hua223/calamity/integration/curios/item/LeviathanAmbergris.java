package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class LeviathanAmbergris extends SprintCurio {
    public LeviathanAmbergris(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isAttachment() {
        return true;
    }

    @Override
    public void onCollision(ServerPlayer player, LivingEntity target) {
        target.hurt(DamageSource.DROWN, 4);
        MobEffect effect = CalamityEffects.RIPTIDE.get();
        if (!target.hasEffect(effect))
            target.addEffect(new MobEffectInstance(effect, 100, 0));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("leviathan_ambergris", 1));
        tooltips.add(CMLangUtil.getTranslatable("leviathan_ambergris", 2));
        tooltips.add(CMLangUtil.getTranslatable("leviathan_ambergris", 3));
        return tooltips;
    }
}
