package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.CriticalHitTriggerListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@CurioRepel(isRoot = true)
public class ElectriciansGlove extends BaseCurio {
    public ElectriciansGlove(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.LIGHTNING_SPELL_POWER.get(),
            new AttributeModifier(uuid, "raiders_talisman", .1, AttributeModifier.Operation.MULTIPLY_BASE));

    }

    @ApplyEvent
    public final void onCriticalHit(CriticalHitTriggerListener listener) {
        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, listener.target.level());
        bolt.setVisualOnly(true);
        bolt.setPos(listener.target.position());
        bolt.setDamage((float) listener.player.getAttributeValue(Attributes.ATTACK_DAMAGE));
        listener.addSinglePenetration(8f);
        listener.applyAmplifier(0.08f);
        if (listener.player.getHealth() < listener.player.getMaxHealth())
            listener.player.heal(2f);
        ServerLevel level = listener.player.serverLevel();
        level.addFreshEntity(bolt);
        listener.target.thunderHit(level, bolt);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "electricians_glove", 1, 2);
        return tooltips;
    }
}
