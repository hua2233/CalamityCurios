package hua223.calamity.register.effects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface IEffectsCallBack {
    default void onAdd(MobEffectInstance effect, LivingEntity entity, Entity source) {}
    default void onRemove(MobEffectInstance effect, LivingEntity entity) {}
    default void onLoad(MobEffectInstance instance, LivingEntity entity) {
        onAdd(instance, entity, null);
    }

    default void inactivationEffect(LivingEntity entity, boolean isApply) {
        if (entity.calamity$IsPlayer && !entity.calamity$Player.isLocalPlayer()) {
            ServerPlayer player = (ServerPlayer) entity;
            if (isApply) player.calamity$InactivationCount++;
            else player.calamity$InactivationCount--;
        }
    }
}
