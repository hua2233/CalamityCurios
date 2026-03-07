package hua223.calamity.net.S2CPacket;

import hua223.calamity.integration.curios.SprintCurio;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class ClientSprint extends S2C {
    private final boolean isSprint;
    private final int time;
    private final double sprintSpeed;

    public ClientSprint(int time, double sprintSpeed) {
        this.time = time;
        this.sprintSpeed = sprintSpeed;
        isSprint = true;
    }

    public static ClientSprint stopSprinting() {
        return new ClientSprint(0, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public ClientSprint(FriendlyByteBuf buf) {
        isSprint = buf.readBoolean();
        time = isSprint ? buf.readVarInt() : 0;
        sprintSpeed = isSprint ? buf.readDouble() : 0;
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeBoolean(isSprint);
        if (isSprint) {
            byteBuf.writeVarInt(time);
            byteBuf.writeDouble(sprintSpeed);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        if (isSprint) SprintCurio.applyClientSprinting(time, sprintSpeed);
        else SprintCurio.stopClientSprint();
    }
}
