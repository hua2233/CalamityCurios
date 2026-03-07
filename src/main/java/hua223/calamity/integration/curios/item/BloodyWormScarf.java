package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
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

import static hua223.calamity.register.Items.CalamityItems.BLOODY_WORM_TOOTH;

public class BloodyWormScarf extends BaseCurio {
    public BloodyWormScarf(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!BLOODY_WORM_TOOTH.isEquip(equipped)) {
            modifier.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(uuid, "attack_bonus", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "armor_bonus", 7, AttributeModifier.Operation.ADDITION));

        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new AttributeModifier(uuid, "injury_free_bonus", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("worm_scarf"));
        return tooltips;
    }
}
