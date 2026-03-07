package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.network.chat.Component;
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

public class OceanShield extends BaseCurio implements ICuriosStorage {
    public OceanShield(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR,
            new VariableAttributeModifier(uuid, "ocean_shield", 2, AttributeModifier.Operation.ADDITION));
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) > 20) {
            var p = getPair(player);
            float[] count = p.getA();
            count[0] = 0;

            if (player.isInWater()) {
                if (count[1] == 0) {
                    VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.ARMOR), p.getB()[0], 6);
                    count[1] = 1;
                }
                player.heal(1f);
            } else if (count[1] == 1) {
                VariableAttributeModifier.updateModifierInInstance(player.getAttribute(Attributes.ARMOR), p.getB()[0], 2);
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
        return 2;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ocean_shield"));
        return tooltips;
    }
}
