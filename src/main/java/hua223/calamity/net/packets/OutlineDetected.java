package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@CommunicationDirection(NetworkDirection.PLAY_TO_CLIENT)
public class OutlineDetected extends DataPack {
    private int id = -1;
    private BlockPos pos;

    public OutlineDetected(Entity projectile) {
        id = projectile.getId();
    }

    public OutlineDetected(BlockPos lootContainerPos) {
        this.pos = lootContainerPos;
    }

    public OutlineDetected(FriendlyByteBuf buf) {
        id = buf.readVarInt();
        if (id < 0) {
            pos = new BlockPos(buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt());
        }
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        if (id < 0) {
            byteBuf.writeVarInt(pos.getX());
            byteBuf.writeVarInt(pos.getY());
            byteBuf.writeVarInt(pos.getZ());
        } else byteBuf.writeVarInt(id);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void handler(NetworkEvent.Context context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (id < 0) {
            if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity entity)
                entity.calamity$Detected = false;
        } else if (level.getEntity(id) instanceof Projectile projectile)
            projectile.calamity$Detected = true;
    }
}
