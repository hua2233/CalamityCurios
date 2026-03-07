package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.Wings;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class HarpyRing extends BaseCurio {
    public HarpyRing(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, "harpy_ring", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        Wings.flyTimeAmplifier += 0.2f;
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        Wings.flyTimeAmplifier -= 0.2f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("harpy_ring"));
        return tooltips;
    }
}
