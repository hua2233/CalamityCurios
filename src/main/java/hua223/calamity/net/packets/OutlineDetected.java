package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@CommunicationDirection(NetworkDirection.PLAY_TO_CLIENT)
public class OutlineDetected extends DataPack {
    private final int id;

    public OutlineDetected(Entity projectile) {
        id = projectile.getId();
    }

    public OutlineDetected(FriendlyByteBuf buf) {
        id = buf.readVarInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeVarInt(id);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void handler(NetworkEvent.Context context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level.getEntity(id) instanceof Projectile projectile) projectile.calamity$Detected = true;
    }
}
