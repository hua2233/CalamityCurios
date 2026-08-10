package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.events.listeners.EffectListener;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

public class AzureAbyssTalisman extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    public AzureAbyssTalisman(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.azureAbyssFlag = 2;
        getPack().putInt("flag", 2);
        sendToClient(player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.azureAbyssFlag = 1;
        getPack().putInt("flag", 1);
        sendToClient(player);
    }

    @ApplyEvent
    @SuppressWarnings("ConstantConditions")
    public final void onHurt(HurtListener listener) {
        if (listener.getCorrectionValue() > listener.player.getMaxHealth() * 0.35f) {
            ItemCooldowns cooldowns = listener.player.getCooldowns();
            var memory = getMemory(listener.player);
            boolean takeEffect = memory.count[1] > 0;
            if (cooldowns.isOnCooldown(this) && takeEffect)
                listener.amplifier -= 0.25f;
            else if (!takeEffect){
                getPack().putInt("flag", 3);
                sendToClient(listener.player);
                memory.count[1] = 160;
                VariableAttributeModifier.updateModifierInInstance(listener.player.getAttribute(
                    ForgeMod.SWIM_SPEED.get()), memory.uuids[0], 1.5f);
            }
        }
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        if (!listener.effect.isBeneficial() && getCount(listener.player)[0] > 0)
            listener.canceledEvent();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().azureAbyssFlag = tag.getInt("flag");
    }

    @Override 
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR,
                new AttributeModifier(uuid, "azure_abyss_talisman", 15, AttributeModifier.Operation.ADDITION));
        modifier.put(Attributes.ARMOR_TOUGHNESS,
            new AttributeModifier(uuid, "azure_abyss_talisman", 6, AttributeModifier.Operation.ADDITION));
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "azure_abyss_talisman", 0.15, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(ForgeMod.SWIM_SPEED.get(),
            new VariableAttributeModifier(uuid, "azure_abyss_talisman", 0.75, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        float[] count = memory.count;
        if (++count[0] == 40) {
            count[0] = 0;
            if (player.getHealth() < player.getMaxHealth())
                player.heal(4f);
        }

        if (count[1] > 0 && --count[1] == 0) {
            getPack().putInt("flag", 2);
            sendToClient((ServerPlayer) player);
            VariableAttributeModifier.updateModifierInInstance(player.getAttribute(
                ForgeMod.SWIM_SPEED.get()), memory.uuids[0], 0.75f);
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
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "azure_abyss_talisman", 1, 2, 3);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("azure_abyss_talisman").withStyle(ChatFormatting.AQUA));
        return tooltips;
    }
}
