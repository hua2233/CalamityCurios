package hua223.calamity.integration.curios.item;

import com.google.common.collect.Multimap;
import hua223.calamity.integration.curios.SprintCurio;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.FatigueSync;
import hua223.calamity.register.attribute.CalamityAttributes;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.VariableAttributeModifier;
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

public class OldDukeScales extends SprintCurio implements ICuriosStorage {
    public OldDukeScales(Properties properties) {
        super(properties);
    }

    @Override
    protected void setAttributeModifiers(
        UUID uuid, ItemStack stack, Multimap<Attribute, AttributeModifier> modifier, LivingEntity equipped) {
        if (!equipped.level.isClientSide) getUUID(equipped)[0] = uuid;
        modifier.put(Attributes.MOVEMENT_SPEED,
            new VariableAttributeModifier(uuid, "old_duke_scales", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
        modifier.put(CalamityAttributes.INJURY_OFFSET.get(),
            new VariableAttributeModifier(uuid, "old_duke_scales", 0.1, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        NetMessages.sendToClient(new FatigueSync(true), player);
        getCount(player)[4] = 2;//初始化恢复量
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        NetMessages.sendToClient(new FatigueSync(false), player);
    }


    @Override
    public void preparingForSprint(ServerPlayer player) {
        float[] floats = getCount(player);
        if (floats[1] < 20) return;

        floats[0] = -100;
        floats[1] -= 20;
        NetMessages.sendToClient(new FatigueSync(floats[1]), player);
        if (floats[3] != 0 && floats[1] <= 0) {//检查标志， 进入疲惫状态
            AttributeInstance instance1 = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance1 == null) return;
            UUID uuid = getFirstUUID(player);
            VariableAttributeModifier.updateModifierInInstance(instance1, uuid, -0.3);

            AttributeInstance instance2 = player.getAttribute(CalamityAttributes.INJURY_OFFSET.get());
            if (instance2 == null) return;
            VariableAttributeModifier.updateModifierInInstance(instance2, uuid, 0);
            floats[3] = 0;//设置疲惫标志，这只在体力耗尽时被改变
        }

        floats[2] = 0;//设置标志，在体力减少时就应该恢复
    }

    @Override
    protected void onPlayerTick(Player player) {
        float[] floats = getCount(player);

        if (floats != null && floats[2] == 0 && ++floats[0] >= 60) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            floats[0] = 0;
            float heal = floats[4];
            floats[1] += serverPlayer.walkDistO == serverPlayer.walkDist ? heal : heal * 2;//站立不动时翻倍
            if (heal < 10) floats[4] += 2;//最大为10

            if (floats[1] >= 100) {//退出疲惫状态
                floats[1] = 100;
                floats[2] = 1;//重设体力标志
                floats[4] = 2;//初始化恢复量
                if (floats[3] == 0) {//检查疲惫标志，防止玩家在体力未耗尽时恢复至满导致的错误处理
                    floats[3] = 1;//重设疲惫标志
                    AttributeInstance instance1 = serverPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (instance1 == null) return;
                    UUID uuid = getFirstUUID(player);
                    VariableAttributeModifier.updateModifierInInstance(instance1, uuid, 0.1);

                    AttributeInstance instance2 = serverPlayer.getAttribute(CalamityAttributes.INJURY_OFFSET.get());
                    if (instance2 == null) return;
                    VariableAttributeModifier.updateModifierInInstance(instance2, uuid, 0.1);
                }
            }
            NetMessages.sendToClient(new FatigueSync(floats[1]), serverPlayer);
        }
    }

    @Override
    public boolean isAttachment() {
        return true;
    }

    @Override
    public boolean isEffectiveAttachment(ServerPlayer player) {
        return getCount(player)[3] == 0;
    }

    @Override
    public double getSpeedAmplifier() {
        return 0.5f;
    }

    @Override
    protected boolean startServerTick() {
        return true;
    }

    @Override
    public int getCountSize() {
        return 5;
    }

    @Override
    public boolean storageID() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("old_duke_scales", 1));
        tooltips.add(CMLangUtil.getTranslatable("old_duke_scales", 2));
        tooltips.add(CMLangUtil.getTranslatable("old_duke_scales", 3));
        tooltips.add(CMLangUtil.getTranslatable("old_duke_scales", 4));
        tooltips.add(CMLangUtil.getTranslatable("old_duke_scales", 5));
        tooltips.add(CMLangUtil.getTranslatable("old_duke_scales", 6));
        return tooltips;
    }
}
