package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class SunWarBanner extends BaseCurio implements ICuriosStorage {
    public SunWarBanner(Properties properties) {
        super(properties);
    }

    public static int getAmplifier(int distance) {
        int amplifier = 6;
        if (distance > 100) {
            return amplifier;
        } else {
            int difference = ((distance / 20) + 1);
            return amplifier - difference;
        }
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (equipped.level.isClientSide) return;
        getUUID(equipped)[0] = uuid;
        VariableAttributeModifier var = new VariableAttributeModifier(uuid, "sun_war_banner", 0, AttributeModifier.Operation.MULTIPLY_BASE);
        modifier.put(Attributes.ATTACK_SPEED, var);
        modifier.put(Attributes.ATTACK_DAMAGE, var);
        modifier.put(CalamityAttributes.CLOSE_RANGE.get(), var);
    }

    private void updateAttribute(Player player, double amplifier) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        VariableAttributeModifier.getModifierInInstance(instance, getFirstUUID(player))
            .setBatchValue(amplifier, instance, player.getAttribute(Attributes.ATTACK_SPEED),
                player.getAttribute(CalamityAttributes.CLOSE_RANGE.get()));
    }

    private static List<LivingEntity> getNearbyEnemies(Player player) {
        double x = player.getX(), y = player.getY(), z = player.getZ();
        AABB scope = new AABB(x - 10, y - 5, z - 10,
            x + 10, y + 5, z + 10);

        return player.level.getEntitiesOfClass(LivingEntity.class, scope,
            entity -> entity instanceof Enemy && entity.isAlive() && !entity.isAlliedTo(player));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        getCount(player)[1] = 6;
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
    protected void onPlayerTick(Player player) {
        if (addCount(player, 0) >= 10) {
            float[] count = getCount(player);
            count[0] = 0;
            List<LivingEntity> mobs = getNearbyEnemies(player);

            float lastAmplifier = count[1];

            if (mobs.isEmpty()) {
                if (lastAmplifier > 0) {
                    updateAttribute(player, 0);
                    count[1] = 6;
                }
                return;
            }

            double distance = 150;
            for (LivingEntity mob : mobs) {
                distance = Math.min(distance, player.distanceToSqr(mob));
            }

            int amplifier = getAmplifier((int) distance);

            if (amplifier == lastAmplifier) {
                return;
            } else if (amplifier == 6) {
                updateAttribute(player, 0);
                count[1] = 6;
                return;
            }

            if (amplifier == 5) {
                updateAttribute(player, 0.2);
            } else {
                updateAttribute(player, amplifier * 0.04);
            }

            count[1] = amplifier;
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
            tooltips.add(CMLangUtil.getTranslatable("warbanner", 2));
            tooltips.add(CMLangUtil.getTranslatable("warbanner", 3));
        } else {
            tooltips.add(CMLangUtil.getTranslatable("warbanner", 1));
        }
        return tooltips;
    }
}
