package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.integration.curios.listeners.HurtListener;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.SpongeData;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
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

public class Sponge extends BaseCurio implements ICuriosStorage {
    public Sponge(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void setAttributeModifiers(UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.ARMOR, new VariableAttributeModifier(uuid, "sponge", 15, AttributeModifier.Operation.ADDITION));
        modifier.put(CalamityAttributes.INJURY_OFFSET.get(), new VariableAttributeModifier(uuid, "sponge", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        addCount(player, 2);
        NetMessages.sendToClient(new SpongeData().updateStart(true), player);
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        NetMessages.sendToClient(new SpongeData().updateStart(false), player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onLogOut(Player player) {
        if (player.isLocalPlayer())
            hua223.calamity.util.clientInfos.Sponge.closeSponge();
    }

    @ApplyEvent
    public final void onHurt(HurtListener listener) {
        var p = getPair(listener.player);
        float[] count = p.getA();

        count[2] = 1;
        count[1] = 180;
        if (count[3] == 0) {
            float hurt = listener.baseAmount;
            float absorbed = ICuriosStorage.getReducedValue(count, 0, hurt);

            if (absorbed == hurt) {
                listener.canceledEvent();
            } else if (absorbed > 0) {
                listener.floating -= absorbed;
            }

            float value = count[0];
            NetMessages.sendToClient(new SpongeData().updateValue(value), listener.player);
            if (value <= 0) {
                count[3] = 1;
                VariableAttributeModifier.updateModifierInInstance(listener.player.getAttribute(CalamityAttributes.INJURY_OFFSET.get()), p.getB()[0], 0);
                VariableAttributeModifier.updateModifierInInstance(listener.player.getAttribute(Attributes.ARMOR), p.getB()[0], 0);
            }
        }
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] count = getCount(player);

        if (count[2] == 1) {
            if (--count[1] <= 0) {
                count[0] += 5;
                if (count[0] >= 30) {
                    count[0] = 30;
                    count[2] = 0;
                    count[3] = 0;
                } else count[1] += 20;

                ServerPlayer server = (ServerPlayer) player;
                UUID uuid = getFirstUUID(server);
                VariableAttributeModifier.updateModifierInInstance(server.getAttribute(Attributes.ARMOR), uuid, 15);
                VariableAttributeModifier.updateModifierInInstance(server.getAttribute(CalamityAttributes.INJURY_OFFSET.get()), uuid, 0.1);
                NetMessages.sendToClient(new SpongeData().updateValue(count[0]), server);
            }
        }
    }

    @Override
    protected boolean startServerTick() {
        return true;
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
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("sponge", 1));
        tooltips.add(CMLangUtil.getTranslatable("sponge", 2));
        tooltips.add(CMLangUtil.getTranslatable("sponge", 3));
        tooltips.add(CMLangUtil.getTranslatable("sponge", 4));
        return tooltips;
    }
}
