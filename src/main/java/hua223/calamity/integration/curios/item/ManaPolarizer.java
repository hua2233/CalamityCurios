package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class ManaPolarizer extends BaseCurio implements ICuriosStorage {
    public ManaPolarizer(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        CalamityCapProvider.safetyRunCalamityMagic(listener.player, expand -> {
            float amplifier =  expand.calamity$GetMana() /
                (float) listener.player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
            float baseValue = listener.baseAmount / 2;
            listener.player.heal(baseValue * amplifier);
        });
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(AttributeRegistry.MAX_MANA.get(),
            new AttributeModifier(uuid, "mana_polarizer", 50, AttributeModifier.Operation.ADDITION));
        modifier.put(AttributeRegistry.LIGHTNING_SPELL_POWER.get(),
            new AttributeModifier(uuid, "mana_polarizer", 0.1f, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) > 400) {
            zeroCount(player, 0);
            CalamityCapProvider.safetyRunCalamityMagic(player, expand -> {
                if (expand.calamity$GetMana() >= player.getAttributeValue(AttributeRegistry.MAX_MANA.get()) / 2)
                    player.hurt(DamageSource.MAGIC, (float) (player.getMaxHealth() * 0.05));
            });
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("mana_polarizer", 1));
        tooltips.add(CMLangUtil.getTranslatable("mana_polarizer", 2));
        tooltips.add(CMLangUtil.getTranslatable("mana_polarizer", 3));
        return tooltips;
    }
}
