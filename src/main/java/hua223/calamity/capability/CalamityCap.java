package hua223.calamity.capability;

import hua223.calamity.integration.curios.item.Calamity;
import hua223.calamity.register.Items.CalamityItems;
import hua223.calamity.util.IDataPackResponse;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.*;

public class CalamityCap implements BaseCap<CalamityCap> {
    private static final Set<UUID> CURSE_PLAYERS = new ObjectOpenHashSet<>();
    private byte curseFlags;

    public CalamityCap() {
        curseFlags = 0;
    }

    public static boolean isInverted(CurseType curseType, ICapabilityProvider player) {
        Optional<CalamityCap> optional = CalamityCapProvider.CALAMITY.getCapabilityFrom(player).resolve();
        return optional.filter(calamityCap -> (calamityCap.curseFlags & 1 << curseType.getBit()) != 0).isPresent();
    }

    public static void curseInverted(CurseType curseType, ServerPlayer player, Calamity calamity) {
        if (isCalamity(player)) {
            CalamityCapProvider.CALAMITY.getCapabilityFrom(player).ifPresent(
                cap -> {
                    cap.curseFlags = (byte) (cap.curseFlags | 1 << curseType.getBit());
                    calamity.getPack().putByte(curseType.name(), (byte) 0);
                    calamity.sendToClient(player);
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
        IDataPackResponse response = (IDataPackResponse) CalamityItems.CALAMITY.get();
        CompoundTag tag = response.getPack();
        for (CurseType type : CurseType.values())
            if (type.reversed) tag.putByte(type.name(), (byte) 0);

       if (!tag.isEmpty()) response.sendToClient(player);
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
        SUNK,
        SULFUR_FIRE,
        SILVA,
        ABYSS,
        DESERT;

        @OnlyIn(Dist.CLIENT)
        public boolean reversed;
        CurseType() {}

        private byte getBit() {
            CurseType[] types = CurseType.values();
            for (int i = 0; i < types.length; i++)
                if (this == types[i]) return (byte) i;

            throw new IllegalStateException("Non-existent enumeration object!");
        }
    }
}
