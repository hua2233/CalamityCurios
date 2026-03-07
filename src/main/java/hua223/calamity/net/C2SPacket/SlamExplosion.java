package hua223.calamity.net.C2SPacket;

import hua223.calamity.integration.curios.item.GravistarSabaton;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.network.NetworkEvent;

public class SlamExplosion extends C2S {
    private final float r;

    public SlamExplosion(float r) {
        this.r = r;
    }

    public SlamExplosion(FriendlyByteBuf buf) {
        this.r = buf.readFloat();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeFloat(r);
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();

        if (player != null) {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            player.level.explode(player, x, y, z, r, Explosion.BlockInteraction.NONE);
            GravistarSabaton.handleExplosionAffectedEntities(player, r, x, y, z);
        }
    }
}
