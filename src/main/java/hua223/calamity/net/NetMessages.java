package hua223.calamity.net;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.packets.*;
import net.jodah.typetools.TypeResolver;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class NetMessages {
    private static SimpleChannel INSTANCE;

    public static void registerNetPack() {
        final String PROTOCOL_VERSION = "1.0";

        //initialization
        INSTANCE = NetworkRegistry.ChannelBuilder
            .named(CalamityCurios.ModResource("calamity_channel"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

        //Am I too lazy?...
        registerPack(ApplySprint::new, ApplyKeyEvent::new, CurseEnchantmentPack::new,
            SpellTypeSync::new, PersistentCurseFontSync::new, OpenEnchantGui::new,
            FatigueDataSync::new, ClientLongPressTrigger::new, DataPackActive::new,
            ItemResponsePack::new, ReduceCooldown::new, OutlineDetected::new,
            EffectSync::new);
    }

    @SafeVarargs
    private static <T extends DataPack> void registerPack(Function<FriendlyByteBuf, T>... decoders) {
        for (int i = 0; i < decoders.length; i++) {
            Class<T> pack = getPackType(decoders[i]);
            INSTANCE.messageBuilder(pack, i, pack.getAnnotation(CommunicationDirection.class).value())
                .decoder(decoders[i])
                .encoder(T::toBytes)
                .consumerMainThread(T::processOnSide).add();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getPackType(Function<FriendlyByteBuf, T> decoder) {
        Class<?> type = TypeResolver.resolveRawArguments(Function.class, decoder.getClass())[1];
        if (type != TypeResolver.Unknown.class) return (Class<T>) type;

        CalamityCurios.LOGGER.error("Failed to resolve data pack type for \"{}\"", decoder);
        throw new IllegalStateException("Failed to parse illegal packet types");
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
