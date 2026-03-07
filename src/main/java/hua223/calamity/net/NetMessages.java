package hua223.calamity.net;

import hua223.calamity.main.CalamityCurios;
import hua223.calamity.net.C2SPacket.*;
import hua223.calamity.net.S2CPacket.*;
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
            .named(CalamityCurios.ModResource("main_channel"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

        registerS2CPacket(RageDataSync.class, RageDataSync::new);

        registerS2CPacket(CrystallizationData.class, CrystallizationData::new);

        registerC2SPacket(RageActive.class, RageActive::new);

        registerC2SPacket(Crystallization.class, Crystallization::new);

        registerS2CPacket(SpongeData.class, SpongeData::new);

        registerC2SPacket(AdrenalineActivate.class, AdrenalineActivate::new);

        registerS2CPacket(AdrenalineData.class, AdrenalineData::new);

        registerC2SPacket(ApplySprint.class, ApplySprint::new);

        registerS2CPacket(ApplyKeyEvent.class, ApplyKeyEvent::new);

        registerC2SPacket(SlamExplosion.class, SlamExplosion::new);

        registerC2SPacket(CurseEnchantmentPack.class, CurseEnchantmentPack::new);

        registerC2SPacket(EvilSpiritsC2S.class, EvilSpiritsC2S::new);

        registerC2SPacket(SpellTypeSync.class, SpellTypeSync::new);

        registerS2CPacket(PersistentCurseFontSync.class, PersistentCurseFontSync::new);

        registerC2SPacket(OpenEnchantGui.class, OpenEnchantGui::new);

        registerS2CPacket(FatigueDataSync.class, FatigueDataSync::new);

        registerC2SPacket(FlyInfinite.class, FlyInfinite::new);

        registerS2CPacket(ExcelsusHit.class, ExcelsusHit::new);

        registerS2CPacket(AtaraxiaHit.class, AtaraxiaHit::new);

        registerC2SPacket(ClientLongPressTrigger.class, ClientLongPressTrigger::new);

        registerS2CPacket(EnableTrippy.class, EnableTrippy::new);

        registerS2CPacket(PlayerFreeze.class, PlayerFreeze::new);

        registerS2CPacket(ClientSprint.class, ClientSprint::new);

        registerS2CPacket(PlayerJumpPower.class, PlayerJumpPower::new);

        registerS2CPacket(PlayerClimbable.class, PlayerClimbable::new);

        registerS2CPacket(PlayerAutoJump.class, PlayerAutoJump::new);

        registerS2CPacket(PlayerVerticalSpeed.class, PlayerVerticalSpeed::new);

        registerS2CPacket(FatigueSync.class, FatigueSync::new);

        registerS2CPacket(OmnisciencePerception.class, OmnisciencePerception::new);

        registerC2SPacket(SpectralTeleport.class, SpectralTeleport::new);

        registerS2CPacket(AstrErosionSync.class, AstrErosionSync::new);
    }

    public static <T extends S2C> void registerS2CPacket(Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        INSTANCE.messageBuilder(clazz, getPacketId(), PLAY_TO_CLIENT)
            .decoder(decoder)
            .encoder(T::toBytes)
            .consumerMainThread(T::processOnClient).add();
    }

    public static <T extends C2S> void registerC2SPacket(Class<T> clazz, Function<FriendlyByteBuf, T> decoder) {
        INSTANCE.messageBuilder(clazz, getPacketId(), PLAY_TO_SERVER)
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
