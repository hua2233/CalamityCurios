package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

@SuppressWarnings({"deprecation", "ConstantConditions"})
@CommunicationDirection(NetworkDirection.PLAY_TO_CLIENT)
public class ReduceCooldown extends DataPack {
    private final Item item;
    private final int tick;

    public ReduceCooldown(Item item, int tick) {
        this.item = item;
        this.tick = tick;
    }

    public ReduceCooldown(FriendlyByteBuf buf) {
        item = buf.readById(BuiltInRegistries.ITEM);
        tick = buf.readVarInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeId(BuiltInRegistries.ITEM, item);
        byteBuf.writeVarInt(tick);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        Minecraft.getInstance().player.getCooldowns().calamity$ReduceCooldown(item, tick, null);
    }
}
