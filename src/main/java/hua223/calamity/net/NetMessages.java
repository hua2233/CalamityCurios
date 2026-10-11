package hua223.calamity.net;

import hua223.calamity.main.AnnotationProcessor;
import hua223.calamity.main.CalamityCurios;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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
        annotationProcessor.addPostProcessor(() -> NetMessages.registerNetPack(annotationProcessor.lookup, packs));
    }

    @SuppressWarnings("unchecked")
    private static void registerNetPack(MethodHandles.Lookup lookup, ArrayList<Tuple<Class<?>, NetworkDirection>> communicationClass) {
        //Ensure consistency of linear detection IDs
        communicationClass.sort(Comparator.comparingInt(pack -> pack.getA().getSimpleName().hashCode()));
        long versionNumber = 0;
        for (Tuple<Class<?>, NetworkDirection> aClass : communicationClass)
            versionNumber += aClass.getA().getSimpleName().hashCode();

        //compression
        final String CALAMITY_PROXY_PROTOCOL_VERSION = Integer.toString(Long.toString(versionNumber).hashCode());
        //initialization
        INSTANCE = NetworkRegistry.ChannelBuilder
            .named(CalamityCurios.ModResource("calamity_channel"))
            .networkProtocolVersion(() -> CALAMITY_PROXY_PROTOCOL_VERSION)
            .clientAcceptedVersions(CALAMITY_PROXY_PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(CALAMITY_PROXY_PROTOCOL_VERSION::equals)
            .simpleChannel();

        final byte[] indexSet = new byte[256];
        MethodType factoryType = MethodType.methodType(Function.class);
        MethodType interfaceType = MethodType.methodType(Object.class, Object.class);
        MethodType constructorType = MethodType.methodType(void.class, FriendlyByteBuf.class);
        //There must be a public constructor that only accepts FriendlyByteBuf, otherwise NoSuchMethodException will be thrown
        try {
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
        //The hashCode of characters is determined by the Java specification,
        //and they should be exactly the same on the same version of JVM, regardless of where they run
        //The network channels provided by Forge can only register up to 256,
        //after which the IDs will be overwritten. A simple linear probe is used to ensure that their IDs are exactly the same in different operating environments
        final int initial = tClass.getSimpleName().hashCode() & 255;
        int id = initial;
        while (true) {
            if (indexSet[id] == 0) {
                indexSet[id] = 1;
                break;
            } else if (++id == indexSet.length) id = 0;
            else if(id == initial) throw new IllegalStateException("This channel has reached the maximum limit for processing data packets");
        }

        INSTANCE.messageBuilder((Class<T>) tClass, id, direction)
            .decoder(decoders)
            .encoder(T::toBytes)
            .consumerMainThread(T::processOnSide).add();
    }

    public static <MSG extends DataPack> void sendToServer(MSG messages) {
        INSTANCE.sendToServer(messages);
    }

    public static <MSG extends DataPack> void sendToClient(MSG messages, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), messages);
    }

    public static <MSG extends DataPack> void sendToTracking(Entity entity, MSG message) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public static <MSG extends DataPack> void sendToTrackingAndSelf(ServerPlayer player, MSG message) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), message);
        sendToClient(message, player);
    }

    public static <MSG extends DataPack> void sendToAllClient(MSG messages) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), messages);
    }
}
