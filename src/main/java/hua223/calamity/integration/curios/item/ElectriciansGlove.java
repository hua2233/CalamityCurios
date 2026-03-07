package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.CriticalHitListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(BloodstainedGlove.class)
public class ElectriciansGlove extends BaseCurio {
    public ElectriciansGlove(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitListener listener) {
        listener.addCallbackAfterCriticalHit(() -> {
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, listener.target.level);
            bolt.setVisualOnly(true);
            bolt.setPos(listener.target.position());
            bolt.setDamage((float) listener.player.getAttributeValue(Attributes.ATTACK_DAMAGE));
            listener.addSinglePenetration(8f);
            listener.applyAmplifier(0.08f);
            if (listener.player.getHealth() < listener.player.getMaxHealth())
                listener.player.heal(2f);
            ServerLevel level = listener.player.getLevel();
            level.addFreshEntity(bolt);
            listener.target.thunderHit(level, bolt);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("electricians_glove", 1));
        tooltips.add(CMLangUtil.getTranslatable("electricians_glove", 2));
        return tooltips;
    }
}
