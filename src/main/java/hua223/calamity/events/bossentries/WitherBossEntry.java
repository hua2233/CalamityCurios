package hua223.calamity.events.bossentries;

import hua223.calamity.events.BossRushBossEntry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL;

public class WitherBossEntry extends BossRushBossEntry {
    @Override
    @SuppressWarnings("ConstantConditions")
    public void spawnBoss(ServerPlayer player) {
        WitherBoss wither = EntityType.WITHER.create(player.level());
        wither.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition());
        wither.makeInvulnerable();
        wither.setTarget(player);
        applyBoss(wither, player, new AttributeContainer(() -> Attributes.MAX_HEALTH, 0.8, MULTIPLY_TOTAL),
            new AttributeContainer(() -> Attributes.ATTACK_DAMAGE, 0.6, MULTIPLY_TOTAL),
            new AttributeContainer(() -> Attributes.MOVEMENT_SPEED, 0.4, MULTIPLY_TOTAL),
            new AttributeContainer(() -> Attributes.ARMOR, wither.getAttributeValue(Attributes.MAX_HEALTH) / 3, ADDITION));
        wither.heal(wither.getMaxHealth());
        setEntryBossEntity(wither);
    }

    @Override
    public byte getThreatLevel() {
        return 1;
    }
}
