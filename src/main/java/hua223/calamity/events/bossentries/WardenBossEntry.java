package hua223.calamity.events.bossentries;

import hua223.calamity.events.BossRushBossEntry;
import hua223.calamity.events.BossRushEvent;
import hua223.calamity.util.delaytask.DelayRunnable;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL;

public class WardenBossEntry extends BossRushBossEntry {
    @Override
    public void spawnBoss(ServerPlayer player) {
        BooleanSupplier supplier = () -> {
            if (!BossRushEvent.isBossRushEventActivating()) return false;

            Optional<Warden> optional = SpawnUtil.trySpawnMob(EntityType.WARDEN, MobSpawnType.TRIGGERED, player.serverLevel(),
                getCenterSpawn(player), 40, 10, 10, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER);
            if (optional.isPresent()) {
                Warden warden = optional.get();
                warden.increaseAngerAt(player, 100000, true);
                applyBoss(warden, player,   new AttributeContainer(() -> Attributes.MAX_HEALTH, 0.5, MULTIPLY_TOTAL),
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
    public byte getThreatLevel() {
        return 0;
    }
}
