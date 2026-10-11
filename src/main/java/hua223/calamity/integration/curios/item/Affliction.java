package hua223.calamity.integration.curios.item;

import hua223.calamity.events.LogoutRelease;
import hua223.calamity.integration.curios.BaseCurio;
import hua223.calamity.register.effects.CalamityEffects;
import hua223.calamity.register.effects.SurvivableEffectInstance;
import hua223.calamity.util.CMLangUtil;
import hua223.calamity.util.ICuriosStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

//Refactoring
public class Affliction extends BaseCurio implements ICuriosStorage {
    private static ServerPlayer blessingGiver;

    public Affliction(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void equipHandle(ServerPlayer player, ItemStack stack) {
        player.Calamity$Player.blessingGiver = true;
        if (blessingGiver == null || blessingGiver.isRemoved()) {
            blessingGiver = player;
            getCount(player)[0] = 29;
            onPlayerTick(player);
        }
    }

    @Override
    protected void unEquipHandle(ServerPlayer player, ItemStack stack) {
        shouldItContinueToExist(player);
    }

    @SuppressWarnings("ConstantConditions")
    private static void shouldItContinueToExist(ServerPlayer player) {
        player.Calamity$Player.blessingGiver = false;
        if (player == blessingGiver) {
            blessingGiver = null;
            List<ServerPlayer> players = player.getServer().getPlayerList().getPlayers();
            if (players.size() > 1) for (ServerPlayer serverPlayer : players)
                if (serverPlayer.Calamity$Player.blessingGiver) {
                    blessingGiver = serverPlayer;
                    return;
                }
        }
    }

    @LogoutRelease
    public static void onLogOut(ServerPlayer player) {
        if (blessingGiver != null) player.removeEffect(CalamityEffects.AFFLICTED.get());
        shouldItContinueToExist(player);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onPlayerTick(Player player) {
        if (blessingGiver != player) return;
        if (resetOrUpdate(player, 0, 30)) {
            MobEffect effect = CalamityEffects.AFFLICTED.get();
            for (ServerPlayer serverPlayer : player.getServer().getPlayerList().getPlayers()) {
                if (!serverPlayer.hasEffect(effect))
                    serverPlayer.addEffect(new SurvivableEffectInstance(effect, -1, 255, () -> blessingGiver != null));
            }
        }
    }

    @Override
    public int getCountSize() {
        return 1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Component> getSlotsTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.add(CMLangUtil.getTranslatable("affliction").withStyle(ChatFormatting.GOLD));
        return tooltips;
    }
}
