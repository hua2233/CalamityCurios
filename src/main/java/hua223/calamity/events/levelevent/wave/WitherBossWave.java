package hua223.calamity.events.levelevent.wave;

import hua223.calamity.events.levelevent.BossRushEvent;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL;

public class WitherBossWave extends BossRushBossWave {
    public WitherBossWave(BossRushEvent event) {
        super(event);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void spawnWave() {
        WitherBoss wither = EntityType.WITHER.create(event.level);
        setEntryBossEntity(wither);
        spawnToWorld(8);
        Player player = event.level.getNearestPlayer(wither, getVenueSize());
        wither.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
        wither.makeInvulnerable();
        wither.setTarget(player);
        wither.heal(wither.getMaxHealth());
        applyBossAttribute(wither,new AttributeContainer(() -> Attributes.MAX_HEALTH, 0.8, MULTIPLY_TOTAL),
            new AttributeContainer(() -> Attributes.ATTACK_DAMAGE, 0.6, MULTIPLY_TOTAL),
            new AttributeContainer(() -> Attributes.MOVEMENT_SPEED, 0.4, MULTIPLY_TOTAL),
            new AttributeContainer(() -> Attributes.ARMOR, wither.getAttributeValue(Attributes.MAX_HEALTH) / 3, ADDITION));
    }

    @Override
    public int getWaveLevel() {
        return 1;
    }
}
