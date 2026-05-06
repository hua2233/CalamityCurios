package hua223.calamity.net.S2CPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class OutlineDetected extends S2C {
    private final boolean chest;
    private int id;
    private BlockPos pos;

    public OutlineDetected(Entity projectile) {
        chest = false;
        id = projectile.getId();
    }

    public OutlineDetected(BlockPos lootContainerPos) {
        chest = true;
        this.pos = lootContainerPos;
    }

    public OutlineDetected(FriendlyByteBuf buf) {
        chest = buf.readByte() == 0;
        if (chest) pos = new BlockPos(buf.readVarInt(),
            buf.readVarInt(), buf.readVarInt());
        else id = buf.readVarInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        if (chest) {
            byteBuf.writeByte(0);
            byteBuf.writeVarInt(pos.getX());
            byteBuf.writeVarInt(pos.getY());
            byteBuf.writeVarInt(pos.getZ());
        } else {
            byteBuf.writeByte(1);
            byteBuf.writeVarInt(id);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("ConstantConditions")
    public void handler(NetworkEvent.Context context) {
        ClientLevel level = Minecraft.getInstance().level;
        if (chest) {
            if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity entity)
                entity.calamity$Detected = false;
        } else if (level.getEntity(id) instanceof Projectile projectile)
            projectile.calamity$Detected = true;
    }
}
