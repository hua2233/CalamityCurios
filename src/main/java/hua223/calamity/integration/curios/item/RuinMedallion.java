package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.net.IDataPackResponse;
import hua223.calamity.util.CalamityPlayer;
import hua223.calamity.util.CurioRepel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@CurioRepel(DarkMatterSheath.class)
public class RuinMedallion extends BaseCurio implements IDataPackResponse {
    public RuinMedallion(Properties properties) {
        super(properties);
    }

    @Override
    public void equipHandle(ServerPlayer player, ItemStack stack) {
        sprintingHit(player.Calamity$Player, true);
    }

    static void sprintingHit(CalamityPlayer player, boolean can) {
        IDataPackResponse response = (IDataPackResponse) CalamityItems.RUIN_MEDALLION.get();
        player.canSprintingHit = can;
        response.getPack().putBoolean("flag", can);
        response.sendToClient(player.getPlayer());
    }

    @Override
    public void unEquipHandle(ServerPlayer player, ItemStack stack) {
        sprintingHit(player.Calamity$Player, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().canSprintingHit = tag.getBoolean("flag");
    }

    @ApplyEvent
    public final void onCriticalCheck(CriticalHitCheckListener listener) {
        listener.applyAmplifier(0.08f);
        listener.probability += 0.08f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ruin_medallion", 1));
        tooltips.add(CMLangUtil.getTranslatable("ruin_medallion", 2));
        return tooltips;
    }
}
