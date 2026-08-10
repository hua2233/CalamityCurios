package hua223.calamity.integration.curios.item.entropy;

import com.google.common.collect.Multimap;
import hua223.calamity.events.ApplyEvent;
import hua223.calamity.generators.DamageMapping;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.HurtListener;
import hua223.calamity.events.listeners.PlayerAttackListener;
import hua223.calamity.events.listeners.PlayerHealListener;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.register.damage.DamageRequester;
import hua223.calamity.register.damage.DamageSupplier;
import hua223.calamity.util.*;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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

public class DeusCore extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    @DamageRequester(key = DamageMapping.BLEEDING, msg = "astr_erosion",
        style = ChatFormatting.AQUA, zh_cn = "%s被星辉侵蚀的千疮百孔")
    public static DamageSupplier supplier;

    public DeusCore(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getMemory(player).putTypeStorage(supplier.get());
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        float erosionValue = getCount(player)[1];
        if (erosionValue > 0f) {
            player.hurt(getMemory(player).getTypeStorage(DamageSource.class), erosionValue);
            getPack().putInt("v", 0);
            sendToClient(player);
        } else syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH,
            new AttributeModifier(uuid, "deus_core", 0.2, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @ApplyEvent(1000)
    public final void onHurt(HurtListener listener) {
        if (listener.isTriggerByLiving) {
            listener.amplifier += 0.2f;
            float[] astrErosion = getCount(listener.player);
            listener.canceledEvent();
            float amount = (Math.min(listener.player.getMaxHealth(), astrErosion[1]
                + listener.getCorrectionValue()));

            astrErosion[1] = amount;
            astrErosion[2] = amount / 5f;
            astrErosion[3] = amount;
            getPack().putInt("v", Mth.ceil(amount / 2f));
            sendToClient(listener.player);
        }
    }

    @ApplyEvent
    public final void onHeal(PlayerHealListener listener) {
        float[] count = getCount(listener.player);
        if (count[1] > 0) {
            int v;
            if (count[1] >= listener.healAmount) {
                v = Mth.ceil(count[1] -= listener.healAmount);
                listener.canceledEvent();
            } else {
                listener.bonus = -count[1];
                count[1] = 0f;
                v = 0;
            }

            getPack().putInt("v", v);
            sendToClient(listener.player);
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
            getPack().putInt("v", Mth.ceil((
                astrErosion[1] -= Math.min(astrErosion[1], value * 0.2f))));
            sendToClient(listener.player);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().setAstrHeart(tag.getInt("v"));
    }

    @Override
    protected void onPlayerTick(Player player) {
        var memory = getMemory(player);
        float[] astrErosion = memory.count;
        if (--astrErosion[0] <= 0) {
            astrErosion[0] = 20;
            if (astrErosion[1] > 0) {
                float hurt = Math.min(astrErosion[2], astrErosion[1]);
                player.hurt(memory.getTypeStorage(DamageSource.class), hurt);
                getPack().putInt("v", Mth.ceil((astrErosion[1] -= hurt) / 2f));
                sendToClient((ServerPlayer) player);
            }
        }
    }

    @Override
    public Class<?>[] defineStorageType() {
        return new Class[] {DamageSource.class};
    }

    @Override
    public int getCountSize() {
        return 4;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.LIGHT_PURPLE, "deus_core", 1, 2);
        return tooltips;
    }
}
