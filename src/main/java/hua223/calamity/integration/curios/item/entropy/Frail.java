package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.loots.ApplyGlobalLoot;
import hua223.calamity.loots.EntitiesLootContext;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
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

@JeiInfo(zh_cn = "远古骑士小概率掉落，受抢夺影响")
public class Frail extends Card {
    public Frail(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "frail", equipped.calamity$Player.Calamity$Player.cardDeck ? 0.25f : 0.15f, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onDamage(DamageListener listener) {
        listener.amplifier += 0.15f;
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @ApplyGlobalLoot
    public void onEntityLoot(EntitiesLootContext context) {
        if (context.onlyVerification(EntityRegistry.KEEPER.get())
            && context.lootingChance(.05f)) context.addLoot(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("frail", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("frail", 2).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
