package hua223.calamity.net;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.C2SPacket.*;
import hua223.calamity.net.S2CPacket.*;
import net.jodah.typetools.TypeResolver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public class NetMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int getPacketId() {
        return packetId++;
    }

    public static void registerNetPack() {
        final String PROTOCOL_VERSION = "1.0";

        INSTANCE = NetworkRegistry.ChannelBuilder
            .named(CalamityCurios.ModResource("calamity_channel"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

        registerC2SPacket(ApplySprint::new);

        registerS2CPacket(ApplyKeyEvent::new);

        registerC2SPacket(CurseEnchantmentPack::new);

        registerC2SPacket(SpellTypeSync::new);

        registerS2CPacket(PersistentCurseFontSync::new);

        registerC2SPacket(OpenEnchantGui::new);

        registerS2CPacket(FatigueDataSync::new);

        registerC2SPacket(ClientLongPressTrigger::new);

        registerS2CPacket(PlayerFreeze::new);

        registerC2SPacket(DataPackActive::new);

        registerS2CPacket(ItemResponsePack::new);

        registerS2CPacket(ReduceCooldown::new);

        registerS2CPacket(EffectSync::new);

        registerS2CPacket(OutlineDetected::new);
    }

    public static <T extends S2C> void registerS2CPacket(Function<FriendlyByteBuf, T> decoder) {
        INSTANCE.messageBuilder(getPackType(decoder), getPacketId(), PLAY_TO_CLIENT)
            .decoder(decoder)
            .encoder(T::toBytes)
            .consumerMainThread(T::processOnClient).add();
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getPackType(Function<FriendlyByteBuf, T> decoder) {
        Class<?> type = TypeResolver.resolveRawArguments(Function.class, decoder.getClass())[1];
        if (type != TypeResolver.Unknown.class) return (Class<T>) type;

        CalamityCurios.LOGGER.error("Failed to resolve data pack type for \"{}\"", decoder);
        throw new IllegalStateException("Failed to parse illegal packet types");
    }

    public static <T extends C2S> void registerC2SPacket(Function<FriendlyByteBuf, T> decoder) {
        INSTANCE.messageBuilder(getPackType(decoder), getPacketId(), PLAY_TO_SERVER)
            .decoder(decoder)
            .encoder(T::toBytes)
            .consumerMainThread(T::processOnServer).add();
    }

    public static <MSG> void sendToServer(MSG messages) {
        INSTANCE.sendToServer(messages);
    }

    public static <MSG> void sendToClient(MSG messages, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), messages);
    }

    public static <MSG> void sendToAllClient(MSG messages) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), messages);
    }
}
