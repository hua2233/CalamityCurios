package hua223.calamity.integration.curios.item;

import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.VariableAttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.UUID;

public class Affliction extends BaseCurio {
    private static short counter;
    private static UUID id;

    public Affliction(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        if (counter == 0) {
            id = UUID.randomUUID();
            player.getServer().getPlayerList().getPlayers().forEach(this::equipHandler);
        }
        counter++;
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (--counter == 0) {
            player.getServer().getPlayerList().getPlayers().forEach(this::unEquipHandler);
            id = null;
        }
    }

    @Override
    public void onLogOut(Player player) {
        //If the player exits, it should be considered as unEquip
        if (!player.isLocalPlayer())
            unEquipHandle((ServerPlayer) player, null);
    }

    private void equipHandler(ServerPlayer player) {
        player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(
            new VariableAttributeModifier(id, "affliction", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));

        player.getAttribute(CalamityAttributes.INJURY_OFFSET.get()).addTransientModifier(
            new VariableAttributeModifier(id, "affliction", 0.07, AttributeModifier.Operation.ADDITION));

        player.getAttribute(CalamityAttributes.DAMAGE_UP.get()).addTransientModifier(
            new VariableAttributeModifier(id, "affliction", 0.1, AttributeModifier.Operation.ADDITION));

        player.getAttribute(Attributes.ARMOR).addTransientModifier(
            new VariableAttributeModifier(id, "affliction", 13, AttributeModifier.Operation.ADDITION));

        player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(
            new VariableAttributeModifier(id, "affliction", 6, AttributeModifier.Operation.ADDITION));
    }

    private void unEquipHandler(ServerPlayer player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        health.removeModifier(id);
        syncHealth(player);

        AttributeInstance injury = player.getAttribute(CalamityAttributes.INJURY_OFFSET.get());
        injury.removeModifier(id);

        AttributeInstance damage = player.getAttribute(CalamityAttributes.DAMAGE_UP.get());
        damage.removeModifier(id);

        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        armor.removeModifier(id);

        AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        toughness.removeModifier(id);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("affliction"));
        return tooltips;
    }
}
