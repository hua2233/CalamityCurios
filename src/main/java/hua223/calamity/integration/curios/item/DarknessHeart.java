package hua223.calamity.integration.curios.item;

import hua223.calamity.capability.CalamityCapProvider;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.render.hud.RageHud;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class DarknessHeart extends BaseCurio implements ICuriosStorage, IDataPackResponse {
    public DarknessHeart(Properties properties) {
        super(properties);
    }

    @Override
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        CalamityCapProvider.RAGE.getCapabilityFrom(player).ifPresent(rage -> rage.setAttenuation(false));
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        CalamityCapProvider.RAGE.getCapabilityFrom(player).ifPresent(rage -> rage.setAttenuation(true));
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    protected void onPlayerTick(Player player) {
        if (resetOrUpdate(player, 0, 20))
            CalamityCapProvider.RAGE.getCapabilityFrom(player).ifPresent(rage ->
                rage.addValue(2F, (ServerPlayer) player, this));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        RageHud.setRageProgress(tag.getFloat("value"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        CMLangUtil.batchColorTexts(tooltips, ChatFormatting.GOLD, "darkness_heart", 2, 3);
        tooltips.add(CMLangUtil.blankLine());
        tooltips.add(CMLangUtil.getTranslatable("darkness_heart", 1).withStyle(ChatFormatting.DARK_RED));
        return tooltips;
    }
}
