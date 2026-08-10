package hua223.calamity.net;

import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.lang.invoke.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;

public class NetMessages {
    private static SimpleChannel INSTANCE;

    public static void registerNetPack(AnnotationProcessor annotationProcessor) {
        ArrayList<Tuple<Class<?>, NetworkDirection>> packs = new ArrayList<>();
        annotationProcessor.addStartProcessingEntries(CommunicationDirection.class, processor ->
            packs.add(new Tuple<>(processor.getDataClass(), NetworkDirection.valueOf(
                ((ModAnnotation.EnumHolder) processor.getAnnotationData().annotationData().get("value")).getValue()))));
        annotationProcessor.addPostProcessor(() -> NetMessages.registerNetPack(packs));
    }

    @SuppressWarnings("unchecked")
    private static void registerNetPack(ArrayList<Tuple<Class<?>, NetworkDirection>> communicationClass) {
        final String PROTOCOL_VERSION = "1.0";

        //initialization
        INSTANCE = NetworkRegistry.ChannelBuilder
            .named(CalamityCurios.ModResource("calamity_channel"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

        MethodType constructorType = MethodType.methodType(void.class, FriendlyByteBuf.class);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType factoryType = MethodType.methodType(Function.class);
        MethodType interfaceType = MethodType.methodType(Object.class, Object.class);

        final byte[] indexSet = new byte[256];
        //There must be a public constructor that only accepts FriendlyByteBuf, otherwise NoSuchMethodException will be thrown
        try {
            //Ensure consistency of linear detection IDs
            communicationClass.sort(Comparator.comparingInt(v -> v.getA().getSimpleName().hashCode()));
            for (Tuple<Class<?>, NetworkDirection> entry : communicationClass) {
                Class<?> clazz = entry.getA();
                MethodHandle ctorHandle = lookup.findConstructor(clazz, constructorType);
                registerPack(indexSet, clazz, (Function<FriendlyByteBuf, DataPack>) LambdaMetafactory.metafactory(
                    lookup, "apply", factoryType, interfaceType, ctorHandle,
                    MethodType.methodType(clazz, FriendlyByteBuf.class)).getTarget().invokeExact(), entry.getB());
            }
        } catch (Throwable e) {
            CalamityCurios.LOGGER.error("Construct packet classes for illegal communication!", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataPack> void registerPack(byte[] indexSet, Class<?> tClass, Function<FriendlyByteBuf, T> decoders, NetworkDirection direction) {
        final int initial = tClass.getSimpleName().hashCode() & 255;
        int id = initial;
        while (true) {
            if (indexSet[id] == 0) {
                indexSet[id] = 1;
                break;
            } else {
                if (++id == indexSet.length) id = 0;
                else if(id == initial) throw new UnsupportedOperationException("This channel has reached the maximum limit for processing data packets");
            }
        }

        INSTANCE.messageBuilder((Class<T>) tClass, id, direction)
            .decoder(decoders)
            .encoder(T::toBytes)
            .consumerMainThread(T::processOnSide).add();
    }

//    @SuppressWarnings("unchecked")
//    private static <T> Class<T> getPackType(Function<FriendlyByteBuf, T> decoder) {
//        Class<?> type = TypeResolver.resolveRawArguments(Function.class, decoder.getClass())[1];
//        if (type != TypeResolver.Unknown.class) return (Class<T>) type;
//
//        CalamityCurios.LOGGER.error("Failed to resolve data pack type for \"{}\"", decoder);
//        throw new IllegalStateException("Failed to parse illegal packet types");
//    }

    public static <MSG extends DataPack> void sendToServer(MSG messages) {
        INSTANCE.sendToServer(messages);
    }

    public static <MSG extends DataPack> void sendToClient(MSG messages, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), messages);
    }

    public static <MSG extends DataPack> void sendToAllClient(MSG messages) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), messages);
    }
}
