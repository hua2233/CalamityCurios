package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class VexationNecklace extends BaseCurio implements ICuriosStorage {
    public VexationNecklace(Properties properties) {
        super(properties);
    }

    @ApplyEvent
    public static void onAttack(PlayerAttackListener listener) {
        MobEffect effect = CalamityEffects.ACID_VENOM.get();
        if (listener.entity.hasEffect(effect)) return;
        listener.entity.addEffect(new MobEffectInstance(effect, 80));
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level.isClientSide) return;
        getUUID(equipped)[0] = uuid;
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new VariableAttributeModifier(uuid, "rotten", 0, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) > 20) {
            var p = getPair(player);
            float[] count = p.getA();
            count[0] = 0;
            if (player.getHealth() >= player.getMaxHealth() / 2) {
                if (count[1] == 0) {
                    VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.DAMAGE_UP.get()), p.getB()[0], 0.2);
                    count[1] = 1;
                }
            } else if (count[1] == 1){
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(CalamityAttributes.DAMAGE_UP.get()), p.getB()[0], 0);
                count[1] = 0;
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @ApplyEvent
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("vexation"));
        return tooltips;
    }
}
