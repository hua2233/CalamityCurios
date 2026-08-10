package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@CommunicationDirection(NetworkDirection.PLAY_TO_CLIENT)
public class ApplyKeyEvent extends DataPack {
    private final int key;
    private final boolean apply;

    public ApplyKeyEvent(int key, boolean apply) {
        this.key = key;
        this.apply = apply;
    }

    public ApplyKeyEvent(IKeyDataPackResponse response, boolean apply) {
        this(response.getKeyCode(), apply);
    }

    public ApplyKeyEvent(FriendlyByteBuf byteBuf) {
        this(byteBuf.readVarInt(), byteBuf.readBoolean());
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(key);
        byteBuf.writeBoolean(apply);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ClientInteraction.applyOrDelete(key, apply);
    }
}
