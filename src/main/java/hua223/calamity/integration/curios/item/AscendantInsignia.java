package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.Wings;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
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

public class AscendantInsignia extends BaseCurio {
    public AscendantInsignia(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "ascendant_insignia", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        Wings.flyTimeAmplifier += 0.3f;
        CalamityHelp.setKeyEvent(player, "INSIGNIA_FLY", true);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        Wings.flyTimeAmplifier -= 0.3f;
        CalamityHelp.setKeyEvent(player, "INSIGNIA_FLY", false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ascendant_insignia", 1));
        tooltips.add(CMLangUtil.getTranslatable("ascendant_insignia", 2));
        tooltips.add(CMLangUtil.getTranslatable("ascendant_insignia", 3));
        return tooltips;
    }
}
