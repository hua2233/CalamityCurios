package hua223.calamity.capability;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.NetMessages;
import hua223.calamity.net.S2CPacket.CalamitySync;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.*;

public class CalamityCap implements BaseCap<CalamityCap> {
    private static final Set<UUID> CURSE_PLAYERS = new HashSet<>();
    private byte curseFlags;

    public CalamityCap() {
        curseFlags = 0;
    }

    public static boolean isInverted(CurseType curseType, ICapabilityProvider player) {
        Optional<CalamityCap> optional = CalamityCapProvider.CALAMITY.getCapabilityFrom(player).resolve();
        return optional.filter(calamityCap -> (calamityCap.curseFlags & 1 << curseType.bit) != 0).isPresent();
    }

    public static void curseInverted(CurseType curseType, ServerPlayer player) {
        if (isCalamity(player)) {
            CalamityCapProvider.CALAMITY.getCapabilityFrom(player).ifPresent(
                cap -> {
                    cap.curseFlags = (byte) (cap.curseFlags | 1 << curseType.bit);
                    NetMessages.sendToClient(new CalamitySync(curseType), player);
                });
        }
    }

    public static boolean notHasCalamity() {
        return CURSE_PLAYERS.isEmpty();
    }

    public static Set<UUID> getCalamityList() {
        return CURSE_PLAYERS;
    }

    public void syncData(ServerPlayer player) {
       CurseType[] types = Arrays.stream(CurseType.values()).filter(curseType -> curseType.reversed).toArray(CurseType[]::new);
       if (types.length != 0) NetMessages.sendToClient(new CalamitySync(types), player);
    }

    @OnlyIn(Dist.CLIENT)
    public static void setText(String[] types) {
        for (String type : types) {
            try {
                CurseType.valueOf(type).reversed = true;
            } catch (IllegalArgumentException e) {
                CalamityCurios.LOGGER.error("Unable to find the corresponding curse type: {}", type, e);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void reSet() {
        for (CurseType type : CurseType.values())
            type.reversed = false;
    }

    public static boolean isCalamity(LivingEntity player) {
        return CURSE_PLAYERS.contains(player.getUUID());
    }

    public static void setCalamity(LivingEntity player, boolean calamity) {
        if (calamity) CURSE_PLAYERS.add(player.getUUID());
        else CURSE_PLAYERS.remove(player.getUUID());
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putByte("curse", curseFlags);
    }

    @Override
    public void load(CompoundTag tag) {
        curseFlags = tag.getByte("curse");
    }

    @Override
    public void deathActivation(CalamityCap old, ServerPlayer _new) {
        CalamityCapProvider.CALAMITY.getCapabilityFrom(_new).ifPresent(
            cap -> cap.curseFlags = old.curseFlags);
    }

    public enum CurseType {
        SUNK(0),
        SULFUR_FIRE(1),
        SILVA(2),
        ABYSS(3),
        DESERT(4);

        private final byte bit;
        @OnlyIn(Dist.CLIENT)
        public boolean reversed;
        CurseType(int bit) {
            this.bit = (byte) bit;
        }
    }
}
