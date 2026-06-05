package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.events.listeners.PlayerHealListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.GlobalCuriosStorage;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class FrostFlare extends BaseCurio implements ICuriosStorage {
    public FrostFlare(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("ConstantConditions")
    private static void onHealthChange(ServerPlayer player, GlobalCuriosStorage.CuriosMemory memory) {
        float heal = player.getHealth();
        float half = player.getMaxHealth() / 2;

        double[] amount;
        if (heal > half) {
            if (memory.count[2] == 0) return;
            memory.count[2] = 0;
            amount = new double[] {0.1, 0, 0};
        } else {
            if (memory.count[2] == 1) return;
            memory.count[2] = 1;
            amount = new double[] {0, 0.15, 10};
        }

        UUID uuid = memory.uuids[0];
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.ATTACK_DAMAGE), uuid, amount[0]);
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.MOVEMENT_SPEED), uuid, amount[1]);
        VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.ARMOR), uuid, amount[2]);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        onHealthChange(player, getMemory(player));
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        if (!listener.entity.hasEffect(MobEffects.MOVEMENT_SLOWDOWN))
            listener.entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        onHealthChange(listener.player, getMemory(listener.player));
        if (listener.source.is(DamageTypeTags.IS_FREEZING)) listener.amplifier -= 0.4f;
    }

    @ApplyEvent
    public final void onHeal(PlayerHealListener listener) {
        onHealthChange(listener.player, getMemory(listener.player));
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level().isClientSide) return;
        getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.MOVEMENT_SPEED, new VariableAttributeModifier(uuid, "frost_flare", 0, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ATTACK_DAMAGE, new VariableAttributeModifier(uuid, "frost_flare", 0, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(uuid, "frost_flare", 0, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (count[2] == 1 && count[0]++ == 10) {
            count[0] = 0;
            if (player.isSprinting()) {
                if ( count[1] < 0.3f) VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.MOVEMENT_SPEED), getFirstUUID(player), count[1] += 0.03f);
            } else if (count[1] != 0) {
                count[1] = 0;
                //Restore to basic values
                VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.MOVEMENT_SPEED), getFirstUUID(player), 0.15);
            }
        }
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.AQUA, "frost_flare", 1, 2, 3, 4);
        return tooltips;
    }
}
