package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.client.gui.screens.Screen;
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

public class InfectedJewel extends BaseCurio implements ICuriosStorage {
    public InfectedJewel(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(uuid, "infected_bonus", 4, AttributeModifier.Operation.ADDITION));
    }

    private void attemptedApplyBonus(Player player, int debuffCount, float[] count) {
        if (count[3] == 1) return;

        if (debuffCount == count[1]) return;
        else count[1] = debuffCount;

        AttributeInstance instance = player.getAttribute(Attributes.ARMOR);
        VariableAttributeModifier modifier = VariableAttributeModifier.getModifierInInstance(instance, getFirstUUID(player));

        if (debuffCount > 0) {
            count[2] = 3f;
            int extra = (debuffCount - 1) * 2;
            modifier.setValue(12 + extra, instance);
        } else {
            count[3] = 1;
            count[2] = 1f;

            DelayRunnable.conditionsLoop(() -> {
                if (modifier.getAmount() - 2 > 4) {
                    modifier.addValue(-2, instance);
                    return false;
                } else {
                    modifier.setValue(4, instance);
                    count[3] = 0;
                    return true;
                }
            }, 10);
        }
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        float[] count = getCount(player);
        count[2] += 1;
    }

    @Override
    public int getCountSize() {
        return 4;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (++count[0] % 20 == 0) {
            attemptedApplyBonus(player, CalamityHelp.getDebuffCount(player), count);
        }

        if (count[0] > 100) {
            count[0] = 0;
            player.heal(count[2]);
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        if (Screen.hasShiftDown()) {
            tooltips.add(CMLangUtil.getTranslatable("infected_jewel", 2));
            tooltips.add(CMLangUtil.getTranslatable("infected_jewel", 3));
            tooltips.add(CMLangUtil.getTranslatable("infected_jewel", 4));
            tooltips.add(CMLangUtil.getTranslatable("infected_jewel", 5));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("infected_jewel", 1));
        }

        return tooltips;
    }
}
