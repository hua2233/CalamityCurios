package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CeaselessHunger extends CalamityEffect {
    public CeaselessHunger(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public void applyServerTick(@NotNull LivingEntity target, int amplifier) {
        if (target.calamity$IsPlayer)
            for (Entity entity : ((ServerLevel) target.level()).getEntities().getAll())
                if (entity != null && entity.getType() == EntityType.ITEM)
                    entity.playerTouch(target.calamity$Player);
    }

    @Override
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("ceaseless_hunger").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
