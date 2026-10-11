package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.ChestLootContext;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@JeiInfo(zh_cn = "在丛林神庙的战利品中可以找到")
public class Endurance extends Card {
    public Endurance(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        boolean deck = equipped.calamity$Player.Calamity$Player.cardDeck;
        modifier.put(Attributes.ARMOR,
            new AttributeModifier(uuid, "endurance", deck ? 8 : 5, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(uuid, "endurance", deck ? 4 : 2, AttributeModifier.Operation.ADDITION));
        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new AttributeModifier(uuid, "endurance", deck ? .06 : 0.04, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyGlobalLoot
    public void onGlobalChestLoot(ChestLootContext context) {
        if (context.fromFuzzyType("jungle_temple") && context.chance(0.3f))
            context.addLoot(this);
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.ORACLE_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("endurance").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
