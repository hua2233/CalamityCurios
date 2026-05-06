package hua223.calamity.integration.curios.listeners;

import hua223.calamity.integration.curios.EventConstructor;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class PlayerAttackListener extends HurtListener {
    public final Boolean isCalamityCriticalHits;
    @EventConstructor
    public PlayerAttackListener(ServerPlayer player, LivingHurtEvent event, Boolean criticalHits) {
        super(player, event);
        isCalamityCriticalHits = criticalHits;
    }
}