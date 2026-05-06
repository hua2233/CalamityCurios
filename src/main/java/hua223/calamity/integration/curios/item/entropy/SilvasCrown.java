package hua223.calamity.integration.curios.item.entropy;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.UUID;

public class SilvasCrown extends BaseCurio implements ICuriosStorage {
    public SilvasCrown(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        int priority = -1;
        AttributeModifier.Operation operation = null;
        UUID uuid = UUID.nameUUIDFromBytes("SilvasCrown".getBytes());
        for (AttributeInstance instance : player.getAttributes().attributes.values()) {
            for (AttributeModifier.Operation o : AttributeModifier.Operation.values()) {
                int value = instance.getModifiers(o).size();
                if (value != 0 && value > priority) {
                    priority = value;
                    operation = o;
                }
            }

            if (operation != null) {
                double amplifier = switch (operation) {
                    case ADDITION -> 5;
                    case MULTIPLY_BASE -> 0.1;
                    case MULTIPLY_TOTAL -> 0.05;
                };

                instance.addTransientModifier(new AttributeModifier(uuid, "SilvasCrown", amplifier, operation));
                operation = null;
                priority = -1;
            }
        }
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        UUID uuid = UUID.nameUUIDFromBytes("SilvasCrown".getBytes());
        for (AttributeInstance instance : player.getAttributes().attributes.values())
            instance.removeModifier(uuid);
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 20) && player.getHealth() < player.getMaxHealth())
            player.heal(player.level().getBiome(player.getOnPos()).is(Tags.Biomes.IS_LUSH) ? 4 : 2);
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("silvas_crown", 1).withStyle(ChatFormatting.YELLOW));
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("silvas_crown", 2).withStyle(ChatFormatting.GREEN));
        return tooltips;
    }
}
