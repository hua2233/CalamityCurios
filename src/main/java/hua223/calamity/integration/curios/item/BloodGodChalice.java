package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityDamageSource;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class BloodGodChalice extends BaseCurio implements ICuriosStorage {
    public BloodGodChalice(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        syncHealth(player);
        float amount = getCount(player)[0];
        if (amount > 0) player.hurt(CalamityDamageSource.getBloodGod(), amount);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(Attributes.MAX_HEALTH, new AttributeModifier(uuid, "blood_god_chalice", 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        float cache = listener.baseAmount - 4;
        if (cache > 0) {
            float[] count = getCount(listener.player);
            listener.event.setAmount(4);

            DelayRunnable.nextTickRun(() -> {
                if (listener.event.isCanceled()) return;

                count[0] += cache;
                if (count[0] <= 2) count[1] = 2;
                count[1] = count[0] / 7;
            });
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 2) == 20) {
            float[] count = getCount(player);
            count[2] = 0;
            if (count[0] <= 0) return;
            player.hurt(CalamityDamageSource.getBloodGod(), ICuriosStorage.getReducedValue(count, 0, count[1]));
            if (count[0] <= 0) {
                count[0] = 0;
                count[1] = 0;
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("blood_god_chalice", 2));
            tooltips.add(CMLangUtil.getTranslatable("blood_god_chalice", 3));
            tooltips.add(CMLangUtil.getTranslatable("blood_god_chalice", 4));
            tooltips.add(CMLangUtil.getTranslatable("blood_god_chalice", 5));
        } else tooltips.add(CMLangUtil.getTranslatable("blood_god_chalice", 1));
        return tooltips;
    }
}
