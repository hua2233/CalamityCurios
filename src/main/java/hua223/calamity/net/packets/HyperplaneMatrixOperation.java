package hua223.calamity.net.packets;

import hua223.calamity.integration.curios.item.HyperplaneMatrix;
import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import hua223.calamity.register.items.CalamityItems;
import hua223.calamity.util.GlobalCuriosStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@CommunicationDirection(NetworkDirection.PLAY_TO_SERVER)
public class HyperplaneMatrixOperation extends DataPack {
    private final int flag;

    public HyperplaneMatrixOperation(int flag) {
        this.flag = flag;
    }

    public HyperplaneMatrixOperation(FriendlyByteBuf byteBuf) {
        this.flag = byteBuf.readVarInt();
    }

    @Override
    protected void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(flag);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        ServerLevel level = player.serverLevel();
        int absId = Math.abs(flag);
        switch (absId) {
            case 1, 6, 2 -> HyperplaneMatrix.setFlag(player, absId, flag);

            case 3 -> level.setDayTime(level.getDayTime() + (24000 - level.getDayTime() % 24000));
            case 4 -> {
                long time = level.getDayTime() % 24000;
                if (time < 6000) level.setDayTime(level.getDayTime() + 6000 - time);
                else level.setDayTime(level.getDayTime() + 6000 + 24000 - time);
            }
            case 5 -> {
                long time = level.getDayTime() % 24000;
                if (time < 12000) level.setDayTime(level.getDayTime() + 12000 - time);
                else level.setDayTime(level.getDayTime() + 12000 + 24000 - time);
            }
        }
    }
}
