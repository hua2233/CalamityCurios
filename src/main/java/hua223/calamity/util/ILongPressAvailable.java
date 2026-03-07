package hua223.calamity.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ILongPressAvailable {
    void onServerResponse(ServerPlayer player, ItemStack stack);

    @OnlyIn(Dist.CLIENT)
    default void onClientResponse(LocalPlayer player, ItemStack stack) {}

    @OnlyIn(Dist.CLIENT)
    boolean isResponseTime(LocalPlayer player, int tick);
}
