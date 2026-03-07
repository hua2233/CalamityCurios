package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class DynamoStemCells extends BaseCurio {
    public DynamoStemCells(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        listener.tryCancel(CalamityEffects.DRAGON_BURN.get(), CalamityEffects.ELECTRIFIED.get());
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MOVEMENT_SPEED,
            new AttributeModifier(uuid, "stem_cells", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.FAR_ATTACK.get(),
            new AttributeModifier(uuid, "stem_cells", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("stem_cells", 2));
            tooltips.add(CMLangUtil.getTranslatable("stem_cells", 3));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("stem_cells", 1));
        }
        return tooltips;
    }
}
