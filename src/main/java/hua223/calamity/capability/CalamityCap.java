package hua223.calamity.capability;

import hua223.calamity.events.LogoutRelease;
import hua223.calamity.integration.curios.item.Calamity;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.net.IDataPackResponse;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class CalamityCap implements BaseCap {
    private static int curseCount;
    private final ServerPlayer player;
    private boolean isCursePlayer;
    private byte curseFlags;

    public CalamityCap(ServerPlayer player) {
        this.player = player;
    }

    public void setCursePlayer(boolean cursePlayer) {
        if (isCursePlayer != cursePlayer) {
            isCursePlayer = cursePlayer;
            curseCount += (isCursePlayer ? 1 : -1);
        }
    }

    public boolean isCursePlayer() {
        return isCursePlayer;
    }

    public boolean isInverted(CurseType type) {
        return (curseFlags & 1 << type.getBit()) != 0;
    }

    public void curseInverted(CurseType curseType, Calamity calamity) {
        if (isCursePlayer) {
            curseFlags = (byte) (curseFlags | 1 << curseType.getBit());
            calamity.getPack().putByte(curseType.name(), curseFlags);
            calamity.sendToClient(player);
        }
    }

    public static boolean notHasCalamity() {
        return curseCount == 0;
    }

    public static int getCalamityPlayerCount() {
        return curseCount;
    }

    @SuppressWarnings("ConstantConditions")
    public List<ServerPlayer> getRestCalamity() {
        List<ServerPlayer> players = player.getServer().getPlayerList().getPlayers();
        players.removeIf(p -> p == player || !p.Calamity$Player.calamityCap.isCursePlayer);
        return players;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onClone(Player old, boolean isDeath) {
        curseFlags = old.Calamity$Player.calamityCap.curseFlags;
    }

    @Override
    public void syncData() {
        IDataPackResponse response = (IDataPackResponse) CalamityItems.CALAMITY.get();
        CompoundTag tag = response.getPack();
        for (CurseType type : CurseType.values())
            if (type.reversed) tag.putByte(type.name(), curseFlags);

        if (!tag.isEmpty()) response.sendToClient(player);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putByte("curse", curseFlags);
    }

    @Override
    public void load(CompoundTag tag) {
        curseFlags = tag.getByte("curse");
    }

    public enum CurseType {
        SUNK,
        SULFUR_FIRE,
        SILVA,
        ABYSS,
        DESERT;

        @OnlyIn(Dist.CLIENT)
        public boolean reversed;
        CurseType() {}

        private byte getBit() {
            return (byte) ordinal();
        }

        @OnlyIn(Dist.CLIENT)
        @LogoutRelease
        public static void reSet(LocalPlayer player) {
            for (CurseType type : values()) type.reversed = false;
        }
    }
}
