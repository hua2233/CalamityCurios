package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class PermafrostConcoction extends BaseCurio implements ICuriosStorage {
    public PermafrostConcoction(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.ICE_SPELL_POWER.get(),
            new AttributeModifier(uuid, "concoction", 0.3f, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(AttributeRegistry.MAX_MANA.get(),
            new AttributeModifier(uuid, "concoction", 50f, AttributeModifier.Operation.ADDITION));
        modifier.put(CalamityAttributes.MAGIC_REDUCTION.get(),
            new AttributeModifier(uuid, "concoction", 0.15f, AttributeModifier.Operation.MULTIPLY_BASE));
    }


    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 100) {
            zeroCount(player, 0);

            float heal_value = 0;
            float max_health = player.getMaxHealth();
            float health = player.getHealth();

            if (health < max_health / 2 && health > max_health / 4) {
                heal_value += 0.5f;
            } else if (health < max_health / 4 && health > max_health / 10) {
                heal_value += 1;
            } else if (health < max_health / 10) {
                heal_value += 2;
            }

            if (player.isOnFire()) heal_value += 3;

            if (heal_value > 0) player.heal(heal_value);
        }
    }

    @ApplyEvent(95)
    public final void onDeath(DeathListener listener) {
        if (listener.canceledPlayerDeathIfNotCooldowns(this, .4f, 2400, 5636095, 5592575, 170, 43690, 0x1111EE))
            listener.player.addEffect(new MobEffectInstance(CalamityEffects.FREEZE.get(), 140, 0));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "concoction", 1, 2, 3);
        tooltips.add(CMLangUtil.getTranslatable("concoction", 4).withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
