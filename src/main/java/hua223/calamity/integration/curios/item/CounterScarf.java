package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@ConflictChain(SprintCurio.class)
public class CounterScarf extends SprintCurio {
    public CounterScarf(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.CLOSE_RANGE.get(),
            new AttributeModifier(uuid, "scarf", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    public final void onHurt(HurtListener listener) {
        if (CalamityHelp.isCanDodge(listener.player, listener.baseAmount, 2, 600)) {
            listener.canceledEvent();
        }
    }


    @Override
    public int getTime() {
        return 4;
    }

    @Override
    public double getSpeed() {
        return 1.3;
    }

    @Override
    public int getCooldownTime() {
        return 200;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("scarf", 1));
        tooltips.add(CMLangUtil.getTranslatable("scarf", 2));
        return tooltips;
    }
}
