package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.PlayerServantsManager;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.ChatFormatting;
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

public class DarkSunRing extends BaseCurio implements ICuriosStorage {
    public DarkSunRing(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        modifier.put(CalamityAttributes.DAMAGE_UP.get(),
            new AttributeModifier(uuid, "dark_sun_ring", 0.12, AttributeModifier.Operation.MULTIPLY_BASE));

        if (!equipped.level.isClientSide) getUUID(equipped)[1] = uuid;
        modifier.put(Attributes.ARMOR,
            new VariableAttributeModifier(uuid, "dark_sun_ring", 8, AttributeModifier.Operation.ADDITION));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        float[] count = getCount(player);
        if (player.level.getDayTime() > 14000) count[1] = 1;
        update(player, count);

        PlayerServantsManager.loadPlayerServantsEntity(player, entity -> {
            PlayerServantsManager.changeAttribute(entity, Attributes.MAX_HEALTH, 1, AttributeModifier.Operation.MULTIPLY_BASE);
            PlayerServantsManager.changeAttribute(entity, Attributes.ATTACK_DAMAGE, 0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        });
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        PlayerServantsManager.removePlayerServantsEntity(player, entity -> {
            PlayerServantsManager.changeAttribute(entity, Attributes.MAX_HEALTH, -1, AttributeModifier.Operation.MULTIPLY_BASE);
            PlayerServantsManager.changeAttribute(entity, Attributes.ATTACK_DAMAGE, -0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        });
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);

        if (count[0]++ == 20) {
            count[0] = 0;
            update(player, count);
        }
    }

    private void update(Player player, float[] count) {
        float heal = 0.5f;
        long time = player.level.getDayTime();
        if (time < 14000) {
            heal += 2;
            if (count[1] == 0) {
                VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.ARMOR), getUUID(player)[1], 0);
                count[1] = 1;
            }

            if (player.level.isRaining()) {
                heal += 1f;
                VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.ARMOR), getUUID(player)[1], 10);
                count[2] = 1;
            } else if (count[2] == 1) {
                VariableAttributeModifier.updateModifierInInstance(
                    player.getAttribute(Attributes.ARMOR), getUUID(player)[1], 0);
                count[2] = 0;
            }

        } else if (count[1] == 1) {
            VariableAttributeModifier.updateModifierInInstance(
                player.getAttribute(Attributes.ARMOR), getUUID(player)[1], 20);
            count[1] = 0;
            count[2] = 0;
        }

        if (player.getHealth() < player.getMaxHealth()) player.heal(heal);
    }

    @Override
    public int getCountSize() {
        return 3;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("dark_sun_ring", 1));
        tooltips.add(CMLangUtil.getTranslatable("dark_sun_ring", 2));
        tooltips.add(CMLangUtil.getTranslatable("dark_sun_ring", 3));
        tooltips.add(CMLangUtil.getTranslatable("dark_sun_ring", 4));
        tooltips.add(CMLangUtil.getTranslatable("dark_sun_ring", 5).withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
