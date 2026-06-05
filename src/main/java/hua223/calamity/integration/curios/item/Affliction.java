package hua223.calamity.integration.curios.item;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public class Affliction extends BaseCurio {
    private static short counter;

    public Affliction(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        if (counter == 0) equipHandler(player.getServer().getPlayerList().getPlayers());
        counter++;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        if (--counter == 0) unEquipHandler(player.getServer().getPlayerList().getPlayers());
    }

    @Override
    public void onLogOut(Player player) {
        //If the player exits, it should be considered as unEquip
        if (!player.isLocalPlayer())
            unEquipHandle((ServerPlayer) player, null);
    }

    @SuppressWarnings("ConstantConditions")
    private void equipHandler(List<ServerPlayer> players) {
        UUID id = UUID.nameUUIDFromBytes("affliction".getBytes());
        ImmutableCollection<Map.Entry<Attribute, AttributeModifier>> entries = ImmutableMultimap.of(
            Attributes.MAX_HEALTH, new AttributeModifier(id, "affliction", 0.1, AttributeModifier.Operation.MULTIPLY_BASE),
            CalamityAttributes.INJURY_OFFSET.get(), new AttributeModifier(id, "affliction", 0.07, AttributeModifier.Operation.ADDITION),
            CalamityAttributes.DAMAGE_UP.get(), new AttributeModifier(id, "affliction", 0.1, AttributeModifier.Operation.ADDITION),
            Attributes.ARMOR, new AttributeModifier(id, "affliction", 13, AttributeModifier.Operation.ADDITION),
            Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, "affliction", 6, AttributeModifier.Operation.ADDITION)).entries();

        for (ServerPlayer player : players)
            for (Map.Entry<Attribute, AttributeModifier> entry : entries)
                player.getAttribute(entry.getKey()).addTransientModifier(entry.getValue());
    }

    @SuppressWarnings("ConstantConditions")
    private void unEquipHandler(List<ServerPlayer> players) {
        UUID id = UUID.nameUUIDFromBytes("affliction".getBytes());

        Attribute[] attributes = {Attributes.MAX_HEALTH, CalamityAttributes.INJURY_OFFSET.get(),
            CalamityAttributes.DAMAGE_UP.get(), Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS};

        for (ServerPlayer player : players)
            for (Attribute attribute : attributes)
                player.getAttribute(attribute).removeModifier(id);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("affliction").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
