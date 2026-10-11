package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.CurioRepel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@CurioRepel(Baroclaw.class)
@JeiInfo(zh_cn = "杀死溺尸小概率掉落")
public class CrawCarapace extends BaseCurio {
    public CrawCarapace(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public final void onDamage(DamageListener listener) {
        if (listener.isTriggerByLiving) {
            listener.entity.hurt(listener.player.damageSources().generic(), listener.baseAmount * 0.45f + 2f);
            CalamityHelp.addIfDoesNotExist(listener.entity, 100, 0,
                CalamityEffects.CRUMBLING.get());
        }
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "craw", 0.07, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyGlobalLoot
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.DROWNED) && context.chance(.08f))
            context.addLoot(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("craw"));
        return tooltips;
    }
}
