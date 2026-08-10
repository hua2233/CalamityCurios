package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CurioRepel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.level.Level;

@CurioRepel(Abaddon.class)
public class ExtinctionVoid extends Abaddon {
    public ExtinctionVoid(Properties properties, double value) {
        super(properties, value);
    }

    @ApplyEvent
    public final void getEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.BRIMSTONE_FLAMES.get());
    }

    @ApplyEvent
    public final void attack(PlayerAttackListener listener) {
        if (listener.isCalamityCriticalHits) {
            ItemCooldowns cooldowns = listener.player.getCooldowns();
            if (cooldowns.isOnCooldown(this)) return;

            listener.player.calamity$SetInvulnerableTime(2);
            listener.entity.level().explode(null, listener.entity.getX(),
                listener.entity.getY(), listener.entity.getZ(), 2f, Level.ExplosionInteraction.NONE);
            listener.entity.addEffect(new MobEffectInstance(CalamityEffects.BRIMSTONE_FLAMES.get(), 40));
            cooldowns.addCooldown(this, 200);
        }
    }
}
