package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@JeiInfo(zh_cn = "在穿刺破冰船的战利品箱中可能找到")
public class FrostBarrier extends BaseCurio {
    public FrostBarrier(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.ICE_MAGIC_RESIST.get(), new AttributeModifier(uuid, "frost_barrier", .15f, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onDamage(DamageListener listener) {
        if (listener.isTriggerByLiving) {
            int duration = (int) (300 * Math.min(1f, listener.baseAmount / listener.player.getMaxHealth()));

            MobEffect effect = CalamityEffects.GLACIAL_STATE.get();
            MobEffectInstance instance = listener.entity.getEffect(effect);

            if (instance == null) listener.entity.addEffect(new MobEffectInstance(effect, duration));
            else if (instance.getDuration() < duration) instance.calamity$SetProperties(duration, instance.getAmplifier(), listener.entity);
        }
    }

    @ApplyGlobalLoot
    public void onChestLoot(ChestLootContext context) {
        if (context.fromFuzzyType("impaled_icebreaker") && context.chance(0.12f))
            context.addLoot(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("frost_barrier"));
        return tooltips;
    }
}
