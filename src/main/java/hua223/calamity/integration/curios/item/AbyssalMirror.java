package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(MirageMirror.class)
public class AbyssalMirror extends BaseCurio implements ICuriosStorage {
    public AbyssalMirror(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.calamity$Invisible = 0.3f;
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.calamity$Invisible = 0f;
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving && CalamityHelp.isCanDodge(listener.player, listener.baseAmount,
                listener.player.getMaxHealth() * 0.05f, (int) Mth.clamp(listener.baseAmount * 100, 300, 1800))) {
            listener.canceledEvent();
            getCount(listener.player)[0] = 0.5f;
            List<Mob> entities = listener.player.level.getEntitiesOfClass(Mob.class, listener.player.getBoundingBox().inflate(5));
            if (!entities.isEmpty()) {
                for (Mob mob : entities)
                    mob.setNoAi(true);

                DelayRunnable.addRunTask(40, () -> {
                    for (Mob mob : entities)
                        if (!mob.isDeadOrDying())
                            mob.setNoAi(false);
                });
            }

            new FriendlyEffectCloudBuilder(listener.player, listener.player.position(), 360, 4f)
                .setEffects(new MobEffectInstance(CalamityEffects.EUTROPHICATION.get(), 60, 1),
                    new MobEffectInstance(CalamityEffects.CRUSH_DEPTH.get(), 60, 1))
                .setWaitTime(5).build();
        }
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        if (listener.player.walkDist == listener.player.walkDistO)
            listener.probability += 0.25f;
        else listener.probability += 0.12f;

        float[] count = getCount(listener.player);
        if (count[0] > 0) {
            listener.probability += count[0];
            count[0] = 0f;
        }
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("abyssal_mirror", 1));
        tooltips.add(CMLangUtil.getTranslatable("abyssal_mirror", 2));
        tooltips.add(CMLangUtil.getTranslatable("abyssal_mirror", 3));
        tooltips.add(CMLangUtil.getTranslatable("abyssal_mirror", 4));
        tooltips.add(CMLangUtil.getTranslatable("abyssal_mirror", 5));
        tooltips.add(CMLangUtil.getTranslatable("abyssal_mirror", 6));
        tooltips.add(CMLangUtil.getTranslatable("abyssal_mirror", 7));
        return tooltips;
    }
}
