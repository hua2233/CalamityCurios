package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.DeathListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.entity.GladiatorHealOrb;
import hua223.calamity.util.CMLangUtil;
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

public class GladiatorLocket extends BaseCurio implements ICuriosStorage {
    public GladiatorLocket(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "gladiator_locket", 3, AttributeModifier.Operation.ADDITION));
        VariableAttributeModifier general = new VariableAttributeModifier(uuid, "gladiator_locket", 0, AttributeModifier.Operation.MULTIPLY_BASE);
        modifier.put(Attributes.MOVEMENT_SPEED, general);
        modifier.put(Attributes.ATTACK_DAMAGE, general);

    }

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (!listener.isPlayerDeath) GladiatorHealOrb.create(listener.entity);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);

        if (++count[0] == 20) {
            count[0] = 0;
            float maxHealth = player.getMaxHealth();
            float currentHealth = player.getHealth();
            float healthLost = Math.max(0f, maxHealth - currentHealth);
            float threshold = maxHealth * 0.8f;

            float v = Math.min(0.2f, (healthLost / threshold) * 0.2f);
            if (Math.abs(v - count[1]) > 0.01f) {
                count[1] = v;
                AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
                VariableAttributeModifier modifier = (VariableAttributeModifier) move.getModifier(getFirstUUID(player));

                if (modifier.getAmount() != v) {
                    modifier.setValue(v, move);
                    modifier.setValue(v, player.getAttribute(Attributes.ATTACK_DAMAGE));
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.YELLOW, "gladiator_locket", 1, 2);
        return tooltips;
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @Override
    public boolean storageID() {
        return true;
    }
}
