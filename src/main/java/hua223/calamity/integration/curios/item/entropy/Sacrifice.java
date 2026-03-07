package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.curios.listeners.PlayerHealListener;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Sacrifice extends Card {
    public Sacrifice(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(), new AttributeModifier(
            uuid, "sacrifice", CalamityHelp.getCalamityFlag(equipped, 10) ? 0.16 : 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onHeal(PlayerHealListener listener) {
        listener.amplification -= CalamityHelp.getCalamityFlag(listener.player, 10) ? 0.2f : 0.4f;
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("sacrifice").withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
