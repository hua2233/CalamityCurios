package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.entity.projectiles.ShadowsRain;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class RottenBrain extends BaseCurio implements ICuriosStorage {
    public RottenBrain(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level.isClientSide) return;
        getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.MOVEMENT_SPEED,
            new VariableAttributeModifier(uuid, "rotten", -0.05, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new VariableAttributeModifier(uuid, "rotten", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        ServerPlayer player = listener.player;
        if (player.getCooldowns().isOnCooldown(this)) return;

        if (listener.isTriggerByLiving) {
            if (player.getHealth() > player.getMaxHealth() / 2f) {
                ShadowsRain.of(listener.entity, player, 6);
                player.getCooldowns().addCooldown(this, 80);
            }
        }
    }

    @Override
    public int getCountSize() {
        return 3;
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
            double health = player.getHealth();
            double maxHealth = player.getMaxHealth();

            if (health >= maxHealth * 0.75f) {
                removeModifier(player, Attributes.MOVEMENT_SPEED, count, 1, p.getB()[0]);
                removeModifier(player, CalamityAttributes.DAMAGE_UP.get(), count, 2, p.getB()[0]);
            } else if (health >= maxHealth / 2f) {
                removeModifier(player, Attributes.MOVEMENT_SPEED, count, 1, p.getB()[0]);
                addModifier(player, CalamityAttributes.DAMAGE_UP.get(), count, 2, p.getB()[0], 0.1);
            } else {
                addModifier(player, Attributes.MOVEMENT_SPEED, count, 1, p.getB()[0], 0.05);
                addModifier(player, CalamityAttributes.DAMAGE_UP.get(), count, 2, p.getB()[0], 0.1);
            }
        }
    }

    private static void removeModifier(Player player, Attribute attribute, float[] count, int flag, UUID uuid) {
        if (count[flag] == 0) {
            AttributeInstance instance = player.getAttribute(attribute);
            VariableAttributeModifier modifier = VariableAttributeModifier.getModifierInInstance(instance, uuid);
            modifier.setValue(0, instance);
            count[flag] = 1;
        }
    }

    private static void addModifier(Player player, Attribute attribute, float[] count, int flag, UUID uuid, double value) {
        if (count[flag] == 1) {
            AttributeInstance instance = player.getAttribute(attribute);
            VariableAttributeModifier modifier = VariableAttributeModifier.getModifierInInstance(instance, uuid);
            modifier.setValue(value, instance);
            count[flag] = 0;
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("rotten", 1));
        tooltips.add(CMLangUtil.getTranslatable("rotten", 2));
        return tooltips;
    }
}
