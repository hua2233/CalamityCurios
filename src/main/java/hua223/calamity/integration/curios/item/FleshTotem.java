package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class FleshTotem extends BaseCurio {
    public FleshTotem(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.BLOOD_MAGIC_RESIST.get(), new AttributeModifier(uuid, "flesh_totem", .1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent(250)
    public final void onHurt(HurtListener listener) {
        ItemCooldowns cooldowns = listener.player.getCooldowns();
        if (cooldowns.isOnCooldown(this)) return;

        listener.amplifier -= 0.95f;
        cooldowns.addCooldown(this, 100);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("flesh_totem").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
