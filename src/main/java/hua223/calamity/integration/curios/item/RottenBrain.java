package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DamageListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.register.entity.projectiles.ShadowsRain;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CurioRepel;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

@CurioRepel(AmalgamatedBrain.class)
public class RottenBrain extends BaseCurio implements ICuriosStorage {
    public RottenBrain(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level().isClientSide) return;
        getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.MOVEMENT_SPEED,
            new VariableAttributeModifier(uuid, "rotten", 0, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new VariableAttributeModifier(uuid, "rotten", 0, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent
    public final void onDamage(DamageListener listener) {
        if (listener.isTriggerByLiving && getCount(listener.player)[1] == 2
            && !listener.player.getCooldowns().isOnCooldown(this)) {
            ShadowsRain.of(listener.entity, listener.player, 6);
            listener.player.getCooldowns().addCooldown(this, 80);
        }
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
        var memory = getMemory(player);
        float[] count = memory.count;
        if (count[0]++ > 10) {
            count[0] = 0;
            float healthRatio = player.getHealth() / player.getMaxHealth();
            int state = healthRatio >= .75f ? 0 : healthRatio >= .5f ? 1 : 2;
            if (state != count[1]) {
                UUID uuid = memory.uuids[0];
                switch ((int) (count[1] = state)) {
                    case 0 : {
                        removeModifier(player, Attributes.MOVEMENT_SPEED, uuid);
                        removeModifier(player, CalamityAttributes.DAMAGE_UP.get(), uuid);
                    }

                    case 1 : {
                        removeModifier(player, Attributes.MOVEMENT_SPEED, uuid);
                        addModifier(player, CalamityAttributes.DAMAGE_UP.get(), uuid, 0.1);
                    }

                    case 2 : {
                        addModifier(player, Attributes.MOVEMENT_SPEED, uuid, 0.05);
                        addModifier(player, CalamityAttributes.DAMAGE_UP.get(), uuid, 0.1);
                    }
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void removeModifier(Player player, Attribute attribute, UUID uuid) {
        AttributeInstance instance = player.getAttribute(attribute);
        VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(uuid);
        modifier.setValue(0, instance);
    }

    @SuppressWarnings("ConstantConditions")
    private static void addModifier(Player player, Attribute attribute, UUID uuid, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(uuid);
        modifier.setValue(value, instance);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "rotten", 1, 2);
        return tooltips;
    }
}
