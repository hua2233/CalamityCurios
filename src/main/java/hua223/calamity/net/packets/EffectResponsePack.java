package hua223.calamity.net.packets;

import hua223.calamity.net.CommunicationDirection;
import hua223.calamity.net.DataPack;
import hua223.calamity.net.IEffectDataResponse;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @see hua223.calamity.net.packets.ItemResponsePack
 */
@CommunicationDirection(NetworkDirection.PLAY_TO_CLIENT)
public class EffectResponsePack extends DataPack {
    private final MobEffect effect;
    private final CompoundTag stream;

    public EffectResponsePack(@NotNull MobEffect effect, @NotNull CompoundTag stream) {
        this.effect = effect;
        this.stream = stream;
    }


    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public EffectResponsePack(FriendlyByteBuf buf) {
        effect = buf.readById(BuiltInRegistries.MOB_EFFECT);
        stream = buf.readNbt();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void toBytes(FriendlyByteBuf byteBuf) {
        byteBuf.writeId(BuiltInRegistries.MOB_EFFECT, effect);
        byteBuf.writeNbt(stream);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handler(NetworkEvent.Context context) {
        ((IEffectDataResponse) effect).onClientResponse(stream);
    }
}
