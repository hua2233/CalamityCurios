package hua223.calamity.net.C2SPacket;

import hua223.calamity.register.effects.CalamityEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.network.NetworkEvent;

public class EvilSpiritsC2S extends C2S {

    public EvilSpiritsC2S() {
    }

    public EvilSpiritsC2S(FriendlyByteBuf buf) {
    }

    @Override
    public void handler(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();

        if (player != null) {
            player.addEffect(new MobEffectInstance(CalamityEffects.GRUESOME_EVIL_SPIRITS.get(), 300));
        }
    }
}
