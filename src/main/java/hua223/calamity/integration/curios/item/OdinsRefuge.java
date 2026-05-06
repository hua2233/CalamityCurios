package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.register.effects.CalamityEffect;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import top.theillusivec4.curios.api.SlotContext;

import java.util.List;
import java.util.UUID;

@ConflictChain(value = SprintCurio.class, node = DeitiesRampart.class)
public class OdinsRefuge extends AsgardianAegis implements ICuriosStorage {
    public OdinsRefuge(Properties properties) {
        super(properties);
    }

    @ApplyEvent(180)
    public final void getEffects(EffectListener listener) {
        listener.tryCancelHarmfulOnes(2f);
    }

    @ApplyEvent(195)
    public final void hurt(HurtListener listener) {
        float[] count = getCount(listener.player);
        if (count[0] < 1) {
            listener.canceledEvent();
            count[0] = 400;
        }

        if (count[1] < 1) {
            listener.player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
            listener.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
            listener.entity.addEffect(new MobEffectInstance(CalamityEffects.ASTRAL_INFECTION.get(), 200, 1));
            count[1] = 200;
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        super.onEquip(slotContext, prevStack, stack);
        ((BaseCurio) CalamityItems.DEITIES_RAMPART.get()).onEquip(slotContext, null, null);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);
        ((BaseCurio) CalamityItems.DEITIES_RAMPART.get()).onUnequip(slotContext, null, null);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "odins_refuge", 9.99, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(Attributes.ARMOR, new AttributeModifier(uuid, "odins_refuge", 24, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "odins_refuge", 12, AttributeModifier.Operation.ADDITION));
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        count[0]--;
        count[1]--;
    }

    @Override
    public int getCooldownTime() {
        return 180;
    }

    @Override
    public double getSpeed() {
        return 1.7;
    }

    @Override
    public int getTime() {
        return 8;
    }

    @Override
    public int getCountSize() {
        return 2;
    }

    @Override
    public void onCollision(ServerPlayer player, LivingEntity target) {
        CalamityHelp.addIfDoesNotExist(target, 400, 2, CalamityEffects.GOD_SLAYER_INFERNO.get());
        immuneSprint(player, target, 20, 30);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.LIGHT_PURPLE, "odins_refuge", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        return tooltips;
    }
}
