package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.*;
import hua223.calamity.util.delaytask.DelayRunnable;
import net.minecraft.ChatFormatting;
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

@ConflictChain(value = Radiance.class, node = InfectedJewel.class)
public class InfectedJewel extends BaseCurio implements ICuriosStorage {
    public InfectedJewel(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level().isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(uuid, "infected_jewel", 4, AttributeModifier.Operation.ADDITION));
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
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);
        if (++count[0] % 20 == 0 && count[3] != 1) {
            int debuffCount =  CalamityHelp.getDebuffCount(player);
            if (debuffCount == count[1]) return;
            else count[1] = debuffCount;

            AttributeInstance instance = player.getAttribute(Attributes.ARMOR);
            VariableAttributeModifier modifier = (VariableAttributeModifier) instance.getModifier(getFirstUUID(player));

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

        if (count[0] > 100) {
            count[0] = 0;
            player.heal(count[2]);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "infected_jewel", 2, 3, 4, 5);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("infected_jewel", 1).withStyle(ChatFormatting.GREEN));
        return tooltips;
    }
}
