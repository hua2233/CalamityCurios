package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.PostAttackListener;
import hua223.calamity.events.listeners.PreAttackListener;
import hua223.calamity.integration.curios.Card;
import hua223.calamity.integration.jei.JeiInfo;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Nothing extends Card {
    public Nothing(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.ELDRITCH_SPELL_POWER.get(), new AttributeModifier(uuid, "nothing",
            equipped.calamity$Player.Calamity$Player.cardDeck ? .2 : .1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onAttack(PostAttackListener listener) {
        MobEffect effect = CalamityEffects.VOID_TOUCH.get();
        boolean flag = listener.player.Calamity$Player.cardDeck;
        MobEffectInstance instance = listener.entity.getEffect(effect);
        if (instance != null) {
            int amplifier = instance.getAmplifier();
            if (amplifier < 2 || (flag && amplifier < 5))
                instance.calamity$SetProperties(flag ? 100 : 60, ++amplifier, listener.entity);

        } else listener.entity.addEffect(new MobEffectInstance(effect, flag ? 100 : 60, 0));
    }

    @Override
    protected Item getAffiliatedWith() {
        return CalamityItems.TAINTED_DECK.get();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("nothing", 1).withStyle(ChatFormatting.GOLD));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("nothing", 2).withStyle(ChatFormatting.DARK_PURPLE));
        return tooltips;
    }
}
