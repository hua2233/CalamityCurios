package hua223.calamity.events.levelevent.wave;

import hua223.calamity.events.levelevent.BossRushEvent;
import hua223.calamity.util.delaytask.DelayRunnable;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL;

public class WardenBossWave extends BossRushBossWave {
    public WardenBossWave(BossRushEvent event) {
        super(event);
    }

    @Override
    public void spawnWave() {
        BooleanSupplier supplier = () -> {
            if (!event.inProgress()) return false;

            Optional<Warden> optional = SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, event.level,
                new BlockPos((int) event.original.x, (int) event.original.y, (int) event.original.z),
                40, 10, 10, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER);
            if (optional.isPresent()) {
                Warden warden = optional.get();
                warden.increaseAngerAt(event.level.getNearestPlayer(warden, getVenueSize()), 100000, true);
                applyBossAttribute(warden,new AttributeContainer(() -> Attributes.MAX_HEALTH, 0.5, MULTIPLY_TOTAL),
                    new AttributeContainer(() -> Attributes.ATTACK_DAMAGE, 0.3, MULTIPLY_TOTAL),
                    new AttributeContainer(() -> Attributes.MOVEMENT_SPEED, 0.3, MULTIPLY_TOTAL),
                    new AttributeContainer(() -> Attributes.ARMOR, warden.getAttributeValue(Attributes.MAX_HEALTH) / 4, ADDITION));
                warden.heal(warden.getMaxHealth());
                setEntryBossEntity(warden);
            }
            return optional.isPresent();
        };

        if (!supplier.getAsBoolean())
            DelayRunnable.conditionsLoop(supplier, 10);
    }

    @Override
    public int getWaveLevel() {
        return 0;
    }
}
