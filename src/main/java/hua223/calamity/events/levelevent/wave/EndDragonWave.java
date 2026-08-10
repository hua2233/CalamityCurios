package hua223.calamity.events.levelevent.wave;

import hua223.calamity.events.levelevent.BossRushEvent;
import hua223.calamity.util.delaytask.DelayRunnable;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL;

public class EndDragonWave extends BossRushBossWave {
    private boolean crystalReinforcement;
    public EndDragonWave(BossRushEvent event) {
        super(event);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void spawnWave() {
        ServerLevel end = event.level;
        EndDragonFight fight = end.getDragonFight();
        EnderDragon d = (EnderDragon) end.getEntity(fight.getDragonUUID());
        if (d == null) {
            BlockPos center = fight.portalLocation.above(1);
            List<EndCrystal> crystals = new ArrayList<>(4);
            for(Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos spawn = center.relative(direction, 3);
                EndCrystal crystal = new EndCrystal(end, spawn.getX() + 0.5F, spawn.getY(), spawn.getZ() + 0.5F);
                crystal.setShowBottom(false);
                crystal.setInvulnerable(true);
                crystals.add(crystal);
                end.addFreshEntity(crystal);
            }
            fight.tryRespawn();
            int[] tick = {0};

            DelayRunnable.conditionsLoop(() -> {
                if (!event.inProgress()) {
                    for (EndCrystal crystal : crystals) crystal.kill();
                    return false;
                }


                EnderDragon dragon = (EnderDragon) end.getEntity(fight.getDragonUUID());
                if (dragon != null) {
                    setEntryBossEntity(dragon);
                    for (EndCrystal crystal : crystals)
                        crystal.kill();

                    //Boss Rush Reinforcement
                    applyBossAttribute(dragon, new AttributeContainer(() -> Attributes.MAX_HEALTH, 3, MULTIPLY_TOTAL),
                        new AttributeContainer(() -> Attributes.MOVEMENT_SPEED, 0.2, MULTIPLY_TOTAL),
                        new AttributeContainer(() -> Attributes.ARMOR, dragon.getAttributeValue(Attributes.MAX_HEALTH) / 2, ADDITION));


                    dragon.heal(dragon.getMaxHealth());
                    for(SpikeFeature.EndSpike endspike : SpikeFeature.getSpikesForLevel(end))
                        for (EndCrystal crystal : end.getEntitiesOfClass(EndCrystal.class, endspike.getTopBoundingBox()))
                            crystal.setInvulnerable(true);

                    crystalReinforcement = true;
                    return true;
                } else if (++tick[0] == 10) {
                    fight.tryRespawn();
                    tick[0] = 0;
                }

                return false;
            }, 10);
        } else {
            //如果龙仍然存在那么她将会狂暴且不会再将晶体设置为无敌，而是获得免伤
            applyBossAttribute(d, new AttributeContainer(() -> Attributes.MAX_HEALTH, 5, MULTIPLY_TOTAL),
                new AttributeContainer(() -> Attributes.MOVEMENT_SPEED, 0.4, MULTIPLY_TOTAL),
                new AttributeContainer(() -> Attributes.ARMOR, d.getAttributeValue(Attributes.MAX_HEALTH), ADDITION));
            d.heal(d.getMaxHealth());
            d.calamity$ForciblyAddEffect(new MobEffectInstance(MobEffectRegistry.OAKSKIN.get(), Integer.MAX_VALUE, 9), null);
            setEntryBossEntity(d);
        }
    }

    @Override
    public boolean mustWait() {
        return true;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected Vec3 getNewWorldVenueCenter(ServerLevel level) {
        EndDragonFight fight = level.getDragonFight();
        if (fight.portalLocation == null) {
            fight.skipArenaLoadedCheck = true;
            fight.tick();
            fight.skipArenaLoadedCheck = false;
            fight.spawnExitPortal(false);
        }

        return fight.portalLocation.above(2).getCenter();
    }

    @Override
    protected ResourceKey<Level> getVenueWorld() {
        return Level.END;
    }

    @Override
    protected int getVenueSize() {
        return 180;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onTerminationEvent() {
        //如果事件失败或终止，非BossRush召唤的龙仍然会存在
        EnderDragon dragon = (EnderDragon) getBoss();
        EndDragonFight fight = event.level.getDragonFight();
        if (fight.hasPreviouslyKilledDragon()) fight.spawnExitPortal(true);
        if (dragon != null) {
            if (crystalReinforcement) {
                dragon.discard();
                eliminateReinforcement(event.level);
                fight.scanState();
            } else {
                dragon.removeEffect(MobEffectRegistry.OAKSKIN.get());
                UUID id = getEventUUID();
                for (Attribute attribute : List.of(Attributes.MAX_HEALTH, Attributes.MOVEMENT_SPEED, Attributes.ARMOR))
                    dragon.getAttribute(attribute).removeModifier(id);
            }
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onKillDrop(LivingDropsEvent event) {
        if (crystalReinforcement)
            eliminateReinforcement(this.event.level);
    }

    @Override
    public int getRestTime() {
        return 400;
    }

    private static void eliminateReinforcement(ServerLevel end) {
        for(SpikeFeature.EndSpike endspike : SpikeFeature.getSpikesForLevel(end))
            for (EndCrystal crystal : end.getEntitiesOfClass(EndCrystal.class, endspike.getTopBoundingBox()))
                crystal.kill();
    }

    @Override
    public int getWaveLevel() {
        return 2;
    }
}
