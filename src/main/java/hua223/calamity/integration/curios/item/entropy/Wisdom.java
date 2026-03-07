package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.loots.GlobalLoot;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Wisdom extends Card {
    public Wisdom(Properties properties) {
        super(properties);
        GlobalLoot.mountTo(this);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        boolean deck = equipFromDeck(stack);
        modifier.put(CalamityAttributes.MAGIC_REDUCTION.get(),
            new AttributeModifier(uuid, "wisdom",  deck ? 0.3 : 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
        modifier.put(AttributeRegistry.SPELL_POWER.get(),
            new AttributeModifier(uuid, "wisdom", deck ? 0.1 : 0.05, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyGlobalLoot
    public final void onGlobalEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityType.WITCH) && context.source.isMagic()
            && context.chance(0.4f)) context.addLoot(this, 1);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.ORACLE_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("wisdom").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
