package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

@JeiInfo(zh_cn = "亡灵术士被火焰伤害杀死时概率掉落")
public class Fool extends Card {
    public Fool(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        boolean deck = equipped.calamity$Player.Calamity$Player.cardDeck;
        modifier.put(CalamityAttributes.MAGIC_REDUCTION.get(),
            new AttributeModifier(uuid, "fool", deck ? -0.15 : -0.2, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(AttributeRegistry.SPELL_POWER.get(),
            new AttributeModifier(uuid, "fool", deck ? 0.4 : 0.3, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @ApplyGlobalLoot(onlyPlayer = false)
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityRegistry.NECROMANCER.get()) && context.isDamageType(
            DamageTypeTags.IS_FIRE) && context.chance(0.3f)) context.addLoot(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("fool").withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
