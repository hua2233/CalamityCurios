package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.IDataPackResponse;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
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

public class PermafrostConcoction extends BaseCurio implements ICuriosStorage, IDataPackResponse {
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

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath) {
            ServerPlayer player = listener.player;
            if (player.getCooldowns().isOnCooldown(this)) return;

            player.setHealth((float) (player.getMaxHealth() * 0.4));
            player.addEffect(new MobEffectInstance(CalamityEffects.FREEZE.get(), 140, 0));
            player.getCooldowns().addCooldown(this, 3600);
            listener.canceledEvent();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().freeze = tag.getBoolean("flag");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        Style style = Style.EMPTY.withColor(ChatFormatting.GOLD);
        tooltips.add(CMLangUtil.getTranslatable("concoction", 1).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("concoction", 2).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("concoction", 3).setStyle(style));
        tooltips.add(CMLangUtil.getTranslatable("concoction", 4).withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
