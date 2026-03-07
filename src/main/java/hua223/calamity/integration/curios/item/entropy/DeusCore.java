package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.DeathListener;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.integration.curios.listeners.PlayerAttackListener;
import hua223.calamity.integration.curios.listeners.PlayerHealListener;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.AstrErosionSync;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityDamageSource;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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

public class DeusCore extends BaseCurio implements ICuriosStorage {
    public DeusCore(Properties properties) {
        super(properties);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        float erosionValue = getCount(player)[1];
        if (erosionValue > 0f) {
            player.calamity$HurtNoInvulnerable(new CalamityDamageSource("astr_erosion")
                .setNoDecay(erosionValue).setNotTriggerEvent().setStyle(ChatFormatting.AQUA)
                .bypassArmor().bypassMagic().bypassEnchantments().bypassInvul(), erosionValue);
            NetMessages.sendToClient(new AstrErosionSync(0), player);
        } else syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH,
            new AttributeModifier(uuid, "deus_core", 0.2, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent(1000)
    public final void onHurt(HurtListener listener) {
        listener.amplifier += 0.2f;
        if (listener.isTriggerByLiving) {
            float[] astrErosion = getCount(listener.player);
            listener.canceledEvent();
            float amount = (Math.min(listener.player.getMaxHealth(), astrErosion[1]
                + listener.getCorrectionValue() - listener.player.getAbsorptionAmount()));

            astrErosion[1] = amount;
            astrErosion[2] = amount / 5f;
            astrErosion[3] = amount;
            NetMessages.sendToClient(new AstrErosionSync(Mth.ceil(amount / 2f)), listener.player);
        }
    }

    @ApplyEvent
    public final void onHeal(PlayerHealListener listener) {
        float[] count = getCount(listener.player);
        if (count[1] > 0) {
            if (count[1] >= listener.healAmount) {
                NetMessages.sendToClient(new AstrErosionSync(Mth.ceil(count[1] -= listener.healAmount)), listener.player);
                listener.canceledEvent();
            } else {
                listener.bonus = -count[1];
                count[1] = 0f;
                NetMessages.sendToClient(new AstrErosionSync(0), listener.player);
            }
        }
    }

    @ApplyEvent
    public final void onAttack(PlayerAttackListener listener) {
        float[] astrErosion = getCount(listener.player);
        if (astrErosion[1] > 0f) {
            float maxErosion = listener.player.getMaxHealth();
            float t = Math.min(astrErosion[3] / maxErosion, 1.0f);
            float a = 2.5f;
            float value = listener.baseAmount * (float)  (1.0 + (Math.sqrt(maxErosion) *
                (Math.log(1 + a * t) / Math.log(1 + a)))) - listener.baseAmount;
            listener.floating += value;
            NetMessages.sendToClient(new AstrErosionSync(Mth.ceil((
                astrErosion[1] -= Math.min(astrErosion[1], value * 0.2f)))), listener.player);
        }
    }

    @ApplyEvent
    public final void onDeath(DeathListener listener) {
        if (listener.isPlayerDeath) {
            getCount(listener.player)[1] = 0;
            NetMessages.sendToClient(new AstrErosionSync(0), listener.player);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onLogOut(Player player) {
        if (player.isLocalPlayer()) {
            RenderUtil.astrAmount = 0;
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] astrErosion = getCount(player);
        if (--astrErosion[0] <= 0) {
            astrErosion[0] = 20;
            if (astrErosion[1] > 0) {
                float hurt = Math.min(astrErosion[2], astrErosion[1]);
                player.calamity$HurtNoInvulnerable(new CalamityDamageSource("astr_erosion")
                    .setNoDecay(hurt).setStyle(ChatFormatting.AQUA).setNotTriggerEvent().bypassArmor()
                    .bypassMagic().bypassEnchantments().bypassInvul(), hurt);
                NetMessages.sendToClient(new AstrErosionSync(Mth.ceil((astrErosion[1] -= hurt) / 2f)), (ServerPlayer) player);
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    public int getCountSize() {
        return 4;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("deus_core", 1).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltips.add(CMLangUtil.getTranslatable("deus_core", 2).withStyle(ChatFormatting.LIGHT_PURPLE));
        return tooltips;
    }
}
