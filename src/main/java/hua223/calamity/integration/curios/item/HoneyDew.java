package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.EffectListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
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

public class HoneyDew extends BaseCurio implements ICuriosStorage {
    public HoneyDew(Properties pProperties) {
        super(pProperties);
    }

    @ApplyEvent
    public final void onGetEffect(EffectListener listener) {
        if (!listener.effect.isBeneficial()) {
            listener.instance.calamity$SetDuration(listener.instance.getDuration() / 2);
        }
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH,
            new AttributeModifier(uuid, "honey_dew", 6, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) > 100) {
            zeroCount(player, 0);
            float value = 1f;

            if (player.walkDistO == player.walkDist) {
                value += 1.5f;
            }

            for (MobEffectInstance instance : player.getActiveEffects()) {
                if (!instance.getEffect().isBeneficial()) {
                    value++;
                    break;
                }
            }

            player.heal(value);
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("honey_dew", 1));
        tooltips.add(CMLangUtil.getTranslatable("honey_dew", 2));
        return tooltips;
    }
}
