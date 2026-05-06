package hua223.calamity.net.S2CPacket;

import hua223.calamity.register.keys.ClientInteraction;
import hua223.calamity.register.keys.IKeyDataPackResponse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

public class ApplyKeyEvent extends S2C {
    private final int key;
    private final boolean apply;

    public ApplyKeyEvent(int key, boolean apply) {
        this.key = key;
        this.apply = apply;
    }

    public ApplyKeyEvent(IKeyDataPackResponse response, boolean apply) {
        key = response.getKeyCode();
        this.apply = apply;
    }

    public ApplyKeyEvent(FriendlyByteBuf byteBuf) {
        apply = byteBuf.readBoolean();
        key = byteBuf.readVarInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(apply);
        byteBuf.writeVarInt(key);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ClientInteraction.applyOrDelete(key, apply);
    }
}
