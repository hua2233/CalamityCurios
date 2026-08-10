package hua223.calamity.register.effects;

import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class FungalClump extends CalamityEffect implements IEffectsCallBack {
    public FungalClump(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 39 == 1;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void applyEffectTick(@NotNull LivingEntity target, int amplifier) {
        UUID id = target.getPersistentData().getUUID("owner");
        Player player = target.getServer().getPlayerList().getPlayer(id);
        if (player == null) {
            MobEffectInstance instance = target.getEffect(this);
            instance.calamity$SetProperties(0, instance.getDuration(), null);
        } else if (target != player){
            target.hurt(player.damageSources().genericKill(), 3f);
            player.heal(2f);
        }
    }

    @Override
    public void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {
        if (source instanceof ServerPlayer) entity.getPersistentData().putUUID("owner", source.getUUID());
        else effect.calamity$SetProperties(0, effect.getDuration(), null);;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltips(List<Object> tooltips) {
        tooltips.add(CMLangUtil.getEffectTranslatable("fungal_clump").setStyle(Style.EMPTY.withColor(0x44BBFD)));
    }
}
