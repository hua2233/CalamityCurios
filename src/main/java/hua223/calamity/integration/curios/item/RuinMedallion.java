package hua223.calamity.integration.curios.item;

import hua223.calamity.events.ApplyEvent;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.events.listeners.CriticalHitCheckListener;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.CalamityHelp;
import hua223.calamity.util.ConflictChain;
import hua223.calamity.util.IDataPackResponse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@ConflictChain(value = DarkMatterSheath.class)
public class RuinMedallion extends BaseCurio implements IDataPackResponse {
    public RuinMedallion(Properties properties) {
        super(properties);
    }

    @Override
    public void equipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.canSprintingHit = true;
        getPack().putBoolean("flag", true);
        sendToClient(player);
    }

    @Override
    public void unEquipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.canSprintingHit = false;
        getPack().putBoolean("flag", false);
        sendToClient(player);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onClientResponse(CompoundTag tag) {
        CalamityHelp.getClientCalamity().canSprintingHit = tag.getBoolean("flag");
    }

    @ApplyEvent
    public final void onCriticalCheck(CriticalHitCheckListener listener) {
        listener.applyAmplifier(0.06f);
        listener.probability += 0.06f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("ruin_medallion", 1));
        tooltips.add(CMLangUtil.getTranslatable("ruin_medallion", 2));
        return tooltips;
    }
}
